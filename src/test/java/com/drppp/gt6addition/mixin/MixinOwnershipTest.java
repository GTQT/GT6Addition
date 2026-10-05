package com.drppp.gt6addition.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** Reads annotation bytecode without loading optional external target classes. */
class MixinOwnershipTest {
    private static final String MIXIN = "Lorg/spongepowered/asm/mixin/Mixin;";
    private static final String OWN_PACKAGE = "com.drppp.gt6addition.";
    private static final String MIXIN_PATH = "com/drppp/gt6addition/mixin";

    @Test
    void configuredMixinsOnlyTargetExternalClasses() throws Exception {
        for (String name : configuredMixins()) {
            try (InputStream input = resource(name.replace('.', '/') + ".class")) {
                List<String> targets = targets(new ClassReader(input));
                assertFalse(targets.isEmpty(), "Missing @Mixin targets: " + name);
                assertExternal(targets);
            }
        }
    }

    @Test
    void compiledMixinsAreNotLeftOutsideTheAuditedConfigurations() throws Exception {
        Set<String> configured = configuredMixins();
        String classPath = configured.iterator().next().replace('.', '/') + ".class";
        URL classFile = getClass().getClassLoader().getResource(classPath);
        assertNotNull(classFile);
        assertEquals("file", classFile.getProtocol(), "Expected Gradle main classes directory");
        Path root = Paths.get(classFile.toURI());
        for (String ignored : classPath.substring(MIXIN_PATH.length() + 1).split("/")) root = root.getParent();
        final Path mixinRoot = root;
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path path : (Iterable<Path>) paths.filter(p -> p.toString().endsWith(".class"))::iterator) {
                List<String> targets = targets(new ClassReader(Files.readAllBytes(path)));
                if (targets.isEmpty()) continue;
                String name = (MIXIN_PATH + "/" + mixinRoot.relativize(path)).replace('\\', '/');
                name = name.substring(0, name.length() - ".class".length()).replace('/', '.');
                assertTrue(configured.contains(name), "Unconfigured Mixin: " + name);
                assertExternal(targets);
            }
        }
    }

    @Test
    void guardRejectsOwnClassLiteralTargets() {
        List<String> targets = targets(new ClassReader(fixture("value",
                Type.getObjectType("com/drppp/gt6addition/common/OwnMachine"))));
        assertEquals(1, targets.size());
        assertThrows(AssertionError.class, () -> assertExternal(targets));
    }

    @Test
    void guardRejectsOwnStringTargetsIncludingInternalNames() {
        for (String target : new String[]{"com.drppp.gt6addition.common.OwnMachine",
                "com/drppp/gt6addition/common/OwnMachine"}) {
            List<String> targets = targets(new ClassReader(fixture("targets", target)));
            assertEquals(1, targets.size());
            assertThrows(AssertionError.class, () -> assertExternal(targets));
        }
    }

    private Set<String> configuredMixins() throws Exception {
        Set<String> names = new HashSet<>();
        for (String config : new String[]{"mixins.gt6addition_early.json", "mixins.gt6addition_late.json"}) {
            try (InputStreamReader reader = new InputStreamReader(resource(config), StandardCharsets.UTF_8)) {
                JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
                String prefix = json.get("package").getAsString() + ".";
                for (String side : new String[]{"mixins", "client", "server"}) {
                    JsonArray entries = json.getAsJsonArray(side);
                    if (entries == null) continue;
                    for (JsonElement entry : entries) {
                        String name = prefix + entry.getAsString();
                        assertTrue(names.add(name), "Duplicate Mixin entry: " + name);
                    }
                }
            }
        }
        assertFalse(names.isEmpty());
        return names;
    }

    private InputStream resource(String path) {
        InputStream input = getClass().getClassLoader().getResourceAsStream(path);
        assertNotNull(input, "Missing resource: " + path);
        return input;
    }

    private static List<String> targets(ClassReader reader) {
        List<String> targets = new ArrayList<>();
        reader.accept(new ClassVisitor(Opcodes.ASM5) {
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                if (!MIXIN.equals(descriptor)) return null;
                return new AnnotationVisitor(Opcodes.ASM5) {
                    @Override
                    public AnnotationVisitor visitArray(String name) {
                        if (!"value".equals(name) && !"targets".equals(name)) return null;
                        return new AnnotationVisitor(Opcodes.ASM5) {
                            @Override
                            public void visit(String ignored, Object value) {
                                targets.add(value instanceof Type ? ((Type) value).getClassName() : (String) value);
                            }
                        };
                    }
                };
            }
        }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        return targets;
    }

    private static void assertExternal(List<String> targets) {
        for (String target : targets) {
            assertFalse(target.replace('/', '.').startsWith(OWN_PACKAGE),
                    "Modify owned source directly instead of injecting it: " + target);
        }
    }

    private static byte[] fixture(String field, Object target) {
        ClassWriter writer = new ClassWriter(0);
        writer.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, "audit/Fixture", null, "java/lang/Object", null);
        AnnotationVisitor annotation = writer.visitAnnotation(MIXIN, false);
        AnnotationVisitor array = annotation.visitArray(field);
        array.visit(null, target);
        array.visitEnd();
        annotation.visitEnd();
        writer.visitEnd();
        return writer.toByteArray();
    }
}

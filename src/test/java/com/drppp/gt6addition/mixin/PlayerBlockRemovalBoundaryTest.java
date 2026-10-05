package com.drppp.gt6addition.mixin;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Checks the actual Forge-patched Minecraft bytecode, without starting a world. */
class PlayerBlockRemovalBoundaryTest {
    private static final String MANAGER = "net/minecraft/server/management/PlayerInteractionManager";
    private static final String REMOVAL = "(Lnet/minecraft/util/math/BlockPos;Z)Z";
    private static final String PLAYER_REMOVAL = "(Lnet/minecraft/block/state/IBlockState;" +
            "Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;" +
            "Lnet/minecraft/entity/player/EntityPlayer;Z)Z";
    private static final String TARGET = "Lnet/minecraft/block/Block;removedByPlayer" + PLAYER_REMOVAL;

    @Test
    void forgeRemovalOverloadContainsExactlyOneRedirectTarget() throws Exception {
        int[] matches = {0};
        read(MANAGER, new ClassVisitor(Opcodes.ASM5) {
            @Override public MethodVisitor visitMethod(int access, String name, String descriptor,
                    String signature, String[] exceptions) {
                if (!"removeBlock".equals(name) || !REMOVAL.equals(descriptor)) return null;
                return new MethodVisitor(Opcodes.ASM5) {
                    @Override public void visitMethodInsn(int opcode, String owner, String method,
                            String desc, boolean isInterface) {
                        if ("net/minecraft/block/Block".equals(owner) && "removedByPlayer".equals(method) &&
                                PLAYER_REMOVAL.equals(desc)) matches[0]++;
                    }
                };
            }
        });
        assertEquals(1, matches[0]);
    }

    @Test
    void vanillaRemovalOverloadDelegatesToTheSameForgeBoundary() throws Exception {
        int[] matches = {0};
        read(MANAGER, new ClassVisitor(Opcodes.ASM5) {
            @Override public MethodVisitor visitMethod(int access, String name, String descriptor,
                    String signature, String[] exceptions) {
                if (!"removeBlock".equals(name) || !"(Lnet/minecraft/util/math/BlockPos;)Z".equals(descriptor)) return null;
                return new MethodVisitor(Opcodes.ASM5) {
                    @Override public void visitMethodInsn(int opcode, String owner, String method,
                            String desc, boolean isInterface) {
                        if (MANAGER.equals(owner) && "removeBlock".equals(method) && REMOVAL.equals(desc)) matches[0]++;
                    }
                };
            }
        });
        assertEquals(1, matches[0]);
    }

    @Test
    void redirectUsesExactUnmappedForgeDescriptorsAndRequiresAMatch() throws Exception {
        List<String> methods = new ArrayList<>();
        List<String> targets = new ArrayList<>();
        List<Boolean> remaps = new ArrayList<>();
        List<Integer> required = new ArrayList<>();
        read("com/drppp/gt6addition/mixin/minecraft/PlayerBlockRemovalMixin", new ClassVisitor(Opcodes.ASM5) {
            @Override public MethodVisitor visitMethod(int access, String name, String descriptor,
                    String signature, String[] exceptions) {
                return new MethodVisitor(Opcodes.ASM5) {
                    @Override public AnnotationVisitor visitAnnotation(String annotation, boolean visible) {
                        if (!"Lorg/spongepowered/asm/mixin/injection/Redirect;".equals(annotation)) return null;
                        return new AnnotationVisitor(Opcodes.ASM5) {
                            @Override public void visit(String field, Object value) {
                                if ("remap".equals(field)) remaps.add((Boolean) value);
                                if ("require".equals(field)) required.add((Integer) value);
                            }
                            @Override public AnnotationVisitor visitArray(String field) {
                                if (!"method".equals(field)) return null;
                                return new AnnotationVisitor(Opcodes.ASM5) {
                                    @Override public void visit(String ignored, Object value) { methods.add((String) value); }
                                };
                            }
                            @Override public AnnotationVisitor visitAnnotation(String field, String type) {
                                if (!"at".equals(field)) return null;
                                return new AnnotationVisitor(Opcodes.ASM5) {
                                    @Override public void visit(String key, Object value) {
                                        if ("target".equals(key)) targets.add((String) value);
                                        if ("remap".equals(key)) remaps.add((Boolean) value);
                                    }
                                };
                            }
                        };
                    }
                };
            }
        });
        assertEquals(java.util.Collections.singletonList("removeBlock" + REMOVAL), methods);
        assertEquals(java.util.Collections.singletonList(TARGET), targets);
        assertEquals(java.util.Arrays.asList(false, false), remaps);
        assertEquals(java.util.Collections.singletonList(1), required);
    }

    private void read(String name, ClassVisitor visitor) throws Exception {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(name + ".class")) {
            assertNotNull(input, "Missing compiled class: " + name);
            new ClassReader(input).accept(visitor, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }
    }
}

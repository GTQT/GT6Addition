package com.drppp.gt6addition.common.metatileentity.single.hu;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/** Temporary source-audit utility. Remove only after final parity acceptance. */
class GT6ParityTableRebuilderTemporaryTest {
    private static final Path GT6_ROOT = Paths.get("E:/迅雷下载/gregtech6-master");
    private static final Path TEST_RESOURCES = Paths.get("src/test/resources");
    private static final String MT_HASH = "CF5BD26C6D6E0D4F4078950C7E74DB3182DFF613A46F720DDE72BA535515DE1A";
    private static final String MATERIAL_HASH = "768707D5EEEB6B60F0C5D0A24B39AB50F6F173A29A40F0FA3365308CF0688371";
    private static final String CONFIG_HASH = "4107C41DF6B00726D4D2B2751175408B59B883E538301065991B38F7D793E1D1";
    private static final String CS_HASH = "407EDCE9F541BCF34B6573D59E4AE14E477FB9C145439DEDDB831E2A2E2887A5";

    private static final class SourceMaterial {
        final int id;
        final String name;
        final int line;
        SourceMaterial(int id, String name, int line) { this.id = id; this.name = name; this.line = line; }
    }

    @Test
    void rebuildDeletedReadOnlyTablesFromThePinnedGt6Snapshot() throws Exception {
        // Keep a normal `test` run from silently rewriting its own expected
        // fixtures. Set GT6_REBUILD_TABLES=1 only for an explicit rebuild.
        if (!"1".equals(System.getenv("GT6_REBUILD_TABLES"))) return;
        Path mtPath = GT6_ROOT.resolve("src/main/java/gregapi/data/MT.java");
        Path materialPath = GT6_ROOT.resolve("src/main/java/gregapi/oredict/OreDictMaterial.java");
        Path configPath = GT6_ROOT.resolve("src/main/java/gregapi/oredict/configurations/OreDictConfigurationComponent.java");
        Path constantsPath = GT6_ROOT.resolve("src/main/java/gregapi/data/CS.java");
        String mt = stripComments(new String(Files.readAllBytes(mtPath), StandardCharsets.UTF_8));
        assertEquals(MT_HASH, sha256(mtPath));
        assertEquals(MATERIAL_HASH, sha256(materialPath));
        assertEquals(CONFIG_HASH, sha256(configPath));
        assertEquals(CS_HASH, sha256(constantsPath));
        List<SourceMaterial> materials = sourceMaterials(mt);
        assertEquals(1084, materials.size());

        // Do not regenerate numeric expected-value fixtures from the current
        // port. That would turn a regression check into a self-comparison.
        // Density, phase and target fixtures must only be written by the
        // independent, hash-pinned GT6 source evaluators.
        rebuildAliases(mt);
        rebuildGlowstoneFamily(mt);

        System.out.println("Rebuilt GT6 source-structural tables from pinned source inventory: " + materials.size() +
                " MT identities; source hashes MT/OreDictMaterial/config/CS=" +
                MT_HASH + "/" + MATERIAL_HASH + "/" + CONFIG_HASH + "/" + CS_HASH);
    }

    private static List<SourceMaterial> sourceMaterials(String mt) {
        Pattern pattern = Pattern.compile("(?<factory>[A-Za-z_]\\w*)\\s*\\(\\s*(?<id>\\d+)\\s*,\\s*\"(?<name>[^\"]+)\"");
        Map<Integer, SourceMaterial> byId = new LinkedHashMap<>();
        Matcher matcher = pattern.matcher(mt);
        while (matcher.find()) {
            int id = Integer.parseInt(matcher.group("id"));
            if (id <= 0 || id >= 10000) continue;
            String sourceName = matcher.group("name");
            SourceMaterial row = new SourceMaterial(id, sourceName, lineNumber(mt, matcher.start()));
            SourceMaterial previous = byId.put(id, row);
            assertNull(previous, "duplicate literal MT identity " + id);
        }
        return new ArrayList<>(byId.values());
    }

    private static void rebuildAliases(String mt) throws IOException {
        Map<String, List<Integer>> varargIndexes = objectVarargs(mt);
        List<String> rows = new ArrayList<>();
        Pattern calls = Pattern.compile("(?<factory>[A-Za-z_]\\w*)\\s*\\(\\s*(?<id>\\d+)\\s*,\\s*\"(?<name>[^\"]+)\"");
        Matcher matcher = calls.matcher(mt);
        while (matcher.find()) {
            int id = Integer.parseInt(matcher.group("id"));
            if (id <= 0 || id >= 10000) continue;
            List<Integer> candidates = varargIndexes.get(matcher.group("factory"));
            if (candidates == null || candidates.isEmpty()) continue;
            int open = mt.indexOf('(', matcher.start("factory"));
            int close = matchingParen(mt, open);
            List<String> args = splitTopLevel(mt.substring(open + 1, close));
            int fixed = -1;
            // Prefer an overload whose varargs actually contain source data;
            // a longer overload may otherwise match only because it allows
            // zero varargs and swallow the shorter overload's final alias.
            for (int candidate : candidates) if (candidate < args.size()) fixed = Math.max(fixed, candidate);
            if (fixed < 0 || fixed >= args.size()) continue;
            String sourceName = matcher.group("name");
            for (int i = fixed; i < args.size(); i++) {
                Matcher strings = Pattern.compile("\"((?:\\\\.|[^\"\\\\])*)\"").matcher(args.get(i));
                while (strings.find()) {
                    String alias = unescape(strings.group(1));
                    rows.add(alias + "|" + id + "|" + sourceName + "|" + lineNumber(mt, matcher.start()) + "|" + matcher.group("factory"));
                }
            }
        }

        Map<String, Integer> expected = GT6RegistrationAliasData.identities();
        Set<String> keys = new HashSet<>();
        for (String row : rows) {
            String[] fields = row.split("\\|", -1);
            String key = normalize(fields[0]);
            assertEquals(Integer.valueOf(Integer.parseInt(fields[1])), expected.get(key), row);
            keys.add(key);
        }
        Set<String> missing = new HashSet<>(expected.keySet());
        missing.removeAll(keys);
        assertEquals(209, rows.size(), "GT6 MT.java literal Object-vararg aliases; missing=" + missing);
        assertEquals(202, keys.size(), "normalized alias identity count");
        write("gt6-registration-aliases.txt", rows);
    }

    private static Map<String, List<Integer>> objectVarargs(String mt) {
        Map<String, List<Integer>> result = new HashMap<>();
        Pattern methods = Pattern.compile("static\\s+OreDictMaterial\\s+(?<name>[A-Za-z_]\\w*)\\s*\\(");
        Matcher matcher = methods.matcher(mt);
        while (matcher.find()) {
            int open = mt.indexOf('(', matcher.start("name"));
            int close = matchingParen(mt, open);
            List<String> parameters = splitTopLevel(mt.substring(open + 1, close));
            if (parameters.isEmpty() || !parameters.get(parameters.size() - 1).contains("Object...")) continue;
            result.computeIfAbsent(matcher.group("name"), ignored -> new ArrayList<>()).add(parameters.size() - 1);
        }
        return result;
    }

    private static void rebuildGlowstoneFamily(String mt) throws IOException {
        List<String> rows = new ArrayList<>();
        String[] lines = mt.split("\\n", -1);
        Pattern pattern = Pattern.compile("(?<factory>[A-Za-z_]\\w*)\\s*\\(\\s*(?<id>\\d+)\\s*,\\s*\"(?<name>Glowstone[^\"]+|Gloomstone)\"");
        Matcher matcher = pattern.matcher(mt);
        while (matcher.find()) {
            int sourceLine = lineNumber(mt, matcher.start());
            if (!lines[sourceLine - 1].contains(".setGenerifying(Glowstone)")) continue;
            int id = Integer.parseInt(matcher.group("id"));
            if (id <= 0 || id >= 10000 || GT6MaterialIdentity.name(id) == null) continue;
            rows.add(id + "|" + matcher.group("name") + "|" + sourceLine);
        }
        assertEquals(6, rows.size());
        write("gt6-glowstone-family-members.txt", rows);
    }

    private static String normalize(String name) {
        return name == null ? null : name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private static String stripComments(String source) {
        StringBuilder result = new StringBuilder(source.length());
        boolean inString = false, escaped = false, blockComment = false, lineComment = false;
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            char next = i + 1 < source.length() ? source.charAt(i + 1) : 0;
            if (lineComment) {
                if (c == '\n') { lineComment = false; result.append(c); }
                continue;
            }
            if (blockComment) {
                if (c == '*' && next == '/') { blockComment = false; i++; }
                else if (c == '\n') result.append(c);
                continue;
            }
            if (inString) {
                result.append(c);
                if (escaped) escaped = false;
                else if (c == '\\') escaped = true;
                else if (c == '"') inString = false;
                continue;
            }
            if (c == '"') { inString = true; result.append(c); }
            else if (c == '/' && next == '/') { lineComment = true; i++; }
            else if (c == '/' && next == '*') { blockComment = true; i++; }
            else result.append(c);
        }
        return result.toString();
    }

    private static int matchingParen(String source, int open) {
        int depth = 0;
        boolean inString = false, escaped = false;
        for (int i = open; i < source.length(); i++) {
            char c = source.charAt(i);
            if (inString) {
                if (escaped) escaped = false;
                else if (c == '\\') escaped = true;
                else if (c == '"') inString = false;
                continue;
            }
            if (c == '"') inString = true;
            else if (c == '(') depth++;
            else if (c == ')' && --depth == 0) return i;
        }
        throw new IllegalArgumentException("Unbalanced parentheses at " + open);
    }

    private static List<String> splitTopLevel(String source) {
        if (source.trim().isEmpty()) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        int start = 0, round = 0, square = 0, curly = 0;
        boolean inString = false, escaped = false;
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (inString) {
                if (escaped) escaped = false;
                else if (c == '\\') escaped = true;
                else if (c == '"') inString = false;
                continue;
            }
            if (c == '"') inString = true;
            else if (c == '(') round++;
            else if (c == ')') round--;
            else if (c == '[') square++;
            else if (c == ']') square--;
            else if (c == '{') curly++;
            else if (c == '}') curly--;
            else if (c == ',' && round == 0 && square == 0 && curly == 0) {
                result.add(source.substring(start, i).trim());
                start = i + 1;
            }
        }
        result.add(source.substring(start).trim());
        return result;
    }

    private static int lineNumber(String source, int offset) {
        int lines = 1;
        for (int i = 0; i < offset; i++) if (source.charAt(i) == '\n') lines++;
        return lines;
    }

    private static String unescape(String value) {
        return value.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static String sha256(Path path) throws Exception {
        byte[] bytes = java.security.MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path));
        StringBuilder result = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) result.append(String.format(Locale.ROOT, "%02X", value & 0xFF));
        return result.toString();
    }

    private static void write(String name, List<String> lines) throws IOException {
        Files.createDirectories(TEST_RESOURCES);
        Files.write(TEST_RESOURCES.resolve(name), lines, StandardCharsets.UTF_8);
    }
}

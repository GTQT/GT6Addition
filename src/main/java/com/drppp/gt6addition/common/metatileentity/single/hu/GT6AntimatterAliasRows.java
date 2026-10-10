package com.drppp.gt6addition.common.metatileentity.single.hu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Generated read-only GT6 source data. Do not edit by hand; regenerate with
 * scripts/generateGt6JavaMaterialTables.py after rebuilding its source fixture.
 * Fixture: gt6-antimatter-source-aliases.txt
 * Source: AM_SHA256=8F012B6BA27396BB799882B91EA4C46BB011F4FC1B6C150233F8078A4403BEEE
 */
final class GT6AntimatterAliasRows {
    static final List<Row> ROWS = Collections.unmodifiableList(build());

    private static List<Row> build() {
        List<Row> rows = new ArrayList<>(22);
        rows.addAll(Arrays.asList(chunk00()));
        return rows;
    }

    private static Row[] chunk00() {
        return new Row[]{
                new Row("AntiElectron", "Positron"),
                new Row("Anti-Natrium", "Anti-Sodium"),
                new Row("Anti-Aluminum", "Anti-Aluminium"),
                new Row("Anti-Kalium", "Anti-Potassium"),
                new Row("Anti-Titan", "Anti-Titanium"),
                new Row("Anti-Chrome", "Anti-Chromium"),
                new Row("Anti-Gregorium", "Anti-Technetium"),
                new Row("Anti-Wolframium", "Anti-Tungsten"),
                new Row("Anti-Wolfram", "Anti-Tungsten"),
                new Row("Anti-Quicksilver", "Anti-Mercury"),
                new Row("Anti-QuickSilver", "Anti-Mercury"),
                new Row("Anti-Uranium238", "Anti-Uranium"),
                new Row("Anti-Uran", "Anti-Uranium"),
                new Row("Anti-Plutonium244", "Anti-Plutonium"),
                new Row("Anti-Ununseptium", "Anti-Farnsium"),
                new Row("Anti-Unpentbium", "Anti-Vibranium"),
                new Row("Anti-Unseptquadium", "Anti-Naquadah"),
                new Row("Anti-Adamantine", "Anti-Adamantium"),
                new Row("Anti-Adamant", "Anti-Adamantium"),
                new Row("Anti-Bibibium", "Anti-Adamantium"),
                new Row("Anti-Bitriennium", "Anti-Mac-Guffium"),
                new Row("Anti-Triseptbium", "Anti-Gravitonium"),
        };
    }

    static final class Row {
        final String alias;
        final String target;
        private Row(String alias, String target) {
            this.alias = alias;
            this.target = target;
        }
    }
}

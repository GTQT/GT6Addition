#!/usr/bin/env python3
"""Generate runtime Java tables from independently reconstructed GT6 fixtures.

The fixture files are source-audit inputs under src/test/resources. Runtime
code consumes only the generated Java classes, so missing resource files can
no longer break crucible initialization.
"""

from __future__ import annotations

import argparse
import json
import math
import re
from dataclasses import dataclass
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
PACKAGE_DIR = ROOT / "src/main/java/com/drppp/gt6addition/common/metatileentity/single/hu"
FIXTURE_DIR = ROOT / "src/test/resources"
CHUNK_SIZE = 128


@dataclass(frozen=True)
class Table:
    group: str
    fixture: str
    class_name: str
    fields: tuple[tuple[str, str], ...]
    expected_rows: int
    unique_field: str | None = None


TABLES = (
    Table("targets", "gt6-target-source-values.txt", "GT6LiteralTargetRows",
          (("id", "int"), ("sourceName", "String"), ("sourceLine", "int"),
           ("hotTargetId", "int"), ("hotTargetName", "String"), ("hotAmount", "long"),
           ("coldTargetId", "int"), ("coldTargetName", "String"), ("coldAmount", "long")), 1084, "id"),
    Table("phase", "gt6-phase-source-values.txt", "GT6LiteralPhaseRows",
          (("id", "int"), ("sourceName", "String"), ("sourceLine", "int"),
           ("melting", "int"), ("boiling", "long")), 1084, "id"),
    Table("density", "gt6-density-source-values.txt", "GT6LiteralDensityRows",
          (("id", "int"), ("sourceName", "String"), ("sourceLine", "int"),
           ("density", "double")), 1084, "id"),
    Table("flags", "gt6-flags-source-values.txt", "GT6LiteralHazardRows",
          (("id", "int"), ("sourceName", "String"), ("sourceLine", "int"),
           ("flags", "int")), 1084, "id"),
    Table("antimatter", "gt6-antimatter-source-values.txt", "GT6AntimatterRows",
          (("id", "int"), ("name", "String"), ("melting", "int"), ("boiling", "int"),
           ("density", "double"), ("meltingFlag", "int")), 418, "id"),
    Table("antimatter", "gt6-antimatter-source-aliases.txt", "GT6AntimatterAliasRows",
          (("alias", "String"), ("target", "String")), 22),
)


def read_rows(table: Table) -> tuple[list[str], list[list[str]]]:
    path = FIXTURE_DIR / table.fixture
    if not path.is_file():
        raise ValueError(f"Missing pinned GT6 source fixture: {path}")
    headers: list[str] = []
    rows: list[list[str]] = []
    for line_number, raw in enumerate(path.read_text(encoding="utf-8-sig").splitlines(), 1):
        line = raw.strip()
        if not line:
            continue
        if line.startswith("#"):
            headers.append(line[1:].strip())
            continue
        fields = line.split("|")
        if len(fields) != len(table.fields):
            raise ValueError(f"{path}:{line_number}: expected {len(table.fields)} fields, got {len(fields)}")
        for value, (name, kind) in zip(fields, table.fields):
            if kind in ("int", "long") and not re.fullmatch(r"-?\d+", value):
                raise ValueError(f"{path}:{line_number}: invalid {kind} for {name}: {value!r}")
            if kind == "double":
                parsed = float(value)
                if not math.isfinite(parsed):
                    raise ValueError(f"{path}:{line_number}: non-finite {name}: {value!r}")
            if kind == "String" and not value:
                raise ValueError(f"{path}:{line_number}: empty string field {name}")
        rows.append(fields)
    if len(rows) != table.expected_rows:
        raise ValueError(f"{path}: expected {table.expected_rows} rows, got {len(rows)}")
    if table.unique_field:
        unique_index = next(i for i, (name, _) in enumerate(table.fields) if name == table.unique_field)
        values = [row[unique_index] for row in rows]
        if len(set(values)) != len(values):
            raise ValueError(f"{path}: duplicate {table.unique_field}")
    return headers, rows


def java_string(value: str) -> str:
    return json.dumps(value, ensure_ascii=True)


def java_value(value: str, kind: str) -> str:
    if kind == "String":
        return java_string(value)
    if kind == "long":
        return value + "L"
    if kind == "double":
        return value + "D"
    return value


def render(table: Table, headers: list[str], rows: list[list[str]]) -> str:
    lines = [
        "package com.drppp.gt6addition.common.metatileentity.single.hu;",
        "",
        "import java.util.ArrayList;",
        "import java.util.Arrays;",
        "import java.util.Collections;",
        "import java.util.List;",
        "",
        "/**",
        " * Generated read-only GT6 source data. Do not edit by hand; regenerate with",
        " * scripts/generateGt6JavaMaterialTables.py after rebuilding its source fixture.",
        f" * Fixture: {table.fixture}",
    ]
    lines.extend(f" * Source: {header}" for header in headers)
    lines.extend([
        " */",
        f"final class {table.class_name} {{",
        f"    static final List<Row> ROWS = Collections.unmodifiableList(build());",
        "",
        "    private static List<Row> build() {",
        f"        List<Row> rows = new ArrayList<>({len(rows)});",
    ])
    for chunk_index in range((len(rows) + CHUNK_SIZE - 1) // CHUNK_SIZE):
        lines.append(f"        rows.addAll(Arrays.asList(chunk{chunk_index:02d}()));")
    lines.extend([
        "        return rows;",
        "    }",
        "",
    ])
    for chunk_index, start in enumerate(range(0, len(rows), CHUNK_SIZE)):
        chunk = rows[start:start + CHUNK_SIZE]
        lines.append(f"    private static Row[] chunk{chunk_index:02d}() {{")
        lines.append("        return new Row[]{")
        for row in chunk:
            values = ", ".join(java_value(value, kind) for value, (_, kind) in zip(row, table.fields))
            lines.append(f"                new Row({values}),")
        lines.extend(["        };", "    }", ""])
    lines.extend(["    static final class Row {"])
    for name, kind in table.fields:
        lines.append(f"        final {kind} {name};")
    constructor_args = ", ".join(f"{kind} {name}" for name, kind in table.fields)
    lines.extend([f"        private Row({constructor_args}) {{"])
    for name, _ in table.fields:
        lines.append(f"            this.{name} = {name};")
    lines.extend(["        }", "    }", "}", ""])
    return "\n".join(lines)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--group", choices=("all", "targets", "phase", "density", "flags", "antimatter"),
                        default="all", help="table group to generate")
    parser.add_argument("--check", action="store_true", help="verify generated Java is current without writing")
    args = parser.parse_args()

    selected = [table for table in TABLES if args.group == "all" or table.group == args.group]
    errors: list[str] = []
    for table in selected:
        headers, rows = read_rows(table)
        destination = PACKAGE_DIR / f"{table.class_name}.java"
        generated = render(table, headers, rows)
        if args.check:
            if not destination.is_file() or destination.read_text(encoding="utf-8") != generated:
                errors.append(str(destination))
        else:
            destination.write_text(generated, encoding="utf-8", newline="\n")
            print(f"Generated {destination.relative_to(ROOT)} ({len(rows)} rows)")
    if errors:
        print("Generated GT6 Java table is stale or missing:")
        for error in errors:
            print(f"  {error}")
        return 1
    if args.check:
        print(f"Verified {len(selected)} generated GT6 Java table(s)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

#!/usr/bin/env python3
"""Independently evaluate GT6 literal density, phase, and hazard tables.

The evaluator reads only hash-pinned GT6 source. It does not import GT6Addition
production data. By default it compares existing test fixtures without writes;
--write is an explicit opt-in after reviewing the resolved source results.
After rebuilding a fixture, run generateGt6JavaMaterialTables.py to refresh the
corresponding production Java data table.
"""

from __future__ import annotations

import argparse
import hashlib
import importlib.util
import re
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path


EXPECTED_HASHES = {
    "MT.java": "CF5BD26C6D6E0D4F4078950C7E74DB3182DFF613A46F720DDE72BA535515DE1A",
    "OreDictMaterial.java": "768707D5EEEB6B60F0C5D0A24B39AB50F6F173A29A40F0FA3365308CF0688371",
    "OreDictConfigurationComponent.java": "4107C41DF6B00726D4D2B2751175408B59B883E538301065991B38F7D793E1D1",
    "CS.java": "407EDCE9F541BCF34B6573D59E4AE14E477FB9C145439DEDDB831E2A2E2887A5",
}
HEADER = [f"# {name.replace('.java', '')}_SHA256={digest}" for name, digest in EXPECTED_HASHES.items()]
CS_CONSTANTS: dict[str, str] = {}

_spec = importlib.util.spec_from_file_location("gt6_target_source_evaluator",
                                               Path(__file__).with_name("rebuildGt6TargetSourceTable.py"))
if _spec is None or _spec.loader is None:
    raise RuntimeError("Could not load the shared GT6 literal-source parser")
_source = importlib.util.module_from_spec(_spec)
sys.modules[_spec.name] = _source
_spec.loader.exec_module(_source)


@dataclass
class Physical:
    melting: int = 1000
    boiling: int = 3000
    density: float = 1.0
    flags: int = 0
    # OreDictMaterial initializes targetSmelting to self/U. Track its amount
    # separately because setAllToTheOutputOf calls setSmelting and thereby
    # grants MELTING even when it copies that default self target.
    hot_target_amount: int = _source.U


class SourceError(RuntimeError):
    pass


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest().upper()


def eval_float(expr: str, env: dict[str, str]) -> float:
    value = _source.substitute(expr, env).strip()
    value = value.replace("CS.C", "273")
    for name, constant in CS_CONSTANTS.items():
        value = re.sub(rf"\b{re.escape(name)}\b", constant, value)
    value = re.sub(r"\bC\b", "273", value)
    tokens = re.findall(r"(?:\d+\.\d*|\.\d+|\d+)|[()+*/-]", value.replace(" ", ""))
    if "".join(tokens) != re.sub(r"\s+", "", value):
        raise SourceError(f"Unsupported floating expression {expr!r} => {value!r}")
    pos = 0

    def atom() -> float:
        nonlocal pos
        if pos >= len(tokens):
            raise SourceError(f"Incomplete floating expression: {expr}")
        token = tokens[pos]
        if token == "+":
            pos += 1
            return atom()
        if token == "-":
            pos += 1
            return -atom()
        if token == "(":
            pos += 1
            result = add()
            if pos >= len(tokens) or tokens[pos] != ")":
                raise SourceError(f"Unclosed floating expression: {expr}")
            pos += 1
            return result
        pos += 1
        return float(token)

    def mul() -> float:
        nonlocal pos
        result = atom()
        while pos < len(tokens) and tokens[pos] in ("*", "/"):
            op = tokens[pos]
            pos += 1
            rhs = atom()
            result = result * rhs if op == "*" else result / rhs
        return result

    def add() -> float:
        nonlocal pos
        result = mul()
        while pos < len(tokens) and tokens[pos] in ("+", "-"):
            op = tokens[pos]
            pos += 1
            rhs = mul()
            result = result + rhs if op == "+" else result - rhs
        return result

    result = add()
    if pos != len(tokens):
        raise SourceError(f"Trailing floating expression tokens: {expr}")
    return result


def resolve(expression: str, env: dict[str, str], fields: dict[str, int], simple_fields: dict[str, int],
            name_ids: dict[str, int], current: int, scope: str) -> int:
    try:
        return _source.resolve_material(expression, env, fields, simple_fields, name_ids, current, name_ids, scope)
    except _source.SourceError as error:
        raise SourceError(str(error)) from error


def density_value(expression: str, env: dict[str, str], material: object, state: dict[int, Physical],
                  fields: dict[str, int], simple_fields: dict[str, int], name_ids: dict[str, int],
                  scope: str) -> float:
    value = _source.substitute(expression, env).strip()
    field = re.fullmatch(r"(?P<material>.+)\.mGramPerCubicCentimeter", value)
    if field:
        material_id = resolve(field.group("material"), {}, fields, simple_fields, name_ids,
                              material.material_id, scope)
        if material_id not in state:
            raise SourceError(f"Density source ID {material_id} not initialized")
        return state[material_id].density
    return eval_float(expression, env)


def eval_temperature(expression: str, env: dict[str, str], material: object, states: dict[int, Physical],
                     fields: dict[str, int], simple_fields: dict[str, int], name_ids: dict[str, int],
                     scope: str) -> int:
    value = _source.substitute(expression, env).strip()
    pattern = re.compile(r"(?P<material>(?:[A-Za-z_]\w*\.)*[A-Za-z_]\w*)\s*\."
                         r"(?P<property>mMeltingPoint|mBoilingPoint)")
    for match in reversed(list(pattern.finditer(value))):
        material_id = resolve(match.group("material"), {}, fields, simple_fields, name_ids,
                              material.material_id, scope)
        source = states.get(material_id)
        if source is None:
            raise SourceError(f"Temperature source ID {material_id} is missing")
        resolved = source.melting if match.group("property") == "mMeltingPoint" else source.boiling
        value = value[:match.start()] + str(resolved) + value[match.end():]
    return _source.eval_int(value, {})


def apply_property(name: str, args: list[str], env: dict[str, str], material: object,
                   states: dict[int, Physical], initialized: set[int], fields: dict[str, int],
                   simple_fields: dict[str, int], name_ids: dict[str, int], scope: str) -> None:
    own = states[material.material_id]
    if name == "put":
        # Capture the material flags which affect Smeltery hazard handling.
        # `put` is additive in OreDictMaterial; unrelated tags are ignored.
        for argument in args:
            for tag in re.findall(r"\b[A-Z][A-Z0-9_]*\b", argument):
                own.flags |= {
                    "FLAMMABLE": 1,
                    "EXPLOSIVE": 2,
                    "UNBURNABLE": 4,
                    "MELTING": 8,
                    "ACID": 16,
                }.get(tag, 0)
        return
    if name == "setSmelting":
        if len(args) != 2:
            raise SourceError(f"Unexpected setSmelting arity {len(args)} for {material.name}")
        amount = _source.eval_int(args[1], env)
        own.hot_target_amount = amount
        if amount > 0:
            # OreDictMaterial.setSmelting adds Processing.MELTING only for
            # positive targets. A default self/U target does not grant it.
            own.flags |= 8
        return
    if name == "setAllToTheOutputOf":
        if len(args) not in (1, 3):
            raise SourceError(f"Unexpected setAllToTheOutputOf arity {len(args)} for {material.name}")
        source_expression = args[0].strip()
        if source_expression in ("null", "this"):
            source_id = material.material_id
        else:
            source_id = resolve(source_expression, env, fields, simple_fields, name_ids,
                                material.material_id, scope)
        if source_id not in states:
            raise SourceError(f"setAllToTheOutputOf source {source_expression} is unresolved for {material.name}")
        if source_id != material.material_id and source_id not in initialized:
            raise SourceError(f"setAllToTheOutputOf source {source_expression} is not initialized for {material.name}")
        amount = states[source_id].hot_target_amount
        if len(args) == 3:
            multiplier = _source.eval_int(args[1], env)
            divider = _source.eval_int(args[2], env)
            if divider == 0:
                raise SourceError(f"setAllToTheOutputOf has zero divider for {material.name}")
            amount = _source.java_divide(amount * multiplier, divider)
        own.hot_target_amount = amount
        if amount > 0:
            # OreDictMaterial.setAllToTheOutputOf routes the copied target
            # through setSmelting, which adds MELTING for a positive amount.
            own.flags |= 8
        return
    if name in ("alloySimple", "alloyCentrifuge", "alloyElectrolyzer"):
        # These OreDictMaterial helpers wrap heat(...); no-argument overloads
        # only register a recipe and leave the default/previous heat intact.
        if not args:
            return
        if len(args) == 1:
            try:
                source_id = resolve(args[0], env, fields, simple_fields, name_ids,
                                    material.material_id, scope)
            except SourceError:
                source_id = None
            if source_id is not None and source_id in states:
                source = states[source_id]
                own.melting, own.boiling = source.melting, source.boiling
                return
        if len(args) in (1, 2):
            apply_property("heat", args, env, material, states, initialized,
                           fields, simple_fields, name_ids, scope)
            return
        raise SourceError(f"Unexpected {name} arity {len(args)} for {material.name}")
    if name in ("setMeltingPoint",):
        name = "heat"
    if name in ("setStatsEnergetic",):
        name = "heat"
    if name in ("stealStatsEnergetic",):
        name = "heat"
    if name == "heat":
        if len(args) == 1:
            try:
                source_id = resolve(args[0], env, fields, simple_fields, name_ids, material.material_id, scope)
            except SourceError:
                source_id = None
            if source_id is not None and re.fullmatch(r"\s*\(?\s*(?:[A-Za-z_]\w*\.)*[A-Za-z_]\w*\s*\)?\s*",
                                                       _source.substitute(args[0], env)):
                if source_id not in initialized:
                    raise SourceError(f"{material.name} copies heat from not-yet-initialized ID {source_id}")
                source = states[source_id]
                own.melting, own.boiling = source.melting, source.boiling
                return
            melt = eval_temperature(args[0], env, material, states, fields, simple_fields, name_ids, scope)
            own.melting, own.boiling = melt, melt * 2
            return
        if len(args) not in (2, 3):
            raise SourceError(f"Unexpected heat arity {len(args)} for {material.name}")
        own.melting = eval_temperature(args[0], env, material, states, fields, simple_fields, name_ids, scope)
        own.boiling = eval_temperature(args[1], env, material, states, fields, simple_fields, name_ids, scope)
        return
    if name == "setStats":
        if len(args) != 5:
            raise SourceError(f"Unexpected setStats arity {len(args)} for {material.name}")
        own.melting = eval_temperature(args[2], env, material, states, fields, simple_fields, name_ids, scope)
        own.boiling = eval_temperature(args[3], env, material, states, fields, simple_fields, name_ids, scope)
        own.density = density_value(args[4], env, material, states, fields, simple_fields, name_ids, scope)
        return
    if name == "setStatsElement":
        if len(args) != 5:
            raise SourceError(f"Unexpected setStatsElement arity {len(args)} for {material.name}")
        own.density = density_value(args[4], env, material, states, fields, simple_fields, name_ids, scope)
        return
    if name == "setDensity":
        if len(args) != 1:
            raise SourceError(f"Unexpected setDensity arity {len(args)} for {material.name}")
        own.density = density_value(args[0], env, material, states, fields, simple_fields, name_ids, scope)
        return
    if name == "steal":
        if len(args) != 1:
            raise SourceError(f"Unexpected steal arity {len(args)} for {material.name}")
        source_id = resolve(args[0], env, fields, simple_fields, name_ids, material.material_id, scope)
        if source_id not in initialized:
            raise SourceError(f"{material.name} steals statistics from not-yet-initialized ID {source_id}")
        source = states[source_id]
        own.melting, own.boiling, own.density = source.melting, source.boiling, source.density
        return
    if name == "stealStatsElement":
        if len(args) != 1:
            raise SourceError(f"Unexpected stealStatsElement arity {len(args)} for {material.name}")
        source_id = resolve(args[0], env, fields, simple_fields, name_ids, material.material_id, scope)
        if source_id not in initialized:
            raise SourceError(f"{material.name} copies element stats from not-yet-initialized ID {source_id}")
        own.density = states[source_id].density
        return
    if name in ("setMcfg", "uumMcfg", "setAloy", "uumAloy"):
        if len(args) < 3 or (len(args) - 1) % 2:
            raise SourceError(f"Unexpected {name} component arity {len(args)} for {material.name}")
        divider = _source.eval_int(args[0], env)
        components: list[tuple[int, int]] = []
        for index in range(1, len(args), 2):
            component_id = resolve(args[index], env, fields, simple_fields, name_ids,
                                   material.material_id, scope)
            amount = _source.eval_int(args[index + 1], env)
            components.append((component_id, amount))
        if divider == 0:
            divider = _source.java_divide(sum(amount for _, amount in components), _source.U)
        if divider == 0:
            raise SourceError(f"Zero effective component divider in {material.name}.{name}")
        value = 0.0
        total_weight = 0
        melting = 0.0
        boiling = 0.0
        for component_id, amount in components:
            if component_id not in initialized:
                raise SourceError(f"{material.name}.{name} references uninitialized component ID {component_id}")
            divided_amount = _source.java_divide(amount, divider)
            if divided_amount < 0:
                raise SourceError(f"Negative divided component amount in {material.name}.{name}")
            value += states[component_id].density * divided_amount / _source.U
            total_weight += divided_amount
            melting += states[component_id].melting * divided_amount
            boiling += states[component_id].boiling * divided_amount
        if total_weight <= 0:
            raise SourceError(f"Empty component configuration in {material.name}.{name}")
        own.density = value
        own.melting = max(1, int(melting / total_weight))
        own.boiling = max(own.melting + 1, int(boiling / total_weight))
        return
    raise SourceError(f"Unsupported GT6 physical setter {name}")


PROPERTY_METHODS = {
    "heat", "setStats", "setStatsElement", "setDensity", "steal",
    "stealStatsElement", "stealStatsEnergetic", "setMeltingPoint", "setStatsEnergetic",
    "alloySimple", "alloyCentrifuge", "alloyElectrolyzer", "put", "setSmelting",
    "setAllToTheOutputOf",
    "setMcfg", "uumMcfg", "setAloy", "uumAloy",
}


def walk(text: str, env: dict[str, str], material: object, methods: dict[str, list[object]],
         states: dict[int, Physical], initialized: set[int], fields: dict[str, int],
         simple_fields: dict[str, int], name_ids: dict[str, int], scope: str, depth: int = 0) -> None:
    if depth > 32:
        raise SourceError(f"GT6 factory recursion exceeded 32 levels for {material.name}")
    factory_names = set(methods)
    factory_pattern = re.compile(r"(?<![.\w])(?P<name>[A-Za-z_]\w*)\s*\(")
    property_pattern = re.compile(r"\.\s*(?P<name>" + "|".join(sorted(PROPERTY_METHODS)) + r")\s*\(")
    events: list[tuple[int, int, str, str, list[str]]] = []
    for match in factory_pattern.finditer(text):
        if match.group("name") not in factory_names:
            continue
        args, close = _source.invocation_args(text, text.index("(", match.start()))
        events.append((match.start(), close, "factory", match.group("name"), args))
    for match in property_pattern.finditer(text):
        args, close = _source.invocation_args(text, text.index("(", match.start()))
        events.append((match.start(), close, "property", match.group("name"), args))
    events.sort(key=lambda event: (event[0], 0 if event[2] == "factory" else 1))
    last_end = -1
    for _, close, kind, name, args in events:
        if close < last_end:
            continue
        last_end = close
        args = [_source.substitute(arg, env).strip() for arg in args]
        if kind == "factory":
            method = _source.choose_method(name, args, methods)
            nested_env = _source.bind_method(method, args)
            walk(method.body, nested_env, material, methods, states, initialized, fields,
                 simple_fields, name_ids, "MT", depth + 1)
        else:
            apply_property(name, args, env, material, states, initialized, fields,
                           simple_fields, name_ids, scope)


def prepare_fields(mt: str, materials: list[object], methods: dict[str, list[object]]) -> tuple[
        dict[str, int], dict[str, int], dict[str, int], list[tuple[int, int, str]]]:
    fields: dict[str, int] = {}
    candidates: dict[str, set[int]] = {}
    names: dict[str, int] = {}
    scopes = _source.class_scopes(mt)
    for material in materials:
        names[_source.normalize(material.name)] = material.material_id
        for field in material.field_names:
            key = material.scope + "." + field
            if key in fields and fields[key] != material.material_id:
                raise SourceError(f"Qualified field conflict {key}")
            fields[key] = material.material_id
            candidates.setdefault(field, set()).add(material.material_id)
    try:
        _source.add_method_field_aliases(mt, scopes, _source.zero_argument_material_methods(methods), fields, candidates)
    except _source.SourceError as error:
        raise SourceError(str(error)) from error
    simple = {name: next(iter(ids)) for name, ids in candidates.items() if len(ids) == 1}
    return fields, simple, names, scopes


def apply_static_properties(mt: str, materials: list[object], states: dict[int, Physical],
                            initialized: set[int], fields: dict[str, int], simple_fields: dict[str, int],
                            name_ids: dict[str, int]) -> list[str]:
    material_by_id = {material.material_id: material for material in materials}
    unresolved: list[str] = []
    static_pattern = re.compile(r"\bstatic\s*\{")
    for static in static_pattern.finditer(mt):
        opening = mt.index("{", static.start())
        closing = _source.matching(mt, opening, "{", "}")
        body = mt[opening + 1:closing]
        # Match every property mutator in a fluent chain, not only the first
        # `Material.method(...)` call. MT.java's Lava initializer chains
        # setSolidifying(...).setDensity(Obsidian.mGramPerCubicCentimeter),
        # and ignoring the second call leaves the runtime table at the stale
        # constructor default instead of GT6's final initialized density.
        call = re.compile(r"\.\s*(?P<name>" + "|".join(sorted(PROPERTY_METHODS)) + r")\s*\(")
        for statement in re.finditer(r"[^;]+;", body):
            root = re.match(r"\s*(?P<receiver>[A-Za-z_]\w*)\s*\.", statement.group())
            if not root:
                continue
            receiver_key = "MT." + root.group("receiver")
            if receiver_key not in fields:
                continue
            material = material_by_id[fields[receiver_key]]
            statement_body = statement.group()
            for match in call.finditer(statement_body):
                args, _ = _source.invocation_args(statement_body, statement_body.index("(", match.start()))
                try:
                    apply_property(match.group("name"), args, {}, material, states, initialized,
                                   fields, simple_fields, name_ids, "MT")
                except SourceError as error:
                    unresolved.append(f"static@{_source.source_line(mt, static.start())}|{error}")
    return unresolved


def apply_alloying_recipe_properties(mt: str, materials: list[object], states: dict[int, Physical],
                                     fields: dict[str, int], simple_fields: dict[str, int],
                                     name_ids: dict[str, int]) -> list[str]:
    """Apply MT.init()'s post-declaration melting-point safety clamps.

    OreDictMaterial.addAlloyingRecipe lowers an alloy's melting point when it
    would otherwise exceed a non-element component's boiling point. MT.java
    also has a small loop over ANY.Glowstone.mToThis; its exact members are the
    declarations with setGenerifying(Glowstone), all present in the pinned MT.
    """
    unresolved: list[str] = []
    by_id = {material.material_id: material for material in materials}
    material_by_key = {material.scope + "." + field: material.material_id
                       for material in materials for field in material.field_names}
    dynamic_glowstone = [material.material_id for material in materials
                         if re.search(r"\.\s*setGenerifying\s*\(\s*(?:MT\.)?Glowstone\s*\)", material.expression)]
    dynamic_glowstone = sorted(set(dynamic_glowstone))
    source = _source.strip_comments(mt)
    call_pattern = re.compile(r"(?P<receiver>(?:[A-Za-z_]\w*\.)*[A-Za-z_]\w*)\s*\.\s*"
                              r"addAlloyingRecipe\s*\(")
    stack_pattern = re.compile(r"(?:OM\s*\.\s*)?stack\s*\(")
    for match in call_pattern.finditer(source):
        try:
            call_args, _ = _source.invocation_args(source, source.index("(", match.start()))
            if len(call_args) != 1:
                raise SourceError(f"Unexpected addAlloyingRecipe arity at line {_source.source_line(source, match.start())}")
            config = re.search(r"new\s+OreDictConfigurationComponent\s*\(", call_args[0])
            if config is None:
                raise SourceError(f"Unsupported alloy recipe expression at line {_source.source_line(source, match.start())}")
            config_args, _ = _source.invocation_args(call_args[0], call_args[0].index("(", config.start()))
            if len(config_args) < 3:
                raise SourceError(f"Empty alloy recipe at line {_source.source_line(source, match.start())}")
            divider = _source.eval_int(config_args[0], {})
            receiver_id = resolve(match.group("receiver"), {}, fields, simple_fields, name_ids, 0, "MT")
            if receiver_id not in states:
                raise SourceError(f"Alloy output is unresolved: {match.group('receiver')}")
            components: list[tuple[int, int]] = []
            stack_source = ",".join(config_args[1:])
            for component_match in stack_pattern.finditer(stack_source):
                stack_args, _ = _source.invocation_args(stack_source, stack_source.index("(", component_match.start()))
                if len(stack_args) != 2:
                    raise SourceError(f"Unexpected stack in alloy recipe at line {_source.source_line(source, match.start())}")
                component_name = stack_args[0].strip()
                amount = _source.eval_int(stack_args[1], {})
                if component_name in ("tMat", "tMaterial"):
                    ids = dynamic_glowstone
                    if not ids:
                        raise SourceError("MT.init dynamic Glowstone loop has no source members")
                else:
                    ids = [resolve(component_name, {}, fields, simple_fields, name_ids, receiver_id, "MT")]
                for component_id in ids:
                    if component_id not in states:
                        raise SourceError(f"Alloy component is unresolved: {component_name}")
                    components.append((component_id, amount))
            if not components:
                raise SourceError(f"No alloy components at line {_source.source_line(source, match.start())}")
            output = states[receiver_id]
            for component_id, _ in components:
                component = by_id[component_id]
                if component.name.lower() == "air":
                    continue
                if output.melting >= states[component_id].boiling:
                    # MT.java's addAlloyingRecipe contract uses C+20 K.
                    output.melting = max(293, states[component_id].boiling - 20)
        except (SourceError, _source.SourceError, ZeroDivisionError, OverflowError) as error:
            unresolved.append(f"alloy@{_source.source_line(source, match.start())}|{error}")
    return unresolved


def evaluate(mt_raw: str) -> tuple[list[object], list[Physical], list[str]]:
    mt = _source.strip_comments(mt_raw)
    materials = _source.declarations(mt)
    methods = _source.method_table(mt)
    fields, simple_fields, name_ids, _ = prepare_fields(mt, materials, methods)
    states = {material.material_id: Physical() for material in materials}
    initialized: set[int] = set()
    unresolved: list[str] = []
    for material in materials:
        root = re.match(r"(?P<name>[A-Za-z_]\w*)\s*\(", material.expression)
        if not root:
            unresolved.append(f"{material.material_id}|{material.name}|{material.line}|missing factory")
            continue
        args, close = _source.invocation_args(material.expression, material.expression.index("(", root.start()))
        try:
            method = _source.choose_method(root.group("name"), args, methods)
            env = _source.bind_method(method, args)
            walk(method.body, env, material, methods, states, initialized, fields, simple_fields, name_ids, "MT")
            tail = material.expression[close + 1:]
            walk(tail, {}, material, methods, states, initialized, fields, simple_fields, name_ids, material.scope)
            initialized.add(material.material_id)
        except (_source.SourceError, SourceError, ZeroDivisionError, OverflowError) as error:
            unresolved.append(f"{material.material_id}|{material.name}|{material.line}|{error}")
    unresolved.extend(apply_static_properties(mt, materials, states, initialized, fields, simple_fields, name_ids))
    unresolved.extend(apply_alloying_recipe_properties(mt, materials, states, fields, simple_fields, name_ids))
    return materials, [states[material.material_id] for material in materials], unresolved


def render(materials: list[object], states: list[Physical], mode: str) -> list[str]:
    rows = []
    for material, state in zip(materials, states):
        common = (str(material.material_id), material.name, str(material.line))
        if mode == "density":
            # GT6 stores g/cm^3; the audited production table is kg/m^3.
            density = state.density * 1000.0
            rendered_density = (f"{density:.1f}" if density.is_integer() else format(density, ".15g"))
            rows.append("|".join((*common, rendered_density)))
        elif mode == "phase":
            rows.append("|".join((*common, str(state.melting), str(state.boiling))))
        else:
            rows.append("|".join((*common, str(state.flags))))
    return rows


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--gt6-root", type=Path, default=Path("E:/迅雷下载/gregtech6-master"))
    parser.add_argument("--mode", choices=("density", "phase", "flags"), required=True)
    parser.add_argument("--fixture", type=Path)
    parser.add_argument("--write", action="store_true", help="explicitly rewrite the selected test fixture")
    args = parser.parse_args()
    source_paths = {
        "MT.java": args.gt6_root / "src/main/java/gregapi/data/MT.java",
        "OreDictMaterial.java": args.gt6_root / "src/main/java/gregapi/oredict/OreDictMaterial.java",
        "OreDictConfigurationComponent.java": args.gt6_root / "src/main/java/gregapi/oredict/configurations/OreDictConfigurationComponent.java",
        "CS.java": args.gt6_root / "src/main/java/gregapi/data/CS.java",
    }
    for name, path in source_paths.items():
        actual = sha256(path)
        if actual != EXPECTED_HASHES[name]:
            raise SourceError(f"Pinned GT6 source hash mismatch for {name}: expected {EXPECTED_HASHES[name]}, got {actual}")
    cs_source = _source.strip_comments(source_paths["CS.java"].read_text(encoding="utf-8"))
    for match in re.finditer(r"\b(?:byte|short|int|long|float|double)\s+(?P<name>[A-Za-z_]\w*)\s*=\s*(?P<value>-?(?:\d+\.\d*|\.\d+|\d+)(?:[eE][+-]?\d+)?)\s*;", cs_source):
        CS_CONSTANTS[match.group("name")] = match.group("value")
    mt_raw = source_paths["MT.java"].read_text(encoding="utf-8")
    materials, states, unresolved = evaluate(mt_raw)
    rows = render(materials, states, args.mode)
    if unresolved:
        print(f"Source evaluation incomplete: resolved={len(materials) - len(unresolved)} total={len(materials)} unresolved={len(unresolved)}")
        for item in unresolved[:100]:
            print("UNRESOLVED " + item)
        return 2
    fixture = args.fixture or Path(f"src/test/resources/gt6-{args.mode}-source-values.txt")
    # Keep all four pinned source identities in both fixtures: phase values
    # also depend on material/component initialization and global constants.
    generated = HEADER + rows
    if args.write:
        fixture.parent.mkdir(parents=True, exist_ok=True)
        fixture.write_text("\n".join(generated) + "\n", encoding="utf-8", newline="\n")
        print(f"Wrote {len(rows)} independently evaluated GT6 {args.mode} rows to {fixture}")
        if args.fixture is None:
            java_group = "flags" if args.mode == "flags" else args.mode
            generator = Path(__file__).with_name("generateGt6JavaMaterialTables.py")
            generated_java = subprocess.run([sys.executable, str(generator), "--group", java_group], check=False)
            if generated_java.returncode != 0:
                return generated_java.returncode
        return 0
    if not fixture.is_file():
        raise SourceError(f"Fixture missing; pass --write only after reviewing full source evaluation: {fixture}")
    current = fixture.read_text(encoding="utf-8").splitlines()
    diffs: list[tuple[int, str, str]] = []
    if len(current) != len(generated):
        print(f"GT6 {args.mode} fixture line mismatch: expected {len(generated)}, found {len(current)}")
        return 1
    header_count = len(HEADER)
    if current[:header_count] != generated[:header_count]:
        diffs.extend((index + 1, old, new) for index, (old, new) in
                     enumerate(zip(current[:header_count], generated[:header_count])) if old != new)
    for offset, (old, new) in enumerate(zip(current[header_count:], generated[header_count:]), header_count + 1):
        old_fields, new_fields = old.split("|"), new.split("|")
        if len(old_fields) != len(new_fields) or old_fields[:-1] != new_fields[:-1]:
            diffs.append((offset, old, new))
            continue
        if args.mode == "density":
            try:
                equal = abs(float(old_fields[-1]) - float(new_fields[-1])) <= 1e-6
            except ValueError:
                equal = False
        else:
            equal = old_fields == new_fields
        if not equal:
            diffs.append((offset, old, new))
    if diffs:
        print(f"GT6 {args.mode} fixture mismatches: {len(diffs)} rows")
        for index, old, new in diffs[:80]:
            print(f"line {index}: fixture={old}\n         source={new}")
        return 1
    print(f"GT6 source {args.mode} table verified: {len(rows)} rows, 0 unresolved, 0 mismatches at " +
          ("1e-6 kg/m^3" if args.mode == "density" else
           "exact kelvin" if args.mode == "phase" else "exact flag mask") + " tolerance")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (OSError, SourceError, _source.SourceError) as error:
        print(f"ERROR: {error}", file=sys.stderr)

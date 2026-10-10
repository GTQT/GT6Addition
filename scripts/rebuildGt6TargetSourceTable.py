#!/usr/bin/env python3
"""Independently evaluate GT6 literal hot/cold material targets.

The default mode is read-only and compares the evaluated source values with the
checked-in test fixture. Pass --write only after reviewing a successful,
fully-resolved evaluation to replace that fixture. No GT6Addition production
table is imported or consulted. After rebuilding the fixture, run
generateGt6JavaMaterialTables.py to refresh the production Java data table.
"""

from __future__ import annotations

import argparse
import hashlib
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
U = 648648000
HEADER = [f"# {name.replace('.java', '')}_SHA256={digest}" for name, digest in EXPECTED_HASHES.items()]


@dataclass
class Method:
    name: str
    params: list[str]
    param_types: list[str]
    body: str

    @property
    def varargs(self) -> bool:
        return bool(self.params and self.params[-1].startswith("..."))

    @property
    def fixed_count(self) -> int:
        return len(self.params) - (1 if self.varargs else 0)


@dataclass
class Target:
    material_id: int
    amount: int


@dataclass
class Material:
    material_id: int
    name: str
    line: int
    scope: str
    expression: str
    field_names: list[str]
    hot: Target
    cold: Target


class SourceError(RuntimeError):
    pass


def sha256(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest().upper()


def strip_comments(text: str) -> str:
    out: list[str] = []
    i = 0
    string = False
    char_literal = False
    escaped = False
    line_comment = False
    block_comment = False
    while i < len(text):
        c = text[i]
        n = text[i + 1] if i + 1 < len(text) else "\0"
        if line_comment:
            if c == "\n":
                line_comment = False
                out.append(c)
            else:
                out.append(" ")
        elif block_comment:
            if c == "*" and n == "/":
                out.extend("  ")
                i += 1
                block_comment = False
            else:
                out.append("\n" if c == "\n" else " ")
        elif string or char_literal:
            out.append(c)
            if escaped:
                escaped = False
            elif c == "\\":
                escaped = True
            elif string and c == '"':
                string = False
            elif char_literal and c == "'":
                char_literal = False
        elif c == '"':
            string = True
            out.append(c)
        elif c == "'":
            char_literal = True
            out.append(c)
        elif c == "/" and n == "/":
            line_comment = True
            out.extend("  ")
            i += 1
        elif c == "/" and n == "*":
            block_comment = True
            out.extend("  ")
            i += 1
        else:
            out.append(c)
        i += 1
    return "".join(out)


def matching(text: str, opening: int, left: str, right: str) -> int:
    depth = 0
    string = False
    char_literal = False
    escaped = False
    for i in range(opening, len(text)):
        c = text[i]
        if string or char_literal:
            if escaped:
                escaped = False
            elif c == "\\":
                escaped = True
            elif string and c == '"':
                string = False
            elif char_literal and c == "'":
                char_literal = False
            continue
        if c == '"':
            string = True
        elif c == "'":
            char_literal = True
        elif c == left:
            depth += 1
        elif c == right:
            depth -= 1
            if depth == 0:
                return i
    raise SourceError(f"Unbalanced {left}{right} starting at source offset {opening}")


def split_top_level(text: str) -> list[str]:
    if not text.strip():
        return []
    parts: list[str] = []
    start = 0
    round_depth = square_depth = curly_depth = 0
    string = char_literal = escaped = False
    for i, c in enumerate(text):
        if string or char_literal:
            if escaped:
                escaped = False
            elif c == "\\":
                escaped = True
            elif string and c == '"':
                string = False
            elif char_literal and c == "'":
                char_literal = False
            continue
        if c == '"':
            string = True
        elif c == "'":
            char_literal = True
        elif c == "(":
            round_depth += 1
        elif c == ")":
            round_depth -= 1
        elif c == "[":
            square_depth += 1
        elif c == "]":
            square_depth -= 1
        elif c == "{":
            curly_depth += 1
        elif c == "}":
            curly_depth -= 1
        elif c == "," and round_depth == square_depth == curly_depth == 0:
            parts.append(text[start:i].strip())
            start = i + 1
    parts.append(text[start:].strip())
    return parts


def invocation_args(text: str, open_paren: int) -> tuple[list[str], int]:
    close = matching(text, open_paren, "(", ")")
    return split_top_level(text[open_paren + 1:close]), close


def source_line(text: str, offset: int) -> int:
    return text.count("\n", 0, offset) + 1


def class_scopes(text: str) -> list[tuple[int, int, str]]:
    """Return nested Java class ranges with their fully-qualified MT scopes."""
    ranges: list[tuple[int, int, str]] = []
    pattern = re.compile(r"\bclass\s+(?P<name>[A-Za-z_]\w*)[^{}]*\{")
    for match in pattern.finditer(text):
        opening = text.index("{", match.start())
        closing = matching(text, opening, "{", "}")
        parents = [item for item in ranges if item[0] < match.start() < item[1]]
        parents.sort(key=lambda item: item[1] - item[0], reverse=True)
        names = [item[2].split(".")[-1] for item in parents]
        names.append(match.group("name"))
        ranges.append((opening, closing, ".".join(names)))
    return ranges


def scope_at(ranges: list[tuple[int, int, str]], offset: int) -> str:
    containing = [item for item in ranges if item[0] < offset < item[1]]
    if not containing:
        raise SourceError(f"No Java class scope at source offset {offset}")
    return min(containing, key=lambda item: item[1] - item[0])[2]


def declarations(mt: str) -> list[Material]:
    pattern = re.compile(r'(?P<factory>[A-Za-z_]\w*)\s*\(\s*(?P<id>\d+)\s*,\s*"(?P<name>[^"]+)"')
    scopes = class_scopes(mt)
    result: list[Material] = []
    seen: set[int] = set()
    for match in pattern.finditer(mt):
        material_id = int(match.group("id"))
        if not 0 < material_id < 10000:
            continue
        if material_id in seen:
            raise SourceError(f"Duplicate literal MT ID {material_id} at line {source_line(mt, match.start())}")
        seen.add(material_id)
        factory_open = mt.index("(", match.start("factory"))
        _, factory_close = invocation_args(mt, factory_open)
        end = factory_close + 1
        depth = 0
        string = char_literal = escaped = False
        while end < len(mt):
            c = mt[end]
            if string or char_literal:
                if escaped:
                    escaped = False
                elif c == "\\":
                    escaped = True
                elif string and c == '"':
                    string = False
                elif char_literal and c == "'":
                    char_literal = False
            elif c == '"':
                string = True
            elif c == "'":
                char_literal = True
            elif c == "(":
                depth += 1
            elif c == ")":
                depth -= 1
            elif depth == 0 and c in ",;":
                break
            end += 1
        expression = mt[match.start("factory"):end].strip()
        prefix = mt[max(mt.rfind(";", 0, match.start()), mt.rfind("{", 0, match.start()), mt.rfind("}", 0, match.start())) + 1:match.start()]
        # A chained declaration such as `y, Photon = y = create(...)` needs
        # only the actual assigned storage field (`y`). Taking the full comma
        # prefix would incorrectly absorb a previous initializer's aliases.
        left = re.search(r"(?P<name>[A-Za-z_]\w*)\s*=\s*(?:(?:[A-Za-z_]\w*)\s*=\s*)?$", prefix)
        fields = [left.group("name")] if left else []
        inner = re.search(r"=\s*(?P<name>[A-Za-z_]\w*)\s*=\s*$", prefix)
        if inner and inner.group("name") not in fields:
            fields.append(inner.group("name"))
        result.append(Material(material_id, match.group("name"), source_line(mt, match.start()),
                               scope_at(scopes, match.start()), expression, fields,
                               Target(material_id, U), Target(material_id, U)))
    if len(result) != 1084:
        raise SourceError(f"Expected 1084 positive literal MT identities, parsed {len(result)}")
    return result


def method_table(mt: str) -> dict[str, list[Method]]:
    methods: dict[str, list[Method]] = {}
    signature = re.compile(r"static\s+OreDictMaterial\s+(?P<name>[A-Za-z_]\w*)\s*\(")
    for match in signature.finditer(mt):
        open_paren = mt.index("(", match.start("name"))
        raw_params, close_paren = invocation_args(mt, open_paren)
        body_open = mt.find("{", close_paren)
        if body_open < 0:
            raise SourceError(f"Missing method body for {match.group('name')}")
        body_close = matching(mt, body_open, "{", "}")
        params: list[str] = []
        param_types: list[str] = []
        for raw in raw_params:
            names = re.findall(r"[A-Za-z_]\w*", raw)
            if not names:
                raise SourceError(f"Unrecognized parameter in {match.group('name')}: {raw}")
            param_types.append(raw[:raw.rfind(names[-1])].strip())
            if "..." in raw:
                params.append("..." + names[-1])
            else:
                params.append(names[-1])
        methods.setdefault(match.group("name"), []).append(
            Method(match.group("name"), params, param_types, mt[body_open + 1:body_close]))
    return methods


def zero_argument_material_methods(methods: dict[str, list[Method]]) -> dict[str, int]:
    """Map GT6's named element helper methods (carbon(), iron(), ...) to IDs."""
    literal = re.compile(r'[A-Za-z_]\w*\s*\(\s*(?P<id>\d+)\s*,\s*"(?P<name>[^"]+)"')
    result: dict[str, int] = {}
    for name, overloads in methods.items():
        identities: set[int] = set()
        for method in overloads:
            if method.params:
                continue
            identities.update(int(match.group("id")) for match in literal.finditer(method.body))
        if len(identities) == 1:
            result[name] = next(iter(identities))
        elif len(identities) > 1:
            raise SourceError(f"Zero-argument GT6 method {name} creates multiple literal material IDs")
    return result


def add_method_field_aliases(mt: str, scopes: list[tuple[int, int, str]], method_ids: dict[str, int],
                             fields: dict[str, int], field_candidates: dict[str, set[int]]) -> None:
    # Element symbols such as C, Fe, and U_238 are declared as static material
    # fields backed by no-argument MT factory methods rather than inline IDs.
    declaration = re.compile(r"\bstatic\s+(?:final\s+)?OreDictMaterial\b")
    assignment = re.compile(r"(?P<field>[A-Za-z_]\w*)\s*=\s*(?P<method>[A-Za-z_]\w*)\s*\(\s*\)")
    for match in declaration.finditer(mt):
        scope = scope_at(scopes, match.start())
        if scope != "MT":
            continue
        end = mt.find(";", match.end())
        if end < 0:
            raise SourceError(f"Unterminated material field declaration at line {source_line(mt, match.start())}")
        statement = mt[match.end():end]
        for alias in assignment.finditer(statement):
            method_id = method_ids.get(alias.group("method"))
            if method_id is None:
                continue
            field = alias.group("field")
            qualified = scope + "." + field
            existing = fields.get(qualified)
            if existing is not None and existing != method_id:
                raise SourceError(f"Material field alias {qualified} conflicts: {existing} vs {method_id}")
            fields[qualified] = method_id
            field_candidates.setdefault(field, set()).add(method_id)


def choose_method(name: str, args: list[str], methods: dict[str, list[Method]]) -> Method:
    def type_compatibility(method: Method) -> int:
        score = 0
        for argument, declared_type in zip(args[:method.fixed_count], method.param_types):
            normalized_type = declared_type.replace("...", "[]").replace(" ", "")
            expression = argument.strip()
            has_number = bool(re.search(r"(?<![A-Za-z_])\d+(?![A-Za-z_])", expression))
            has_string = '"' in expression
            has_texture_set = bool(re.search(r"\b(?:SET_[A-Z0-9_]+|TextureSet)\b", expression))
            has_flag = bool(re.search(r"\b(?:UNBURNABLE|UNRECYCLABLE|FLAMMABLE|EXPLOSIVE|MELTING|ACID)\b", expression))
            if "TextureSet[]" in normalized_type:
                score += 6 if has_texture_set else (-8 if has_number or has_flag or has_string else 0)
            elif normalized_type in ("String", "java.lang.String"):
                score += 4 if has_string else (-5 if has_number else 0)
            elif normalized_type in ("int", "long", "short", "byte", "float", "double"):
                score -= 8 if has_texture_set or has_string or has_flag else 0
        return score

    candidates = []
    for method in methods.get(name, []):
        valid = len(args) == len(method.params) if not method.varargs else len(args) >= method.fixed_count
        if valid:
            score = (type_compatibility(method), 1 if len(args) == len(method.params) else 0,
                     method.fixed_count)
            candidates.append((score, method))
    if not candidates:
        raise SourceError(f"No GT6 source factory overload {name}/{len(args)}")
    candidates.sort(key=lambda pair: pair[0], reverse=True)
    best_score = candidates[0][0]
    best = [method for score, method in candidates if score == best_score]
    if len(best) > 1:
        # Overloads with equivalent source target calls are safe; choose the
        # first only after verifying their relevant statements are identical.
        signatures = [target_calls(method.body) for method in best]
        if any(value != signatures[0] for value in signatures[1:]):
            raise SourceError(f"Ambiguous target-affecting overload {name}/{len(args)}")
    return best[0]


def target_calls(text: str) -> list[tuple[str, str]]:
    pattern = re.compile(r"\.(setSmelting|setSolidifying|setAllToTheOutputOf)\s*\(")
    calls = []
    for match in pattern.finditer(text):
        args, _ = invocation_args(text, text.index("(", match.start()))
        calls.append((match.group(1), ",".join(args)))
    return calls


def bind_method(method: Method, args: list[str]) -> dict[str, str]:
    bound: dict[str, str] = {}
    for i, param in enumerate(method.params):
        if param.startswith("..."):
            bound[param[3:]] = ", ".join(args[i:])
        elif i < len(args):
            bound[param] = args[i]
        else:
            raise SourceError(f"Missing argument for {method.name}.{param}")
    return bound


def substitute(text: str, env: dict[str, str]) -> str:
    for name in sorted(env, key=len, reverse=True):
        text = re.sub(rf"\b{re.escape(name)}\b", lambda _: f"({env[name]})", text)
    return text


def java_divide(a: int, b: int) -> int:
    if b == 0:
        raise SourceError("Division by zero in GT6 source expression")
    return (abs(a) // abs(b)) * (-1 if (a < 0) != (b < 0) else 1)


def eval_int(text: str, env: dict[str, str]) -> int:
    expression = substitute(text, env).replace("CS.C", "273").replace("CS.U", str(U))
    expression = re.sub(r"\bU(\d+)\b", lambda m: str(java_divide(U, int(m.group(1)))), expression)
    expression = re.sub(r"\bU\b", str(U), expression)
    expression = re.sub(r"\bC\b", "273", expression)
    tokens = re.findall(r"\d+|[()+*/-]", expression.replace(" ", ""))
    if "".join(tokens) != re.sub(r"\s+", "", expression):
        raise SourceError(f"Unsupported integer expression: {text!r} => {expression!r}")
    pos = 0

    def atom() -> int:
        nonlocal pos
        if pos >= len(tokens):
            raise SourceError(f"Incomplete integer expression: {text}")
        token = tokens[pos]
        if token == "+":
            pos += 1
            return atom()
        if token == "-":
            pos += 1
            return -atom()
        if token == "(":
            pos += 1
            value = add()
            if pos >= len(tokens) or tokens[pos] != ")":
                raise SourceError(f"Unclosed integer expression: {text}")
            pos += 1
            return value
        pos += 1
        return int(token)

    def mul() -> int:
        nonlocal pos
        value = atom()
        while pos < len(tokens) and tokens[pos] in ("*", "/"):
            op = tokens[pos]
            pos += 1
            rhs = atom()
            value = value * rhs if op == "*" else java_divide(value, rhs)
        return value

    def add() -> int:
        nonlocal pos
        value = mul()
        while pos < len(tokens) and tokens[pos] in ("+", "-"):
            op = tokens[pos]
            pos += 1
            rhs = mul()
            value = value + rhs if op == "+" else value - rhs
        return value

    value = add()
    if pos != len(tokens):
        raise SourceError(f"Trailing integer expression tokens: {text}")
    return value


def normalize_symbol(expr: str) -> str:
    expr = expr.strip()
    expr = re.sub(r"\([^()]*\)", "", expr)
    expr = expr.replace("this", "")
    expr = expr.strip()
    return expr.split(".")[-1].strip()


def resolve_material(expr: str, env: dict[str, str], fields: dict[str, int], simple_fields: dict[str, int],
                     name_ids: dict[str, int], current: int, name_to_id: dict[str, int], scope: str) -> int:
    value = substitute(expr, env).strip()
    ternary = re.fullmatch(r'\s*"([^"]+)"\.equals\((.*)\)\s*\?\s*(.*?)\s*:\s*(.*?)\s*', value)
    if ternary:
        test_name = substitute(ternary.group(2), env).strip()
        while test_name.startswith("(") and test_name.endswith(")"):
            if matching(test_name, 0, "(", ")") == len(test_name) - 1:
                test_name = test_name[1:-1].strip()
            else:
                break
        test_name = test_name.strip('"')
        branch = ternary.group(3) if test_name.lower() == ternary.group(1).lower() else ternary.group(4)
        value = branch.strip()
    if value in ("null", "this") or value == "":
        return current
    while value.startswith("(") and value.endswith(")"):
        try:
            if matching(value, 0, "(", ")") == len(value) - 1:
                value = value[1:-1].strip()
            else:
                break
        except SourceError:
            break
    value = value.replace("this.", "")
    candidates: list[str] = []
    if value.startswith("MT."):
        candidates.append(value)
    elif value.split(".", 1)[0] in {"TECH", "OREMATS", "STONES", "WOODS", "UNUSED"}:
        candidates.append("MT." + value)
    elif "." in value:
        # Unqualified references within a nested MT class resolve there first,
        # then against MT's top-level material fields.
        candidates.extend((scope + "." + value.split(".")[-1], "MT." + value.split(".")[-1]))
    else:
        candidates.extend((scope + "." + value, "MT." + value))
    for key in candidates:
        if key in fields:
            return fields[key]
    key = normalize_symbol(value)
    if key in simple_fields:
        return simple_fields[key]
    if key.lower() in name_ids:
        return name_ids[key.lower()]
    if key.lower() in name_to_id:
        return name_to_id[key.lower()]
    raise SourceError(f"Unknown GT6 material reference {expr!r} => {value!r}")


def apply_mutation(method_name: str, args: list[str], env: dict[str, str], material: Material,
                   states: dict[int, tuple[Target, Target]], fields: dict[str, int], simple_fields: dict[str, int],
                   name_ids: dict[str, int], processed: set[int], scope: str) -> None:
    if method_name in ("setSmelting", "setSolidifying"):
        if len(args) != 2:
            raise SourceError(f"Unexpected {method_name} arity {len(args)} in {material.name}")
        target_id = resolve_material(args[0], env, fields, simple_fields, name_ids,
                                     material.material_id, name_ids, scope)
        amount = eval_int(args[1], env)
        target = Target(target_id, amount)
        if method_name == "setSmelting":
            material.hot = target
        else:
            material.cold = target
        return
    if method_name == "setAllToTheOutputOf":
        if not 1 <= len(args) <= 3:
            raise SourceError(f"Unexpected {method_name} arity {len(args)} in {material.name}")
        source_id = resolve_material(args[0], env, fields, simple_fields, name_ids,
                                     material.material_id, name_ids, scope)
        if source_id != material.material_id and source_id not in processed:
            raise SourceError(f"{material.name} copies target of not-yet-initialized ID {source_id}")
        source_hot, source_cold = states.get(source_id, (Target(source_id, U), Target(source_id, U)))
        if len(args) == 1:
            material.hot = Target(source_hot.material_id, source_hot.amount)
            material.cold = Target(source_cold.material_id, source_cold.amount)
        else:
            multiplier = eval_int(args[1], env)
            divider = eval_int(args[2], env) if len(args) == 3 else 1
            material.hot = Target(source_hot.material_id, java_divide(source_hot.amount * multiplier, divider))
            material.cold = Target(source_cold.material_id, java_divide(source_cold.amount * multiplier, divider))
        return
    raise SourceError(f"Unsupported GT6 target mutation {method_name}")


def walk_factory_calls(text: str, env: dict[str, str], material: Material, methods: dict[str, list[Method]],
                       states: dict[int, tuple[Target, Target]], fields: dict[str, int], simple_fields: dict[str, int],
                       name_ids: dict[str, int], processed: set[int], scope: str, depth: int = 0) -> None:
    if depth > 32:
        raise SourceError(f"GT6 factory recursion exceeded 32 levels for {material.name}")
    factory_names = set(methods)
    factory_pattern = re.compile(r"(?<![.\w])(?P<name>[A-Za-z_]\w*)\s*\(")
    mutation_pattern = re.compile(r"\.\s*(?P<name>setSmelting|setSolidifying|setAllToTheOutputOf)\s*\(")
    events: list[tuple[int, int, str, int, list[str]]] = []
    for match in factory_pattern.finditer(text):
        if match.group("name") not in factory_names:
            continue
        args, close = invocation_args(text, text.index("(", match.start()))
        events.append((match.start(), close, "factory", match.start("name"), [match.group("name"), *args]))
    for match in mutation_pattern.finditer(text):
        args, close = invocation_args(text, text.index("(", match.start()))
        events.append((match.start(), close, "mutation", match.start("name"), [match.group("name"), *args]))
    events.sort(key=lambda event: (event[0], 0 if event[2] == "factory" else 1))
    last_end = -1
    for _, close, kind, _, values in events:
        if close < last_end:
            continue
        last_end = close
        name, *args = values
        args = [substitute(arg, env).strip() for arg in args]
        if kind == "factory":
            method = choose_method(name, args, methods)
            nested_env = bind_method(method, args)
            walk_factory_calls(method.body, nested_env, material, methods, states, fields, simple_fields, name_ids,
                               processed, "MT", depth + 1)
        else:
            apply_mutation(name, args, env, material, states, fields, simple_fields, name_ids, processed, scope)


def apply_initializer(material: Material, methods: dict[str, list[Method]], states: dict[int, tuple[Target, Target]],
                      fields: dict[str, int], simple_fields: dict[str, int], name_ids: dict[str, int],
                      processed: set[int]) -> None:
    # The declaration itself supplies the factory arguments; helper factory
    # implementations are expanded from their actual MT.java method bodies.
    factory = re.match(r"(?P<name>[A-Za-z_]\w*)\s*\(", material.expression)
    if not factory:
        raise SourceError(f"Could not find root factory for {material.name}")
    args, close = invocation_args(material.expression, material.expression.index("(", factory.start()))
    method = choose_method(factory.group("name"), args, methods)
    env = bind_method(method, args)
    walk_factory_calls(method.body, env, material, methods, states, fields, simple_fields, name_ids, processed, "MT")
    tail = material.expression[close + 1:]
    walk_factory_calls(tail, {}, material, methods, states, fields, simple_fields, name_ids, processed, material.scope)


def apply_static_initializers(mt: str, materials: dict[int, Material], states: dict[int, tuple[Target, Target]],
                              fields: dict[str, int], simple_fields: dict[str, int], name_ids: dict[str, int],
                              processed: set[int]) -> None:
    # MT's top-level static block contains exactly the three native target
    # overrides. The enclosing source text and method contracts are hash-pinned.
    static = re.search(r"\bstatic\s*\{", mt)
    if not static:
        raise SourceError("MT.java static initializer not found")
    opening = mt.index("{", static.start())
    closing = matching(mt, opening, "{", "}")
    body = mt[opening + 1:closing]
    direct = re.compile(r"(?P<receiver>[A-Za-z_]\w*)\s*\.\s*(?P<name>setSmelting|setSolidifying|setAllToTheOutputOf)\s*\(")
    for match in direct.finditer(body):
        receiver = match.group("receiver")
        receiver_key = "MT." + receiver
        if receiver_key not in fields:
            raise SourceError(f"Unknown static-initializer receiver {receiver}")
        material_id = fields[receiver_key]
        args, _ = invocation_args(body, body.index("(", match.start()))
        material = materials[material_id]
        apply_mutation(match.group("name"), args, {}, material, states, fields, simple_fields,
                       name_ids, processed, "MT")
        states[material_id] = (material.hot, material.cold)


def normalize(name: str) -> str:
    return re.sub(r"[^a-z0-9]", "", name.lower())


def evaluate(mt_raw: str) -> tuple[list[Material], list[str], list[str]]:
    mt = strip_comments(mt_raw)
    all_materials = declarations(mt)
    methods = method_table(mt)
    scopes = class_scopes(mt)
    fields: dict[str, int] = {}
    field_candidates: dict[str, set[int]] = {}
    name_ids: dict[str, int] = {}
    for material in all_materials:
        name_ids[normalize(material.name)] = material.material_id
        for field in material.field_names:
            qualified = material.scope + "." + field
            if qualified in fields and fields[qualified] != material.material_id:
                raise SourceError(f"Qualified field {qualified} points to two IDs")
            fields[qualified] = material.material_id
            field_candidates.setdefault(field, set()).add(material.material_id)
    add_method_field_aliases(mt, scopes, zero_argument_material_methods(methods), fields, field_candidates)
    simple_fields = {name: next(iter(ids)) for name, ids in field_candidates.items() if len(ids) == 1}
    states = {material.material_id: (material.hot, material.cold) for material in all_materials}
    processed: set[int] = set()
    unresolved: list[str] = []
    for material in all_materials:
        try:
            apply_initializer(material, methods, states, fields, simple_fields, name_ids, processed)
            states[material.material_id] = (material.hot, material.cold)
            processed.add(material.material_id)
        except SourceError as error:
            unresolved.append(f"{material.material_id}|{material.name}|{material.line}|{error}")
            # Do not allow a later copy to treat this identity as successfully
            # evaluated; retain its constructor-default state only for tracing.
            states[material.material_id] = (material.hot, material.cold)
    try:
        apply_static_initializers(mt, {m.material_id: m for m in all_materials}, states, fields,
                                  simple_fields, name_ids, processed)
    except SourceError as error:
        unresolved.append(f"static-initializer|{error}")
    return all_materials, unresolved, HEADER


def render(materials: list[Material]) -> list[str]:
    by_id = {material.material_id: material for material in materials}
    rows = []
    for material in materials:
        hot = by_id.get(material.hot.material_id)
        cold = by_id.get(material.cold.material_id)
        if hot is None or cold is None:
            raise SourceError(f"Target ID missing from positive literal MT snapshot: {material.material_id}")
        rows.append("|".join((str(material.material_id), material.name, str(material.line),
                              str(material.hot.material_id), hot.name, str(material.hot.amount),
                              str(material.cold.material_id), cold.name, str(material.cold.amount))))
    return rows


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--gt6-root", type=Path, default=Path("E:/迅雷下载/gregtech6-master"))
    parser.add_argument("--fixture", type=Path, default=Path("src/test/resources/gt6-target-source-values.txt"))
    parser.add_argument("--write", action="store_true", help="explicitly rewrite the test fixture after source evaluation")
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
    mt_raw = source_paths["MT.java"].read_text(encoding="utf-8")
    materials, unresolved, header = evaluate(mt_raw)
    rows = render(materials)
    if unresolved:
        print(f"Source evaluation incomplete: resolved={len(materials) - len(unresolved)} total={len(materials)} unresolved={len(unresolved)}")
        for item in unresolved[:80]:
            print("UNRESOLVED " + item)
        return 2
    generated = header + rows
    if args.write:
        args.fixture.parent.mkdir(parents=True, exist_ok=True)
        args.fixture.write_text("\n".join(generated) + "\n", encoding="utf-8", newline="\n")
        print(f"Wrote {len(rows)} independently evaluated source target rows to {args.fixture}")
        if args.fixture == Path("src/test/resources/gt6-target-source-values.txt"):
            generator = Path(__file__).with_name("generateGt6JavaMaterialTables.py")
            generated_java = subprocess.run([sys.executable, str(generator), "--group", "targets"], check=False)
            if generated_java.returncode != 0:
                return generated_java.returncode
        return 0
    if not args.fixture.is_file():
        raise SourceError(f"Fixture missing; use --write only after reviewing source output: {args.fixture}")
    current = args.fixture.read_text(encoding="utf-8").splitlines()
    if current != generated:
        print(f"Source target fixture mismatch: expected {len(generated)} lines, found {len(current)}")
        for index, (actual, expected) in enumerate(zip(current, generated), 1):
            if actual != expected:
                print(f"line {index}: fixture={actual}\n         source={expected}")
                if index > 80:
                    break
        return 1
    print(f"GT6 source target table verified: {len(rows)} rows, 0 unresolved, 0 mismatches")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (OSError, SourceError) as error:
        print(f"ERROR: {error}", file=sys.stderr)
        raise SystemExit(2)

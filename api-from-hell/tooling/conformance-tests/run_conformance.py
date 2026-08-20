#!/usr/bin/env python3
"""Run catalog-driven HTTP conformance checks against an API From Hell server."""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from fromhelllib import (
    NO_BODY_STATUS_CODES,
    endpoint_body,
    endpoint_header,
    load_catalog,
    path_with_prefix,
    print_errors,
    request_raw,
    validate_catalog,
)


METHOD_CANDIDATES = ["GET", "POST", "PUT", "PATCH", "DELETE"]


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--base-url", required=True, help="Server origin, e.g. http://localhost:3001"
    )
    parser.add_argument("--catalog", help="Catalog JSON file")
    parser.add_argument("--prefix", default="/fromhell")
    parser.add_argument(
        "--proxy",
        help="Optional HTTP proxy URL, e.g. http://127.0.0.1:8080",
    )
    parser.add_argument(
        "--strict-no-body-statuses",
        action="store_true",
        help=argparse.SUPPRESS,
    )
    parser.add_argument(
        "--allow-normalized-no-body-statuses",
        action="store_true",
        help="Allow HTTP stacks to suppress catalogued 204/205/304 bodies.",
    )
    parser.add_argument(
        "--check-405-routing",
        action="store_true",
        help=(
            "Also send unsupported-method requests to known paths and require "
            "405 Method Not Allowed plus an Allow header."
        ),
    )
    args = parser.parse_args()

    catalog = load_catalog(args.catalog)
    errors = validate_catalog(catalog)
    if errors:
        return print_errors(errors)

    failures = []
    for endpoint in catalog.get("endpoints", []):
        failures.extend(check_endpoint(args.base_url, args.prefix, endpoint, args))

    if args.check_405_routing:
        failures.extend(check_wrong_methods(args.base_url, args.prefix, catalog, args.proxy))
    failures.extend(check_unknown_path(args.base_url, args.prefix, args.proxy))

    if failures:
        for failure in failures:
            print(failure, file=sys.stderr)
        return 1

    print(
        "Conformance passed for "
        + str(len(catalog.get("endpoints", [])))
        + " endpoints at "
        + args.base_url.rstrip("/")
        + args.prefix
    )
    return 0


def check_endpoint(base_url: str, prefix: str, endpoint: dict, args: argparse.Namespace) -> list[str]:
    failures = []
    method = endpoint["method"]
    status, headers, body = request_raw(
        base_url, method, endpoint["path"], prefix, proxy=args.proxy
    )
    expected_body = endpoint_body(endpoint).encode("utf-8")
    label = method + " " + path_with_prefix(endpoint["path"], prefix)

    if status != endpoint["statusCode"]:
        failures.append(f"{label}: expected status {endpoint['statusCode']} but got {status}")

    for header in endpoint.get("headers", []):
        actual = headers.get(header["name"].lower())
        if actual != header["value"]:
            failures.append(
                f"{label}: expected header {header['name']}={header['value']!r} but got {actual!r}"
            )

    if endpoint_header(endpoint, "Content-Type") is None and "content-type" in headers:
        failures.append(f"{label}: expected no Content-Type but got {headers['content-type']!r}")

    if status in NO_BODY_STATUS_CODES and expected_body and args.allow_normalized_no_body_statuses:
        return failures

    if body != expected_body:
        failures.append(body_mismatch_message(label, expected_body, body))
    return failures


def body_mismatch_message(label: str, expected: bytes, actual: bytes) -> str:
    mismatch_at = first_mismatch_index(expected, actual)
    return (
        f"{label}: body mismatch; expected length {len(expected)} but got {len(actual)}; "
        + byte_difference_message(expected, actual, mismatch_at)
        + "; "
        + byte_window_message("expected", expected, mismatch_at)
        + "; "
        + byte_window_message("actual", actual, mismatch_at)
        + "; "
        + text_window_message("expected", expected, mismatch_at)
        + "; "
        + text_window_message("actual", actual, mismatch_at)
    )


def first_mismatch_index(expected: bytes, actual: bytes) -> int:
    for index, expected_byte in enumerate(expected):
        if index >= len(actual) or expected_byte != actual[index]:
            return index
    return len(expected)


def byte_difference_message(expected: bytes, actual: bytes, index: int) -> str:
    return (
        f"first difference at byte {index}: "
        + f"expected {byte_value_at(expected, index)} but got {byte_value_at(actual, index)}"
    )


def byte_value_at(body: bytes, index: int) -> str:
    if index >= len(body):
        return "<missing>"
    return f"0x{body[index]:02x}"


def byte_window_message(label: str, body: bytes, index: int, radius: int = 16) -> str:
    start, end = window_bounds(body, index, radius)
    return f"{label} bytes[{start}:{end}]={body[start:end].hex(' ')}"


def text_window_message(label: str, body: bytes, index: int, radius: int = 16) -> str:
    start, end = window_bounds(body, index, radius)
    text = body[start:end].decode("utf-8", errors="backslashreplace")
    return f"{label} text[{start}:{end}]={text!r}"


def window_bounds(body: bytes, index: int, radius: int) -> tuple[int, int]:
    if not body:
        return 0, 0
    safe_index = min(index, len(body) - 1)
    start = max(0, safe_index - radius)
    end = min(len(body), safe_index + radius + 1)
    return start, end


def check_wrong_methods(
    base_url: str, prefix: str, catalog: dict, proxy: str | None = None
) -> list[str]:
    failures = []
    by_path = {}
    for endpoint in catalog.get("endpoints", []):
        by_path.setdefault(endpoint["path"], set()).add(endpoint["method"])

    for path, allowed in by_path.items():
        allowed_with_head_options = set(allowed)
        if "GET" in allowed:
            allowed_with_head_options.add("HEAD")
        allowed_with_head_options.add("OPTIONS")
        wrong_method = next(
            (method for method in METHOD_CANDIDATES if method not in allowed_with_head_options),
            None,
        )
        if not wrong_method:
            continue
        status, headers, _body = request_raw(
            base_url, wrong_method, path, prefix, proxy=proxy
        )
        label = wrong_method + " " + path_with_prefix(path, prefix)
        if status != 405:
            failures.append(f"{label}: expected wrong method status 405 but got {status}")
        if "allow" not in headers:
            failures.append(f"{label}: expected Allow header")
    return failures


def check_unknown_path(base_url: str, prefix: str, proxy: str | None = None) -> list[str]:
    status, _headers, _body = request_raw(
        base_url, "GET", "/not-in-catalog", prefix, proxy=proxy
    )
    if status != 404:
        return [
            "GET "
            + path_with_prefix("/not-in-catalog", prefix)
            + f": expected unknown path status 404 but got {status}"
        ]
    return []


if __name__ == "__main__":
    raise SystemExit(main())

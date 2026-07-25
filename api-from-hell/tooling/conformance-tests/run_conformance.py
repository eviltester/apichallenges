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
    parser.add_argument("--base-url", required=True, help="Server origin, e.g. http://localhost:3001")
    parser.add_argument("--catalog", help="Catalog JSON file")
    parser.add_argument("--prefix", default="/fromhell")
    parser.add_argument(
        "--strict-no-body-statuses",
        action="store_true",
        help="Fail if a 204/205/304 body is not observable on the wire.",
    )
    args = parser.parse_args()

    catalog = load_catalog(args.catalog)
    errors = validate_catalog(catalog)
    if errors:
        return print_errors(errors)

    failures = []
    for endpoint in catalog.get("endpoints", []):
        failures.extend(check_endpoint(args.base_url, args.prefix, endpoint, args))

    failures.extend(check_wrong_methods(args.base_url, args.prefix, catalog))
    failures.extend(check_unknown_path(args.base_url, args.prefix))

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
    status, headers, body = request_raw(base_url, method, endpoint["path"], prefix)
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

    if status in NO_BODY_STATUS_CODES and expected_body and not args.strict_no_body_statuses:
        return failures

    if body != expected_body:
        failures.append(
            f"{label}: expected body length {len(expected_body)} but got {len(body)}"
        )
    return failures


def check_wrong_methods(base_url: str, prefix: str, catalog: dict) -> list[str]:
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
        status, headers, _body = request_raw(base_url, wrong_method, path, prefix)
        label = wrong_method + " " + path_with_prefix(path, prefix)
        if status != 405:
            failures.append(f"{label}: expected wrong method status 405 but got {status}")
        if "allow" not in headers:
            failures.append(f"{label}: expected Allow header")
    return failures


def check_unknown_path(base_url: str, prefix: str) -> list[str]:
    status, _headers, _body = request_raw(base_url, "GET", "/not-in-catalog", prefix)
    if status != 404:
        return [
            "GET "
            + path_with_prefix("/not-in-catalog", prefix)
            + f": expected unknown path status 404 but got {status}"
        ]
    return []


if __name__ == "__main__":
    raise SystemExit(main())

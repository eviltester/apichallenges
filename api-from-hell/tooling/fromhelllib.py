"""Shared API From Hell catalog helpers.

The implementation scripts intentionally use only Python's standard library so that the catalog
can be validated and transformed in lightweight CI jobs.
"""

from __future__ import annotations

import http.client
import json
import sys
import urllib.parse
from pathlib import Path
from typing import Any


ALLOWED_METHODS = {
    "GET",
    "POST",
    "PUT",
    "PATCH",
    "DELETE",
    "HEAD",
    "OPTIONS",
    "TRACE",
}

NO_BODY_STATUS_CODES = {204, 205, 304}

COMMON_CORS_HEADERS = [
    {"name": "Access-Control-Allow-Origin", "value": "*"},
    {
        "name": "Access-Control-Allow-Methods",
        "value": "GET,POST,PUT,PATCH,DELETE,HEAD,OPTIONS",
    },
    {
        "name": "Access-Control-Allow-Headers",
        "value": "Content-Type, Origin, Accept, Authorization, Content-Length, X-Requested-With",
    },
]


def repository_root() -> Path:
    return Path(__file__).resolve().parents[1]


def default_catalog_path() -> Path:
    return repository_root() / "catalog" / "fromhell-catalog.json"


def load_catalog(path: str | Path | None = None) -> dict[str, Any]:
    catalog_path = Path(path) if path else default_catalog_path()
    with catalog_path.open("r", encoding="utf-8-sig") as catalog_file:
        return json.load(catalog_file)


def validate_catalog(catalog: dict[str, Any]) -> list[str]:
    errors: list[str] = []

    if not isinstance(catalog.get("name"), str) or not catalog.get("name"):
        errors.append("name must be a non-empty string")
    if not isinstance(catalog.get("description"), str):
        errors.append("description must be a string")

    endpoints = catalog.get("endpoints")
    if not isinstance(endpoints, list) or not endpoints:
        errors.append("endpoints must be a non-empty array")
        return errors

    seen: set[tuple[str, str]] = set()
    for index, endpoint in enumerate(endpoints):
        location = f"endpoints[{index}]"
        if not isinstance(endpoint, dict):
            errors.append(f"{location} must be an object")
            continue

        method = endpoint.get("method")
        path = endpoint.get("path")
        status_code = endpoint.get("statusCode")

        if method not in ALLOWED_METHODS:
            errors.append(f"{location}.method must be one of {sorted(ALLOWED_METHODS)}")
        if not isinstance(path, str) or not path.startswith("/") or "?" in path or "#" in path:
            errors.append(f"{location}.path must be a slash-prefixed path with no query/fragment")
        if isinstance(method, str) and isinstance(path, str):
            key = (method.upper(), path)
            if key in seen:
                errors.append(f"{location} duplicates {method.upper()} {path}")
            seen.add(key)

        if not isinstance(status_code, int) or status_code < 100 or status_code > 599:
            errors.append(f"{location}.statusCode must be an integer from 100 to 599")
        if not isinstance(endpoint.get("label"), str) or not endpoint.get("label"):
            errors.append(f"{location}.label must be a non-empty string")
        for text_field in ("documentation", "problem", "expectation", "body"):
            if text_field in endpoint and not isinstance(endpoint.get(text_field), str):
                errors.append(f"{location}.{text_field} must be a string")

        headers = endpoint.get("headers")
        if not isinstance(headers, list):
            errors.append(f"{location}.headers must be an array")
            continue
        for header_index, header in enumerate(headers):
            header_location = f"{location}.headers[{header_index}]"
            if not isinstance(header, dict):
                errors.append(f"{header_location} must be an object")
                continue
            if not isinstance(header.get("name"), str) or not header.get("name"):
                errors.append(f"{header_location}.name must be a non-empty string")
            if "value" not in header or not isinstance(header.get("value"), str):
                errors.append(f"{header_location}.value must be a string")

    return errors


def endpoint_header(endpoint: dict[str, Any], header_name: str) -> str | None:
    for header in endpoint.get("headers", []):
        if header.get("name", "").lower() == header_name.lower():
            return header.get("value", "")
    return None


def endpoint_body(endpoint: dict[str, Any]) -> str:
    body = endpoint.get("body")
    return body if isinstance(body, str) else ""


def path_with_prefix(path: str, prefix: str) -> str:
    clean_prefix = "/" + prefix.strip("/") if prefix else ""
    return clean_prefix + path


def build_openapi(
    catalog: dict[str, Any],
    server_url: str = "http://localhost:3001",
    prefix: str = "/fromhell",
) -> dict[str, Any]:
    paths: dict[str, Any] = {}

    for endpoint in catalog.get("endpoints", []):
        full_path = path_with_prefix(endpoint["path"], prefix)
        path_item = paths.setdefault(full_path, {})
        method = endpoint["method"].lower()
        content_type = endpoint_header(endpoint, "Content-Type")

        response: dict[str, Any] = {
            "description": endpoint.get("documentation") or endpoint.get("label") or "",
            "headers": response_headers(endpoint),
        }

        if content_type:
            response["content"] = {
                content_type: {
                    "schema": {"type": "string"},
                    "example": endpoint_body(endpoint),
                }
            }

        path_item[method] = {
            "summary": endpoint.get("label", ""),
            "description": operation_description(endpoint),
            "operationId": operation_id(endpoint),
            "responses": {str(endpoint.get("statusCode", 200)): response},
        }

    return {
        "openapi": "3.0.3",
        "info": {
            "title": catalog.get("name", "API From Hell"),
            "version": "1.0.0",
            "description": catalog.get("description", ""),
        },
        "servers": [{"url": server_url.rstrip("/")}],
        "paths": paths,
    }


def response_headers(endpoint: dict[str, Any]) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    for header in endpoint.get("headers", []):
        headers[header["name"]] = {
            "schema": {"type": "string"},
            "example": header.get("value", ""),
        }
    return headers


def operation_description(endpoint: dict[str, Any]) -> str:
    parts = []
    if endpoint.get("documentation"):
        parts.append(endpoint["documentation"])
    if endpoint.get("problem"):
        parts.append("Problem: " + endpoint["problem"])
    if endpoint.get("expectation"):
        parts.append("Expected client behavior: " + endpoint["expectation"])
    return "\n\n".join(parts)


def operation_id(endpoint: dict[str, Any]) -> str:
    raw = endpoint["method"].lower() + "-" + endpoint["path"].strip("/").replace("/", "-")
    return "".join(part.capitalize() if index else part for index, part in enumerate(raw.split("-")))


def build_mockoon(catalog: dict[str, Any]) -> dict[str, Any]:
    routes = []
    for endpoint in catalog.get("endpoints", []):
        routes.append(
            {
                "uuid": stable_uuid(endpoint),
                "type": "http",
                "documentation": endpoint.get("documentation", ""),
                "method": endpoint["method"].lower(),
                "endpoint": endpoint["path"].strip("/"),
                "responses": [
                    {
                        "uuid": stable_uuid(endpoint) + "-response",
                        "body": endpoint_body(endpoint),
                        "latency": 0,
                        "statusCode": endpoint["statusCode"],
                        "label": endpoint.get("label", ""),
                        "headers": endpoint.get("headers", []),
                    }
                ],
                "enabled": True,
                "responseMode": None,
            }
        )

    return {
        "uuid": "api-from-hell-generated",
        "lastMigration": 32,
        "name": catalog.get("name", "API From Hell"),
        "endpointPrefix": "",
        "latency": 0,
        "port": 3001,
        "routes": routes,
    }


def stable_uuid(endpoint: dict[str, Any]) -> str:
    slug = (endpoint["method"] + "-" + endpoint["path"].strip("/")).lower()
    cleaned = "".join(character if character.isalnum() else "-" for character in slug)
    return "fromhell-" + "-".join(part for part in cleaned.split("-") if part)


def request_raw(
    base_url: str,
    method: str,
    path: str,
    prefix: str = "/fromhell",
    timeout: float = 5.0,
) -> tuple[int, dict[str, str], bytes]:
    parsed = urllib.parse.urlparse(base_url)
    connection_class = (
        http.client.HTTPSConnection if parsed.scheme == "https" else http.client.HTTPConnection
    )
    port = parsed.port
    host = parsed.hostname or "localhost"
    connection = connection_class(host, port, timeout=timeout)
    full_path = path_with_prefix(path, prefix)
    try:
        connection.request(method, full_path, headers={"Accept": "*/*"})
        response = connection.getresponse()
        headers = {name.lower(): value for name, value in response.getheaders()}
        return response.status, headers, response.read()
    finally:
        connection.close()


def dump_json(data: dict[str, Any]) -> str:
    return json.dumps(data, indent=2, ensure_ascii=False) + "\n"


def print_errors(errors: list[str]) -> int:
    if not errors:
        return 0
    for error in errors:
        print(error, file=sys.stderr)
    return 1

"""Shared API From Hell catalog helpers.

The implementation scripts intentionally use only Python's standard library so that the catalog
can be validated and transformed in lightweight CI jobs.
"""

from __future__ import annotations

import json
import socket
import ssl
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
    proxy: str | None = None,
) -> tuple[int, dict[str, str], bytes]:
    parsed = urllib.parse.urlparse(base_url)
    port = parsed.port
    host = parsed.hostname or "localhost"
    full_path = path_with_prefix(path, prefix)
    connection = connection_for_request(parsed, host, port, timeout, proxy)
    request_target = request_target_for(parsed, host, port, full_path, proxy)
    try:
        connection.sendall(raw_http_request(method, request_target, parsed, host, port, proxy))
        return parse_raw_http_response(read_all(connection))
    finally:
        connection.close()


def connection_for_request(
    parsed: urllib.parse.ParseResult,
    host: str,
    port: int | None,
    timeout: float,
    proxy: str | None,
) -> socket.socket:
    if not proxy:
        connection = socket.create_connection(
            (host, port or default_port(parsed)), timeout=timeout
        )
        if parsed.scheme == "https":
            return ssl.create_default_context().wrap_socket(connection, server_hostname=host)
        return connection

    proxy_parsed = parse_proxy(proxy)
    connection = socket.create_connection(
        (proxy_parsed.hostname, proxy_parsed.port or 80), timeout=timeout
    )

    if parsed.scheme == "https":
        tunnel_through_proxy(connection, host, port or 443, timeout)
        return ssl.create_default_context().wrap_socket(connection, server_hostname=host)

    return connection


def request_target_for(
    parsed: urllib.parse.ParseResult,
    host: str,
    port: int | None,
    full_path: str,
    proxy: str | None,
) -> str:
    if proxy and parsed.scheme == "http":
        return absolute_request_uri(parsed, host, port, full_path)
    return full_path


def raw_http_request(
    method: str,
    request_target: str,
    parsed: urllib.parse.ParseResult,
    host: str,
    port: int | None,
    proxy: str | None,
) -> bytes:
    lines = [
        f"{method} {request_target} HTTP/1.1",
        f"Host: {host_header(parsed, host, port)}",
        "Accept: */*",
        "User-Agent: api-from-hell-conformance",
        "Connection: close",
    ]
    if proxy and parsed.scheme == "http":
        lines.append("Proxy-Connection: close")
    if method_allows_request_body(method):
        lines.append("Content-Length: 0")
    return ("\r\n".join(lines) + "\r\n\r\n").encode("iso-8859-1")


def method_allows_request_body(method: str) -> bool:
    return method.upper() in {"POST", "PUT", "PATCH"}


def read_all(connection: socket.socket) -> bytes:
    chunks = []
    while True:
        chunk = connection.recv(8192)
        if not chunk:
            return b"".join(chunks)
        chunks.append(chunk)


def parse_raw_http_response(response: bytes) -> tuple[int, dict[str, str], bytes]:
    header_end = response.find(b"\r\n\r\n")
    if header_end == -1:
        raise ValueError("HTTP response did not contain a header terminator")
    header_text = response[:header_end].decode("iso-8859-1")
    header_lines = header_text.split("\r\n")
    status = int(header_lines[0].split(" ", 2)[1])
    headers = {}
    for header_line in header_lines[1:]:
        separator = header_line.find(":")
        if separator > 0:
            headers[header_line[:separator].lower()] = header_line[separator + 1 :].strip()
    body = response[header_end + 4 :]
    if "chunked" in headers.get("transfer-encoding", "").lower():
        body = decode_chunked_body(body)
    elif "content-length" in headers:
        body = body[: int(headers["content-length"])]
    return status, headers, body


def decode_chunked_body(body: bytes) -> bytes:
    decoded = bytearray()
    position = 0
    while True:
        line_end = body.find(b"\r\n", position)
        if line_end == -1:
            raise ValueError("Chunked body did not contain a complete chunk size")
        size_text = body[position:line_end].split(b";", 1)[0]
        size = int(size_text, 16)
        position = line_end + 2
        if size == 0:
            return bytes(decoded)
        decoded.extend(body[position : position + size])
        position = position + size + 2


def tunnel_through_proxy(
    connection: socket.socket, host: str, port: int, timeout: float
) -> None:
    connection.settimeout(timeout)
    target = f"{host}:{port}"
    request = (
        f"CONNECT {target} HTTP/1.1\r\n"
        + f"Host: {target}\r\n"
        + "Proxy-Connection: close\r\n\r\n"
    )
    connection.sendall(request.encode("iso-8859-1"))
    status, _headers, _body = parse_raw_http_response(read_until_headers(connection))
    if status < 200 or status >= 300:
        raise ConnectionError(f"Proxy CONNECT failed with HTTP {status}")


def read_until_headers(connection: socket.socket) -> bytes:
    response = bytearray()
    while b"\r\n\r\n" not in response:
        chunk = connection.recv(8192)
        if not chunk:
            break
        response.extend(chunk)
    return bytes(response)


def default_port(parsed: urllib.parse.ParseResult) -> int:
    return 443 if parsed.scheme == "https" else 80


def parse_proxy(proxy: str) -> urllib.parse.ParseResult:
    proxy_with_scheme = proxy if "://" in proxy else "http://" + proxy
    parsed = urllib.parse.urlparse(proxy_with_scheme)
    if parsed.scheme != "http" or not parsed.hostname:
        raise ValueError("Proxy must be an HTTP URL, e.g. http://127.0.0.1:8080")
    return parsed


def absolute_request_uri(
    parsed: urllib.parse.ParseResult,
    host: str,
    port: int | None,
    full_path: str,
) -> str:
    scheme = parsed.scheme or "http"
    return urllib.parse.urlunparse(
        (scheme, host_header(parsed, host, port), full_path, "", "", "")
    )


def host_header(parsed: urllib.parse.ParseResult, host: str, port: int | None) -> str:
    if port is None or (parsed.scheme == "http" and port == 80) or (
        parsed.scheme == "https" and port == 443
    ):
        return host
    return f"{host}:{port}"


def dump_json(data: dict[str, Any]) -> str:
    return json.dumps(data, indent=2, ensure_ascii=False) + "\n"


def print_errors(errors: list[str]) -> int:
    if not errors:
        return 0
    for error in errors:
        print(error, file=sys.stderr)
    return 1

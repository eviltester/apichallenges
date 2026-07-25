#!/usr/bin/env python3
"""Standalone API From Hell implementation using Python's standard HTTP server."""

from __future__ import annotations

import json
import os
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path


PORT = int(os.environ.get("PORT", "3001"))
PREFIX = "/" + os.environ.get("FROMHELL_PREFIX", "/fromhell").strip("/")
if PREFIX == "/":
    PREFIX = ""
CATALOG_PATH = Path(
    os.environ.get(
        "FROMHELL_CATALOG",
        str(Path(__file__).resolve().parents[2] / "catalog" / "fromhell-catalog.json"),
    )
)

CATALOG = json.loads(CATALOG_PATH.read_text(encoding="utf-8-sig"))
ENDPOINTS_BY_PATH = {}
for endpoint in CATALOG["endpoints"]:
    ENDPOINTS_BY_PATH.setdefault(endpoint["path"], {})[endpoint["method"].upper()] = endpoint


class FromHellHandler(BaseHTTPRequestHandler):
    def do_GET(self):  # noqa: N802
        self.handle_method("GET")

    def do_POST(self):  # noqa: N802
        self.handle_method("POST")

    def do_PUT(self):  # noqa: N802
        self.handle_method("PUT")

    def do_PATCH(self):  # noqa: N802
        self.handle_method("PATCH")

    def do_DELETE(self):  # noqa: N802
        self.handle_method("DELETE")

    def do_HEAD(self):  # noqa: N802
        self.handle_method("HEAD")

    def do_OPTIONS(self):  # noqa: N802
        self.handle_method("OPTIONS")

    def handle_method(self, method: str) -> None:
        request_path = self.path.split("?", 1)[0]
        if method == "GET" and request_path == "/docs/openapi.json":
            self.send_json(openapi_for(self))
            return

        catalog_path = remove_prefix(request_path)
        if catalog_path not in ENDPOINTS_BY_PATH:
            body = b"Not Found"
            self.send_response(404)
            self.apply_common_headers()
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            self.wfile.write(body)
            return

        endpoints_for_path = ENDPOINTS_BY_PATH[catalog_path]
        allowed = allowed_methods(endpoints_for_path)
        if method == "OPTIONS":
            self.send_response(204)
            self.apply_common_headers()
            self.send_header("Allow", ", ".join(allowed))
            self.send_header("Content-Length", "0")
            self.end_headers()
            return

        endpoint = endpoints_for_path.get(method)
        if endpoint is None:
            body = b"Method Not Allowed"
            self.send_response(405)
            self.apply_common_headers()
            self.send_header("Allow", ", ".join(allowed))
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            self.wfile.write(body)
            return

        body = endpoint.get("body", "").encode("utf-8")
        self.send_response(endpoint["statusCode"])
        self.apply_common_headers()
        for header in endpoint.get("headers", []):
            self.send_header(header["name"], header["value"])
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        if method != "HEAD":
            self.wfile.write(body)

    def apply_common_headers(self) -> None:
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET,POST,PUT,PATCH,DELETE,HEAD,OPTIONS")
        self.send_header(
            "Access-Control-Allow-Headers",
            "Content-Type, Origin, Accept, Authorization, Content-Length, X-Requested-With",
        )

    def send_json(self, data: dict) -> None:
        body = json.dumps(data, indent=2).encode("utf-8")
        self.send_response(200)
        self.apply_common_headers()
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)


def remove_prefix(request_path: str) -> str | None:
    if not PREFIX:
        return request_path
    if request_path == PREFIX:
        return "/"
    if not request_path.startswith(PREFIX + "/"):
        return None
    return request_path[len(PREFIX) :]


def allowed_methods(endpoints_for_path: dict) -> list[str]:
    methods = list(endpoints_for_path.keys())
    if "GET" in methods and "HEAD" not in methods:
        methods.append("HEAD")
    methods.append("OPTIONS")
    return list(dict.fromkeys(methods))


def openapi_for(handler: FromHellHandler) -> dict:
    host = handler.headers.get("Host", f"localhost:{PORT}")
    paths = {}
    for endpoint in CATALOG["endpoints"]:
        full_path = PREFIX + endpoint["path"]
        paths.setdefault(full_path, {})[endpoint["method"].lower()] = {
            "summary": endpoint["label"],
            "description": "\n\n".join(
                item
                for item in [
                    endpoint.get("documentation", ""),
                    endpoint.get("problem", ""),
                    endpoint.get("expectation", ""),
                ]
                if item
            ),
            "responses": {
                str(endpoint["statusCode"]): {
                    "description": endpoint.get("documentation", endpoint["label"])
                }
            },
        }
    return {
        "openapi": "3.0.3",
        "info": {
            "title": CATALOG["name"],
            "version": "1.0.0",
            "description": CATALOG["description"],
        },
        "servers": [{"url": f"http://{host}"}],
        "paths": paths,
    }


if __name__ == "__main__":
    server = ThreadingHTTPServer(("", PORT), FromHellHandler)
    print(f"API From Hell python-native-http listening on http://localhost:{PORT}{PREFIX}")
    server.serve_forever()

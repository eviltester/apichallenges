#!/usr/bin/env python3
"""Standalone API From Hell implementation using Flask."""

from __future__ import annotations

import json
import os
from pathlib import Path

from flask import Flask, Response, jsonify, request


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

app = Flask(__name__)


@app.after_request
def add_common_headers(response):
    response.headers["Access-Control-Allow-Origin"] = "*"
    response.headers["Access-Control-Allow-Methods"] = "GET,POST,PUT,PATCH,DELETE,HEAD,OPTIONS"
    response.headers["Access-Control-Allow-Headers"] = (
        "Content-Type, Origin, Accept, Authorization, Content-Length, X-Requested-With"
    )
    return response


for catalog_path, endpoints_for_path in ENDPOINTS_BY_PATH.items():
    route_path = PREFIX + catalog_path
    methods = allowed_methods(endpoints_for_path)

    def handler(path=catalog_path):
        endpoints = ENDPOINTS_BY_PATH[path]
        if request.method == "OPTIONS":
            response = Response("", status=204)
            response.headers["Allow"] = ", ".join(allowed_methods(endpoints))
            return response

        endpoint = endpoints.get(request.method)
        if endpoint is None:
            response = Response("Method Not Allowed", status=405)
            response.headers["Allow"] = ", ".join(allowed_methods(endpoints))
            return response

        response = Response(endpoint.get("body", ""), status=endpoint["statusCode"])
        for header in endpoint.get("headers", []):
            response.headers[header["name"]] = header["value"]
        if not has_content_type(endpoint):
            response.headers.pop("Content-Type", None)
        return response

    app.add_url_rule(
        route_path,
        endpoint="fromhell_" + catalog_path.strip("/").replace("/", "_"),
        view_func=handler,
        methods=methods,
    )


@app.get("/docs/openapi.json")
def openapi():
    origin = request.host_url.rstrip("/")
    return jsonify(
        {
            "openapi": "3.0.3",
            "info": {
                "title": CATALOG["name"],
                "version": "1.0.0",
                "description": CATALOG["description"],
            },
            "servers": [{"url": origin}],
            "paths": {
                PREFIX + endpoint["path"]: {
                    endpoint["method"].lower(): {
                        "summary": endpoint["label"],
                        "responses": {
                            str(endpoint["statusCode"]): {
                                "description": endpoint.get(
                                    "documentation", endpoint["label"]
                                )
                            }
                        },
                    }
                }
                for endpoint in CATALOG["endpoints"]
            },
        }
    )


def allowed_methods(endpoints_for_path: dict) -> list[str]:
    methods = list(endpoints_for_path.keys())
    if "GET" in methods and "HEAD" not in methods:
        methods.append("HEAD")
    methods.append("OPTIONS")
    return list(dict.fromkeys(methods))


def has_content_type(endpoint: dict) -> bool:
    return any(header["name"].lower() == "content-type" for header in endpoint.get("headers", []))


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=PORT)

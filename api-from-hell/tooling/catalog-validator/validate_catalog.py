#!/usr/bin/env python3
"""Validate an API From Hell catalog."""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from fromhelllib import load_catalog, print_errors, validate_catalog


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("catalog", nargs="?", help="Catalog JSON file")
    args = parser.parse_args()

    catalog = load_catalog(args.catalog)
    errors = validate_catalog(catalog)
    if errors:
        return print_errors(errors)

    print(
        "Catalog is valid: "
        + str(len(catalog.get("endpoints", [])))
        + " endpoints in "
        + (args.catalog or "default catalog")
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

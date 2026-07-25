#!/usr/bin/env python3
"""Generate a Mockoon environment from the API From Hell catalog."""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from fromhelllib import build_mockoon, dump_json, load_catalog, print_errors, validate_catalog


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("catalog", nargs="?", help="Catalog JSON file")
    parser.add_argument("--output", help="Output file. Defaults to stdout.")
    args = parser.parse_args()

    catalog = load_catalog(args.catalog)
    errors = validate_catalog(catalog)
    if errors:
        return print_errors(errors)

    output = dump_json(build_mockoon(catalog))
    if args.output:
        Path(args.output).write_text(output, encoding="utf-8")
    else:
        print(output, end="")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

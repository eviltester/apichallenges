# Python Flask

Standalone API From Hell implementation using Flask.

Run from this folder:

```bash
python -m venv .venv
. .venv/bin/activate
pip install -r requirements.txt
python server.py
```

Configuration:

- `PORT`, default `3001`
- `FROMHELL_PREFIX`, default `/fromhell`
- `FROMHELL_CATALOG`, default `../../catalog/fromhell-catalog.json`

Flask may normalize some deliberately awkward responses, especially no-body status codes.

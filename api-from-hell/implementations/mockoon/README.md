# Mockoon

`fromhell.generated.json` is generated from the shared catalog.

Regenerate from the `api-from-hell` folder:

```bash
python tooling/mockoon-generator/generate_mockoon.py \
  catalog/fromhell-catalog.json \
  --output implementations/mockoon/fromhell.generated.json
```

Do not edit the generated file directly. Change `catalog/fromhell-catalog.json` instead.

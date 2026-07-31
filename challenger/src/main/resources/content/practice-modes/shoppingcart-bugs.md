---
title: Buggy API Deliberate Bugs
seo_title: Buggy API Deliberate Bugs for Testers | API Challenges
description: Maintainer notes for the deliberate Buggy API bugs.
lastmod: 2026-07-31
seo_description: Maintainer notes for Buggy API deliberate bugs, trigger examples, clean-mode expectations, and regression coverage ideas.
sitemap: false
---

# Buggy API Deliberate Bugs

This page is intentionally not linked from the navigation or sitemap.

The Buggy API is buggy by default. Start with `-shopbugs=none` to switch bugs off.

## Default Bugs

- Any cart can be viewed by id without using the cart owner's bearer token.
- Cart item create/update accepts quantities greater than current product stock.
- Cart item create/update accepts negative quantities.
- Cart item create/update accepts zero quantities.
- Cart item creation accepts hidden `unitPriceAtAdd` and `stockAtAdd` fields even though they are not advertised in the request schema.
- `BOOK_API` checkout can use `stockAtAdd` instead of current stock for availability checks.
- Checkout can use `stockAtAdd` instead of current stock when a product's current stock reaches zero after the item was added to a cart.
- `DVD_BUGS` checkout decrements product stock by 1 per line instead of by quantity.
- `CD_STATUS` checkout can allow stock to become negative.
- Closed carts can still be modified.
- A cart can be checked out more than once, reducing stock again.
- `POST /shop/carts/{cartId}/items` accepts a valid bearer token from another cart when the body updates an existing `id`.
- JSON input types are not fully validated against the domain model; for example, an integer such as `4` can be accepted for a STRING field because it can be converted to `"4"`.

## Clean Behaviour Reference

When started with `-shopbugs=none`:

- Cart quantities must be positive.
- Cart quantity cannot exceed current product stock.
- Closed carts cannot be changed.
- Checkout fails if current stock cannot fulfil the cart.
- Checkout reduces product stock by the exact cart quantity.
- Checkout can only happen once for a cart.
- A token from another cart cannot amend or checkout this cart.

## Testing Notes

- Use `POST /shop/register` twice to create two carts and compare bearer-token behaviour.
- Use `GET /shop/products` to identify `BOOK_API`, `DVD_BUGS`, and `CD_STATUS`.
- Add an item to one cart, use another cart to reduce that product stock to zero, then checkout the first cart.
- Add more `CD_STATUS` units than are in stock, then checkout to observe negative stock.
- Checkout a cart, then try to modify it or checkout again.

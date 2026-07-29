---
title: Buggy API
seo_title: Buggy API Practice Mode for Testers | API Challenges
description: A deliberately buggy shopping cart API for practising API testing with auth, cart, stock, and checkout rules.
lastmod: 2026-07-29
seo_description: Practise API testing against a deliberately buggy shopping cart API with bearer-token cart changes, catalogue stock management, and checkout rules.
og_image: /images/hero/buggy-api-shopping-cart-1600x720.jpg
og_image_alt: Buggy API hero image showing the Buggy API ER Model with the headline Break a shopping cart on purpose.
schema_image: /images/hero/buggy-api-shopping-cart-1600x720.jpg
twitter_card: summary_large_image
---

# Buggy API

<figure class="content-hero-figure buggy-api-hero-image">
  <img src="/images/hero/buggy-api-shopping-cart-1600x720.jpg" width="1600" height="720" loading="eager" decoding="async" alt="Buggy API hero image showing the Buggy API ER Model with the headline Break a shopping cart on purpose.">
</figure>

The Buggy API is a small public Shopping Cart practice API mounted at `/shop`.

It is deliberately buggy by default, so it is useful for practising exploratory API testing, auth checks, business-rule testing, and checkout validation.

## Main Workflow

1. Create a cart with `POST /shop/register`.
2. Copy the returned `token`.
3. View catalogue products with `GET /shop/products`.
4. Add items with `POST /shop/carts/{cartId}/items` using `Authorization: Bearer {token}`.
5. Review a cart with `GET /shop/carts/{cartId}`.
6. Checkout with `POST /shop/checkout/{cartId}` using the same bearer token.

## Domain Rules

- Anyone can view products.
- Only the cart owner token should allow cart mutation or checkout.
- Product catalogue data is read-only.
- Product stock is maintained automatically so at least 10 products have stock greater than zero.
- Stock maintenance never deletes products.
- Old carts are pruned so only 100 carts remain in memory.

## Useful Links

- [Buggy API Docs](/shop/docs)
- [Buggy API Swagger UI](/shop/docs/swagger-ui)
- [Buggy API Data Explorer](/shop/gui/entities)
- [Buggy API OpenAPI Files](/practice-modes/shoppingcart-openapi)

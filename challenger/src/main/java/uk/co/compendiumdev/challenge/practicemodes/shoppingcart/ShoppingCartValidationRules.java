package uk.co.compendiumdev.challenge.practicemodes.shoppingcart;

import uk.co.compendiumdev.thingifier.core.domain.definitions.EntityDefinition;
import uk.co.compendiumdev.thingifier.core.domain.definitions.field.instance.FieldValue;
import uk.co.compendiumdev.thingifier.core.domain.definitions.validation.EntityDomainValidationContext;
import uk.co.compendiumdev.thingifier.core.domain.instances.EntityInstance;
import uk.co.compendiumdev.thingifier.core.reporting.ValidationReport;

final class ShoppingCartValidationRules {

    static final String QUANTITY_MUST_BE_POSITIVE = "quantity must be greater than 0";
    static final String QUANTITY_EXCEEDS_CURRENT_PRODUCT_STOCK =
            "quantity exceeds current product stock";

    private ShoppingCartValidationRules() {}

    static ValidationReport quantityMustBePositive(final FieldValue quantity) {
        if (quantity.asInteger() <= 0) {
            return invalid(QUANTITY_MUST_BE_POSITIVE);
        }
        return valid();
    }

    static ValidationReport quantityMustNotExceedProductStock(
            final EntityDomainValidationContext context) {
        final EntityInstance product = productForCartItem(context);
        if (product == null) {
            return valid();
        }

        if (ShoppingCartSupport.intValue(context.candidate(), "quantity")
                > ShoppingCartSupport.intValue(product, "stock")) {
            return invalid(QUANTITY_EXCEEDS_CURRENT_PRODUCT_STOCK);
        }
        return valid();
    }

    private static EntityInstance productForCartItem(final EntityDomainValidationContext context) {
        final String productId = ShoppingCartSupport.stringValue(context.candidate(), "productId");
        final EntityDefinition product = context.schema().getEntityDefinitionNamed("product");
        return context.store().entityQueries().findByPrimaryKey(product, productId);
    }

    private static ValidationReport valid() {
        return new ValidationReport();
    }

    private static ValidationReport invalid(final String message) {
        return new ValidationReport().setValid(false).addErrorMessage(message);
    }
}

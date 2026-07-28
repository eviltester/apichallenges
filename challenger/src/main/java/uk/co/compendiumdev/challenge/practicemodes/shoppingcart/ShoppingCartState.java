package uk.co.compendiumdev.challenge.practicemodes.shoppingcart;

import java.util.concurrent.atomic.AtomicInteger;

final class ShoppingCartState {

    private final AtomicInteger tick = new AtomicInteger(1);

    int nextTick() {
        return tick.getAndIncrement();
    }

    int currentTick() {
        return tick.get();
    }
}

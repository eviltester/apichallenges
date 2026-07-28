package uk.co.compendiumdev.challenge.practicemodes.shoppingcart;

public enum ShoppingCartBugMode {
    CLASSIC("classic"),
    NONE("none");

    public static final String ARGUMENT_NAME = "-shopbugs";

    private final String argumentValue;

    ShoppingCartBugMode(final String argumentValue) {
        this.argumentValue = argumentValue;
    }

    public static ShoppingCartBugMode fromArgs(final String[] args) {
        if (args == null) {
            return CLASSIC;
        }

        for (String arg : args) {
            if (arg == null) {
                continue;
            }
            if (arg.equalsIgnoreCase(ARGUMENT_NAME + "=none")) {
                return NONE;
            }
        }
        return CLASSIC;
    }

    public boolean bugsEnabled() {
        return this != NONE;
    }

    public String argumentValue() {
        return argumentValue;
    }
}

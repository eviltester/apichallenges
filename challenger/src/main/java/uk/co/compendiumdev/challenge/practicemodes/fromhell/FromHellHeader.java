package uk.co.compendiumdev.challenge.practicemodes.fromhell;

public final class FromHellHeader {

    private final String name;
    private final String value;

    public FromHellHeader(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }
}

package br.com.readroid.catalogodemo;

/** One locally defined demonstration entry. All copy and titles are fictional. */
public final class CatalogItem {
    public enum Type {
        MOVIE("Filmes"), SERIES("Séries"), CHANNEL("Canais");

        private final String label;

        Type(String label) { this.label = label; }

        public String label() { return label; }
    }

    private final String id;
    private final String title;
    private final String description;
    private final Type type;

    public CatalogItem(String id, String title, String description, Type type) {
        if (id == null || id.isBlank() || title == null || title.isBlank()
                || description == null || type == null) {
            throw new IllegalArgumentException("Dados do item inválidos");
        }
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
    }

    public String id() { return id; }
    public String title() { return title; }
    public String description() { return description; }
    public Type type() { return type; }
}

package br.com.readroid.catalogodemo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Offline fixture data, intentionally unrelated to any analyzed app content. */
public final class CatalogRepository {
    private final List<CatalogItem> items;

    public CatalogRepository() {
        List<CatalogItem> demo = new ArrayList<>();
        demo.add(new CatalogItem("film-aurora", "A Cidade das Marés",
                "Demonstração fictícia: uma cartógrafa encontra mapas que mudam com a lua.",
                CatalogItem.Type.MOVIE));
        demo.add(new CatalogItem("film-orbit", "Jardim Orbital",
                "Demonstração fictícia: uma equipe cuida de um jardim em uma estação distante.",
                CatalogItem.Type.MOVIE));
        demo.add(new CatalogItem("series-atlas", "Pequeno Atlas",
                "Demonstração fictícia: cada episódio visita uma paisagem imaginária.",
                CatalogItem.Type.SERIES));
        demo.add(new CatalogItem("series-oficina", "Oficina do Amanhã",
                "Demonstração fictícia: inventores resolvem desafios cotidianos.",
                CatalogItem.Type.SERIES));
        demo.add(new CatalogItem("channel-observatory", "Canal Observatório",
                "Demonstração fictícia de uma entrada de canal, sem transmissão ao vivo.",
                CatalogItem.Type.CHANNEL));
        demo.add(new CatalogItem("channel-garden", "Canal Jardim",
                "Demonstração fictícia de uma entrada de canal, sem conexão de rede.",
                CatalogItem.Type.CHANNEL));
        items = Collections.unmodifiableList(demo);
    }

    public List<CatalogItem> all() { return items; }

    public List<CatalogItem> byType(CatalogItem.Type type) {
        if (type == null) throw new IllegalArgumentException("Tipo ausente");
        List<CatalogItem> result = new ArrayList<>();
        for (CatalogItem item : items) if (item.type() == type) result.add(item);
        return Collections.unmodifiableList(result);
    }

    public CatalogItem find(String id) {
        if (id == null) return null;
        for (CatalogItem item : items) if (item.id().equals(id)) return item;
        return null;
    }
}

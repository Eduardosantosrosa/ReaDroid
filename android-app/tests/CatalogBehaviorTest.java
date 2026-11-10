package br.com.readroid.catalogodemo;

/** Dependency-free JVM tests for demo data and the observed navigation flow. */
public final class CatalogBehaviorTest {
    public static void main(String[] args) {
        repositoryFiltersAndLooksUpItems();
        navigationMovesHomeListDetailAndBack();
        invalidNavigationIsRejected();
        System.out.println("3 tests passaram.");
    }

    private static void repositoryFiltersAndLooksUpItems() {
        CatalogRepository repository = new CatalogRepository();
        check(repository.all().size() == 6, "esperava seis itens de demonstração");
        check(repository.byType(CatalogItem.Type.MOVIE).size() == 2, "filtro de filmes");
        check(repository.byType(CatalogItem.Type.SERIES).size() == 2, "filtro de séries");
        check(repository.byType(CatalogItem.Type.CHANNEL).size() == 2, "filtro de canais");
        check(repository.find("film-aurora") != null, "busca pelo id conhecido");
        check(repository.find("missing") == null, "id desconhecido deve ser ausente");
    }

    private static void navigationMovesHomeListDetailAndBack() {
        CatalogRepository repository = new CatalogRepository();
        CatalogNavigator navigator = new CatalogNavigator();
        check(navigator.current().screen == CatalogNavigator.Screen.HOME, "rota inicial");
        navigator.openList(CatalogItem.Type.MOVIE);
        check(navigator.current().screen == CatalogNavigator.Screen.LIST, "abre lista");
        CatalogItem item = repository.byType(CatalogItem.Type.MOVIE).get(0);
        navigator.openDetail(item);
        check(navigator.current().screen == CatalogNavigator.Screen.DETAIL, "abre detalhe");
        check(item.id().equals(navigator.current().itemId), "detalhe preserva id");
        check(navigator.back() && navigator.current().screen == CatalogNavigator.Screen.LIST,
                "volta ao filtro da lista");
        check(navigator.back() && navigator.current().screen == CatalogNavigator.Screen.HOME,
                "volta à home");
        check(!navigator.back(), "home não possui rota anterior");
    }

    private static void invalidNavigationIsRejected() {
        CatalogNavigator navigator = new CatalogNavigator();
        expectIllegalArgument(() -> navigator.openDetail(
                new CatalogItem("x", "X", "Fictício", CatalogItem.Type.MOVIE)));
        navigator.openList(CatalogItem.Type.MOVIE);
        expectIllegalArgument(() -> navigator.openDetail(
                new CatalogItem("y", "Y", "Fictício", CatalogItem.Type.SERIES)));
    }

    private static void expectIllegalArgument(Runnable action) {
        try {
            action.run();
            throw new AssertionError("esperava IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

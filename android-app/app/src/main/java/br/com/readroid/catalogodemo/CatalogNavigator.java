package br.com.readroid.catalogodemo;

import java.util.ArrayDeque;
import java.util.Deque;

/** Small navigation state machine for the demonstrated home/list/detail flow. */
public final class CatalogNavigator {
    public enum Screen { HOME, LIST, DETAIL }

    public static final class Route {
        public final Screen screen;
        public final CatalogItem.Type type;
        public final String itemId;

        private Route(Screen screen, CatalogItem.Type type, String itemId) {
            this.screen = screen;
            this.type = type;
            this.itemId = itemId;
        }

        static Route home() { return new Route(Screen.HOME, null, null); }
        static Route list(CatalogItem.Type type) { return new Route(Screen.LIST, type, null); }
        static Route detail(CatalogItem.Type type, String itemId) {
            return new Route(Screen.DETAIL, type, itemId);
        }
    }

    private final Deque<Route> backStack = new ArrayDeque<>();
    private Route current = Route.home();

    public Route current() { return current; }

    public void openList(CatalogItem.Type type) {
        if (type == null) throw new IllegalArgumentException("Tipo ausente");
        backStack.push(current);
        current = Route.list(type);
    }

    public void openDetail(CatalogItem item) {
        if (item == null || current.screen != Screen.LIST || item.type() != current.type) {
            throw new IllegalArgumentException("Item não pertence à lista atual");
        }
        backStack.push(current);
        current = Route.detail(item.type(), item.id());
    }

    public boolean back() {
        if (backStack.isEmpty()) return false;
        current = backStack.pop();
        return true;
    }
}

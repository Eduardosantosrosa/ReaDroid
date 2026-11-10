package br.com.readroid.catalogodemo;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

/** Self-contained offline showcase using platform Android widgets only. */
public final class MainActivity extends Activity {
    private static final int INK = Color.rgb(20, 42, 52);
    private static final int MUTED = Color.rgb(91, 111, 119);
    private static final int ACCENT = Color.rgb(19, 124, 112);
    private static final int PAPER = Color.rgb(246, 248, 247);
    private static final int CARD = Color.WHITE;

    private final CatalogRepository catalog = new CatalogRepository();
    private final CatalogNavigator navigator = new CatalogNavigator();
    private LinearLayout page;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(16, 42, 53));
        getWindow().setNavigationBarColor(Color.rgb(16, 42, 53));
        render();
    }

    @Override public void onBackPressed() {
        if (navigator.back()) render();
        else super.onBackPressed();
    }

    private void render() {
        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setBackgroundColor(PAPER);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(20), dp(14), dp(20), dp(14));
        header.setBackgroundColor(Color.WHITE);
        if (navigator.current().screen != CatalogNavigator.Screen.HOME) {
            Button back = new Button(this);
            back.setText("‹");
            back.setTextSize(28);
            back.setTextColor(INK);
            back.setBackgroundColor(Color.TRANSPARENT);
            back.setContentDescription("Voltar");
            back.setOnClickListener(v -> { navigator.back(); render(); });
            header.addView(back, new LinearLayout.LayoutParams(dp(48), dp(48)));
        }
        LinearLayout headings = new LinearLayout(this);
        headings.setOrientation(LinearLayout.VERTICAL);
        TextView title = text("Catálogo de demonstração", 18, INK, true);
        TextView subtitle = text("Offline · conteúdo fictício", 12, MUTED, false);
        headings.addView(title);
        headings.addView(subtitle);
        header.addView(headings);
        shell.addView(header);

        ScrollView scroll = new ScrollView(this);
        page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(20), dp(22), dp(20), dp(28));
        scroll.addView(page);
        shell.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(shell);

        CatalogNavigator.Route route = navigator.current();
        if (route.screen == CatalogNavigator.Screen.HOME) renderHome();
        else if (route.screen == CatalogNavigator.Screen.LIST) renderList(route.type);
        else renderDetail(route.itemId);
    }

    private void renderHome() {
        page.addView(text("Explore o catálogo", 26, INK, true));
        page.addView(spacer(8));
        page.addView(text("Este aplicativo é uma demonstração independente. Os itens abaixo são dados locais fictícios.",
                15, MUTED, false));
        page.addView(spacer(24));
        for (CatalogItem.Type type : CatalogItem.Type.values()) {
            int count = catalog.byType(type).size();
            addCard(type.label(), count + (count == 1 ? " item de demonstração" : " itens de demonstração"),
                    "Abrir lista", () -> { navigator.openList(type); render(); });
        }
        page.addView(spacer(10));
        page.addView(text("Escopo demonstrativo", 18, INK, true));
        page.addView(spacer(6));
        page.addView(text("A navegação entre lista e detalhe é uma implementação local baseada em indícios dos nomes de layouts. Login, serviços online e reprodução não foram confirmados nem implementados.",
                13, MUTED, false));
    }

    private void renderList(CatalogItem.Type type) {
        page.addView(text(type.label(), 26, INK, true));
        page.addView(spacer(6));
        page.addView(text("Amostra fictícia · nenhuma conexão de rede", 14, MUTED, false));
        page.addView(spacer(18));
        List<CatalogItem> items = catalog.byType(type);
        for (CatalogItem item : items) {
            addCard(item.title(), item.description(), "Ver detalhes", () -> {
                navigator.openDetail(item);
                render();
            });
        }
    }

    private void renderDetail(String itemId) {
        CatalogItem item = catalog.find(itemId);
        if (item == null) {
            page.addView(text("Item de demonstração indisponível", 20, INK, true));
            return;
        }
        page.addView(text(item.type().label(), 13, ACCENT, true));
        page.addView(spacer(10));
        page.addView(text(item.title(), 28, INK, true));
        page.addView(spacer(14));
        page.addView(text(item.description(), 16, MUTED, false));
        page.addView(spacer(22));
        TextView notice = text("Conteúdo inteiramente fictício. Esta tela não inicia reprodução nem acessa serviços externos.",
                14, INK, false);
        notice.setPadding(dp(14), dp(14), dp(14), dp(14));
        notice.setBackground(rounded(Color.rgb(226, 239, 235), dp(12)));
        page.addView(notice);
    }

    private void addCard(String heading, String description, String action, Runnable onClick) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(14));
        card.setBackground(rounded(CARD, dp(16)));
        card.setElevation(dp(2));
        TextView title = text(heading, 18, INK, true);
        TextView body = text(description, 14, MUTED, false);
        body.setPadding(0, dp(8), 0, dp(10));
        Button button = new Button(this);
        button.setText(action);
        button.setTextColor(ACCENT);
        button.setAllCaps(false);
        button.setOnClickListener(v -> onClick.run());
        card.addView(title);
        card.addView(body);
        card.addView(button);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.bottomMargin = dp(14);
        page.addView(card, params);
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setLineSpacing(dp(2), 1f);
        return view;
    }

    private View spacer(int height) {
        View view = new View(this);
        view.setLayoutParams(new LinearLayout.LayoutParams(1, dp(height)));
        return view;
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(radius);
        return shape;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}

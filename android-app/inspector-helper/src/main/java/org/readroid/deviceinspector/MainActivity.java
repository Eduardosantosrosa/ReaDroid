package org.readroid.deviceinspector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.security.SecureRandom;

/** Permission/setup screen. Accessibility and screen capture require explicit user approval. */
public final class MainActivity extends Activity {
    private static final int REQUEST_CAPTURE = 301;
    private String pairingCode;
    private TextView status;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        pairingCode = getPreferences(MODE_PRIVATE).getString("pairing_code", null);
        if (pairingCode == null) {
            byte[] random = new byte[8];
            new SecureRandom().nextBytes(random);
            StringBuilder code = new StringBuilder(16);
            for (byte value : random) code.append(String.format(java.util.Locale.US, "%02x", value & 0xff));
            pairingCode = code.toString();
            getPreferences(MODE_PRIVATE).edit().putString("pairing_code", pairingCode).apply();
        }
        buildUi();
    }

    @Override protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(24), dp(28), dp(24), dp(28));
        page.setBackgroundColor(0xfff5f7f6);
        scroll.addView(page);

        TextView title = label("ReaDroid Device Inspector", 24, 0xff142a34, true);
        page.addView(title);
        TextView intro = label("Auxiliar local de inspeção. Não envia dados pela internet nem grava capturas no próprio app.", 15, 0xff526770, false);
        LinearLayout.LayoutParams introParams = params();
        introParams.topMargin = dp(12);
        page.addView(intro, introParams);

        TextView code = label("Código de conexão local\n" + pairingCode + "\n127.0.0.1:8765", 20, 0xff137c70, true);
        LinearLayout.LayoutParams codeParams = params();
        codeParams.topMargin = dp(20);
        page.addView(code, codeParams);

        status = label("Verificando autorizações…", 14, 0xff526770, false);
        LinearLayout.LayoutParams statusParams = params();
        statusParams.topMargin = dp(16);
        page.addView(status, statusParams);

        Button accessibility = button("1 · Abrir configurações de acessibilidade");
        accessibility.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        page.addView(accessibility, topMargin(20));

        Button projection = button("2 · Autorizar captura desta sessão");
        projection.setOnClickListener(v -> {
            MediaProjectionManager manager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            startActivityForResult(manager.createScreenCaptureIntent(), REQUEST_CAPTURE);
        });
        page.addView(projection, topMargin(10));

        TextView note = label("O Android pedirá confirmação manual para compartilhar a tela. A captura começa somente após essa confirmação. Para parar, volte a este app e encerre a sessão.", 13, 0xff526770, false);
        page.addView(note, topMargin(16));
        setContentView(scroll);
        refreshStatus();
    }

    private void refreshStatus() {
        if (status == null) return;
        boolean enabled = DeviceAccessibilityService.isEnabled(this);
        boolean active = InspectorBridgeService.isRunning();
        status.setText("Acessibilidade: " + (enabled ? "habilitada" : "precisa habilitar")
                + "\nCaptura/ponte local: " + (active ? "ativa" : "aguardando autorização"));
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == REQUEST_CAPTURE) {
            if (result == RESULT_OK && data != null) {
                Intent start = new Intent(this, InspectorBridgeService.class)
                        .putExtra(InspectorBridgeService.EXTRA_RESULT_CODE, result)
                        .putExtra(InspectorBridgeService.EXTRA_RESULT_DATA, data)
                        .putExtra(InspectorBridgeService.EXTRA_PAIRING_CODE, pairingCode);
                startForegroundService(start);
            } else {
                status.setText("Captura não autorizada; nenhum quadro foi capturado.");
            }
        }
    }

    private Button button(String title) {
        Button button = new Button(this);
        button.setText(title);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        return button;
    }

    private TextView label(String value, int size, int color, boolean bold) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);
        if (bold) text.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        text.setLineSpacing(dp(3), 1f);
        return text;
    }

    private LinearLayout.LayoutParams params() { return new LinearLayout.LayoutParams(-1, -2); }
    private LinearLayout.LayoutParams topMargin(int margin) {
        LinearLayout.LayoutParams p = params(); p.topMargin = dp(margin); return p;
    }
    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }
}

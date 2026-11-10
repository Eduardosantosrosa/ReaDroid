package br.com.independenttv.player;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public class PlayerActivity extends Activity {
    private ExoPlayer player;
    private PlayerView playerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        );

        String url = getIntent().getStringExtra("url");
        String title = getIntent().getStringExtra("title");

        if (url == null || url.trim().isEmpty()) {
            showError("Endereço de reprodução não informado.");
            return;
        }

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(0xFF000000);

        playerView = new PlayerView(this);
        playerView.setUseController(true);
        playerView.setShowBuffering(
            PlayerView.SHOW_BUFFERING_WHEN_PLAYING
        );

        root.addView(
            playerView,
            new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        );

        setContentView(root);

        try {
            player = new ExoPlayer.Builder(this).build();
            playerView.setPlayer(player);

            player.addListener(new Player.Listener() {
                @Override
                public void onPlayerError(PlaybackException error) {
                    showError(
                        "Falha na reprodução: " + error.getMessage()
                    );
                }
            });

            MediaItem mediaItem = new MediaItem.Builder()
                .setUri(url)
                .setMediaId(title == null ? "" : title)
                .build();

            player.setMediaItem(mediaItem);
            player.prepare();
            player.play();

        } catch (Exception error) {
            showError("Erro ao iniciar o player: " + error.getMessage());
        }
    }

    private void showError(String message) {
        TextView view = new TextView(this);
        view.setText(message + "\n\nPressione VOLTAR para sair.");
        view.setTextColor(0xFFFFFFFF);
        view.setTextSize(18);
        view.setPadding(32, 32, 32, 32);
        view.setGravity(android.view.Gravity.CENTER);
        setContentView(view);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (playerView != null) {
            playerView.setPlayer(null);
        }

        if (player != null) {
            player.release();
            player = null;
        }
    }
}

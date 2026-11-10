"""Primeira interface Kivy: celular e navegação básica por D-pad."""
from kivy.app import App
from kivy.clock import Clock
from kivy.metrics import dp
from kivy.core.window import Window
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.label import Label
from kivy.uix.screenmanager import ScreenManager, Screen
from kivy.uix.textinput import TextInput

from appcore.m3u import parse_m3u


class MenuScreen(Screen):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        root = BoxLayout(orientation="vertical", padding=dp(18), spacing=dp(12))
        root.add_widget(Label(
            text="[b]INDEPENDENT TV[/b]\\nPython edition",
            markup=True, font_size="26sp", size_hint_y=None, height=dp(90)
        ))

        for title, target in (
            ("TV AO VIVO", "live"),
            ("FILMES", "movies"),
            ("SÉRIES", "series"),
            ("EPG / GUIA", "epg"),
            ("ADICIONAR PLAYLIST", "playlist"),
            ("CONFIGURAÇÕES", "settings"),
        ):
            btn = Button(text=title, font_size="18sp", size_hint_y=None,
                         height=dp(58))
            btn.bind(on_release=lambda _b, t=target: self.open_section(t))
            root.add_widget(btn)

        self.add_widget(root)

    def open_section(self, target):
        self.manager.current = target


class ContentScreen(Screen):
    def __init__(self, title, **kwargs):
        super().__init__(**kwargs)
        layout = BoxLayout(orientation="vertical", padding=dp(14), spacing=dp(8))
        header = BoxLayout(size_hint_y=None, height=dp(52))
        back = Button(text="‹ VOLTAR", size_hint_x=None, width=dp(120))
        back.bind(on_release=lambda *_: setattr(self.manager, "current", "home"))
        header.add_widget(back)
        header.add_widget(Label(text=title, font_size="20sp"))
        layout.add_widget(header)
        self.body = BoxLayout(orientation="vertical")
        layout.add_widget(self.body)
        self.add_widget(layout)


    def on_pre_enter(self, *_args):
        app = App.get_running_app()
        if app and hasattr(app, "render_catalog"):
            app.render_catalog(self.name, self.body)



class PlaylistScreen(ContentScreen):
    def __init__(self, **kwargs):
        super().__init__("Adicionar conexão", **kwargs)
        from kivy.uix.spinner import Spinner

        self.body.orientation = "vertical"
        self.mode = Spinner(
            text="M3U",
            values=("M3U", "Xtream"),
            size_hint_y=None,
            height=dp(48)
        )
        self.body.add_widget(self.mode)

        self.url = TextInput(
            hint_text="URL M3U/M3U8",
            multiline=False,
            size_hint_y=None,
            height=dp(48)
        )
        self.body.add_widget(self.url)

        self.host = TextInput(
            hint_text="Servidor Xtream",
            multiline=False,
            size_hint_y=None,
            height=dp(48)
        )
        self.body.add_widget(self.host)

        self.username = TextInput(
            hint_text="Usuário Xtream",
            multiline=False,
            size_hint_y=None,
            height=dp(48)
        )
        self.body.add_widget(self.username)

        self.password = TextInput(
            hint_text="Senha Xtream",
            password=True,
            multiline=False,
            size_hint_y=None,
            height=dp(48)
        )
        self.body.add_widget(self.password)

        self.result = Label(
            text="Selecione M3U ou Xtream.",
            size_hint_y=None,
            height=dp(65)
        )
        self.body.add_widget(self.result)

        button = Button(
            text="CONECTAR E CARREGAR",
            size_hint_y=None,
            height=dp(55)
        )
        button.bind(on_release=self.load_playlist)
        self.body.add_widget(button)

    def on_pre_enter(self, *_args):
        # Preserva os campos do formulário ao abrir esta tela.
        pass

    def load_playlist(self, *_):
        from threading import Thread

        app = App.get_running_app()

        if self.mode.text == "M3U":
            url = self.url.text.strip()
            if not url:
                self.result.text = "Informe a URL M3U."
                return

            self.result.text = "Carregando playlist..."
            Thread(
                target=self._load_m3u,
                args=(app, url),
                daemon=True
            ).start()
            return

        host = self.host.text.strip()
        username = self.username.text.strip()
        password = self.password.text

        if not host or not username or not password:
            self.result.text = "Preencha servidor, usuário e senha."
            return

        self.result.text = "Autenticando Xtream..."
        Thread(
            target=self._load_xtream,
            args=(app, host, username, password),
            daemon=True
        ).start()

    def _load_m3u(self, app, url):
        import requests

        try:
            response = requests.get(url, timeout=30)
            response.raise_for_status()
            playlist = parse_m3u(response.text, source=url)

            if not playlist.items:
                raise ValueError("Nenhum canal reconhecido na playlist.")

            def success(_dt):
                try:
                    playlist_id = app.storage.add_playlist(
                        playlist.name, url, "m3u"
                    )
                    app.catalog = {
                        "live": playlist.items,
                        "movies": [],
                        "series": []
                    }
                    app.active_playlist_id = playlist_id
                    self.result.text = (
                        f"Playlist carregada: {len(playlist.items)} canais\n"
                        f"Categorias: {len(playlist.categories)}"
                    )
                except Exception as exc:
                    self.result.text = f"Erro ao salvar playlist: {exc}"

            Clock.schedule_once(success, 0)

        except Exception as exc:
            message = str(exc)
            Clock.schedule_once(
                lambda _dt: setattr(
                    self.result, "text", f"Erro M3U: {message}"
                ), 0
            )

    def _load_xtream(self, app, host, username, password):
        from appcore.xtream import XtreamClient
        from appcore.catalog import (
            normalize_live, normalize_movies, normalize_series
        )

        try:
            client = XtreamClient(host, username, password)
            client.authenticate()

            live_categories = client.live_categories()
            movie_categories = client.vod_categories()
            series_categories = client.series_categories()

            live = normalize_live(
                client.live_streams(), client, live_categories
            )
            movies = normalize_movies(
                client.vod_streams(), client, movie_categories
            )
            series = normalize_series(
                client.series(), series_categories
            )

            if not (live or movies or series):
                raise ValueError("O servidor não retornou conteúdo.")

            def success(_dt):
                try:
                    playlist_id = app.storage.add_playlist(
                        "Xtream - " + host,
                        host,
                        "xtream",
                        credentials={"username": username}
                    )
                    app.catalog = {
                        "live": live,
                        "movies": movies,
                        "series": series
                    }
                    app.active_playlist_id = playlist_id
                    app.xtream_client = client

                    self.result.text = (
                        "Conexão concluída!\n"
                        f"Canais: {len(live)} | Filmes: {len(movies)} "
                        f"| Séries: {len(series)}"
                    )
                except Exception as exc:
                    self.result.text = f"Erro ao salvar conexão: {exc}"

            Clock.schedule_once(success, 0)

        except Exception as exc:
            message = str(exc)
            Clock.schedule_once(
                lambda _dt: setattr(
                    self.result, "text", f"Erro Xtream: {message}"
                ), 0
            )


class PythonTVApp(App):
    playlist = None
    catalog = {
        "live": [],
        "movies": [],
        "series": []
    }
    active_playlist_id = None
    xtream_client = None

    def build(self):
        from pathlib import Path
        from appcore.storage import Storage

        self.title = "Independent TV"
        Window.clearcolor = (0.035, 0.047, 0.065, 1)

        db_path = Path(self.user_data_dir) / "independent_tv.db"
        self.storage = Storage(str(db_path))

        manager = ScreenManager()
        manager.add_widget(MenuScreen(name="home"))
        manager.add_widget(PlaylistScreen(name="playlist"))

        for key, title in (
            ("live", "TV ao vivo"),
            ("movies", "Filmes"),
            ("series", "Séries"),
            ("epg", "Guia EPG"),
            ("settings", "Configurações")
        ):
            manager.add_widget(ContentScreen(title, name=key))

        Window.bind(on_key_down=self.on_key_down)
        return manager

    def render_catalog(self, section, container):
        if container is None:
            return

        container.clear_widgets()

        if section not in self.catalog:
            container.add_widget(Label(
                text="Esta função ainda está em implementação."
            ))
            return

        items = self.catalog[section]

        if not items:
            container.add_widget(Label(
                text="Nenhum conteúdo carregado.\n"
                     "Adicione uma playlist ou conexão Xtream."
            ))
            return

        from kivy.uix.scrollview import ScrollView
        from kivy.uix.gridlayout import GridLayout

        scroll = ScrollView()
        listing = GridLayout(
            cols=1,
            spacing=dp(5),
            padding=dp(5),
            size_hint_y=None
        )
        listing.bind(minimum_height=listing.setter("height"))

        for item in items:
            button = Button(
                text=f"{item.name}\n{item.category}",
                size_hint_y=None,
                height=dp(64),
                halign="left"
            )
            button.bind(
                on_release=lambda _btn, selected=item:
                self.play_item(selected)
            )
            listing.add_widget(button)

        scroll.add_widget(listing)
        container.add_widget(scroll)

    def play_item(self, item):
        from urllib.parse import urlparse

        if not item.url:
            self.show_item_message(item)
            return

        scheme = urlparse(item.url).scheme.lower()
        if scheme not in ("http", "https", "rtsp"):
            self.show_item_message(item)
            return

        try:
            from jnius import autoclass

            PythonActivity = autoclass(
                "org.kivy.android.PythonActivity"
            )
            PlayerActivity = autoclass(
                "br.com.independenttv.player.PlayerActivity"
            )
            Intent = autoclass("android.content.Intent")

            activity = PythonActivity.mActivity
            intent = Intent(activity, PlayerActivity)
            intent.putExtra("url", item.url)
            intent.putExtra("title", item.name)
            activity.startActivity(intent)

        except Exception as exc:
            from kivy.uix.popup import Popup

            Popup(
                title="Player indisponível",
                content=Label(
                    text=(
                        "O player nativo só estará disponível "
                        "na versão Android compilada.\\n\\n"
                        + str(exc)
                    )
                ),
                size_hint=(0.85, 0.5)
            ).open()

    def show_item_message(self, item):
        from kivy.uix.popup import Popup

        Popup(
            title="Detalhes do conteúdo",
            content=Label(
                text=(
                    f"{item.name}\n\n"
                    f"Categoria: {item.category}\n"
                    f"Tipo: {item.media_type}\n\n"
                    "Reprodutor ainda não integrado."
                )
            ),
            size_hint=(0.85, 0.55)
        ).open()

    def on_key_down(self, _window, key, *_args):
        if key in (27, 8):
            if self.root and self.root.current != "home":
                self.root.current = "home"
                return True
        return False

    def on_stop(self):
        if hasattr(self, "storage"):
            self.storage.close()


if __name__ == "__main__":
    PythonTVApp().run()

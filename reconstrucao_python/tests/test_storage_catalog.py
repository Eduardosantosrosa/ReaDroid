import tempfile
import unittest
from pathlib import Path

from appcore.storage import Storage
from appcore.catalog import normalize_live, normalize_movies, normalize_series


class FakeClient:
    def live_url(self, stream_id, extension):
        return f"https://server/live/{stream_id}.{extension}"

    def movie_url(self, stream_id, extension):
        return f"https://server/movie/{stream_id}.{extension}"


class StorageTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.db = Storage(str(Path(self.tmp.name) / "test.db"))

    def tearDown(self):
        self.db.close()
        self.tmp.cleanup()

    def test_playlist_and_credentials(self):
        playlist_id = self.db.add_playlist(
            "Teste", "https://example.org/list.m3u", "m3u",
            {"username": "demo"}
        )
        rows = self.db.list_playlists()
        self.assertEqual(rows[0]["id"], playlist_id)
        self.assertIn("demo", rows[0]["credentials"])

    def test_favorites_and_history(self):
        playlist_id = self.db.add_playlist("Teste", "local", "m3u")
        self.db.set_favorite(
            playlist_id, "12", "Canal", "https://example.org/12", "live"
        )
        self.assertEqual(len(self.db.list_favorites(playlist_id)), 1)

        self.db.add_history(
            playlist_id, "12", "Canal", "https://example.org/12", "live"
        )
        self.assertEqual(self.db.list_history()[0]["name"], "Canal")

    def test_settings(self):
        self.db.set_setting("theme", "dark")
        self.assertEqual(self.db.get_setting("theme"), "dark")
        self.assertEqual(self.db.get_setting("missing", 42), 42)


class CatalogTests(unittest.TestCase):
    def test_live(self):
        items = normalize_live([{
            "stream_id": 7,
            "name": "Canal",
            "category_name": "Notícias",
            "container_extension": "ts",
        }], FakeClient())
        self.assertEqual(items[0].media_type, "live")
        self.assertIn("/7.ts", items[0].url)

    def test_movies(self):
        items = normalize_movies([{
            "stream_id": 8,
            "name": "Filme",
            "container_extension": "mkv",
        }], FakeClient())
        self.assertEqual(items[0].media_type, "movie")
        self.assertIn("/8.mkv", items[0].url)

    def test_series(self):
        items = normalize_series([{
            "series_id": 9,
            "name": "Série",
        }])
        self.assertEqual(items[0].media_type, "series")
        self.assertEqual(items[0].metadata["series_id"], 9)


if __name__ == "__main__":
    unittest.main()

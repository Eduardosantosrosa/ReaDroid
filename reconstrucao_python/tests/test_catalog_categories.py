import unittest
from appcore.catalog import (
    normalize_live, normalize_movies, normalize_series
)


class Client:
    def live_url(self, sid, ext):
        return f"https://host/live/{sid}.{ext}"

    def movie_url(self, sid, ext):
        return f"https://host/movie/{sid}.{ext}"


class CategoryTests(unittest.TestCase):
    def test_live_category(self):
        items = normalize_live(
            [{"stream_id": 1, "category_id": "4", "name": "Canal"}],
            Client(),
            [{"category_id": "4", "category_name": "Esportes"}]
        )
        self.assertEqual(items[0].category, "Esportes")

    def test_movie_category(self):
        items = normalize_movies(
            [{"stream_id": 2, "category_id": "5", "name": "Filme"}],
            Client(),
            [{"category_id": "5", "category_name": "Cinema"}]
        )
        self.assertEqual(items[0].category, "Cinema")

    def test_series_category(self):
        items = normalize_series(
            [{"series_id": 3, "category_id": "6", "name": "Série"}],
            [{"category_id": "6", "category_name": "Drama"}]
        )
        self.assertEqual(items[0].category, "Drama")


if __name__ == "__main__":
    unittest.main()

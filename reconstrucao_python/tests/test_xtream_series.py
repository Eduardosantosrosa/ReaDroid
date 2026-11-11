import unittest
from unittest.mock import patch

from appcore.xtream import XtreamClient
from appcore.catalog import normalize_series


class XtreamSeriesTests(unittest.TestCase):

    def setUp(self):
        self.client = XtreamClient(
            "https://servidor-teste.example",
            "usuario_teste",
            "senha_teste"
        )

    def test_series_info_uses_correct_action_and_id(self):
        response = {
            "seasons": [{"season_number": 1}],
            "episodes": {"1": [{"id": 101, "title": "Episódio 1"}]}
        }

        with patch.object(
            self.client, "_request", return_value=response
        ) as request:
            result = self.client.series_info(123)

        request.assert_called_once_with(
            "get_series_info", {"series_id": 123}
        )
        self.assertEqual(result, response)

    def test_episode_url_uses_series_path(self):
        url = self.client.episode_url(101, "mkv")

        self.assertEqual(
            url,
            "https://servidor-teste.example/series/"
            "usuario_teste/senha_teste/101.mkv"
        )

    def test_episode_url_defaults_to_mp4(self):
        url = self.client.episode_url(101)

        self.assertTrue(url.endswith("/101.mp4"))

    def test_normalize_series_preserves_series_id(self):
        items = normalize_series([
            {
                "series_id": 123,
                "name": "Série de teste",
                "category_id": "6"
            }
        ])

        self.assertEqual(len(items), 1)
        self.assertEqual(items[0].metadata["series_id"], 123)
        self.assertEqual(items[0].media_type, "series")


if __name__ == "__main__":
    unittest.main()


class SeriesDataTests(unittest.TestCase):

    def setUp(self):
        from appcore.models import MediaItem

        class Client:
            def episode_url(self, episode_id, extension="mp4"):
                return (
                    f"https://host/series/user/pass/"
                    f"{episode_id}.{extension}"
                )

        self.client = Client()
        self.series = MediaItem(
            name="Série teste",
            url="",
            category="Drama",
            media_type="series",
            metadata={"series_id": 123}
        )

    def test_groups_dictionary_seasons_in_numeric_order(self):
        from appcore.series import group_episodes_by_season

        info = {
            "seasons": [
                {"season_number": 10, "name": "Décima"},
                {"season_number": 2, "name": "Segunda"},
                {"season_number": 1, "name": "Primeira"}
            ],
            "episodes": {
                "10": [{"id": 10}],
                "2": [{"id": 2}],
                "1": [{"id": 1}]
            }
        }

        result = group_episodes_by_season(info)

        self.assertEqual(
            [season[0] for season in result],
            ["1", "2", "10"]
        )
        self.assertEqual(result[0][1], "Primeira")

    def test_groups_list_episodes_by_season(self):
        from appcore.series import group_episodes_by_season

        info = {
            "episodes": [
                {"id": 1, "season": 1},
                {"id": 2, "season": 2},
                {"id": 3, "season": 1}
            ]
        }

        result = group_episodes_by_season(info)

        self.assertEqual(
            [season[0] for season in result],
            ["1", "2"]
        )
        self.assertEqual(len(result[0][2]), 2)

    def test_builds_episode_items_and_urls(self):
        from appcore.series import build_episode_items

        episodes = [{
            "id": 101,
            "episode_num": 3,
            "title": "O retorno",
            "container_extension": "mkv",
            "info": {"movie_image": "https://host/image.jpg"}
        }]

        result = build_episode_items(
            self.series, episodes, self.client
        )

        self.assertEqual(len(result), 1)
        self.assertEqual(result[0].name, "E3 - O retorno")
        self.assertTrue(result[0].url.endswith("/101.mkv"))
        self.assertEqual(result[0].category, "Drama")
        self.assertEqual(result[0].media_type, "episode")
        self.assertEqual(
            result[0].logo, "https://host/image.jpg"
        )

    def test_skips_episodes_without_id(self):
        from appcore.series import build_episode_items

        result = build_episode_items(
            self.series,
            [{"title": "Sem ID"}, None, {"id": 102, "name": "Válido"}],
            self.client
        )

        self.assertEqual(len(result), 1)
        self.assertEqual(result[0].name, "Válido")

    def test_rejects_invalid_series_response(self):
        from appcore.series import group_episodes_by_season

        with self.assertRaises(ValueError):
            group_episodes_by_season(None)

    def test_rejects_invalid_episodes_format(self):
        from appcore.series import group_episodes_by_season

        with self.assertRaises(ValueError):
            group_episodes_by_season({"episodes": "inválido"})

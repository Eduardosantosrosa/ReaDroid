import json
import unittest
from urllib.error import URLError
from urllib.parse import parse_qs, urlparse
from unittest.mock import patch

from appcore.xtream import XtreamClient, XtreamError


class FakeResponse:
    def __init__(self, payload):
        self.payload = payload

    def __enter__(self):
        return self

    def __exit__(self, *args):
        return False

    def read(self):
        return json.dumps(self.payload).encode("utf-8")


class XtreamClientTests(unittest.TestCase):

    def setUp(self):
        self.client = XtreamClient(
            "https://servidor-teste.example",
            "usuario_teste",
            "senha_teste",
            timeout=7
        )

    def test_normalizes_host(self):
        client = XtreamClient("servidor-teste.example/", "u", "p")
        self.assertEqual(
            client.hosts,
            ["http://servidor-teste.example"]
        )

    def test_request_sends_credentials_action_and_extra(self):
        payload = [{"stream_id": 10}]

        with patch(
            "appcore.xtream.urlopen",
            return_value=FakeResponse(payload)
        ) as mocked:
            result = self.client._request(
                "get_live_streams",
                {"category_id": "5"}
            )

        self.assertEqual(result, payload)
        request = mocked.call_args.args[0]
        query = parse_qs(urlparse(request.full_url).query)

        self.assertEqual(query["username"], ["usuario_teste"])
        self.assertEqual(query["password"], ["senha_teste"])
        self.assertEqual(query["action"], ["get_live_streams"])
        self.assertEqual(query["category_id"], ["5"])
        self.assertEqual(mocked.call_args.kwargs["timeout"], 7)

    def test_authentication_accepts_valid_account(self):
        payload = {"user_info": {"auth": "1"}}

        with patch.object(
            self.client, "_request", return_value=payload
        ):
            self.assertEqual(self.client.authenticate(), payload)

    def test_authentication_rejects_invalid_account(self):
        with patch.object(
            self.client, "_request",
            return_value={"user_info": {"auth": 0}}
        ):
            with self.assertRaises(XtreamError):
                self.client.authenticate()

    def test_authentication_rejects_non_dictionary(self):
        with patch.object(
            self.client, "_request", return_value=[]
        ):
            with self.assertRaises(XtreamError):
                self.client.authenticate()

    def test_failover_uses_mirror_after_network_error(self):
        client = XtreamClient(
            "https://principal.example",
            "u",
            "p",
            mirrors=["https://espelho.example"]
        )

        with patch(
            "appcore.xtream.urlopen",
            side_effect=[
                URLError("servidor indisponível"),
                FakeResponse({"user_info": {"auth": 1}})
            ]
        ) as mocked:
            result = client.authenticate()

        self.assertEqual(result["user_info"]["auth"], 1)
        self.assertEqual(client.active_host, 1)
        self.assertEqual(mocked.call_count, 2)
        self.assertIn(
            "principal.example",
            mocked.call_args_list[0].args[0].full_url
        )
        self.assertIn(
            "espelho.example",
            mocked.call_args_list[1].args[0].full_url
        )

    def test_all_servers_failing_raises_xtream_error(self):
        client = XtreamClient(
            "https://principal.example", "u", "p",
            mirrors=["https://espelho.example"]
        )

        with patch(
            "appcore.xtream.urlopen",
            side_effect=URLError("offline")
        ):
            with self.assertRaises(XtreamError):
                client.authenticate()

    def test_invalid_json_raises_xtream_error(self):
        class InvalidResponse(FakeResponse):
            def read(self):
                return b"{json-invalido"

        with patch(
            "appcore.xtream.urlopen",
            return_value=InvalidResponse(None)
        ):
            with self.assertRaises(XtreamError):
                self.client._request()

    def test_category_and_catalog_actions(self):
        methods = [
            ("live_categories", "get_live_categories", False),
            ("live_streams", "get_live_streams", True),
            ("vod_categories", "get_vod_categories", False),
            ("vod_streams", "get_vod_streams", True),
            ("series_categories", "get_series_categories", False),
            ("series", "get_series", True),
        ]

        for method_name, action, sends_extra in methods:
            with self.subTest(method=method_name):
                with patch.object(
                    self.client, "_request", return_value=[]
                ) as request:
                    getattr(self.client, method_name)()

                if sends_extra:
                    request.assert_called_once_with(action, None)
                else:
                    request.assert_called_once_with(action)

    def test_catalog_methods_forward_category_id(self):
        for method_name, action in [
            ("live_streams", "get_live_streams"),
            ("vod_streams", "get_vod_streams"),
            ("series", "get_series"),
        ]:
            with self.subTest(method=method_name):
                with patch.object(
                    self.client, "_request", return_value=[]
                ) as request:
                    getattr(self.client, method_name)("12")

                request.assert_called_once_with(
                    action, {"category_id": "12"}
                )


if __name__ == "__main__":
    unittest.main()

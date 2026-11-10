import unittest
from appcore.m3u import parse_m3u
from appcore.xmltv import parse_xmltv


class M3UTests(unittest.TestCase):
    def test_parse_channels_and_groups(self):
        source = '''#EXTM3U
#EXTINF:-1 tvg-id="news.br" tvg-logo="https://example.org/logo.png" group-title="Notícias",Canal Teste
https://example.org/live/test.m3u8
#EXTINF:-1 group-title="Esportes",Canal Esporte
https://example.org/sport.ts
'''
        playlist = parse_m3u(source)
        self.assertEqual(len(playlist.items), 2)
        self.assertEqual(playlist.items[0].name, "Canal Teste")
        self.assertEqual(playlist.items[0].epg_id, "news.br")
        self.assertEqual(playlist.items[1].category, "Esportes")
        self.assertEqual(len(playlist.categories), 2)


class XMLTVTests(unittest.TestCase):
    def test_parse_guide(self):
        source = '''<?xml version="1.0"?>
        <tv>
          <channel id="news.br"><display-name>Notícias</display-name></channel>
          <programme channel="news.br" start="20261110120000 +0000"
                     stop="20261110130000 +0000">
            <title>Jornal</title><desc>Resumo</desc>
          </programme>
        </tv>'''
        result = parse_xmltv(source)
        self.assertIn("news.br", result["channels"])
        self.assertEqual(result["programmes"][0]["title"], "Jornal")
        self.assertIsNotNone(result["programmes"][0]["start"])


if __name__ == "__main__":
    unittest.main()

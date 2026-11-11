"""Cliente Xtream Codes com timeout e failover entre domínios."""
import json
from urllib.error import HTTPError, URLError
from urllib.parse import urlparse, urlunparse, urlencode
from urllib.request import Request, urlopen


class XtreamError(RuntimeError):
    pass


class XtreamClient:
    def __init__(self, host, username, password, mirrors=None, timeout=15):
        self.hosts = [self._normalise(host)]
        self.hosts.extend(self._normalise(h) for h in (mirrors or []))
        self.username = username
        self.password = password
        self.timeout = timeout
        self.active_host = 0

    @staticmethod
    def _normalise(host):
        host = host.strip().rstrip("/")
        if not host.startswith(("http://", "https://")):
            host = "http://" + host
        parsed = urlparse(host)
        if not parsed.netloc:
            raise ValueError("Endereço Xtream inválido")
        return urlunparse((parsed.scheme, parsed.netloc, "", "", "", ""))

    def _request(self, action=None, extra=None):
        params = {"username": self.username, "password": self.password}
        if action:
            params["action"] = action
        if extra:
            params.update(extra)

        last_error = None
        for offset in range(len(self.hosts)):
            index = (self.active_host + offset) % len(self.hosts)
            url = self.hosts[index] + "/player_api.php"
            try:
                query = urlencode(params)
                request = Request(
                    url + "?" + query,
                    headers={"User-Agent": "IndependentPythonTV/0.1"}
                )
                with urlopen(request, timeout=self.timeout) as response:
                    payload = response.read().decode("utf-8-sig")
                data = json.loads(payload)
                self.active_host = index
                return data
            except (HTTPError, URLError, TimeoutError, OSError, ValueError) as exc:
                last_error = exc

        raise XtreamError(f"Todos os servidores Xtream falharam: {last_error}")

    def authenticate(self):
        data = self._request()
        if not isinstance(data, dict):
            raise XtreamError("Resposta de autenticação inválida")
        user_info = data.get("user_info", {})
        if user_info.get("auth") not in (1, "1", True):
            raise XtreamError("Usuário ou senha recusados pelo servidor")
        return data

    def live_categories(self):
        return self._request("get_live_categories")

    def live_streams(self, category_id=None):
        extra = {"category_id": category_id} if category_id else None
        return self._request("get_live_streams", extra)

    def vod_categories(self):
        return self._request("get_vod_categories")

    def vod_streams(self, category_id=None):
        extra = {"category_id": category_id} if category_id else None
        return self._request("get_vod_streams", extra)

    def series_categories(self):
        return self._request("get_series_categories")

    def series(self, category_id=None):
        extra = {"category_id": category_id} if category_id else None
        return self._request("get_series", extra)

    def series_info(self, series_id):
        return self._request("get_series_info", {"series_id": series_id})

    def live_url(self, stream_id, extension="ts"):
        return (
            f"{self.hosts[self.active_host]}/live/"
            f"{self.username}/{self.password}/{stream_id}.{extension}"
        )

    def movie_url(self, stream_id, extension="mp4"):
        return (
            f"{self.hosts[self.active_host]}/movie/"
            f"{self.username}/{self.password}/{stream_id}.{extension}"
        )

    def episode_url(self, episode_id, extension="mp4"):
        return (
            f"{self.hosts[self.active_host]}/series/"
            f"{self.username}/{self.password}/{episode_id}.{extension}"
        )

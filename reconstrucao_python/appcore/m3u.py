"""Parser M3U/M3U8 tolerante a variações comuns de provedores."""
import re
from urllib.parse import urlparse
from .models import MediaItem, Playlist


_ATTR = re.compile(r'([\w-]+)\s*=\s*"([^"]*)"')


def _attributes(line):
    return {k.lower(): v for k, v in _ATTR.findall(line)}


def parse_m3u(text: str, name="Playlist", source="") -> Playlist:
    items = []
    pending = None

    for raw in text.replace("\ufeff", "").splitlines():
        line = raw.strip()
        if not line:
            continue

        if line.startswith("#EXTINF:"):
            head, _, title = line.partition(",")
            attrs = _attributes(head)
            duration_match = re.match(r"#EXTINF:\s*(-?\d+)", head)
            duration = int(duration_match.group(1)) if duration_match else 0
            title = title.strip() or attrs.get("tvg-name", "Sem nome")

            pending = {
                "name": title,
                "logo": attrs.get("tvg-logo") or None,
                "group": attrs.get("group-title", "Geral"),
                "epg_id": attrs.get("tvg-id", ""),
                "duration": duration,
                "metadata": attrs,
            }
        elif line.startswith("#EXTGRP:"):
            if pending is not None:
                pending["group"] = line.partition(":")[2].strip()
        elif line.startswith("#"):
            continue
        elif pending is not None:
            parsed = urlparse(line)
            if parsed.scheme in ("http", "https", "rtsp", "rtmp", "udp"):
                items.append(MediaItem(
                    name=pending["name"],
                    url=line,
                    category=pending["group"] or "Geral",
                    logo=pending["logo"],
                    group=pending["group"] or "Geral",
                    media_type="live",
                    epg_id=pending["epg_id"],
                    duration=pending["duration"],
                    metadata=pending["metadata"],
                ))
            pending = None

    categories = sorted({item.category for item in items}, key=str.casefold)
    return Playlist(name, source, "m3u", items, categories)


def parse_m3u_file(path: str) -> Playlist:
    with open(path, "r", encoding="utf-8-sig", errors="replace") as f:
        return parse_m3u(f.read(), name=path.rsplit("/", 1)[-1], source=path)

from dataclasses import dataclass, field
from typing import Optional


@dataclass
class MediaItem:
    name: str
    url: str
    category: str = "Geral"
    logo: Optional[str] = None
    group: str = ""
    media_type: str = "live"
    epg_id: str = ""
    duration: int = 0
    metadata: dict = field(default_factory=dict)


@dataclass
class Playlist:
    name: str
    source: str
    playlist_type: str
    items: list[MediaItem] = field(default_factory=list)
    categories: list[str] = field(default_factory=list)

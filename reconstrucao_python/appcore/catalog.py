"""Normalização dos catálogos M3U e Xtream."""
from .models import MediaItem


def _text(value, default=""):
    return str(value) if value is not None else default


def _category_map(rows):
    result = {}
    for row in rows or []:
        category_id = row.get("category_id")
        name = row.get("category_name") or row.get("name")
        if category_id is not None and name:
            result[str(category_id)] = str(name)
    return result



def _category_name(categories, category_id):
    """Resolve o nome da categoria recebendo lista ou dicionário."""
    key = str(category_id)

    if isinstance(categories, dict):
        value = categories.get(key, categories.get(category_id, "Geral"))
        if isinstance(value, dict):
            return value.get("category_name") or value.get("name") or "Geral"
        return value or "Geral"

    if isinstance(categories, (list, tuple)):
        for row in categories:
            if not isinstance(row, dict):
                continue
            row_id = row.get("category_id", row.get("id"))
            if str(row_id) == key:
                return row.get("category_name") or row.get("name") or "Geral"

    return "Geral"


def normalize_live(rows, client, categories=None):
    categories = categories or {}
    result = []
    for row in rows or []:
        stream_id = row.get("stream_id")
        if stream_id is None:
            continue
        category_id = row.get("category_id")
        category = _category_name(categories, category_id)
        extension = row.get("container_extension") or "ts"
        result.append(MediaItem(
            name=_text(row.get("name"), "Canal sem nome"),
            url=client.live_url(stream_id, extension),
            category=category,
            logo=row.get("stream_icon") or None,
            group=category,
            media_type="live",
            epg_id=_text(row.get("epg_channel_id")),
            metadata=dict(row),
        ))
    return result


def normalize_movies(rows, client, categories=None):
    categories = categories or {}
    result = []
    for row in rows or []:
        stream_id = row.get("stream_id")
        if stream_id is None:
            continue
        category_id = row.get("category_id")
        category = _category_name(categories, category_id)
        extension = row.get("container_extension") or "mp4"
        result.append(MediaItem(
            name=_text(row.get("name"), "Filme sem nome"),
            url=client.movie_url(stream_id, extension),
            category=category,
            logo=row.get("stream_icon") or None,
            group=category,
            media_type="movie",
            metadata=dict(row),
        ))
    return result


def normalize_series(rows, categories=None):
    categories = categories or {}
    result = []
    for row in rows or []:
        series_id = row.get("series_id")
        if series_id is None:
            continue
        category_id = row.get("category_id")
        category = _category_name(categories, category_id)
        result.append(MediaItem(
            name=_text(row.get("name"), "Série sem nome"),
            url="",
            category=category,
            logo=row.get("cover") or None,
            group=category,
            media_type="series",
            metadata=dict(row),
        ))
    return result

"""Tratamento dos dados de temporadas e episódios Xtream."""

from .models import MediaItem


def group_episodes_by_season(info):
    if not isinstance(info, dict):
        raise ValueError("Resposta de detalhes da série inválida.")

    episodes = info.get("episodes") or {}

    if not isinstance(episodes, (dict, list)):
        raise ValueError("Formato de episódios não reconhecido.")

    if isinstance(episodes, list):
        grouped = {}
        for episode in episodes:
            if not isinstance(episode, dict):
                continue
            season = str(episode.get("season", 1))
            grouped.setdefault(season, []).append(episode)
        episodes = grouped

    season_names = {}
    for season in info.get("seasons") or []:
        if not isinstance(season, dict):
            continue
        number = season.get("season_number", season.get("season"))
        if number is not None:
            season_names[str(number)] = (
                season.get("name") or f"Temporada {number}"
            )

    def sort_key(value):
        try:
            return (0, int(value))
        except (TypeError, ValueError):
            return (1, str(value))

    result = []
    for number in sorted(episodes, key=sort_key):
        rows = episodes[number]
        if not isinstance(rows, list):
            continue
        result.append((
            str(number),
            season_names.get(str(number), f"Temporada {number}"),
            rows
        ))

    return result


def build_episode_items(series, episodes, client):
    result = []

    for episode in episodes:
        if not isinstance(episode, dict):
            continue

        episode_id = episode.get("id")
        if episode_id is None:
            continue

        extension = episode.get("container_extension") or "mp4"
        title = (
            episode.get("title")
            or episode.get("name")
            or f"Episódio {episode.get('episode_num', '')}"
        )

        number = episode.get("episode_num")
        label = f"E{number} - {title}" if number else str(title)

        episode_info = episode.get("info")
        logo = (
            episode_info.get("movie_image")
            if isinstance(episode_info, dict) else None
        )

        result.append(MediaItem(
            name=str(label),
            url=client.episode_url(episode_id, extension),
            category=series.category,
            logo=logo,
            group=series.name,
            media_type="episode",
            metadata=dict(episode)
        ))

    return result

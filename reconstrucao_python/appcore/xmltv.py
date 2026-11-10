"""Leitura XMLTV para programação EPG."""
from datetime import datetime, timezone
import xml.etree.ElementTree as ET


def _xmltv_time(value):
    value = (value or "").strip()
    if not value:
        return None
    try:
        return datetime.strptime(value[:14], "%Y%m%d%H%M%S").replace(
            tzinfo=timezone.utc
        )
    except ValueError:
        return None


def parse_xmltv(xml_text):
    root = ET.fromstring(xml_text)
    channels = {}
    programmes = []

    for node in root.findall("channel"):
        channel_id = node.get("id", "")
        display = node.findtext("display-name", default=channel_id)
        icon = node.find("icon")
        channels[channel_id] = {
            "id": channel_id,
            "name": display,
            "icon": icon.get("src") if icon is not None else None,
        }

    for node in root.findall("programme"):
        title = node.findtext("title", default="Sem título")
        desc = node.findtext("desc", default="")
        programmes.append({
            "channel": node.get("channel", ""),
            "title": title,
            "description": desc,
            "start": _xmltv_time(node.get("start")),
            "stop": _xmltv_time(node.get("stop")),
        })

    programmes.sort(key=lambda p: p["start"] or datetime.min.replace(
        tzinfo=timezone.utc
    ))
    return {"channels": channels, "programmes": programmes}

"""Persistência local de playlists, favoritos e histórico."""
import json
import sqlite3
from pathlib import Path


class Storage:
    def __init__(self, path="independent_tv.db"):
        self.path = str(Path(path))
        self.db = sqlite3.connect(self.path)
        self.db.row_factory = sqlite3.Row
        self._create_tables()

    def _create_tables(self):
        with self.db:
            self.db.executescript("""
                CREATE TABLE IF NOT EXISTS playlists (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    source TEXT NOT NULL,
                    kind TEXT NOT NULL,
                    credentials TEXT DEFAULT '{}',
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                );

                CREATE TABLE IF NOT EXISTS favorites (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    playlist_id INTEGER,
                    media_id TEXT NOT NULL,
                    name TEXT NOT NULL,
                    url TEXT NOT NULL,
                    media_type TEXT NOT NULL,
                    UNIQUE(playlist_id, media_id, media_type)
                );

                CREATE TABLE IF NOT EXISTS history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    playlist_id INTEGER,
                    media_id TEXT NOT NULL,
                    name TEXT NOT NULL,
                    url TEXT NOT NULL,
                    media_type TEXT NOT NULL,
                    watched_at TEXT DEFAULT CURRENT_TIMESTAMP
                );

                CREATE TABLE IF NOT EXISTS settings (
                    key TEXT PRIMARY KEY,
                    value TEXT NOT NULL
                );
            """)

    def add_playlist(self, name, source, kind, credentials=None):
        with self.db:
            cur = self.db.execute(
                """INSERT INTO playlists(name,source,kind,credentials)
                   VALUES(?,?,?,?)""",
                (name, source, kind, json.dumps(credentials or {}))
            )
        return cur.lastrowid

    def list_playlists(self):
        return [dict(r) for r in self.db.execute(
            "SELECT * FROM playlists ORDER BY id DESC"
        )]

    def remove_playlist(self, playlist_id):
        with self.db:
            self.db.execute("DELETE FROM favorites WHERE playlist_id=?",
                            (playlist_id,))
            self.db.execute("DELETE FROM history WHERE playlist_id=?",
                            (playlist_id,))
            self.db.execute("DELETE FROM playlists WHERE id=?",
                            (playlist_id,))

    def set_favorite(self, playlist_id, media_id, name, url, media_type):
        with self.db:
            self.db.execute(
                """INSERT OR IGNORE INTO favorites
                   (playlist_id,media_id,name,url,media_type)
                   VALUES(?,?,?,?,?)""",
                (playlist_id, str(media_id), name, url, media_type)
            )

    def remove_favorite(self, playlist_id, media_id, media_type):
        with self.db:
            self.db.execute(
                """DELETE FROM favorites
                   WHERE playlist_id=? AND media_id=? AND media_type=?""",
                (playlist_id, str(media_id), media_type)
            )

    def list_favorites(self, playlist_id=None):
        if playlist_id is None:
            rows = self.db.execute("SELECT * FROM favorites ORDER BY name")
        else:
            rows = self.db.execute(
                "SELECT * FROM favorites WHERE playlist_id=? ORDER BY name",
                (playlist_id,)
            )
        return [dict(r) for r in rows]

    def add_history(self, playlist_id, media_id, name, url, media_type):
        with self.db:
            self.db.execute(
                """INSERT INTO history
                   (playlist_id,media_id,name,url,media_type)
                   VALUES(?,?,?,?,?)""",
                (playlist_id, str(media_id), name, url, media_type)
            )

    def list_history(self, limit=100):
        return [dict(r) for r in self.db.execute(
            "SELECT * FROM history ORDER BY watched_at DESC LIMIT ?",
            (limit,)
        )]

    def set_setting(self, key, value):
        with self.db:
            self.db.execute(
                """INSERT INTO settings(key,value) VALUES(?,?)
                   ON CONFLICT(key) DO UPDATE SET value=excluded.value""",
                (key, json.dumps(value))
            )

    def get_setting(self, key, default=None):
        row = self.db.execute(
            "SELECT value FROM settings WHERE key=?", (key,)
        ).fetchone()
        return json.loads(row["value"]) if row else default

    def close(self):
        self.db.close()

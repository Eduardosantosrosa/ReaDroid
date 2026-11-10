import json
import tempfile
import unittest
import zipfile
from unittest.mock import patch

from readroid.reconstructor import reconstruct_apk
from pathlib import Path

from readroid.functional_spec import (
    build_functional_spec,
    write_functional_spec,
)


class FunctionalSpecTests(unittest.TestCase):

    def sample_data(self):
        return (
            {
                "sha256": "abc123",
                "file_size_bytes": 2048,
                "dex_files": ["classes.dex"],
                "resource_file_count": 120,
                "asset_file_count": 8,
                "zip_entry_count": 140,
            },
            {
                "decoded": True,
                "package": "private.original.package",
                "components": [
                    {
                        "type": "activity",
                        "name": "private.original.LoginActivity",
                        "exported": True,
                    },
                    {
                        "type": "service",
                        "name": "private.original.PrivateService",
                        "exported": False,
                    },
                ],
                "permissions": [
                    {"name": "android.permission.INTERNET"},
                    {"name": "android.permission.ACCESS_NETWORK_STATE"},
                    {"name": "private.permission.SECRET"},
                ],
                "application": {
                    "debuggable": False,
                    "allow_backup": True,
                },
            },
            {
                "status": "partial",
                "artifact_type": "layout_evidence_catalog",
                "source_layouts": [
                    {"name": "activity_login", "view_count": 5},
                    {"name": "activity_player", "view_count": 8},
                    {"name": "item_canal", "view_count": 3},
                ],
            },
        )

    def test_layout_names_only_generate_low_confidence_clues(self):
        info, manifest, coverage = self.sample_data()
        spec = build_functional_spec(info, manifest, coverage)

        categories = {
            item["category"]: item["confidence"]
            for item in spec["candidate_features"]
        }

        self.assertEqual(categories["authentication"], "low")
        self.assertEqual(categories["media_playback"], "low")
        self.assertIn("content_listing", categories)
        self.assertFalse(spec["provenance"]["runtime_behavior_verified"])
        self.assertIn(
            "Navegacao, rotas e transicoes entre telas",
            spec["not_established"],
        )

    def test_private_identifiers_and_values_are_not_exported(self):
        info, manifest, coverage = self.sample_data()
        spec = build_functional_spec(info, manifest, coverage)

        serialized = json.dumps(spec, ensure_ascii=False)

        forbidden = [
            "private.original.package",
            "private.original.LoginActivity",
            "private.original.PrivateService",
            "private.permission.SECRET",
            "activity_login",
            "activity_player",
            "item_canal",
        ]

        for value in forbidden:
            self.assertNotIn(value, serialized)

        self.assertIn("android.permission.INTERNET", serialized)

    def test_output_files_are_created_with_sanitized_content(self):
        info, manifest, coverage = self.sample_data()

        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            result = write_functional_spec(
                root, info, manifest, coverage
            )

            self.assertEqual(result["status"], "created")
            self.assertTrue((root / "functional_spec.json").is_file())
            self.assertTrue((root / "FUNCTIONAL_SPEC.md").is_file())

            data = json.loads(
                (root / "functional_spec.json").read_text(
                    encoding="utf-8"
                )
            )
            markdown = (
                root / "FUNCTIONAL_SPEC.md"
            ).read_text(encoding="utf-8")

            self.assertEqual(
                data["implementation_target"]["ui_framework"],
                "Kivy",
            )
            self.assertIn("Diretrizes para o Codex", markdown)
            self.assertNotIn("private.original", markdown)
            self.assertNotIn("activity_login", markdown)


    def test_reconstructor_writes_spec_with_layout_evidence(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            apk = root / "synthetic.apk"

            with zipfile.ZipFile(apk, "w") as archive:
                archive.writestr(
                    "AndroidManifest.xml",
                    """<manifest xmlns:android="http://schemas.android.com/apk/res/android"
                    package="private.integration.package">
                    <uses-permission android:name="android.permission.INTERNET"/>
                    <application>
                        <activity android:name=".Main" android:exported="true"/>
                    </application>
                    </manifest>""",
                )
                archive.writestr("classes.dex", b"dex\\n035\\x00")

            def fake_apktool(_apk, output):
                layout = output / "res/layout/activity_login.xml"
                layout.parent.mkdir(parents=True)
                layout.write_text(
                    """<LinearLayout
                    xmlns:android="http://schemas.android.com/apk/res/android"
                    android:layout_width="match_parent"
                    android:layout_height="match_parent">
                    <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"/>
                    </LinearLayout>""",
                    encoding="utf-8",
                )
                return {"ok": True, "returncode": 0}

            with patch(
                "readroid.reconstructor.decompile_with_jadx",
                return_value={"ok": False, "status": "unavailable"},
            ), patch(
                "readroid.reconstructor.extract_smali_with_apktool",
                side_effect=fake_apktool,
            ):
                result = reconstruct_apk(
                    apk, root / "out", progress=lambda _: None
                )

            output = Path(result["output_root"])
            self.assertTrue((output / "functional_spec.json").is_file())
            self.assertTrue((output / "FUNCTIONAL_SPEC.md").is_file())

            spec = json.loads(
                (output / "functional_spec.json").read_text(
                    encoding="utf-8"
                )
            )
            self.assertEqual(
                spec["confirmed_static_evidence"]["layout_catalog"]["layout_count"],
                1,
            )
            self.assertNotIn(
                "private.integration.package",
                json.dumps(spec),
            )

    def test_reconstructor_writes_spec_without_layout_evidence(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            apk = root / "synthetic.apk"

            with zipfile.ZipFile(apk, "w") as archive:
                archive.writestr(
                    "AndroidManifest.xml",
                    '<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="org.example.test"><application/></manifest>',
                )
                archive.writestr("classes.dex", b"dex\\n035\\x00")

            with patch(
                "readroid.reconstructor.decompile_with_jadx",
                return_value={"ok": False},
            ), patch(
                "readroid.reconstructor.extract_smali_with_apktool",
                return_value={"ok": False},
            ):
                result = reconstruct_apk(
                    apk, root / "out", progress=lambda _: None
                )

            output = Path(result["output_root"])
            self.assertTrue((output / "functional_spec.json").is_file())
            self.assertTrue((output / "FUNCTIONAL_SPEC.md").is_file())

            spec = json.loads(
                (output / "functional_spec.json").read_text(
                    encoding="utf-8"
                )
            )
            self.assertEqual(
                spec["confirmed_static_evidence"]["layout_catalog"]["layout_count"],
                0,
            )
            self.assertEqual(spec["candidate_features"], [])



    def test_invalid_metadata_values_are_handled_safely(self):
        info, manifest, coverage = self.sample_data()

        coverage["artifact_type"] = ["valor", "inesperado"]
        info["sha256"] = "abc123"

        spec = build_functional_spec(info, manifest, coverage)

        self.assertEqual(
            spec["confirmed_static_evidence"]["layout_catalog"]["artifact_type"],
            "unknown",
        )
        self.assertEqual(spec["provenance"]["source_sha256"], "")

        coverage["artifact_type"] = {"valor": "inesperado"}
        spec = build_functional_spec(info, manifest, coverage)

        self.assertEqual(
            spec["confirmed_static_evidence"]["layout_catalog"]["artifact_type"],
            "unknown",
        )

    def test_invalid_layout_records_are_ignored(self):
        info, manifest, coverage = self.sample_data()
        coverage["source_layouts"] = [
            {"name": "activity_login", "view_count": 5},
            None,
            "registro invalido",
            123,
        ]

        spec = build_functional_spec(info, manifest, coverage)
        catalog = spec["confirmed_static_evidence"]["layout_catalog"]

        self.assertEqual(catalog["layout_count"], 1)
        self.assertEqual(catalog["view_count_total"], 5)


if __name__ == "__main__":
    unittest.main()

import json
import tempfile
import unittest
import zipfile
from pathlib import Path
from unittest.mock import patch

from readroid.reconstructor import reconstruct_apk


class ReconstructionTests(unittest.TestCase):
    def make_apk(self, root):
        apk = root / "synthetic.apk"
        with zipfile.ZipFile(apk, "w") as archive:
            archive.writestr(
                "AndroidManifest.xml",
                '''<manifest xmlns:android="http://schemas.android.com/apk/res/android"
                package="org.example.synthetic">
                <application>
                    <activity android:name=".Main" android:exported="true"/>
                </application></manifest>''',
            )
            archive.writestr("classes.dex", b"dex\n035\x00")
        return apk

    def test_activity_without_layout_proof_is_not_reported_as_success(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            apk = self.make_apk(root)

            with patch(
                "readroid.reconstructor.decompile_with_jadx",
                return_value={"ok": False, "stderr": "erro privado"},
            ), patch(
                "readroid.reconstructor.extract_smali_with_apktool",
                return_value={"ok": False, "stderr": "erro privado"},
            ):
                result = reconstruct_apk(
                    apk, root / "out", progress=lambda _: None
                )

            self.assertEqual(result["status"], "reconstruction_unavailable")
            self.assertIsNone(result["generated_project"])
            self.assertEqual(
                result["coverage"]["status"], "insufficient_evidence"
            )

            output = Path(result["output_root"])
            self.assertFalse((output / "analysis").exists())
            self.assertFalse((output / "work").exists())

            report_text = (
                output / "reconstruction_report.json"
            ).read_text(encoding="utf-8")
            self.assertNotIn("erro privado", report_text)

    def test_layout_catalog_does_not_invent_launcher_activity(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            apk = self.make_apk(root)
            temporary_paths = []

            def extract(_apk, output):
                temporary_paths.append(output)
                layout = output / "res/layout/main.xml"
                layout.parent.mkdir(parents=True)
                layout.write_text(
                    '''<LinearLayout
                    xmlns:android="http://schemas.android.com/apk/res/android"
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:orientation="vertical">
                    <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Evidence" />
                    </LinearLayout>''',
                    encoding="utf-8",
                )
                return {"ok": True}

            with patch(
                "readroid.reconstructor.decompile_with_jadx",
                return_value={"ok": False},
            ), patch(
                "readroid.reconstructor.extract_smali_with_apktool",
                side_effect=extract,
            ):
                result = reconstruct_apk(
                    apk, root / "out", progress=lambda _: None
                )

            self.assertEqual(result["status"], "partial")
            generated = result["generated_project"]
            self.assertEqual(generated["screens_identified"], 0)
            self.assertFalse(generated["launcher_activity_generated"])
            self.assertEqual(generated["java_files_generated"], 0)

            project = Path(generated["project_root"])
            manifest = (
                project / "app/src/main/AndroidManifest.xml"
            ).read_text(encoding="utf-8")

            self.assertNotIn("<activity", manifest)
            self.assertFalse(
                (project / "app/src/main/res/layout/activity_reconstructed.xml").exists()
            )
            self.assertFalse(
                (project / "app/src/main/java").exists()
            )

            coverage = json.loads(
                (project / "coverage_report.json").read_text(encoding="utf-8")
            )
            self.assertFalse(coverage["launcher_activity_generated"])
            self.assertEqual(coverage["status"], "partial")

            self.assertFalse((project / "analysis").exists())
            self.assertFalse((project / "work").exists())
            self.assertTrue(temporary_paths)
            self.assertFalse(temporary_paths[0].exists())

    def test_sensitive_layout_text_is_not_copied_to_generated_project(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            apk = self.make_apk(root)

            secrets = [
                "SENHA_SUPER_SECRETA_9182",
                "https://private.example/path?token=abc123",
                "CREDENTIAL_PRIVATE_7721",
            ]

            def extract(_apk, output):
                layout = output / "res/layout/private_screen.xml"
                layout.parent.mkdir(parents=True)
                layout.write_text(
                    f'''<LinearLayout
                    xmlns:android="http://schemas.android.com/apk/res/android"
                    android:layout_width="match_parent"
                    android:layout_height="match_parent">
                    <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="{secrets[0]}" />
                    <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:contentDescription="{secrets[1]}" />
                    <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:hint="{secrets[2]}" />
                    </LinearLayout>''',
                    encoding="utf-8",
                )
                return {"ok": True}

            with patch(
                "readroid.reconstructor.decompile_with_jadx",
                return_value={"ok": False},
            ), patch(
                "readroid.reconstructor.extract_smali_with_apktool",
                side_effect=extract,
            ):
                result = reconstruct_apk(
                    apk, root / "out", progress=lambda _: None
                )

            project = Path(result["generated_project"]["project_root"])
            for file in project.rglob("*"):
                if file.is_file():
                    content = file.read_text(
                        encoding="utf-8", errors="replace"
                    )
                    for secret in secrets:
                        self.assertNotIn(secret, content)

    def test_safe_visual_attributes_are_preserved_but_resource_values_are_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            apk = self.make_apk(root)

            def extract(_apk, output):
                layout = output / "res/layout/visual.xml"
                layout.parent.mkdir(parents=True)
                layout.write_text(
                    '''<LinearLayout
                    xmlns:android="http://schemas.android.com/apk/res/android"
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    android:orientation="vertical"
                    android:padding="12dp"
                    android:background="#112233">
                    <TextView
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:layout_marginTop="8dp"
                    android:textSize="18sp"
                    android:textColor="#AABBCC"
                    android:text="TEXTO_PRIVADO_NAO_COPIAR"
                    android:background="@color/private_background" />
                    <ImageView
                    android:layout_width="48dp"
                    android:layout_height="48dp"
                    android:scaleType="centerCrop"
                    android:src="@drawable/private_image" />
                    </LinearLayout>''',
                    encoding="utf-8",
                )
                return {"ok": True}

            with patch(
                "readroid.reconstructor.decompile_with_jadx",
                return_value={"ok": False},
            ), patch(
                "readroid.reconstructor.extract_smali_with_apktool",
                side_effect=extract,
            ):
                result = reconstruct_apk(
                    apk, root / "out", progress=lambda _: None
                )

            project = Path(result["generated_project"]["project_root"])
            generated_layout = (
                project / "app/src/main/res/layout/visual.xml"
            ).read_text(encoding="utf-8")

            self.assertIn('android:padding="12dp"', generated_layout)
            self.assertIn('android:background="#112233"', generated_layout)
            self.assertIn('android:layout_marginTop="8dp"', generated_layout)
            self.assertIn('android:textSize="18sp"', generated_layout)
            self.assertIn('android:textColor="#AABBCC"', generated_layout)
            self.assertIn('android:scaleType="centerCrop"', generated_layout)
            self.assertNotIn("TEXTO_PRIVADO_NAO_COPIAR", generated_layout)
            self.assertNotIn("@color/private_background", generated_layout)
            self.assertNotIn("@drawable/private_image", generated_layout)
            self.assertNotIn("android:src", generated_layout)

            coverage = json.loads(
                (project / "coverage_report.json").read_text(encoding="utf-8")
            )
            self.assertGreater(
                coverage["sanitization"]["safe_literal_visual_attributes_preserved"],
                0,
            )
            self.assertFalse(coverage["sanitization"]["resource_references_copied"])

    def test_raw_analysis_directories_are_not_retained(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            apk = self.make_apk(root)
            captured = []

            def fake_jadx(_apk, output, threads=2):
                captured.append(output)
                output.mkdir(parents=True)
                (output / "PrivateSource.java").write_text(
                    "private source marker", encoding="utf-8"
                )
                return {
                    "ok": True,
                    "status": "success",
                    "files_generated": 1,
                    "stdout": "private stdout marker",
                    "stderr": "private stderr marker",
                }

            def fake_apktool(_apk, output):
                captured.append(output)
                output.mkdir(parents=True)
                (output / "private.smali").write_text(
                    "private smali marker", encoding="utf-8"
                )
                return {
                    "ok": True,
                    "stdout": "private apktool marker",
                    "stderr": "",
                }

            with patch(
                "readroid.reconstructor.decompile_with_jadx",
                side_effect=fake_jadx,
            ), patch(
                "readroid.reconstructor.extract_smali_with_apktool",
                side_effect=fake_apktool,
            ):
                result = reconstruct_apk(
                    apk, root / "out", progress=lambda _: None
                )

            output = Path(result["output_root"])
            self.assertTrue(captured)
            for temporary_path in captured:
                self.assertFalse(temporary_path.exists())

            for file in output.rglob("*"):
                if file.is_file():
                    content = file.read_text(
                        encoding="utf-8", errors="replace"
                    )
                    self.assertNotIn("private source marker", content)
                    self.assertNotIn("private smali marker", content)
                    self.assertNotIn("private stdout marker", content)
                    self.assertNotIn("private stderr marker", content)
                    self.assertNotIn("private apktool marker", content)

            self.assertFalse((output / "analysis").exists())
            self.assertFalse((output / "work").exists())


if __name__ == "__main__":
    unittest.main()

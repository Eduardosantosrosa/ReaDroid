from __future__ import annotations

import json
import re
import tempfile
import time
from datetime import datetime
from pathlib import Path

from .functional_spec import write_functional_spec

from .analyzer import (
    inspect_apk,
    analyze_manifest,
    decompile_with_jadx,
    extract_smali_with_apktool,
    write_report,
)


def reconstruct_apk(apk: Path, output_base: Path, progress=print) -> dict:
    """Analisa o APK localmente; mantém somente resultados sanitizados."""
    start = time.time()
    apk = apk.expanduser().resolve()

    if not apk.is_file():
        raise FileNotFoundError("APK selecionado não encontrado.")

    info = inspect_apk(apk)
    digest = info["sha256"]
    stamp = datetime.now().strftime("%Y%m%d_%H%M%S")

    output_base.mkdir(parents=True, exist_ok=True)
    root = output_base / f"reconstrucao_{digest[:12]}_{stamp}"
    root.mkdir(parents=True, exist_ok=False)

    report = {
        "tool": "ReaDroid Native Reconstruction",
        "source_sha256": digest,
        "status": "incomplete",
        "steps": {},
        "warnings": [],
        "privacy": {
            "raw_extraction_retained": False,
            "decompiled_source_retained": False,
            "temporary_analysis": True,
        },
        "output_root": str(root),
    }

    try:
        with tempfile.TemporaryDirectory(prefix="readroid_recon_") as temporary:
            work = Path(temporary)
            manifest_dir = work / "manifest"
            jadx_dir = work / "jadx"
            apktool_dir = work / "apktool"

            progress("1/3 - Analisando manifesto em área temporária...")
            manifest_raw = analyze_manifest(apk, manifest_dir)

            # Somente informações estruturais mínimas são aproveitadas.
            package = manifest_raw.get("package") or "com.readroid.reconstructed"
            package = re.sub(r"[^A-Za-z0-9_.]", ".", str(package))
            manifest = {
                "package": package,
                "components": [],
                "permissions": [],
            }

            report["steps"]["manifest"] = {
                "decoded": bool(manifest_raw.get("decoded")),
                "package_available": bool(manifest_raw.get("package")),
                "component_count": len(manifest_raw.get("components", [])),
                "permission_count": len(manifest_raw.get("permissions", [])),
            }

            progress("2/3 - Extraindo evidências em área temporária...")
            jadx = decompile_with_jadx(apk, jadx_dir, threads=2)
            apktool = extract_smali_with_apktool(apk, apktool_dir)

            # Não registrar stdout, stderr, comandos, caminhos ou nomes privados.
            report["steps"]["jadx"] = {
                "status": jadx.get("status", "unavailable"),
                "ok": bool(jadx.get("ok")),
                "partial": bool(jadx.get("partial")),
                "files_generated": int(jadx.get("files_generated", 0)),
            }
            report["steps"]["apktool"] = {
                "ok": bool(apktool.get("ok")),
                "available": "returncode" in apktool,
            }

            progress("3/3 - Gerando projeto com evidências sanitizadas...")
            try:
                from .native_project import generate_native_project

                generated = generate_native_project(
                    root, package, manifest, apktool_dir, {}
                )
            except ValueError as exc:
                # Não expor mensagens arbitrárias de ferramentas ou do APK.
                report["warnings"].append(
                    "Não foi possível gerar projeto: evidências visuais insuficientes."
                )
                report["generated_project"] = None
                report["status"] = "reconstruction_unavailable"
                coverage = {
                    "status": "insufficient_evidence",
                    "layouts": 0,
                    "behaviors": {
                        "proven": [],
                        "inferred": [],
                        "unavailable": [
                            "interface",
                            "navigation",
                            "backend",
                        ],
                    },
                }
                report["coverage"] = coverage
                (root / "coverage_report.json").write_text(
                    json.dumps(coverage, ensure_ascii=False, indent=2),
                    encoding="utf-8",
                )
            else:
                report["generated_project"] = generated
                report["status"] = "partial"
                report["coverage"] = "coverage_report.json"

            # Exporta somente contagens e categorias sanitizadas.
            coverage_data = coverage if report["status"] == "reconstruction_unavailable" else json.loads(
                (root / "coverage_report.json").read_text(encoding="utf-8")
            )
            report["functional_spec"] = write_functional_spec(
                root, info, manifest_raw, coverage_data
            )

    except Exception:
        report["status"] = "error"
        report["warnings"].append(
            "A análise foi interrompida. Dados temporários não são incluídos no projeto."
        )
        report["elapsed_seconds"] = round(time.time() - start, 2)
        write_report(report, root / "reconstruction_report.json")
        raise

    report["elapsed_seconds"] = round(time.time() - start, 2)
    write_report(report, root / "reconstruction_report.json")
    return report

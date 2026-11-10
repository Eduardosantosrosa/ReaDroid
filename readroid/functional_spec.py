import json
import re
from collections import Counter
from pathlib import Path

RULES = (
    ("authentication", r"login|signin|sign_in|auth|senha|password"),
    ("media_playback", r"player|playback|video|audio"),
    ("content_listing", r"lista|list|catalog|home|channel|canal"),
    ("content_details", r"detalhe|detail|episode|episodio|poster"),
    ("loading_or_splash", r"splash|load|loading"),
    ("account_or_profile", r"conta|account|profile|perfil"),
)


def count(value):
    if isinstance(value, bool):
        return 0
    if isinstance(value, (list, tuple, set)):
        return len(value)
    if isinstance(value, dict):
        return 0
    try:
        return max(0, int(value or 0))
    except (TypeError, ValueError, OverflowError):
        return 0


def safe_sha256(value):
    value = str(value or "").lower()
    return value if re.fullmatch(r"[a-f0-9]{64}", value) else ""


def safe_artifact_type(value):
    allowed = {"layout_evidence_catalog"}
    if not isinstance(value, str):
        return "unknown"
    return value if value in allowed else "unknown"


def build_functional_spec(apk_info, manifest, coverage):
    components = manifest.get("components", [])
    permissions = manifest.get("permissions", [])
    types, exported = Counter(), Counter()

    for item in components if isinstance(components, list) else []:
        if not isinstance(item, dict):
            continue
        kind = str(item.get("type", "unknown")).lower()
        if kind not in {"activity", "service", "receiver", "provider"}:
            kind = "unknown"
        types[kind] += 1
        if item.get("exported") is True:
            exported[kind] += 1

    safe_permissions = sorted({
        str(p.get("name")) for p in permissions
        if isinstance(p, dict) and re.fullmatch(
            r"android\.permission\.[A-Z0-9_]+",
            str(p.get("name", ""))
        )
    }) if isinstance(permissions, list) else []

    layouts = coverage.get("source_layouts", [])
    layouts = (
        [item for item in layouts if isinstance(item, dict)]
        if isinstance(layouts, list) else []
    )
    clues, view_total = Counter(), 0

    for layout in layouts:
        name = str(layout.get(
            "name", layout.get("layout", layout.get("file", ""))
        ))
        category = next(
            (label for label, pattern in RULES
             if re.search(pattern, name, re.I)),
            "unclassified_layout"
        )
        clues[category] += 1
        view_total += count(layout.get("view_count", 0))

    app = manifest.get("application", {})
    if not isinstance(app, dict):
        app = {}

    return {
        "schema_version": 1,
        "purpose": "Especificacao funcional sanitizada para reimplementacao independente",
        "implementation_target": {
            "language": "Python",
            "ui_framework": "Kivy"
        },
        "provenance": {
            "analysis_type": "static_apk_analysis",
            "source_sha256": safe_sha256(apk_info.get("sha256", "")),
            "runtime_behavior_verified": False
        },
        "confirmed_static_evidence": {
            "apk": {
                "size_bytes": count(apk_info.get("file_size_bytes")),
                "dex_file_count": count(apk_info.get("dex_files")),
                "resource_file_count": count(apk_info.get("resource_file_count")),
                "asset_file_count": count(apk_info.get("asset_file_count")),
                "zip_entry_count": count(apk_info.get("zip_entry_count"))
            },
            "manifest": {
                "decoded": bool(manifest.get("decoded")),
                "component_count": sum(types.values()),
                "components_by_type": dict(sorted(types.items())),
                "exported_components_by_type": dict(sorted(exported.items())),
                "standard_android_permissions": safe_permissions,
                "application_flags": {
                    k: app[k]
                    for k in (
                        "debuggable", "allow_backup",
                        "uses_cleartext_traffic"
                    )
                    if isinstance(app.get(k), bool)
                }
            },
            "layout_catalog": {
                "layout_count": len(layouts),
                "view_count_total": view_total,
                "generic_feature_clues": dict(sorted(clues.items())),
                "artifact_type": safe_artifact_type(coverage.get("artifact_type"))
            }
        },
        "interpretation_policy": {
            "confirmed_evidence_only": True,
            "layout_names_are_not_screen_proof": True,
            "candidate_features_are_not_implementation_requirements": True,
            "unknown_behavior_must_be_validated_before_implementation": True
        },
        "candidate_features": [
            {
                "category": k,
                "evidence": "layout_filename_pattern",
                "evidence_count": n,
                "confidence": "low",
                "interpretation": (
                    "Indicio de interface; nao confirma tela, fluxo "
                    "ou comportamento executavel."
                )
            }
            for k, n in sorted(clues.items())
            if k != "unclassified_layout"
        ],
        "not_established": [
            "Activity inicial e pontos de entrada",
            "Associacao entre componentes Android e layouts",
            "Navegacao, rotas e transicoes entre telas",
            "Acoes de botoes, gestos e manipuladores de eventos",
            "Regras de negocio e resultados em tempo de execucao",
            "Autenticacao, contas e persistencia de sessao",
            "Servicos remotos, APIs, endpoints e protocolos efetivamente usados",
            "Comportamento de video, audio e integracoes nativas"
        ],
        "implementation_constraints": [
            "Implementar independentemente em Python e Kivy.",
            "Usar a especificacao sanitizada como evidencia, nao como codigo-fonte.",
            "Nao copiar codigo, textos, imagens, marcas, identificadores privados ou recursos protegidos do APK.",
            "Validar cada fluxo em testes proprios; nao transformar indicios em funcionalidades confirmadas.",
            "Avaliar pontes Android nativas quando uma capacidade nao for coberta pelo Kivy puro."
        ],
        "limitations": [
            "A analise e estatica e nao comprova comportamento em execucao.",
            "Nomes originais de pacotes, classes, componentes e layouts foram omitidos.",
            "Textos, URLs, dominios, enderecos IP, credenciais, codigo e arquivos extraidos nao fazem parte desta especificacao.",
            "Categorias derivadas de nomes de layouts sao indicios de baixa confianca."
        ]
    }


def render_functional_spec(spec):
    ev = spec["confirmed_static_evidence"]
    apk, mf, cat = ev["apk"], ev["manifest"], ev["layout_catalog"]

    lines = [
        "# Especificacao funcional sanitizada",
        "",
        "## Objetivo",
        spec["purpose"],
        "",
        "## Plataforma",
        "- Linguagem: Python",
        "- Interface: Kivy",
        "",
        "## Evidencias estaticas confirmadas",
        f"- Tamanho do APK: {apk['size_bytes']} bytes",
        f"- Arquivos DEX: {apk['dex_file_count']}",
        f"- Recursos: {apk['resource_file_count']}",
        f"- Assets: {apk['asset_file_count']}",
        f"- Entradas ZIP: {apk['zip_entry_count']}",
        f"- Manifesto decodificado: {'sim' if mf['decoded'] else 'nao'}",
        f"- Componentes declarados: {mf['component_count']}",
        f"- Layouts catalogados: {cat['layout_count']}",
        f"- Views catalogadas: {cat['view_count_total']}",
        "",
        "### Componentes por tipo"
    ]

    lines.extend(
        f"- {k}: {v}" for k, v in mf["components_by_type"].items()
    )
    lines.extend(["", "### Componentes exportados"])
    lines.extend(
        f"- {k}: {v}"
        for k, v in mf["exported_components_by_type"].items()
    )
    if not mf["exported_components_by_type"]:
        lines.append("- Nenhum componente exportado identificado.")

    lines.extend(["", "### Flags do aplicativo"])
    lines.extend(
        f"- {k}: {'sim' if v else 'nao'}"
        for k, v in mf["application_flags"].items()
    )
    if not mf["application_flags"]:
        lines.append("- Nenhuma flag booleana identificada.")

    lines.extend(["", "### Permissoes Android padrao"])
    lines.extend(
        f"- `{p}`" for p in mf["standard_android_permissions"]
    )
    if not mf["standard_android_permissions"]:
        lines.append("- Nenhuma identificada ou manifesto indisponivel.")

    lines.extend([
        "",
        f"- Tipo de catalogo: `{cat['artifact_type']}`",
        "",
        "## Indicios de funcionalidades (baixa confianca)"
    ])
    lines.extend([
        f"- {x['category']}: {x['evidence_count']} layout(s); "
        "nao confirma fluxo funcional."
        for x in spec["candidate_features"]
    ] or ["- Nenhuma categoria identificada."])

    lines.extend([
        "",
        "Aviso: estes indicios nao sao requisitos nem autorizam "
        "a criacao automatica de telas ou fluxos.",
        "",
        "## O que ainda nao foi comprovado"
    ])
    lines.extend(f"- {x}" for x in spec["not_established"])
    lines.extend(["", "## Diretrizes para o Codex"])
    lines.extend(f"- {x}" for x in spec["implementation_constraints"])
    lines.extend(["", "## Privacidade e limitacoes"])
    lines.extend(f"- {x}" for x in spec["limitations"])
    lines.extend([
        "",
        "## Proveniencia",
        f"- SHA-256 da origem: `{spec['provenance']['source_sha256']}`",
        "- Analise estatica; comportamento em execucao nao verificado.",
        ""
    ])
    return "\n".join(lines)


def write_functional_spec(output_root, apk_info, manifest, coverage):
    spec = build_functional_spec(apk_info, manifest, coverage)
    (output_root / "functional_spec.json").write_text(
        json.dumps(spec, ensure_ascii=False, indent=2),
        encoding="utf-8"
    )
    (output_root / "FUNCTIONAL_SPEC.md").write_text(
        render_functional_spec(spec),
        encoding="utf-8"
    )
    return {
        "status": "created",
        "json": "functional_spec.json",
        "markdown": "FUNCTIONAL_SPEC.md"
    }

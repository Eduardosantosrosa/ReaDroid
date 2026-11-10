from __future__ import annotations

import json
import re
import xml.etree.ElementTree as ET
from pathlib import Path

ANDROID = "http://schemas.android.com/apk/res/android"
ET.register_namespace("android", ANDROID)

SAFE_TAGS = {
    "View", "ViewGroup", "LinearLayout", "RelativeLayout",
    "FrameLayout", "TextView", "Button", "ImageView",
    "ImageButton", "EditText", "ScrollView",
    "HorizontalScrollView", "ProgressBar", "CheckBox",
    "RadioButton", "Switch", "ToggleButton", "Spinner",
    "TableLayout", "TableRow", "GridLayout",
}

SAFE_ATTRIBUTES = {
    "layout_width", "layout_height", "orientation",
    "gravity", "layout_gravity", "padding", "paddingLeft",
    "paddingRight", "paddingTop", "paddingBottom",
    "paddingStart", "paddingEnd", "paddingHorizontal",
    "paddingVertical", "textSize", "visibility", "enabled",
    "checked", "inputType", "singleLine", "maxLines",
    "minLines", "maxLength", "layout_weight",
    "layout_margin", "layout_marginLeft", "layout_marginRight",
    "layout_marginTop", "layout_marginBottom",
    "layout_marginStart", "layout_marginEnd",
    "background", "textColor", "textColorHint",
    "scaleType", "textStyle", "ellipsize",
    "minWidth", "minHeight", "maxWidth", "maxHeight",
    "elevation", "alpha", "rotation",
    "layout_column", "layout_span", "numColumns",
    "scrollbars", "indeterminate", "progress", "max",
    "baselineAligned", "clipToPadding", "fillViewport",
    "stretchMode",
}

_DIMENSION = re.compile(
    r"-?(?:[0-9]+(?:\.[0-9]+)?|\.[0-9]+)(?:dp|dip|sp|px|pt|in|mm)\Z",
    re.IGNORECASE,
)
_COLOR = re.compile(r"#[0-9a-fA-F]{6}(?:[0-9a-fA-F]{2})?\Z")
_INTEGER = re.compile(r"[0-9]+\Z")
_NUMBER = re.compile(r"(?:[0-9]+(?:\.[0-9]+)?|\.[0-9]+)\Z")

_DIMENSION_ATTRIBUTES = {
    "padding", "paddingLeft", "paddingRight", "paddingTop",
    "paddingBottom", "paddingStart", "paddingEnd",
    "paddingHorizontal", "paddingVertical", "textSize",
    "layout_margin", "layout_marginLeft", "layout_marginRight",
    "layout_marginTop", "layout_marginBottom",
    "layout_marginStart", "layout_marginEnd",
    "minWidth", "minHeight", "maxWidth", "maxHeight", "elevation",
}
_COLOR_ATTRIBUTES = {"background", "textColor", "textColorHint"}
_BOOLEAN_ATTRIBUTES = {
    "enabled", "checked", "singleLine", "indeterminate",
    "baselineAligned", "clipToPadding", "fillViewport",
}
_INTEGER_ATTRIBUTES = {
    "maxLines", "minLines", "maxLength", "layout_column",
    "layout_span", "numColumns", "progress", "max",
}
_GRAVITY_VALUES = {
    "top", "bottom", "left", "right", "start", "end",
    "center", "center_horizontal", "center_vertical",
    "fill", "fill_horizontal", "fill_vertical",
    "clip_horizontal", "clip_vertical",
}
_INPUT_TYPE_VALUES = {
    "text", "number", "phone", "datetime", "date", "time",
    "textEmailAddress", "textUri", "textPersonName",
    "textCapWords", "textCapSentences", "textMultiLine",
    "numberDecimal", "numberSigned",
}


def safe_attribute_value(name: str, value: str) -> bool:
    """Aceita somente valores literais simples e visualmente seguros."""
    if not isinstance(value, str) or not value or value != value.strip():
        return False

    if value.startswith(("@", "?", "http://", "https://")):
        return False
    if re.search(r"(?i)(token|password|senha|credential|https?://|www\.)", value):
        return False

    if name in {"layout_width", "layout_height"}:
        return value in {"match_parent", "wrap_content", "fill_parent"} or bool(
            _DIMENSION.fullmatch(value)
        )
    if name in _DIMENSION_ATTRIBUTES:
        return bool(_DIMENSION.fullmatch(value))
    if name in _COLOR_ATTRIBUTES:
        return bool(_COLOR.fullmatch(value))
    if name in _BOOLEAN_ATTRIBUTES:
        return value.lower() in {"true", "false"}
    if name in _INTEGER_ATTRIBUTES:
        return bool(_INTEGER.fullmatch(value))
    if name in {"gravity", "layout_gravity"}:
        parts = value.split("|")
        return all(part in _GRAVITY_VALUES for part in parts)
    if name == "orientation":
        return value in {"horizontal", "vertical"}
    if name == "visibility":
        return value in {"visible", "invisible", "gone"}
    if name == "layout_weight":
        return bool(_NUMBER.fullmatch(value)) and float(value) >= 0
    if name == "alpha":
        return bool(_NUMBER.fullmatch(value)) and 0 <= float(value) <= 1
    if name == "rotation":
        return bool(_NUMBER.fullmatch(value)) and abs(float(value)) <= 360
    if name == "scaleType":
        return value in {
            "matrix", "fitXY", "fitStart", "fitCenter", "fitEnd",
            "center", "centerInside", "centerCrop",
        }
    if name == "textStyle":
        return all(part in {"normal", "bold", "italic"} for part in value.split("|"))
    if name == "ellipsize":
        return value in {"start", "middle", "end", "marquee", "none"}
    if name == "inputType":
        return all(part in _INPUT_TYPE_VALUES for part in value.split("|"))
    if name == "scrollbars":
        return value in {"none", "horizontal", "vertical"}
    if name == "stretchMode":
        return value in {"none", "spacingWidth", "columnWidth", "allColumnWidths"}
    return False



def write(path: Path, content: str):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


def safe_package(value: str) -> str:
    parts = []
    for part in str(value or "").split("."):
        part = re.sub(r"[^a-zA-Z0-9_]", "_", part)
        if not part or not part[0].isalpha():
            part = "app_" + part
        parts.append(part)
    return ".".join(parts[:5]) or "com.example.rebuilt"


def generate_native_project(
    root: Path,
    package: str,
    manifest: dict,
    analysis: Path,
    apk_info: dict,
) -> dict:
    """Gera catálogo de layouts sanitizados, sem inventar telas ou navegação."""
    package = safe_package(package)
    app = root / "app"
    layout_dir = app / "src/main/res/layout"
    values_dir = app / "src/main/res/values"

    layout_dir.mkdir(parents=True, exist_ok=True)
    values_dir.mkdir(parents=True, exist_ok=True)

    evidence_layouts = []
    omitted_elements = 0
    preserved_attributes = 0
    source_dir = analysis / "res" / "layout"

    def sanitize(node):
        nonlocal omitted_elements, preserved_attributes

        tag = node.tag.rsplit("}", 1)[-1]
        if tag not in SAFE_TAGS:
            omitted_elements += sum(1 for _ in node.iter())
            return None

        out = ET.Element(tag)

        for key, value in node.attrib.items():
            name = key.rsplit("}", 1)[-1]

            # Textos, IDs, referências, URLs, credenciais e recursos
            # de origem não são copiados para o projeto independente.
            if name not in SAFE_ATTRIBUTES:
                continue
            if not safe_attribute_value(name, value):
                continue

            out.set("{" + ANDROID + "}" + name, value)
            preserved_attributes += 1

        for child in node:
            item = sanitize(child)
            if item is not None:
                out.append(item)

        return out

    if source_dir.is_dir():
        for source in sorted(source_dir.glob("*.xml")):
            if not re.fullmatch(r"[a-z0-9_]+", source.stem):
                continue

            try:
                original = ET.parse(source).getroot()
            except (ET.ParseError, OSError):
                continue

            clean = sanitize(original)
            if clean is None or sum(1 for _ in clean.iter()) < 2:
                continue

            destination = layout_dir / (source.stem + ".xml")
            ET.ElementTree(clean).write(
                destination,
                encoding="utf-8",
                xml_declaration=True,
            )

            evidence_layouts.append({
                "layout": source.stem,
                "view_count": sum(1 for _ in clean.iter()),
            })

    if not evidence_layouts:
        raise ValueError(
            "Não há layouts XML decodificados com evidência visual suficiente."
        )

    write(
        values_dir / "strings.xml",
        '<?xml version="1.0" encoding="utf-8"?>'
        '<resources><string name="app_name">'
        'Catálogo de layouts baseado em evidências'
        '</string></resources>',
    )

    write(
        values_dir / "styles.xml",
        '<?xml version="1.0" encoding="utf-8"?>'
        '<resources><style name="AppTheme" '
        'parent="@android:style/Theme.Material.Light.NoActionBar" />'
        '</resources>',
    )

    write(
        app / "build.gradle",
        f"""plugins {{ id 'com.android.application' }}
android {{
    namespace '{package}'
    compileSdk 35
    defaultConfig {{
        applicationId '{package}'
        minSdk 23
        targetSdk 28
        versionCode 1
        versionName '1.0'
    }}
}}
""",
    )

    write(
        root / "settings.gradle",
        "pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }\n"
        "dependencyResolutionManagement { repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS); "
        "repositories { google(); mavenCentral() } }\n"
        "rootProject.name='CatalogoDeLayouts'\ninclude(':app')\n",
    )

    write(
        root / "build.gradle",
        "plugins { id 'com.android.application' version '8.7.3' apply false }\n",
    )

    write(
        root / "gradle.properties",
        "org.gradle.jvmargs=-Xmx768m -Dfile.encoding=UTF-8\n"
        "android.useAndroidX=true\n",
    )

    # Manifesto deliberadamente sem Activity lançadora:
    # não há prova de qual layout pertence à tela inicial.
    write(
        app / "src/main/AndroidManifest.xml",
        f"""<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="{ANDROID}" package="{package}">
    <application
        android:label="@string/app_name"
        android:theme="@style/AppTheme" />
</manifest>
""",
    )

    coverage = {
        "status": "partial",
        "artifact_type": "layout_evidence_catalog",
        "source_layouts": evidence_layouts,
        "activities_with_interface_proof": [],
        "launcher_activity_generated": False,
        "navigation_reconstructed": False,
        "behaviors": {
            "proven": [],
            "inferred": [],
            "unavailable": [
                "activity-to-layout mapping",
                "launcher screen",
                "event handlers",
                "navigation",
                "backend",
                "authentication",
                "network features",
            ],
        },
        "sanitization": {
            "literal_text_copied": False,
            "resource_references_copied": False,
            "safe_literal_visual_attributes_preserved": preserved_attributes,
            "unknown_view_types_omitted": omitted_elements,
        },
        "limitations": [
            "Layouts são evidências catalogadas, não telas funcionais reconstruídas.",
            "Não foi presumida relação entre Activities e layouts.",
            "Não foi criada Activity lançadora artificial.",
            "Textos literais, IDs, referências e recursos originais foram omitidos.",
            "Views desconhecidas ou personalizadas foram omitidas.",
            "Navegação, lógica e backend não foram reconstruídos.",
            "Nenhuma compilação Gradle foi executada.",
        ],
    }

    write(
        root / "coverage_report.json",
        json.dumps(coverage, ensure_ascii=False, indent=2),
    )

    write(
        root / "README.md",
        "# Catálogo Android de layouts baseado em evidências\n\n"
        "Este projeto contém layouts XML sanitizados encontrados na análise local.\n\n"
        "**Não é uma reconstrução funcional do aplicativo original.** "
        "Nenhuma tela inicial, Activity, navegação ou comportamento foi presumido.\n\n"
        "Textos literais, IDs e referências aos recursos originais foram omitidos "
        "por segurança e independência da implementação.\n\n"
        "Consulte `coverage_report.json` para conhecer as evidências e limitações.\n"
        "Nenhuma compilação Gradle foi executada.\n",
    )

    return {
        "package": package,
        "screens_identified": 0,
        "java_files_generated": 0,
        "xml_layouts_generated": len(evidence_layouts),
        "launcher_activity_generated": False,
        "project_root": str(root),
        "status": "partial",
    }

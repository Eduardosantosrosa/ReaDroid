"""Guided, local-only inspection client for the Android Device Inspector helper."""
from __future__ import annotations

import hashlib
import json
import subprocess
import time
import urllib.error
import urllib.request
from datetime import datetime
from pathlib import Path
from typing import Callable


BRIDGE = "http://127.0.0.1:8765"
DEFAULT_REFERENCE_PACKAGE = "com.turboplay.play"


class InspectorClient:
    def __init__(self, pairing_code: str, base_url: str = BRIDGE):
        self.base_url = base_url.rstrip("/")
        self.pairing_code = pairing_code.strip()

    def request(self, path: str, method: str = "GET", data: dict | None = None,
                timeout: float = 8, binary: bool = False):
        payload = None if data is None else json.dumps(data, ensure_ascii=False).encode("utf-8")
        headers = {"X-Inspector-Code": self.pairing_code, "Accept": "application/json"}
        if payload is not None:
            headers["Content-Type"] = "application/json; charset=utf-8"
        req = urllib.request.Request(self.base_url + path, data=payload, headers=headers, method=method)
        try:
            with urllib.request.urlopen(req, timeout=timeout) as response:
                content = response.read()
        except urllib.error.HTTPError as exc:
            body = exc.read().decode("utf-8", errors="replace")
            try:
                detail = json.loads(body).get("error", body)
            except ValueError:
                detail = body
            raise RuntimeError(f"HTTP {exc.code}: {detail}") from exc
        return content if binary else json.loads(content.decode("utf-8"))

    def health(self) -> dict:
        req = urllib.request.Request(self.base_url + "/health", headers={"Accept": "application/json"})
        with urllib.request.urlopen(req, timeout=2) as response:
            return json.loads(response.read().decode("utf-8"))


class InspectionSession:
    """Stores screenshots, accessible UI trees, accessibility events and transitions."""

    def __init__(self, reports_dir: Path, client: InspectorClient, package: str):
        stamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        self.root = reports_dir / "device_inspections" / f"{stamp}_{package.replace('.', '_')}"
        self.screens_dir = self.root / "screens"
        self.screens_dir.mkdir(parents=True, exist_ok=False)
        self.client = client
        self.package = package
        self.cursor = 0
        self.screen_no = 0
        self.screens: list[dict] = []
        self.transitions: list[dict] = []
        self._save_report("running")

    @staticmethod
    def fingerprint(snapshot: dict) -> str:
        visible = []
        for node in snapshot.get("nodes", []):
            if node.get("editable") or node.get("password"):
                continue
            text = (node.get("text") or "").strip()
            desc = (node.get("content_description") or "").strip()
            if text or desc:
                visible.append((node.get("class", ""), text[:240], desc[:240]))
        payload = json.dumps([snapshot.get("package", ""), visible], ensure_ascii=False, separators=(",", ":"))
        return hashlib.sha256(payload.encode("utf-8")).hexdigest()[:16]

    def capture(self, emit: Callable, reason: str) -> dict:
        emit("Lendo árvore de acessibilidade…", stage="Captura da árvore UI")
        snapshot = self.client.request("/snapshot")
        emit("Capturando quadro autorizado do MediaProjection…", stage="Captura de tela")
        png = self.client.request("/capture", timeout=10, binary=True)
        self.screen_no += 1
        prefix = f"screen_{self.screen_no:04d}"
        image_path = self.screens_dir / f"{prefix}.png"
        tree_path = self.screens_dir / f"{prefix}.json"
        image_path.write_bytes(png)
        fingerprint = self.fingerprint(snapshot)
        snapshot["screen_fingerprint"] = fingerprint
        snapshot["screen_number"] = self.screen_no
        snapshot["capture_reason"] = reason
        snapshot["screenshot"] = str(image_path)
        tree_path.write_text(json.dumps(snapshot, ensure_ascii=False, indent=2), encoding="utf-8")
        screen = {"number": self.screen_no, "fingerprint": fingerprint,
                  "package": snapshot.get("package", ""), "screenshot": str(image_path),
                  "metadata": str(tree_path), "reason": reason,
                  "accessible_node_count": snapshot.get("node_count", 0)}
        self.screens.append(screen)
        emit(f"Tela {self.screen_no}: {screen['package'] or 'pacote da janela indisponível'}; "
             f"{screen['accessible_node_count']} nós; PNG salvo.", stage=f"Tela {self.screen_no} catalogada")
        self._collect_events()
        self._save_report("running")
        return snapshot

    def _collect_events(self) -> list[dict]:
        result = self.client.request(f"/events?since={self.cursor}")
        events = result.get("events", [])
        if events:
            with (self.root / "events.jsonl").open("a", encoding="utf-8") as stream:
                for event in events:
                    stream.write(json.dumps(event, ensure_ascii=False) + "\n")
                    self.cursor = max(self.cursor, int(event.get("sequence", self.cursor)))
        return events

    def perform_and_capture(self, action: dict, label: str, before: dict, emit: Callable) -> dict:
        emit("Enviando ação explicitamente selecionada…", stage="Ação no dispositivo")
        result = self.client.request("/action", method="POST", data=action)
        emit("Ação aceita pelo auxiliar: " + str(result.get("ok")), stage="Resultado da ação")
        if not result.get("ok"):
            raise RuntimeError("O serviço de acessibilidade recusou a ação.")
        old = self.fingerprint(before)
        deadline = time.monotonic() + 5
        changed = False
        while time.monotonic() < deadline:
            time.sleep(.4)
            try:
                candidate = self.client.request("/snapshot", timeout=3)
                if self.fingerprint(candidate) != old:
                    changed = True
                    break
            except Exception:
                pass
        after = self.capture(emit, f"após ação: {label}")
        edge = {"from_fingerprint": old, "to_fingerprint": self.fingerprint(after),
                "action": action, "label": label, "visible_screen_change": changed,
                "recorded_at": datetime.now().astimezone().isoformat()}
        self.transitions.append(edge)
        self._save_report("running")
        emit("Mudança visível detectada." if changed else
             "Nenhuma mudança de árvore confirmada após a ação.",
             kind="success" if changed else "warning", stage="Transição registrada")
        return after

    def finish(self, status: str = "finished") -> Path:
        self._collect_events()
        return self._save_report(status)

    def _save_report(self, status: str) -> Path:
        unique = {}
        for screen in self.screens:
            unique.setdefault(screen["fingerprint"], screen)
        report = {
            "tool": "ReaDroid Device Inspector",
            "status": status,
            "target_package": self.package,
            "created_at": datetime.now().astimezone().isoformat(),
            "screens_captured": len(self.screens),
            "unique_accessible_screens": len(unique),
            "screen_catalog": list(unique.values()),
            "captures": self.screens,
            "transitions": self.transitions,
            "verified_observations": [
                {"screen_fingerprint": screen["fingerprint"], "package": screen["package"],
                 "screenshot": screen["screenshot"], "metadata": screen["metadata"]}
                for screen in unique.values()
            ],
            "not_observed": [
                "Fluxos não visitados durante esta sessão.",
                "Comportamentos que não aparecem na árvore de acessibilidade.",
                "Resultados de backend ou mídia não são inferidos a partir de nomes ou imagens.",
            ],
            "privacy": {"editable_and_password_text_saved": False,
                        "network_access_by_inspector": False,
                        "screenshots_saved_by_readroid": True},
        }
        path = self.root / "inspection_report.json"
        temp = path.with_suffix(".tmp")
        temp.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
        temp.replace(path)
        return path


def installed_third_party_packages() -> list[str]:
    try:
        result = subprocess.run(["pm", "list", "packages", "-3"], capture_output=True,
                                text=True, timeout=8, check=False)
        return sorted(line.removeprefix("package:").strip() for line in result.stdout.splitlines()
                      if line.startswith("package:") and line.strip())
    except (OSError, subprocess.TimeoutExpired):
        return []


def _run_monitored(title: str, operation: Callable, reports_dir: Path,
                   summary: Callable | None = None):
    from .process_viewer import run_with_viewer
    return run_with_viewer(reports_dir, title, operation, summary=summary)


def inspect_device(reports_dir: Path, safe_input: Callable[[str], str],
                   print_line: Callable[..., None], colors: dict | None = None) -> None:
    """Interactive guided inspection. Screenshots are automatic; taps are explicit."""
    from .process_viewer import ProcessRun
    try:
        health = InspectorClient("").health()
    except Exception:
        print_line("Auxiliar não conectado em 127.0.0.1:8765.")
        print_line("Instale o APK android-app/inspector-helper, habilite o serviço em "
                   "Configurações > Acessibilidade, abra o auxiliar e autorize a captura da tela.")
        print_line("Não habilite opções de depuração USB; o bridge local não usa ADB.")
        return
    if not health.get("ok"):
        print_line("Resposta de saúde inesperada do auxiliar.")
        return

    code = safe_input("Código de conexão exibido no auxiliar: ").strip()
    client = InspectorClient(code)
    try:
        status = client.request("/status")
    except Exception as exc:
        print_line(f"Não foi possível autenticar no auxiliar: {exc}")
        return
    if not status.get("accessibility_enabled") or not status.get("projection_active"):
        print_line("Autorizações incompletas. Acessibilidade:", status.get("accessibility_enabled"),
                   "· captura:", status.get("projection_active"))
        print_line("Volte ao auxiliar e habilite o AccessibilityService e o MediaProjection manualmente.")
        return

    packages = installed_third_party_packages()
    default = DEFAULT_REFERENCE_PACKAGE if DEFAULT_REFERENCE_PACKAGE in packages else ""
    package = safe_input(f"Pacote instalado para inspecionar [{default}]: ").strip() or default
    if package not in packages:
        print_line("Pacote não aparece na lista de apps de terceiros visível ao Termux.")
        return

    session: InspectionSession | None = None
    try:
        run, _ = _run_monitored(
            "Abrir app e catalogar primeira tela",
            lambda emit: _launch_and_capture(client, package, reports_dir, emit),
            reports_dir,
            summary=lambda value: {"ok": True, "package": package,
                                   "screen": value["snapshot"].get("screen_fingerprint")},
        )
        session = run["session"]
        snapshot = run["snapshot"]
        print_line(f"Inspeção salva em: {session.root}")
    except Exception as exc:
        print_line(f"Falha ao abrir/capturar o app: {exc}")
        return

    try:
        while True:
            print_line("\nTela", snapshot.get("screen_number"), "· pacote:", snapshot.get("package", "?"))
            candidates = []
            for node in snapshot.get("nodes", []):
                if node.get("editable") or node.get("password"):
                    continue
                value = (node.get("text") or node.get("content_description") or "").strip()
                if not value and not node.get("clickable"):
                    continue
                if node.get("clickable") and node.get("enabled"):
                    candidates.append(node)
                if value:
                    safe_value = value[:160]
                    print_line(f"  #{node.get('index')} {'[clicável]' if node.get('clickable') else ''} "
                               f"{node.get('class', '')} {safe_value} {node.get('bounds', {})}")
            print_line("Ações: número = clique acessível · b = voltar · t x,y = toque coordenado · "
                       "r = capturar novamente · q = encerrar")
            choice = safe_input("Inspector > ").strip().lower()
            if choice in ("q", "0"):
                break
            if choice == "r":
                snapshot = _capture_monitored(session, "captura solicitada pelo usuário")
                continue
            if choice == "b":
                action, label = {"type": "back"}, "Voltar (ação global Android)"
            elif choice.startswith("t "):
                try:
                    x, y = (float(value.strip()) for value in choice[2:].split(",", 1))
                except ValueError:
                    print_line("Formato inválido. Exemplo: t 210,640")
                    continue
                action, label = {"type": "tap", "x": x, "y": y}, f"Toque em ({x:g}, {y:g})"
            elif choice.isdigit():
                node = next((item for item in candidates if int(item.get("index", -1)) == int(choice)), None)
                if node is None:
                    print_line("Índice não clicável ou fora da tela atual.")
                    continue
                value = (node.get("text") or node.get("content_description") or "")[:120]
                action, label = {"type": "click", "index": int(choice)}, f"Clique #{choice}: {value}"
            else:
                print_line("Ação não reconhecida.")
                continue
            try:
                snapshot = _action_monitored(session, action, label, snapshot)
            except Exception as exc:
                print_line(f"Ação/captura falhou: {exc}")
        report = session.finish("finished")
        print_line("Relatório final:", report)
        print_line("Telas únicas:", len({item['fingerprint'] for item in session.screens}),
                   "· capturas:", len(session.screens), "· transições:", len(session.transitions))
    except KeyboardInterrupt:
        report = session.finish("interrupted")
        print_line("Inspeção interrompida; registros preservados:", report)


def _launch_and_capture(client: InspectorClient, package: str, reports_dir: Path, emit: Callable) -> dict:
    session = InspectionSession(reports_dir, client, package)
    emit(f"Solicitando abertura do pacote escolhido: {package}", stage="Abrindo app de referência")
    result = client.request("/action", method="POST", data={"type": "launch", "package": package})
    if not result.get("ok"):
        raise RuntimeError("O auxiliar não abriu o app selecionado.")
    time.sleep(1.0)
    snapshot = session.capture(emit, "tela inicial após abertura explícita")
    return {"session": session, "snapshot": snapshot}


def _capture_monitored(session: InspectionSession, reason: str) -> dict:
    result, _ = _run_monitored("Capturar tela atual", lambda emit: session.capture(emit, reason),
                               session.root.parents[1], summary=lambda value: {"ok": True})
    return result


def _action_monitored(session: InspectionSession, action: dict, label: str, before: dict) -> dict:
    result, _ = _run_monitored(
        "Executar interação e registrar transição",
        lambda emit: session.perform_and_capture(action, label, before, emit),
        session.root.parents[1], summary=lambda value: {"ok": True,
                                                        "screen": value.get("screen_fingerprint")},
    )
    return result

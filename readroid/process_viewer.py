"""Acompanhamento persistente de operações longas do ReaDroid no terminal."""
from __future__ import annotations

import json
import os
import re
import threading
import time
import uuid
from datetime import datetime
from pathlib import Path
from typing import Callable

_CONTROL = re.compile(r"\x1b\[[0-9;]*[a-zA-Z]|[\x00-\x08\x0b\x0c\x0e-\x1f\x7f]")


class ProcessRun:
    """Registra eventos em disco enquanto uma operação roda em uma thread."""

    def __init__(self, reports_dir: Path, title: str):
        self.root = reports_dir / "process_runs"
        self.root.mkdir(parents=True, exist_ok=True)
        self.run_id = datetime.now().strftime("%Y%m%d_%H%M%S") + "_" + uuid.uuid4().hex[:6]
        self.log_path = self.root / f"{self.run_id}.jsonl"
        self.state_path = self.root / f"{self.run_id}.json"
        self.started_at = datetime.now().astimezone()
        self.lock = threading.RLock()
        self.state = {
            "id": self.run_id, "title": title, "status": "aguardando",
            "stage": "Aguardando início", "percent": None, "started_at": self.started_at.isoformat(),
            "elapsed_seconds": 0, "events": [], "result": None,
        }
        self._save()

    def elapsed_seconds(self) -> float:
        """Wall elapsed time advances even while Android suspends monotonic CPU time."""
        return max(0.0, (datetime.now().astimezone() - self.started_at).total_seconds())

    def _save(self) -> None:
        self.state["elapsed_seconds"] = round(self.elapsed_seconds(), 1)
        temp = self.state_path.with_suffix(".tmp")
        temp.write_text(json.dumps(self.state, ensure_ascii=False, indent=2), encoding="utf-8")
        os.replace(temp, self.state_path)

    def emit(self, message: str, kind: str = "info", stage: str | None = None) -> None:
        event = {"time": datetime.now().astimezone().isoformat(timespec="seconds"),
                 "kind": kind, "message": _CONTROL.sub("", str(message))}
        with self.lock:
            if stage:
                self.state["stage"] = stage
            if self.state["status"] == "aguardando":
                self.state["status"] = "executando"
            self.state["events"].append(event)
            with self.log_path.open("a", encoding="utf-8") as log:
                log.write(json.dumps(event, ensure_ascii=False) + "\n")
            self._save()

    def finish(self, status: str, result: dict | None = None, message: str | None = None) -> None:
        with self.lock:
            self.state["status"] = status
            self.state["result"] = result
            if message:
                kind = "success" if status == "concluído" else "warning" if status == "concluído com ressalvas" else "error"
                self.emit(message, kind, message)
            self._save()


def run_with_viewer(reports_dir: Path, title: str, operation: Callable[[Callable], dict],
                    summary: Callable[[dict], dict] | None = None) -> tuple[dict | None, ProcessRun]:
    """Executa sem bloquear a atualização do painel; percentual fica indeterminado."""
    run = ProcessRun(reports_dir, title)
    outcome: dict = {}
    failure: list[BaseException] = []

    def worker() -> None:
        try:
            run.emit("Execução iniciada.", stage=title)
            result = operation(run.emit)
            outcome["result"] = result
            compact = summary(result) if summary else result
            status = compact.get("monitor_status")
            if status is None:
                succeeded = bool(result.get("ok", result.get("status") not in ("error", "incomplete", "reconstruction_unavailable")))
                status = "concluído" if succeeded else "com erro"
            message = ("Processo concluído e validação confirmada." if status == "concluído"
                       else "Processo terminou sem validação completa." if status == "concluído com ressalvas"
                       else "Processo terminou com erro ou pendências.")
            run.finish(status, compact, message)
        except BaseException as exc:
            failure.append(exc)
            run.finish("com erro", {"error": type(exc).__name__}, f"Falha: {type(exc).__name__}: {exc}")

    thread = threading.Thread(target=worker, name="readroid-process", daemon=True)
    thread.start()
    try:
        _render(run)
        while thread.is_alive():
            time.sleep(1)
            _render(run)
        thread.join()
        _render(run)
    except KeyboardInterrupt:
        run.finish("interrompido", message="Acompanhamento interrompido pelo usuário; registro preservado.")
        print("\nA execução pode continuar em segundo plano. Registro:", run.log_path)
    if failure:
        raise failure[0]
    return outcome.get("result"), run


def _render(run: ProcessRun) -> None:
    with run.lock:
        state = json.loads(json.dumps(run.state))
    state["elapsed_seconds"] = round(run.elapsed_seconds(), 1)
    if os.environ.get("TERM"):
        print("\033[H\033[2J", end="")
    elapsed = int(state["elapsed_seconds"])
    print("╭─ ReaDroid · Acompanhamento do processo ─────────────────╮")
    print(f"│ {state['title'][:54]}")
    print(f"│ Estado: {state['status']}  ·  Etapa: {state['stage'][:44]}")
    print(f"│ Progresso: percentual indisponível  ·  Tempo: {elapsed // 60:02}:{elapsed % 60:02}")
    print("├─ Registro ──────────────────────────────────────────────┤")
    for event in state["events"][-9:]:
        marker = {"warning": "!", "error": "×", "success": "✓", "info": "·"}.get(event["kind"], "·")
        print(f"│ {event['time'][11:19]} {marker} {event['message'][:47]}")
    print("╰─────────────────────────────────────────────────────────╯", flush=True)

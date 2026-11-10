import json
import tempfile
import unittest
from datetime import timedelta
from pathlib import Path
from unittest.mock import patch

from readroid.process_viewer import ProcessRun, _render, run_with_viewer


class ProcessViewerTests(unittest.TestCase):
    def test_events_are_appended_and_snapshot_is_preserved(self):
        with tempfile.TemporaryDirectory() as temporary:
            run = ProcessRun(Path(temporary), "build demo")
            run.emit("Compilando", stage="compile")
            run.emit("aviso de demonstração", kind="warning")
            run.finish("concluído com ressalvas", {"apk": None}, "APK não gerado")

            events = [json.loads(line) for line in run.log_path.read_text(encoding="utf-8").splitlines()]
            state = json.loads(run.state_path.read_text(encoding="utf-8"))
            self.assertEqual(["Compilando", "aviso de demonstração", "APK não gerado"],
                             [event["message"] for event in events])
            self.assertEqual("warning", events[1]["kind"])
            self.assertEqual("concluído com ressalvas", state["status"])
            self.assertEqual({"apk": None}, state["result"])

    def test_async_operation_reports_real_events_and_success(self):
        with tempfile.TemporaryDirectory() as temporary, patch("readroid.process_viewer._render") as render:
            def operation(emit):
                emit("Testes iniciados", stage="testes")
                emit("3 testes passaram", kind="success", stage="testes concluídos")
                return {"ok": True, "tests": 3}

            result, run = run_with_viewer(Path(temporary), "testes", operation)
            self.assertEqual({"ok": True, "tests": 3}, result)
            self.assertEqual("concluído", run.state["status"])
            self.assertTrue(render.call_count >= 1)
            persisted = json.loads(run.state_path.read_text(encoding="utf-8"))
            self.assertEqual("concluído", persisted["status"])
            self.assertIn("3 testes passaram", run.log_path.read_text(encoding="utf-8"))

    def test_failed_operation_is_not_marked_as_success(self):
        with tempfile.TemporaryDirectory() as temporary, patch("readroid.process_viewer._render"):
            result, run = run_with_viewer(
                Path(temporary), "falha", lambda emit: {"ok": False, "stage": "assinatura"})
            self.assertFalse(result["ok"])
            self.assertEqual("com erro", run.state["status"])

    def test_dashboard_clock_advances_without_new_process_events(self):
        with tempfile.TemporaryDirectory() as temporary, patch("builtins.print") as output:
            run = ProcessRun(Path(temporary), "clock")
            run.started_at -= timedelta(seconds=71)
            _render(run)
            rendered = " ".join(str(call.args[0]) for call in output.call_args_list if call.args)
            self.assertIn("Tempo: 01:11", rendered)


if __name__ == "__main__":
    unittest.main()

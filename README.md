# FlowOS

**"Your phone understands the work."**

FlowOS is an **AI Work Operating System** that understands your work context, predicts what needs attention, builds the next-best workflow, and helps you execute it from your phone. It is not a chatbot and not another to-do list. The product loop is:

```
CAPTURE → UNDERSTAND → CONNECT → PREDICT → PLAN → CONFIRM → ACT → REMEMBER
```

## The problem

Useful information arrives as messages, screenshots, notices and voice notes. It contains tasks, deadlines, people and priorities — but converting it into action is manual work. FlowOS understands that information on the device and turns it into an ordered, confirmable workflow with real Android actions behind it.

## Features

- **Capture anywhere**: camera (TakePicture + permission flow), gallery, voice (Android SpeechRecognizer with typed fallback), documents (PDF via render+OCR, TXT direct, images via OCR), and plain typing.
- **One understanding pipeline**: every input type produces the same structured `AIAnalysisResult` (summary, intent, priority, people, deadlines, tasks, dependencies, project, confidence). Application logic never reads free-form AI text.
- **Context Engine**: captures attach to the active project and remember people, so tasks are never stored as isolated rows.
- **Workflow Planner**: topological ordering with deadline/priority tie-breaking, shown as a vertical dependency timeline.
- **FlowPulse (AI Work OS layer)**: a deterministic `WorkStateEngine` picks the most relevant active work thread (deadline proximity → dependency blocking → priority → open work → project relevance), and a `NextBestActionEngine` explains *why* that action matters ("It unblocks \"Send updated document\"\."). Rendered on Home as the AI Pulse dashboard.
- **Calendar intelligence**: with explicit in-context permission, events are read locally and matched to projects by title and deadline proximity; `PREPARE FOR EVENT` builds an Action Bundle — a confirmation-gated group of safe actions (task creation, reminders, share sheet). No silent writes, ever.
- **Focus Mode**: one task at a time from the dependency chain with OPEN / COMPLETE / SKIP; completion flows back into WorkState in real time.
- **Navigation**: HOME (AI overview) · PLAN (timeline + preparation) · FLOW (dependency graph) · CONTEXT (knowledge graph) · ACTIVITY (history).
- **Action Engine**: honest execution — local task persistence, AlarmManager reminder notifications, calendar insert intents, system share sheet, file open. Nothing reports success that didn't happen.
- **Persistence**: Room database (tasks, projects, people, captures, workflows, workflow steps). Everything survives restart.
- **Offline-first**: no network calls anywhere in the pipeline; no accounts, no cloud.
- **Demo mode**: realistic iQOO Hackathon data on first launch, resettable from Settings.

## Architecture

```
UI (Compose screens, Material 3)
  ↓
ViewModels (per screen, AndroidViewModel + StateFlow)
  ↓
FlowOSRepository (single data gateway)
  ├── Room database (entities/DAOs)
  ├── ContextEngine   (connects captures → projects/people)
  ├── WorkflowPlanner (orders tasks into a plan)
  ├── ActionEngine    (real Android intents + notifications)
  └── FlowPulse       (WorkStateEngine → NextBestActionEngine → CalendarIntelligenceEngine)
        ↑
AIEngine abstraction
  ├── MockAIEngine      (deterministic demo engine)
  └── LocalAIEngine     (offline rule-based understanding)
        ↑
DeviceInferenceEngine abstraction (Mock / reserved Local impl)
```

Packages: `domain.model`, `data.local`, `data.repository`, `data.demo`, `ai`, `calendar`, `pulse`, `context`, `workflow`, `action`, `capture`, `notifications`, `crossdevice`, `settings`, `di`, `ui` (screens/components/theme).

## AI architecture & honesty policy

- `AIEngine` is the single contract; engines are swappable in `AppContainer` without UI changes.
- `MockAIEngine` returns the spec's deterministic demo output and its processing label is **"Demo AI processing"**.
- `LocalAIEngine` runs deterministic heuristics fully offline; its label is **"Processing on device"**.
- `DeviceInferenceEngine` reserves the slot for a Qualcomm/Snapdragon-compatible runtime. The current implementations report `isAvailable() = false` and accelerate `NONE`/`CPU` — **no NPU claims are made** until a real runtime is wired. When it is, model/runtime/accelerator/latency belong on a debug screen only.

## Local-first design

Captures, tasks, workflows and settings never leave the phone. The only outbound intents are user-visible Android share/calendar intents. "Clear all local data" wipes every table and restores fresh demo data.

## Database

Room v3 with `TaskEntity`, `ProjectEntity`, `PersonEntity`, `CaptureEntity`, `WorkflowEntity`, `WorkflowStepEntity`, `ActivityEventEntity`, `TaskDependencyEntity` (persisted dependency edges — the backbone of FlowPulse, the Flow graph and Focus Mode). DAOs expose reactive `Flow`s for lists/counts and suspend functions for writes; a grouped wipe backs the privacy setting. WorkState itself is derived on the fly — only real facts are persisted.

## Android integrations

- **Camera**: `TakePicture` contract into a FileProvider-backed cache file (no storage permission needed), then on-device OCR (ML Kit, bundled model).
- **Microphone**: `RECORD_AUDIO` requested at tap-time; `SpeechRecognizer` availability probed with a typed fallback.
- **Notifications**: dedicated channel + AlarmManager reminders (exact where permitted, inexact otherwise, stated honestly).
- **Calendar/share**: standard system intents; the user always confirms in the target app. `READ_CALENDAR` is requested contextually from the Plan screen only — never at startup — and event reads happen on demand, not by polling.
- **Cross-device**: `CrossDeviceManager` with `sendFile/receiveFile/shareText/syncTask` — works today via share sheet + FileProvider, with a marked `OfficeKitBridge` slot for the hackathon environment.

## Building

Requirements: JDK 17, Android Studio (Koala or newer) or Gradle 8.9, Android SDK 34.

```bash
# open in Android Studio and press Run, or:
./gradlew :app:assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

No API keys or secrets are required — the project has none.

## Demo workflow (under 3 minutes)

1. Launch → Home shows the FlowPulse hero: "Architecture Review · N% ready", the next best action with its dependency reason, Quick Capture and Today.
2. **QUICK CAPTURE → TEXT** (or VOICE/DOCUMENT) and enter:
   *"Project review tomorrow at 10 AM. Finish the architecture diagram and send it to Prakash tonight."*
3. Processing animates through the pipeline with an honest mode label.
4. **AI UNDERSTANDING** shows the summary, intent prediction, actions, people, deadlines, project, confidence.
5. **BUILD WORKFLOW** → vertical timeline; confirming it persists the dependency chain (Room).
6. **EXECUTE WORKFLOW** → task persisted, reminders scheduled, event drafted, share sheet opened — each reported truthfully.
7. Home / Flow / Context now reflect the updated thread; restart the app and everything is still there (Room).
8. PLAN tab → CONNECT CALENDAR (in-context permission) → events appear with readiness; **PREPARE FOR EVENT** opens an Action Bundle → **APPROVE & EXECUTE** runs it with honest per-item results.
9. **START FOCUS** walks the dependency chain one step at a time; completions update FlowPulse live.
10. Settings → AI Mode switch and **Clear all local data** (resets demo).

## Known limits (by design, not faked)

- No on-device LLM/NPU runtime is bundled yet; Local AI is rule-based and the device-inference abstraction reports honestly unavailable.
- PDF text extraction uses render+OCR, so scanned quality affects results.
- Voice needs a speech service on the device; emulators fall back to typing.

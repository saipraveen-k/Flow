# FlowOS Manual Update Guide

This document identifies manual steps required for full integration of the Adaptive Personal Work OS.

## 1. Android Permissions
The following permissions must be granted at runtime (handled contextually by FlowOS):
- `READ_CALENDAR`: Required for PLAN screen and capacity intelligence.
- `RECORD_AUDIO`: Required for VOICE capture.
- `POST_NOTIFICATIONS`: Required for task reminders.

## 2. Calendar Setup
- Ensure the device/emulator has a system calendar provider.
- For testing, add events to the primary calendar with titles matching project names (e.g., "Architecture Review").

## 3. Local AI & Voice
- FlowOS uses a rule-based `LocalAIEngine`. No manual model download is required for the MVP.
- Voice capture requires the **Google Speech Services** app to be installed and updated on the device.

## 4. Office Kit Pairing
- Office Kit is implemented via `CrossDeviceManager`.
- Currently, it utilizes the system share sheet and FileProvider.
- For a real laptop handoff, ensure a target app (like a file receiver or browser) is available to handle the `SEND` intent.

## 5. Build & Verification
- Clean build: `./gradlew clean`
- Assemble: `./gradlew assembleDebug`
- Test: `./gradlew test`

## 6. Known Limitations
- **PDF Extraction**: Uses on-device render + OCR; results vary based on scan quality.
- **NPU Acceleration**: Currently not verified. AI runs on CPU/GPU via deterministic heuristics.

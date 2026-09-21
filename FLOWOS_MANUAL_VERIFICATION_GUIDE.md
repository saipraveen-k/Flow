# FLOWOS — Manual Device Verification Guide (40 Standard Scenarios)

This guide provides step-by-step physical device testing procedures for verifying FlowOS system APIs, domain engines, cross-device handoff, and UI state handling.

---

## Pre-requisites & Test Setup
- **Target Device**: Physical Android device (Android 8.0 / SDK 26+ up to Android 15 / SDK 35).
- **Optional Companion Device**: Windows PC running FlowOS Companion or connected via local Wi-Fi.
- **Dependencies Installed**: Health Connect App (Android 13 and below) or System Health Connect (Android 14+).

---

## Test Execution Matrix

### 1. Installation & Cold Launch
- **Precondition**: Clean state, no prior app data.
- **Steps**:
  1. Install `app-debug.apk` via `adb install -r app-debug.apk`.
  2. Launch FlowOS from home launcher.
- **Expected Result**: App launches instantly, renders animated splash logo, transitions cleanly to Onboarding/Home.
- **Pass Criteria**: Zero crashes, fluid animation, edge-to-edge system insets respected.
- **Fail Symptoms**: Crash on start, blank screen, white flash.
- **Recovery**: Clear app data (`adb shell pm clear com.flowos.app`).

### 2. Onboarding Completion
- **Precondition**: First launch after clean install.
- **Steps**: Tap through onboarding pages and tap "Get Started".
- **Expected Result**: Preference `onboarding_completed` is set to `true`, navigates directly to `HomeScreen`.

### 3. Light Theme Verification
- **Precondition**: System theme set to Light or FlowOS Theme setting set to `LIGHT`.
- **Steps**: Open FlowOS -> Settings -> Theme -> Select `LIGHT`.
- **Expected Result**: Background turns to `#F8FAFC`, surface cards turn white `#FFFFFF`, text turns `#0F172A`. Zero black-on-dark text or white-on-white text.

### 4. Dark Theme Verification
- **Precondition**: System theme set to Dark or FlowOS Theme setting set to `DARK`.
- **Steps**: Select `DARK` in Settings.
- **Expected Result**: Background `#0D1117`, surface `#161B22`, text `#F0F6FC`.

### 5. System Default Theme Dynamic Switching
- **Precondition**: FlowOS Theme set to `SYSTEM DEFAULT`.
- **Steps**: Toggle system Dark Theme on/off while FlowOS is open in split screen or background.
- **Expected Result**: FlowOS dynamically adapts colors without activity restart or visual artifacts.

### 6. Calendar Provider Permission Request
- **Precondition**: Calendar permission not yet granted.
- **Steps**: Navigate to `PlanScreen` -> Tap "Connect Device Calendar".
- **Expected Result**: System permission dialog for `READ_CALENDAR` appears.

### 7. Real Device Calendar Event Ingestion
- **Precondition**: `READ_CALENDAR` granted, real events exist in Google/Device Calendar.
- **Steps**: Refresh `PlanScreen`.
- **Expected Result**: Real calendar events render on timeline with title, start/end time, and location.

### 8. Calendar + Outcome Critical Path Unification
- **Precondition**: Calendar events ingested.
- **Steps**: Open `HomeScreen` / `FlowPulse`.
- **Expected Result**: Available focus time is automatically calculated by deducting calendar event durations from 24h capacity.

### 9. Health Connect Permission Rationale
- **Precondition**: Health Connect installed.
- **Steps**: Open Health Connect permission flow in FlowOS.
- **Expected Result**: `HealthConnectRationaleActivity` displays purpose explanation: step count, workout sessions, active calories, and distance.

### 10. Real Health Connect Step Count Aggregation
- **Precondition**: Health Connect permission granted with real step records.
- **Steps**: View Fitness section on Home / Context screen.
- **Expected Result**: Step count shows aggregated total from Health Connect API (not naive sum of overlapping records).

### 11. Health Connect Workout Session Reading
- **Precondition**: Workout recorded in Health Connect.
- **Steps**: Trigger fitness sync.
- **Expected Result**: Workout session title, duration, and calories display under Fitness context.

### 12. Fitness Context -> Protected Workout Time
- **Precondition**: Fitness session recorded.
- **Steps**: Open `Planner` / `FlowPulse`.
- **Expected Result**: Workout window is treated as `PROTECTED FITNESS TIME` and deducted from available task capacity.

### 13. Preplanned Routine Template Application (College Day)
- **Precondition**: Routine Engine initialized.
- **Steps**: Select `COLLEGE DAY` routine template.
- **Expected Result**: Blocks generated (Wake 05:00, Travel 06:40, College 09:00, Fitness 19:00, Study 20:00, Sleep 23:00) without creating duplicate daily database tasks.

### 14. Routine vs Calendar Conflict Detection
- **Precondition**: College Day routine active, calendar event scheduled at 10:00 AM.
- **Steps**: Inspect `FrictionRadar`.
- **Expected Result**: `FrictionRadar` flags `ROUTINE_CONFLICT` between College block and Calendar event with action buttons.

### 15. Camera CV / Image Capture
- **Precondition**: Camera permission granted.
- **Steps**: Open `CaptureScreen` -> Select Camera -> Photograph document/notice.
- **Expected Result**: ML Kit OCR extracts text cleanly, displays preview.

### 16. Structured Document Date & Deadline Extraction
- **Precondition**: Photograph containing "Submission deadline: Oct 15, 11:59 PM".
- **Steps**: Run DocumentExtractor.
- **Expected Result**: Extracted deadline `Oct 15, 11:59 PM` isolated with high confidence.

### 17. Flow Snap Quick Settings Tile Service
- **Precondition**: `Flow Snap` tile added to Quick Settings panel.
- **Steps**: Pull down Quick Settings shade -> Tap `Flow Snap`.
- **Expected Result**: FlowOS opens directly into Flow Snap capture review screen.

### 18. Storage Access Framework (SAF) Document Picker
- **Precondition**: Files exist in device Storage.
- **Steps**: Open `Flow Space` -> Tap "Add File".
- **Expected Result**: Native Android document picker opens (`ACTION_OPEN_DOCUMENT`).

### 19. Persistable File URI Permission Retention
- **Precondition**: Document picked via SAF.
- **Steps**: Restart FlowOS app -> Re-open `Flow Space`.
- **Expected Result**: Document preview loads without `SecurityException` due to `takePersistableUriPermission`.

### 20. Office Kit Companion Detection
- **Precondition**: Office Kit API available on device (vivo/iQOO) or local Wi-Fi active.
- **Steps**: Open `OfficeKitScreen`.
- **Expected Result**: Connection state correctly reports `SUPPORTED`, `CONNECTED`, or fallback `DISCONNECTED` (never hardcodes fake connection).

### 21. Phone -> PC Secure Network File Handoff
- **Precondition**: PC Companion app active on local network.
- **Steps**: Select file -> Tap "Send to PC".
- **Expected Result**: Progress bar displays transfer percentage, completes with SHA-256 checksum verification.

### 22. PC -> Phone File Receive
- **Precondition**: PC sends file to Phone.
- **Steps**: Incoming transfer prompt appears.
- **Expected Result**: Prompts user confirmation, saves file to local Flow Space directory upon approval.

### 23. FLOW BRIDGE AppWidget Glance Render
- **Precondition**: AppWidget added to Android Home Screen.
- **Steps**: Inspect `FLOW BRIDGE` widget.
- **Expected Result**: Reads `RealCrossDeviceConnectionState`, displays `● PC CONNECTED` or `PC NOT CONNECTED` dynamically.

### 24. Task Handoff ("Desktop Work Required")
- **Precondition**: Task marked with PC/laptop deliverable.
- **Steps**: Tap "Continue on PC".
- **Expected Result**: Task state transitions to `WAITING FOR PC`.

### 25. Outcome Completion via Evidence Upload
- **Precondition**: Task in `WAITING FOR PC`.
- **Steps**: Attach return file / screenshot proof.
- **Expected Result**: Verification state updates from `WAITING FOR PC` -> `VERIFIED` -> Outcome completed.

### 26. Screen Mirroring Entry Point
- **Precondition**: Supported OEM environment.
- **Steps**: Tap "Mirror Phone".
- **Expected Result**: Invokes system screen mirroring intent if available; otherwise displays clear fallback message.

### 27. Friction Radar Actionable Alerts
- **Precondition**: Friction detected (e.g. PC Disconnected).
- **Steps**: View Friction card on HomeScreen.
- **Expected Result**: Displays exact reason and 3 actionable buttons ("Connect PC", "Work on Phone", "Replan").

### 28. Adaptive Replanning Proposal Review
- **Precondition**: Tap "Replan" on Friction alert.
- **Steps**: Review `ReplanProposal`.
- **Expected Result**: Shows "Before" vs "After" timeline comparison with "Accept" and "Reject" choices.

### 29. Multi-Dimensional FlowScore Calculation
- **Precondition**: Tasks completed, fitness synced, focus session logged.
- **Steps**: Open `HomeScreen` -> View `FlowScore`.
- **Expected Result**: Displays total score (0-100), breakdown of 6 dimensions (Outcome Progress, Focus Efficiency, Plan Reliability, Time Utilization, Friction Handling, Recovery), and human explanation insight.

### 30. Process Death & State Restoration
- **Precondition**: FlowOS in background with active task in Focus mode.
- **Steps**: Kill process via `adb shell am kill com.flowos.app` -> Re-open app.
- **Expected Result**: Room database restores persisted task states, active outcome, and FlowScore without data loss.

### 31. Permission Denial Graceful Handling (Calendar)
- **Precondition**: Calendar permission denied by user.
- **Steps**: Open `PlanScreen`.
- **Expected Result**: Displays "Calendar access not granted. FlowOS plan operates in offline capacity mode." Zero crashes.

### 32. Health Connect Unavailable Handling
- **Precondition**: Device without Health Connect installed.
- **Steps**: Open Fitness view.
- **Expected Result**: Displays "Health Connect is unavailable on this device." with button to install from Play Store.

### 33. Empty Fitness Data State
- **Precondition**: Health Connect granted but 0 records logged for today.
- **Steps**: View Fitness metrics.
- **Expected Result**: Displays "No fitness data available yet." (never displays fake 0 steps).

### 34. Malformed Document OCR Failure Handling
- **Precondition**: Photograph blurry/blank image.
- **Steps**: Run OCR.
- **Expected Result**: Displays user-friendly message "FlowOS couldn't read text clearly. Try rephrasing or a clearer image."

### 35. Expired File URI Recovery
- **Precondition**: Picked SAF file deleted externally from device storage.
- **Steps**: Tap file preview in Flow Space.
- **Expected Result**: Catches `FileNotFoundException`, displays "File no longer accessible" with option to re-bind.

### 36. Disconnected PC Transfer Retry
- **Precondition**: Initiate file transfer while PC Wi-Fi disconnected.
- **Steps**: Tap "Send to PC".
- **Expected Result**: Transitions state to `FAILED`, shows "Retry" and "Local Fallback" options.

### 37. Routine Recurrence Without Task Duplication
- **Precondition**: College Day routine active across multiple days.
- **Steps**: Advance system date by 1 day.
- **Expected Result**: Routine blocks apply to new date using stable occurrence IDs without spawning duplicate task database rows.

### 38. Database Version 5 Migration Verification
- **Precondition**: Database version upgraded from 4 to 5.
- **Steps**: Launch updated app build.
- **Expected Result**: Room DB initializes tables `routines`, `routine_blocks`, `routine_occurrences` smoothly without wiping existing tasks or projects.

### 39. Rapid Edge-to-Edge Navigation Stress Test
- **Precondition**: FlowOS running.
- **Steps**: Rapidly tap bottom navigation tabs (Home -> Outcomes -> Calendar -> Space -> Home).
- **Expected Result**: Smooth Compose transitions without visual clipping, memory leaks, or duplicate ViewModel instantiation.

### 40. Full End-to-End Integration Flow
- **Precondition**: All system APIs connected.
- **Steps**:
  1. Capture document via Camera OCR / Flow Snap.
  2. Extracted deadline confirmed -> Calendar event & Outcome generated.
  3. Work Graph prioritizes outcome -> FlowPulse recommends Next Best Action.
  4. Desktop work detected -> Handoff to PC -> WAITING FOR PC.
  5. Returned evidence uploaded -> Outcome VERIFIED -> FlowScore updated.
- **Expected Result**: Entire FlowOS capture-to-outcome pipeline executes seamlessly as one unified system.

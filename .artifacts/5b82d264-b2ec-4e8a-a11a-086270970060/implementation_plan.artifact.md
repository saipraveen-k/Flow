# FlowOS Master Build — Adaptive Personal Work OS

This plan transforms FlowOS into the locked Adaptive Personal Work OS by implementing a coherent intelligence loop: Capture → Understand → Outcome Compiler → Work Graph → FlowPulse → Focus → Friction Radar → Adaptive Replanning → Outcome Proof → FlowScore.

## User Review Required

> [!IMPORTANT]
> - **Hierarchy Shift**: We are moving from a "Task List" to an "Outcome OS". `Goal` and `Outcome` are now the primary drivers.
> - **Friction & Adaptation**: These are the core differentiators. FlowOS will detect deviations and propose (not force) schedule changes.
> - **Unified Context**: Professional, Personal, Learning, and Fitness are now context signals feeding the unified planner.
> - **Outcome Proof**: Real-world evidence (files, confirmation) is required to mark an outcome as `VERIFIED`.

## Proposed Changes

### Phase 1: Data Architecture & Room Migrations [COMPLETED]
- Introduced `Goal`, `Outcome`, `Evidence`, `FlowScore`, `LifeHub`, `Strategy`.
- Updated `ExtractedTask` with durations and status.
- Migrated Room to Version 4.

### Phase 2: Intelligence Engines [COMPLETED]
- Implemented `OutcomeCompiler.kt` for structured extraction.
- Expanded `WorkStateEngine` scoring with Risk and Capacity.
- Implemented `AdaptivePlanner.kt` for time-aware scheduling.
- Implemented `FrictionRadar.kt` for deviation detection.
- Implemented `AdaptiveReplanner.kt` for before/after adaptation proposals.

### Phase 3: Premium UI & Core Workflows [COMPLETED]
- Redesigned `HomeScreen.kt` as an Attention Center.
- Overhauled `FocusScreen.kt` with pulsing timer and genuine proof collection.
- Implemented `OutcomeListScreen` and `OutcomeDetailScreen` (merged Flow/Graph).
- Refactored `ContextScreen.kt` as the Life Context Hub.
- Implemented `ReplanningScreen.kt` for adaptive transitions.

### Phase 4: Strategy, Memory & Documentation [COMPLETED]
- Implemented `FlowScoreEngine.kt` for explainable scoring.
- Implemented `StrategyEngine.kt` with predefined productivity patterns.
- Created `FlowMemoryRepository.kt` for work-context memory.
- Final hardening: Connected real Android launchers for Proof and Capture (OCR/PDF).

## Verification Plan

### Automated Tests [PASSED]
- `AdaptivePlannerTest`: Capacity-aware scheduling verified.
- `FrictionRadarTest`: Overrun detection logic verified.
- `OutcomeCompilerTest`: Structured extraction verified.
- 35 total pass.

### Final Hardening Results
- **Outcome Proof**: Genuine file/image picker integrated.
- **Capture**: CAMERA and DOCUMENT buttons wired to ML Kit OCR and PDF extraction.
- **Adaptive Replanning**: Fully data-driven BEFORE/AFTER comparison implemented.
- **Honesty Audit**: Claims reflect rule-based on-device heuristics (no fake NPU claims).

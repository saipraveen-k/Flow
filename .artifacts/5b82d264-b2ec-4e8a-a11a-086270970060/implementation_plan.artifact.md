# FlowOS Master Build — Adaptive Personal Work OS

This plan transforms FlowOS into the locked Adaptive Personal Work OS by implementing a coherent intelligence loop: Capture → Understand → Outcome Compiler → Work Graph → FlowPulse → Focus → Friction Radar → Adaptive Replanning → Outcome Proof → FlowScore.

## User Review Required

> [!IMPORTANT]
> - **Hierarchy Shift**: We are moving from a "Task List" to an "Outcome OS". `Goal` and `Outcome` are now the primary drivers.
> - **Friction & Adaptation**: These are the core differentiators. FlowOS will detect deviations and propose (not force) schedule changes.
> - **Unified Context**: Professional, Personal, Learning, and Fitness are now context signals feeding the unified planner.
> - **Outcome Proof**: Real-world evidence (files, confirmation) is required to mark an outcome as `VERIFIED`.

## Proposed Changes

### Phase 1: Data Architecture & Room Migrations
#### [MODIFY] [Models.kt](file:///C:/tempp/projects/Flow/app/src/main/java/com/flowos/app/domain/model/Models.kt)
- Introduce `Goal`, `Outcome`, `Evidence`, `FlowScore`, `LifeHub`, `Strategy`.
- Update `ExtractedTask` with `estimatedDurationMinutes`, `actualDurationMinutes`, and `outcomeId`.

#### [MODIFY] [Entities.kt](file:///C:/tempp/projects/Flow/app/src/main/java/com/flowos/app/data/local/Entities.kt)
- Add `GoalEntity`, `OutcomeEntity`, `EvidenceEntity`, `FlowScoreEntity`.
- Update `TaskEntity` to track durations and status timestamps.
- **Action**: Implement Room Version 3 migration.

---

### Phase 2: Intelligence Engines (The Locked Loop)

#### [NEW] [OutcomeCompiler.kt](file:///C:/tempp/projects/Flow/app/src/main/java/com/flowos/app/workflow/OutcomeCompiler.kt)
- Compiles messy AI results into structured `Outcomes` with goal-alignment and dependency validation.

#### [MODIFY] [FlowPulse Engine](file:///C:/tempp/projects/Flow/app/src/main/java/com/flowos/app/pulse/WorkStateEngine.kt)
- Expand `WorkStateEngine` scoring: `0.30 * Urgency + 0.25 * Blocking + 0.15 * Capacity + 0.15 * Priority + 0.15 * Risk`.

#### [NEW] [AdaptivePlanner.kt](file:///C:/tempp/projects/Flow/app/src/main/java/com/flowos/app/planner/AdaptivePlanner.kt)
- Time-aware scheduler: merges calendar availability, task durations, and critical path into a realistic timeline.

#### [NEW] [FrictionRadar.kt](file:///C:/tempp/projects/Flow/app/src/main/java/com/flowos/app/pulse/FrictionRadar.kt)
- **Detection**: Monitors overrun, collisions, capacity shortages, and deadline risk in real-time.

#### [NEW] [AdaptiveReplanner.kt](file:///C:/tempp/projects/Flow/app/src/main/java/com/flowos/app/planner/AdaptiveReplanner.kt)
- **Correction**: Generates BEFORE/AFTER plan comparisons (Explain → Review → Accept).

---

### Phase 3: Premium UI & Core Workflows

#### [MODIFY] [HomeScreen.kt](file:///C:/tempp/projects/Flow/app/src/main/java/com/flowos/app/ui/screens/HomeScreen.kt)
- Dashboard overhaul: "What deserves attention now?" (FlowScore, NBA, Current Outcome, Friction Alerts).

#### [MODIFY] [FocusScreen.kt](file:///C:/tempp/projects/Flow/app/src/main/java/com/flowos/app/ui/screens/FocusScreen.kt)
- Add active timer, duration tracking, and "Outcome Proof" collection UI.

#### [MODIFY] [ContextScreen.kt](file:///C:/tempp/projects/Flow/app/src/main/java/com/flowos/app/ui/screens/ContextScreen.kt)
- Refactor as the unified **Life Context Hub** (source signals for the planner).

---

### Phase 4: Strategy, Memory & Documentation

#### [NEW] [FlowScoreEngine.kt](file:///C:/tempp/projects/Flow/app/src/main/java/com/flowos/app/scoring/FlowScoreEngine.kt)
- Multi-dimensional explainable score.

#### [NEW] [StrategyEngine.kt](file:///C:/tempp/projects/Flow/app/src/main/java/com/flowos/app/workflow/StrategyEngine.kt)
- Predefined (Deep Work, Exam Sprint) and Custom Strategy builder.

#### [NEW] [FLOWOS_MANUAL_UPDATE_GUIDE.md](file:///C:/tempp/projects/Flow/FLOWOS_MANUAL_UPDATE_GUIDE.md)
- **Mandatory Guide**: Step-by-step for manual human configuration (API keys, permissions, models).

#### [NEW] [FLOWOS_IMPLEMENTATION_STATUS.md](file:///C:/tempp/projects/Flow/FLOWOS_IMPLEMENTATION_STATUS.md)
- **Tracking**: Truthful status of every locked feature, verification matrix, and build results.

## Verification Plan

### Automated Tests
- `AdaptivePlannerTest`: Verify capacity-aware scheduling.
- `FrictionRadarTest`: Overrun detection logic.
- `OutcomeCompilerTest`: Validation of structured extractions.

### Final Demo Loop
1. **Capture**: "Hackathon demo by 8 PM."
2. **Pulse**: Recommends "Finish architecture".
3. **Friction**: Simulate architecture taking 30m over estimate.
4. **Radar**: Triggers "Plan at Risk" alert.
5. **Replanner**: Proposes revised schedule (Protecting 8 PM).
6. **User**: Accepts plan.
7. **Proof**: Attach evidence screenshot.
8. **Score**: Explainable score update.

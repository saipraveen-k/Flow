# Fix Code and Improve Build Configuration

This plan addresses syntax errors, runtime exceptions, and improves the Gradle configuration for the FlowOS project.

## User Review Required

> [!IMPORTANT]
> - I am upgrading the Room dependencies and adding the Room Gradle Plugin to improve build reliability and KSP integration.
> - I am fixing a `FileUriExposedException` in `ActionEngine` by using `FileProvider`. This assumes the `FileProvider` is correctly configured in `AndroidManifest.xml` (which it appears to be).

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/tempp/projects/Flow/gradle/libs.versions.toml)
- Add Room Gradle Plugin definition.
- (Optional) Update versions to latest stable where appropriate.

#### [MODIFY] [build.gradle.kts](file:///C:/tempp/projects/Flow/build.gradle.kts) (root)
- Add Room Gradle Plugin to the plugins block (apply false).

#### [MODIFY] [app/build.gradle.kts](file:///C:/tempp/projects/Flow/app/build.gradle.kts)
- Apply the Room Gradle Plugin.
- Configure `room` extension for schema location.
- Fix `packaging` block syntax for better compatibility.

---

### Logic & Syntax Fixes

#### [MODIFY] [TimeParser.kt](file:///C:/tempp/projects/Flow/app/src/main/java/com/flowos/app/util/TimeParser.kt)
- Fix syntax error in `parseDeadline` where a `when` branch was outside the block.
- Fix `humanLabel` to correctly convert `Long` epoch millis to `LocalDateTime` using `Instant`.

#### [MODIFY] [ActionEngine.kt](file:///C:/tempp/projects/Flow/app/src/main/java/com/flowos/app/action/ActionEngine.kt)
- Fix `openFile` to use `FileProvider` instead of `Uri.fromFile` to avoid `FileUriExposedException`.

#### [MODIFY] [AndroidManifest.xml](file:///C:/tempp/projects/Flow/app/src/main/AndroidManifest.xml)
- Add `android.permission.INTERNET` (often needed for ML Kit/Speech components).
- Add `<queries>` for common actions like email, calendar, and speech recognition to ensure `resolveActivity` works on Android 11+.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to ensure all syntax errors are resolved and the project builds.
- Run unit tests if available.

### Manual Verification
- Deploy to a device/emulator.
- Test "Capture" (Voice/Text/Image).
- Test "Execute" (Reminder scheduling, sharing, opening files).

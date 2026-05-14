# Contributing to NeuroMesh

Quick guide for teammates working on the codebase. Optimize for shipping the hackathon submission, not for long-term perfection.

---

## Branching

We work on short-lived feature branches off `main`. No long-running development branches — they cause merge pain near the deadline.

**Branch naming:**
- `feat/<short-desc>` — new feature (e.g., `feat/foreground-service`)
- `fix/<short-desc>` — bug fix (e.g., `fix/camera-rotation`)
- `docs/<short-desc>` — README, comments, demo script changes
- `refactor/<short-desc>` — code restructuring with no behavior change
- `chore/<short-desc>` — build, deps, gitignore, etc.

**Lifetime:** A branch should live no more than 24-48 hours. If it grows past that, split it.

---

## Commits

One commit = one logical change. Squash before merging if your branch grew messy.

**Commit message format:**
```
<area>: <short imperative summary, <60 chars>

Optional body explaining *why* (not *what* — diff shows what).
Wrap at 72 chars.
```

**Areas:** `agent`, `consensus`, `mesh`, `sensor`, `ui`, `ml`, `db`, `di`, `build`, `docs`

**Examples:**
```
agent: tighten observer JSON schema to reduce parse failures

consensus: lower CONSENSUS_THRESHOLD to 0.55 for 2-device demos

ui: animate scan overlay during detection
```

Avoid `WIP`, `fix stuff`, `updates`. They make `git log` useless when we're scrambling at the deadline.

---

## Pull Requests

Push the branch, open a PR against `main`, request review from at least one teammate, merge with **Squash and Merge** unless the branch has clean, atomic commits worth preserving.

**PR description template:**
```markdown
## What
One-sentence summary.

## Why
The motivation. Link to issue if there is one.

## How
Key implementation choices. Anything non-obvious.

## Test plan
- [ ] Built locally with `./gradlew assembleDebug`
- [ ] Installed on device, exercised the affected flow
- [ ] (If consensus/mesh changes) Tested with 2+ devices
- [ ] Logcat shows expected output
```

Don't merge your own PRs unless reviewers are asleep and the deadline is in <12 hours.

---

## Code Style

We follow the [Kotlin official style](https://kotlinlang.org/docs/coding-conventions.html). Android Studio will enforce most of it.

**Architecture rules (these matter, please don't break them):**

1. **Domain layer (`domain/`) has no Android imports** where avoidable. The `agent/`, `consensus/`, and `usecase/` packages should look like plain Kotlin. This is so the core logic can be unit-tested without an emulator.
2. **Infrastructure layer (`infrastructure/`) is the only place that touches SDKs.** Anything talking to CameraX, MediaPipe, Nearby, Room, sensors, files — it lives in `infrastructure/`.
3. **Presentation layer (`presentation/`) depends on Domain, not the other way around.** ViewModels orchestrate use cases; views render state.
4. **New SDK interactions should expose Kotlin `Flow` / `suspend` APIs, not callbacks.** If the SDK is callback-based (Nearby Connections is), wrap it once in `infrastructure/` and convert to Flow there. The rest of the app stays coroutine-native.
5. **No hardcoded magic numbers in business logic.** Put them in `util/Constants.kt` with a comment explaining the value.

**Specific anti-patterns to avoid:**

- `GlobalScope.launch` — use `viewModelScope` or pass in a `CoroutineScope`
- `runBlocking` outside of tests — it freezes the UI
- `!!` on nullable types — use `?.let { }` or explicit fallback
- Hilt-injecting `Context` directly into domain classes — keep `Context` confined to `infrastructure/` and `presentation/`
- New `companion object` constants that should be in `Constants.kt`

---

## How to Add Things

### A new agent

1. Define your output data class in `data/model/`.
2. Add the prompt template + JSON schema in `infrastructure/ml/PromptBuilder.kt`.
3. Add a parser in `infrastructure/ml/OutputParser.kt`.
4. Create the agent in `domain/agent/<YourAgent>.kt`. Mirror the structure of `ObserverAgent` / `ReasonerAgent`.
5. Provide it via Hilt in `di/NetworkModule.kt` (despite the name, that module is where agents currently live).
6. Plumb it into the relevant use case.

### A new sensor

1. Add the manager in `infrastructure/sensor/<Name>SensorManager.kt`.
2. Expose its current reading as either a `suspend fun` returning a one-shot description string, or as a `Flow` if it's continuous.
3. Inject it into `ObserverAgent` (or a new agent if it has its own reasoning needs).
4. Add the permission to `AndroidManifest.xml` and the runtime request to `MainActivity.requiredPermissions`.

### A new mesh message type

1. Add the enum value to `data/model/MeshPeer.kt::MessageType`.
2. Add a decoder in `infrastructure/network/MessageSerializer.kt` if it has a unique payload shape.
3. Handle it in `MainViewModel.collectMeshMessages()`.

### A new crisis type

1. Add the value to `data/model/Observation.kt::CrisisType`.
2. Update the JSON schemas in `PromptBuilder` to mention it.
3. Add an emoji + color mapping in `CrisisAlertView` and `CrisisAlertActivity`.
4. Add a drawable in `res/drawable/ic_<type>.xml`.
5. Add a string in `strings.xml`.

---

## Local Testing Checklist (before opening a PR)

```bash
# Should compile cleanly
./gradlew assembleDebug

# Install and exercise the affected flow on a real device
./gradlew installDebug
```

Then on the device:
- Launch the app, watch logcat (`adb logcat | grep NeuroMesh`)
- For agent/ML changes: confirm "X inference completed in Yms" appears and the latency hasn't regressed
- For mesh/consensus changes: install on 2+ devices, confirm "Consensus reached" fires
- For UI changes: walk the screens manually

We don't have automated tests yet (hackathon scope). The domain layer is structured to be testable later — feel free to add JUnit tests for `VotingStrategy`, `OutputParser`, `GossipProtocol` if you want to.

---

## Submitting Work Near the Deadline

In the final 24 hours before the May 19, 2026 deadline:

- **Stop refactoring.** Bug fixes only.
- **All PRs need a screenshot or a 5-second screen recording** showing the change working on a device.
- **No silent dependency upgrades.** If a dep version bump is needed, it gets its own PR with a clear reason.
- **Tag the final commit** with `v1.0-submission` so we can rebuild the exact binary later if Kaggle requests it.

---

## Communication

- **Architectural questions** → Slack / Discord with `@team-neuromesh` ping
- **Code questions** → comment on the relevant PR
- **Blocked** → mention it within 30 minutes. We have a tight deadline; don't burn hours alone.

If you find yourself rewriting more than 200 lines on a single task, pause and check in with the team before continuing.

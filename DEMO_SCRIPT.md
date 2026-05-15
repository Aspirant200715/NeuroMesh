# NeuroMesh — Submission Demo Script

A complete script for the hackathon submission video. Target length: **3 minutes** (Kaggle's typical demo cap). We have a 30-second buffer for fade-ins / branding.

---

## Pre-production Checklist

### Hardware
- [ ] 3 Android phones, fully charged, with NeuroMesh installed and model loaded
- [ ] Tripod or phone stand for the "camera operator" phone (your recording phone)
- [ ] A 4th phone or a laptop for the "ignition source" (playing fire-alarm audio, showing flame imagery)
- [ ] Power bank if shooting outdoors
- [ ] Quiet location for clean audio (no fans, no background music)

### Props
- [ ] A candle and lighter (real flame for the visual)
- [ ] Laptop with a fire-alarm sound clip loaded (search "fire alarm sound 10 seconds" on YouTube; download the audio)
- [ ] A printout or whiteboard showing the architecture diagram (for the Architecture section, if you don't have animated graphics)

### Software
- [ ] OBS Studio or your phone's native screen recorder for the in-app shots
- [ ] DaVinci Resolve / iMovie / CapCut for editing
- [ ] Royalty-free background music (use [pixabay.com/music](https://pixabay.com/music) — search "tense ambient" or "documentary")

### Pre-flight on the day
- [ ] **Bluetooth ON, Location ON on all phones. Wi-Fi can be OFF** (the mesh is Bluetooth-preferred and does not need Wi-Fi)
- [ ] All hero phones have ~4 GB+ RAM (so they run the full LLM, not heuristic mode)
- [ ] All 3 phones launched into NeuroMesh, mesh indicator shows 2 connected peers (so we're confident it works before the camera rolls)
- [ ] Verify model is loaded on all 3 — status pill should say "Monitoring"
- [ ] Clear notifications, set Do Not Disturb on all phones (no SMS popping mid-demo)
- [ ] Phone brightness on max
- [ ] One full dry run before the real take

---

## Shot List

We need these clips. Shoot them once, even multiple times for safety, and assemble in the edit.

1. **Title card** (3 s) — "NeuroMesh" + tagline
2. **Problem montage** (15 s) — news clips of disasters where cell towers went down (b-roll, see Asset Sources below)
3. **Solution intro to camera** (15 s) — one teammate piece-to-camera explaining what we built
4. **Architecture animated/static** (20 s) — the layered diagram with voice-over
5. **Single-device detection** (20 s) — phone screen recording: point at candle, alert fires
6. **Multi-device consensus** (45 s) — the headline moment, 3 phones simultaneously
7. **Reasoning trace close-up** (15 s) — screen recording of CrisisAlertActivity showing the "Why this alert?" panel
8. **Privacy / offline beauty shot** (10 s) — show airplane mode toggled on, app still working
9. **Closing piece-to-camera** (15 s) — call to action
10. **Credits** (5 s) — team names + GitHub URL

Total: ~163 s. Trim to 180 s with transitions.

---

## The Script

Below: the spoken voice-over (VO) for each section, plus what's on screen. Time stamps are approximate, adjust in the edit.

### 0:00 — 0:03  Title Card
**SCREEN:** Black background, NeuroMesh logo fades in. Tagline below: "AI crisis response without the internet."
**VO:** (none, just music sting)

### 0:03 — 0:18  The Problem
**SCREEN:** Quick montage — earthquake aftermath b-roll, cell tower falling, phones showing "no signal", people with phones held up trying to call for help.
**VO:**
> When disaster hits, the first thing to fail is the infrastructure people depend on to call for help. Cell towers go dark. Internet routes break. The 911 app on your phone becomes useless in the exact moment you need it most. And yet — in any disaster zone, hundreds of survivors are within a few meters of each other, each one holding a powerful sensor with cameras, microphones, accelerometers, and 4 gigabytes of RAM.

### 0:18 — 0:33  Our Solution
**SCREEN:** Cut to teammate, piece-to-camera, NeuroMesh app open on a phone visible in the frame.
**VO (on camera):**
> We built NeuroMesh. It turns every Android phone in a disaster zone into a node in an AI swarm. Each phone runs specialized agents on Gemma 4 — on-device, completely offline. The phones discover each other directly over Bluetooth, share what they're observing, vote on what's happening, and produce trustworthy emergency guidance. No internet. No cloud. No central server. No Wi-Fi or cell tower needed. Just survivors helping survivors, with AI in the middle.

### 0:33 — 0:53  Architecture
**SCREEN:** The layered architecture diagram from the README. Highlight each layer as it's mentioned (animated reveal or just the static diagram with a moving spotlight).
**VO:**
> The architecture is layered. At the bottom, infrastructure: CameraX, the microphone, the accelerometer, and MediaPipe LiteRT running a quantized Gemma 4. In the middle, three agents: the Observer turns raw sensor data into structured observations. The Reasoner fuses observations from this phone *and* neighboring phones into a situation assessment with full reasoning trace. The Action agent produces specific, life-saving guidance. Wrapping all of that is a gossip-based mesh consensus engine — peers broadcast their assessments, votes are weighted by confidence, and a 60% supermajority triggers a mesh-wide alert. End-to-end, detection to alert: under five seconds.

### 0:53 — 1:13  Single-Device Detection
**SCREEN:** Screen recording from Phone A. Show the camera preview with the status pill at the bottom reading "Monitoring". A teammate's hand brings a lit candle into frame. Within 3 seconds the status pill flips to "Analyzing". 2 seconds later, a full-screen fire alert overlays the screen.
**VO:**
> Here's a single phone detecting a local fire. The Observer agent picks up the orange-dominant pixels and the fire-alarm audio. The Reasoner concludes: FIRE, HIGH severity, with 78% confidence. The Action agent generates immediate steps: evacuate via the nearest stairwell, do not use the elevator, cover your face. All of this — running locally, on the device, in 3.2 seconds.

### 1:13 — 1:58  Multi-Device Consensus (Headline Moment)
**SCREEN:** Wide shot of three phones on a table. Camera operator phone records from above. Then cut between screen recordings as we narrate.

**Step 1 (1:13-1:23):** All three phones show "Monitoring", mesh indicator on each shows "2 devices" in green. Hold for a beat — this proves the mesh is alive before the test.
**VO:**
> Three phones. They've already discovered each other over Bluetooth — no router involved, no internet. The mesh indicator on each one shows two connected peers.

**Step 2 (1:23-1:38):** Bring a fire indicator into frame of Phone A only. Phone A flips to Analyzing → Alert. Phones B and C *also* flip to Alert within 2-3 seconds, even though their cameras see nothing relevant.
**VO:**
> When I show only Phone A a fire, watch what happens. Phone A detects it locally — and broadcasts its assessment to its peers. Phones B and C receive the assessment, run it through their own consensus engines, and even though their own cameras saw nothing, they trust the mesh. All three phones fire alerts simultaneously. Look at the bottom of the alert: "Confirmed by 1 mesh device." That's not a centralized broadcast — that's distributed trust.

**Step 3 (1:38-1:58):** Now bring fire indicators into Phones B and C as well. Show the alert on Phone D (a 4th phone if available, or a refresh on one of B/C). The contributing device count climbs from 1 → 2 → 3.
**VO:**
> As more phones independently confirm the fire, confidence rises. The contributing device count climbs. This is the core insight: a single sensor reading is unreliable. But three independent sensors agreeing? That's a high-trust signal. And it took zero infrastructure to produce.

### 1:58 — 2:13  Reasoning Trace (Explainability)
**SCREEN:** Tap the alert. The CrisisAlertActivity opens. Scroll down to "Why this alert?". Show the reasoning trace: numbered steps with evidence.
**VO:**
> Crucially, no alert in NeuroMesh is a black box. Every alert ships with its reasoning trace — the exact chain of evidence that led to the conclusion. Step one: visual analysis detected red-orange color dominance. Step two: audio analysis detected a 2.4 kilohertz alarm pattern at 87 decibels. Step three: two peer devices independently confirmed similar observations. This isn't AI telling you what to do — it's AI showing you why, so you can decide if you trust it.

### 2:13 — 2:23  Privacy / Offline Beauty Shot
**SCREEN:** Cut to a phone with airplane mode toggling ON. The mesh indicator stays green. A new detection happens. The alert fires.
**VO:**
> No data ever leaves the local mesh. No server to compromise. No operator to subpoena. Works in airplane mode. Works in a basement. Works in a war zone. Works anywhere there are phones.

### 2:23 — 2:38  Closing & Call to Action
**SCREEN:** Cut to teammate, piece-to-camera, with all three phones visible.
**VO (on camera):**
> We built NeuroMesh in five days, on Gemma 4 E2B, using Google AI Edge LiteRT. The hardware to run it is already in 6 billion pockets worldwide. We think this is what good AI looks like — embedded in the devices that survivors actually carry, helping them help each other when the centralized world stops working. Thank you.

### 2:38 — 2:43  Credits
**SCREEN:** Team names, GitHub URL, Kaggle submission ID.
**VO:** (none, music swells and fades)

---

## Asset Sources (royalty-free)

- **Disaster b-roll** — Pexels: search "earthquake", "wildfire", "flood city"; Pond5 free section
- **Cell tower / "no signal" footage** — Pixabay video, search "cell tower" and "no signal phone"
- **Music** — Pixabay Music: "Cinematic Tension" or similar; Bensound's "The Lounge" works for calmer moments
- **Sound effects** — Freesound.org: search "fire alarm", "earthquake rumble"
- **Fonts** — Inter (Google Fonts) for titles; system sans-serif for VO captions

---

## Captions / Subtitles

Burn subtitles into the video. The Kaggle judges may watch on mute. Use a simple white sans-serif at the bottom third of the frame with a 60% opacity dark background behind the text.

---

## Common Demo Pitfalls (avoid these)

- **Bluetooth disabled.** The mesh is Bluetooth-preferred (`P2P_STAR`). Bluetooth MUST be ON on every phone. Wi-Fi is *not* required and the app no longer forces it on — leaving Wi-Fi off is fine (and matches the "no infrastructure" story for the airplane-mode shot). Location must be ON (Nearby/BLE scanning needs it on Android).
- **Demo phones must have ~4 GB+ RAM.** Below that the phone runs in heuristic mode (no LLM-generated guidance text — just templated hints). For the headline shots use 4 GB+ (ideally 6 GB+) phones so the alerts show full reasoning traces. A low-RAM phone is fine as an *extra* mesh peer to pad the device count, but don't make it the hero phone.
- **Phones too far apart.** Stay within 5 meters for the demo. Bluetooth's effective range with body interference is worse than the spec.
- **Phones already paired to a hundred other Bluetooth things.** Reboot them before the shoot — fresh Bluetooth state pairs faster.
- **Wrong filename for the model.** If the model is named anything other than exactly `gemma4_e2b_q4.tflite`, `ModelLoader` won't find it. Verify before shoot day.
- **Battery saver mode kicking in.** Disable battery optimization for NeuroMesh on every demo phone, or it'll throttle Bluetooth scanning.
- **Screen recording while inference is running.** Some phones will throttle inference if the GPU is also rendering a screen recording. Test ahead.
- **Long startup.** First launch copies the 1.3 GB model from assets to filesDir — that takes 30-60 s. Launch all phones once before the camera rolls, then keep them launched.

---

## Rehearsal Plan

| When | What |
|---|---|
| T-3 days | Full dry run on the actual phones, capture rough cut |
| T-2 days | Review rough cut as a team, identify weakest section, reshoot it |
| T-1 day | Final cut, captions, music levels |
| T-0     | Final review pass, upload to YouTube unlisted, paste link into Kaggle submission |

---

## Submission Checklist (Kaggle May 19, 2026)

- [ ] Demo video uploaded to YouTube (Unlisted)
- [ ] GitHub repo public (or accessible via Kaggle's review process)
- [ ] README.md polished and includes the YouTube link
- [ ] Model is **not** in the repo (gitignored)
- [ ] A separate `models/` release on GitHub or a Kaggle dataset link for downloading the .tflite
- [ ] Three prize tracks selected in the Kaggle form: Global Resilience, Cactus Track, LiteRT Track
- [ ] Team members all added on Kaggle
- [ ] Submitted with **>2 hours** to spare (Kaggle's upload pipeline can be slow at deadline)

Good luck. Ship it.

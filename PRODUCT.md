# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users
The owner, listening mostly on an Android phone while on the go (commuting, walking, the gym), often one-handed and in short glances. A few friends may use it too, only if they ask. Desktop (Linux) and a browser build exist as secondary surfaces for longer sessions.

## Product Purpose
A personal music player for a library the owner controls. It is not for sale and has no accounts, ads or recommendations. Success means it is the app the owner reaches for instead of a streaming service, because listening to their own music feels better in it.

## Positioning
Maestro plays music the owner already has and treats each album as a whole work, with the cover front and center. Nothing leaves the owner's devices unless they choose it, and every interaction is built to feel physical and immediate. A streaming service can't truthfully claim either.

## Operating Context
- Phone use is glanceable and interrupted: pick an album, start it, pocket the phone; control playback from the mini player, the lock screen or headphones.
- Desktop use is a window beside other work, driven by keyboard shortcuts and the menu bar.
- The browser build doubles as a way for friends to try it and as the target for Playwright visual review (`design/shots.ts`).

## Capabilities and Constraints
- Built with Kotlin Multiplatform and Compose Multiplatform: Android, desktop (JVM) and web (Wasm) share one UI.
- Music sources: local files on each device and an optional self-hosted server, behind one `LibraryRepository` interface. (Decided; not yet built. Chunk 1 uses a fictional sample library and a silent `SimulatedPlayer`.)
- Library structure: Albums, Artists, Songs; album and artist detail; one player that docks as a mini player and expands to Now Playing.
- Undecided: playlists, search, lyrics, which self-hosted server protocol to support.

## Brand Commitments
- Name: Maestro.
- The feel is the product: 1:1 direct manipulation, interruptible spring motion, momentum and rubber-banding (see the apple-design skill and `ui/theme/Motion.kt`).
- Album-first: cover art carries the color; the interface stays quiet around it.

## Evidence on Hand
No real music, artwork or testimonials yet. The sample albums and artists in `data/SampleLibrary.kt` are fictional and generated covers stand in for artwork; do not present them as real.

## Product Principles
1. Own the library: no algorithmic feeds, no tracking, nothing uploaded without an explicit choice.
2. Album-first: the whole record and its cover come before individual tracks and chrome.
3. Feel over features: an interaction ships only when it responds instantly and can be interrupted.
4. One hand, one glance: the most frequent actions are reachable at the bottom of a phone screen.

## Accessibility & Inclusion
Text at 4.5:1 or better in light and dark, 44dp minimum targets, system text scaling, screen-reader labels and adjustable controls, and a reduced-motion path that swaps movement for fades.

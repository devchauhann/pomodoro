# Pomodoro

A minimal, distraction-free Pomodoro timer for Android built with Jetpack Compose.

## Showcase

https://github.com/devchauhann/pomodoro/raw/master/tutorial.webm

## Features

- **Focus & Break sessions** with adjustable durations
- **Focus mode** — timer scales up, UI fades away
- **Background timer** — keeps running when app is minimized
- **Notifications** — alerts on session completion with vibration
- **Dark & Light theme** with smooth animated transitions
- **Landscape optimized** layout

## Installation

1. Download the latest APK from [Releases](https://github.com/devchauhann/pomodoro/releases)
2. Install on your Android device (Android 10+)

### Build from source

```bash
git clone https://github.com/devchauhann/pomodoro.git
cd pomodoro
./gradlew assembleRelease
```

The APK will be at `app/build/outputs/apk/release/`.

## Usage

1. Tap **Play** to start a focus session
2. Tap the **FOCUS / BREAK** label to switch session type
3. Adjust durations in **Settings** (gear icon)
4. Timer continues in background with notification updates
5. Vibration + notification on session complete

## Tech Stack

- Kotlin
- Jetpack Compose + Material 3
- Foreground Service for background timer
- Min SDK 29 (Android 10)

## License

Open source. Made by [Dev Chauhan](https://devchauhan.in)

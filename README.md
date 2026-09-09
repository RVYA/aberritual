<p align="center">
  <img src="art/logo.svg" alt="Aberritual Logo" width="120" height="120" />
</p>

<h1 align="center">Aberritual</h1>

<p align="center">
  <b>Interactive Physical Lenticular Live Wallpaper Engine for Android</b>
</p>

<p align="center">
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-GPLv3-blue.svg" alt="License: GPLv3"></a>
  <img src="https://img.shields.io/badge/Platform-Android%208.0%2B-green.svg" alt="Platform: Android">
  <img src="https://img.shields.io/badge/Engine-OpenGL%20ES-orange.svg" alt="Engine: OpenGL ES">
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-purple.svg" alt="UI: Jetpack Compose">
</p>

---

## Overview

**Aberritual** (*Aberration* + *Ritual*) blends multiple photographs (2 to 5 layers) into an optical raster that responds to physical smartphone movement in real time. As you tilt your device, hardware-accelerated OpenGL ES shaders simulate the physical ridges of a lenticular lens, smoothly cross-fading and shifting layers across the viewport with dynamic chromatic aberration.

### Key Highlights
- **Hardware-Accelerated Render Pipeline**: Custom OpenGL ES 2.0/3.0 engine embedded directly within Android's `WallpaperService`.
- **Sensor Fusion with EMA Smoothing**: Driven by `Sensor.TYPE_ROTATION_VECTOR` combined with Exponential Moving Average (EMA) low-pass filtering and adaptive pitch baselines.
- **Zero-Drain Standby**: Lifecycle management with immediate suspension of OpenGL render threads and sensor listeners when the screen is locked or the launcher is hidden.
- **Material 3 Creator Studio**: Modern Jetpack Compose UI for importing, auto-orienting, reordering, and previewing image stacks with interactive touch and tilt simulation.
- **Wrap-Around Transitions**: Continuous rotation mode for seamless circular loop transitions across photo sets.

---

## Architecture & Subsystems

```
com.caylakym.aberritual/
├── MainActivity.kt                      # Jetpack Compose UI & navigation entrypoint
├── wallpaper/
│   ├── AberritualWallpaperService.kt    # Android WallpaperService entrypoint
│   └── GLWallpaperEngine.kt             # EGL context & Surface lifecycle manager
├── core/
│   ├── engine/
│   │   ├── LenticularRenderer.kt        # Quad geometry & draw call pipeline
│   │   ├── ShaderProgram.kt             # GLSL compilation & uniform caching
│   │   └── TextureManager.kt            # Bitmap-to-GPU texture memory pipeline
│   ├── sensor/
│   │   ├── MotionSensorManager.kt       # Sensor.TYPE_ROTATION_VECTOR listener
│   │   └── EmaFilter.kt                 # EMA smoothing & deadband thresholding
│   └── util/
│       └── BitmapProcessor.kt           # Landscape auto-rotation & downsampling
├── data/
│   ├── model/                           # Domain configuration models
│   ├── local/                           # SharedPreferences & WebP storage manager
│   └── repository/                      # WallpaperRepository implementation
└── ui/
    ├── creator/                         # Studio screen, photo picker & layer deck
    ├── preview/                         # GLSurfaceView & preview bottom sheet
    └── theme/                           # Material 3 dynamic color tokens
```

---

## Tech Stack

- **Language**: Kotlin 2.x
- **UI Framework**: Jetpack Compose + Material 3
- **Graphics Pipeline**: OpenGL ES (GLSL Vertex & Fragment Shaders)
- **System Integration**: Android `WallpaperService` & `WallpaperColors` API
- **State & Storage**: SharedPreferences + WebP Internal App Storage
- **Build System**: Gradle Version Catalog (`libs.versions.toml`)

---

## License & Trademark Notice

### Source Code
The source code of **Aberritual** is licensed under the [GNU General Public License v3.0 (GPLv3)](LICENSE). You are free to inspect, study, modify, and distribute the code under the terms of the GPLv3.

### Brand Name, Logos & Proprietary Assets
The name **Aberritual**, project logos, branding icons, and any proprietary default asset/preset imagery are **NOT** covered by the GPLv3. All rights to these branding and media elements are strictly reserved by the copyright holder. You may not use the "Aberritual" name or branding in commercial distributions or derivative works without explicit written permission.

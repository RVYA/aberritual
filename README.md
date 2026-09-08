# Aberritual

An interactive native Android Live Wallpaper engine that creates authentic lenticular optical print effects and dynamic parallax motion driven by real-time device tilt sensors.

---

## Overview

**Aberritual** (*Aberration* + *Ritual*) blends multiple photographs (2 to 5 layers) into an optical raster that responds to physical smartphone movement in real time. As you tilt your device, hardware-accelerated OpenGL ES shaders simulate the physical ridges of a lenticular lens, smoothly cross-fading and shifting layers across the viewport.

### Key Highlights
- **Hardware-Accelerated Render Pipeline**: Custom OpenGL ES 2.0/3.0 engine embedded directly within Android's `WallpaperService`.
- **Sensor Fusion with EMA Smoothing**: Driven by `Sensor.TYPE_ROTATION_VECTOR` combined with Exponential Moving Average (EMA) low-pass filtering to eliminate micro-jitter.
- **Zero-Drain Standby**: Full lifecycle management with immediate suspension of OpenGL render threads and sensor listeners when the screen is off or the launcher is hidden.
- **Material 3 Creator Studio**: Modern Jetpack Compose UI for importing, reordering, and previewing image stacks with interactive touch and tilt simulation.

---

## Architecture & Subsystems

```
com.caylakym.aberritual/
├── MainActivity.kt                      # Jetpack Compose UI & navigation entrypoint
├── wallpaper/
│   ├── AberritualWallpaperService.kt    # Android WallpaperService entrypoint
│   ├── gl/
│   │   ├── GLWallpaperEngine.kt         # EGL context & Surface lifecycle manager
│   │   ├── LenticularRenderer.kt        # Quad geometry & draw call pipeline
│   │   ├── ShaderProgram.kt             # GLSL compilation & uniform caching
│   │   └── TextureManager.kt            # Bitmap-to-GPU texture memory pipeline
│   └── sensor/
│       ├── MotionSensorManager.kt       # Sensor.TYPE_ROTATION_VECTOR listener
│       └── Filter.kt                    # EMA smoothing & deadband thresholding
├── data/
│   ├── model/                           # Wallpaper configuration models
│   └── repository/                      # DataStore & internal bitmap caching
└── ui/
    ├── creator/                         # Layer import, cropping, and ordering
    ├── preview/                         # Interactive touch/tilt live preview
    └── theme/                           # Material 3 design tokens
```

---

## Tech Stack

- **Language**: Kotlin 2.x
- **UI Framework**: Jetpack Compose + Material 3
- **Graphics Pipeline**: OpenGL ES (GLSL Vertex & Fragment Shaders)
- **System Integration**: Android `WallpaperService` & `WallpaperColors` API
- **State & Storage**: Jetpack DataStore Preferences + Internal App Storage
- **Build System**: Gradle Version Catalog (`libs.versions.toml`)

---

## License & Trademark Notice

### Source Code
The source code of **Aberritual** is licensed under the [GNU General Public License v3.0 (GPLv3)](LICENSE). You are free to inspect, study, modify, and distribute the code under the terms of the GPLv3.

### Brand Name, Logos & Proprietary Assets
The name **Aberritual**, project logos, branding icons, and any proprietary default asset/preset imagery are **NOT** covered by the GPLv3. All rights to these branding and media elements are strictly reserved by the copyright holder. You may not use the "Aberritual" name or branding in commercial distributions or derivative works without explicit written permission.

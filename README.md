# Kinetic Cues (Vehicle Motion Cues for Android)

**Kinetic Cues** is an intelligent, privacy-first Android application designed to help passengers reduce motion discomfort (car sickness) when reading or viewing their phone in a moving vehicle.

Inspired by the neuro-vestibular concept of peripheral motion synchronizers (such as Apple Vehicle Motion Cues), **Kinetic Cues** features an original, modern Android design with real physical motion tracking, a touch-through system overlay window, and a dedicated multi-stage motion processing engine.

---

## 🚗 Core Principles & How It Works

Motion sickness typically occurs when there is a sensory conflict:
- Your **inner ear (vestibular system)** feels the accelerations, braking, and turns of the car.
- Your **eyes** are focused on a static digital screen that appears completely motionless.

**Kinetic Cues** resolves this conflict by displaying discreet, animated dots along the perimeter of the screen that move in real-time alignment with the vehicle's physical inertia. Because peripheral vision is highly sensitive to motion, your brain unconsciously perceives the vehicle's movement while your central vision remains focused on your content.

---

## 🏗️ Architecture Overview

The application follows clean, reactive MVVM architecture with decoupled subsystems:

```
Android Sensor Framework (Accelerometer, Gyroscope, Gravity, Linear Accel)
      │
      ▼
SensorDataManager (Fallback estimation, display rotation remapping)
      │
      ▼
MotionEngine (Noise deadband, low-pass filter, complementary fusion, Vehicle State Machine)
      │
      ▼
ProcessedMotion State
      ├──► LivePreviewPhone (In-app Compose interactive canvas)
      └──► MotionCueOverlayView (Choreographer 60 FPS hardware-accelerated overlay canvas)
            ▲
            │
MotionCueService (Android Foreground Service with Ongoing Notification & Quick Actions)
```

### 1. Motion Processing Engine (`MotionEngine`)
- **Noise Deadband Filtering:** Eliminates micro-tremors and sensor noise floor (< 0.16 m/s²).
- **Complementary Fusion:** Merges linear acceleration with gyroscope yaw velocity to capture lateral centrifugal forces during sharp cornering.
- **Orientation Awareness:** Compensates for device display rotation (Portrait, Landscape 90°, Reverse Landscape 270°, Reverse Portrait 180°).
- **Vehicle State Machine:**
  - `IDLE`: Stationary at rest.
  - `POSSIBLE_MOTION`: Accumulating acceleration evidence.
  - `VEHICLE_MOTION`: Sustained directional momentum.
  - `ACTIVE`: Cues actively rendering.
  - `LOW_MOTION`: Cruising at constant velocity.
  - `STOPPED`: Automatic fade-out after user-defined delay (default 6s).

### 2. Android Overlay System (`MotionOverlayManager`)
- Implements `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`.
- Strict touch-through flags:
  - `FLAG_NOT_FOCUSABLE`: Never intercepts keystrokes or input.
  - `FLAG_NOT_TOUCHABLE`: All touch, scroll, and tap events pass directly through to underlying apps (YouTube, Chrome, Telegram, etc.).
  - `FLAG_LAYOUT_IN_SCREEN` & `FLAG_LAYOUT_NO_LIMITS`: Edge-to-edge drawing.
  - `FLAG_HARDWARE_ACCELERATED`: Fluid 60 FPS rendering.
- **Smart Edge Avoidance:** Accounts for WindowInsets, display cutouts (notches / punch-holes), and bottom gesture navigation pills.

### 3. Foreground Service (`MotionCueService`)
- Android 14+ / 15+ compatible with `foregroundServiceType="specialUse"`.
- Low-importance notification channel to prevent annoying alerts while keeping the service running smoothly.
- Quick notification actions to **Pause / Resume** or **Turn Off**.

---

## 🧪 Testing & Motion Simulation

You do **not** need to sit in a car to evaluate or test Kinetic Cues!

The app includes an **Interactive Motion Simulator** with pre-configured realistic maneuvers:
1. **Stationary:** Zero inertial push.
2. **Gentle Cruise:** Subtle straight-line highway vibrations.
3. **Hard Braking:** Forward inertial surge ($+Y$).
4. **Rapid Acceleration:** Backward inertial pull ($-Y$).
5. **Sharp Left Turn:** Rightward centrifugal drift ($+X$).
6. **Sharp Right Turn:** Leftward centrifugal drift ($-X$).
7. **Curvy Mountain Road:** Alternating slalom sway with braking dynamics.

---

## 🔒 Permissions & Privacy

Kinetic Cues is built on **privacy-first** principles:
- **No Internet Access:** All sensor data is processed locally in RAM; zero data is collected, stored, or transmitted.
- **No Location/GPS Permission:** Uses inertial sensors only, preserving battery and privacy.
- **Permissions Declared:**
  - `SYSTEM_ALERT_WINDOW`: Required to draw over third-party applications.
  - `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_SPECIAL_USE`: Required to maintain active tracking while switching apps.
  - `POST_NOTIFICATIONS`: Android 13+ permission for the foreground service control notification.
  - `VIBRATE`: Optional haptic feedback.

---

## ⚙️ OEM & Battery Optimization Guidelines

Some Android manufacturers enforce aggressive background killing policies:

### Xiaomi / HyperOS / MIUI
1. Go to **Settings > Apps > Manage Apps > Kinetic Cues**.
2. Enable **Autostart**.
3. In **Battery Saver**, select **No restrictions**.
4. In **Permissions**, ensure **Display pop-up windows while running in the background** is enabled.

### Samsung (One UI)
1. Go to **Settings > Battery and device care > Battery**.
2. Tap **Background usage limits > Never sleeping apps**.
3. Add **Kinetic Cues**.

### Google Pixel
1. Go to **Settings > Apps > Kinetic Cues > App battery usage**.
2. Select **Unrestricted**.

### OnePlus / OPPO / Realme (OxygenOS / ColorOS)
1. Go to **Settings > Battery > App battery management**.
2. Find **Kinetic Cues** and enable **Allow background activity** and **Allow auto-launch**.

---

## 🛠️ Building the Project

Run standard Gradle compilation:
```bash
gradle :app:assembleDebug
```

Run local JVM unit and Robolectric tests:
```bash
gradle :app:testDebugUnitTest
```

---

## ⚠️ Passenger Comfort Notice
*Kinetic Cues is intended strictly for passengers. Do not operate your phone or activate motion cues while driving. This software is an ergonomic comfort aid and is not a medical device.*

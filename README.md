# android_esp32_bluetooth

Android app (written in **Kotlin**) intended to communicate with an **ESP32 over Bluetooth**, providing a simple UI flow to start the app and then control/interact with an ESP32 device.

> Repo: `paulynaa/android_esp32_bluetooth`  
> Default branch: `main`

---

## Table of Contents

- [Project Overview](#project-overview)
- [What This Repository Contains](#what-this-repository-contains)
- [High-Level Architecture](#high-level-architecture)
- [Module & Folder Structure](#module--folder-structure)
- [File-by-File Breakdown](#file-by-file-breakdown)
- [Key Classes & Expected Responsibilities](#key-classes--expected-responsibilities)
- [Setup Instructions](#setup-instructions)
- [Usage Examples](#usage-examples)
- [Build & Run](#build--run)
- [Notes / TODOs](#notes--todos)

---

## Project Overview

This repository is an **Android Studio / Gradle** project organized as a typical multi-module Android app with an `app` module. The project name and package naming suggest it is meant for **ESP32 control** (Lithuanian: *valdymas* = *control/management*) via **Bluetooth**.

At a glance, the Android UI entry points present in the repo are:

- `StartActivity` — likely an initial screen (splash / start / permission gate).
- `MainActivity` — likely hosts the main UI content (often a Compose host or Fragment container).
- `MainFragment` — a UI component that likely implements the actual interaction screen.

There is also a top-level directory named `bt_def`, which (based on the name) probably contains Bluetooth definitions / shared constants / protocol definitions for communicating with the ESP32.

---

## What This Repository Contains

From the repository structure currently available:

- A standard Gradle build setup using Kotlin DSL (`*.gradle.kts`)
- An Android `app` module with:
  - `AndroidManifest.xml`
  - Kotlin sources under `app/src/main/java/...`
  - Android resources under `app/src/main/res`
- Gradle wrapper scripts (`gradlew`, `gradlew.bat`)
- Project configuration files (`settings.gradle.kts`, `gradle.properties`, `.gitignore`)
- A minimal existing `README.md` (currently very small)

---

## High-Level Architecture

### Components

**1) Android UI layer**
- `StartActivity` → first screen / launcher entry.
- `MainActivity` → main host activity.
- `MainFragment` → main UI logic (likely where controls/buttons exist).

**2) Bluetooth / ESP32 communication layer**
- Expected to exist based on repository intent and the presence of `bt_def`.
- Typical responsibilities in an ESP32 Bluetooth controller app:
  - Discover devices / scan
  - Request runtime permissions (Bluetooth + location/nearby devices depending on Android version)
  - Connect to the ESP32 (Classic Bluetooth SPP or BLE GATT)
  - Encode/decode commands (simple text protocol, byte protocol, or BLE characteristics)
  - Maintain connection state and expose it to UI

> Important: The exact Bluetooth implementation details (Classic vs BLE, protocol, UUIDs, etc.) can only be documented precisely once the Bluetooth-related source files are inspected.

### Typical Runtime Flow (inferred from file names)

1. App launches into **StartActivity**
2. User proceeds into **MainActivity**
3. **MainActivity** hosts **MainFragment**
4. **MainFragment** provides UI actions that trigger Bluetooth communication with the ESP32

---

## Module & Folder Structure

```text
.
├── app/                            # Android application module
│   ├── build.gradle.kts            # App module Gradle config
│   ├── proguard-rules.pro          # ProGuard/R8 rules
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/
│       │   │   └── com/example/esp32_valdymas/
│       │   │       ├── MainActivity.kt
│       │   │       ├── MainFragment.kt
│       │   │       ├── StartActivity.kt
│       │   │       └── ui/theme/   # Compose theme package (common in Compose projects)
│       │   └── res/                # Android resources (layouts/drawables/values/etc.)
│       ├── test/                   # Unit tests
│       └── androidTest/            # Instrumentation tests
├── bt_def/                         # Likely Bluetooth definitions / protocol constants
├── gradle/                         # Gradle wrapper support files
├── build.gradle.kts                # Top-level Gradle config
├── settings.gradle.kts             # Module includes and project settings
├── gradle.properties               # Gradle/Android properties
├── gradlew / gradlew.bat           # Gradle wrapper scripts
└── README.md                       # (Existing minimal README)
```

---

## File-by-File Breakdown

### Root-level

- `.gitignore`  
  Git ignore rules for the repository.

- `README.md`  
  Currently a short placeholder README (this file is meant to replace it with a complete one).

- `build.gradle.kts`  
  Top-level Gradle build configuration (plugins, repositories, global configuration).

- `settings.gradle.kts`  
  Declares modules (at minimum includes `:app`) and project naming/settings.

- `gradle.properties`  
  Gradle and Android build flags and configuration values.

- `gradle/`, `gradlew`, `gradlew.bat`  
  Gradle wrapper files so the project can be built without installing Gradle globally.

- `.idea/`  
  Android Studio / IntelliJ project configuration (usually not essential for documentation).

- `bt_def/`  
  Folder name strongly suggests “Bluetooth definitions”. Common contents would be:
  - UUID definitions (if BLE)
  - Command constants for ESP32 protocol
  - Packet formats
  - Shared enums / sealed classes representing commands or responses

### `app/` module

- `app/build.gradle.kts`  
  Android application module build configuration: `applicationId`, SDK versions, dependencies (likely AndroidX / Compose / Bluetooth libraries).

- `app/proguard-rules.pro`  
  Keep rules for minification/obfuscation if release builds are enabled.

- `app/src/main/AndroidManifest.xml`  
  Declares app components and permissions. For Bluetooth apps, this often includes permissions such as:
  - `BLUETOOTH`, `BLUETOOTH_ADMIN` (older Android)
  - `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT` (Android 12+)
  - Location-related permissions (sometimes needed for scanning on older versions)
  - Activities (`StartActivity`, `MainActivity`), intent filters, etc.

- `app/src/main/java/com/example/esp32_valdymas/MainActivity.kt`  
  Main host activity. In many modern apps, this:
  - sets the content view (Compose `setContent { ... }` or XML)
  - sets up navigation
  - hosts fragments (if using Fragment-based UI)

- `app/src/main/java/com/example/esp32_valdymas/MainFragment.kt`  
  Fragment likely containing:
  - UI controls for interacting with ESP32
  - logic for establishing Bluetooth connection
  - handlers for send/receive events

- `app/src/main/java/com/example/esp32_valdymas/StartActivity.kt`  
  Entry activity likely responsible for:
  - requesting permissions
  - quick setup or device selection
  - forwarding user into the main screen

- `app/src/main/java/com/example/esp32_valdymas/ui/theme/`  
  Standard location for Jetpack Compose theme definitions (colors, typography, shapes).

---

## Key Classes & Expected Responsibilities

Because only the file list is currently available (not the full file contents), the following describes the **expected** responsibilities based on conventional Android architecture and the filenames:

### `StartActivity`
Typically used to:
- check whether Bluetooth is supported/enabled
- request runtime permissions required for scanning/connecting
- provide a “Start” button or immediately route to `MainActivity`

### `MainActivity`
Typically used to:
- host the main UI (Compose root or fragment container)
- set up app-wide UI theme
- coordinate navigation (if applicable)

### `MainFragment`
Typically used to:
- display the control surface (buttons/sliders/toggles for ESP32)
- initiate scan/connect operations
- send commands to the ESP32
- display received state/telemetry from ESP32

---

## Setup Instructions

### Prerequisites

- **Android Studio** (recent stable version recommended)
- **Android SDK** installed via Android Studio
- A physical Android device (Bluetooth testing is best on a real device)
- An ESP32 flashed with firmware that matches your Bluetooth protocol (Classic SPP or BLE GATT)

### Clone and open

```bash
git clone https://github.com/paulynaa/android_esp32_bluetooth.git
cd android_esp32_bluetooth
```

Open the folder in **Android Studio**.

### Build

From Android Studio:
- Let Gradle sync complete
- Build the project via **Build > Make Project**

From command line:
```bash
./gradlew :app:assembleDebug
```

---

## Usage Examples

### Example: Running the app

1. Install the app to a device:
   - Run from Android Studio (Run ▶), or install the generated APK.
2. Open the app.
3. On first run:
   - grant Bluetooth (and potentially nearby devices / location) permissions when prompted.
4. Proceed from **StartActivity** to the main screen.
5. Use the controls on the main UI (likely in **MainFragment**) to connect and send commands.

### Example: Typical ESP32 control flow (conceptual)

While the exact protocol is repository-specific, a common pattern is:

- Tap **Scan** → pick ESP32 device
- Tap **Connect**
- Tap actions such as:
  - “LED ON” → send command (e.g., `LED:1\n`)
  - “LED OFF” → send command (e.g., `LED:0\n`)
  - “STATUS” → request status and show response in UI

> Update this section with the real commands/UUIDs once the Bluetooth/protocol definitions in `bt_def/` are documented.

---

## Build & Run

- Debug build:
  ```bash
  ./gradlew :app:installDebug
  ```

- Unit tests:
  ```bash
  ./gradlew :app:test
  ```

- Instrumented tests (requires device/emulator):
  ```bash
  ./gradlew :app:connectedAndroidTest
  ```

---

## Notes / TODOs

To make this README truly “complete” (including **architecture explanation, file-by-file breakdown, and explanation of key functions**), the remaining work is to extract and document:

- the Bluetooth mode:
  - Classic SPP vs BLE GATT
- device filtering and pairing expectations
- the ESP32 command protocol:
  - command names
  - payload formats
  - expected responses
- key functions inside:
  - `MainFragment.kt`
  - any Bluetooth manager / service classes (likely under `bt_def/` or other packages)
- permissions and manifest declarations from `AndroidManifest.xml`
- dependencies from `app/build.gradle.kts`

Once those source contents are included, this README can be expanded with:
- exact classes (e.g., `BluetoothGattCallback`, `BluetoothSocket`, managers, repositories/viewmodels)
- diagrams of data flow (UI → Bluetooth layer → ESP32 and back)
- exact code examples that match your implementation



Copilot said: The `bt_def` module in your repo is
The bt_def module in your repo is a reusable Bluetooth “helper/library” Android module that provides:

a small UI flow for listing/choosing devices, and
the core connection logic (thread that opens a Bluetooth socket), plus
supporting model/adapter/utilities/constants.
Below is a detailed breakdown of what’s inside bt_def, how the pieces fit together, and how it works inside your Android app.

1) What bt_def contains (inventory)
Gradle / Android plumbing
bt_def/build.gradle.kts — config for this module (it is its own Android module).
bt_def/src/main/AndroidManifest.xml — module manifest (declares permissions/features used by the module, and/or merges into the app manifest).
Proguard files: consumer-rules.pro, proguard-rules.pro
Kotlin source (library code)
Package: bt_def/src/main/java/com/example/bt_def/

BaseActivity.kt
BluetoothConstants.kt
DeviceListFragment.kt
Extensions.kt
ItemAdapter.kt
ListItem.kt
bluetooth/
BluetoothController.kt
ConnectThread.kt
UI resources used by the module
bt_def/src/main/res/layout/

activity_base.xml
fragment_list.xml
list_item.xml
2) Architecture: how the module is structured
bt_def is essentially split into two layers:

A) UI / “device picker” layer
DeviceListFragment + ItemAdapter + ListItem + layouts

This layer is responsible for:

presenting a list UI (RecyclerView-style) based on fragment_list.xml
rendering each row using list_item.xml
providing a mechanism for the user to choose a Bluetooth device (typically a paired device or discovered device list)
passing the user’s selection onward (to your app’s screen) so the app can connect
B) Bluetooth connection layer
BluetoothController + ConnectThread + BluetoothConstants

This layer is responsible for:

holding Bluetooth constants needed for connection (e.g., UUID/name keys/request codes)
initiating and managing the connection attempt
doing the blocking socket connect work off the main thread (ConnectThread)
From file names and typical patterns, this looks like Classic Bluetooth SPP-style socket connection (the presence of ConnectThread strongly suggests you call BluetoothSocket.connect() in a background thread). If it were BLE, you’d usually see BluetoothGatt, callbacks, characteristic UUIDs, etc.

3) Detailed file-by-file logic (what each file does in the system)
BluetoothConstants.kt
Role: shared constants for the module.

Typical contents (based on common Android Bluetooth patterns):

a fixed UUID for the service to connect to (for SPP often 00001101-0000-1000-8000-00805F9B34FB, or your custom one)
keys for bundling selected device address/name between Fragment/Activity (e.g., EXTRA_DEVICE_ADDRESS)
request codes for startActivityForResult / permission flows (if used)
How it affects the app:

Ensures your app and the module agree on “what key name” carries the MAC address, and what UUID to use when opening a socket.
ConnectThread.kt
Role: does the actual Bluetooth socket connection on a background thread.

Core logic (what it “actually does”):

Receives a target BluetoothDevice (and likely a UUID).
Creates a BluetoothSocket (usually via device.createRfcommSocketToServiceRecord(uuid)).
Calls bluetoothAdapter.cancelDiscovery() before connecting (important because discovery slows connection).
Calls socket.connect() (blocking call).
On success:
exposes the connected socket back to the rest of the module/app (via callback, handler, listener, or controller state).
On failure:
closes the socket and returns an error (again via callback/handler/state).
How it affects your app:

This is the piece that turns a chosen device into an actual connection attempt without freezing the UI thread.
Your MainFragment / main UI will only be able to send data after ConnectThread reports “connected” and you have streams.
What you usually do next (in the app layer):

Get socket.outputStream to write bytes/strings to ESP32.
Get socket.inputStream to read responses (often another “ConnectedThread” exists for continuous reads; in your module list I don’t see it, so either:
reads are done elsewhere (in your app module), or
the module currently only connects and doesn’t implement continuous read loop yet.
BluetoothController.kt
Role: higher-level coordinator around Bluetooth actions.

What it typically does in practice:

Holds a reference to BluetoothAdapter
Provides functions like:
“is Bluetooth supported / enabled?”
“get paired devices”
“connect(device)” → creates and starts ConnectThread
possibly “stop/cancel connection”
Acts as the boundary between UI (DeviceListFragment) and the thread (ConnectThread)
How it affects your app:

Your app can call controller methods (directly or indirectly through the fragment) instead of manually handling low-level socket creation everywhere.
It centralizes connection behavior so MainFragment or StartActivity can just trigger “connect”.
DeviceListFragment.kt
Role: device list screen that the user interacts with.

What it typically does:

Initializes Bluetooth adapter/controller.
Collects candidate devices:
often paired devices (bluetoothAdapter.bondedDevices)
possibly discovered devices via BroadcastReceiver (if implemented)
Builds a list of ListItem items.
Uses ItemAdapter to show items in a list.
Handles click:
when user taps a device row, it returns or forwards the selected device address/name.
may start connection immediately or simply return “selection” to the hosting Activity/Fragment.
How it works in your app:

Your app would either:
embed this fragment into one of your Activities, or
navigate to it, let the user pick a device, then navigate back with the result (device MAC), then connect in MainFragment.
It uses:

fragment_list.xml as its UI
ItemAdapter to render device rows
probably helper methods from Extensions.kt (for toasts, view visibility, etc.)
ItemAdapter.kt
Role: RecyclerView adapter for showing a list of items (Bluetooth devices) in the fragment.

What it does:

Takes a List<ListItem>
Binds each item to list_item.xml
Handles item click callback
How it affects your app:

Determines how devices appear in the list and what data is passed on tap.
ListItem.kt
Role: model for a row in the list.

What it likely contains:

title (device name or “Unknown device”)
subtitle (MAC address)
maybe BluetoothDevice reference or address string
Extensions.kt
Role: utility extension functions used across the module.

Typical examples:

Context.toast("...")
View.visible() / gone()
Fragment.requireBluetoothAdapter() helpers
string formatting helpers
How it affects your app:

Reduces boilerplate and makes UI/controller code in bt_def cleaner.
BaseActivity.kt + activity_base.xml
Role: a shared Activity base and its layout.

Why it exists:

The module likely provides a “container” Activity used to host DeviceListFragment (or any other bt_def UI).
activity_base.xml is probably just a FrameLayout container where fragments are swapped in.
How it affects your app:

Your app can start this base activity (or subclass it) to reuse the module’s device-selection UI.
4) How bt_def “actually works” with your app module
From your app module structure, the main UI is in:

app/src/main/java/com/example/esp32_valdymas/MainFragment.kt
MainActivity.kt
StartActivity.kt
The typical integration patterns are:

Pattern A: App launches device picker (bt_def), receives selected device, then connects
In StartActivity or MainFragment, you open DeviceListFragment (either directly or via an Activity that hosts it).
User selects a device.
You receive the device address/name.
You pass it to the Bluetooth layer (BluetoothController → ConnectThread) to connect.
Once connected, your main screen sends commands to ESP32.
Pattern B: bt_def both selects and connects, app only uses the socket/connected state
User selects device inside bt_def UI.
bt_def starts ConnectThread and connects.
bt_def returns “connected socket/session” (via shared singleton, callback, or controller reference).
Your main UI uses that connection to send data.
Which of these you’re using depends on how DeviceListFragment is wired to your MainFragment (navigation/result passing).

5) Practical “mental model” for debugging / extending bt_def
If connection fails:

ConnectThread is where to look first (socket creation, UUID mismatch, connect exceptions).
BluetoothConstants is where UUID/address keys would be defined.
DeviceListFragment is where you ensure you’re selecting the correct device (ESP32 name/MAC) and not trying to connect while discovering.
If device list is empty:

DeviceListFragment logic for retrieving paired/discovered devices is the key.
Also ensure required permissions are granted (manifest + runtime).
If UI doesn’t show:

activity_base.xml container and fragment transaction logic in BaseActivity/DeviceListFragment.

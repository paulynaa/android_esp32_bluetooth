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


# Android ESP32 Bluetooth Controller (Classic SPP)

This repository contains an Android application (Kotlin) that connects to an ESP32 over **Classic Bluetooth (RFCOMM / SPP)**, lets the user select/pair a device, connect/disconnect, and send simple messages (currently the app sends `"A"`). The Bluetooth functionality is split into a reusable module `bt_def` that provides both UI (device list) and the underlying connection thread/controller.

---

## Table of Contents

- [Project Overview](#project-overview)
- [Architecture](#architecture)
  - [Modules](#modules)
  - [Runtime Flow](#runtime-flow)
  - [Bluetooth Transport](#bluetooth-transport)
- [File-by-File Breakdown](#file-by-file-breakdown)
  - [Root](#root)
  - [`app` module](#app-module)
  - [`bt_def` module](#bt_def-module)
- [Key Functions / Logic Explained](#key-functions--logic-explained)
  - [Device selection & pairing flow](#device-selection--pairing-flow)
  - [Saving and using the selected MAC address](#saving-and-using-the-selected-mac-address)
  - [Connecting and message I/O](#connecting-and-message-io)
- [Setup Instructions](#setup-instructions)
  - [Prerequisites](#prerequisites)
  - [Build & Run](#build--run)
  - [Permissions Notes (Android 12+)](#permissions-notes-android-12)
- [Usage Examples](#usage-examples)
  - [Pick a device and connect](#pick-a-device-and-connect)
  - [Send a command to ESP32](#send-a-command-to-esp32)
- [Known Issues / Improvements](#known-issues--improvements)

---

## Project Overview

**Goal:** Control or communicate with an ESP32 device from Android using Bluetooth Classic.

**What it currently does:**
- Shows a device list UI (`DeviceListFragment` from `bt_def`)
  - Shows **paired devices**
  - Discovers nearby devices and allows **bonding/pairing**
  - Lets you choose a paired device (checkbox selection)
  - Stores the chosen device **MAC address** in SharedPreferences
- Provides a connection API (`BluetoothController`) to:
  - connect to the device using the stored MAC
  - send a message (writes to the socket OutputStream)
  - read incoming messages in a loop (reads from InputStream and notifies a listener)

---

## Architecture

### Modules

This is a multi-module Gradle project:

- **`app/`** — the main Android application UI and entry points.
- **`bt_def/`** — a reusable “Bluetooth definitions + device picker + connector” module:
  - Device list UI and pairing
  - SharedPreferences keys/constants
  - Bluetooth connection controller + background thread that manages socket I/O

### Runtime Flow

High-level runtime behavior (as implemented today):

1. **Launcher Activity:** `StartActivity` (in `app`)
2. The app then navigates into `MainActivity` (in `app`)
3. `MainActivity` immediately launches `com.example.bt_def.BaseActivity` (in `bt_def`) and finishes itself.
4. `BaseActivity` hosts `DeviceListFragment` which:
   - requests permissions (if needed)
   - allows enabling Bluetooth
   - shows paired devices
   - can discover devices and pair them
   - saves the selected MAC address into SharedPreferences

Separately (in `MainFragment`), the app has a UI to:
- connect using the saved MAC
- send a message `"A"`

> Note: In the current code you provided, `MainActivity` always launches `BaseActivity`, so the `MainFragment` flow may not be reachable unless there is additional navigation/activities not shown here. The Bluetooth “connect/send” logic is implemented and ready to use from `MainFragment`, but the app’s current entry sequence prioritizes the device list activity.

### Bluetooth Transport

The connection uses this UUID:

- `00001101-0000-1000-8000-00805F9B34FB`

That UUID corresponds to the common Bluetooth Classic **SPP (Serial Port Profile)** UUID used by many ESP32 Classic Bluetooth serial implementations.

The actual transport used is:
- `BluetoothDevice.createRfcommSocketToServiceRecord(UUID)`
- `BluetoothSocket.connect()`
- read from `BluetoothSocket.inputStream`
- write to `BluetoothSocket.outputStream`

---

## File-by-file Breakdown

### Root

- `build.gradle.kts` — top-level Gradle configuration.
- `settings.gradle.kts` — includes modules (`app`, `bt_def`).
- `gradle.properties` — Gradle/Android properties.
- `gradlew`, `gradlew.bat`, `gradle/` — Gradle wrapper.

---

## `app` module

#### `app/src/main/AndroidManifest.xml`
- Declares the app’s launcher activity `StartActivity`.
- Also registers `com.example.bt_def.BaseActivity` with a theme override.

#### `app/src/main/java/com/example/esp32_valdymas/StartActivity.kt`
- The **launcher** activity.
- Currently only inflates `ContentStartBinding` and sets the content view.

#### `app/src/main/java/com/example/esp32_valdymas/MainActivity.kt`
- A thin activity that immediately starts `BaseActivity` from the `bt_def` module:
  - `startActivity(Intent(this, BaseActivity::class.java))`
  - `finish()`

#### `app/src/main/java/com/example/esp32_valdymas/MainFragment.kt`
Implements `BluetoothController.Listener` and is designed to:
- read saved MAC from SharedPreferences (`BluetoothConstants.PREFERENCES`, `BluetoothConstants.MAC`)
- create `BluetoothController(btAdapter)`
- connect via `bluetoothController.connect(mac, this)`
- send `"A"` via `bluetoothController.sendMessage("A")`
- update UI based on callbacks in `onReceive(message: String)`

---

## `bt_def` module

#### `bt_def/src/main/AndroidManifest.xml`
Declares the Bluetooth-related permissions (merged into the final app manifest during build), including:
- `BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN`
- legacy `BLUETOOTH`, `BLUETOOTH_ADMIN`
- `ACCESS_COARSE_LOCATION`, `ACCESS_FINE_LOCATION`

Also registers:
- `BaseActivity`

#### `bt_def/src/main/java/com/example/bt_def/BaseActivity.kt`
- Hosts `DeviceListFragment` in `activity_base` layout:
  - Replaces `R.id.placeHolder` with `DeviceListFragment()`

#### `bt_def/src/main/java/com/example/bt_def/BluetoothConstants.kt`
- SharedPreferences constants:
  - `PREFERENCES = "main_preferences"`
  - `MAC = "mac"`

#### `bt_def/src/main/java/com/example/bt_def/DeviceListFragment.kt`
- The core device list / permission / discovery UI.
- Responsibilities:
  - Initialize Bluetooth adapter via `BluetoothManager`
  - Request permissions (using Activity Result API)
  - Show paired devices
  - Start discovery (scan)
  - Register BroadcastReceiver for discovery events
  - Allow bonding/pairing for discovered devices
  - Save selected device MAC to SharedPreferences on click

#### `bt_def/src/main/java/com/example/bt_def/Extensions.kt`
- Utility extension functions used by fragments:
  - `changeButtonColor(button, color)`
  - `checkBtPermissions()` (Android 12+ checks `BLUETOOTH_CONNECT` + location; older checks location)

#### `bt_def/src/main/java/com/example/bt_def/ItemAdapter.kt`
- RecyclerView `ListAdapter<ListItem, ...>`
- Used for:
  - **paired devices list** (checkbox selection visible)
  - **discovered devices list** (checkbox hidden; tapping triggers `createBond()`)

#### `bt_def/src/main/java/com/example/bt_def/ListItem.kt`
- Data model: `(device: BluetoothDevice, isChecked: Boolean)`

#### `bt_def/src/main/java/com/example/bt_def/bluetooth/BluetoothController.kt`
- The API layer used by the app:
  - `connect(mac, listener)`
  - `sendMessage(message)`
  - `closeConnection()`
- Owns a single `ConnectThread`.

#### `bt_def/src/main/java/com/example/bt_def/bluetooth/ConnectThread.kt`
- Background thread that:
  - creates an RFCOMM socket
  - calls `connect()`
  - notifies `listener.onReceive(BLUETOOTH_CONNECTED)`
  - continuously reads from `inputStream` and forwards strings to the listener
  - provides `sendMessage()` to write to output stream

---

## Key Functions / Logic Explained

### Device selection & pairing flow

#### `DeviceListFragment.onViewCreated(...)`
This wires up the UI and system components:
- Reads preferences:
  - `preferences = activity?.getSharedPreferences(PREFERENCES, MODE_PRIVATE)`
- Bluetooth enable button:
  - calls `btLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))`
- Search button:
  - `bAdapter?.startDiscovery()`
  - hides search icon, shows progress bar
- Registers broadcast receivers (`intentFilters()`)
- Checks permissions (`checkPermissions()`)
- Initializes RecyclerViews (`initRcViews()`)
- Registers the enable-Bluetooth activity result (`registerBtLauncher()`)
- Initializes Bluetooth adapter (`initBtAdapter()`)
- Updates UI based on current Bluetooth state (`bluetoothState()`)

#### BroadcastReceiver: `bReceiver`
Handles discovery/bonding lifecycle:
- `ACTION_FOUND`:
  - gets `BluetoothDevice` from `EXTRA_DEVICE`
  - adds it to the discovery adapter list (deduplicated via `mutableSetOf`)
  - updates “empty search” label visibility
- `ACTION_BOND_STATE_CHANGED`:
  - refreshes paired list via `getPairedDevices()`
- `ACTION_DISCOVERY_FINISHED`:
  - restores search icon, hides progress bar

#### Pairing behavior (in `ItemAdapter`)
- For the discovery list (`adapterType == true`):
  - tapping a device row calls `device.createBond()`
- For paired list (`adapterType == false`):
  - tapping checks the checkbox and calls the listener `onClick(item)`

### Saving and using the selected MAC address

#### `DeviceListFragment.saveMac(mac: String)`
Stores the chosen address:
- `preferences.edit().putString(MAC, mac).apply()`

#### `DeviceListFragment.getPairedDevices()`
Builds the paired list and marks the selected item:
- reads `bondedDevices`
- for each device, sets `isChecked = (savedMac == device.address)`
- submits list to `itemAdapter`

#### `MainFragment` usage of saved MAC
- Reads:
  - `mac = pref.getString(MAC, "")`
- Uses it on connect button:
  - `bluetoothController.connect(mac ?: "", this)`

### Connecting and message I/O

#### `BluetoothController.connect(mac, listener)`
- Validates: Bluetooth enabled + non-empty MAC
- Looks up remote device:
  - `adapter.getRemoteDevice(mac)`
- Starts `ConnectThread(device, listener)`

#### `ConnectThread.run()`
- Attempts:
  - `mSocket?.connect()`
- On success:
  - `listener.onReceive(BLUETOOTH_CONNECTED)`
  - calls `readMessage()` (infinite loop)
- On failure:
  - `listener.onReceive(BLUETOOTH_NO_CONNECTED)`

#### `ConnectThread.readMessage()`
- Infinite loop:
  - reads up to 256 bytes from `inputStream`
  - converts to String and calls `listener.onReceive(message)`
- On IOException:
  - notifies not connected and breaks loop

#### `ConnectThread.sendMessage(message)`
- `outputStream.write(message.toByteArray())`

#### `MainFragment.onReceive(message)`
Runs on UI thread and updates UI:
- If message is `BLUETOOTH_CONNECTED`:
  - set button tint red and text `"Disconnect"`
- If message is `BLUETOOTH_NO_CONNECTED`:
  - set button tint green and text `"Connect"`
- Otherwise:
  - show received message in `tvStatus`

---

## Setup Instructions

### Prerequisites
- Android Studio (latest stable)
- Android SDK installed
- Physical Android device with Bluetooth (recommended)
- ESP32 firmware that exposes Classic Bluetooth SPP (RFCOMM) service compatible with SPP UUID

### Build & Run

Clone:
```bash
git clone https://github.com/paulynaa/android_esp32_bluetooth.git
cd android_esp32_bluetooth
```

Build debug APK:
```bash
./gradlew :app:assembleDebug
```

Install on a connected device:
```bash
./gradlew :app:installDebug
```

### Permissions Notes (Android 12+)
- Your module requests `BLUETOOTH_CONNECT` and uses it in permission checks.
- Discovery typically also requires `BLUETOOTH_SCAN` on Android 12+; your manifest includes it, but `launchBtPermissions()` currently requests:
  - `BLUETOOTH_CONNECT`
  - `ACCESS_FINE_LOCATION`
  - (it does **not** request `BLUETOOTH_SCAN`)

If discovery does not work on Android 12+, you may need to request `BLUETOOTH_SCAN` at runtime as well.

---

## Usage Examples

### Pick a device and connect

1. Launch the app.
2. In the device list screen:
   - Tap Bluetooth power icon to enable Bluetooth (if off).
   - Tap search icon to discover devices.
   - Tap a discovered device to pair (bond).
   - In paired devices list, tap the device to select it (checkbox).
   - This saves the MAC address to SharedPreferences.
3. In your connect UI (MainFragment), tap **Connect** to connect to the saved MAC.

### Send a command to ESP32
Once connected:
- Tap **Send** (currently hard-coded):
  - sends `"A"` over RFCOMM to ESP32:
    ```kotlin
    bluetoothController.sendMessage("A")
    ```

If ESP32 responds, the app displays the received message in `tvStatus`.

---

## Known Issues / Improvements

1. **`closeConnection()` bug in `ConnectThread`:**
   - Currently calls `mSocket?.connect()` instead of `mSocket?.close()`.
   - It should close the socket to disconnect.

2. **Runtime permission coverage:**
   - On Android 12+, discovery often requires requesting `BLUETOOTH_SCAN` runtime permission.

3. **Lifecycle cleanup:**
   - `DeviceListFragment` registers a receiver via `activity?.registerReceiver(...)` but does not unregister it in `onDestroyView()` / `onDestroy()`.
   - This can lead to leaks/crashes.

4. **Error handling & UI:**
   - Many `catch (SecurityException)` blocks are empty; adding user-facing messages helps debugging.

5. **Navigation consistency:**
   - `MainActivity` always launches `BaseActivity` and finishes; ensure this matches your intended UX if you want `MainFragment` to be used.

---

If you want, I can also generate an improved README section with a **sequence diagram** (device selection → save MAC → connect thread → read/write callbacks) and propose code fixes for the disconnect and permissions issues.

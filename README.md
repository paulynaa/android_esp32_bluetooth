# Android ESP32 Bluetooth Controller

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
Ateičiai: 
Dabar tu siunti "A" ir priimi bet ką kaip tekstą. Kitas žingsnis – susitarti, kad žinutės turės paprastą formatą, pvz.:

Komandos iš Android → ESP:

LED:1\n (įjungti)
LED:0\n (išjungti)
PWM:128\n (ryškumas)
REQ:TEMP\n (paprašyti temperatūros)
Atsakymai iš ESP → Android:

OK\n
TEMP:23.7\n
ERR:BAD_CMD\n
Kodėl tai svarbu: tada tavo UI galės aiškiai žinoti, ką rodyti, o ne tik „stringą“.

Minimaliai Android pusėje: priimtą tekstą parsinti su split(":") ir pagal pirmą dalį atnaujinti UI.


## `bt_def` modulis (Bluetooth pagalbinė biblioteka)

`bt_def` modulis šiame repozitorijoje yra **pakartotinai naudojamas Android Bluetooth „helper/library“ modulis**, kuris suteikia:

- paprastą **UI srautą įrenginių sąrašui peržiūrėti ir pasirinkti** (suporuotiems ir rastiems įrenginiams),
- pagrindinę **prisijungimo logiką** (gija, kuri atidaro `BluetoothSocket` ir vykdo prisijungimą fone),
- papildomus **modelius / adapterius / utilitus / konstantas**, reikalingus UI ir ryšiui.

---

### 1) Kas yra `bt_def` modulyje (inventorius)

#### Gradle / Android konfigūracija
- `bt_def/build.gradle.kts` – šio modulio konfiguracija (tai atskiras Android modulis).
- `bt_def/src/main/AndroidManifest.xml` – modulio manifestas (aprašo reikalingus leidimus ir sujungiamas su pagrindiniu programėlės manifestu).
- Proguard failai: `consumer-rules.pro`, `proguard-rules.pro`.

#### Kotlin išeities kodas (bibliotekos logika)
**Paketas:** `bt_def/src/main/java/com/example/bt_def/`

- `BaseActivity.kt`
- `BluetoothConstants.kt`
- `DeviceListFragment.kt`
- `Extensions.kt`
- `ItemAdapter.kt`
- `ListItem.kt`
- `bluetooth/`
  - `BluetoothController.kt`
  - `ConnectThread.kt`

#### UI resursai, naudojami modulyje
`bt_def/src/main/res/layout/`

- `activity_base.xml`
- `fragment_list.xml`
- `list_item.xml`

---

### 2) Architektūra: kaip sudarytas modulis

`bt_def` modulis iš esmės padalintas į du sluoksnius:

#### A) UI / „įrenginio pasirinkimo“ sluoksnis
**`DeviceListFragment` + `ItemAdapter` + `ListItem` + XML layout’ai**

Šis sluoksnis atsakingas už:
- sąrašo UI atvaizdavimą (RecyclerView principu) pagal `fragment_list.xml`,
- kiekvienos eilutės atvaizdavimą pagal `list_item.xml`,
- mechanizmą vartotojui pasirinkti Bluetooth įrenginį (dažniausiai iš suporuotų arba rastų per paiešką),
- vartotojo pasirinkimo perdavimą tolimesniam programėlės veikimui (kad vėliau būtų galima prisijungti).

#### B) Bluetooth prisijungimo sluoksnis
**`BluetoothController` + `ConnectThread` + `BluetoothConstants`**

Šis sluoksnis atsakingas už:
- Bluetooth konstantų ir raktų laikymą (pvz., MAC adreso saugojimo raktas, kiti nustatymai),
- prisijungimo bandymo inicijavimą ir valdymą,
- „blokuojančio“ `BluetoothSocket.connect()` vykdymą **ne UI gijoje**, o fone (per `ConnectThread`).

> Pagal turimą kodą tai yra **Classic Bluetooth SPP (RFCOMM)** tipo prisijungimas (naudojamas RFCOMM lizdas ir SPP UUID). Jei tai būtų BLE, dažniausiai matytum `BluetoothGatt`, callback’us, characteristic UUID ir pan.

---

### 3) Detali logika pagal failus (ką daro kiekvienas failas)

#### `BluetoothConstants.kt`
**Paskirtis:** bendros modulio konstantos.

Praktinis efektas:
- užtikrina, kad ir `bt_def`, ir `app` modulis naudotų tuos pačius SharedPreferences pavadinimus ir raktus (pvz., MAC adreso saugojimui).

---

#### `ConnectThread.kt`
**Paskirtis:** realus prisijungimas prie Bluetooth įrenginio fone (atskiroje gijoje) ir I/O (skaitymas/rašymas).

Pagrindinė logika:
1. Iš `BluetoothDevice` sukuria `BluetoothSocket` per `createRfcommSocketToServiceRecord(UUID)`.
2. `run()` metode bando prisijungti:
   - `mSocket?.connect()` (blokuojantis iškvietimas).
3. Jei pavyksta:
   - praneša listener’iui `Bluetooth Connected`,
   - pradeda skaityti įeinančias žinutes cikle (`readMessage()`).
4. Jei nepavyksta:
   - praneša listener’iui `Not Connected`.

`readMessage()`:
- nuolat skaito iš `mSocket.inputStream` į bufferį,
- paverčia gautus baitus į `String`,
- perduoda tekstą į `listener.onReceive(...)`.

`sendMessage(message)`:
- rašo į `mSocket.outputStream` (siunčia duomenis į ESP32/Arduino).

Praktinis efektas programėlei:
- būtent šis failas „paverčia“ pasirinktą MAC adresą į realų ryšį ir įgalina siuntimą/priėmimą neblokuojant UI.

---

#### `BluetoothController.kt`
**Paskirtis:** aukštesnio lygio valdiklis, apjungiantis Bluetooth veiksmus.

Ką daro:
- turi `BluetoothAdapter` nuorodą,
- pagal MAC adresą susiranda `BluetoothDevice` (`adapter.getRemoteDevice(mac)`),
- sukuria `ConnectThread` ir paleidžia ją,
- pateikia patogius metodus:
  - `connect(mac, listener)`
  - `sendMessage(message)`
  - `closeConnection()`

Praktinis efektas:
- `app` modulis gali kviesti `BluetoothController` metodus, o ne tiesiogiai dirbti su `BluetoothSocket` kiekvienoje vietoje.

---

#### `DeviceListFragment.kt`
**Paskirtis:** ekrano dalis, kur vartotojas:
- įjungia Bluetooth,
- paleidžia paiešką (discovery),
- mato suporuotus/rastus įrenginius,
- pasirenka įrenginį ir išsaugo jo MAC adresą.

Pagrindiniai veiksmai:
- inicializuoja `BluetoothAdapter` per `BluetoothManager`,
- sukonfigūruoja sąrašus (paired ir discovered) su `ItemAdapter`,
- paleidžia `startDiscovery()` ir rodo paieškos progresą,
- naudoja `BroadcastReceiver`, kad:
  - gautų `ACTION_FOUND` ir pildytų rastų įrenginių sąrašą,
  - gautų `ACTION_BOND_STATE_CHANGED` ir atnaujintų suporuotų įrenginių sąrašą,
  - gautų `ACTION_DISCOVERY_FINISHED` ir grąžintų UI būseną.

Pasirinkimo logika:
- kai vartotojas paspaudžia ant suporuoto įrenginio, išsaugomas jo MAC į SharedPreferences (`saveMac(...)`).

---

#### `ItemAdapter.kt`
**Paskirtis:** RecyclerView adapteris Bluetooth įrenginių atvaizdavimui sąraše.

Ką daro:
- priima `List<ListItem>`,
- sujungia kiekvieną elementą su `list_item.xml`,
- apdoroja paspaudimus:
  - discovery sąraše: paspaudus ant įrenginio bandoma `createBond()` (suporavimas),
  - paired sąraše: leidžia pasirinkti įrenginį (checkbox) ir informuoti `Listener`.

---

#### `ListItem.kt`
**Paskirtis:** vienos sąrašo eilutės modelis.

Šiuo metu turi:
- `device: BluetoothDevice`
- `isChecked: Boolean` (ar tai šiuo metu pasirinktas įrenginys)

---

#### `Extensions.kt`
**Paskirtis:** pagalbinės `Fragment` extension funkcijos.

Pvz.:
- `changeButtonColor(...)` – pakeičia ImageButton ikonėlės spalvą.
- `checkBtPermissions()` – patikrina ar suteikti reikalingi leidimai (skiriasi pagal Android versiją).

Praktinis efektas:
- sumažina „boilerplate“ kodą fragmentuose ir leidžia logiką rašyti aiškiau.

---

#### `BaseActivity.kt` + `activity_base.xml`
**Paskirtis:** „konteinerinė“ Activity, kuri talpina `DeviceListFragment`.

Kaip veikia:
- `BaseActivity` nustato `activity_base.xml`,
- į `placeHolder` įdeda `DeviceListFragment`.

Praktinis efektas:
- `app` modulis gali tiesiog paleisti `BaseActivity`, kad vartotojas galėtų pasirinkti įrenginį iš sąrašo.

---

### 4) Kaip `bt_def` realiai integruojasi su `app` moduliu

Pagrindinės vietos `app` modulyje:
- `MainFragment.kt`
- `MainActivity.kt`
- `StartActivity.kt`

Tipinis scenarijus:
1. Vartotojas per `bt_def` UI (DeviceListFragment) pasirenka/suporuoja įrenginį.
2. MAC adresas išsaugomas į SharedPreferences (`BluetoothConstants.MAC`).
3. `MainFragment` perskaito išsaugotą MAC ir kviečia:
   - `BluetoothController.connect(mac, listener)`
4. Prisijungus:
   - `sendMessage(...)` siunčia duomenis į ESP/Arduino,
   - `readMessage()` grąžina atsakymus atgal į UI per `listener.onReceive(...)`.

---

### 5) Praktinis „mental model“ (debug / plėtra)

Jei **nepavyksta prisijungti**:
- pirmiausia tikrinti `ConnectThread` (UUID, `connect()` klaidos, `SecurityException`, įrenginio pasiekiamumas),
- tikrinti, ar MAC teisingas ir ar įrenginys suporuotas,
- tikrinti leidimus (ypač Android 12+).

Jei **įrenginių sąrašas tuščias / paieška neranda**:
- tikrinti `DeviceListFragment` discovery logiką ir BroadcastReceiver,
- tikrinti, ar suteikti runtime leidimai ir ar įjungtas Bluetooth.

Jei **UI nerodomas**:
- tikrinti `activity_base.xml` konteinerį (`placeHolder`) ir fragmento įdėjimą `BaseActivity` viduje.


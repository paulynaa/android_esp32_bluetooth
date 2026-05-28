Arduino
#include "BluetoothSerial.h"
#include <ESP32Servo.h>

BluetoothSerial SerialBT;

#define LED_PIN     2
#define LM35_PIN    34
#define RGB_R_PIN   27
#define RGB_G_PIN   26
#define RGB_B_PIN   25
#define BUZZER_PIN  33
#define SERVO_PIN   23
#define BUTTON_PIN  12

Servo myServo;

bool blinkEnabled    = false;
int  blinkInterval   = 500;
unsigned long lastBlink = 0;
bool ledState        = false;

bool alarmEnabled    = false;
int  alarmPhase      = 0;
unsigned long lastAlarm = 0;
const int ALARM_TONES[] = {2000, 1000};
const int ALARM_DUR     = 200;

// Telemetry throttle
unsigned long lastTelemetry = 0;
const int TELEMETRY_MS = 500;

// Temperature
float smoothedChipTemp = 0.0f;
float smoothedLm35Temp = 0.0f;
bool tempInitialized   = false;
unsigned long lastTempRead = 0;
const int TEMP_READ_MS = 300;
const float ALPHA = 0.2f;

bool buttonLastState   = HIGH;
unsigned long lastDebounce = 0;

String cmdBuffer = "";

void setup() {
  Serial.begin(115200);
  SerialBT.begin("ESP32");

  pinMode(LED_PIN, OUTPUT);
  pinMode(BUZZER_PIN, OUTPUT);
  pinMode(BUTTON_PIN, INPUT_PULLUP);

  ledcSetup(0, 5000, 8); ledcAttachPin(RGB_R_PIN, 0);
  ledcSetup(1, 5000, 8); ledcAttachPin(RGB_G_PIN, 1);
  ledcSetup(2, 5000, 8); ledcAttachPin(RGB_B_PIN, 2);

  ledcSetup(3, 2000, 8);
  ledcAttachPin(BUZZER_PIN, 3);

  myServo.attach(SERVO_PIN);
  myServo.write(90);

  smoothedChipTemp = temperatureRead();
  float v = analogRead(LM35_PIN) * (3.3f / 4095.0f);
  smoothedLm35Temp = v * 100.0f;
  tempInitialized  = true;

  Serial.println("ESP32 ready.");
}

void loop() {
  readBluetooth();
  updateBlink();
  updateAlarm();
  updateTemperature();
  updateTelemetry();
  updateButton();
}

void readBluetooth() {
  while (SerialBT.available()) {
    char c = (char)SerialBT.read();
    if (c == '\n') {
      cmdBuffer.trim();
      if (cmdBuffer.length() > 0) {
        handleCommand(cmdBuffer);
        cmdBuffer = "";
      }
    } else if (c != '\r') {
      cmdBuffer += c;
      if (cmdBuffer.length() > 64) cmdBuffer = "";
    }
  }
  while (Serial.available()) {
    char c = (char)Serial.read();
    SerialBT.write(c);
  }
}

void handleCommand(String cmd) {
  Serial.println("CMD: " + cmd);

  if (cmd == "LED1:1") {
    blinkEnabled = false;
    digitalWrite(LED_PIN, HIGH);
    SerialBT.println("OK");

  } else if (cmd == "LED1:0") {
    blinkEnabled = false;
    digitalWrite(LED_PIN, LOW);
    SerialBT.println("OK");

  } else if (cmd == "LED1:BLINK") {
    blinkEnabled = true;
    SerialBT.println("OK");

  } else if (cmd == "LED1:BLINK_STOP") {
    blinkEnabled = false;
    digitalWrite(LED_PIN, LOW);
    SerialBT.println("OK");

  } else if (cmd.startsWith("BLINK_SPEED:")) {
    int speed = constrain(cmd.substring(12).toInt(), 1, 10);
    blinkInterval = map(speed, 1, 10, 1000, 100);
    SerialBT.println("OK");

  } else if (cmd == "RGB:RED") {
    ledcWrite(0, 255); ledcWrite(1, 0); ledcWrite(2, 0);
    SerialBT.println("OK");

  } else if (cmd == "RGB:GREEN") {
    ledcWrite(0, 0); ledcWrite(1, 255); ledcWrite(2, 0);
    SerialBT.println("OK");

  } else if (cmd == "RGB:BLUE") {
    ledcWrite(0, 0); ledcWrite(1, 0); ledcWrite(2, 255);
    SerialBT.println("OK");

  } else if (cmd == "RGB:OFF") {
    ledcWrite(0, 0); ledcWrite(1, 0); ledcWrite(2, 0);
    SerialBT.println("OK");

  } else if (cmd == "BUZZ:1") {
    alarmEnabled = false;
    ledcWriteTone(3, 1000);
    delay(300);
    ledcWriteTone(3, 0);
    SerialBT.println("OK");

  } else if (cmd == "BUZZ:ALARM") {
    alarmEnabled = true;
    alarmPhase = 0;
    lastAlarm = millis();
    SerialBT.println("OK");

  } else if (cmd == "BUZZ:STOP") {
    alarmEnabled = false;
    ledcWriteTone(3, 0);
    SerialBT.println("OK");

  } else if (cmd.startsWith("SERVO:")) {
    int angle = constrain(cmd.substring(6).toInt(), 0, 180);
    myServo.write(angle);
    SerialBT.println("OK");

  } else if (cmd == "REQ:MEM") {
    uint32_t freeH  = ESP.getFreeHeap();
    uint32_t totalH = ESP.getHeapSize();
    uint32_t usedH  = totalH - freeH;
    SerialBT.println("MEM:" + String(freeH) + "," + String(usedH) + "," + String(totalH));

  } else {
    SerialBT.println("ERR:BAD_CMD");
  }
}

void updateTemperature() {
  if (millis() - lastTempRead < TEMP_READ_MS) return;
  lastTempRead = millis();

  float chip = temperatureRead();
  float v    = analogRead(LM35_PIN) * (3.3f / 4095.0f);
  float lm35 = v * 100.0f;

  if (!tempInitialized) {
    smoothedChipTemp = chip;
    smoothedLm35Temp = lm35;
    tempInitialized  = true;
  } else {
    smoothedChipTemp = ALPHA * chip + (1.0f - ALPHA) * smoothedChipTemp;
    smoothedLm35Temp = ALPHA * lm35 + (1.0f - ALPHA) * smoothedLm35Temp;
  }
}

void updateBlink() {
  if (!blinkEnabled) return;
  if (millis() - lastBlink >= (unsigned long)blinkInterval) {
    lastBlink = millis();
    ledState  = !ledState;
    digitalWrite(LED_PIN, ledState ? HIGH : LOW);
  }
}

void updateAlarm() {
  if (!alarmEnabled) return;
  if (millis() - lastAlarm >= ALARM_DUR) {
    lastAlarm  = millis();
    alarmPhase = (alarmPhase + 1) % 4;
    if (alarmPhase < 2) {
      ledcWriteTone(3, ALARM_TONES[alarmPhase % 2]);
    } else {
      ledcWriteTone(3, 0);
    }
  }
}

void updateTelemetry() {
  if (!SerialBT.hasClient()) return;
  if (millis() - lastTelemetry < TELEMETRY_MS) return;
  lastTelemetry = millis();

  unsigned long ms = millis();
  unsigned long s  = ms / 1000;
  unsigned long m  = s  / 60; s  %= 60;
  unsigned long h  = m  / 60; m  %= 60;
  unsigned long d  = h  / 24; h  %= 24;
  SerialBT.println("UPTIME:" + String(d) + "d," + String(h) + "h," + String(m) + "m," + String(s) + "s");

  SerialBT.println("CHIP_TEMP:" + String(smoothedChipTemp, 1));
  SerialBT.println("LM35_TEMP:" + String(smoothedLm35Temp, 1));
}

void updateButton() {
  bool state = digitalRead(BUTTON_PIN);
  if (state != buttonLastState && millis() - lastDebounce > 50) {
    lastDebounce  = millis();
    if (state == LOW) SerialBT.println("BTN:PRESSED");
    buttonLastState = state;
  }
}

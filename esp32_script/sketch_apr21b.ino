#include <BLEDevice.h>
#include <BLEAdvertising.h>

//checkpoint azonosito
#define NODE_ID "n4"

BLEAdvertising* pAdvertising;

void setup(){
  Serial.begin(115200);
  Serial.println("BLE Checkpoint indul...");

  BLEDevice::init(NODE_ID);
  pAdvertising = BLEDevice::getAdvertising();

  BLEAdvertisementData advertisementData;
  advertisementData.setName(NODE_ID);
  pAdvertising->setAdvertisementData(advertisementData);
  pAdvertising->setMinInterval(100);
  pAdvertising->setMaxInterval(200);

  pAdvertising->start();
  Serial.println("Beacon fut! Node ID: " NODE_ID);
}

void loop() {
  delay(1000);
  Serial.println("Beacon aktív...");
}
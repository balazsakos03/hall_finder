# 🗺️ Hall Finder - Indoor Navigation App

Egy modern, Android alapú beltéri navigációs alkalmazás, amely valós idejű hardveres szenzorfúzióval (PDR), A* útvonalkereséssel és QR-kódos pozicionálással segíti a felhasználókat az épületen belüli tájékozódásban.

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?logo=jetpackcompose&logoColor=white)

<div align="center">
  <img src="link_a_qr_olvasorol.jpg" width="250">
  <img src="link_a_terkeprol.jpg" width="250">
  <img src="link_a_sotet_modrol.jpg" width="250">
</div>

## ✨ Fő Funkciók

* 🚶‍♂️ **Valós idejű lépéskövetés (PDR):** A beépített lépésérzékelő és a forgásvektor (iránytű) kombinálásával az app offline, GPS nélkül is milliméter pontosan követi a mozgást.
* 🧭 **Directional Map Matching:** Fejlett algoritmus, amely csak akkor regisztrálja a lépést a térképen, ha a felhasználó a kiszámolt útvonal irányába néz.
* 📷 **Beépített QR Pozicionálás:** Villámgyors kezdőpont-kalibráció az appba integrált Google ML Kit vonalkód-olvasóval.
* 🧠 **A* Útvonalkeresés:** Optimális útvonaltervezés a megadott csomópontok (Node-ok) és emeletek között.
* 🎥 **Dinamikus Kamerakövetés:** "Google Maps" stílusú navigáció – a térkép forog és követi a felhasználót, de gesztusokra automatikusan kikapcsol, hogy szabadon lehessen böngészni.
* 🧹 **Intelligens Útvonaltisztítás:** Haladás közben a már megtett útvonal automatikusan eltűnik a felhasználó mögött.
* 🌓 **Kétnyelvű & Témák:** Magyar és Angol lokalizáció, beépített Világos / Sötét mód.
* 📱 **Immersive Mode:** Zavaró navigációs sávok nélküli, teljes képernyős élmény.

## 🛠️ Technológiai Stack

A projekt a legmodernebb Android fejlesztési irányelveket követi:
* **Nyelv:** Kotlin
* **UI Framework:** Jetpack Compose (Material Design 3)
* **Kamera & ML:** CameraX API, Google ML Kit (Barcode Scanning)
* **Hardver integráció:** Android SensorManager (`TYPE_STEP_DETECTOR`, `TYPE_ROTATION_VECTOR`)
* **Animációk:** Compose Animation API (`Animatable`, `animateFloatAsState`)

## 🧠 Így működik a motorháztető alatt

A beltéri navigáció GPS hiányában a **Pedestrian Dead Reckoning (PDR)** módszerre épül:
1.  **QR Beolvasás:** A felhasználó beolvas egy fizikai QR kódot (pl. `n1`), ami leteszi a kezdőpontra.
2.  **Szenzorfúzió:** A rendszer másodpercenként többször ellenőrzi a felhasználó lépéseit és az eszköz dőlésszögét/irányát.
3.  **Szűrés:** A szoftver kiszűri a "zajt" (pl. egy helyben forgolódás, vagy eszközrázkódás), és csak a valós, megfelelő irányba történő haladást vezeti át a 2D-s Canvas térképre.

## 🚀 Telepítés és Futtatás

1. Klónozd a tárolót:
   ```bash
   git clone [https://github.com/felhasznaloneved/hall-finder.git](https://github.com/felhasznaloneved/hall-finder.git)
   ```
2. Nyisd meg a projektet Android Studio-ban.

3. Szinkronizáld a Gradle fájlokat.

4. Futtasd az alkalmazást egy fizikai Android eszközön (Emulátoron a lépésszámláló és a kamera funkciók korlátozottan működnek!).

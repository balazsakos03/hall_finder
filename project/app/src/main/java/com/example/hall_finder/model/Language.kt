package com.example.hall_finder.model

enum class AppLanguage(val code: String, val displayName: String) {
    HU("hu", "Magyar"),
    EN("en", "English")
}

object Translations {
    fun getDestinations(language: AppLanguage): List<Pair<String, String>> {
        return when (language) {
            AppLanguage.HU -> listOf(
                // 1. emelet
                "n7" to "Büfé", "n8" to "I. Iroda", "n9" to "II. Iroda",
                "n10" to "Titkárság", "n11" to "III. Iroda", "n12" to "IV. Iroda",
                "n13" to "II. Raktár", "n14" to "I. Raktár", "n15" to "Admin",
                "n16" to "Férfi mosdó (Fsz.)", "n17" to "Női mosdó (Fsz.)",
                "n20" to "Lift (Fsz.)",
                // 2. emelet
                "n27" to "Nagy Tárgyaló",
                "n30" to "Kis Tárgyaló",
                "n32" to "2. Büfé",
                "n34" to "V. Iroda",
                "n36" to "VI. Iroda",
                "n39" to "HR Osztály",
                "n40" to "Szerverszoba",
                "n43" to "Pihenő / Konyha",
                "n44" to "Nyomtató állomás",
                "n47" to "III. Raktár",
                "n49" to "Női mosdó (2. em.)",
                "n51" to "Vezetői Iroda",
                "n52" to "Férfi mosdó (2. em.)",
                "n23" to "Lift (2. em.)"
            )
            AppLanguage.EN -> listOf(
                // Ground floor
                "n7" to "Cafeteria", "n8" to "Office I", "n9" to "Office II",
                "n10" to "Secretariat", "n11" to "Office III", "n12" to "Office IV",
                "n13" to "Storage II", "n14" to "Storage I", "n15" to "Admin",
                "n16" to "Men's Restroom (F1)", "n17" to "Women's Restroom (F1)",
                "n20" to "Elevator (F1)",
                // 2nd floor
                "n27" to "Boardroom",
                "n30" to "Meeting Room",
                "n32" to "Cafeteria 2",
                "n34" to "Office V",
                "n36" to "Office VI",
                "n39" to "HR Department",
                "n40" to "Server Room",
                "n43" to "Lounge / Kitchen",
                "n44" to "Print Station",
                "n47" to "Storage III",
                "n49" to "Women's Restroom (F2)",
                "n51" to "Manager's Office",
                "n52" to "Men's Restroom (F2)",
                "n23" to "Elevator (F2)"
            )
        }
    }

    // QR képernyő szövegei
    fun qrTitle(lang: AppLanguage) =
        if (lang == AppLanguage.HU) "Keresse az önhöz\nlegközelebbi QR kódot"
        else "Find the nearest\nQR code to you"

    fun qrSubtitle(lang: AppLanguage) =
        if (lang == AppLanguage.HU) "Szkennelje be a folyosón elhelyezett\nQR kódot a navigáció elindításához"
        else "Scan the QR code placed in the hallway\nto start navigation"

    fun qrScanBtn(lang: AppLanguage) =
        if (lang == AppLanguage.HU) "QR kód szkennelése" else "Scan QR Code"

    fun qrDemoBtn(lang: AppLanguage) =
        if (lang == AppLanguage.HU) "Demo indítása (n1)" else "Start Demo (n1)"

    // Akadálymentesség
    fun accessibleLabel(lang: AppLanguage) =
        if (lang == AppLanguage.HU) "Akadálymentes útvonal" else "Accessible route"

    fun accessibleDescription(lang: AppLanguage) =
        if (lang == AppLanguage.HU) "Csak liftet használ, lépcsőket elkerüli"
        else "Uses elevator only, avoids stairs"

    // Térkép képernyő szövegei
    fun mapDestination(lang: AppLanguage) =
        if (lang == AppLanguage.HU) "Úti cél" else "Destination"

    fun mapSearchPlaceholder(lang: AppLanguage) =
        if (lang == AppLanguage.HU) "Keresés (pl. Iroda)" else "Search (e.g. Office)"

    fun mapNoResults(lang: AppLanguage, query: String) =
        if (lang == AppLanguage.HU) "Nincs találat erre: \"$query\""
        else "No results for: \"$query\""

    fun mapRecenter(lang: AppLanguage) =
        if (lang == AppLanguage.HU) "Középre igazítás" else "Re-center"

    fun mapAccessibleToggle(lang: AppLanguage) =
        if (lang == AppLanguage.HU) "Akadálymentes" else "Accessible"
}
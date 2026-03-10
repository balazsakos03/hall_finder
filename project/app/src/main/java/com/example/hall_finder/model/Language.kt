package com.example.hall_finder.model

//valaszthato nyelvek
enum class AppLanguage(val code: String, val displayName: String){
    HU("hu", "Magyar"),
    EN("en", "English")
}

//forditasokat tarolo objektum
object Translations{
    fun getDestinations(language: AppLanguage): List<Pair<String, String>>{
        return when (language){
            AppLanguage.HU -> listOf(
                "n7" to "Büfé", "n8" to "I. Iroda", "n9" to "II. Iroda",
                "n10" to "Titkárság", "n11" to "III. Iroda", "n12" to "IV. Iroda",
                "n13" to "II. Raktár", "n14" to "I. Raktár", "n15" to "Admin",
                "n16" to "Férfi mosdó", "n17" to "Női mosdó"
            )
            AppLanguage.EN -> listOf(
                "n7" to "Cafeteria", "n8" to "Office I", "n9" to "Office II",
                "n10" to "Secretariat", "n11" to "Office III", "n12" to "Office IV",
                "n13" to "Storage II", "n14" to "Storage I", "n15" to "Admin",
                "n16" to "Men's Restroom", "n17" to "Women's Restroom"
            )
        }
    }

    //QR kepernyo szovegei
    fun qrTitle(lang: AppLanguage) = if (lang == AppLanguage.HU) "Keresse az önhöz\nlegközelebbi QR kódot" else "Find the nearest\nQR code to you"
    fun qrSubtitle(lang: AppLanguage) = if (lang == AppLanguage.HU) "Szkennelje be a folyosón elhelyezett\nQR kódot a navigáció elindításához" else "Scan the QR code placed in the hallway\nto start navigation"
    fun qrScanBtn(lang: AppLanguage) = if (lang == AppLanguage.HU) "QR kód szkennelése" else "Scan QR Code"
    fun qrDemoBtn(lang: AppLanguage) = if (lang == AppLanguage.HU) "Demo indítása (n1)" else "Start Demo (n1)"

    //terkep kepernyo szovegei
    fun mapDestination(lang: AppLanguage) = if (lang == AppLanguage.HU) "Úti cél" else "Destination"
    fun mapSearchPlaceholder(lang: AppLanguage) = if (lang == AppLanguage.HU) "Keresés (pl. Iroda)" else "Search (e.g. Office)"
    fun mapNoResults(lang: AppLanguage, query: String) = if (lang == AppLanguage.HU) "Nincs találat erre: \"$query\"" else "No results for: \"$query\""
    fun mapRecenter(lang: AppLanguage) = if (lang == AppLanguage.HU) "Középre igazítás" else "Re-center"
}
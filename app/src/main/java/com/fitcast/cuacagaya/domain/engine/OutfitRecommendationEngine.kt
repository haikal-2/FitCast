package com.fitcast.cuacagaya.domain.engine

class OutfitRecommendationEngine {
    fun recommendOutfit(tempCelsius: Double, condition: String): String {
        return if (tempCelsius > 30.0) {
            "Kaos dan Celana Pendek"
        } else if (condition.lowercase().contains("rain")) {
            "Jaket Rainproof & Sweter"
        } else {
            "Kemeja Casual & Celana Panjang"
        }
    }
}

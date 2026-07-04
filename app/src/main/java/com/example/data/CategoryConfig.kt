package com.example.data

import androidx.compose.ui.graphics.Color

data class CategoryInfo(
    val name: String, 
    val icon: String, 
    val color: Color,
    val description: String = ""
)

object CategoryConfig {
    val categories = listOf(
        CategoryInfo("Khana", "🍽️", Color(0xFF8B5CF6), "Bahar ka khana, restaurant"),
        CategoryInfo("Ghar Kharch", "🏠", Color(0xFF14B8A6), "Groceries, sabzi, milk"),
        CategoryInfo("Rent/EMI", "🏡", Color(0xFF4F46E5), "Ghar ka kiraya ya loan"),
        CategoryInfo("Petrol", "⛽", Color(0xFFF59E0B), "Fuel for bike/car"),
        CategoryInfo("Safar", "🚗", Color(0xFF3B82F6), "Auto, bus, metro, taxi"),
        CategoryInfo("Masti", "🎮", Color(0xFFEC4899), "Movies, games, outing"),
        CategoryInfo("Shopping", "🛍️", Color(0xFF22C55E), "Kapde, shoes, electronics"),
        CategoryInfo("Health", "💊", Color(0xFFDC2626), "Doctor, medicine, hospital"),
        CategoryInfo("Padhai", "📚", Color(0xFF06B6D4), "Books, courses, fees"),
        CategoryInfo("Personal", "💇", Color(0xFF10B981), "Salon, gym, self care"),
        CategoryInfo("Gift", "🎁", Color(0xFFF43F5E), "Kisi ko diya"),
        CategoryInfo("Savings", "💰", Color(0xFF22C55E), "FD, investment, piggy bank"),
        CategoryInfo("Pooja", "🙏", Color(0xFFEAB308), "Mandir, donations, festivals"),
        CategoryInfo("Recharge", "📱", Color(0xFF0F172A), "Mobile, internet, DTH"),
        CategoryInfo("Other", "📦", Color(0xFFF97316), "Jo upar fit na ho")
    )

    fun getCategoryByName(name: String): CategoryInfo {
        return categories.firstOrNull { it.name.trim().lowercase() == name.trim().lowercase() }
            ?: categories.firstOrNull { name.trim().lowercase().contains(it.name.lowercase()) }
            ?: CategoryInfo("Other", "📦", Color(0xFFF97316), "Jo upar fit na ho")
    }
}

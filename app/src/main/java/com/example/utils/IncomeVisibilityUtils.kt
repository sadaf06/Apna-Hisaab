package com.example.utils

import java.security.MessageDigest
import java.text.NumberFormat
import java.util.Locale

object IncomeVisibilityUtils {
    
    fun maskAmount(amount: Double, isVisible: Boolean): String {
        return if (isVisible) {
            try {
                val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
                "₹" + formatter.format(amount.toInt())
            } catch (e: Exception) {
                "₹${amount.toInt()}"
            }
        } else {
            "₹ ••••"
        }
    }

    fun maskText(text: String, isVisible: Boolean): String {
        return if (isVisible) text else "••••••"
    }

    fun hashPin(pin: String): String {
        return try {
            val bytes = pin.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            digest.fold("") { str, it -> str + "%02x".format(it) }
        } catch (e: Exception) {
            pin // Fallback to raw if hashing fails somehow
        }
    }
}

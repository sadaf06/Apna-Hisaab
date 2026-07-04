package com.example.utils

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object IncomeVisibilityManager {
    
    private val _isIncomeVisible = MutableStateFlow(false)
    val isIncomeVisible: StateFlow<Boolean> = _isIncomeVisible.asStateFlow()

    private var cachedHashedPin: String? = null

    fun loadPin(userId: String) {
        if (userId.isEmpty()) return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("profile")
            .document("info")
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    cachedHashedPin = document.getString("incomePin")
                }
            }
    }

    fun isPinSet(): Boolean {
        return !cachedHashedPin.isNullOrEmpty()
    }

    fun setPin(userId: String, pin: String) {
        if (userId.isEmpty() || pin.isEmpty()) return
        val hashed = IncomeVisibilityUtils.hashPin(pin)
        cachedHashedPin = hashed
        val data = mapOf("incomePin" to hashed)
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("profile")
            .document("info")
            .set(data, SetOptions.merge())
    }

    fun clearPin(userId: String) {
        cachedHashedPin = null
        _isIncomeVisible.value = false
        if (userId.isEmpty()) return
        val data = mapOf("incomePin" to null)
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("profile")
            .document("info")
            .set(data, SetOptions.merge())
    }

    fun verifyPin(enteredPin: String): Boolean {
        if (enteredPin.isEmpty() || cachedHashedPin.isNullOrEmpty()) return false
        val hashedEntered = IncomeVisibilityUtils.hashPin(enteredPin)
        return hashedEntered == cachedHashedPin
    }

    fun toggleVisibility(context: Context, userId: String) {
        if (!isPinSet()) {
            PinDialogHelper.showSetPinDialog(context, userId) {
                _isIncomeVisible.value = true
            }
        } else {
            if (_isIncomeVisible.value) {
                // Already visible, hide it immediately (no PIN needed)
                _isIncomeVisible.value = false
            } else {
                // Hidden, require PIN verification to show
                PinDialogHelper.showEnterPinDialog(context) {
                    _isIncomeVisible.value = true
                }
            }
        }
    }

    fun hideOnBackground() {
        _isIncomeVisible.value = false
    }
}

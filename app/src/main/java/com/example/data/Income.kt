package com.example.data

data class Income(
    val id: String = "",
    val sourceName: String,
    val sourceType: String, // "Salary", "Freelance", "Business", "Investment", "Other"
    val amount: Double,
    val type: String, // "monthly" or "one-time"
    val createdAt: Long = System.currentTimeMillis()
)

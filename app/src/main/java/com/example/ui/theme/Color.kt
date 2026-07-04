package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme

// Semantic colour accessors backed by the (dark-only) Material colour scheme.
val AppPurple: Color
    @Composable
    get() = MaterialTheme.colorScheme.primary

val AppTeal: Color
    @Composable
    get() = MaterialTheme.colorScheme.secondary

val AppBackground: Color
    @Composable
    get() = MaterialTheme.colorScheme.background

val AppText: Color
    @Composable
    get() = MaterialTheme.colorScheme.onBackground

val AppTextLight: Color
    @Composable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

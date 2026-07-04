package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Goal
import com.example.ui.theme.Palette
import com.example.ui.theme.Dimens
import com.example.ui.theme.Shapes
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import com.example.viewmodel.ExpenseViewModel
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.focus.onFocusChanged
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalView
import android.view.ViewTreeObserver
import android.graphics.Rect
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

// ---- Refined Glass palette — thin aliases onto the single source of truth (Palette). ----
private val GMint = Palette.Teal
private val GMintDeep = Palette.TealDeep
private val GViolet = Palette.Purple
private val GCoral = Palette.Danger
private val GText = Palette.TextPrimary
private val GText2 = Palette.TextSecondary
private val GGold = Palette.Warning      // completed/reactivate accent
private val GInk = Palette.OnAccent      // dark text on bright accent fills

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SapnaScreen(viewModel: ExpenseViewModel) {
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    var showAddGoalSheet by remember { mutableStateOf(false) }
    var showAddSavingsSheet by remember { mutableStateOf<Goal?>(null) }
    var showEditGoalSheet by remember { mutableStateOf<Goal?>(null) }
    var showCelebration by remember { mutableStateOf<Goal?>(null) }
    var goalToDelete by remember { mutableStateOf<Goal?>(null) }

    val activeGoals = goals.filter { !it.isCompleted }
    val completedGoals = goals.filter { it.isCompleted }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .animateScreenEntrance()
                .padding(horizontal = Dimens.lg, vertical = Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.sectionGap),
            contentPadding = PaddingValues(bottom = 180.dp)
        ) {
            // 1. HEADER — glass card, no hard gradient
            item {
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateCardStagger(0)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Mera Sapna",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = GText
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Har sapna, ek kadam se shuru hota hai",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = GText2
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 2. ACTIVE GOALS or EMPTY STATE
            item {
                Text(
                    text = "Active Sapne",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = GMint
                    )
                )
            }
            if (activeGoals.isNotEmpty()) {
                itemsIndexed(activeGoals) { index, goal ->
                    Box(modifier = Modifier.animateCardStagger(index + 1)) {
                        GoalCard(
                            goal = goal,
                            onAddSavings = { showAddSavingsSheet = goal },
                            onEdit = { showEditGoalSheet = goal },
                            onDelete = { goalToDelete = goal }
                        )
                    }
                }
                if (activeGoals.size == 1) {
                    item { EmptyGoalCard(onClick = { showAddGoalSheet = true }) }
                }
            } else {
                item { EmptyGoalCard(onClick = { showAddGoalSheet = true }) }
            }

            // 4. COMPLETED GOALS section
            if (completedGoals.isNotEmpty()) {
                item {
                    Text(
                        text = "Pure Sapne",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = GGold
                        )
                    )
                }
                itemsIndexed(completedGoals) { index, goal ->
                    Box(modifier = Modifier.animateCardStagger(activeGoals.size + 2 + index)) {
                        CompletedGoalCard(
                            goal = goal,
                            onEdit = { showEditGoalSheet = goal },
                            onDelete = { goalToDelete = goal }
                        )
                    }
                }
            }
        }

        // FloatingActionButton overlapping bottom bar — mint glow
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 100.dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(GMint, GMintDeep)))
                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                .clickable { showAddGoalSheet = true }
                .animateButtonPress(),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, tint = GInk, contentDescription = "Add Goal")
        }
    }

    if (showAddGoalSheet) {
        AddGoalBottomSheet(
            onDismiss = { showAddGoalSheet = false },
            onSave = { name, amount, cat, emoji ->
                viewModel.addGoal(name, amount, cat, emoji)
                showAddGoalSheet = false
            }
        )
    }

    if (showAddSavingsSheet != null) {
        val goal = showAddSavingsSheet!!
        AddSavingsBottomSheet(
            goal = goal,
            onDismiss = { showAddSavingsSheet = null },
            onSave = { amount ->
                viewModel.addSavingsToGoal(goal.id, amount, goal.savedAmount, goal.targetAmount)
                showAddSavingsSheet = null
                if (goal.savedAmount + amount >= goal.targetAmount) {
                    showCelebration = goal
                }
            }
        )
    }

    if (showEditGoalSheet != null) {
        EditGoalBottomSheet(
            goal = showEditGoalSheet!!,
            onDismiss = { showEditGoalSheet = null },
            onSave = { name, targetAmount, emoji, savedAmount, isCompleted ->
                viewModel.updateGoal(showEditGoalSheet!!.id, name, targetAmount, emoji, savedAmount, isCompleted)
                showEditGoalSheet = null
            }
        )
    }

    if (showCelebration != null) {
        GoalCelebrationDialog(
            goal = showCelebration!!,
            onDismiss = { showCelebration = null }
        )
    }

    if (goalToDelete != null) {
        val goal = goalToDelete!!
        AlertDialog(
            onDismissRequest = { goalToDelete = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = Shapes.lg,
            containerColor = GInk,
            tonalElevation = 6.dp,
            title = {
                Text(
                    text = "Sapna Delete Karein? 🗑️",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = GText
                )
            },
            text = {
                Text(
                    text = "Kya aap sach mein \"${goal.emoji} ${goal.name}\" ko delete karna chahte hain? Isse aapki bachaayi hui bachat bhee delete ho jaayegi.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GText2
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteGoal(goal.id)
                        goalToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GCoral),
                    shape = Shapes.sm
                ) {
                    Text(
                        text = "Haan, Delete Karo",
                        color = GInk,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { goalToDelete = null }
                ) {
                    Text(
                        text = "Nahi, Rehne Do",
                        color = GText2,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

// 2. GOAL CARD — Refined Glass
@Composable
fun GoalCard(goal: Goal, onAddSavings: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val progress = if (goal.targetAmount > 0) (goal.savedAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    val progressPercent = (progress * 100).toInt()

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing),
        label = "progress_animation"
    )

    val reducedMotionShimmer = com.example.ui.components.rememberReducedMotion()
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (reducedMotionShimmer) 0f else 1800f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Top row: emoji tile + goal name + delete
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(Shapes.md)
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), Shapes.md),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = goal.emoji, fontSize = 26.sp, modifier = Modifier.clearAndSetSemantics {})
                    }
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = GText,
                            fontSize = 18.sp
                        )
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(48.dp).animateButtonPress()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit Goal",
                            tint = GText2
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(48.dp).animateButtonPress()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete Goal",
                            tint = GText2
                        )
                    }
                }
            }

            // Progress Bar: mint -> violet with shimmer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                if (animatedProgress > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .clip(CircleShape)
                            .background(Brush.horizontalGradient(listOf(GMint, GViolet)))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.White.copy(alpha = 0.35f),
                                            Color.Transparent
                                        ),
                                        start = Offset(shimmerOffset - 250f, 0f),
                                        end = Offset(shimmerOffset, 0f)
                                    )
                                )
                        )
                    }
                }
            }

            // Below bar: "₹X / ₹Y" left, "X% complete" right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${goal.savedAmount.toInt()} / ₹${goal.targetAmount.toInt()}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = GText2
                    )
                )
                Text(
                    text = "$progressPercent% complete",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = GMint
                    )
                )
            }

            // Motivation line
            val motivationLine = when {
                progressPercent == 0 -> "Shuru karo yaar! 💪"
                progressPercent in 1..30 -> "Acha shuru! Jari rakho! 🔥"
                progressPercent in 31..70 -> "Halfway there!"
                progressPercent in 71..99 -> "Bas thoda aur!"
                else -> "Sapna pura!"
            }
            Text(
                text = motivationLine,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    color = GText2
                )
            )

            // "+ Paisa Jodo" — mint gradient CTA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(Shapes.pill)
                    .background(Brush.horizontalGradient(listOf(GMint, GMintDeep)))
                    .clickable { onAddSavings() }
                    .animateButtonPress(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+ Paisa Jodo",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = GInk
                    )
                )
            }
        }
    }
}

// 4. COMPLETED GOAL CARD — Refined Glass
@Composable
fun CompletedGoalCard(goal: Goal, onEdit: () -> Unit, onDelete: () -> Unit) {
    LiquidGlassCard(modifier = Modifier.fillMaxWidth().animateButtonPress()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Mint checkmark circle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GMint),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✓", color = GInk, fontWeight = FontWeight.Black, fontSize = 18.sp)
                }
                Text(text = goal.emoji, fontSize = 28.sp, modifier = Modifier.clearAndSetSemantics {})
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                            color = GMint
                        )
                    )
                    Box(
                        modifier = Modifier
                            .clip(Shapes.sm)
                            .background(GMint.copy(alpha = 0.15f))
                            .border(1.dp, GMint.copy(alpha = 0.4f), Shapes.sm)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Pura hua! 🎉",
                            color = GMint,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit Completed Goal",
                        tint = GText2
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete Completed Goal",
                        tint = GCoral
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGoalBottomSheet(goal: Goal, onDismiss: () -> Unit, onSave: (String, Double, String, Double, Boolean) -> Unit) {
    var name by remember { mutableStateOf(goal.name) }
    var amount by remember { mutableStateOf(goal.targetAmount.toInt().toString()) }
    var savedAmountState by remember { mutableStateOf(goal.savedAmount) }
    var isCompletedState by remember { mutableStateOf(goal.isCompleted) }

    var withdrawInput by remember { mutableStateOf("") }
    var withdrawError by remember { mutableStateOf<String?>(null) }
    var isAnyFieldFocused by remember { mutableStateOf(false) }

    val categories = listOf("📱" to "Gadget", "🏠" to "Ghar", "🚗" to "Gaadi", "✈️" to "Safar", "💍" to "Shaadi", "📚" to "Padhai", "🎯" to "Other")
    var selectedCat by remember { 
        mutableStateOf(categories.find { it.first == goal.emoji } ?: (goal.emoji to goal.category)) 
    }

    val sheetState = rememberModalBottomSheetState(
        confirmValueChange = { it != androidx.compose.material3.SheetValue.Hidden }
    )
    // Hoisted so onDismissRequest (back / scrim) can hide the keyboard instead of closing the sheet.
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    ModalBottomSheet(
        // Back press / scrim tap route here. Never close the sheet — only hide the keyboard if open.
        // The Close (X) button is the ONLY way to dismiss.
        onDismissRequest = {
            if (isAnyFieldFocused) {
                keyboardController?.hide()
                focusManager.clearFocus()
            }
        },
        properties = androidx.compose.material3.ModalBottomSheetProperties(shouldDismissOnBackPress = false),
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(bottom = 32.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sapna Edit Karo ✏️", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = GText)
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = GText)
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Sapne ka naam") },
                modifier = Modifier.fillMaxWidth().onFocusChanged { isAnyFieldFocused = it.isFocused },
                singleLine = true
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                label = { Text("Target amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().onFocusChanged { isAnyFieldFocused = it.isFocused },
                singleLine = true
            )

            Text("Category & Emoji", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = GText2)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCat.first == cat.first,
                        onClick = { selectedCat = cat },
                        label = { Text("${cat.first} ${cat.second}") }
                    )
                }
            }

            // Savings and status display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bachat (Saved): ₹${savedAmountState.toInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = GText
                )
                if (isCompletedState) {
                    Text(
                        text = "Completed 🎉",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = GMint
                    )
                }
            }

            // Withdraw/Subtract option
            if (savedAmountState > 0.0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Shapes.sm)
                        .background(Palette.SurfaceInset)
                        .border(1.dp, GCoral.copy(alpha = 0.3f), Shapes.sm)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Paisa Nikalein (Withdraw Savings) 💸",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = GCoral
                    )
                    Text(
                        text = "Agar aapne is sapne se paisa nikala hai, toh niche amount likhein.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GText2
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = withdrawInput,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() }) {
                                    withdrawInput = it
                                    withdrawError = null
                                }
                            },
                            placeholder = { Text("Amount (₹)", color = GText2.copy(alpha = 0.5f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GText,
                                unfocusedTextColor = GText,
                                focusedBorderColor = GCoral,
                                unfocusedBorderColor = GText2.copy(alpha = 0.5f)
                            )
                        )
                        Button(
                            onClick = {
                                val wAmount = withdrawInput.toDoubleOrNull() ?: 0.0
                                if (wAmount <= 0.0) {
                                    withdrawError = "Pehle valid amount likhein!"
                                } else if (wAmount > savedAmountState) {
                                    withdrawError = "Bachat se zyada nahi nikal sakte! (Max: ₹${savedAmountState.toInt()})"
                                } else {
                                    savedAmountState -= wAmount
                                    withdrawInput = ""
                                    withdrawError = "₹${wAmount.toInt()} nikal liye! Ab bachat ₹${savedAmountState.toInt()} hai."
                                    val targetVal = amount.toDoubleOrNull() ?: goal.targetAmount
                                    if (savedAmountState < targetVal) {
                                        isCompletedState = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GCoral),
                            shape = Shapes.sm,
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("Nikalein 💸", color = GInk, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (withdrawError != null) {
                        Text(
                            text = withdrawError!!,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (withdrawError!!.contains("nikal") || withdrawError!!.contains("bachat")) GMint else GCoral,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Reactivate option for Completed goals
            if (isCompletedState) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Shapes.sm)
                        .background(Palette.SurfaceInset)
                        .border(1.dp, GGold.copy(alpha = 0.3f), Shapes.sm)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Sapna Wapas Active Karein 🔄",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = GGold
                    )
                    Text(
                        text = "Ye sapna pura ho chuka hai! Isko dobara active karne ke liye niche click karein. Isse bachat target se ₹100 kam ho jayegi taaki aap aur save kar sakein.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GText2
                    )
                    Button(
                        onClick = {
                            isCompletedState = false
                            val targetVal = amount.toDoubleOrNull() ?: goal.targetAmount
                            if (savedAmountState >= targetVal) {
                                savedAmountState = maxOf(0.0, targetVal - 100.0)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GGold),
                        shape = Shapes.sm,
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = "Active Karein 🔄",
                            color = GInk,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(Shapes.sm)
                    .background(Brush.horizontalGradient(listOf(GMint, GMintDeep)))
                    .clickable {
                        val targetAmount = amount.toDoubleOrNull() ?: 0.0
                        if (name.isNotEmpty() && targetAmount > 0) {
                            val finalCompleted = if (savedAmountState >= targetAmount) {
                                isCompletedState
                            } else {
                                false
                            }
                            onSave(name, targetAmount, selectedCat.first, savedAmountState, finalCompleted)
                        }
                    }
                    .animateButtonPress(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Badlaav Save Karo",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = GInk)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalBottomSheet(onDismiss: () -> Unit, onSave: (String, Double, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val categories = listOf("📱" to "Gadget", "🏠" to "Ghar", "🚗" to "Gaadi", "✈️" to "Safar", "💍" to "Shaadi", "📚" to "Padhai", "🎯" to "Other")
    var selectedCat by remember { mutableStateOf(categories[0]) }
    var isAnyFieldFocused by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(
        confirmValueChange = { it != androidx.compose.material3.SheetValue.Hidden }
    )
    // Hoisted so onDismissRequest (back / scrim) can hide the keyboard instead of closing the sheet.
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    ModalBottomSheet(
        // Back press / scrim tap route here. Never close the sheet — only hide the keyboard if open.
        // The Close (X) button is the ONLY way to dismiss.
        onDismissRequest = {
            if (isAnyFieldFocused) {
                keyboardController?.hide()
                focusManager.clearFocus()
            }
        },
        properties = androidx.compose.material3.ModalBottomSheetProperties(shouldDismissOnBackPress = false),
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(bottom = 32.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Naya Sapna Jodo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = GText)
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = GText)
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Sapne ka naam (e.g. iPhone khareedna hai)") },
                modifier = Modifier.fillMaxWidth().onFocusChanged { isAnyFieldFocused = it.isFocused },
                singleLine = true
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                label = { Text("Target amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().onFocusChanged { isAnyFieldFocused = it.isFocused },
                singleLine = true
            )

            Text("Category", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = GText2)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCat == cat,
                        onClick = { selectedCat = cat },
                        label = { Text("${cat.first} ${cat.second}") }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(Shapes.sm)
                    .background(Brush.horizontalGradient(listOf(GMint, GMintDeep)))
                    .clickable {
                        val targetAmount = amount.toDoubleOrNull() ?: 0.0
                        if (name.isNotEmpty() && targetAmount > 0) {
                            onSave(name, targetAmount, selectedCat.second, selectedCat.first)
                        }
                    }
                    .animateButtonPress(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sapna Save Karo",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = GInk)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSavingsBottomSheet(goal: Goal, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var isAnyFieldFocused by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        @OptIn(ExperimentalLayoutApi::class)
        val imeVisible = WindowInsets.isImeVisible
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        BackHandler(enabled = imeVisible || isAnyFieldFocused) {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
        Column(
            modifier = Modifier
                .padding(16.dp)
                .padding(bottom = 32.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Kitna bachaaya aaj? 🪙", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = GText)
            Text("For: ${goal.emoji} ${goal.name}", style = MaterialTheme.typography.bodyMedium, color = GText2)

            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                label = { Text("Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().onFocusChanged { isAnyFieldFocused = it.isFocused },
                singleLine = true
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(Shapes.sm)
                    .background(Brush.horizontalGradient(listOf(GMint, GMintDeep)))
                    .clickable {
                        val addAmount = amount.toDoubleOrNull() ?: 0.0
                        if (addAmount > 0) onSave(addAmount)
                    }
                    .animateButtonPress(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sapne mein jodo",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = GInk)
                )
            }
        }
    }
}

@Composable
fun GoalCelebrationDialog(goal: Goal, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GInk,
        shape = Shapes.lg,
        title = {
            Text(
                text = "🎉 SAPNA PURA HUA! 🎉",
                fontWeight = FontWeight.Black,
                color = GMint,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(text = "🎊", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = goal.emoji, fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "${goal.name} ab tumhara hai!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GText,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Kitni mehnat ki tumne! 💪",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GText2,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .animateButtonPress(),
                colors = ButtonDefaults.buttonColors(containerColor = GMint),
                shape = Shapes.sm
            ) {
                Text("Naya Sapna Dekho", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = GInk))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(top = 8.dp)
                    .animateButtonPress(),
                shape = Shapes.sm
            ) {
                Text("Is Sapne ko Archive Karo", style = MaterialTheme.typography.bodyLarge, color = GText2)
            }
        }
    )
}

@Composable
fun EmptyGoalCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(Shapes.xl)
            .background(Palette.SurfaceLow)
            .clickable(onClick = onClick)
            .drawBehind {
                val stroke = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                )
                drawRoundRect(
                    color = Palette.BorderStrong,
                    style = stroke,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(Dimens.radiusXl.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Naya Sapna Jodo",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Black,
                color = GText.copy(alpha = 0.85f)
            )
        )
    }
}

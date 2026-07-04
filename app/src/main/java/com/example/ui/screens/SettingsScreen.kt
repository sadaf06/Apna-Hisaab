package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.ExpenseViewModel
import com.example.LoginActivity
import android.content.Intent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.focus.onFocusChanged
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalView
import android.view.ViewTreeObserver
import android.graphics.Rect
import java.util.Calendar
import com.example.ui.theme.Palette
import com.example.ui.theme.Dimens
import com.example.ui.theme.Shapes
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription

// ---- Refined Glass palette — thin aliases onto the single source of truth (Palette). ----
private val SMint = Palette.Teal
private val SMintDeep = Palette.TealDeep
private val SViolet = Palette.Purple
private val SCoral = Palette.Danger
private val SAmber = Palette.Warning
private val SGreen = Palette.Success
private val SText = Palette.TextPrimary
private val SText2 = Palette.TextSecondary
private val SInk = Palette.OnAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: ExpenseViewModel, onAboutClick: () -> Unit, onAdminControlClick: () -> Unit = {}) {
    val context = LocalContext.current
    val budget by viewModel.monthlyBudget.collectAsStateWithLifecycle()
    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()

    // ------------------ PROFILE SECTION VARIABLES ------------------
    val currentFirebaseUser = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser }
    val isAdmin = currentFirebaseUser?.email?.equals("s.i.siddiqui06@gmail.com", ignoreCase = true) == true
    val userName by viewModel.userName.collectAsStateWithLifecycle(initialValue = "")
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle(initialValue = "")
    val loginMethod by viewModel.loginMethod.collectAsStateWithLifecycle(initialValue = "")

    val showEditNameDialog by viewModel.showEditNameDialog.collectAsStateWithLifecycle()
    var tempNameField by remember { mutableStateOf("") }
    val showPasswordResetConfirmDialog by viewModel.showPasswordResetConfirmDialog.collectAsStateWithLifecycle()
    val showLogoutConfirmDialog by viewModel.showLogoutConfirmDialog.collectAsStateWithLifecycle()
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmSecondDialog by remember { mutableStateOf(false) }
    var deleteErrorState by remember { mutableStateOf<String?>(null) }
    var isAuthActionLoading by remember { mutableStateOf(false) }
    var showFeatureSheet by remember { mutableStateOf(false) }

    LaunchedEffect(showEditNameDialog) {
        if (showEditNameDialog) {
            tempNameField = userName
        }
    }

    val profileInitials = remember(userName, userEmail) {
        val nameToUse = userName.ifEmpty { userEmail.substringBefore("@") }.trim()
        if (nameToUse.isNotEmpty()) {
            nameToUse.split(" ")
                .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                .take(2)
                .joinToString("")
        } else {
            "U"
        }
    }

    // ------------------ INCOME SECTION VARIABLES ------------------
    val isIncomeVisible by com.example.utils.IncomeVisibilityManager.isIncomeVisible.collectAsState()
    val incomeList by viewModel.income.collectAsStateWithLifecycle()
    var showIncomeManagementSheet by remember { mutableStateOf(false) }

    val incomeSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != androidx.compose.material3.SheetValue.Hidden }
    )
    var incomeToDelete by remember { mutableStateOf<com.example.data.Income?>(null) }

    // Calculate current month statistics
    val calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfMonth = calendar.timeInMillis
    val monthlyExpenses = expenses.filter { it.timestamp >= startOfMonth }
    val totalSpent = monthlyExpenses.sumOf { it.amount }

    // Budget Configuration state
    var budgetInput by remember { mutableStateOf("") }
    LaunchedEffect(budget) {
        if (budget > 0) {
            budgetInput = budget.toInt().toString()
        }
    }

    val percentUsed = if (budget > 0) (totalSpent / budget) else 0.0
    val isAlertTriggered = budget > 0 && totalSpent >= (budget * 0.8)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .animateScreenEntrance(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header
        item {
            Text(
                text = "Settings & Vibe",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = SText
                )
            )
        }

        // ------------------ PROFILE CARD (TOP) ------------------
        item {
            val isGoogle = loginMethod.equals("Google", ignoreCase = true)
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateCardStagger(0)
                    .testTag("profile_section_card")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Large gradient circle avatar (initials) — mint -> violet
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(SViolet, SMint))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profileInitials,
                                color = SInk,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .clickable {
                                        tempNameField = userName
                                        viewModel.showEditNameDialog.value = true
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = userName.ifEmpty { "Naam Add Karo ✏️" },
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (userName.isEmpty()) SText2 else SText
                                )
                                if (userName.isNotEmpty()) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Name",
                                        tint = SMint,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Text(
                                text = userEmail.ifEmpty { "No Email Set" },
                                style = MaterialTheme.typography.bodySmall,
                                color = SText2
                            )
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    // Action Row: Password (Email users only), Logout, Delete
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!isGoogle && userEmail.isNotEmpty()) {
                            GlassActionButton(
                                label = "Password 🔑",
                                accent = SText,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("password_badlo_btn"),
                                onClick = { viewModel.showPasswordResetConfirmDialog.value = true }
                            )
                        }
                        GlassActionButton(
                            label = "Logout 🚪",
                            accent = SAmber,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("logout_btn"),
                            onClick = { viewModel.showLogoutConfirmDialog.value = true }
                        )
                        GlassActionButton(
                            label = "Delete 🗑️",
                            accent = SCoral,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("delete_btn"),
                            onClick = { showDeleteConfirmDialog = true }
                        )
                    }
                }
            }
        }

        // ------------------ MERI KAMAAI (INCOME) CARD ------------------
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateCardStagger(1)
                    .clip(Shapes.xl)
                    .clickable { showIncomeManagementSheet = true }
            ) {
                LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                        // Mint left strip
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(4.dp)
                                .clip(CircleShape)
                                .background(SMint)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "💰 Meri Kamaai",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SText
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Manage Karein ➔",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = SMint,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = {
                                            com.example.utils.IncomeVisibilityManager.toggleVisibility(context, currentFirebaseUser?.uid ?: "")
                                        },
                                        modifier = Modifier.size(28.dp).padding(start = 4.dp)
                                    ) {
                                        Text(
                                            text = if (isIncomeVisible) "🔓" else "🔒",
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }

                            if (incomeList.isEmpty()) {
                                Text(
                                    text = "Abhi koi kamaai add nahi ki 😊 Click karke add karein!",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = SText2,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    incomeList.forEach { income ->
                                        val typeBadge = if (income.type == "monthly") "Monthly" else "One-time"
                                        val typeIcon = when (income.sourceType) {
                                            "Salary" -> "💼"
                                            "Freelance" -> "💻"
                                            "Business" -> "🏪"
                                            "Investment" -> "📈"
                                            else -> "🎁"
                                        }
                                        com.example.ui.components.BlurLockable(
                                            locked = !isIncomeVisible,
                                            onUnlock = { com.example.utils.IncomeVisibilityManager.toggleVisibility(context, currentFirebaseUser?.uid ?: "") },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "$typeIcon ${income.sourceName}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SText
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(
                                                        text = "₹${income.amount.toInt()}",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = SText
                                                    )
                                                    Text(
                                                        text = "($typeBadge)",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = SText2
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ------------------ MERGED BUDGET & KAMAAI CARD ------------------
        item {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateCardStagger(2)
                    .testTag("budget_insights_card")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SMint.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📊", fontSize = 18.sp)
                            }
                            Column {
                                Text(
                                    text = "Budget & Kamaai",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                                    color = SText
                                )
                                Text(
                                    text = "Is mahine ke kharche, budget aur kamaai",
                                    style = MaterialTheme.typography.labelSmall.copy(color = SText2)
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                com.example.utils.IncomeVisibilityManager.toggleVisibility(context, currentFirebaseUser?.uid ?: "")
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text(
                                text = if (isIncomeVisible) "🔓" else "🔒",
                                fontSize = 14.sp
                            )
                        }
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left Column (Spent)
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Kharch Kiya (Spent)",
                                style = MaterialTheme.typography.labelSmall.copy(color = SText2)
                            )
                            Text(
                                text = "₹${totalSpent.toInt()}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = SMint
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val percentText = if (budget > 0) (percentUsed * 100).toInt() else 0
                            val pillColor = when {
                                percentUsed < 0.5 -> SMint
                                percentUsed < 0.8 -> SAmber
                                else -> SCoral
                            }
                            Box(
                                modifier = Modifier
                                    .clip(Shapes.sm)
                                    .background(pillColor.copy(alpha = 0.12f))
                                    .border(1.dp, pillColor.copy(alpha = 0.4f), Shapes.sm)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$percentText% Used",
                                    color = pillColor,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                                )
                            }
                        }

                        // Right Column (Budget) — blurred when locked
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Aapka Budget",
                                style = MaterialTheme.typography.labelSmall.copy(color = SText2)
                            )
                            if (budget > 0) {
                                com.example.ui.components.BlurLockable(
                                    locked = !isIncomeVisible,
                                    onUnlock = { com.example.utils.IncomeVisibilityManager.toggleVisibility(context, currentFirebaseUser?.uid ?: "") }
                                ) {
                                    Text(
                                        text = "₹${budget.toInt()}",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            color = SText
                                        )
                                    )
                                }
                            } else {
                                Text(
                                    text = "Kamaai Jodein",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        color = SText2
                                    ),
                                    modifier = Modifier.clickable { showIncomeManagementSheet = true }
                                )
                            }
                            if (budget > 0) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Kamaai se automatic set",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SText2,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }

                    if (budget > 0) {
                        val progressColor = when {
                            percentUsed < 0.5 -> SMint
                            percentUsed < 0.8 -> SAmber
                            else -> SCoral
                        }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            var hasStarted by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) { hasStarted = true }
                            val animatedProgress by animateFloatAsState(
                                targetValue = if (hasStarted) percentUsed.toFloat().coerceIn(0f, 1f) else 0f,
                                animationSpec = tween(600, easing = LinearOutSlowInEasing),
                                label = "budget_progress"
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(Color.White.copy(alpha = 0.1f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(animatedProgress)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(progressColor)
                                )
                            }
                        }
                    }

                    // Alert banner if threshold exceeded
                    AnimatedVisibility(
                        visible = isAlertTriggered,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(Shapes.md)
                                .background(SCoral.copy(alpha = 0.08f))
                                .border(1.2.dp, SCoral.copy(alpha = 0.25f), Shapes.md)
                                .padding(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Alert icon",
                                    tint = SCoral,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Budget Alert Seema Paar! ⚠️",
                                        fontWeight = FontWeight.Black,
                                        style = MaterialTheme.typography.labelMedium.copy(color = SCoral)
                                    )
                                    Text(
                                        text = "Aap is mahine ka 80% budget seema paar kar chuke hain. Sambhalkar kharch karein!",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = SCoral.copy(alpha = 0.85f),
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        ),
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ------------------ CATEGORY BUDGET LIMITS CARD ------------------
        item {
            var showCategoryBudgets by remember { mutableStateOf(false) }
            val categoryBudgets by viewModel.categoryBudgets.collectAsStateWithLifecycle()
            val categoryArrowRotation by animateFloatAsState(
                targetValue = if (showCategoryBudgets) 180f else 0f,
                label = "category_arrow_rotation"
            )

            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateCardStagger(3)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCategoryBudgets = !showCategoryBudgets },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SViolet.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🎯", fontSize = 18.sp)
                            }
                            Column {
                                Text("Category Limits", style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp), fontWeight = FontWeight.Bold, color = SText)
                                Text("Har category ka alag target set karein", style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp), color = SText2)
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = if (showCategoryBudgets) "Collapse" else "Expand",
                            tint = SMint,
                            modifier = Modifier
                                .size(28.dp)
                                .graphicsLayer(rotationZ = categoryArrowRotation)
                        )
                    }

                    if (showCategoryBudgets) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Jis category ka limit set karna hai, uska amount likhein. Khali chhodne se limit hat jayega.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SText2
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        com.example.data.CategoryConfig.categories.forEach { cat ->
                            val currentLimit = categoryBudgets[cat.name] ?: 0.0
                            var categoryLimitInput by remember(currentLimit) {
                                mutableStateOf(if (currentLimit > 0.0) currentLimit.toInt().toString() else "")
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.2f)) {
                                    Text(cat.icon, style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(cat.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SText)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.weight(1.8f)
                                ) {
                                    OutlinedTextField(
                                        value = categoryLimitInput,
                                        onValueChange = { if (it.all { char -> char.isDigit() }) categoryLimitInput = it },
                                        placeholder = { Text("Unlimited", style = MaterialTheme.typography.bodySmall) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .width(110.dp)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        textStyle = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Button(
                                        onClick = {
                                            val limitVal = categoryLimitInput.toDoubleOrNull() ?: 0.0
                                            viewModel.updateCategoryBudget(cat.name, limitVal)
                                            com.example.utils.PremiumToast.show(context, "${cat.name} budget updated! ✅")
                                        },
                                        contentPadding = PaddingValues(horizontal = 12.dp),
                                        modifier = Modifier.height(38.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SMint),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Set", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SInk)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // SUGGEST A FEATURE LINK
        item {
            SettingsItem(
                icon = Icons.Default.Lightbulb,
                title = "Suggest a Feature",
                subtitle = "Kya New Feature Chahiye or Suggestions",
                onClick = { showFeatureSheet = true }
            )
        }

        if (isAdmin) {
            item {
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Admin Control",
                    subtitle = "Manage users and feedback",
                    onClick = onAdminControlClick
                )
            }
        }

        // ABOUT SECTION LINK
        item {
            SettingsItem(
                icon = Icons.Default.Info,
                title = "About App",
                subtitle = "Version, developer info",
                onClick = onAboutClick
            )
        }
    }

    // ------------------ ALL DIALOGS & SHEETS (OUTSIDE SCROLL CONTAINER) ------------------

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showEditNameDialog.value = false },
            title = {
                val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
                val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
                val view = androidx.compose.ui.platform.LocalView.current
                var isKeyboardVisible by remember { mutableStateOf(false) }

                DisposableEffect(view) {
                    val listener = android.view.ViewTreeObserver.OnGlobalLayoutListener {
                        val rect = android.graphics.Rect()
                        view.getWindowVisibleDisplayFrame(rect)
                        val screenHeight = view.rootView.height
                        val keypadHeight = screenHeight - rect.bottom
                        isKeyboardVisible = keypadHeight > screenHeight * 0.15
                    }
                    view.viewTreeObserver.addOnGlobalLayoutListener(listener)
                    onDispose {
                        view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
                    }
                }

                androidx.activity.compose.BackHandler(enabled = isKeyboardVisible) {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }

                Text(
                    text = "Naam Badlo ✏️",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = Palette.TextPrimary
                )
            },
            text = {
                OutlinedTextField(
                    value = tempNameField,
                    onValueChange = { tempNameField = it },
                    label = { Text("Apna Naam Likhein") },
                    placeholder = { Text("Poora naam likhein") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_profile_name_field_dialog"),
                    shape = Shapes.sm
                )
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = SMint),
                    onClick = {
                        viewModel.updateUserName(tempNameField) { success ->
                            if (success) {
                                try {
                                    val profileChangeRequest = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                        .setDisplayName(tempNameField)
                                        .build()
                                    com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                                        ?.updateProfile(profileChangeRequest)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                com.example.utils.PremiumToast.show(context, "Naam update ho gaya! ✅")
                                viewModel.showEditNameDialog.value = false
                            }
                        }
                    }
                ) {
                    Text("Save", fontWeight = FontWeight.Bold, color = SInk)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showEditNameDialog.value = false }) {
                    Text("Cancel", fontWeight = FontWeight.Bold, color = SText2)
                }
            },
            shape = Shapes.lg,
            containerColor = SInk
        )
    }

    if (showPasswordResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showPasswordResetConfirmDialog.value = false },
            title = { Text("Password Reset? 🔑", fontWeight = FontWeight.Bold, color = SText) },
            text = { Text("Password reset link bhejein $userEmail pe?", color = SText2) },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = SMint),
                    onClick = {
                        viewModel.showPasswordResetConfirmDialog.value = false
                        com.google.firebase.auth.FirebaseAuth.getInstance()
                            .sendPasswordResetEmail(userEmail)
                        com.example.utils.PremiumToast.show(context, "Reset link bhej diya! Spam folder bhi check karna 😊📧", longDuration = true)
                    }
                ) {
                    Text("Haan Bhejo 📧", fontWeight = FontWeight.Bold, color = SInk)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showPasswordResetConfirmDialog.value = false }) {
                    Text("Nahi Rukna Hai", fontWeight = FontWeight.Bold, color = SText2)
                }
            },
            shape = Shapes.lg,
            containerColor = SInk
        )
    }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showLogoutConfirmDialog.value = false },
            title = { Text("Logout karna chahte ho?", fontWeight = FontWeight.Bold, color = SText) },
            text = { Text("Aapka data safe rahega 😊", color = SText2) },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = SCoral),
                    onClick = {
                        viewModel.showLogoutConfirmDialog.value = false
                        viewModel.logout {
                            val intent = Intent(context, LoginActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            context.startActivity(intent)
                        }
                    }
                ) {
                    Text("Haan Logout", color = SInk)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showLogoutConfirmDialog.value = false }) {
                    Text("Nahi Rukna Hai", color = SText2)
                }
            },
            shape = Shapes.lg,
            containerColor = SInk
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("⚠️ Account Delete?", fontWeight = FontWeight.Bold, color = SText) },
            text = {
                Text("Pakka delete karna hai? 🥺", color = SText2)
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = SCoral),
                    onClick = {
                        showDeleteConfirmDialog = false
                        deleteErrorState = null
                        showDeleteConfirmSecondDialog = true
                    }
                ) {
                    Text("Haan, Aage Badho", color = SInk)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Nahi Bachao", color = SText2)
                }
            },
            shape = Shapes.lg,
            containerColor = SInk
        )
    }

    if (showDeleteConfirmSecondDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmSecondDialog = false },
            title = { Text("⚠️ Last Warning!", fontWeight = FontWeight.Bold, color = SText) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Ye wapas nahi hoga! Sach mein delete karein?", color = SText2)
                    if (deleteErrorState != null) {
                        Text(text = deleteErrorState!!, color = SCoral, style = MaterialTheme.typography.labelSmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = SCoral),
                    onClick = {
                        isAuthActionLoading = true
                        deleteErrorState = null
                        viewModel.deleteUserAccount { success, errorMsg ->
                            isAuthActionLoading = false
                            if (success) {
                                showDeleteConfirmSecondDialog = false
                                com.example.utils.PremiumToast.show(context, "Account delete ho gaya", longDuration = true)
                                val intent = Intent(context, LoginActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                }
                                context.startActivity(intent)
                            } else {
                                deleteErrorState = errorMsg ?: "Security error. Please sign in again to delete account."
                            }
                        }
                    }
                ) {
                    if (isAuthActionLoading) {
                        CircularProgressIndicator(color = SInk, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Haan, Delete Kardo", color = SInk)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmSecondDialog = false }) {
                    Text("Nahi Ruk Jao", color = SText2)
                }
            },
            shape = Shapes.lg,
            containerColor = SInk
        )
    }

    if (showFeatureSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFeatureSheet = false },
            containerColor = SInk,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            val focusManager = LocalFocusManager.current
            val keyboardController = LocalSoftwareKeyboardController.current
            var feedbackText by remember { mutableStateOf("") }
            var isSubmitting by remember { mutableStateOf(false) }
            var isFeedbackFocused by remember { mutableStateOf(false) }

            BackHandler(enabled = isFeedbackFocused) {
                keyboardController?.hide()
                focusManager.clearFocus()
            }

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .padding(bottom = 32.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Suggest a Feature 💡",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SText
                )

                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    label = { Text("Apna suggestion likhein") },
                    placeholder = { Text("Kaunsa naya feature chahiye?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .onFocusChanged { isFeedbackFocused = it.isFocused },
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SText,
                        unfocusedTextColor = SText,
                        focusedBorderColor = SMint,
                        unfocusedBorderColor = SText2.copy(alpha = 0.5f)
                    )
                )

                Button(
                    onClick = {
                        if (feedbackText.isNotBlank()) {
                            isSubmitting = true
                            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            val feedbackData = hashMapOf(
                                "text" to feedbackText,
                                "uid" to (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "anon"),
                                "email" to (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: ""),
                                "appVersion" to com.example.BuildConfig.VERSION_NAME,
                                "ts" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                            )
                            db.collection("feature_feedback")
                                .add(feedbackData)
                                .addOnSuccessListener {
                                    isSubmitting = false
                                    com.example.utils.PremiumToast.show(context, "Shukriya! Feedback mil gaya 🙏")
                                    feedbackText = ""
                                    showFeatureSheet = false
                                }
                                .addOnFailureListener {
                                    isSubmitting = false
                                    com.example.utils.PremiumToast.show(context, "Kuch galat hua. Kripya fir se koshish karein.")
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = feedbackText.isNotBlank() && !isSubmitting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SMint,
                        disabledContainerColor = SMint.copy(alpha = 0.5f)
                    ),
                    shape = Shapes.sm
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = SInk, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "Submit Suggestion",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = SInk
                            )
                        )
                    }
                }
            }
        }
    }

    if (showIncomeManagementSheet) {
        // Hoisted so onDismissRequest (back / scrim) can hide the keyboard instead of closing the sheet.
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        var isAnyFieldFocused by remember { mutableStateOf(false) }
        ModalBottomSheet(
            // Back press / scrim tap route here. Never close the sheet — only hide the keyboard if open.
            // The Close (X) button is the ONLY way to dismiss. (A separate BackHandler cannot win over
            // ModalBottomSheet's own dialog back-handling, so the guard must live here.)
            onDismissRequest = {
                if (isAnyFieldFocused) {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            },
            properties = androidx.compose.material3.ModalBottomSheetProperties(shouldDismissOnBackPress = false),
            sheetState = incomeSheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = SInk
        ) {
            var incomeSourceName by remember { mutableStateOf("") }
            var incomeSourceType by remember { mutableStateOf("Salary") }
            var incomeAmount by remember { mutableStateOf("") }
            var incomeType by remember { mutableStateOf("monthly") }
            var formErrorMsg by remember { mutableStateOf<String?>(null) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Kamaai Management",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = SText
                        )
                    }
                    IconButton(onClick = { showIncomeManagementSheet = false }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = SMint)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 1. Total Card
                val totalIncomeVal by viewModel.totalIncome.collectAsStateWithLifecycle()
                LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Kul Maasik Kamaai (Monthly Budget):",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = SText2
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        com.example.ui.components.BlurLockable(
                            locked = !isIncomeVisible,
                            onUnlock = { com.example.utils.IncomeVisibilityManager.toggleVisibility(context, currentFirebaseUser?.uid ?: "") }
                        ) {
                            Text(
                                text = "₹${totalIncomeVal.toInt()}",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = SMint
                                )
                            )
                        }
                        Text(
                            text = "Aapka budget automatic is kamaai se update hota hai.",
                            style = MaterialTheme.typography.labelSmall,
                            color = SText2
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 2. Add-Income Form
                Text(
                    text = "Nayi Kamaai Jodein ➕",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SText
                )
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SInk),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Source Ka Type Select Karein:",
                        style = MaterialTheme.typography.labelSmall,
                        color = SText2,
                        fontWeight = FontWeight.Bold
                    )

                    val sourceTypes = listOf(
                        "Salary" to "💼 Salary",
                        "Freelance" to "💻 Freelance",
                        "Business" to "🏪 Business",
                        "Investment" to "📈 Investment",
                        "Other" to "🎁 Other"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        sourceTypes.forEach { (id, label) ->
                            val isSelected = incomeSourceType == id
                            FilterChip(
                                selected = isSelected,
                                onClick = { incomeSourceType = id },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SMint,
                                    selectedLabelColor = SInk
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = incomeSourceName,
                        onValueChange = {
                            incomeSourceName = it
                            if (it.isNotEmpty()) formErrorMsg = null
                        },
                        label = { Text("Source ka naam (jaise: TCS Salary, Freelance project)") },
                        placeholder = { Text("TCS Salary") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().onFocusChanged { isAnyFieldFocused = it.isFocused }
                    )

                    OutlinedTextField(
                        value = incomeAmount,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                incomeAmount = input
                                if (input.isNotEmpty()) formErrorMsg = null
                            }
                        },
                        label = { Text("Amount (₹)") },
                        placeholder = { Text("15000") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().onFocusChanged { isAnyFieldFocused = it.isFocused }
                    )

                    Text(
                        text = "Kamaai Kab Kab Aati Hai?",
                        style = MaterialTheme.typography.labelSmall,
                        color = SText2,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val types = listOf(
                            "monthly" to "🔄 Monthly (har mahine)",
                            "one-time" to "1️⃣ Ek baar (one-time)"
                        )
                        types.forEach { (id, label) ->
                            val isSelected = incomeType == id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(Shapes.sm)
                                    .background(if (isSelected) SMint.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.05f))
                                    .border(
                                        width = if (isSelected) 1.8.dp else 1.dp,
                                        color = if (isSelected) SMint else Color.White.copy(alpha = 0.1f),
                                        shape = Shapes.sm
                                    )
                                    .clickable { incomeType = id }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = if (isSelected) SMint else SText
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    if (formErrorMsg != null) {
                        Text(
                            text = formErrorMsg!!,
                            color = SCoral,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SInk)
                            .padding(vertical = 4.dp)
                    ) {
                        Button(
                            onClick = {
                                if (incomeSourceName.isBlank()) {
                                    formErrorMsg = "Source ka naam likhna zaroori hai!"
                                    return@Button
                                }
                                val amt = incomeAmount.toDoubleOrNull() ?: 0.0
                                if (amt <= 0) {
                                    formErrorMsg = "Vajoodi amount likhein (₹0 se bada)!"
                                    return@Button
                                }

                                viewModel.addIncome(
                                    sourceName = incomeSourceName,
                                    sourceType = incomeSourceType,
                                    amount = amt,
                                    type = incomeType
                                )

                                com.example.utils.PremiumToast.show(context, "Kamaai add ho gayi!")

                                incomeSourceName = ""
                                incomeAmount = ""
                                incomeSourceType = "Salary"
                                incomeType = "monthly"
                                formErrorMsg = null
                            },
                            shape = Shapes.md,
                            colors = ButtonDefaults.buttonColors(containerColor = SMint),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("add_income_submit_btn")
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SInk)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Kamaai Jodo ✅",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = SInk)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(16.dp))

                // 3. Income List
                Text(
                    text = "Aapki Kamaai Ke Sources",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SText
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (incomeList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(Shapes.md)
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Abhi koi kamaai add nahi ki 😊",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = SText2
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        incomeList.forEach { income ->
                            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    com.example.ui.components.BlurLockable(
                                        locked = !isIncomeVisible,
                                        onUnlock = { com.example.utils.IncomeVisibilityManager.toggleVisibility(context, currentFirebaseUser?.uid ?: "") },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                val emoji = when (income.sourceType) {
                                                    "Salary" -> "💼"
                                                    "Freelance" -> "💻"
                                                    "Business" -> "🏪"
                                                    "Investment" -> "📈"
                                                    else -> "🎁"
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(SMint.copy(alpha = 0.1f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(emoji, fontSize = 20.sp)
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = income.sourceName,
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = SText
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    val typeLabel = if (income.type == "monthly") "🔄 Monthly" else "1️⃣ One-time"
                                                    Text(
                                                        text = "${income.sourceType} • $typeLabel",
                                                        style = MaterialTheme.typography.labelSmall.copy(color = SText2)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "₹${income.amount.toInt()}",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                                                color = SText,
                                                modifier = Modifier.padding(end = 4.dp)
                                            )
                                        }
                                    }
                                    IconButton(onClick = { incomeToDelete = income }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Income",
                                            tint = SCoral
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    if (incomeToDelete != null) {
        AlertDialog(
            onDismissRequest = { incomeToDelete = null },
            title = { Text("Kamaai Delete Karein? 🗑️", fontWeight = FontWeight.Bold, color = SText) },
            text = {
                Text(
                    "Kya aap sach mein apni kamaai source '${incomeToDelete?.sourceName}' (₹${incomeToDelete?.amount?.toInt()}) delete karna chahte hain? Isse aapka monthly budget bhi automatic update ho jayega.",
                    color = SText2
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = incomeToDelete?.id ?: ""
                        if (id.isNotEmpty()) {
                            viewModel.deleteIncome(id)
                            com.example.utils.PremiumToast.show(context, "Kamaai delete ho gayi! ❌")
                        }
                        incomeToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SCoral)
                ) {
                    Text("Haan, Delete Karo", fontWeight = FontWeight.Bold, color = SInk)
                }
            },
            dismissButton = {
                TextButton(onClick = { incomeToDelete = null }) {
                    Text("Cancel", fontWeight = FontWeight.Bold, color = SText2)
                }
            },
            shape = Shapes.lg,
            containerColor = SInk
        )
    }
}

@Composable
private fun GlassActionButton(
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(Shapes.pill)
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, accent.copy(alpha = 0.35f), Shapes.pill)
            .clickable { onClick() }
            .animateButtonPress(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = accent,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(Shapes.xl)
            .clickable { onClick() }
            .testTag("settings_item_${title.replace(" ", "_").lowercase()}")
    ) {
        LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SMint.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = SMint,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                        color = SText
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(color = SText2)
                    )
                }
                Text("➔", color = SMint, fontWeight = FontWeight.Bold)
            }
        }
    }
}

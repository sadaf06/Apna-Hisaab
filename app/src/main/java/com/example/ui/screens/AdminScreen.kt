package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Palette
import com.example.ui.theme.Dimens
import com.example.ui.theme.Shapes
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*

data class AdminUser(
    val uid: String,
    val name: String,
    val email: String,
    val disabled: Boolean,
    val lastActive: com.google.firebase.Timestamp? = null
)

data class UserEntry(
    val id: String,
    val originalText: String,
    val totalAmount: Double,
    val date: com.google.firebase.Timestamp?
)

data class UserIncome(
    val id: String,
    val sourceName: String,
    val sourceType: String,
    val amount: Double,
    val createdAt: com.google.firebase.Timestamp?
)

data class AdminFeedback(
    val id: String,
    val text: String,
    val email: String,
    val appVersion: String,
    val ts: com.google.firebase.Timestamp?
)

fun formatLastActive(timestamp: com.google.firebase.Timestamp?): String {
    if (timestamp == null) return "Latest entry: Purana record"
    val diff = System.currentTimeMillis() - timestamp.toDate().time
    val minutes = diff / (1000 * 60)
    val hours = minutes / 60
    val days = hours / 24
    
    return when {
        minutes < 1 -> "Latest entry: Abhi abhi 🟢"
        minutes < 60 -> "Latest entry: ${minutes}m pehle"
        hours < 24 -> "Latest entry: ${hours}h pehle"
        days < 7 -> "Latest entry: ${days}d pehle"
        else -> {
            val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            "Latest entry: ${sdf.format(timestamp.toDate())}"
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    
    var selectedTab by remember { mutableStateOf(0) } // 0 = Users, 1 = Feedback
    
    // Users state
    var usersList by remember { mutableStateOf<List<AdminUser>>(emptyList()) }
    var isLoadingUsers by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<AdminUser?>(null) }
    
    // User details sub-data state
    var userEntries by remember { mutableStateOf<List<UserEntry>>(emptyList()) }
    var isLoadingEntries by remember { mutableStateOf(false) }
    var userIncomes by remember { mutableStateOf<List<UserIncome>>(emptyList()) }
    var isLoadingIncomes by remember { mutableStateOf(false) }
    
    // Feedback state
    var feedbackList by remember { mutableStateOf<List<AdminFeedback>>(emptyList()) }
    var isLoadingFeedback by remember { mutableStateOf(false) }

    // Fetch initial data
    fun fetchUsers() {
        isLoadingUsers = true
        db.collection("users").get()
            .addOnSuccessListener { result ->
                val list = mutableListOf<AdminUser>()
                val documents = result.documents
                if (documents.isEmpty()) {
                    usersList = emptyList()
                    isLoadingUsers = false
                    return@addOnSuccessListener
                }
                
                for (doc in documents) {
                    val uid = doc.id
                    val profile = doc.get("profile") as? Map<*, *>
                    val name = (profile?.get("name") as? String) ?: (doc.getString("name") ?: "Unknown")
                    val email = (profile?.get("email") as? String) ?: (doc.getString("email") ?: doc.getString("email") ?: "No Email")
                    val disabled = doc.getBoolean("disabled") ?: false
                    
                    val fallbackTime = doc.getTimestamp("lastActive")
                        ?: (profile?.get("createdAt") as? com.google.firebase.Timestamp)
                        ?: (profile?.get("updatedAt") as? com.google.firebase.Timestamp)
                        ?: doc.getTimestamp("updatedAt")
                    
                    // Check entries subcollection for latest entry
                    db.collection("users").document(uid).collection("entries")
                        .orderBy("createdAt", Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .addOnCompleteListener { entryTask ->
                            var lastEntryTime = fallbackTime
                            if (entryTask.isSuccessful && entryTask.result != null && !entryTask.result.isEmpty) {
                                val latestDoc = entryTask.result.documents.firstOrNull()
                                if (latestDoc != null) {
                                    val entryCreatedAt = latestDoc.getTimestamp("createdAt")
                                        ?: latestDoc.getTimestamp("date")
                                        ?: latestDoc.getTimestamp("updatedAt")
                                    if (entryCreatedAt != null) {
                                        lastEntryTime = entryCreatedAt
                                    }
                                }
                            }
                            
                            synchronized(list) {
                                list.add(AdminUser(
                                    uid = uid,
                                    name = name,
                                    email = email,
                                    disabled = disabled,
                                    lastActive = lastEntryTime
                                ))
                                
                                if (list.size == documents.size) {
                                    // Sort by lastActive (latest entry time) descending
                                    usersList = list.sortedWith { u1, u2 ->
                                        val t1 = u1.lastActive?.toDate()?.time ?: 0L
                                        val t2 = u2.lastActive?.toDate()?.time ?: 0L
                                        t2.compareTo(t1)
                                    }
                                    isLoadingUsers = false
                                }
                            }
                        }
                }
            }
            .addOnFailureListener {
                isLoadingUsers = false
                com.example.utils.PremiumToast.show(context, "Users load nahi ho paaye ❌")
            }
    }

    fun fetchFeedback() {
        isLoadingFeedback = true
        db.collection("feature_feedback")
            .orderBy("ts", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val list = mutableListOf<AdminFeedback>()
                for (doc in result) {
                    val text = doc.getString("text") ?: ""
                    val email = doc.getString("email") ?: ""
                    val appVersion = doc.getString("appVersion") ?: ""
                    val ts = doc.getTimestamp("ts")
                    list.add(AdminFeedback(id = doc.id, text = text, email = email, appVersion = appVersion, ts = ts))
                }
                feedbackList = list
                isLoadingFeedback = false
            }
            .addOnFailureListener {
                isLoadingFeedback = false
                com.example.utils.PremiumToast.show(context, "Feedback load nahi ho paaya ❌")
            }
    }

    // Trigger loads
    LaunchedEffect(Unit) {
        fetchUsers()
        fetchFeedback()
    }

    // Fetch sub-data when a user is selected
    LaunchedEffect(selectedUser) {
        val user = selectedUser
        if (user != null) {
            isLoadingEntries = true
            db.collection("users").document(user.uid).collection("entries")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    val list = mutableListOf<UserEntry>()
                    for (doc in result) {
                        val originalText = doc.getString("originalText") ?: ""
                        val totalAmount = doc.getDouble("totalAmount") ?: 0.0
                        val date = doc.getTimestamp("createdAt") ?: doc.getTimestamp("date")
                        list.add(UserEntry(id = doc.id, originalText = originalText, totalAmount = totalAmount, date = date))
                    }
                    userEntries = list
                    isLoadingEntries = false
                }
                .addOnFailureListener {
                    // Fallback in case of index issues or simple order-by failures
                    db.collection("users").document(user.uid).collection("entries").get()
                        .addOnSuccessListener { fallbackResult ->
                            val list = mutableListOf<UserEntry>()
                            for (doc in fallbackResult) {
                                val originalText = doc.getString("originalText") ?: ""
                                val totalAmount = doc.getDouble("totalAmount") ?: 0.0
                                val date = doc.getTimestamp("createdAt") ?: doc.getTimestamp("date")
                                list.add(UserEntry(id = doc.id, originalText = originalText, totalAmount = totalAmount, date = date))
                            }
                            userEntries = list
                            isLoadingEntries = false
                        }
                        .addOnFailureListener {
                            isLoadingEntries = false
                        }
                }

            isLoadingIncomes = true
            db.collection("users").document(user.uid).collection("income")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { result ->
                    val list = mutableListOf<UserIncome>()
                    for (doc in result) {
                        val sourceName = doc.getString("sourceName") ?: ""
                        val sourceType = doc.getString("sourceType") ?: ""
                        val amount = doc.getDouble("amount") ?: 0.0
                        val createdAt = doc.getTimestamp("createdAt")
                        list.add(UserIncome(id = doc.id, sourceName = sourceName, sourceType = sourceType, amount = amount, createdAt = createdAt))
                    }
                    userIncomes = list
                    isLoadingIncomes = false
                }
                .addOnFailureListener {
                    db.collection("users").document(user.uid).collection("income").get()
                        .addOnSuccessListener { fallbackResult ->
                            val list = mutableListOf<UserIncome>()
                            for (doc in fallbackResult) {
                                val sourceName = doc.getString("sourceName") ?: ""
                                val sourceType = doc.getString("sourceType") ?: ""
                                val amount = doc.getDouble("amount") ?: 0.0
                                val createdAt = doc.getTimestamp("createdAt")
                                list.add(UserIncome(id = doc.id, sourceName = sourceName, sourceType = sourceType, amount = amount, createdAt = createdAt))
                            }
                            userIncomes = list
                            isLoadingIncomes = false
                        }
                        .addOnFailureListener {
                            isLoadingIncomes = false
                        }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedUser != null) "User Details" else "Admin Control Center",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (selectedUser != null) {
                                selectedUser = null
                            } else {
                                onBack()
                            }
                        },
                        modifier = Modifier.testTag("admin_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Palette.TextPrimary,
                    navigationIconContentColor = Palette.TextPrimary
                )
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .animateScreenEntrance()
        ) {
            if (selectedUser != null) {
                // User Details Sub-View
                val user = selectedUser!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimens.lg),
                    verticalArrangement = Arrangement.spacedBy(Dimens.sectionGap),
                    contentPadding = PaddingValues(bottom = 150.dp)
                ) {
                    // Profile details card
                    item {
                        LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimens.lg)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Dimens.md)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
                                            .clip(CircleShape)
                                            .background(Brush.linearGradient(listOf(Palette.Purple, Palette.PurpleDeep))),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = user.name.take(2).uppercase(),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = Palette.OnAccent,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = user.name,
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Palette.TextPrimary)
                                        )
                                        Text(
                                            text = user.email,
                                            style = MaterialTheme.typography.bodyMedium.copy(color = Palette.TextSecondary)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "UID: ${user.uid}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Palette.TextTertiary, fontSize = 11.sp)
                                )
                            }
                        }
                    }

                    // Block toggle card
                    item {
                        LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimens.lg),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Block / Disable User",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = Palette.TextPrimary)
                                    )
                                    Text(
                                        text = if (user.disabled) "Aapne is user ko block kiya hai" else "User active hai aur app use kar sakta hai",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Palette.TextSecondary)
                                    )
                                }
                                Switch(
                                    checked = user.disabled,
                                    onCheckedChange = { isBlocked ->
                                        // Update Firestore doc
                                        db.collection("users").document(user.uid)
                                            .set(mapOf("disabled" to isBlocked), SetOptions.merge())
                                            .addOnSuccessListener {
                                                val updatedUser = user.copy(disabled = isBlocked)
                                                selectedUser = updatedUser
                                                // Sync back with local users list
                                                usersList = usersList.map { if (it.uid == user.uid) updatedUser else it }
                                                com.example.utils.PremiumToast.show(
                                                    context,
                                                    if (isBlocked) "User ko Block kar diya gaya hai! 🔒" else "User ko Unblock kar diya gaya hai! Unlock ✅"
                                                )
                                            }
                                            .addOnFailureListener {
                                                com.example.utils.PremiumToast.show(context, "Setting change nahi ho payi ❌")
                                            }
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Palette.Danger,
                                        checkedTrackColor = Palette.DangerBg,
                                        uncheckedThumbColor = Palette.Teal,
                                        uncheckedTrackColor = Palette.SurfaceInset
                                    )
                                )
                            }
                        }
                    }

                    // Kharche (Entries) section
                    item {
                        Text(
                            text = "Kharche / Entries",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Palette.Purple),
                            modifier = Modifier.padding(top = Dimens.sm, bottom = Dimens.xs)
                        )
                    }

                    if (isLoadingEntries) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Palette.Purple)
                            }
                        }
                    } else if (userEntries.isEmpty()) {
                        item {
                            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Koi kharcha record nahi mila.",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Palette.TextTertiary),
                                    modifier = Modifier.padding(Dimens.lg).fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(userEntries) { entry ->
                            LiquidGlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Dimens.lg),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = entry.originalText,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, color = Palette.TextPrimary)
                                        )
                                        val dateStr = remember(entry.date) {
                                            if (entry.date != null) {
                                                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(entry.date.toDate())
                                            } else "N/A"
                                        }
                                        Text(
                                            text = dateStr,
                                            style = MaterialTheme.typography.bodySmall.copy(color = Palette.TextTertiary)
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .background(Palette.DangerBg, shape = Shapes.sm)
                                                .padding(horizontal = Dimens.sm, vertical = Dimens.xs)
                                        ) {
                                            Text(
                                                text = "₹${entry.totalAmount.toInt()}",
                                                style = MaterialTheme.typography.bodyMedium.copy(color = Palette.Danger, fontWeight = FontWeight.Bold)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = {
                                                db.collection("users").document(user.uid).collection("entries").document(entry.id).delete()
                                                    .addOnSuccessListener {
                                                        userEntries = userEntries.filter { it.id != entry.id }
                                                        com.example.utils.PremiumToast.show(context, "Hisaab Entry delete ho gayi! ❌")
                                                    }
                                                    .addOnFailureListener {
                                                        com.example.utils.PremiumToast.show(context, "Delete nahi ho paaya ❌")
                                                    }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete entry",
                                                tint = Palette.Danger
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Kamaai (Income) section
                    item {
                        Text(
                            text = "Kamaai / Income Source",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Palette.Teal),
                            modifier = Modifier.padding(top = Dimens.sm, bottom = Dimens.xs)
                        )
                    }

                    if (isLoadingIncomes) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Palette.Teal)
                            }
                        }
                    } else if (userIncomes.isEmpty()) {
                        item {
                            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Koi kamaai record nahi mila.",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Palette.TextTertiary),
                                    modifier = Modifier.padding(Dimens.lg).fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(userIncomes) { income ->
                            LiquidGlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Dimens.lg),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = income.sourceName,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, color = Palette.TextPrimary)
                                        )
                                        Text(
                                            text = "Type: ${income.sourceType}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Palette.TextTertiary)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(Palette.SuccessBg, shape = Shapes.sm)
                                            .padding(horizontal = Dimens.sm, vertical = Dimens.xs)
                                    ) {
                                        Text(
                                            text = "₹${income.amount.toInt()}",
                                            style = MaterialTheme.typography.bodyMedium.copy(color = Palette.Success, fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Tab Selection Layout
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Palette.Purple,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Palette.Teal
                        )
                    },
                    divider = {
                        HorizontalDivider(color = Palette.BorderSoft)
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Person, contentDescription = "Users", modifier = Modifier.size(18.dp))
                                Text(
                                    "Users",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 0) Palette.TextPrimary else Palette.TextSecondary
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Lightbulb, contentDescription = "Feedback", modifier = Modifier.size(18.dp))
                                Text(
                                    "Feedback",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 1) Palette.TextPrimary else Palette.TextSecondary
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // Users list Tab
                    if (isLoadingUsers) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Palette.Purple)
                        }
                    } else if (usersList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Koi users nahi mile 🕵️‍♂️", style = MaterialTheme.typography.bodyLarge.copy(color = Palette.TextSecondary))
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = Dimens.lg),
                            verticalArrangement = Arrangement.spacedBy(Dimens.sectionGap),
                            contentPadding = PaddingValues(bottom = 150.dp)
                        ) {
                            items(usersList) { user ->
                                LiquidGlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedUser = user }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(Dimens.lg),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(Dimens.md),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(CircleShape)
                                                    .background(Brush.linearGradient(listOf(Palette.Purple, Palette.PurpleDeep))),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = user.name.take(2).uppercase(),
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        color = Palette.OnAccent,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = user.name,
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = Palette.TextPrimary)
                                                )
                                                Text(
                                                    text = user.email,
                                                    style = MaterialTheme.typography.bodySmall.copy(color = Palette.TextSecondary)
                                                )
                                                Text(
                                                    text = formatLastActive(user.lastActive),
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = Palette.Purple,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp
                                                    ),
                                                    modifier = Modifier.padding(top = 2.dp)
                                                )
                                            }
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (user.disabled) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(Palette.DangerBg, shape = Shapes.pill)
                                                        .padding(horizontal = Dimens.sm, vertical = Dimens.xs)
                                                ) {
                                                    Text(
                                                        text = "Blocked",
                                                        style = MaterialTheme.typography.bodySmall.copy(color = Palette.Danger, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                                    )
                                                }
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .background(Palette.SuccessBg, shape = Shapes.pill)
                                                        .padding(horizontal = Dimens.sm, vertical = Dimens.xs)
                                                ) {
                                                    Text(
                                                        text = "Active",
                                                        style = MaterialTheme.typography.bodySmall.copy(color = Palette.Success, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowRight,
                                                contentDescription = "Open detail",
                                                tint = Palette.TextTertiary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Feedback Tab
                    if (isLoadingFeedback) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Palette.Purple)
                        }
                    } else if (feedbackList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("Koi feedback suggestions nahi mile 💬", style = MaterialTheme.typography.bodyLarge.copy(color = Palette.TextSecondary))
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = Dimens.lg),
                            verticalArrangement = Arrangement.spacedBy(Dimens.sectionGap),
                            contentPadding = PaddingValues(bottom = 150.dp)
                        ) {
                            items(feedbackList) { feedback ->
                                LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(Dimens.lg)
                                    ) {
                                        Text(
                                            text = feedback.text,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium, color = Palette.TextPrimary)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        HorizontalDivider(color = Palette.BorderSoft)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "By: ${feedback.email}",
                                                    style = MaterialTheme.typography.bodySmall.copy(color = Palette.TextSecondary)
                                                )
                                                val dateStr = remember(feedback.ts) {
                                                    if (feedback.ts != null) {
                                                        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(feedback.ts.toDate())
                                                    } else "N/A"
                                                }
                                                Text(
                                                    text = dateStr,
                                                    style = MaterialTheme.typography.bodySmall.copy(color = Palette.TextTertiary, fontSize = 11.sp)
                                                )
                                            }
                                            if (feedback.appVersion.isNotEmpty()) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(Palette.SurfaceInset, shape = Shapes.sm)
                                                        .padding(horizontal = Dimens.sm, vertical = Dimens.xs)
                                                ) {
                                                    Text(
                                                        text = "v${feedback.appVersion}",
                                                        style = MaterialTheme.typography.bodySmall.copy(color = Palette.TextSecondary, fontSize = 10.sp)
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
    }
}

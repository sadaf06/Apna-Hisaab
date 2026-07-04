package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.Dimens
import com.example.ui.theme.Palette
import com.example.ui.theme.Shapes
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onFinished: (String) -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()
    var userName by remember { mutableStateOf("") }

    AppBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 0.dp)
            ) { page ->
                // Subtle parallax/fade effect based on offset
                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            // Fade out as it moves away
                            alpha = 1f - kotlin.math.abs(pageOffset) * 0.5f
                            // Slight parallax shift
                            translationX = pageOffset * size.width * 0.1f
                        }
                ) {
                    when (page) {
                        0 -> WelcomePage()
                        1 -> HowItWorksPage()
                        2 -> FeaturesPage()
                        3 -> ReadyPage(userName) { userName = it }
                    }
                }
            }

            // Bottom Controls
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = Dimens.xxxl)
                    .padding(horizontal = Dimens.xxl)
            ) {
                // Page Indicator Dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 24.dp else 8.dp,
                            label = "dotWidth"
                        )
                        val alpha by animateFloatAsState(
                            targetValue = if (isSelected) 1f else 0.4f,
                            label = "dotAlpha"
                        )
                        
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(height = 8.dp, width = width)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) brandBrush else SolidColor(Palette.BorderStrong)
                                )
                                .graphicsLayer { this.alpha = alpha }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.xxl))

                // Navigation Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (pagerState.currentPage < 3) Arrangement.SpaceBetween else Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (pagerState.currentPage < 3) {
                        TextButton(
                            onClick = { onFinished("") }
                        ) {
                            Text(
                                "Skip",
                                color = Palette.TextSecondary,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            shape = Shapes.pill,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .widthIn(min = 100.dp)
                                .background(brandBrush, Shapes.pill)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = Dimens.xl),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("Aage", color = Palette.TextPrimary, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(Dimens.xs))
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Palette.TextPrimary
                                )
                            }
                        }
                    } else {
                        // Page 3: "Chalein!" full-width primary button
                        Button(
                            onClick = { onFinished(userName) },
                            shape = Shapes.pill,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(brandBrush, Shapes.pill)
                        ) {
                            Text(
                                "Chalein!",
                                color = Palette.TextPrimary,
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomePage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.xxxl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Glass Medallion 🙏
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(listOf(Palette.SurfaceHigh, Palette.SurfaceLow))
                )
                .border(Dimens.border, accentBorderBrush, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("🙏", fontSize = 56.sp)
        }
        
        Spacer(modifier = Modifier.height(Dimens.xxxl))
        
        Text(
            text = "Apna Hisaab mein aapka swagat hai!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                color = Palette.TextPrimary
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(Dimens.md))
        
        Text(
            text = "Paisa track karo, apni zubaan mein",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Palette.TextSecondary
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun HowItWorksPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.xxl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Bas ek line likho",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Black,
                color = Palette.TextPrimary
            )
        )
        
        Spacer(modifier = Modifier.height(Dimens.xxl))
        
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            highlight = true
        ) {
            Text(
                "\"Chai pe 30, lunch 120 gaya aaj\"",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Palette.TextPrimary
                )
            )
            
            Spacer(modifier = Modifier.height(Dimens.md))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("↓", color = Palette.TextTertiary, fontSize = 24.sp)
            }
            
            Spacer(modifier = Modifier.height(Dimens.md))
            
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                GlassPill(
                    modifier = Modifier.fillMaxWidth(),
                    selected = true,
                    accent = Palette.Teal
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("☕ Chai", color = Palette.TextPrimary)
                        Text("₹30", fontWeight = FontWeight.Bold, color = Palette.Teal)
                    }
                }
                
                GlassPill(
                    modifier = Modifier.fillMaxWidth(),
                    selected = true,
                    accent = Palette.Purple
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🍽️ Lunch", color = Palette.TextPrimary)
                        Text("₹120", fontWeight = FontWeight.Bold, color = Palette.Purple)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(Dimens.xl))
        
        Text(
            "AI baaki samjhega!",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Palette.TextSecondary,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Composable
fun FeaturesPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.xxl),
        verticalArrangement = Arrangement.Center
    ) {
        SectionHeader(title = "Khasiyat")
        
        Spacer(modifier = Modifier.height(Dimens.xl))
        
        val features = listOf(
            "🌟 Mera Sapna" to "goal tracker",
            "💰 Kamaai track karo" to "income tracker",
            "📊 Monthly kahani" to "monthly analytics",
            "🔒 Sirf tumhara data" to "100% private"
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
            features.forEachIndexed { index, (title, subtitle) ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(Dimens.md)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index % 2 == 0) Palette.Teal.copy(alpha = 0.15f) else Palette.Purple.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                title.take(2), // Take emoji
                                fontSize = 20.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(Dimens.md))
                        
                        Column {
                            Text(
                                title.drop(2).trim(),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Palette.TextPrimary
                                )
                            )
                            Text(
                                subtitle,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Palette.TextTertiary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReadyPage(userName: String, onNameChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.xxl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Shuru karte hain! 🚀",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
                color = Palette.TextPrimary
            )
        )
        
        Spacer(modifier = Modifier.height(Dimens.md))
        
        Text(
            text = "Bas apna naam batao, aur hum taiyar hain",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = Palette.TextSecondary
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(Dimens.xxxl))
        
        OutlinedTextField(
            value = userName,
            onValueChange = onNameChange,
            label = { Text("Aapka naam?") },
            modifier = Modifier.fillMaxWidth(),
            shape = Shapes.md,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Palette.TextPrimary,
                unfocusedTextColor = Palette.TextPrimary,
                focusedBorderColor = Palette.Purple,
                unfocusedBorderColor = Palette.BorderSoft,
                focusedLabelColor = Palette.Purple,
                unfocusedLabelColor = Palette.TextTertiary,
                cursorColor = Palette.Purple,
                focusedContainerColor = Palette.SurfaceInset,
                unfocusedContainerColor = Palette.SurfaceInset
            ),
            singleLine = true
        )
    }
}

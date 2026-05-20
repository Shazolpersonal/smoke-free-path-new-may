@file:OptIn(androidx.compose.animation.ExperimentalAnimationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui

import com.example.data.QuitProfile
import com.example.data.CravingLog
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun QuitSmokeDashboard(
    modifier: Modifier = Modifier,
    viewModel: QuitViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Positive Psychology Quotes list in Bengali
    val dailyQuotes = remember {
        listOf(
            "আমি ধূমপানের বিষের চেয়েও অনেক বেশি শক্তিশালী এবং নিজের স্বাস্থ্যের অধিকার আমার নিজের হাতে।",
            "আমার ফুসফুস প্রতি মুহূর্তে নিজেকে পরিষ্কার করছে এবং আমি প্রতি নিঃশ্বাসে নতুন জীবন ফিরে পাচ্ছি।",
            "ধূমপানের তীব্র ইচ্ছেটি একটি সাময়িক মেঘের মতো। এটি ঝড় না তুলিয়েই শান্তভাবে ঘনীভূত হয়ে কেটে যাবে।",
            "ধূমপান মুক্তির পথে প্রতিটি অতিবাহিত সেকেন্ড হচ্ছে ফুসফুস ও কোষগুলোর একটি করে সজীব পুনর্জন্ম।",
            "নিজের শরীরকে নিকোটিনের বিষ থেকে রক্ষা করা কোনো ত্যাগ নয়, এটি নিজের প্রতি সুন্দরতম উপহার।"
        )
    }
    var currentQuoteIndex by remember { mutableStateOf(0) }
    var showSOSDialog by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    // Trigger Snackbar for success notifications
    LaunchedEffect(state.showSuccessMessage) {
        state.showSuccessMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showSOSDialog = true
                },
                containerColor = TerracottaWarn,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.padding(bottom = 8.dp, end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "🚨 তীব্র ইচ্ছা প্রশমন কিট (SOS)",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "🚨 SOS",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(PrimaryMint)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Health Icon",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "মুক্ত জীবন",
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = PrimaryMint,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header: Daily Positive psychology Quote generator
            item {
                AffirmationCard(
                    quote = dailyQuotes[currentQuoteIndex],
                    onNextQuote = {
                        currentQuoteIndex = (currentQuoteIndex + 1) % dailyQuotes.size
                    }
                )
            }

            // Section 1: Main dynamic adjustable Tracker circle
            item {
                CountdownWheelDashboard(
                    timePassed = state.timePassed,
                    profile = state.profile,
                    onCustomizeClick = { viewModel.setShowCustomize(true) },
                    onOpenLogCraving = { viewModel.setShowLogCraving(true) },
                    onOpenSOSClick = { showSOSDialog = true }
                )
            }

            // Section 2: Expanded Respiratory Gym and Breathing Coping Mechanisms (Box vs 4-7-8)
            item {
                AdvancedBreathingCard(
                    breathingState = state.breathingState,
                    selectedStyle = state.selectedBreathingStyle,
                    onStyleSelected = { viewModel.setBreathingStyle(it) },
                    onStartBreathing = { viewModel.startBreathingExercise() },
                    onCancelBreathing = { viewModel.cancelBreathingExercise() },
                    breathsCompletedCount = state.profile.breathsCompleted
                )
            }

            // Section 3: Interactive Sobor Warrior Diary / Resistant Logs View
            item {
                CravingsLogSection(
                    logs = state.cravingLogs,
                    totalResisted = state.profile.cravingsResisted,
                    viewModel = viewModel
                )
            }

            // Section 3b: Trigger & Severity Analytics Card
            item {
                QuitAnalyticsDashboard(
                    logs = state.cravingLogs
                )
            }

            // Section 4: Body Recovery Timeline Info (Physiology progression)
            item {
                RecoveryTimelineHeader()
            }

            val quitDurationSeconds = (System.currentTimeMillis() - state.profile.quitTimestamp) / 1000
            val timelineItems = getTimelineItems()
            items(timelineItems) { item ->
                TimelineMilestoneRow(
                    item = item,
                    currentSecondsElapsed = if (quitDurationSeconds > 0) quitDurationSeconds else 0
                )
            }

            // Section 5: Dynamic educational guidance tabs (Reasons vs Harm vs Actionable Tips)
            item {
                EducationalTabsSection()
            }
        }
    }

    // Modal dialog for customizable countdown timer
    if (state.showCustomizeDialog) {
        CustomizeTimerDialog(
            profile = state.profile,
            onDismiss = { viewModel.setShowCustomize(false) },
            onSave = { name, timestamp, dailyCount, price ->
                viewModel.updateProfileSettings(userName = name, quitTimestamp = timestamp, cigarettesPerDay = dailyCount, pricePerCigarette = price)
            }
        )
    }

    // Modal dialog to log a structured craving resistance
    if (state.showLogCravingDialog) {
        LogCravingDialog(
            onDismiss = { viewModel.setShowLogCraving(false) },
            onSave = { trigger, severity, coping ->
                viewModel.saveDetailedCravingLog(trigger, severity, coping)
            }
        )
    }

    // Modal dialog for Emergency SOS Grounding Kit
    if (showSOSDialog) {
        SOSGroundingDialog(
            onDismiss = { showSOSDialog = false },
            onLogCrisisResisted = { trigger, severity, coping ->
                viewModel.saveDetailedCravingLog(trigger, severity, coping)
                showSOSDialog = false
            }
        )
    }
}

@Composable
fun AffirmationCard(quote: String, onNextQuote: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PrimaryMint.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "✨",
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "আজকের ইতিবাচক আত্মবিশ্বাস (Affirmation)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryMint,
                        letterSpacing = 0.2.sp
                    )
                }
                IconButton(
                    onClick = onNextQuote,
                    modifier = Modifier
                        .size(32.dp)
                        .background(PrimaryMint.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Quote",
                        tint = PrimaryMint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "“",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black,
                    color = PrimaryMint.copy(alpha = 0.08f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-4).dp, y = (-28).dp)
                )
                
                AnimatedContent(
                    targetState = quote,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(400)) + slideInVertically(animationSpec = tween(400)) { it / 3 }) togetherWith
                        (fadeOut(animationSpec = tween(250)) + slideOutVertically(animationSpec = tween(250)) { -it / 3 })
                    },
                    label = "QuoteAnimation"
                ) { targetQuote ->
                    Text(
                        text = "“$targetQuote”",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.padding(start = 12.dp, end = 4.dp, bottom = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CountdownWheelDashboard(
    timePassed: TimeRemaining,
    profile: QuitProfile,
    onCustomizeClick: () -> Unit,
    onOpenLogCraving: () -> Unit,
    onOpenSOSClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    // Infinite breathing animation to represent calm lung recovery
    val infiniteTransition = rememberInfiniteTransition(label = "TrackerLungPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.04f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ধূমপানমুক্ত মহাকাব্যিক রিয়েল-টাইম ট্র্যাকার",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 0.1.sp
                    )
                    Text(
                        text = "নিকোটিনমুক্ত সুবর্ণ পথ অতিক্রমণ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCustomizeClick()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(PrimaryMint.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "সময় কাস্টমাইজ করুন",
                        tint = PrimaryMint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Beautiful interactive Circular design with lung pulse representation
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(220.dp)
            ) {
                // Animated outer pulmonary breathing wave
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = pulseScale * 1.15f
                            scaleY = pulseScale * 1.15f
                        }
                        .size(190.dp)
                        .background(PrimaryMint.copy(alpha = pulseAlpha), CircleShape)
                )
                
                // Animated middle pulmonary breathing wave
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                        .size(180.dp)
                        .background(SecondaryEmerald.copy(alpha = pulseAlpha / 1.5f), CircleShape)
                )

                // Main countdown container
                Box(
                    modifier = Modifier
                        .size(165.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface,
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.015f)
                                )
                            )
                        )
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(PrimaryMint, SecondaryEmerald.copy(alpha = 0.4f))
                            ),
                            shape = CircleShape
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (timePassed.isQuitDateInFuture) "পরিকল্পিত হতে" else "ধূমপানমুক্ত আছেন",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (timePassed.days > 0) "${timePassed.days} দিন" else "${timePassed.hours} ঘণ্টা",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryMint,
                            textAlign = TextAlign.Center,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${timePassed.minutes}মি ${timePassed.seconds}সে",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SecondaryEmerald,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .background(PrimaryMint.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "ফুসফুস সচল",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryMint
                            )
                        }
                    }
                }
            }

            if (timePassed.isQuitDateInFuture) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "⚠️ পরিকল্পিত সময়টি ভবিষ্যতে! কাউন্টডাউন শুরু হতে অপেক্ষা করুন।",
                    fontSize = 11.sp,
                    color = TerracottaWarn,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Beautiful interactive triple-metrics: Money Saved, Cigarettes Avoided, & Life Extended
            val elapsedMs = System.currentTimeMillis() - profile.quitTimestamp
            val elapsedMsSafe = if (elapsedMs > 0) elapsedMs else 0L
            val elapsedDays = elapsedMsSafe.toDouble() / (1000.0 * 60 * 60 * 24)
            
            val cigarettesCount = profile.cigarettesPerDay
            val stickPrice = profile.pricePerCigarette
            val cigarettesAvoided = (elapsedDays * cigarettesCount).toInt()
            val bdtSaved = elapsedDays * cigarettesCount * stickPrice
            val formattedBdtSaved = String.format(Locale.getDefault(), "%.1f", bdtSaved)
            
            // Each cigarette avoided restores approx 11 minutes of life
            val lifeMinutesRegained = (cigarettesAvoided * 11)
            val lifeRegainedText = when {
                lifeMinutesRegained >= 1440 -> {
                    val d = lifeMinutesRegained / 1440
                    val h = (lifeMinutesRegained % 1440) / 60
                    "$d দিন $h ঘ."
                }
                lifeMinutesRegained >= 60 -> {
                    val h = lifeMinutesRegained / 60
                    val m = lifeMinutesRegained % 60
                    "$h ঘ. $m মি."
                }
                else -> "$lifeMinutesRegained মি."
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 1.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Money Saved Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = 1.dp,
                            color = PrimaryMint.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryMint.copy(alpha = 0.02f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(AccentSky.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "৳", fontSize = 14.sp, fontWeight = FontWeight.Black, color = AccentSky)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "সাশ্রয়ী টাকা",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "৳ $formattedBdtSaved",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryMint,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 2. Cigarettes Avoided Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = 1.dp,
                            color = TerracottaWarn.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = TerracottaWarn.copy(alpha = 0.02f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(TerracottaWarn.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Avoided Stick",
                                tint = TerracottaWarn,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "এড়ানো সিগারেট",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$cigarettesAvoided টি",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = TerracottaWarn,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 3. Life Regained Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = 1.dp,
                            color = SecondaryEmerald.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SecondaryEmerald.copy(alpha = 0.02f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(SecondaryEmerald.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Life Regained",
                                tint = SecondaryEmerald,
                                modifier = Modifier.size(12.dp)
                             )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "অর্জিত বাড়তি আয়ু",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = lifeRegainedText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = SecondaryEmerald,
                            textAlign = TextAlign.Center,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Trigger log craving action block
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Shield Badges",
                            tint = AccentSky,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "আজ অবদমিত ধূমপানের ইচ্ছা:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                    Text(
                        text = "${profile.cravingsResisted} বার সফল প্রতিরোধ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SecondaryEmerald,
                        modifier = Modifier.padding(start = 24.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pulsating SOS Button
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onOpenSOSClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaWarn),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color.White, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "SOS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Existing "ইচ্ছে সামলেছি" Button
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onOpenLogCraving()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryMint),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "অভিযান লগ",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "ইচ্ছে সামলেছি", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AdvancedBreathingCard(
    breathingState: BreathingState,
    selectedStyle: BreathingStyle,
    onStyleSelected: (BreathingStyle) -> Unit,
    onStartBreathing: () -> Unit,
    onCancelBreathing: () -> Unit,
    breathsCompletedCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with custom completed stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ফুসফুস কন্ডিশনিং ও ব্রিদিং জিম",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "নিকোটিন তাড়াতে শ্বাস-প্রশ্বাস ব্যায়াম সবচেয়ে কার্যকর",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
                Box(
                    modifier = Modifier
                        .background(SecondaryEmerald.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "সম্পন্ন: $breathsCompletedCount",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SecondaryEmerald
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (breathingState) {
                is BreathingState.Idle -> {
                    // Interactive respiratory session style selector!
                    Text(
                        text = "যেকোনো একটি পদ্ধতি বেছে নিন:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BreathingStyle.values().forEach { style ->
                            val isSelected = style == selectedStyle
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSelected) PrimaryMint.copy(alpha = 0.08f) else Color.Transparent
                                    )
                                    .clickable { onStyleSelected(style) }
                                    .shadow(if (isSelected) 0.dp else 1.dp, RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSelected) Color.Transparent else MaterialTheme.colorScheme.background,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { onStyleSelected(style) },
                                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryMint)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = style.titleBengali,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) PrimaryMint else MaterialTheme.colorScheme.onBackground
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = style.shortDesc,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onStartBreathing,
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryEmerald),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "প্রক্রিয়া শুরু"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ব্যায়াম সেশন আরম্ভ করুন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                is BreathingState.Active -> {
                    // Ultimate High fidelity animating breathing progress UI!
                    val scale by animateFloatAsState(
                        targetValue = when (breathingState.phase) {
                            BreathingState.Phase.INHALE -> 1.5f
                            BreathingState.Phase.HOLD -> 1.5f
                            BreathingState.Phase.EXHALE -> 1.0f
                            BreathingState.Phase.HOLD_POST -> 1.0f
                        },
                        animationSpec = tween(durationMillis = 3500, easing = LinearEasing),
                        label = "Fidelity Scaling Bubble"
                    )

                    val bubbleColor = when (breathingState.phase) {
                        BreathingState.Phase.INHALE -> PrimaryMint
                        BreathingState.Phase.HOLD -> AccentSky
                        BreathingState.Phase.EXHALE -> SecondaryEmerald
                        BreathingState.Phase.HOLD_POST -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    }

                    val actionTitleBengali = when (breathingState.phase) {
                        BreathingState.Phase.INHALE -> "বুক ভরে শ্বাস নিন..."
                        BreathingState.Phase.HOLD -> "দম আটকে রাখুন..."
                        BreathingState.Phase.EXHALE -> "ধীরে ধীরে মুখ দিয়ে ছাড়ুন..."
                        BreathingState.Phase.HOLD_POST -> "ফুসফুস খালি রাখুন..."
                    }

                    val phaseHelpText = when (breathingState.phase) {
                         BreathingState.Phase.INHALE -> "১. নাক দিয়ে বুক ফুলিয়ে গভীর বাতাস টানুন (ফুসফুসের অলভিওলি প্রসারিত হচ্ছে)"
                         BreathingState.Phase.HOLD -> "২. আলতো করে ফুসফুসে অক্সিজেন ধরে রাখুন (সুস্থ সেল গঠন করছে)"
                         BreathingState.Phase.EXHALE -> "৩. মুখ গোল করে পরম শান্তিতে বিষাক্ত বাতাস ছেড়ে দিন (ক্র্যাভিং দূর হচ্ছে)"
                         BreathingState.Phase.HOLD_POST -> "৪. শরীর সম্পূর্ণ শিথিল করে মন শান্ত রাখুন (রক্তচাপ স্বাভাবিক হচ্ছে)"
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .background(bubbleColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "সাইকেল: ${breathingState.currentCycle} / ${breathingState.totalCycles}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = bubbleColor
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = actionTitleBengali,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = bubbleColor,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Animated breathing cell simulator container
                        Box(
                            modifier = Modifier
                                .height(210.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            // Outer ambient halo that expands even more
                            Box(
                                modifier = Modifier
                                    .graphicsLayer(
                                        scaleX = scale * 1.25f,
                                        scaleY = scale * 1.25f
                                    )
                                    .size(130.dp)
                                    .background(bubbleColor.copy(alpha = 0.1f), CircleShape)
                            )

                            // Middle ambient halo
                            Box(
                                modifier = Modifier
                                    .graphicsLayer(
                                        scaleX = scale * 1.12f,
                                        scaleY = scale * 1.12f
                                    )
                                    .size(130.dp)
                                    .background(bubbleColor.copy(alpha = 0.22f), CircleShape)
                            )

                            // Main core interactive bubble
                            Box(
                                modifier = Modifier
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale
                                    )
                                    .size(130.dp)
                                    .shadow(8.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                bubbleColor,
                                                bubbleColor.copy(alpha = 0.6f)
                                             )
                                         )
                                    )
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${breathingState.secondsRemaining}",
                                        color = Color.White,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Health explanation panel
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = bubbleColor.copy(alpha = 0.04f))
                        ) {
                            Text(
                                text = phaseHelpText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(12.dp).fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Symmetrical Progress cycle indicator lights
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            for (i in 1..breathingState.totalCycles) {
                                val isPastCycle = i < breathingState.currentCycle
                                val isCurrentCycle = i == breathingState.currentCycle
                                val circleColor = if (isPastCycle) SecondaryEmerald else if (isCurrentCycle) bubbleColor else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
                                val circleSize = if (isCurrentCycle) 10.dp else 6.dp
                                val circlePulse by animateDpAsState(targetValue = circleSize, label = "CycleDotSize")
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(circlePulse)
                                        .clip(CircleShape)
                                        .background(circleColor)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = onCancelBreathing,
                            colors = ButtonDefaults.textButtonColors(contentColor = TerracottaWarn)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "সমাপ্ত করুন",
                                modifier = Modifier.size(16.dp),
                                tint = TerracottaWarn
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "ব্যায়াম সেশন বন্ধ করুন", fontWeight = FontWeight.Bold, color = TerracottaWarn, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(12.dp))

            // Non-nicotine visual tasks triggers
            Text(
                text = "ফুসফুস পুনরুদ্ধারকালীন মন ডাইভারশনের অল্টারনেটিভ কার্যক্রম:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickCopingPill(icon = "💧", action = "১ গ্লাস পানি", desc = "সাক্ষাৎ স্বস্তি")
                QuickCopingPill(icon = "🚶", action = "২ মিনিট হাঁটা", desc = "রক্ত সক্রিয়")
                QuickCopingPill(icon = "🥛", action = "টক খাওয়া", desc = "স্বাদ পরিবর্তন")
            }
        }
    }
}

@Composable
fun RowScope.QuickCopingPill(icon: String, action: String, desc: String) {
    var isTriggered by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .weight(1f)
            .shadow(if (isTriggered) 0.dp else 1.dp, RoundedCornerShape(12.dp))
            .background(
                if (isTriggered) PrimaryMint.copy(alpha = 0.15f) else MaterialTheme.colorScheme.background,
                RoundedCornerShape(12.dp)
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isTriggered = !isTriggered
            }
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = action,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isTriggered) PrimaryMint else MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (isTriggered) "সম্পন্ন ✓" else desc,
                fontSize = 9.sp,
                color = if (isTriggered) PrimaryMint else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CravingsLogSection(
    logs: List<CravingLog>,
    totalResisted: Int,
    viewModel: QuitViewModel
) {
    var isExpandedHistory by remember { mutableStateOf(false) }

    // Dynamic Warrior Tier Badge calculation based on resistance logs
    val (tierTitle, tierColor, tierIcon) = when {
        totalResisted >= 15 -> Triple("গোল্ডেন ডিফেন্ডার (Golden Defender)", AccentSky, "🏆")
        totalResisted >= 7 -> Triple("সিলভার গার্ডিয়ান (Silver Guardian)", PrimaryMint, "🛡️")
        totalResisted >= 1 -> Triple("ব্রোঞ্জ শিল্ড (Bronze Shield)", SecondaryEmerald, "✊")
        else -> Triple("নবীন যোদ্ধা (Novice Warrior)", MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), "🌱")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Journal Icon",
                        tint = PrimaryMint,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ধূমপানমুক্ত যোদ্ধা ডায়েরি (Diary)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                if (logs.isNotEmpty()) {
                    TextButton(
                        onClick = { isExpandedHistory = !isExpandedHistory },
                        colors = ButtonDefaults.textButtonColors(contentColor = PrimaryMint)
                    ) {
                        Text(
                            text = if (isExpandedHistory) "লুকান" else "ডায়েরি দেখুন",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = "আপনার ধূমপানের তীব্র ইচ্ছে প্রতিরোধ ও ট্রিগারের রিয়েল-টাইম ট্র্যাক রেকর্ড।",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Achievement Shield Tier Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(tierColor.copy(alpha = 0.08f))
                    .border(1.dp, tierColor.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = tierIcon, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ক্র্যাভিং প্রতিরোধ মেডেল",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                            Text(
                                text = tierTitle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(tierColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$totalResisted প্রতিরোধ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (tierColor == MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)) MaterialTheme.colorScheme.onBackground else tierColor
                        )
                    }
                }
            }

            if (logs.isEmpty()) {
                // Empty state layout beautifully styled
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.02f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📔", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ডায়েরি এখনো খালি!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "ধূমপানের তীব্র ইচ্ছে হলে 'ইচ্ছে সামলেছি' বাটনে চেপে রেজিস্টার করুন।",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Latest log preview card
                val latest = logs.first()
                val severityColor = when (latest.severity) {
                    "তীব্র" -> TerracottaWarn
                    "মাঝারি" -> AccentSky
                    else -> PrimaryMint
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.02f))
                        .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Colored accent bar specifying severity
                        Box(
                            modifier = Modifier
                                .width(5.dp)
                                .height(95.dp)
                                .background(severityColor)
                        )
                        
                        Column(modifier = Modifier.padding(14.dp).weight(1f)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "সর্বশেষ প্রতিহত ট্রিগার",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = severityColor
                                )
                                Text(
                                    text = formatEpochTime(latest.timestamp),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ট্রিগার: ${latest.trigger}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(
                                    modifier = Modifier
                                        .background(
                                            severityColor.copy(alpha = 0.12f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "তীব্রতা: ${latest.severity}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = severityColor
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "প্রতিরোধ কৌশল: ${latest.copingMethod}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                            )
                        }
                    }
                }

                // Expanded historical logs layout dynamically inside card
                AnimatedVisibility(
                    visible = isExpandedHistory,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "বিগত ইতিহাস (সর্বমোট: $totalResisted বার প্রতিরোধ)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            
                            TextButton(
                                onClick = { viewModel.clearAllCravingLogs() },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red.copy(alpha = 0.8f))
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Clear All", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "সব মুছুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Box/Column of history records
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            logs.drop(1).take(5).forEach { log ->
                                val histSeverityColor = when (log.severity) {
                                    "তীব্র" -> TerracottaWarn
                                    "মাঝারি" -> AccentSky
                                    else -> PrimaryMint
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.015f))
                                        .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .height(54.dp)
                                                .background(histSeverityColor)
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = log.trigger,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onBackground
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .background(histSeverityColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    ) {
                                                        Text(
                                                            text = log.severity,
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = histSeverityColor
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = "কৌশল: ${log.copingMethod}",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                                )
                                            }
                                            Text(
                                                text = formatEpochTime(log.timestamp),
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
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

@Composable
fun RecoveryTimelineHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        Text(
            text = "ধূমপান ছাড়ার পর শরীরের দুর্দান্ত উন্নতি",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "আপনার শরীর পুনরায় প্রাণবন্ত হওয়া শুরু করেছে। কোন ধাপে কতটুকু রিকভারি হলো দেখে নিন:",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            lineHeight = 16.sp
        )
    }
}

@Composable
fun TimelineMilestoneRow(
    item: HealthMilestone,
    currentSecondsElapsed: Long
) {
    var expanded by remember { mutableStateOf(false) }
    val isCompleted = currentSecondsElapsed >= item.secondsNeeded

    val remainingSeconds = item.secondsNeeded - currentSecondsElapsed
    val remainingText = if (remainingSeconds > 0) {
        val days = remainingSeconds / (24 * 3600)
        val hours = (remainingSeconds % (24 * 3600)) / 3600
        val minutes = (remainingSeconds % 3600) / 60
        val secs = remainingSeconds % 60
        when {
            days > 0 -> "আর মাত্র $days দিন $hours ঘণ্টা বাকি"
            hours > 0 -> "আর মাত্র $hours ঘণ্টা $minutes মিনিট বাকি"
            else -> "আর মাত্র $minutes মিনিট $secs সেকেন্ড বাকি"
        }
    } else ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .shadow(if (expanded) 3.dp else 1.dp, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) PrimaryMint.copy(alpha = 0.02f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCompleted) SecondaryEmerald.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isCompleted) "🏆" else "🔒",
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isCompleted) "${item.timeLabel} (অর্জিত!)" else item.timeLabel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isCompleted) SecondaryEmerald else MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.shortBenefit,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!isCompleted && remainingText.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .background(TerracottaWarn.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "লকড",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaWarn
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand details",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .background(
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.03f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = "স্বাস্থ্যের উপর প্রভাব ও বৈজ্ঞানিক তথ্য:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryMint
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.fullDetailBengali,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!isCompleted) {
                        val progress = currentSecondsElapsed.toFloat() / item.secondsNeeded.toFloat()
                        val clampedProgress = progress.coerceIn(0f, 1f)
                        Column {
                            LinearProgressIndicator(
                                progress = { clampedProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = PrimaryMint,
                                trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = remainingText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracottaWarn
                                )
                                Text(
                                    text = "অগ্রগতি: ${(clampedProgress * 100).toInt()}%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryMint
                                )
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                tint = SecondaryEmerald,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "এই রিকভারি ধাপটি আপনি সফলভাবে সম্পন্ন করেছেন!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SecondaryEmerald
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EducationalTabsSection() {
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        // Tab Selection Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.03f),
                    RoundedCornerShape(16.dp)
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabTitles = listOf("কেন ছাড়বেন?", "ক্ষতিকর প্রভাব", "সহায়ক টিপস")
            tabTitles.forEachIndexed { index, title ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedTab == index) PrimaryMint else Color.Transparent)
                        .clickable { selectedTab = index }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == index) Color.White else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic Contents switching
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
            },
            label = "Educational Content Switcher"
        ) { targetIndex ->
            when (targetIndex) {
                0 -> ReasonsToQuitContent()
                1 -> HarmfulEffectsContent()
                2 -> QuickTipsToQuitContent()
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
        Spacer(modifier = Modifier.height(16.dp))

        // Trigger Selector Section (Interactive Tool)
        Text(
            text = "⚡ আপনার মূল ধূমপানের ট্রিগার সনাক্ত করুন",
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "ধূমপানের ইচ্ছেগুলো একেক সময় একেক কারণে অনিয়ন্ত্রিত হয়। আপনার ক্ষেত্রে কোনটি বেশি সচল? সিলেক্ট করে তাৎক্ষণিক বৈজ্ঞানিক সমাধানটি জেনে নিন:",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            lineHeight = 15.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        var activeTriggerIndex by remember { mutableStateOf<Int?>(null) }
        val haptic = LocalHapticFeedback.current

        val triggers = remember {
            listOf(
                Triple("☕", "চা/কফি পরবর্তী", "সকালের বা বিকেলের আড্ডায় চায়ের বা কফির পর ধূমপানের ইচ্ছা জাগলে"),
                Triple("💼", "কাজের ক্লান্তি ও স্ট্রেস", "অফিস বা পড়ার অতিরিক্ত চাপে বা টেনশনে নিজেকে সতেজ রাখতে"),
                Triple("👥", "বন্ধুদের অনুরোধ / সোশ্যাল", "আড্ডায় বা বন্ধুদের দেখাদেখি অবচেতন মনের সামাজিক ট্রিগার"),
                Triple("🛋️", "অবসর সময় ও অলসতা", "অলস বসে থাকা অবস্থায় হাত ও মুখ অলস থাকার ট্রিগার")
            )
        }

        // Horizontal scrolling chips
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            triggers.forEachIndexed { index, trigger ->
                val isSelected = activeTriggerIndex == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) PrimaryMint.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.03f)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) PrimaryMint else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            activeTriggerIndex = if (isSelected) null else index
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = trigger.first, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = trigger.second,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) PrimaryMint else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        if (activeTriggerIndex != null) {
            Spacer(modifier = Modifier.height(12.dp))
        }

        AnimatedVisibility(
            visible = activeTriggerIndex != null,
            enter = expandVertically(animationSpec = spring()) + fadeIn(),
            exit = shrinkVertically(animationSpec = spring()) + fadeOut()
        ) {
            activeTriggerIndex?.let { index ->
                val triggerInfo = triggers[index]
                val scientificSolution = when (index) {
                    0 -> "১. চায়ের প্রথম চুমুক দিয়ে সিগারেট জ্বালানোর পরিবর্তে লিলি বা লেমনগ্রাস ভেষজ চা পান করুন। অথবা চায়ের পর মুখে সাথে সাথে একটি চুইঙ্গাম বা আদা কুচি দিয়ে দিন যেন হাত ও মুখ সচল থাকে।\n২. চায়ের স্থানটি পরিবর্তন করুন।"
                    1 -> "১. যখনই অফিসের কাজের চাপ অনুভব করবেন, ৪ সেকেন্ডে বুক ভরে শ্বাস নিন, ৭ সেকেন্ড বুক আটকে রাখুন এবং ৮ সেকেন্ডে মুখ দিয়ে ফুঁ দিয়ে ছাড়ুন (৪-৭-৮ শ্বাসের রুটিন)।\n২. কাজের মাঝখানে ৩ মিনিট হালকা হেঁটে আসুন।"
                    2 -> "১. বন্ধুদের আড্ডায় সরাসরি হাসিমুখে বলুন: 'আমি ধূমপানমুক্ত জীবন বেছে নিয়েছি এবং আজ আমার ১০ম দিন!' আপনার এই দৃঢ়তা দেখে বন্ধুরা আপনাকে আর অফার করবে না।\n২. আড্ডার প্রথম ১০ মিনিট একটি মাউথ ফ্রেশনার বা লবঙ্গ মুখে রাখুন।"
                    else -> "১. অবসর সময়ে হাত ফাঁকা লাগলে একটি স্ট্রেস-বল ঘোরান বা বাবল গেম খেলুন বা ডায়েরি লিখুন।\n২. একাকীত্ব কাটাতে কোনো গঠনমূলক কাজে জড়িয়ে পড়ুন বা নিজের রিকভারি স্ট্যাটাসটি স্ক্রিনশট নিয়ে রাখুন।"
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryMint.copy(alpha = 0.05f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = PrimaryMint.copy(alpha = 0.25f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "💡", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${triggerInfo.second} কাটানোর জাদুকরী বৈজ্ঞানিক কৌশল:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryMint
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = scientificSolution,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReasonsToQuitContent() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        EducationalItem(
            icon = "🫁",
            title = "ফুসফুস ও শ্বাসের ক্ষমতা পুনরুদ্ধার",
            shortDesc = "নিকোটিন ধোঁয়া ফুসফুসের ক্ষমতা কমিয়ে শ্বাস রুখে দেয়।",
            detailedDesc = "ধূমপান ছাড়ার মাত্র ১২ ঘণ্টার মধ্যে রক্তে ক্ষতিকর কার্বন মনোক্সাইডের মাত্রা স্বাভাবিক হয়ে আসে। প্রথম ৩ মাসের মধ্যে ফুসফুসের কার্যকারিতা প্রায় ৩০% পর্যন্ত বৃদ্ধি পেয়ে কর্মক্ষমতা নিশ্চিত করে।",
            actionPlan = "যখনই কায়িক পরিশ্রমে বা দ্রুত হাঁটতে গিয়ে সমস্যা অনুভব করবেন, প্রতিদিন সকালে ও রাতে কমপক্ষে ৩ বার করে আমাদের ফুসফুস রিভাইভ ব্রিদিং ব্যায়ামটি সম্পূর্ণ করুন।"
        )
        EducationalItem(
            icon = "💖",
            title = "রক্তসঞ্চালন ও হৃদযন্ত্রের পরম সুরক্ষা",
            shortDesc = "নিকোটিন রক্তনালীকে মারাত্মকভাবে কুঞ্চিত ও শক্ত করে তোলে।",
            detailedDesc = "ধূমপান ত্যাগের মাত্র ২০ মিনিটের মাথায় হৃদস্পন্দন ও রক্তচাপ স্বাভাবিক ধারায় নেমে আসে। ১ বছরের মধ্যে হৃদরোগ বা স্ট্রোকের ঝুঁকি গড়ে শতকরা ৫০ ভাগ পর্যন্ত হ্রাস পায় যা অবিশ্বাস্যভাবে নিরাপদ জীবন উপহার দেয়।",
            actionPlan = "হৃদস্পন্দনের গতি ও ফুসফুসের রক্তচলাচল নিয়মিত স্বাভাবিক রাখতে দিনে অন্তত ১৫-২০ মিনিট দ্রুত গতিতে হাঁটার অভ্যাস গড়ুন এবং ধূমপানমুক্ত জীবন উপভোগ করুন।"
        )
        EducationalItem(
            icon = "⏳",
            title = "১০ বছর পর্যন্ত দীর্ঘ ও সতেজ দীর্ঘায়ু",
            shortDesc = "৩৫ বছরের পূর্বে তামাক বর্জন করলে ১০ বছর প্রত্যাশিত আয়ু বাড়ে।",
            detailedDesc = "গবেষণায় প্রতীয়মান হয় যে, যেকোনো বয়সে তামাক বর্জন দীর্ঘস্থায়ী রোগ প্রতিরোধে কার্যকর। ৩৫ বছর বয়সের আগে সম্পূর্ণভাবে তামাক ত্যাগ করতে পারলে একজন ধূমপায়ীর স্বাভাবিক মানুষের তুল্য দীর্ঘায়ু ফিরে পাওয়ার সুযোগ নিশ্চিত হয়।",
            actionPlan = "আজই আপনার ডায়েরিতে বা আমাদের অ্যাপের অভিযান লগবুকে আপনার লক্ষ্য ও প্রতিশ্রুতি সুন্দরভাবে লিখে রাখুন এবং প্রতিদিন ত্যাগের অগ্রগতিতে শুভকামনা জানান নিজেকে।"
        )
    }
}

@Composable
fun HarmfulEffectsContent() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        EducationalItem(
            icon = "☠️",
            title = "মস্তিষ্ক ও চরম ডোপামিন আসক্তির ফাঁদ",
            shortDesc = "নিকোটিন মস্তিষ্কে কৃত্রিম ডোপামিন ছড়িয়ে মারাত্মক বিভ্রান্তি তৈরি করে।",
            detailedDesc = "নিকোটিন খুব দ্রুত রক্তের মাধ্যমে মস্তিষ্কে ডোপামিন নামক এক রাসায়নিক উপাদান তৈরি করে অবাস্তব প্রশান্তি জোগায়। দীর্ঘমেয়াদে মগজ নিষ্ক্রিয় হতে থাকে, যা প্রাকৃতিক আনন্দে অনুভূতির ঘাটতি ও আসক্তি তৈরি করে ধূমপানে জিম্মি করে ফেলে।",
            actionPlan = "ডোপামিন ঘাটতি এড়াতে কফি বা মিষ্টি ফ্লেভার এর পরিবর্তে বেশি করে সতেজ পানি এবং লেবুর রস ও ফলমূল খান যাতে মন সতেজ ও প্রফুল্ল থাকে।"
        )
        EducationalItem(
            icon = "💔",
            title = "মরণঘাতী রাসায়নিক ও ধমনীর প্লাক সৃষ্টি",
            shortDesc = "সিগারেটে থাকা রেশ আর্সেনিক ও সীসা ধমনীদের চিরতরে সংকীর্ণ করে দেয়।",
            detailedDesc = "ধোঁয়ার সাথে প্রায় ৭০টির অধিক ক্যান্সার-সৃষ্টিকারী ক্ষারক যেমন আর্সেনিক, ক্যাডমিয়াম এবং ফরমালডিহাইড রক্তনালীতে সঞ্চিত হয়ে স্থায়ী প্লাক বা চর্বির স্তর প্রলেপ সৃষ্টি করে, যা হৃদযন্ত্র বিকল বা চিরস্থায়ী প্যারালাইসিস ঘটাতে পারে।",
            actionPlan = "টক্সিন ও বিষাক্ত ধাতব অংশগুলো শরীর থেকে তাড়াতাড়ি ফিল্টার করে বের করে দিবার লক্ষ্যে দিনে কমপক্ষে ৮-১০ গ্লাস বিশুদ্ধ পানি পান করুন।"
        )
        EducationalItem(
            icon = "💨",
            title = "ফুসফুসের স্থায়ী অ্যালভিওলাই বিকল (COPD)",
            shortDesc = "স্থায়ী কফ এবং সিওপিডি (COPD) ধমকের মতো ফুসফুসের পথ বন্ধ করে দেয়।",
            detailedDesc = "ধূমপান ফুসফুসের ক্ষুদ্র বায়ুথলি বা ‘অ্যালভিওলাই’ চিরতরে নষ্ট করে দেয়। ফলে বাতাস থেকে অক্সিজেন গ্রহণের সক্ষমতা কমতে কমতে একসময় অবশ কফ ও সিওপিডি নামক যন্ত্রণাদায়ক রোগে মানুষকে মৃত্যুর কোলে টেনে নেয়।",
            actionPlan = "ধূমপানের উদ্রেক হওয়া বাতাসে দীর্ঘ ক্ষণ ধরে বাইরে খোলা আকাশের নিচে মৃদু হাত-পা নেড়ে বুক ভরে ফ্রেশ অক্সিজেন নিয়ে বুক প্রসারিত করার চেষ্টা করুন।"
        )
    }
}

@Composable
fun QuickTipsToQuitContent() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        EducationalItem(
            icon = "⏱️",
            title = "১০ মিনিটের সুবর্ণ বৈজ্ঞানিক ‘ডিলে’ রুল",
            shortDesc = "তীব্র ইচ্ছে জাগলে ঘড়ি মিলিয়ে ১০ মিনিট অপেক্ষা অত্যন্ত কার্যকর।",
            detailedDesc = "তামাকের আকাঙ্ক্ষা বা ক্র্যাভিং মস্তিষ্কে স্থায়ী ও নিয়ন্ত্রণহীন অনুভূতি নয়, এটি সাধারণত ৩ থেকে সর্বোচ্চ ৫ মিনিটের মধ্যে প্রশমিত হয়ে যায়। তাই এই মুহূর্তটি সচেতনতায় পার করা গেলে আসক্তি এড়ানো অত্যন্ত সহজ হয়।",
            actionPlan = "তীব্র ক্র্যাভিং হলে সাথে সাথে আমাদের ‘ইচ্ছে সামলেছি’ অভিযান বুথ এ ক্লিক করুন এবং নিজের হাতকে সচল রাখতে একটু জোরে জোরে নিশ্বাস টানুন।"
        )
        EducationalItem(
            icon = "🫚",
            title = "হাতে ও মুখে বিকল্প স্বাদের ভেষজ কুচি চিবানো",
            shortDesc = "লবঙ্গ, ললিপপ, এলাচ বা আদা চিবিয়ে মুখের স্বাদ বৈপ্লবিক বদলানো সম্ভব।",
            detailedDesc = "যখন হাত বা মুখের পেশিগুলো সিগারেটের অভ্যাস খোঁজে, তখন আদা কুচি, লবঙ্গ বা পুদিনা পাতা মুখে দিলে মুখের ঝাঁঝালো স্বাদ চমৎকার উদ্দীপনা দেয় যা তামাকের গন্ধ এবং আসক্তি উভয়কেই দূরে ঠেলে দিতে সাহায্য করে।",
            actionPlan = "সব সময় আপনার পকেটে বা ব্যাগে ছোট কোটায় লবঙ্গ বা এলাচ সংগ্রহে রাখুন। যখনই অবচেতনভাবে ধূমপানের ইচ্ছা হবে, সাথে সাথে মুখে একটি কুচি দিয়ে দিন।"
        )
        EducationalItem(
            icon = "🛡️",
            title = "ট্রিগার জোন ও সামাজিক পরিবেশ কঠোর এড়ানো",
            shortDesc = "ধূমপানের উদ্দীপনা জাগায় এমন আড্ডা বা কাজের স্থান কিছুদিন এড়িয়ে চলুন।",
            detailedDesc = "স্মরণ রাখুন, আগের যেই অভ্যাস আপনার মনের অবচেতন অংশে ধূমপানের ডাক দিত—যেমন কাজের বিরতি বা ধোঁয়াটে আড্ডা—পুনরায় সেখানে ফিরে গেলে মন দুর্বল হয়ে পড়ে। ট্রিগার থেকে দূরে থাকা মানেই জয়ের প্রথম ধাপ।",
            actionPlan = "যারা আপনাকে তামাক ছাড়তে উৎসাহ দেয় শুধু তাদের সাথে সময় কাটান এবং কাজের ফাঁকে সিগারেটের দোকানের আড্ডা পুরোপুরি এড়িয়ে চলে স্বাস্থ্যকর কোনো বিকল্প খাবার বা পানীয় উপভোগ করুন।"
        )
    }
}

@Composable
fun EducationalItem(
    icon: String,
    title: String,
    shortDesc: String,
    detailedDesc: String,
    actionPlan: String
) {
    var isExpanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isExpanded = !isExpanded
            }
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) {
                PrimaryMint.copy(alpha = 0.04f)
            } else {
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.02f)
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isExpanded) PrimaryMint.copy(alpha = 0.15f)
                            else PrimaryMint.copy(alpha = 0.08f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isExpanded) PrimaryMint else MaterialTheme.colorScheme.onBackground
                    )
                    if (!isExpanded) {
                        Text(
                            text = shortDesc,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand Status",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(10.dp))

                // Short Description
                Text(
                    text = shortDesc,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Detailed Scientific Explanation Box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.03f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(10.dp)
                ) {
                    Text(text = "🔬", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "বিজ্ঞানসম্মত সত্যটি:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryMint
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = detailedDesc,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // First Steps Action Block
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            SecondaryEmerald.copy(alpha = 0.04f),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = SecondaryEmerald.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(10.dp)
                ) {
                    Text(text = "🎯", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "আপনার প্রথম পদক্ষেপ (Action Plan):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SecondaryEmerald
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = actionPlan,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomizeTimerDialog(
    profile: QuitProfile,
    onDismiss: () -> Unit,
    onSave: (String, Long, Int, Double) -> Unit
) {
    val initialOffsetMs = System.currentTimeMillis() - profile.quitTimestamp
    val initialOffsetSafe = if (initialOffsetMs > 0) initialOffsetMs else 0L
    val initialDays = initialOffsetSafe / (1000L * 60 * 60 * 24)
    val initialHours = (initialOffsetSafe % (1000L * 60 * 60 * 24)) / (1000L * 60 * 60)
    val initialMinutes = (initialOffsetSafe % (1000L * 60 * 60)) / (1000L * 60)

    var userNameInput by remember { mutableStateOf(profile.userName) }
    var inputDays by remember { mutableStateOf(initialDays.toString()) }
    var inputHours by remember { mutableStateOf(initialHours.toString()) }
    var inputMinutes by remember { mutableStateOf(initialMinutes.toString()) }
    var inputCigarettesPerDay by remember { mutableStateOf(profile.cigarettesPerDay.toString()) }
    var inputPricePerCigarette by remember { mutableStateOf(profile.pricePerCigarette.toInt().toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .shadow(10.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Calendar Layout Info",
                    tint = PrimaryMint,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "প্রোফাইল ও ট্র্যাকার কাস্টমাইজ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "আপনার সঠিক হিসাব বজায় রাখতে নিচের তথ্যগুলো প্রদান করুন।",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Custom User Name Field
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "আপনার নাম / ছদ্মনাম",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryMint
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = userNameInput,
                        onValueChange = { userNameInput = it },
                        singleLine = true,
                        placeholder = { Text("ধূমপানমুক্ত যোদ্ধা", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryMint)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quit Duration Fields
                Text(
                    text = "কতদিন আগে ধূমপান ছেড়েছেন?",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryMint,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = inputDays,
                            onValueChange = { inputDays = it.filter { c -> c.isDigit() } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            label = { Text("দিন", fontSize = 9.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryMint)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = inputHours,
                            onValueChange = { inputHours = it.filter { c -> c.isDigit() } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            label = { Text("ঘণ্টা", fontSize = 9.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryMint)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = inputMinutes,
                            onValueChange = { inputMinutes = it.filter { c -> c.isDigit() } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            label = { Text("মিনিট", fontSize = 9.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryMint)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Habits (Cigarettes stats) Fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "দৈনিক ধূমপানের সংখ্যা",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryMint
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = inputCigarettesPerDay,
                            onValueChange = { inputCigarettesPerDay = it.filter { c -> c.isDigit() } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            placeholder = { Text("১০", fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryMint)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "প্রতিটির গড় মূল্য (৳)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryMint
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = inputPricePerCigarette,
                            onValueChange = { inputPricePerCigarette = it.filter { c -> c.isDigit() } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            placeholder = { Text("১৫", fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryMint)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    ) {
                        Text(text = "বাতিল", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val name = if (userNameInput.isNotBlank()) userNameInput else "ধূমপানমুক্ত যোদ্ধা"
                            val d = inputDays.toLongOrNull() ?: 0L
                            val h = inputHours.toLongOrNull() ?: 0L
                            val m = inputMinutes.toLongOrNull() ?: 0L
                            val cigarettes = inputCigarettesPerDay.toIntOrNull() ?: 10
                            val price = inputPricePerCigarette.toDoubleOrNull() ?: 15.0

                            val offsetMs = (d * 24L * 60L * 60L * 1000L) + (h * 60L * 60L * 1000L) + (m * 60L * 1000L)
                            val targetTs = System.currentTimeMillis() - offsetMs
                            onSave(name, targetTs, cigarettes, price)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryMint),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "সংরক্ষণ", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun LogCravingDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    val triggers = listOf("কাজের চাপ/অবসাদ", "বন্ধুদের আড্ডা", "খাবারের পর", "উদ্বেগ/একাকীত্ব", "সকালের চা/কফি")
    val severities = listOf("তীব্র", "মাঝারি", "সামান্য")
    val copingMethods = listOf("গভীর শ্বাস-প্রশ্বাস ব্যায়াম", "১ গ্লাস ঠাণ্ডা পানি পান", "২ মিনিট সতেজ হাঁটাহাঁটি", "মুখরোচক আদা/লবঙ্গ চিবানো")

    var selectedTrigger by remember { mutableStateOf(triggers[0]) }
    var selectedSeverity by remember { mutableStateOf(severities[1]) }
    var selectedCoping by remember { mutableStateOf(copingMethods[0]) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ইচ্ছে দমন ডায়েরি লিখুন",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Text(
                    text = "ধূমপানের তীব্র ইচ্ছে সফলভাবে সামলেছেন! আপনার অভিজ্ঞতা নথিবদ্ধ করুন:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Select Trigger
                Text(
                    text = "১. ধূমপানের ইচ্ছে জাগানোর মূল কারণ (Trigger):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryMint,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(triggers) { trig ->
                        val isSel = trig == selectedTrigger
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSel) PrimaryMint else MaterialTheme.colorScheme.background
                                )
                                .clickable { selectedTrigger = trig }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = trig,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Select Severity
                Text(
                    text = "২. তীব্রতার মাত্রা (Severity Level):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryMint,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    severities.forEach { sev ->
                        val isSel = sev == selectedSeverity
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSel) {
                                        when (sev) {
                                            "তীব্র" -> TerracottaWarn
                                            "মাঝারি" -> AccentSky
                                            else -> PrimaryMint
                                        }
                                    } else MaterialTheme.colorScheme.background
                                )
                                .clickable { selectedSeverity = sev }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sev,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Select Coping method
                Text(
                    text = "৩. ধূমপান এড়াতে আপনি যে পদক্ষেপ নিয়েছেন:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryMint,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    copingMethods.forEach { method ->
                        val isSel = method == selectedCoping
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSel) SecondaryEmerald.copy(alpha = 0.12f) else MaterialTheme.colorScheme.background
                                )
                                .clickable { selectedCoping = method }
                                .border(
                                    width = 1.dp,
                                    color = if (isSel) SecondaryEmerald else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selection State",
                                    tint = if (isSel) SecondaryEmerald else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = method,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) SecondaryEmerald else MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onSave(selectedTrigger, selectedSeverity, selectedCoping) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryMint),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "ডায়েরি সংরক্ষণ করুন", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

// Data holder of body recovery timeline
data class HealthMilestone(
    val timeLabel: String,
    val secondsNeeded: Long,
    val shortBenefit: String,
    val fullDetailBengali: String
)

private fun getTimelineItems(): List<HealthMilestone> {
    return listOf(
        HealthMilestone(
            timeLabel = "২০ মিনিট পর",
            secondsNeeded = 20 * 60L,
            shortBenefit = "রক্তচাপ ও হৃৎকম্পন স্বাভাবিক হয়",
            fullDetailBengali = "নিকোটিন গ্রহণ বন্ধ করে দেওয়ার মাত্র ২০ মিনিটের মাথায় হৃদরোগের প্রাথমিক চালক ধমনীর রক্তচাপ ও ধুকপুকুনি হৃৎকম্পন শান্ত হয়ে স্বাভাবিক ও নিরুদ্বেগ পর্যায়ে নেমে আসে।"
        ),
        HealthMilestone(
            timeLabel = "১২ ঘণ্টা পর",
            secondsNeeded = 12 * 60 * 60L,
            shortBenefit = "কার্বন মনোক্সাইডের মাত্রা স্বাভাবিক হয়",
            fullDetailBengali = "রক্তে জমতে থাকা কার্বন মনোক্সাইড (যা ক্ষতিকর নিকোটিনের দহনে বের হয় এবং লোহিত কণিকায় অক্সিজেন প্রবাহ কমিয়ে দেয়) তার বিষাক্ত মাত্রা সম্পূর্ণ হ্রাস পেয়ে শরীরের রক্তের অক্সিজেন প্রবাহ চমৎকারভাবে স্বাভাবিক স্তরে পৌছায়।"
        ),
        HealthMilestone(
            timeLabel = "৩ দিন পর",
            secondsNeeded = 3 * 24 * 60 * 60L,
            shortBenefit = "শরীরে নিকোটিন নির্মূল ও শ্বাসের সতেজতা",
            fullDetailBengali = "মস্তিষ্ক ও রক্ত থেকে সিংহভাগ নিকোটিন বর্জ্য মূত্রের মাধ্যমে বাইরে চলে যায়। ফুসফুসে জমে থাকা ক্ষতিকর শ্লেষ্মা ও অবরুদ্ধ সিলিয়ার জটলা কেটে দীর্ঘশ্বাস নিতে বুক হালকা ও সম্পূর্ণ সতেজ অনুভূতি দেয়।"
        ),
        HealthMilestone(
            timeLabel = "৩ মাস পর",
            secondsNeeded = 90 * 24 * 60 * 60L,
            shortBenefit = "রক্তসঞ্চালন ও ফুসফুসের ক্ষমতা পুনরুদ্ধার",
            fullDetailBengali = "হৃৎপিণ্ডের রক্তনালীগুলো সম্পূর্ণ সজীব ও নমনীয় হয়ে সারা শরীরে নতুনভাবে পুষ্টি প্রবাহিত করে। কোনো ভারী কাজ বা দ্রুত হাঁটলে ফুসফুসের শৌর্য বেড়ে যাওয়ায় ক্লান্ত বা হাঁপিয়ে ওঠার প্রবণতা বন্ধ হয়ে যায়।"
        ),
        HealthMilestone(
            timeLabel = "১ বছর পর",
            secondsNeeded = 365 * 24 * 60 * 60L,
            shortBenefit = "হার্ট অ্যাটাক ও হৃদরোগের ঝুঁকি অর্ধেকে নামা",
            fullDetailBengali = "বুকের গভীর রক্তনালীর অক্সিজেন প্রবাহ ফিরে পাওয়ায় এবং ধূলিকণা দূর করার ক্ষমতা স্বাভাবিক হওয়ায় আপনার হৃৎপিণ্ড সুরক্ষিত হয়। হৃদরোগের সামগ্রিক ঝুঁকি একজন নিয়মিত ধূমপায়ীর তুলনায় ৫০% চিরতরে হ্রাস পায়।"
        )
    )
}

private fun formatEpochTime(epoch: Long): String {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    return sdf.format(Date(epoch))
}

@Composable
fun SOSGroundingDialog(
    onDismiss: () -> Unit,
    onLogCrisisResisted: (String, String, String) -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0 to 4 represent 5 steps
    val haptic = LocalHapticFeedback.current

    // Keep overall steps checked state
    var step1Completed by remember { mutableStateOf(false) }
    var step2Completed by remember { mutableStateOf(false) }
    var step3Completed by remember { mutableStateOf(false) }
    var step4Completed by remember { mutableStateOf(false) }

    // TIMER states
    var secondsLeft by remember { mutableStateOf(180) }
    var isTimerRunning by remember { mutableStateOf(true) }

    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (secondsLeft > 0) {
                delay(1000L)
                secondsLeft--
            }
        }
    }

    // Step 1: Water states
    var sipsCount by remember { mutableStateOf(0) }
    LaunchedEffect(sipsCount) {
        if (sipsCount >= 3) {
            step1Completed = true
        }
    }

    // Step 2: Breathing states
    var breathCycleCompleted by remember { mutableStateOf(0) }
    var breathingPhase by remember { mutableStateOf("নিষ্ক্রিয়") }
    var breathProgress by remember { mutableStateOf(0f) }
    var breathingIsActive by remember { mutableStateOf(false) }

    LaunchedEffect(breathingIsActive) {
        if (breathingIsActive) {
            while (breathCycleCompleted < 3) {
                // Inhale 4s
                breathingPhase = "শ্বাস নিন (Inhale) 🫁"
                for (i in 1..40) {
                    if (!breathingIsActive) break
                    breathProgress = i / 40f
                    delay(100L)
                }
                if (!breathingIsActive) break
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                // Hold 4s
                breathingPhase = "ধরে রাখুন (Hold) 🔒"
                breathProgress = 1f
                delay(4000L)
                if (!breathingIsActive) break
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                // Exhale 5s
                breathingPhase = "শ্বাস ত্যাগ করুন (Exhale) 🌬️"
                for (i in 40 downTo 1) {
                    if (!breathingIsActive) break
                    breathProgress = i / 40f
                    delay(125L)
                }
                if (!breathingIsActive) break
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                breathCycleCompleted++
            }
            if (breathCycleCompleted >= 3) {
                step2Completed = true
                breathingIsActive = false
                breathingPhase = "সম্পূর্ণ!"
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        } else {
            breathingPhase = "নিষ্ক্রিয়"
            breathProgress = 0f
        }
    }

    // Step 3: Sensory Grounding Game checkboxes/toggles
    var seeList = remember { mutableStateListOf(false, false, false, false, false) }
    var feelList = remember { mutableStateListOf(false, false, false, false) }
    var hearList = remember { mutableStateListOf(false, false, false) }
    var smellList = remember { mutableStateListOf(false, false) }
    var thinkChecked by remember { mutableStateOf(false) }

    val totalSensoryItems = 15
    val completedSensoryItems = seeList.count { it } + feelList.count { it } + hearList.count { it } + smellList.count { it } + (if (thinkChecked) 1 else 0)
    
    LaunchedEffect(completedSensoryItems) {
        if (completedSensoryItems >= totalSensoryItems) {
            step3Completed = true
        }
    }

    // Step 4: Affirmation
    val resolutionQuotes = remember {
        listOf(
            "“ can you believe it? বিষাক্ত নিকোটিন আমার ইচ্ছাশক্তির চেয়ে শক্তিশালী নয়। আমি আজ জিতবই।”",
            "“আমি আমার সুন্দর ফুসফুস ও আদরের পরিবারকে সীমাহীন ভালবাসি, cigaretteকে নয়।”",
            "“প্রতিটি সেকেন্ডের জয় আমাকে ধূমপানমুক্ত চমৎকার জীবনের দিকে নিয়ে যাচ্ছে।”",
            "“নেশা বা সাময়িক তাড়না ক্ষণস্থায়ী, কিন্তু আমার সুস্থ থাকার তৃপ্তি চিরস্থায়ী।”",
            "“আমার দেহ একটি পবিত্র মন্দির, একে নিকোটিনের ধোঁয়ায় দগ্ধ হতে দেবো না।”",
            "“আমি শক্তিশালী, আমি স্বাধীন, আমি আজকের যুদ্ধে একজন সফল বিজয়ী।”"
        )
    }
    var currentQuoteIdx by remember { mutableStateOf(0) }
    var resolutionAccepted by remember { mutableStateOf(false) }
    LaunchedEffect(resolutionAccepted) {
        if (resolutionAccepted) {
            step4Completed = true
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .shadow(16.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Warning badge header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(TerracottaWarn.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "SOS Header Alert",
                            tint = TerracottaWarn,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "তীব্র ইচ্ছা প্রশমন গাইড (SOS)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = TerracottaWarn
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Beautiful Progressive Horizontal Stepper Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val stepTitles = listOf("১. জল", "২. শ্বাস", "৩. পঞ্চইন্দ্রিয়", "৪. সংকল্প", "৫. টাইমার")
                    val stepStatus = listOf(step1Completed, step2Completed, step3Completed, step4Completed, secondsLeft == 0)

                    stepTitles.forEachIndexed { idx, label ->
                        val isActive = activeTab == idx
                        val isDone = stepStatus[idx]
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    activeTab = idx
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .background(
                                        when {
                                            isDone -> PrimaryMint
                                            isActive -> TerracottaWarn.copy(alpha = 0.9f)
                                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                        },
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Done",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else {
                                    Text(
                                        text = (idx + 1).toString(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = label,
                                fontSize = 9.sp,
                                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isActive) TerracottaWarn else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        if (idx < 4) {
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(1.dp)
                                    .background(
                                        if (isDone) PrimaryMint else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable container for different active pages
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    when (activeTab) {
                        0 -> {
                            // Page 1: Water hydration simulation game
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "💧 ১ গ্লাস ঠান্ডা পানির স্পর্শ",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "ঠান্ডা পানি মুখের ও জিভকে শান্ত করে এবং ধূমপানের তীব্র ইচ্ছে কমিয়ে দেয়। অন্তত ৩ চুমুক পানি পান করে সেশনটি সফল করুন।",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Interactive glass canvas
                                Box(
                                    modifier = Modifier
                                        .size(100.dp, 130.dp)
                                        .border(3.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp, topStart = 4.dp, topEnd = 4.dp))
                                        .background(Color.Transparent)
                                        .padding(4.dp),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    val waterPercent = (3 - sipsCount) / 3f
                                    val waveHeightAnim by animateFloatAsState(
                                        targetValue = waterPercent,
                                        animationSpec = tween(durationMillis = 800)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(waveHeightAnim)
                                            .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color(0xFF81D4FA),
                                                        Color(0xFF0288D1)
                                                    )
                                                )
                                            )
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = if (sipsCount >= 3) "সম্পূর্ণ! আপনি সফলভাবে ঠান্ডা পানি পান করেছেন। 🎉" else "নেওয়া চুমুক: $sipsCount / ৩ বার",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (sipsCount >= 3) PrimaryMint else MaterialTheme.colorScheme.onBackground
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        if (sipsCount < 3) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            sipsCount++
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (sipsCount >= 3) PrimaryMint else AccentSky
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth().height(44.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Favorite, contentDescription = "Sip", tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (sipsCount >= 3) "ধাপ ১ শেষ (পরের ধাপে যান) 👍" else "এক ঢোক পানি পান করুন (চুমুক দিন) 🥛",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        1 -> {
                            // Page 2: Breathing circles
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🌬️ ৩ বার গভীর ফুসফুসের শ্বাসাঘাত",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "৫ সেকেন্ড বুক ভরে শ্বাস টেনে, ৪ সেকেন্ড ধরে রাখুন এবং ধীরে ধীরে মুখ দিয়ে ৮ সেকেন্ড জুড়ে ছাড়ুন।",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 14.sp
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Pulsing core visualizer
                                val breathScaleAnim by animateFloatAsState(
                                    targetValue = if (breathingIsActive) 1f + (breathProgress * 0.8f) else 1f,
                                    animationSpec = tween(durationMillis = 100)
                                )

                                Box(
                                    modifier = Modifier
                                        .size(130.dp)
                                        .shadow(4.dp, CircleShape)
                                        .graphicsLayer {
                                            scaleX = breathScaleAnim
                                            scaleY = breathScaleAnim
                                        }
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    PrimaryMint.copy(alpha = 0.5f),
                                                    AccentSky.copy(alpha = 0.2f)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Breath Loop",
                                            tint = PrimaryMint,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = breathingPhase,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = if (breathCycleCompleted >= 3) "ধাপটি সফলভাবে ৩ বার সম্পন্ন হয়েছে! 🎉" else "সম্পন্ন চক্র: $breathCycleCompleted বার / ৩ বার",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (breathCycleCompleted >= 3) PrimaryMint else MaterialTheme.colorScheme.onBackground
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        if (breathCycleCompleted < 3) {
                                            breathingIsActive = !breathingIsActive
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (breathingIsActive) TerracottaWarn else PrimaryMint
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth().height(44.dp)
                                ) {
                                    Text(
                                        text = when {
                                            breathCycleCompleted >= 3 -> "ধাপ ২ শেষ (পরের ধাপে যান) 👍"
                                            breathingIsActive -> "অনুশীলন বন্ধ করুন ⏸️"
                                            else -> "শ্বাস শুরু করার টাইমার সচল করুন 🫁"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        2 -> {
                            // Page 3: Cognitive sensory mini game
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "🧠 ৫-৪-৩-২-১ পঞ্চ-ইন্দ্রিয় গ্রাউন্ডিং",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "প্রতিটি বক্স স্পর্শ করে বর্তমানে আপনার চারপাশে থাকা বিষয়গুলো খেয়াল করুন এবং শান্ত হন। ($completedSensoryItems/$totalSensoryItems শেষ)",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 13.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(210.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                                ) {
                                    val scrollState = rememberScrollState()
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(scrollState)
                                            .padding(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // 5 See
                                        Column {
                                            Text("👁️ ৫টি দৃশ্যমান সাধারণ বস্তু (ট্যাপ করুন):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TerracottaWarn)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                val seeNames = listOf("দেয়াল ঘড়ি", "হাতপাখা", "মোবাইল", "টেবিল", "ফুলদানী")
                                                seeNames.forEachIndexed { sIdx, name ->
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(if (seeList[sIdx]) PrimaryMint.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                                                            .border(1.dp, if (seeList[sIdx]) PrimaryMint else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                                            .clickable {
                                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                seeList[sIdx] = !seeList[sIdx]
                                                            }
                                                            .padding(vertical = 6.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(text = name, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold, color = if (seeList[sIdx]) PrimaryMint else MaterialTheme.colorScheme.onBackground)
                                                    }
                                                }
                                            }
                                        }

                                        // 4 Feel
                                        Column {
                                            Text("🤚 ৪টি শারীরিক স্পর্শ ও অনুভূতি:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentSky)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                val feelNames = listOf("পায়ের নীচে মাটি", "শরীরে হাওয়া", "কাপড়ের ওজন", "ফোনের মসৃণতা")
                                                feelNames.forEachIndexed { fIdx, name ->
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(if (feelList[fIdx]) AccentSky.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                                                            .border(1.dp, if (feelList[fIdx]) AccentSky else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                                            .clickable {
                                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                feelList[fIdx] = !feelList[fIdx]
                                                            }
                                                            .padding(vertical = 6.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(text = name, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold, color = if (feelList[fIdx]) AccentSky else MaterialTheme.colorScheme.onBackground)
                                                    }
                                                }
                                            }
                                        }

                                        // 3 Hear
                                        Column {
                                            Text("👂 ৩টি পারিপার্শ্বিক শব্দ ও সুর:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryMint)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                val hearNames = listOf("পাখার ঘূর্ণন", "যানবাহনের আওয়াজ", "পাখির ডাক")
                                                hearNames.forEachIndexed { hIdx, name ->
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(if (hearList[hIdx]) PrimaryMint.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                                                            .border(1.dp, if (hearList[hIdx]) PrimaryMint else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                                            .clickable {
                                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                hearList[hIdx] = !hearList[hIdx]
                                                            }
                                                            .padding(vertical = 6.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(text = name, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold, color = if (hearList[hIdx]) PrimaryMint else MaterialTheme.colorScheme.onBackground)
                                                    }
                                                }
                                            }
                                        }

                                        // 2 Smell
                                        Column {
                                            Text("👃 ২টি ঘ্রাণ অনুভব করার চেষ্টা:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                val smellNames = listOf("ঘরের সতেজ হাওয়া", "চায়ের হালকা সুবাস")
                                                smellNames.forEachIndexed { smIdx, name ->
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(if (smellList[smIdx]) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                                                            .border(1.dp, if (smellList[smIdx]) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                                            .clickable {
                                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                                smellList[smIdx] = !smellList[smIdx]
                                                            }
                                                            .padding(vertical = 6.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(text = name, fontSize = 8.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold, color = if (smellList[smIdx]) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground)
                                                    }
                                                }
                                            }
                                        }

                                        // 1 Think
                                        Column {
                                            Text("✨ ১টি ইতিবাচক ধ্রুব সত্য সত্যতা:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TerracottaWarn)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (thinkChecked) TerracottaWarn.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                                                    .border(1.dp, if (thinkChecked) TerracottaWarn else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                                    .clickable {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        thinkChecked = !thinkChecked
                                                    }
                                                    .padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = "“আমার সুস্থ থাকা ও বেঁচে থাকা তামাকের চেয়ে অনেক দামী”", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (thinkChecked) TerracottaWarn else MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(horizontal = 6.dp))
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (step3Completed) "ধাপ ৩ সফল! মনঃসংযোগ সম্পূর্ণ ঘুরে গেছে। 🎉" else "আরও ${totalSensoryItems - completedSensoryItems}টি অনুভব ট্যাপ করতে বাকি",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (step3Completed) PrimaryMint else MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        3 -> {
                            // Page 4: Strategic Motivational Resolution Quotes card
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "✊ শপথ ও অনুপ্রেরণার সংকল্প",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "ইতিবাচক সংকল্প আপনার ভেতরের ইচ্ছাশক্তিকে জাগ্রত ও সচেতন করবে। বাণীটি গভীর বিশ্বাসের সাথে পাঠ করুন:",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 15.sp
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(2.dp, PrimaryMint.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                                    colors = CardDefaults.cardColors(containerColor = PrimaryMint.copy(alpha = 0.05f)),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Idea Icon",
                                            tint = PrimaryMint,
                                            modifier = Modifier.size(26.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = resolutionQuotes[currentQuoteIdx],
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 18.sp
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        
                                        // Rotation button
                                        TextButton(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                currentQuoteIdx = (currentQuoteIdx + 1) % resolutionQuotes.size
                                            }
                                        ) {
                                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Next Quote", modifier = Modifier.size(16.dp), tint = PrimaryMint)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("পরবর্তী শক্তিশালী বাণী 🔄", fontSize = 11.sp, color = PrimaryMint, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        resolutionAccepted = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (resolutionAccepted) PrimaryMint else TerracottaWarn
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth().height(44.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = "Resolution Accept", tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (resolutionAccepted) "সংকল্পবদ্ধ হয়েছি! 👍" else "আমি সংকল্পে অবিচল থাকবো ✊",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        4 -> {
                            // Page 5: Master Countdown and psychological cooling indicators
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "⏳ ইচ্ছা নিরাময়ের ১৮০ সেকেন্ড টাইমার",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "বিজ্ঞান প্রমাণ করে ধূমপানের তীব্র ইচ্ছেটি ৩ মিনিটের বেশি স্থায়ী হয় না। নিজেকে বিজয়ী করতে টাইমার সমাপ্ত হতে দিন।",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 14.sp
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                val minLeft = secondsLeft / 60
                                val secLeft = secondsLeft % 60
                                val timeLabel = String.format(Locale.getDefault(), "%02d:%02d", minLeft, secLeft)
                                
                                // Large cooling timer display
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(TerracottaWarn.copy(alpha = 0.05f))
                                        .border(2.dp, TerracottaWarn.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                        .padding(16.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = timeLabel,
                                            fontSize = 42.sp,
                                            fontWeight = FontWeight.Black,
                                            color = TerracottaWarn,
                                            letterSpacing = 2.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "মস্তিষ্ক শীতল হচ্ছে...",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TerracottaWarn.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Dynamic Psychological facts box
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        val quoteText = when {
                                            secondsLeft > 140 -> "🧠 ১ম মিনিট: আপনার লালা ও গলার স্নায়ুগুলো শান্ত হতে শুরু করেছে। বিষাক্ত রিসেপ্টর সংকেত দুর্বল হয়ে যাচ্ছে।"
                                            secondsLeft > 80 -> "🧬 ২য় মিনিট: ফুসফুসে জমে থাকা কার্বন মনোক্সাইডের টান কেটে অক্সিজেনের প্রবাহ সুষম হচ্ছে। স্নায়বিক উত্তেজনা স্বাভাবিক হচ্ছে।"
                                            secondsLeft > 0 -> "💪 ৩য় মিনিট: চরম সীমানা পার হয়েছে! হরমোনের তাড়না এখন অত্যন্ত ক্ষীণ। আপনার মস্তিষ্ক ডোপামিনের জাল ভেঙে জয় পেলো!"
                                            else -> "🏆 অভিনন্দন! সম্পূর্ণ ১৮০ সেকেন্ডের কঠিন স্নায়বীয় মনস্তাত্ত্বিক যুদ্ধে আপনি বিজয়ী লড়াই লড়েছেন!"
                                        }
                                        Text(
                                            text = quoteText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons footer
                val totalStepsCompleted = (if (step1Completed) 1 else 0) + (if (step2Completed) 1 else 0) + (if (step3Completed) 1 else 0) + (if (step4Completed) 1 else 0)
                val isFullySuccess = totalStepsCompleted >= 4 || secondsLeft == 0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "বন্ধ করুন ❌",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = {
                            if (isFullySuccess) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLogCrisisResisted("SOS তীব্র মানসিক লড়াকু", "তীব্র", "গ্রাউন্ডিং কিট ও тайமர்")
                            } else {
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFullySuccess) PrimaryMint else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text(
                            text = if (isFullySuccess) "আমি বিজয়ী! 🏆" else "পদক্ষেপ শেষ হয়নি",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = if (isFullySuccess) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun QuitAnalyticsDashboard(logs: List<CravingLog>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header with custom icon
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(AccentSky.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Analytics Icon",
                        tint = AccentSky,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "📊 ক্র্যাভিং ও ট্রিগার উপাত্ত বিশ্লেষণ",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "আপনার অবচেতনের ধূমপানের ইচ্ছা ও তার কারণ সমূহের বিশ্লেষণ",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (logs.isEmpty()) {
                // Friendly advice for empty logs
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "No analytic logs empty",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "কোনো ক্র্যাভিং উপাত্ত এখনো রেকর্ড করা হয়নি।",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "ধূমপানের ইচ্ছা এড়ানো প্রতিবার ‘ইচ্ছে সামলেছি’ বা ‘SOS’ বাটনটি প্রেস করে উপাত্ত যোগ করুন। ধীরে ধীরে আপনার প্রধান মানসিক কারণ বিশ্লেষণ এখানে সচল হবে।",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                }
            } else {
                val totalCount = logs.size.toFloat()

                // 1. Top Triggers Breakdown
                val triggerFreq = logs.groupBy { it.trigger }.mapValues { it.value.size }.toList().sortedByDescending { it.second }
                
                Text(
                    text = "শীর্ষ ধূমপানের ট্রিগার পরিস্থিতি:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryMint
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    triggerFreq.take(4).forEach { (trigger, count) ->
                        val ratio = count / totalCount
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.02f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = trigger,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1.5f),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(2f)) {
                                LinearProgressIndicator(
                                    progress = { ratio },
                                    color = PrimaryMint,
                                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${(ratio * 100).toInt()}% ($count बार)",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = PrimaryMint,
                                modifier = Modifier.width(50.dp),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Severity Profile (তীব্র বনাম মাঝারি বনাম সামান্য)
                val severityGroups = logs.groupBy { it.severity.trim() }.mapValues { it.value.size }
                val highCount = severityGroups["তীব্র"] ?: 0
                val midCount = severityGroups["মাঝারি"] ?: 0
                val lowCount = (severityGroups["সামান্য"] ?: 0) + (severityGroups["সাধারণ"] ?: 0)

                Text(
                    text = "ক্র্যাভিং তীব্রতার বিন্যাস:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TerracottaWarn
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // High Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = TerracottaWarn.copy(alpha = 0.02f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TerracottaWarn.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🔥 তীব্র", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TerracottaWarn)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "$highCount বার", fontSize = 12.sp, fontWeight = FontWeight.Black, color = TerracottaWarn)
                            Text(text = "${((highCount / totalCount) * 100).toInt()}%", fontSize = 9.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        }
                    }

                    // Mid Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = AccentSky.copy(alpha = 0.02f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentSky.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "⚡ মাঝারি", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AccentSky)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "$midCount বার", fontSize = 12.sp, fontWeight = FontWeight.Black, color = AccentSky)
                            Text(text = "${((midCount / totalCount) * 100).toInt()}%", fontSize = 9.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        }
                    }

                    // Low Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = SecondaryEmerald.copy(alpha = 0.02f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SecondaryEmerald.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "🌱 সামান্য", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SecondaryEmerald)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "$lowCount বার", fontSize = 12.sp, fontWeight = FontWeight.Black, color = SecondaryEmerald)
                            Text(text = "${((lowCount / totalCount) * 100).toInt()}%", fontSize = 9.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}

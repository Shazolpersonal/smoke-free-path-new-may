@file:OptIn(androidx.compose.animation.ExperimentalAnimationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui

import com.example.data.QuitProfile
import com.example.data.CravingLog
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
                    onOpenLogCraving = { viewModel.setShowLogCraving(true) }
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
            currentTimestamp = state.profile.quitTimestamp,
            onDismiss = { viewModel.setShowCustomize(false) },
            onSave = { newTimestamp -> viewModel.updateQuitTimestamp(newTimestamp) }
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
}

@Composable
fun AffirmationCard(quote: String, onNextQuote: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PrimaryMint.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "✨", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "আজকের ইতিবাচক আত্মবিশ্বাস (Affirmation)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryMint
                    )
                }
                IconButton(
                    onClick = onNextQuote,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Quote",
                        tint = PrimaryMint.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "“$quote”",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                lineHeight = 18.sp,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
fun CountdownWheelDashboard(
    timePassed: TimeRemaining,
    profile: QuitProfile,
    onCustomizeClick: () -> Unit,
    onOpenLogCraving: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
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
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "নিকোটিনমুক্ত সুবর্ণ পথ অতিক্রমণ",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }

                IconButton(
                    onClick = onCustomizeClick,
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

            Spacer(modifier = Modifier.height(24.dp))

            // Beautiful interactive Circular/Symmetrical design representing breathing cell
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                PrimaryMint.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Secondary circular ring trace boundary
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.02f))
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
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryMint,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${timePassed.minutes}মি ${timePassed.seconds}সে",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
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
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
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

                Button(
                    onClick = onOpenLogCraving,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryMint),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "অভিযান লগ",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "ইচ্ছে সামলেছি", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = bubbleColor,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Pulsing Visual Bubble Core
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .shadow(6.dp, CircleShape)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            bubbleColor.copy(alpha = 0.9f),
                                            bubbleColor.copy(alpha = 0.4f)
                                        )
                                    )
                                )
                                .padding(24.dp)
                                .align(Alignment.CenterHorizontally),
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

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "স্থির থাকুন এবং বায়ুর সাথে মনোসংযোগ স্থাপন করুন।",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TextButton(
                            onClick = onCancelBreathing,
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                        ) {
                            Text(text = "ব্যায়াম বন্ধ করুন", fontWeight = FontWeight.Bold)
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

    Box(
        modifier = Modifier
            .weight(1f)
            .shadow(if (isTriggered) 0.dp else 1.dp, RoundedCornerShape(12.dp))
            .background(
                if (isTriggered) PrimaryMint.copy(alpha = 0.15f) else MaterialTheme.colorScheme.background,
                RoundedCornerShape(12.dp)
            )
            .clickable { isTriggered = !isTriggered }
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(PrimaryMint.copy(alpha = 0.04f))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "সর্বশেষ প্রতিহত ট্রিগার",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryMint
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
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        when (latest.severity) {
                                            "তীব্র" -> TerracottaWarn.copy(alpha = 0.15f)
                                            "মাঝারি" -> AccentSky.copy(alpha = 0.15f)
                                            else -> PrimaryMint.copy(alpha = 0.15f)
                                        },
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "তীব্রতা: ${latest.severity}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (latest.severity) {
                                        "তীব্র" -> TerracottaWarn
                                        "মাঝারি" -> AccentSky
                                        else -> PrimaryMint
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "প্রতিরোধ কৌশল: ${latest.copingMethod}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
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
                                text = "বিগত ইতিহাস (সর্বমোট: $totalResisted বারীর প্রতিরোধ)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            
                            TextButton(
                                onClick = { viewModel.clearAllCravingLogs() },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
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
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.02f),
                                            RoundedCornerShape(12.dp)
                                        )
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
                                            Text(
                                                text = "(${log.severity})",
                                                fontSize = 10.sp,
                                                color = when (log.severity) {
                                                    "তীব্র" -> TerracottaWarn
                                                    "মাঝারি" -> AccentSky
                                                    else -> PrimaryMint
                                                }
                                            )
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .shadow(if (expanded) 3.dp else 1.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
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
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Lock,
                        contentDescription = "Status",
                        tint = if (isCompleted) SecondaryEmerald else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = item.timeLabel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isCompleted) SecondaryEmerald else MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = item.shortBenefit,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand details",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
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
                            RoundedCornerShape(10.dp)
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

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!isCompleted) {
                        val progress = currentSecondsElapsed.toFloat() / item.secondsNeeded.toFloat()
                        val clampedProgress = progress.coerceIn(0f, 1f)
                        Column {
                            LinearProgressIndicator(
                                progress = clampedProgress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = PrimaryMint,
                                trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "শারীরিক রূপান্তর অগ্রগতি: ${(clampedProgress * 100).toInt()}%",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        Text(
                            text = "✓ এই রিকভারি ধাপটি আপনি সফলভাবে সম্পন্ন করেছেন!",
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
    }
}

@Composable
fun ReasonsToQuitContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        EducationalItem(
            icon = "🫁",
            title = "ফুসফুস ও শ্বাসের ক্ষমতা বাড়ে",
            description = "ধূমপান ছাড়ার মাত্র কয়েক সপ্তাহের মধ্যে ফুসফুসের কর্মক্ষমতা প্রায় ৩০% বৃদ্ধি পায়। ফলে সিঁড়ি বেয়ে উঠতে বা শারীরিক কসরত করতে গিয়ে হাঁপিয়ে ওঠার প্রবণতা উধাও হয়।"
        )
        EducationalItem(
            icon = "✨",
            title = "ত্বক ও দাঁতের উজ্জ্বলতা বৃদ্ধি",
            description = "নিকোটিন রক্তের স্বাভাবিক প্রবাহে বাধা দেয় যা ত্বককে শুষ্ক ও অকালে বুড়িয়ে দেয়। ধূমপানমুক্ত জীবন শুরু করলে ত্বকের স্বাভাবিক আর্দ্রতা ও দাঁতের উজ্জ্বল্য ফিরে আসে।"
        )
        EducationalItem(
            icon = "⏳",
            title = "দীর্ঘ সতেজ জীবনের নিশ্চয়তা",
            description = "গবেষণায় দেখা গেছে যে ৩৫ বছর বয়সের আগে ধূমপান ছাড়লে মানুষের জীবনের প্রত্যাশিত আয়ুষ্কাল প্রায় ১০ বছর পর্যন্ত বেড়ে যেতে পারে।"
        )
    }
}

@Composable
fun HarmfulEffectsContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        EducationalItem(
            icon = "☠️",
            title = "ক্যানসারের মরণঘাতী ঝুঁকি",
            description = "তামাকে থাকা ক্ষতিকর উপাদান শুধু ফুসফুস নয়, মুখের ক্যানসার, ল্যারিনক্স, গলব্লাডার এবং অগ্ন্যাশয়ে ক্যানসারের ঝুঁকি বহুগুণ বাড়িয়ে তোলে।"
        )
        EducationalItem(
            icon = "💔",
            title = "হৃদরোগ ও ব্রেইন স্ট্রোক",
            description = "নিকোটিন রক্তনালীকে শক্ত ও সংকুচিত করে তোলে। এটি রক্তনালীতে প্লাক জমতে দ্রুত সাহায্য করে, যা মারাত্মক হার্ট অ্যাটাক এবং প্যারালাইসিস স্ট্রোকের অন্যতম প্রধান কারণ।"
        )
        EducationalItem(
            icon = "💨",
            title = "যক্ষ্মা ও চিরস্থায়ী শ্বাসকষ্ট (COPD)",
            description = "ফুসফুসের স্থায়ী মারাত্মক রোগ সিওপিডি (COPD) এর মূল হোতা ধূমপান। এর ফলে ফুসফুসের অ্যালভিওলাই চিরতরে নষ্ট হয়ে শ্বাস নেবার ক্ষমতা কমে যায়।"
        )
    }
}

@Composable
fun QuickTipsToQuitContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        EducationalItem(
            icon = "⏱️",
            title = "১০ মিনিটের সুবর্ণ সূত্র (Delay Rule)",
            description = "যখনই ধূমপানের তীব্র ইচ্ছে জাগবে, ঘড়ির দিকে তাকিয়ে ১০ মিনিট অপেক্ষা করুন। ধূমপানের তীব্র অনুভূতিটি সাধারণত ৩ থেকে ৫ মিনিটের বেশি টিকে থাকে না।"
        )
        EducationalItem(
            icon = "🫚",
            title = "নিকোটিনের বিকল্প মুখরোচক খাবার",
            description = "হাতে এবং মুখে কিছু রাখার বিকল্প অভ্যাস করুন। মুখের স্বাদ পরিবর্তন করার জন্য লবঙ্গ, কাঁচা আদা বা পুদিনা চিবানো চমৎকার সাহায্যকারী।"
        )
        EducationalItem(
            icon = "🛡️",
            title = "ট্রিগার ও ধূমপানের পরিবেশ এড়ানো",
            description = "যে আড্ডা, সামাজিক দল বা কাজের অবসরে ধূমপানের ইচ্ছে হতো, সেই স্থানগুলো কিছুদিন এড়িয়ে চলুন এবং নিজেকে সৃজনশীল কাজে ব্যস্ত রাখুন।"
        )
    }
}

@Composable
fun EducationalItem(icon: String, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(34.dp)
                .clip(CircleShape)
                .background(PrimaryMint.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun CustomizeTimerDialog(
    currentTimestamp: Long,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit
) {
    var inputDays by remember { mutableStateOf("0") }
    var inputHours by remember { mutableStateOf("2") }
    var inputMinutes by remember { mutableStateOf("0") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(8.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Calendar Layout Info",
                    tint = PrimaryMint,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "সময় কাস্টমাইজ করুন",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "সঠিক দিন ও সময়কাল থেকে ট্র্যাক করতে কত সময় আগে শেষ ধূমপান করেছেন তা বলুন।",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "দিন সংখ্যা", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryMint)
                        OutlinedTextField(
                            value = inputDays,
                            onValueChange = { inputDays = it.filter { char -> char.isDigit() } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryMint)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "ঘণ্টা", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryMint)
                        OutlinedTextField(
                            value = inputHours,
                            onValueChange = { inputHours = it.filter { char -> char.isDigit() } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryMint)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "মিনিট", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryMint)
                        OutlinedTextField(
                            value = inputMinutes,
                            onValueChange = { inputMinutes = it.filter { char -> char.isDigit() } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryMint)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                            val d = inputDays.toLongOrNull() ?: 0L
                            val h = inputHours.toLongOrNull() ?: 0L
                            val m = inputMinutes.toLongOrNull() ?: 0L

                            val offsetMs = (d * 24L * 60L * 60L * 1000L) + (h * 60L * 60L * 1000L) + (m * 60L * 1000L)
                            val targetTs = System.currentTimeMillis() - offsetMs
                            onSave(targetTs)
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

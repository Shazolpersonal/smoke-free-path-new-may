@file:OptIn(androidx.compose.animation.ExperimentalAnimationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui

import com.example.data.QuitProfile
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QuitSmokeDashboard(
    modifier: Modifier = Modifier,
    viewModel: QuitViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Trigger Snackbar for success notifications
    LaunchedEffect(state.showSuccessMessage) {
        state.showSuccessMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSuccessMessage()
        }
    }

    // Main structural layout
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
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Health Icon",
                            tint = SecondaryEmerald,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "মুক্ত জীবন",
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryMint,
                            letterSpacing = 1.sp
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
            // Section 1: Hero Welcome / Banner info
            item {
                HeroBanner(profile = state.profile)
            }

            // Section 2: High fidelity customizable Countdown Tracker
            item {
                CountdownDashboard(
                    timePassed = state.timePassed,
                    profile = state.profile,
                    onCustomizeClick = { viewModel.setShowCustomize(true) },
                    onResistCraving = { viewModel.resistCraving() }
                )
            }

            // Section 3: Interactive Deep Breathing & Coping Activities panel
            item {
                BreathingAndCopingCard(
                    breathingState = state.breathingState,
                    onStartBreathing = { viewModel.startBreathingExercise() },
                    onCancelBreathing = { viewModel.cancelBreathingExercise() }
                )
            }

            // Section 4: Body Recovery Timeline Info (How body improves)
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

            // Section 5: Tabbed educational guidance (Tips vs Harm vs Reasons)
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
}

@Composable
fun HeroBanner(profile: QuitProfile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            PrimaryMint.copy(alpha = 0.08f),
                            SecondaryEmerald.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(PrimaryMint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Self Improvement",
                    tint = PrimaryMint,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "স্বাগতম, ${profile.userName}!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "বিষমুক্ত ফুসফুস, স্বাস্থ্যবান দীর্ঘ জীবনের পথে আপনার যাত্রা সফল হোক।",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun CountdownDashboard(
    timePassed: TimeRemaining,
    profile: QuitProfile,
    onCustomizeClick: () -> Unit,
    onResistCraving: () -> Unit
) {
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (timePassed.isQuitDateInFuture) "ধূমপানমুক্ত যাত্রার পরিকল্পনাতি" else "ধূমপানমুক্ত থাকার সময়কাল",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
                
                // Customize Button
                IconButton(
                    onClick = onCustomizeClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(PrimaryMint.copy(alpha = 0.12f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "কাস্টমাইজ করুন",
                        tint = PrimaryMint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time Display Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TimeBox(value = timePassed.days, label = "দিন")
                TimeBox(value = timePassed.hours, label = "ঘণ্টা")
                TimeBox(value = timePassed.minutes, label = "মিনিট")
                TimeBox(value = timePassed.seconds, label = "সেকেন্ড")
            }

            if (timePassed.isQuitDateInFuture) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "পরিকল্পিত সময় থেকে কাউন্টডাউন শুরু হবে।",
                    fontSize = 12.sp,
                    color = TerracottaWarn,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(16.dp))

            // Stats row & Gamified craving defense
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Shield",
                            tint = PrimaryMint,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "আজ রুখে দিয়েছেন:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                    Text(
                        text = "${profile.cravingsResisted} টি তীব্র ইচ্ছা",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryEmerald,
                        modifier = Modifier.padding(start = 24.dp)
                    )
                }

                Button(
                    onClick = onResistCraving,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryMint),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "ইচ্ছা দমন",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "ইচ্ছে সামলেছি!", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TimeBox(value: Long, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(68.dp)
            .background(PrimaryMint.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = String.format("%02d", value),
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = PrimaryMint
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun BreathingAndCopingCard(
    breathingState: BreathingState,
    onStartBreathing: () -> Unit,
    onCancelBreathing: () -> Unit
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
            Text(
                text = "বিরত থাকার মনস্তাত্ত্বিক কার্যকলাপ",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Text(
                text = "নিকোটিনের তীব্র আকর্ষণ এলে ফুসফুসের ক্ষমতা পুনরুদ্ধার এবং আত্মনিয়ন্ত্রণ দীর্ঘ করার একটি অত্যন্ত কার্যকরী উপায় হলো ব্রিদিং বা নিয়মতান্ত্রিক শ্বাস নেওয়া।",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                lineHeight = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (breathingState) {
                is BreathingState.Idle -> {
                    // Instruction layout to initiate Breathing
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PrimaryMint.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "ব্যায়াম টিপস",
                            tint = PrimaryMint,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "বুক ভরে শ্বাস নিয়ে ৪ সেকেন্ড ধরে রাখার একটি ছোট ফুসফুস ব্যায়াম সাইকেল শুরু করুন। এটি মুহূর্তেই ধূমপানের তীব্র ইচ্ছে কমিয়ে আনবে।",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            lineHeight = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onStartBreathing,
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryEmerald),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "ব্যায়াম শুরু"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "শ্বাস-প্রশ্বাস ব্যায়াম শুরু করুন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                is BreathingState.Active -> {
                    // Beautiful animating breathing bubble layout!
                    val scale by animateFloatAsState(
                        targetValue = when (breathingState.phase) {
                            BreathingState.Phase.INHALE -> 1.5f
                            BreathingState.Phase.HOLD -> 1.5f
                            BreathingState.Phase.EXHALE -> 1.0f
                        },
                        animationSpec = tween(durationMillis = 4000, easing = LinearEasing),
                        label = "Bubble Scaling"
                    )

                    val bubbleColor = when (breathingState.phase) {
                        BreathingState.Phase.INHALE -> PrimaryMint
                        BreathingState.Phase.HOLD -> AccentSky
                        BreathingState.Phase.EXHALE -> SecondaryEmerald
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when (breathingState.phase) {
                                BreathingState.Phase.INHALE -> "শ্বাস নিন..."
                                BreathingState.Phase.HOLD -> "ধরে রাখুন..."
                                BreathingState.Phase.EXHALE -> "ধীরে ধীরে ছাড়ুন..."
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = bubbleColor
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // Animating Bubble
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .shadow(8.dp, CircleShape)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            bubbleColor.copy(alpha = 0.9f),
                                            bubbleColor.copy(alpha = 0.5f)
                                        )
                                    )
                                )
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally),
                            contentAlignment = Alignment.Center
                        ) {
                            // Secondary pulsing ring inside
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${breathingState.secondsRemaining}",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Text(
                            text = "সাইকেল শেষ হওয়া পর্যন্ত স্থির থাকুন। ফুসফুস সচল হচ্ছে।",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

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
            Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(12.dp))
            
            // Healthy Quick Tasks Options list
            Text(
                text = "অন্যান্য তাৎক্ষণিক সহায়ক কার্যক্রম:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionPill(icon = "💧", label = "১ গ্লাস পানি")
                QuickActionPill(icon = "🚶", label = "হাঁটাহাঁটি")
                QuickActionPill(icon = "📞", label = "বন্ধুকে কল")
            }
        }
    }
}

@Composable
fun QuickActionPill(icon: String, label: String) {
    var isClicked by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .shadow(if (isClicked) 0.dp else 1.dp, RoundedCornerShape(12.dp))
            .background(
                if (isClicked) PrimaryMint.copy(alpha = 0.15f) else MaterialTheme.colorScheme.background,
                RoundedCornerShape(12.dp)
            )
            .clickable { isClicked = !isClicked }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isClicked) "সম্পন্ন" else label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isClicked) PrimaryMint else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun RecoveryTimelineHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "ধূমপান ছাড়ার পর শরীরের দুর্দান্ত উন্নতি",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "আপনার শরীর পুনরায় প্রাণবন্ত হওয়া শুরু করেছে। কোন ধাপে কতটুকু রিকভারি হলো দেখে নিন:",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
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
    
    // Smooth transition
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

            // Expanded deep information of recovery progress
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
                        .padding(10.dp)
                ) {
                    Text(
                        text = "স্বাস্থ্যের উপর প্রভাব ও বৈজ্ঞানিক তথ্য:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryMint
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.fullDetailBengali,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress bar inside item
                    if (!isCompleted) {
                        val progress = currentSecondsElapsed.toFloat() / item.secondsNeeded.toFloat()
                        val clampedProgress = progress.coerceIn(0f, 1f)
                        Column {
                            LinearProgressIndicator(
                                progress = clampedProgress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
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
        // Tab Selection Header
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
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == index) Color.White else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Content Display
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) with fadeOut(animationSpec = tween(220))
            }
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
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        EducationalItem(
            title = "ফুসফুস ও শ্বাসের ক্ষমতা বাড়ে",
            description = "ধূমপান ছাড়ার মাত্র কয়েক সপ্তাহের মধ্যে ফুসফুসের কর্মক্ষমতা প্রায় ৩০% বৃদ্ধি পায়। ফলে সিঁড়ি বেয়ে উঠতে বা ব্যায়াম করতে গিয়ে হাঁপিয়ে ওঠার প্রবণতা উধাও হয়।"
        )
        EducationalItem(
            title = "ত্বক ও দাঁতের উজ্জ্বলতা বৃদ্ধি",
            description = "নিকোটিন রক্তের স্বাভাবিক প্রবাহে বাধা দেয় যা ত্বককে শুষ্ক ও বুড়িয়ে দেয়। ধূমপান মুক্ত জীবন আপনার ত্বকের সৌন্দর্য ও তরতাজা ভাব ফিরিয়ে আনে।"
        )
        EducationalItem(
            title = "দীর্ঘ জীবনের প্রত্যাশা",
            description = "গবেষণায় দেখা গেছে যে ৩৫ বছর বয়সের আগে ধূমপান ছাড়লে মানুষের জীবনের প্রত্যাশিত আয়ুষ্কাল প্রায় ১০ বছর পর্যন্ত বেড়ে যেতে পারে।"
        )
    }
}

@Composable
fun HarmfulEffectsContent() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        EducationalItem(
            title = "ক্যানসারের মরণঘাতী ঝুঁকি",
            description = "ধূমপানের ফলে শুধু ফুসফুস নয়, মুখের ক্যানসার, ল্যারিনক্স, গলব্লাডার এবং অগ্ন্যাশয়ে ক্যানসারের ঝুঁকি বহুগুণ বেড়ে যায়। এটি ফুসফুসের ফুসকুড়ি নষ্ট করে দেয়।"
        )
        EducationalItem(
            title = "হৃদরোগ ও ব্রেইন স্ট্রোক",
            description = "নিকোটিন রক্তনালীকে সংকুচিত ও শক্ত করে তোলে, যা স্বাভাবিক রক্তপ্রবাহকে রুদ্ধ করে। এটি মারাত্মক হার্ট অ্যাটাক এবং প্যারালাইসিস স্ট্রোকের অন্যতম প্রধান কারণ।"
        )
        EducationalItem(
            title = "যক্ষ্মা ও চিরস্থায়ী শ্বাসকষ্ট (COPD)",
            description = "ফুসফুসের স্থায়ী মারাত্মক রোগ সিওপিডি (COPD) এর মূল হোতা ধূমপান। এর ফলে শেষ বয়সে সার্বক্ষণিক কৃত্রিম অক্সিজেন সিলিন্ডারের উপর নির্ভর হতে হয়।"
        )
    }
}

@Composable
fun QuickTipsToQuitContent() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        EducationalItem(
            title = "১০ মিনিটের সূত্র (Delay Rule)",
            description = "যখনই ধূমপানের তীব্র ইচ্ছে জাগবে, ঘড়ির দিকে তাকিয়ে ১০ মিনিট অপেক্ষা করুন। তীব্র অনুভূতিটি সাধারণত ৩ থেকে ৫ মিনিটের বেশি স্থায়ী হয় না।"
        )
        EducationalItem(
            title = "নিকোটিনের বিকল্প মুখরোচক খাবার",
            description = "হাতে এবং মুখে কিছু রাখার অভ্যাস করুন। মুখের স্বাদ পরিবর্তন করার জন্য লবঙ্গ, কাঁচা আদা বা পুদিনা পাতা চিবানো চমৎকার পরিপূরক।"
        )
        EducationalItem(
            title = "ট্রিগার ও ধূমপানের পরিবেশ এড়ানো",
            description = "যে আড্ডা, চাপ বা কাজের ফাঁকে আপনার ধূমপানের ইচ্ছে হত, সেই মুহূর্তগুলোতে নিজেকে ব্যস্ত রাখার নতুন স্বাস্থ্যকর রুটিন তৈরি করুন।"
        )
    }
}

@Composable
fun EducationalItem(title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(6.dp)
                .background(PrimaryMint, CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
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
                    contentDescription = "Calendar",
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
                    text = "সঠিক ট্র্যাক করার জন্য কত দিন বা সময় আগে ধূমপান ছেড়েছেন তা প্রবেশ করান।",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Custom relative inputs for Days, Hours, Minutes
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

                            // Calculate new epoch timestamp from current time backwards
                            val offsetMs = (d * 24L * 60L * 60L * 1000L) + (h * 60L * 60L * 1000L) + (m * 60L * 1000L)
                            val targetTs = System.currentTimeMillis() - offsetMs
                            onSave(targetTs)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryMint),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                    }
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
            shortBenefit = "রক্তচাপ ও হৃদস্পন্দন স্বাভাবিক হওয়া",
            fullDetailBengali = "সিগারেট ছেড়ে দেওয়ার মাত্র ২০ মিনিটের মধ্যে শরীরের হৃৎপিণ্ড রক্তের পাম্পিং ও চাপ কমিয়ে স্বাভাবিক শান্ত অবস্থায় নিয়ে আসে যা হৃদপিণ্ডের কাজের ধকল কমায়।"
        ),
        HealthMilestone(
            timeLabel = "১২ ঘণ্টা পর",
            secondsNeeded = 12 * 60 * 60L,
            shortBenefit = "কার্বন মনোক্সাইডের মাত্রা স্বাভাবিক হওয়া",
            fullDetailBengali = "রক্তে থাকা অত্যন্ত মারাত্মক গ্যাস কার্বন মনোক্সাইড (যা গ্যাস অণুকে অক্সিজেন পরিবহনে বাধা দেয়) তার বিষাক্ত মাত্রা ঝরে রক্তে অক্সিজেন প্রবাহ দ্বিগুণ বেড়ে যায়।"
        ),
        HealthMilestone(
            timeLabel = "৩ দিন পর",
            secondsNeeded = 3 * 24 * 60 * 60L,
            shortBenefit = "শরীরে নিকোটিন নির্মূল ও শক্তি বৃদ্ধি",
            fullDetailBengali = "৩ দিনের মধ্যে শরীর থেকে প্রায় সিংহভাগ নিকোটিন মূত্রের মাধ্যমে দূর হয়। ফুসফুসে জমে থাকা ক্ষতিকর টক্সিনের প্রবাহ বন্ধ হয়ে ফুসকুড়ির সিলিয়াগুলো সতেজ হয় এবং বাতাস ফুসফুসের গভীরে পৌঁছায়।"
        ),
        HealthMilestone(
            timeLabel = "৩ মাস পর",
            secondsNeeded = 90 * 24 * 60 * 60L,
            shortBenefit = "রক্ত সঞ্চালন স্বভাবিক ও ফুসফুসের সুরক্ষা",
            fullDetailBengali = "রক্তনালীগুলো সজীব হয়ে সারা শরীরে রক্ত সঞ্চালন প্রক্রিয়া তীব্র তরতাজা হয়। ফুসফুসের ময়লা ফেলার প্রাকৃতিক ক্ষমতা বহুলাংশে উজ্জীবিত হয় এবং শ্বাস প্রশ্বাস স্বাভাবিক ও ভারী কাজে হাঁপিয়ে ওঠা বন্ধ হয়।"
        ),
        HealthMilestone(
            timeLabel = "১ বছর পর",
            secondsNeeded = 365 * 24 * 60 * 60L,
            shortBenefit = "হার্ট অ্যাটাকের ঝুঁকি অর্ধেকে নামা",
            fullDetailBengali = "ধূমপান ছেড়ে রক্ত প্রবাহের স্বস্তি ফিরে পাওয়ায় আপনার হৃদরোগ বা হার্ট অ্যাটাকের সামগ্রিক ঝুঁকি একজন নিয়মিত ধূমপায়ীর তুলনায় ৫০% হ্রাস পেয়ে যায়।"
        )
    )
}

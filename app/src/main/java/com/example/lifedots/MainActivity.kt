package com.example.lifedots

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.remember
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedots.ui.theme.LifeDotsTheme
import com.example.lifedots.wallpaper.LifeDotsWallpaperService
import java.util.Calendar

// ── Futuristic font — Roboto Mono (built into every Android device, offline) ──
private val fuentFuturistica = FontFamily.Monospace

// ── Glass card colors ────────────────────────────────────────────────────────
private val GlassWhite   = Color(0xCCFFFFFF)   // translucent white
private val GlassBorder1 = Color(0xFF7DF9FF)   // electric cyan
private val GlassBorder2 = Color(0xFFB0C4DE)   // light steel blue
private val GlassBorder3 = Color(0xFFFFFFFF)   // white
private val GlowColor    = Color(0x447DF9FF)   // soft cyan glow
private val ScrimColor   = Color(0xCC000000)   // dark overlay behind card

// ── Animation specs — file-level so they are never recreated ─────────────────
private val CardEnterTransition =
    fadeIn(tween(300)) +
    scaleIn(
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        initialScale = 0.85f
    )
private val CardExitTransition =
    fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.9f)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LifeDotsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    OnboardingScreen(
                        onSetWallpaper = { openWallpaperPicker() },
                        onOpenSettings = { openSettings() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun openWallpaperPicker() {
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(this@MainActivity, LifeDotsWallpaperService::class.java)
            )
        }
        startActivity(intent)
    }

    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }
}



// ── Credit card composable ───────────────────────────────────────────────────
@Composable
private fun DeveloperCreditCard(onDismiss: () -> Unit) {
    // Hoist all Brush objects so they are created once, not every recomposition
    val borderBrush = remember {
        Brush.linearGradient(
            colors = listOf(GlassBorder1, GlassBorder2, GlassBorder3, GlassBorder1)
        )
    }
    val cardBgBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x44FFFFFF),
                Color(0x22AADDFF),
                Color(0x11FFFFFF)
            )
        )
    }
    val topLineBrush = remember {
        Brush.horizontalGradient(listOf(Color.Transparent, GlassBorder1, Color.Transparent))
    }
    val bottomLineBrush = remember {
        Brush.horizontalGradient(listOf(Color.Transparent, GlassBorder2, Color.Transparent))
    }

    // Full-screen scrim — tap anywhere outside card to dismiss
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScrimColor)
            .clickable(
                indication        = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        // Glass card — stop click propagation so tapping the card itself
        // doesn't also fire the scrim dismiss
        Box(
            modifier = Modifier
                .padding(horizontal = 40.dp)
                .wrapContentSize()
                .clickable(
                    indication        = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { /* consume — don't dismiss when tapping the card */ }
                // Outer glow — hardware-accelerated, actually renders
                .shadow(
                    elevation   = 24.dp,
                    shape       = RoundedCornerShape(24.dp),
                    ambientColor = GlowColor,
                    spotColor    = GlowColor
                )
                // Gradient border
                .border(
                    width  = 1.5.dp,
                    brush  = borderBrush,
                    shape  = RoundedCornerShape(24.dp)
                )
                .clip(RoundedCornerShape(24.dp))
                // Frosted glass body — layered translucencies
                .background(cardBgBrush)
                .padding(horizontal = 32.dp, vertical = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // Thin decorative line above name
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(1.dp)
                        .background(topLineBrush)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // "DEVELOPED BY" label
                Text(
                    text      = "DEVELOPED BY",
                    fontFamily = fuentFuturistica,
                    fontWeight = FontWeight.Bold,
                    fontSize  = 10.sp,
                    letterSpacing = 4.sp,
                    color     = GlassBorder1.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Main name — large Orbitron
                Text(
                    text      = "ROHAN REDDY",
                    fontFamily = fuentFuturistica,
                    fontWeight = FontWeight.Bold,
                    fontSize  = 26.sp,
                    letterSpacing = 2.sp,
                    color     = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Thin decorative line below name
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(1.dp)
                        .background(bottomLineBrush)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // "on earth" in smaller, softer text
                Text(
                    text      = "on earth 🌎",
                    fontFamily = fuentFuturistica,
                    fontWeight = FontWeight.Normal,
                    fontSize  = 13.sp,
                    letterSpacing = 1.sp,
                    color     = Color.White.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Tap to close hint
                Text(
                    text      = "tap to close",
                    fontSize  = 10.sp,
                    letterSpacing = 1.sp,
                    color     = Color.White.copy(alpha = 0.25f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ── Main onboarding screen ───────────────────────────────────────────────────
@Composable
fun OnboardingScreen(
    onSetWallpaper: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dayOfYear  = remember { Calendar.getInstance().get(Calendar.DAY_OF_YEAR) }
    val totalDays  = remember { Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_YEAR) }

    var showCreditCard by remember { mutableStateOf(false) }
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    // Threshold: 80dp worth of pixels in the right direction to trigger
    val swipeThresholdPx = 80.dp

    Box(modifier = modifier.fillMaxSize()) {

        // ── Main content ─────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(32.dp)
                // Swipe detection — left-to-right only
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { dragAccumulator = 0f },
                        onDragEnd   = { dragAccumulator = 0f },
                        onDragCancel = { dragAccumulator = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            dragAccumulator += dragAmount
                            if (dragAccumulator > swipeThresholdPx.toPx()) {
                                showCreditCard = true
                                dragAccumulator = 0f
                            }
                        }
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            DotsPreview(
                dayOfYear = dayOfYear,
                totalDays = 365,
                modifier  = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text       = stringResource(R.string.onboarding_title),
                fontSize   = 36.sp,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text     = stringResource(R.string.onboarding_subtitle),
                fontSize = 18.sp,
                color    = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text        = stringResource(R.string.onboarding_description),
                fontSize    = 16.sp,
                color       = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign   = TextAlign.Center,
                lineHeight  = 24.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text       = stringResource(R.string.days_passed, dayOfYear),
                fontSize   = 14.sp,
                fontWeight = FontWeight.Medium,
                color      = MaterialTheme.colorScheme.primary
            )
            Text(
                text     = stringResource(R.string.days_remaining, totalDays - dayOfYear),
                fontSize = 14.sp,
                color    = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick  = onSetWallpaper,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text       = stringResource(R.string.set_wallpaper),
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick  = onOpenSettings,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape    = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text       = stringResource(R.string.open_settings),
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // ── Credit card overlay ───────────────────────────────────────────────
        AnimatedVisibility(
            visible = showCreditCard,
            enter   = CardEnterTransition,
            exit    = CardExitTransition
        ) {
            DeveloperCreditCard(onDismiss = { showCreditCard = false })
        }
    }
}

// ── Dots preview (unchanged) ─────────────────────────────────────────────────
@Composable
fun DotsPreview(
    dayOfYear: Int,
    totalDays: Int,
    modifier: Modifier = Modifier
) {
    val filledColor = MaterialTheme.colorScheme.onBackground
    val emptyColor  = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
    val todayColor  = Color(0xFF4A90D9)

    Canvas(modifier = modifier) {
        val cols = 15
        val rows = (totalDays + cols - 1) / cols

        val cellSize   = minOf(size.width / cols, size.height / rows)
        val dotRadius  = cellSize * 0.35f
        val gridWidth  = cols * cellSize
        val gridHeight = rows * cellSize
        val startX     = (size.width  - gridWidth)  / 2
        val startY     = (size.height - gridHeight) / 2

        var dotIndex = 0
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                if (dotIndex >= totalDays) break
                val cx    = startX + col * cellSize + cellSize / 2
                val cy    = startY + row * cellSize + cellSize / 2
                val color = when {
                    dotIndex + 1 == dayOfYear -> todayColor
                    dotIndex + 1  < dayOfYear -> filledColor
                    else                      -> emptyColor
                }
                drawCircle(color = color, radius = dotRadius, center = Offset(cx, cy))
                dotIndex++
            }
            if (dotIndex >= totalDays) break
        }
    }
}

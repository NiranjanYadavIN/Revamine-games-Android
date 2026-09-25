package com.revamine.games.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revamine.games.R
import com.revamine.games.ui.theme.RevaIndigoPrimary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isDarkTheme: Boolean,
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Dynamic Light / Dark styling
    val bgColor = if (isDarkTheme) Color(0xFF0B0F19) else Color(0xFFF8FAFC)
    val titlePrimary = if (isDarkTheme) Color.White else Color(0xFF0F172A)
    val titleAccent = if (isDarkTheme) Color(0xFF818CF8) else RevaIndigoPrimary
    val footerFromColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
    val footerCompanyColor = if (isDarkTheme) Color.White else Color(0xFF0F172A)
    val dotColor = if (isDarkTheme) Color(0xFF6366F1) else Color(0xFF4F46E5)

    // Smooth Entrance Scale & Alpha Animation
    val scale = remember { Animatable(0.85f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = LinearEasing)
        )
    }

    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = LinearEasing)
        )
        // Splash display duration: 1.8 seconds, then smooth transition to Main Screen
        delay(1800)
        onSplashFinished()
    }

    // Infinite pulsating animation for 3 loading dots
    val infiniteTransition = rememberInfiniteTransition(label = "dots_transition")
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // ================= Center Branding: App Logo & App Name =================
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .scale(scale.value)
                .alpha(alpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // RevaMine Games Logo (App Controller Logo)
            Image(
                painter = painterResource(id = R.drawable.ic_revamine_logo),
                contentDescription = "RevaMine Games",
                modifier = Modifier.size(110.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // App Title: RevaMine Games (Exact branding casing, zero extra taglines)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "RevaMine",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                    color = titlePrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Games",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                    color = titleAccent
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3 Clean Pulsing Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .alpha(dot1Alpha)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .alpha(dot2Alpha)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .alpha(dot3Alpha)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }

        // ================= Bottom Branding: Option A (from RevaMine) =================
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp + navBarPadding)
                .alpha(alpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "from",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.2.sp,
                color = footerFromColor
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // RevaMine Company Logo (Blue Gradient 'r' Symbol)
                Image(
                    painter = painterResource(id = R.drawable.ic_revamine_company_logo),
                    contentDescription = "RevaMine",
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // RevaMine Company Name (Exact case: RevaMine)
                Text(
                    text = "RevaMine",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp,
                    color = footerCompanyColor
                )
            }
        }
    }
}

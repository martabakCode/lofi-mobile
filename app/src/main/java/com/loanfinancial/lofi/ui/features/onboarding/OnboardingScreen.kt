package com.loanfinancial.lofi.ui.features.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.loanfinancial.lofi.data.local.datastore.DataStoreManager
import com.loanfinancial.lofi.ui.components.LofiButton
import com.loanfinancial.lofi.ui.theme.TextPrimary
import kotlinx.coroutines.launch

/**
 * Determines if a background color is light or dark.
 * Used to automatically adjust text colors for optimal contrast.
 *
 * @return true if the color is considered light (luminance > 0.5)
 */
private fun Color.isLight(): Boolean = this.luminance() > 0.5f

@Composable
fun OnboardingScreen(
    dataStoreManager: DataStoreManager,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
) {
    val pageCount = onboardingSlides.size
    val pagerState =
        rememberPagerState(
            initialPage = 0,
            pageCount = { pageCount },
        )
    val scope = rememberCoroutineScope()

    val currentSlide = onboardingSlides[pagerState.currentPage]
    val backgroundColor = Color(currentSlide.backgroundColor)
    val isBackgroundLight = backgroundColor.isLight()

    // Dynamic colors based on background brightness
    val contentColor = if (isBackgroundLight) TextPrimary else Color.White
    val contentColorSecondary = if (isBackgroundLight) TextPrimary.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.8f)
    val contentColorMuted = if (isBackgroundLight) TextPrimary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.6f)

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(backgroundColor),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Skip Button Row
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    onClick = {
                        scope.launch {
                            dataStoreManager.setFirstInstall(false)
                            onSkip()
                        }
                    },
                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor = contentColorSecondary,
                        ),
                ) {
                    Text(
                        text = "Lewati",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            // Pager Content
            HorizontalPager(
                state = pagerState,
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.Top,
            ) { page ->
                OnboardingPage(
                    slide = onboardingSlides[page],
                    contentColor = if (Color(onboardingSlides[page].backgroundColor).isLight()) TextPrimary else Color.White,
                    contentColorSecondary = if (Color(onboardingSlides[page].backgroundColor).isLight()) TextPrimary.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.fillMaxSize(),
                )
            }

            // Page Indicators
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(pageCount) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier =
                            Modifier
                                .padding(horizontal = 4.dp)
                                .size(
                                    width = if (isSelected) 24.dp else 8.dp,
                                    height = 8.dp,
                                ).clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isSelected) {
                                        contentColor
                                    } else {
                                        contentColor.copy(alpha = 0.4f)
                                    },
                                ),
                    )
                }
            }

            // Bottom CTA Area
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
            ) {
                if (pagerState.currentPage == pageCount - 1) {
                    // Final slide: Show primary button
                    LofiButton(
                        text = "Mulai Sekarang",
                        onClick = {
                            scope.launch {
                                dataStoreManager.setFirstInstall(false)
                                onComplete()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = if (isBackgroundLight) TextPrimary else Color.White,
                        contentColor = if (isBackgroundLight) Color.White else TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = {
                            scope.launch {
                                dataStoreManager.setFirstInstall(false)
                                onSkip()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            ButtonDefaults.textButtonColors(
                                contentColor = contentColorSecondary,
                            ),
                    ) {
                        Text(
                            text = "Sudah punya akun? Login",
                            color = contentColorSecondary,
                        )
                    }
                } else {
                    // Non-final slides: Show Continue button
                    LofiButton(
                        text = "Lanjutkan",
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = if (isBackgroundLight) TextPrimary else Color.White,
                        contentColor = if (isBackgroundLight) Color.White else TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Geser untuk melanjutkan",
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColorMuted,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingPage(
    slide: OnboardingSlide,
    contentColor: Color,
    contentColorSecondary: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Illustration
        Image(
            painter = painterResource(id = slide.illustrationResId),
            contentDescription = slide.title,
            modifier =
                Modifier
                    .size(220.dp)
                    .padding(8.dp),
            contentScale = ContentScale.Fit,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            text = slide.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Description
        Text(
            text = slide.description,
            style = MaterialTheme.typography.bodyLarge,
            color = contentColorSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
            maxLines = 4,
        )

        // Benefits List
        slide.benefits?.let { benefits ->
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                benefits.forEach { benefit ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = benefit,
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColorSecondary,
                        )
                    }
                }
            }
        }

        // Steps
        slide.steps?.let { steps ->
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                steps.forEach { step ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(contentColor.copy(alpha = 0.12f))
                                .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier =
                                Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(contentColor),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = step.number.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(slide.backgroundColor),
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }

        // Trust Badges
        slide.trustBadges?.let { badges ->
            Spacer(modifier = Modifier.height(16.dp))
            @OptIn(ExperimentalLayoutApi::class)
            FlowRow(
                modifier = Modifier.padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 2,
            ) {
                badges.forEach { badge ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = contentColor.copy(alpha = 0.15f),
                        modifier = Modifier.padding(horizontal = 4.dp),
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

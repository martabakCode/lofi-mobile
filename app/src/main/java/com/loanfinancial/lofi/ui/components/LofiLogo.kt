package com.loanfinancial.lofi.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.loanfinancial.lofi.R

/**
 * Reusable LoFi Logo component
 *
 * @param modifier Modifier for customization
 * @param size Size of the logo (width and height)
 * @param alignment Alignment of the logo within the box
 */
@Composable
fun LofiLogo(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    alignment: Alignment = Alignment.Center
) {
    Box(
        modifier = modifier,
        contentAlignment = alignment
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = "LoFi Logo",
            modifier = Modifier.size(size)
        )
    }
}

/**
 * Small logo variant for TopBar
 */
@Composable
fun LofiLogoSmall(
    modifier: Modifier = Modifier
) {
    LofiLogo(
        modifier = modifier,
        size = 32.dp
    )
}

/**
 * Large logo variant for Splash Screen
 */
@Composable
fun LofiLogoLarge(
    modifier: Modifier = Modifier
) {
    LofiLogo(
        modifier = modifier,
        size = 120.dp
    )
}

/**
 * Medium logo variant for Auth Screens
 */
@Composable
fun LofiLogoMedium(
    modifier: Modifier = Modifier
) {
    LofiLogo(
        modifier = modifier,
        size = 64.dp
    )
}
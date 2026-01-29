package com.loanfinancial.lofi.core.common.exception

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.loanfinancial.lofi.ui.theme.LofiTheme

class DeveloperErrorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val crashReport = intent.getStringExtra("crash_report") ?: "No crash report available"

        setContent {
            LofiTheme {
                DeveloperErrorScreen(
                    crashReport = crashReport,
                    onClose = { finishAffinity() },
                    onShare = { shareCrashReport(crashReport) },
                )
            }
        }
    }

    private fun shareCrashReport(report: String) {
        val shareIntent =
            android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_SUBJECT, "Lofi Crash Report")
                putExtra(android.content.Intent.EXTRA_TEXT, report)
            }
        startActivity(android.content.Intent.createChooser(shareIntent, "Share Crash Report"))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeveloperErrorScreen(
    crashReport: String,
    onClose: () -> Unit,
    onShare: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer Mode - Crash Report") },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Red,
                        titleContentColor = Color.White,
                    ),
                actions = {
                    IconButton(onClick = onShare) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White,
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                colors =
                    CardDefaults.cardColors(
                        containerColor = Color(0xFF1E1E1E),
                    ),
            ) {
                Text(
                    text = crashReport,
                    style =
                        MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF00FF00),
                        ),
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                )
            }

            Button(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Close App")
            }
        }
    }
}

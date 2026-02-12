package com.loanfinancial.lofi.ui.features.profile

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.loanfinancial.lofi.R
import com.loanfinancial.lofi.core.util.FileUtil
import com.loanfinancial.lofi.ui.components.LofiButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    isGuest: Boolean = false,
    onLogoutClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onProfileDetailClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onSetPinClick: () -> Unit = {},
    onChangeGooglePinClick: () -> Unit = {},
    onSetGooglePinClick: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    if (isGuest) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.login_to_view_profile),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(24.dp))
            LofiButton(
                text = stringResource(R.string.login_button),
                onClick = onLogoutClick, // Reusing onLogoutClick as "Go to Login"
                modifier =
                    Modifier
                        .width(200.dp)
                        .height(48.dp),
            )
        }
        return
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isLogoutSuccessful) {
        if (uiState.isLogoutSuccessful) {
            onLogoutClick()
        }
    }

    // Image Picking Logic
    val context = LocalContext.current
    var showActionSheet by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val sheetState = rememberModalBottomSheetState()

    // Gallery Launcher
    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            uri?.let {
                FileUtil.from(context, it)?.let { file ->
                    viewModel.onPhotoSelected(file)
                }
            }
            showActionSheet = false
        }

    // Camera Launcher
    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture(),
        ) { success ->
            if (success && tempPhotoUri != null) {
                FileUtil.from(context, tempPhotoUri!!)?.let { file ->
                    viewModel.onPhotoSelected(file)
                }
            }
            showActionSheet = false
        }

    // Permission Launcher
    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (isGranted) {
                val photoFile = createImageFile(context)
                val uri =
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        photoFile,
                    )
                tempPhotoUri = uri
                cameraLauncher.launch(uri)
            }
        }

    if (showActionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showActionSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = null,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column {
                        // Take Photo
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                            val photoFile = createImageFile(context)
                                            val uri =
                                                FileProvider.getUriForFile(
                                                    context,
                                                    "${context.packageName}.fileprovider",
                                                    photoFile,
                                                )
                                            tempPhotoUri = uri
                                            cameraLauncher.launch(uri)
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    }.padding(vertical = 18.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("Take Photo", color = MaterialTheme.colorScheme.primary, fontSize = 20.sp)
                        }

                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

                        // Choose from Library
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        galleryLauncher.launch("image/*")
                                    }.padding(vertical = 18.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("Choose from Library", color = MaterialTheme.colorScheme.primary, fontSize = 20.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Cancel Button
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { showActionSheet = false }
                                .padding(vertical = 18.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Cancel", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp)) // Bottom padding
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // 🖼️ Avatar Area
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp,
                ) {
                    Box(modifier = Modifier.padding(4.dp)) {
                        // Placeholder (Always behind)
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(60.dp),
                            )
                        }

                        // Actual Image
                        AsyncImage(
                            model =
                                ImageRequest
                                    .Builder(LocalContext.current)
                                    .data(uiState.profilePictureUrl)
                                    .crossfade(true)
                                    .build(),
                            contentDescription = "Profile Picture",
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .clickable { if (!isGuest) showActionSheet = true },
                            contentScale = ContentScale.Crop,
                        )

                        // Edit Icon Overlay
                        if (!isGuest) {
                            Box(
                                modifier =
                                    Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .size(32.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        .clickable { showActionSheet = true },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = uiState.name,
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                    ),
            )

            Text(
                text = uiState.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 📋 Profile Options (iOS style list)
            Column(
                modifier =
                    Modifier
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface),
            ) {
                ProfileOptionItem(
                    icon = Icons.Default.PersonOutline,
                    title = "Profile Information",
                    onClick = onProfileDetailClick,
                )
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.outlineVariant)
                ProfileOptionItem(
                    icon = Icons.Default.Edit,
                    title = "Edit Profile",
                    onClick = onEditProfileClick,
                )
                HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.outlineVariant)
                if (!uiState.isGoogleUser) {
                    ProfileOptionItem(
                        icon = Icons.Default.LockOpen,
                        title = "Change Password",
                        onClick = onChangePasswordClick,
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.outlineVariant)
                }
                
                ProfileOptionItem(
                    icon = Icons.Default.LockOpen,
                    title = if (uiState.hasPin) "PIN Management" else "Set PIN",
                    onClick = if (uiState.isGoogleUser) {
                        if (uiState.hasPin) onChangeGooglePinClick else onSetGooglePinClick
                    } else {
                        onSetPinClick
                    },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 🚪 Logout Button
            PaddingValues(horizontal = 20.dp).let {
                LofiButton(
                    text = "Log Out",
                    onClick = { viewModel.onLogout() },
                    isLoading = uiState.isLoading,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.error,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun ProfileOptionItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                    ),
                modifier = Modifier.weight(1f),
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

private fun createImageFile(context: android.content.Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.cacheDir
    return File.createTempFile(
        "JPEG_${timeStamp}_",
        ".jpg",
        storageDir,
    )
}

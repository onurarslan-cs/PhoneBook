package com.example.nexoftcasephonebook.presentation.contacts

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.*
import com.example.nexoftcasephonebook.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.material.icons.filled.Check

private enum class SaveStep { FORM, SAVING, DONE_ANIM, SUCCESS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewContact(
    vm: ContactsViewModel,
    onCancel: () -> Unit,
    onSaved: () -> Unit = {}
) {
    BackHandler { onCancel() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var showPhotoSheet by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    var step by remember { mutableStateOf(SaveStep.FORM) }

    val canSave =
        firstName.isNotBlank() && lastName.isNotBlank() && phone.isNotBlank() && step == SaveStep.FORM

    // Gallery picker
    val pickPhoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) photoUri = uri
    }

    // Camera
    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) photoUri = tempCameraUri
        tempCameraUri = null
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // Root overlay stacking (Form + Sheet + Overlay)
        Box(Modifier.fillMaxSize()) {

            when (step) {
                SaveStep.FORM, SaveStep.SAVING -> {
                    // === FORM UI ===
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        // Top bar
                        Box(Modifier.fillMaxWidth()) {
                            TextButton(
                                onClick = onCancel,
                                modifier = Modifier.align(Alignment.CenterStart)
                            ) { Text("Cancel") }

                            Text(
                                text = "New Contact",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.align(Alignment.Center)
                            )

                            TextButton(
                                enabled = canSave,
                                onClick = {
                                    scope.launch {
                                        step = SaveStep.SAVING
                                        try {
                                            vm.createUser(
                                                firstName = firstName.trim(),
                                                lastName = lastName.trim(),
                                                phoneNumber = phone.trim(),

                                                profileImageUrl = photoUri?.toString().orEmpty()
                                            )
                                            vm.onEvent(ContactsEvent.Load)

                                            // success
                                            step = SaveStep.DONE_ANIM
                                        } catch (e: Exception) {
                                            // error
                                            step = SaveStep.FORM
                                        }
                                    }
                                },
                                modifier = Modifier.align(Alignment.CenterEnd)
                            ) { Text("Done") }
                        }

                        Spacer(Modifier.height(22.dp))

                        // Avatar + Add Photo
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (photoUri != null) {
                                AsyncImage(
                                    model = photoUri,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFD9D9D9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(52.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            TextButton(
                                onClick = { showPhotoSheet = true },
                                enabled = (step == SaveStep.FORM) // saving close
                            ) { Text("Add Photo") }
                        }

                        Spacer(Modifier.height(18.dp))

                        ContactField(
                            value = firstName,
                            onValueChange = { firstName = it },
                            placeholder = "First Name",
                            imeAction = ImeAction.Next
                        )
                        Spacer(Modifier.height(12.dp))
                        ContactField(
                            value = lastName,
                            onValueChange = { lastName = it },
                            placeholder = "Last Name",
                            imeAction = ImeAction.Next
                        )
                        Spacer(Modifier.height(12.dp))
                        ContactField(
                            value = phone,
                            onValueChange = { phone = it },
                            placeholder = "Phone Number",
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        )
                    }

                    // Saving overlay
                    if (step == SaveStep.SAVING) {
                        SavingOverlay()
                    }
                }

                SaveStep.DONE_ANIM -> {
                    DoneLottieFullScreen(
                        rawRes = R.raw.done,
                        onFinished = { step = SaveStep.SUCCESS }
                    )
                }

                SaveStep.SUCCESS -> {
                    SaveSuccessScreen()

                    LaunchedEffect(Unit) {
                        delay(1200)
                        onSaved()
                        onCancel()
                    }
                }
            }

            // Photo source bottom sheet (overlay)
            if (showPhotoSheet) {
                PhotoSourceSheet(
                    onDismiss = { showPhotoSheet = false },
                    onCamera = {
                        showPhotoSheet = false
                        val uri = createImageUri(context)
                        tempCameraUri = uri
                        takePicture.launch(uri)
                    },
                    onGallery = {
                        showPhotoSheet = false
                        pickPhoto.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
            }
        }
    }
}

fun createImageUri(context: Context): Uri {
    val dir = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File(dir, "photo_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

@Composable
private fun ContactField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoSourceSheet(
    onDismiss: () -> Unit,
    onCamera: () -> Unit,
    onGallery: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCamera,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(vertical = 18.dp)
            ) {
                Icon(imageVector = Icons.Outlined.PhotoCamera, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text("Camera", style = MaterialTheme.typography.titleMedium)
            }

            OutlinedButton(
                onClick = onGallery,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(vertical = 18.dp)
            ) {
                Icon(imageVector = Icons.Outlined.PhotoLibrary, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text("Gallery", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(6.dp))

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Cancel", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
fun DoneLottieFullScreen(
    rawRes: Int,
    onFinished: () -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(rawRes))
    val anim = rememberLottieAnimatable()

    LaunchedEffect(composition) {
        if (composition != null) {
            anim.animate(
                composition = composition!!,
                iterations = 1
            )
            onFinished()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { anim.progress },
            modifier = Modifier.size(220.dp)
        )
    }
}

@Composable
fun SaveSuccessScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Green circle + white check
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF19C23E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(Modifier.height(22.dp))

            Text(
                text = "All Done!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "New contact saved 🎉",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SavingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.25f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(12.dp))
                Text("Saving...", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

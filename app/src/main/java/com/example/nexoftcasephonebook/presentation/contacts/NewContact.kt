package com.example.nexoftcasephonebook.presentation.contacts

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewContact(
    vm: ContactsViewModel,
    onCancel: () -> Unit,
    onSaved: () -> Unit
) {
    BackHandler { onCancel() }

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var showPhotoSheet by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val canSave = firstName.isNotBlank() && lastName.isNotBlank() && phone.isNotBlank()
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }


    val pickPhoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            photoUri = uri
        }
    }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri = tempCameraUri
        }
        tempCameraUri = null
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
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


        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
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
                        vm.createUser(
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            phoneNumber = phone.trim(),
                            profileImageUrl = photoUri?.toString() ?: ""
                        )
                        onSaved()
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) { Text("Done") }
            }

            // Avatar + Add Photo
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (photoUri != null) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = null,
                        modifier = Modifier.size(110.dp).clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier.size(110.dp).clip(CircleShape).background(Color(0xFFD9D9D9)),
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
                TextButton(onClick = { showPhotoSheet = true }) { Text("Add Photo") }
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
            // Camera
            OutlinedButton(
                onClick = onCamera,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(vertical = 18.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoCamera,
                    contentDescription = null
                )
                Spacer(Modifier.width(12.dp))
                Text("Camera", style = MaterialTheme.typography.titleMedium)
            }

            // Gallery
            OutlinedButton(
                onClick = onGallery,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                contentPadding = PaddingValues(vertical = 18.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoLibrary,
                    contentDescription = null
                )
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


package com.example.nexoftcasephonebook.presentation.contacts

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactSheet(
    onDismiss: () -> Unit,
    onSave: (firstName: String, lastName: String, phone: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var showAddSheet by remember { mutableStateOf(false) }

    val canSave = firstName.isNotBlank() && lastName.isNotBlank() && phone.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.ime) // pad up
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            // Top bar: Cancel - Title - Done
            Box(Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) { Text("Cancel") }

                Text(
                    text = "New Contact",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.align(Alignment.Center)
                )

                TextButton(
                    onClick = { onSave(firstName.trim(), lastName.trim(), phone.trim()) },
                    enabled = canSave,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) { Text("Done") }
            }

            Spacer(Modifier.height(22.dp))

            // Avatar + Add Photo
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(RoundedCornerShape(55.dp))
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

                Spacer(Modifier.height(10.dp))

                TextButton(onClick = {  showAddSheet = true  }) {
                    Text("Add Photo")
                }
            }

            Spacer(Modifier.height(18.dp))

            // Input fields (iOS style)
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

            Spacer(Modifier.height(24.dp))
        }
    }
}
@Composable
fun AddNewContact(
    onCancel: () -> Unit,
    onSave: (firstName: String, lastName: String, phone: String) -> Unit
) {
    BackHandler { onCancel() } // back

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var showAddSheet by remember { mutableStateOf(false) }

    val canSave = firstName.isNotBlank() && lastName.isNotBlank() && phone.isNotBlank()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Top bar: Cancel - Title - Done
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
                    onClick = { onSave(firstName.trim(), lastName.trim(), phone.trim()) },
                    enabled = canSave,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) { Text("Done") }
            }

            Spacer(Modifier.height(22.dp))

            // Avatar + Add Photo
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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

                Spacer(Modifier.height(10.dp))

                TextButton(onClick = { showAddSheet = true  }) {
                    Text("Add Photo")
                }
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

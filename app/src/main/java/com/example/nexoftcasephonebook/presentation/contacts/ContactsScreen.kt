package com.example.nexoftcasephonebook.presentation.contacts

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nexoftcasephonebook.domain.model.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.filled.CheckCircle
import kotlinx.coroutines.delay
import android.content.ContentProviderOperation
import android.provider.ContactsContract
import android.net.Uri
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

private sealed interface ContactsRoute {
    data object List : ContactsRoute
    data object Add : ContactsRoute
    data class Profile(val contact: Contact) : ContactsRoute
    data class Edit(val contact: Contact) : ContactsRoute
}

@Composable
fun ContactsScreen(vm: ContactsViewModel) {
    val state by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.onEvent(ContactsEvent.Load) }

    var route by remember { mutableStateOf<ContactsRoute>(ContactsRoute.List) }

    when (val r = route) {
        ContactsRoute.List -> ContactsListScreen(
            state = state,
            onAddClick = { route = ContactsRoute.Add },
            onContactClick = { c -> route = ContactsRoute.Profile(c) },
            onEditClick = { c -> route = ContactsRoute.Edit(c) },
            onDeleteClick = { c ->
                vm.deleteUser(c.id)              // ✅ String id
            },
            onQueryChanged = { q -> vm.onEvent(ContactsEvent.QueryChanged(q)) }
        )

        ContactsRoute.Add -> AddNewContact(
            vm = vm,
            onCancel = { route = ContactsRoute.List },
            onSaved = { route = ContactsRoute.List }
        )

        is ContactsRoute.Profile -> ContactProfileScreen(
            contact = r.contact,
            onBack = { route = ContactsRoute.List },
            onEdit = { route = ContactsRoute.Edit(r.contact) },
            onDelete = {
                vm.deleteUser(r.contact.id)
                route = ContactsRoute.List
            }
        )

        is ContactsRoute.Edit -> EditContactScreen(
            vm = vm,
            contact = r.contact,
            onCancel = { route = ContactsRoute.Profile(r.contact) },
            onDone = {
                vm.onEvent(ContactsEvent.Load)
                route = ContactsRoute.List
            }
        )
    }
}


@Composable
private fun ContactsListScreen(
    state: ContactsState,
    onAddClick: () -> Unit,
    onContactClick: (Contact) -> Unit,
    onEditClick: (Contact) -> Unit,
    onDeleteClick: (Contact) -> Unit,
    onQueryChanged: (String) -> Unit
) {
    val groupsSorted = remember(state.grouped) { state.grouped.toSortedMap() }
    val isEmpty = groupsSorted.values.sumOf { it.size } == 0

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            HeaderRow(onAddClick = onAddClick)

            Spacer(Modifier.height(14.dp))

            SearchField(
                value = state.query,
                onValueChange = onQueryChanged
            )

            Spacer(Modifier.height(16.dp))

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                state.error != null -> {
                    Text("Error: ${state.error}")
                }

                isEmpty -> {
                    EmptyContacts(
                        query = state.query,
                        onCreateClick = onAddClick
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        groupsSorted.forEach { (letter, list) ->
                            item(key = "group_$letter") {
                                LetterGroupCard(
                                    letter = letter,
                                    contacts = list,
                                    onContactClick = onContactClick,
                                    onEditClick = onEditClick,
                                    onDeleteClick = onDeleteClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderRow(onAddClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Contacts",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(Modifier.weight(1f))

        IconButton(
            onClick = onAddClick,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("Search by name") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = MaterialTheme.colorScheme.surface,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.surface,
            disabledIndicatorColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun LetterGroupCard(
    letter: Char,
    contacts: List<Contact>,
    onContactClick: (Contact) -> Unit,
    onEditClick: (Contact) -> Unit,
    onDeleteClick: (Contact) -> Unit
) {

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(Modifier.fillMaxWidth()) {
            Text(
                text = letter.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp)
            )

            Divider(color = MaterialTheme.colorScheme.surfaceVariant)

            contacts.forEachIndexed { index, c ->
                SwipeActionsContactRow(
                    contact = c,
                    onEdit = { onEditClick(c) },
                    onDelete = { onDeleteClick(c) }
                )

                if (index != contacts.lastIndex) {
                    Divider(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeActionsContactRow(
    contact: Contact,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { true }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 68.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Edit
                FilledTonalIconButton(
                    onClick = {
                        onEdit()
                        scope.launch { dismissState.reset() }
                    }
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Edit")
                }

                Spacer(Modifier.width(10.dp))

                // Delete
                FilledTonalIconButton(
                    onClick = {
                        onDelete()
                        scope.launch { dismissState.reset() }
                    },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                }
            }
        },
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(contact)
                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = "${contact.firstName} ${contact.lastName}".trim(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = contact.phoneNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    )
}

@Composable
private fun Avatar(c: Contact) {
    val size = 44.dp
    val hasImage = !c.profileImageUrl.isNullOrBlank()

    if (hasImage) {
        AsyncImage(
            model = c.profileImageUrl,
            contentDescription = null,
            modifier = Modifier.size(size).clip(CircleShape)
        )
    } else {
        val initial = (c.firstName.firstOrNull() ?: c.lastName.firstOrNull() ?: '#').uppercase()
        Box(
            modifier = Modifier.size(size).clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {}
            Text(
                text = initial.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** ---------------- PROFILE SCREEN ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactProfileScreen(
    contact: Contact,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    BackHandler { onBack() }

    var menuExpanded by remember { mutableStateOf(false) }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Box(Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) { Text("Cancel") }

                // sağ üst 3 nokta
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "Menu")
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        },
                        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            // Avatar + Change Photo
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val hasImage = !contact.profileImageUrl.isNullOrBlank()
                if (hasImage) {
                    AsyncImage(
                        model = contact.profileImageUrl,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp).clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE5E5E5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Change Photo",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(18.dp))

            ReadOnlyField(value = contact.firstName)
            Spacer(Modifier.height(12.dp))
            ReadOnlyField(value = contact.lastName)
            Spacer(Modifier.height(12.dp))
            ReadOnlyField(value = contact.phoneNumber)

            Spacer(Modifier.height(26.dp))

            val ctx = androidx.compose.ui.platform.LocalContext.current
            val scope = rememberCoroutineScope()

            var isAlreadySaved by remember { mutableStateOf(false) }
            var showBanner by remember { mutableStateOf(false) }
            var bannerText by remember { mutableStateOf("") }
            // saved control
            LaunchedEffect(contact.phoneNumber) {
                val hasRead = ContextCompat.checkSelfPermission(
                    ctx, Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED

                isAlreadySaved = if (hasRead) {
                    withContext(Dispatchers.IO) { isContactSavedPhone(ctx, contact.phoneNumber) }
                } else {
                    false
                }
            }


            val permLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { perms ->
                val readOk = perms[Manifest.permission.READ_CONTACTS] == true
                val writeOk = perms[Manifest.permission.WRITE_CONTACTS] == true

                if (readOk && writeOk) {
                    scope.launch {
                        val ok = withContext(Dispatchers.IO) {
                            insertPhoneContact(
                                ctx = ctx,
                                firstName = contact.firstName,
                                lastName = contact.lastName,
                                phone = contact.phoneNumber
                            )
                        }

                        if (ok) {
                            isAlreadySaved = true
                            bannerText = "User is added to your phone!"
                            showBanner = true
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    permLauncher.launch(
                        arrayOf(
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.WRITE_CONTACTS
                        )
                    )
                },
                enabled = !isAlreadySaved,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(30.dp)
            ) {
                Text("Save to My Phone Contact", style = MaterialTheme.typography.titleMedium)
            }

            if (isAlreadySaved) {
                Spacer(Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "This contact is already saved your phone.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // green banner
            Box(Modifier.fillMaxSize()) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = showBanner,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 22.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        tonalElevation = 4.dp,
                        shadowElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF20B15A) // green code
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = bannerText,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF20B15A)
                            )
                        }
                    }
                }
            }

            LaunchedEffect(showBanner) {
                if (showBanner) {
                    delay(2000)
                    showBanner = false
                }
            }
        }
    }
}

@Composable
private fun ReadOnlyField(value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        enabled = false,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
}

/** ---------------- EDIT SCREEN ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditContactScreen(
    vm: ContactsViewModel,
    contact: Contact,
    onCancel: () -> Unit,
    onDone: () -> Unit
) {
    BackHandler { onCancel() }

    var firstName by remember { mutableStateOf(contact.firstName) }
    var lastName by remember { mutableStateOf(contact.lastName) }
    var phone by remember { mutableStateOf(contact.phoneNumber) }
    var photoModel by remember { mutableStateOf<Any?>(contact.profileImageUrl) }

    var showPhotoSheet by remember { mutableStateOf(false) }

    // photo choose flow
    // edit profil choose
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val pickPhoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) photoModel = uri
    }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) photoModel = tempCameraUri
        tempCameraUri = null
    }

    if (showPhotoSheet) {
        PhotoSourceSheet(
            onDismiss = { showPhotoSheet = false },
            onCamera = {
                showPhotoSheet = false
                val uri = createImageUri(ctx)
                tempCameraUri = uri
                takePicture.launch(uri)
            },
            onGallery = {
                showPhotoSheet = false
                pickPhoto.launch(
                    androidx.activity.result.PickVisualMediaRequest(
                        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
        )
    }

    val canSave = firstName.isNotBlank() && lastName.isNotBlank() && phone.isNotBlank()
    val scope = rememberCoroutineScope()

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
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
                    text = "Edit Contact",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.align(Alignment.Center)
                )

                TextButton(
                    enabled = canSave,
                    onClick = {
                        scope.launch {
                            vm.updateUser(
                                id = contact.id,
                                firstName = firstName.trim(),
                                lastName = lastName.trim(),
                                phoneNumber = phone.trim(),
                                profileImageUrl = photoModel?.toString()
                            )
                            onDone()
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) { Text("Done") }
            }

            Spacer(Modifier.height(18.dp))

            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                if (photoModel != null && photoModel.toString().isNotBlank()) {
                    AsyncImage(
                        model = photoModel,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp).clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE5E5E5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(56.dp))
                    }
                }

                Spacer(Modifier.height(10.dp))
                TextButton(onClick = { showPhotoSheet = true }) {
                    Text("Change Photo")
                }
            }

            Spacer(Modifier.height(18.dp))

            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("First Name") }
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("Last Name") }
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("Phone Number") }
            )
        }
    }
}


private fun isContactSavedPhone(ctx: android.content.Context, phone: String): Boolean {
    return try {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phone)
        )
        val projection = arrayOf(ContactsContract.PhoneLookup._ID)

        ctx.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            cursor.moveToFirst()
        } ?: false
    } catch (se: SecurityException) {
        // READ_CONTACTS permission not granted
        false
    } catch (e: Exception) {
        false
    }
}

private fun insertPhoneContact(
    ctx: android.content.Context,
    firstName: String,
    lastName: String,
    phone: String
): Boolean {
    return try {
        val ops = ArrayList<ContentProviderOperation>()

        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )

        // Name
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, lastName)
                .build()
        )

        // Phone
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                .withValue(
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                )
                .build()
        )

        ctx.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

@Composable
fun EmptyContacts(
    query: String,
    onCreateClick: () -> Unit
) {
    val title = if (query.isBlank()) "No Contacts" else "No Results"
    val subtitle = if (query.isBlank())
        "Contacts you’ve added will appear here."
    else
        "Try a different search."

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(18.dp))

            TextButton(onClick = onCreateClick) {
                Text(
                    "Create New Contact",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

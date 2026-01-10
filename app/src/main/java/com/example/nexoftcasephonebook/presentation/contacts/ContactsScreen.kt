package com.example.nexoftcasephonebook.presentation.contacts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nexoftcasephonebook.domain.model.Contact

@Composable
fun ContactsScreen(vm: ContactsViewModel) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) { vm.onEvent(ContactsEvent.Load) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        OutlinedTextField(
            value = state.query,
            onValueChange = { vm.onEvent(ContactsEvent.QueryChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search") }
        )

        Spacer(Modifier.height(12.dp))

        when {
            state.isLoading -> {
                CircularProgressIndicator()
            }
            state.error != null -> {
                Text("Error: ${state.error}")
            }
            else -> {
                LazyColumn(Modifier.fillMaxSize()) {
                    state.grouped.toSortedMap().forEach { (letter, list) ->
                        item {
                            Text(
                                text = letter.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(list) { c -> ContactRow(c) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactRow(c: Contact) {
    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(c.fullName, style = MaterialTheme.typography.titleMedium)
            Text(c.phoneNumber, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

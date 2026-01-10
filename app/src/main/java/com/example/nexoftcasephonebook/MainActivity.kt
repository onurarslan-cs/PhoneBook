package com.example.nexoftcasephonebook
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.nexoftcasephonebook.ui.theme.NexoftCasePhoneBookTheme
import androidx.compose.runtime.remember


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val retrofit = com.example.nexoftcasephonebook.core.network.RetrofitFactory
                .create(BuildConfig.NEXOFT_API_KEY)

            val api = retrofit.create(com.example.nexoftcasephonebook.data.remote.ContactsApi::class.java)
            val repo = com.example.nexoftcasephonebook.data.repository.ContactsRepositoryImpl(api)
            val vm = remember { com.example.nexoftcasephonebook.presentation.contacts.ContactsViewModel(repo) }

            com.example.nexoftcasephonebook.presentation.contacts.ContactsScreen(vm)
            NexoftCasePhoneBookTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NexoftCasePhoneBookTheme {
        Greeting("Android")
    }
}
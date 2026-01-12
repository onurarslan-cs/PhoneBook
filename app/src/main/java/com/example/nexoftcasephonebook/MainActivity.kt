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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val retrofit = com.example.nexoftcasephonebook.core.network.RetrofitFactory
                .create(BuildConfig.NEXOFT_API_KEY)

            val api = retrofit.create(com.example.nexoftcasephonebook.data.remote.ContactsApi::class.java)
            val repo = com.example.nexoftcasephonebook.data.repository.ContactsRepositoryImpl(api, appContext = applicationContext)
            val vm = remember { com.example.nexoftcasephonebook.presentation.contacts.ContactsViewModel(repo) }

            com.example.nexoftcasephonebook.presentation.contacts.ContactsScreen(vm)
            NexoftCasePhoneBookTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ){
                com.example.nexoftcasephonebook.presentation.contacts.ContactsScreen(vm)

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
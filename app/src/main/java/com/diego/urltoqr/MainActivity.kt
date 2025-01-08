package com.diego.urltoqr

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.diego.urltoqr.services.QRCodeManager
import com.diego.urltoqr.ui.theme.UrlToQRTheme
import com.diego.urltoqr.viewmodels.MainViewModel
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val integrator = IntentIntegrator(this)
        val qrCodeManager = QRCodeManager(integrator)
        val viewModel = MainViewModel(qrCodeManager)
        setContent {
            UrlToQRTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FormBox(viewModel)
                    UrlsList(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormBox(viewModel: MainViewModel) {
    var text by remember { mutableStateOf("Hello") }

    Row {
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Label") }
        )

        Button(onClick = { viewModel.generateQRCode(text) }) {
            Text("Qr it")
        }
    }
}

@Composable
fun UrlsList(viewModel: MainViewModel) {
    val state = viewModel.uiState.collectAsState()

    Column {
        state.value.list.forEach { url ->
            ListItem(url)
        }
    }
}

@Composable
fun ListItem(url: String) {
    Row {
        Text(url)
        Icon(
            Icons.Rounded.KeyboardArrowRight,
            contentDescription = "Send"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    UrlToQRTheme {
        FormBox(MainViewModel(QRCodeManager(IntentIntegrator(Activity()))))
    }
}
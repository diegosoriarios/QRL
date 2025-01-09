package com.diego.urltoqr

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.diego.urltoqr.services.QRCodeManager
import com.diego.urltoqr.ui.theme.UrlToQRTheme
import com.diego.urltoqr.viewmodels.MainViewModel
import com.google.zxing.integration.android.IntentIntegrator
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL


class MainActivity : ComponentActivity() {
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
                    Column {
                        FormBox(viewModel)
                        UrlsList(viewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormBox(viewModel: MainViewModel) {
    var text by remember { mutableStateOf("") }

    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp)) {
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("URL") }
        )

        Button(onClick = {
            viewModel.addToTheList(text)
            text = ""
        }) {
            Text("Qr it")
        }
    }
}

@Composable
fun UrlsList(viewModel: MainViewModel) {
    val state = viewModel.uiState.collectAsState()

    Column(Modifier.verticalScroll(rememberScrollState())) {
        state.value.list.forEach { url ->
            ListItem(url, viewModel)
        }
    }
}

@Composable
fun ListItem(url: String, viewModel: MainViewModel) {
    var expanded by remember { mutableStateOf (false) }
    val imageBitmap = try {
        val image = viewModel.generateQRCode(url) ?: throw Exception("Null image")
        image.asImageBitmap()
    } catch (e: Exception) {
        val urlImage = URL("https://hds.hel.fi/images/foundation/visual-assets/placeholders/image-m@3x.png")
        BitmapFactory.decodeStream(urlImage.openConnection().getInputStream()).asImageBitmap()
    }

    val activity = LocalContext.current

    fun share(bitmap: Bitmap) {
        val icon: Bitmap = bitmap
        val share = Intent(Intent.ACTION_SEND)
        share.type = "image/jpeg"
        val bytes = ByteArrayOutputStream()
        icon.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = Environment.DIRECTORY_DOWNLOADS
            .toString() + File.separator + "temporary_file.jpg"
        val f = File(
            path
        )
        try {
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse(path))
        activity.startActivity(Intent.createChooser(share, "Share Image"))
    }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .clickable(
            onClick = { expanded = !expanded }
        )) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(url)
            Icon(
                if (!expanded) Icons.Rounded.KeyboardArrowDown else Icons.Rounded.KeyboardArrowUp,
                contentDescription = "Send"
            )
        }
        if (expanded) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
                Image(bitmap = imageBitmap, contentDescription = "QR_CODE_" + url)
                Column {
                    Button(onClick = { share(imageBitmap.asAndroidBitmap()) }) {
                        Text("Share")
                    }
                    Button(onClick = {
                        viewModel.removeFromTheList(url)
                        expanded = false
                    }) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    UrlToQRTheme {
        FormBox(MainViewModel(QRCodeManager(IntentIntegrator(Activity()))))
    }
}
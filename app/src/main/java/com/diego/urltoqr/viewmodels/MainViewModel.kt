package com.diego.urltoqr.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.diego.urltoqr.services.QRCodeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MainState(
    val url: String = "",
    val list: List<String> = mutableListOf<String>(),
)
public class MainViewModel(private val qrCodeManager: QRCodeManager) : ViewModel() {
    private val _uiState = MutableStateFlow(MainState())
    val uiState: StateFlow<MainState> = _uiState.asStateFlow()

    fun generateQRCode(url: String): Bitmap? {
        return qrCodeManager.generateQRCode(url)
    }

    fun addToTheList(url: String) {
        if (url.isEmpty()) return
        if (uiState.value.list.contains(url)) return

        _uiState.update { currentState ->
            currentState.copy(
                list = currentState.list + url
            )
        }
    }

    fun removeFromTheList(url: String) {
        _uiState.update { currentState ->
            currentState.copy(
                list = currentState.list.minus(url)
            )
        }
    }
}

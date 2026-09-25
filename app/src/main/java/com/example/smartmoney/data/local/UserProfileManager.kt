package com.example.smartmoney.data.local

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Singleton managing user profile picture caching and persistence in app-internal storage.
 *
 * Implements main-safe coroutine suspend functions offloading disk I/O and bitmap decoding
 * onto [Dispatchers.IO] to ensure zero UI jank or frame drops during app launch and photo picking.
 *
 * Provides reactive [StateFlow] updates so all UI components (TitleBar, Settings, Hub)
 * update synchronously when a picture is uploaded or removed.
 */
object UserProfileManager {
    const val PROFILE_IMAGE_FILE_NAME = "user_profile_avatar.jpg"

    private val _profileBitmap = MutableStateFlow<ImageBitmap?>(null)
    val profileBitmap: StateFlow<ImageBitmap?> = _profileBitmap.asStateFlow()

    /**
     * Initializes the manager asynchronously by reading any previously saved profile picture from app storage.
     * Main-safe: safe to call from any thread or coroutine scope.
     */
    suspend fun initialize(
        context: Context,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ) = withContext(dispatcher) {
        val file = File(context.filesDir, PROFILE_IMAGE_FILE_NAME)
        if (file.exists() && file.length() > 0) {
            try {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                _profileBitmap.value = bitmap
            } catch (_: Exception) {
                _profileBitmap.value = null
            }
        } else {
            _profileBitmap.value = null
        }
    }

    /**
     * Copies the picked image from the given URI to internal app storage on [Dispatchers.IO]
     * and updates the reactive [profileBitmap] state.
     */
    suspend fun updateProfilePicture(
        context: Context,
        uri: Uri,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): Boolean = withContext(dispatcher) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val file = File(context.filesDir, PROFILE_IMAGE_FILE_NAME)
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                _profileBitmap.value = bitmap
                true
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Deletes the custom profile picture on [Dispatchers.IO] and resets the state to fallback initials.
     */
    suspend fun removeProfilePicture(
        context: Context,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): Boolean = withContext(dispatcher) {
        try {
            val file = File(context.filesDir, PROFILE_IMAGE_FILE_NAME)
            if (file.exists()) {
                file.delete()
            }
            _profileBitmap.value = null
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Resets the in-memory bitmap state, primarily for testing purposes.
     */
    fun resetForTesting() {
        _profileBitmap.value = null
    }
}

package com.example.smartmoney.data.local

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * Singleton managing user profile picture caching and persistence in app-internal storage.
 * Provides reactive StateFlow updates so all UI components (TitleBar, Settings, Hub)
 * update synchronously when a picture is uploaded or removed.
 */
object UserProfileManager {
    private const val PROFILE_IMAGE_FILE_NAME = "user_profile_avatar.jpg"

    private val _profileBitmap = MutableStateFlow<ImageBitmap?>(null)
    val profileBitmap: StateFlow<ImageBitmap?> = _profileBitmap.asStateFlow()

    /**
     * Initializes the manager by reading any previously saved profile picture from app storage.
     */
    fun initialize(context: Context) {
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
     * Copies the picked image from the given URI to internal app storage and updates the state.
     */
    fun updateProfilePicture(context: Context, uri: Uri): Boolean {
        return try {
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
     * Deletes the custom profile picture and resets the state to fallback initials.
     */
    fun removeProfilePicture(context: Context): Boolean {
        return try {
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
}

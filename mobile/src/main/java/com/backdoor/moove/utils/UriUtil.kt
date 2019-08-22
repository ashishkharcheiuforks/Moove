package com.backdoor.moove.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.backdoor.moove.BuildConfig
import timber.log.Timber
import java.io.File

object UriUtil {

    fun getUri(context: Context, filePath: String): Uri? {
        Timber.d("getUri: %s", BuildConfig.APPLICATION_ID)
        return try {
            if (Module.isNougat) {
                FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", File(filePath))
            } else {
                Uri.fromFile(File(filePath))
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getUri(context: Context, file: File): Uri? {
        Timber.d("getUri: %s", BuildConfig.APPLICATION_ID)
        return try {
            if (Module.isNougat) {
                FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
            } else {
                Uri.fromFile(file)
            }
        } catch (e: Exception) {
            null
        }
    }
}

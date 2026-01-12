package com.example.nexoftcasephonebook.core.network

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

fun uriToImagePart(
    context: Context,
    uri: Uri,
    partName: String = "file" // gerekirse Swagger'a göre değiştir
): MultipartBody.Part {
    val cr = context.contentResolver
    val mime = cr.getType(uri) ?: "image/jpeg"

    // Dosya adını yakala (yoksa fallback)
    val fileName = cr.query(uri, null, null, null, null)?.use { c ->
        val nameIdx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (c.moveToFirst() && nameIdx >= 0) c.getString(nameIdx) else null
    } ?: "upload_${System.currentTimeMillis()}.jpg"

    // Stream'i cache'e kopyala
    val tempFile = File(context.cacheDir, fileName)
    cr.openInputStream(uri).use { input ->
        requireNotNull(input) { "Cannot open input stream for uri=$uri" }
        tempFile.outputStream().use { output -> input.copyTo(output) }
    }

    val body = tempFile.asRequestBody(mime.toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(partName, tempFile.name, body)
}

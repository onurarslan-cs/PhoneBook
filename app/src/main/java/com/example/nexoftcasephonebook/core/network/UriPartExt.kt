package com.example.nexoftcasephonebook.core.network

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import android.graphics.ImageDecoder
import android.os.Build


fun uriToJpegPart(
    context: Context,
    uri: Uri,
    partName: String = "file",
    quality: Int = 90
): MultipartBody.Part {
    val resolver = context.contentResolver

    val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= 28) {
        val source = ImageDecoder.createSource(resolver, uri)
        ImageDecoder.decodeBitmap(source)
    } else {
        resolver.openInputStream(uri).use { input ->
            BitmapFactory.decodeStream(input)
        } ?: throw IllegalStateException("Cannot decode image from uri: $uri")
    }

    val baos = ByteArrayOutputStream()
    val ok = bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
    if (!ok) throw IllegalStateException("JPEG compress failed for uri: $uri")

    val bytes = baos.toByteArray()
    val body = bytes.toRequestBody("image/jpeg".toMediaType())

    return MultipartBody.Part.createFormData(
        name = partName,
        filename = "photo_${System.currentTimeMillis()}.jpg",
        body = body
    )
}

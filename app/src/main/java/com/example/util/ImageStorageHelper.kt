package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageStorageHelper {

    private const val DIRECTORY_NAME = "product_images"

    private fun getImagesDir(context: Context): File {
        val dir = File(context.filesDir, DIRECTORY_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Menyimpan gambar dari Uri (misal dari PhotoPicker) ke internal storage aplikasi
     * sehingga tetap dapat diakses setelah aplikasi ditutup/dibuka kembali.
     */
    fun saveImageFromUri(context: Context, sourceUri: Uri): String? {
        return try {
            val dir = getImagesDir(context)
            val newFile = File(dir, "img_${System.currentTimeMillis()}.jpg")
            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            if (inputStream != null) {
                FileOutputStream(newFile).use { output ->
                    inputStream.copyTo(output)
                }
                inputStream.close()
                newFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Menyimpan Bitmap (misal dari Kamera) ke internal storage aplikasi.
     */
    fun saveBitmap(context: Context, bitmap: Bitmap): String? {
        return try {
            val dir = getImagesDir(context)
            val newFile = File(dir, "img_${System.currentTimeMillis()}.jpg")
            FileOutputStream(newFile).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
            }
            newFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Menghapus file gambar jika sudah tidak digunakan
     */
    fun deleteImageFile(filePath: String?) {
        if (filePath.isNullOrBlank()) return
        try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

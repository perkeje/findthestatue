package com.example.findthestatue.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

class ImageConverter(val uri: String, val mode: String, val context: Context) {

    fun getBitmap(): Bitmap {
        var bitmap: Bitmap
        if (mode == "camera") {
            bitmap = BitmapFactory.decodeFile(uri)
            val matrix = Matrix()
            matrix.postRotate(90F)
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap = getCapturedImage(Uri.parse(uri))
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        }

        return bitmap
    }


    private fun getCapturedImage(selectedPhotoUri: Uri): Bitmap {
        val bitmap = when {
            Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(
                context.contentResolver,
                selectedPhotoUri
            )
            else -> {
                val source = ImageDecoder.createSource(context.contentResolver, selectedPhotoUri)
                ImageDecoder.decodeBitmap(source)
            }
        }
        return bitmap
    }

    companion object {
        fun cropBitmap(bitmap: Bitmap): Bitmap {
            var dstBmp: Bitmap
            if (bitmap.width >= bitmap.height) {

                dstBmp = Bitmap.createBitmap(
                    bitmap,
                    bitmap.width / 2 - bitmap.height / 2,
                    0,
                    bitmap.height,
                    bitmap.height
                )

            } else {

                dstBmp = Bitmap.createBitmap(
                    bitmap,
                    0,
                    bitmap.height / 2 - bitmap.width / 2,
                    bitmap.width,
                    bitmap.width
                )
            }
            return dstBmp
        }

    }

}
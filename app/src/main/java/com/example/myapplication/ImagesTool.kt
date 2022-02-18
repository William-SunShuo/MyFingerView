package com.example.myapplication
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.widget.ImageView
import kotlin.math.max
import kotlin.math.roundToInt

object ImagesTool {
    /**
     * 根据ImageView的大小压缩图片
     */
    fun decodeSampledBitmapFromPath(context: Context?, uri: Uri?, imageView: ImageView?): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        val path = getImageAbsolutePath(context, uri)
        BitmapFactory.decodeFile(path, options)
        val imageSize = getImageViewSize(imageView) //获取图片大小，ImageSize是封装着ImageView大小的类
        options.inSampleSize = caculateInSampleSize(options, imageSize.width, imageSize.height) //计算采样率
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(path, options)
    }

    private fun caculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val width = options.outWidth //原始图片宽
        val height = options.outHeight //原始图片高
        var inSampleSize = 1 //采样率
        if (width > reqWidth || height > reqHeight) {
            //原始的宽比目标宽大，或者原始高比目标高大
            val widthRadio = (width * 1.0f / reqWidth).roundToInt()
            val heightRadio = (height * 1.0f / reqHeight).roundToInt()
            inSampleSize = max(widthRadio, heightRadio)
        }
        return inSampleSize
    }

    //获取ImageView的大小
    private fun getImageViewSize(imageView: ImageView?): ImageSize {
        val imageSize = ImageSize()
        val metrics = imageView!!.context.resources.displayMetrics
        val lp = imageView.layoutParams
        var width = imageView.width
        if (width <= 0) {
            width = lp.width
        }
        if (width <= 0) {
            width = imageView.maxWidth
        }
        if (width <= 0) {
            width = metrics.widthPixels
        }
        var height = imageView.height
        if (height <= 0) {
            height = lp.height
        }
        if (height <= 0) {
            height = imageView.maxHeight
        }
        if (height <= 0) {
            height = metrics.heightPixels
        }
        imageSize.width = width
        imageSize.height = height
        return imageSize
    }

    private fun getImageAbsolutePath(context: Context?, imageUri: Uri?): String? {
        if (context == null || imageUri == null) return null
        if (DocumentsContract.isDocumentUri(context, imageUri)) {
            if (isExternalStorageDocument(imageUri)) {
                val docId = DocumentsContract.getDocumentId(imageUri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(imageUri)) {
                val id = DocumentsContract.getDocumentId(imageUri)
                val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(imageUri)) {
                val docId = DocumentsContract.getDocumentId(imageUri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = MediaStore.Images.Media._ID + "=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } // MediaStore (and general)
        else if ("content".equals(imageUri.scheme, ignoreCase = true)) {
            // Return the remote address
            return if (isGooglePhotosUri(imageUri)) imageUri.lastPathSegment else getDataColumn(context, imageUri, null, null)
        } else if ("file".equals(imageUri.scheme, ignoreCase = true)) {
            return imageUri.path
        }
        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    //ImageView大小的封装类
    private class ImageSize {
        var width = 0
        var height = 0
    }
}
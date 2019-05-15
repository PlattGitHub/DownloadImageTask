package com.example.downloadimagetask

import android.app.DownloadManager
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File

/**
 * 10 megabytes size image
 *
 * @author Alexander Gorin
 */
const val imageLink = "http://luxfon.com/images/201203/luxfon.com_3795.jpg"

/**
 * Authority for [FileProvider].
 *
 * @author Alexander Gorin
 */
private const val FILE_PROVIDER_AUTHORITY = "com.example.downloadimagetask.provider"

/**
 * Function that gets extension type of file from url if it's possible.
 * If it's not, default type of image(in case of app) will be jpg.
 *
 * @author Alexander Gorin
 */
fun String.getExtension(): String {
    val extensionType = MimeTypeMap.getFileExtensionFromUrl(this)
    return if (extensionType == "") "jpg" else extensionType
}

/**
 * Used to get URI of downloaded file.
 * It uses [DownloadManager.Query] to find downloaded item
 * and then using [FileProvider] gets URI of file.
 * [FileProvider] is registered in Manifest file.
 *
 * @author Alexander Gorin
 */
fun getFileUri(
    context: Context,
    downloadManager: DownloadManager,
    lastDownloadID: Long
): Uri {
    val query = DownloadManager.Query().setFilterById(lastDownloadID)
    val cursor = downloadManager.query(query)
    if (cursor.moveToFirst()) {
        val downloadStatus =
            cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
        val downloadLocalUri =
            cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
        if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL && downloadLocalUri != null) {
            val attachmentUri = Uri.parse(downloadLocalUri)
            if (attachmentUri != null) {
                if (ContentResolver.SCHEME_FILE == attachmentUri.scheme) {
                    val file = File(attachmentUri.path)
                    val attachmentUriFile =
                        FileProvider.getUriForFile(
                            context,
                            FILE_PROVIDER_AUTHORITY, file
                        )
                    cursor.close()
                    return attachmentUriFile
                }
            }
        }
    }
    cursor.close()
    return Uri.EMPTY
}

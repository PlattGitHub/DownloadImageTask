package com.example.downloadimagetask

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Simple [Fragment] subclass that has [Button] to download an image
 * and then displays it in [ImageView]. It also implements [CoroutineScope] for
 * correct work of coroutines in [Fragment].
 *
 * @author Alexander Gorin
 */
class DownloadImageFragment : Fragment(), CoroutineScope {

    private lateinit var button: Button
    private lateinit var imageView: ImageView

    private lateinit var downloadManager: DownloadManager

    private var lastDownloadID = -1L
    private var attachmentUriFile: Uri = Uri.EMPTY

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_download_image, container, false).apply {
            button = findViewById(R.id.button)
            imageView = findViewById(R.id.image_view)
        }
        job = Job()

        downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        postImage(attachmentUriFile)

        button.setOnClickListener {
            startDownload(imageLink)
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        context?.registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onPause() {
        super.onPause()
        context?.unregisterReceiver(receiver)
        job.cancel()
    }

    /**
     * Method that sets up [DownloadManager] for downloading image from URL.
     * The file will be saved in Pictures folder in public external storage.
     * [DownloadManager] will work in both Wifi and Mobile network.
     * If user turns Wifi/Mobile network off and then turns it on, [DownloadManager] will automatically
     * resume the download.
     *
     * @author Alexander Gorin
     */
    private fun startDownload(url: String) {
        val uri = Uri.parse(url)
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).mkdirs()
        val request = DownloadManager.Request(uri)
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setAllowedOverRoaming(false)
            .setTitle(getString(R.string.downloading_image))
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_PICTURES,
                String.format(
                    getString(R.string.image_file_name),
                    lastDownloadID.toString(),
                    url.getExtension()
                )
            )
        lastDownloadID = downloadManager.enqueue(request)
    }

    /**
     * Method that displays image in [ImageView].
     * As image is really big, it will take some time to load it.
     *
     * @author Alexander Gorin
     */
    private fun postImage(uri: Uri) {
        if (uri != Uri.EMPTY) {
            imageView.setImageURI(uri)
        }
    }

    /**
     * Broadcast receiver that triggers when action [DownloadManager.ACTION_DOWNLOAD_COMPLETE] occurs.
     * Registered/Unregistered in onResume/onPause.
     *
     * Also it triggers when user presses Cancel button in Notification.
     * It seems like a bug. But then [getFileUri] will return [Uri.EMPTY] and no image will be displayed.
     *
     * As [getFileUri] is rather hard work for UI, it works on [Dispatchers.IO] and then image is being
     * displayed in [Dispatchers.Main].
     *
     * @author Alexander Gorin
     **/
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context?.let {
                launch {
                    val uriResult = withContext(Dispatchers.IO) {
                        getFileUri(it, downloadManager, lastDownloadID)
                    }
                    attachmentUriFile = uriResult
                    postImage(attachmentUriFile)
                }
            }
        }
    }

    companion object {
        fun newInstance() = DownloadImageFragment()
    }
}

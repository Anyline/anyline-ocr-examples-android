package io.anyline.examples.scanviewresult

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import io.anyline.examples.R
import io.anyline.examples.util.BitmapUtil
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ShareDataIntentCreator {

    @Throws(IOException::class)
    fun createIntent(result: Map<String, String>, pictures: Map<String, String>, context: Context): Intent {
        val sendIntent = Intent()
        addSubject(context, sendIntent)
        sendIntent.action = Intent.ACTION_SEND_MULTIPLE
        sendIntent.type = "image/jpeg"
        addText(result, sendIntent)
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val uris = mutableListOf<Uri>()

        for ((key, value) in pictures) {
            uris.add(getUriForFile(value, key, context))
        }

        sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
        return Intent.createChooser(sendIntent, null)
    }

    private fun addSubject(context: Context, sendIntent: Intent) {
        val subject = context.getString(R.string.native_share_subject)
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
    }

    private fun addText(result: Map<String, String>, sendIntent: Intent) {
        var resultText = ""

        for ((key, value) in result.entries) {
            resultText += "\n$key: $value"
        }
        sendIntent.putExtra(Intent.EXTRA_TEXT, resultText)
    }

    private fun getUriForFile(filePath: String, fileName: String, context: Context): Uri {
        val destination = File(context.filesDir, "images")
        val destinationFile = File(destination, fileName)

        if (destination.exists().not()) {
            destination.mkdir()
        }

        if (destinationFile.exists().not()) {
            destinationFile.createNewFile()
        }

        val outputStream = FileOutputStream(destinationFile)
        BitmapUtil.getBitmap(filePath).compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
        outputStream.flush()
        outputStream.close()
        return FileProvider.getUriForFile(context, "io.anyline.fileprovider", destinationFile)
    }
}
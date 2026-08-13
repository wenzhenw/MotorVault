package week11.st099681.finalproject.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max
import kotlin.math.min

object ImageUtils {

    /** Loads a bitmap from a content Uri (for previews). */
    fun loadBitmap(context: Context, uri: Uri): Bitmap? = try {
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
    } catch (_: Exception) {
        null
    }

    /**
     * Compresses an image to a small JPEG and encodes it as Base64 so it can be
     * stored inside a Firestore document (keeps the project on the free tier —
     * no Cloud Storage bucket required). Stays well under the 1 MB doc limit.
     */
    fun uriToBase64(context: Context, uri: Uri, maxDim: Int = 720, quality: Int = 55): String? {
        val bmp = loadBitmap(context, uri) ?: return null
        val scale = min(1f, maxDim.toFloat() / max(bmp.width, bmp.height))
        val scaled = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                bmp,
                (bmp.width * scale).toInt().coerceAtLeast(1),
                (bmp.height * scale).toInt().coerceAtLeast(1),
                true
            )
        } else bmp
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
        return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT)
    }

    fun base64ToBitmap(b64: String?): Bitmap? = try {
        if (b64.isNullOrBlank()) null
        else {
            val bytes = Base64.decode(b64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    } catch (_: Exception) {
        null
    }

    /** Creates a FileProvider Uri in cache for the camera "Take Photo" flow. */
    fun newCameraUri(context: Context): Uri {
        val dir = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File(dir, "receipt_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}

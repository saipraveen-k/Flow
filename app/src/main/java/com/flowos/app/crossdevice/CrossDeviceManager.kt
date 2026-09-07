package com.flowos.app.crossdevice

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.flowos.app.data.local.TaskEntity
import java.io.File

/** Result of a cross-device operation. */
sealed interface CrossDeviceResult {
    data class Sent(val via: String) : CrossDeviceResult
    data class Failed(val reason: String) : CrossDeviceResult
}

/**
 * Phone-to-laptop workflow. The Office Kit path is a clearly marked slot: when
 * the hackathon environment provides the APIs, implement [OfficeKitBridge]
 * and wire it in [setBridge]. Until then everything flows through Android's
 * share sheet and FileProvider, which is fully functional today.
 */
class CrossDeviceManager(private val context: Context) {

    interface OfficeKitBridge {
        fun sendFile(path: String): CrossDeviceResult
        fun shareText(text: String): CrossDeviceResult
        fun syncTask(task: TaskEntity): CrossDeviceResult
    }

    private var bridge: OfficeKitBridge? = null

    fun setBridge(officeKitBridge: OfficeKitBridge?) {
        bridge = officeKitBridge
    }

    fun sendFile(path: String): CrossDeviceResult {
        bridge?.let { return it.sendFile(path) }
        val file = File(path)
        if (!file.exists()) return CrossDeviceResult.Failed("File not found: ${file.name}")
        return try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
            val intent = Intent(Intent.ACTION_SEND)
                .setType(guessMimeType(file.name))
                .putExtra(Intent.EXTRA_STREAM, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(
                Intent.createChooser(intent, "Send to laptop").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            CrossDeviceResult.Sent(via = "Android share sheet")
        } catch (_: Exception) {
            CrossDeviceResult.Failed("Couldn't share ${file.name}")
        }
    }

    fun receiveFile(): CrossDeviceResult =
        CrossDeviceResult.Failed(
            "Receiving needs the Office Kit bridge; incoming shares are handled via the system share UI",
        )

    fun shareText(text: String): CrossDeviceResult {
        bridge?.let { return it.shareText(text) }
        return try {
            val intent = Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, text)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(
                Intent.createChooser(intent, "Continue on laptop").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
            CrossDeviceResult.Sent(via = "Android share sheet")
        } catch (_: Exception) {
            CrossDeviceResult.Failed("Couldn't open the share sheet")
        }
    }

    fun syncTask(task: TaskEntity): CrossDeviceResult {
        bridge?.let { return it.syncTask(task) }
        return shareText(
            buildString {
                append("[FlowOS] ${task.title}")
                task.deadlineLabel?.let { append(" · Due $it") }
                task.personName?.let { append(" · With $it") }
            },
        )
    }

    private fun guessMimeType(name: String): String = when {
        name.endsWith(".pdf", true) -> "application/pdf"
        name.endsWith(".txt", true) -> "text/plain"
        name.endsWith(".png", true) -> "image/png"
        name.endsWith(".jpg", true) || name.endsWith(".jpeg", true) -> "image/jpeg"
        else -> "*/*"
    }
}

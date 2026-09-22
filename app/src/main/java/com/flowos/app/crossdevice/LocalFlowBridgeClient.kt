package com.flowos.app.crossdevice

import android.content.ContentResolver
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Socket

/** Authenticated, local-network-only Flow Bridge protocol. Not an OEM Office Kit API. */
class LocalFlowBridgeClient {
    suspend fun connect(host: String, token: String, port: Int = PORT): CrossDeviceResult =
        transfer(host, token, "", 0, null, port)

    suspend fun sendFile(
        resolver: ContentResolver,
        uri: Uri,
        displayName: String,
        size: Long,
        host: String,
        token: String,
        port: Int = PORT,
    ): CrossDeviceResult = transfer(host, token, displayName, size, { resolver.openInputStream(uri) }, port)

    private suspend fun transfer(host: String, token: String, name: String, size: Long, input: (() -> java.io.InputStream?)?, port: Int): CrossDeviceResult = withContext(Dispatchers.IO) {
        if (host.isBlank() || token.length < 8) return@withContext CrossDeviceResult.Failed("Enter the PC address and an 8+ character pairing token.")
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host.trim(), port), TIMEOUT_MS)
                socket.soTimeout = TIMEOUT_MS
                DataOutputStream(socket.getOutputStream()).use { output ->
                    output.write("FLOWOS1\u0000".toByteArray(Charsets.US_ASCII))
                    output.writeInt(token.toByteArray(Charsets.UTF_8).size); output.write(token.toByteArray(Charsets.UTF_8))
                    output.writeInt(name.toByteArray(Charsets.UTF_8).size); output.write(name.toByteArray(Charsets.UTF_8)); output.writeLong(size)
                    input?.invoke()?.use { it.copyTo(output) }; output.flush()
                    val response = DataInputStream(socket.getInputStream())
                    val ok = response.readUnsignedByte() == 1
                    val messageSize = response.readInt()
                    if (messageSize !in 0..4096) return@withContext CrossDeviceResult.Failed("Invalid response from PC.")
                    val bytes = ByteArray(messageSize); response.readFully(bytes)
                    val message = bytes.toString(Charsets.UTF_8)
                    if (ok) CrossDeviceResult.Sent("Local PC Connection: $message") else CrossDeviceResult.Failed(message)
                }
            }
        } catch (e: Exception) { CrossDeviceResult.Failed("PC connection failed: ${e.message ?: "unavailable"}") }
    }

    private companion object { const val PORT = 43910; const val TIMEOUT_MS = 8_000 }
}

package com.flowos.app.capture

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** States surfaced by the voice capture UI. */
sealed interface VoiceState {
    data object Idle : VoiceState
    data object Listening : VoiceState
    data object Processing : VoiceState
    data class Result(val text: String) : VoiceState
    data class Error(val message: String) : VoiceState
}

/**
 * Speech-to-text abstraction. The device recognizer (on-device where the
 * phone supports it) is the real provider; the typed-fallback keeps the app
 * runnable on emulators or phones without a speech service.
 */
interface SpeechToTextEngine {

    /** False when the device has no speech service (UI then offers typing). */
    fun isAvailable(): Boolean

    fun listen(): Flow<VoiceState>

    fun stop()
}

/** Real recognizer backed by Android's SpeechRecognizer (Google service). */
class DeviceSpeechToTextEngine(
    private val context: Context,
) : SpeechToTextEngine {

    private var recognizer: SpeechRecognizer? = null

    override fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    override fun listen(): Flow<VoiceState> = callbackFlow {
        if (!isAvailable()) {
            trySend(VoiceState.Error("Speech recognition isn't available on this phone. Type it instead."))
            awaitClose { }
            return@callbackFlow
        }

        trySend(VoiceState.Listening)

        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        recognizer = speechRecognizer

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                trySend(VoiceState.Processing)
            }

            override fun onError(error: Int) {
                val message = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                        "FlowOS didn't catch that. Try again a bit closer."

                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                        "Microphone permission is needed for voice capture."

                    else -> "Voice recognition hit a snag. You can type it instead."
                }
                trySend(VoiceState.Error(message))
                close()
            }

            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                if (text.isBlank()) {
                    trySend(VoiceState.Error("FlowOS didn't catch that. Try again."))
                } else {
                    trySend(VoiceState.Result(text))
                }
                close()
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (!partial.isNullOrBlank()) {
                    trySend(VoiceState.Processing)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(intent)

        awaitClose {
            speechRecognizer.destroy()
            recognizer = null
        }
    }

    override fun stop() {
        recognizer?.stopListening()
    }
}

/**
 * Fallback used when no speech service exists (emulators, some devices).
 * VoiceState.Error carries a message so the UI can offer typing immediately.
 */
class UnavailableSpeechToTextEngine : SpeechToTextEngine {

    override fun isAvailable(): Boolean = false

    override fun listen(): Flow<VoiceState> = callbackFlow {
        trySend(
            VoiceState.Error("Speech recognition isn't available on this phone. Type it instead."),
        )
        awaitClose { }
        return@callbackFlow
    }

    override fun stop() {}
}

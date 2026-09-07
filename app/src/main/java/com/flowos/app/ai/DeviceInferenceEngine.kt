package com.flowos.app.ai

import com.flowos.app.domain.model.AIAnalysisResult
import com.flowos.app.domain.model.CaptureDraft

/**
 * Abstraction over on-device model runtimes. A Qualcomm/Snapdragon-compatible
 * runtime (or any future on-device LLM) implements this; nothing above this
 * layer changes.
 */
interface DeviceInferenceEngine {

    /** Metadata about the loaded runtime, for the debug/developer screen. */
    data class Capabilities(
        val runtime: String,
        val accelerator: String, // CPU | GPU | NPU | NONE
        val modelName: String,
        val isAvailable: Boolean,
    )

    fun initialize()

    fun isAvailable(): Boolean

    fun getDeviceCapabilities(): Capabilities

    suspend fun runTextInference(draft: CaptureDraft): AIAnalysisResult?

    suspend fun runVisionInference(imageBytes: ByteArray): String?
}

/**
 * Honest default: no model is bundled yet, so nothing is claimed. The UI only
 * shows "on device" when the active engine genuinely processes locally.
 */
class MockDeviceInferenceEngine : DeviceInferenceEngine {

    private var initialized = false

    override fun initialize() {
        initialized = true
    }

    override fun isAvailable(): Boolean = false

    override fun getDeviceCapabilities(): DeviceInferenceEngine.Capabilities =
        DeviceInferenceEngine.Capabilities(
            runtime = "none (model not bundled)",
            accelerator = "NONE",
            modelName = "-",
            isAvailable = false,
        )

    override suspend fun runTextInference(draft: CaptureDraft): AIAnalysisResult? = null

    override suspend fun runVisionInference(imageBytes: ByteArray): String? = null
}

/**
 * Placeholder for the future on-device runtime. Wire a real Snapdragon/NNAPI/
 * LiteRT engine here; until then it correctly reports unavailable.
 */
class LocalDeviceInferenceEngine : DeviceInferenceEngine {

    private var initialized = false

    override fun initialize() {
        initialized = true
    }

    override fun isAvailable(): Boolean = false

    override fun getDeviceCapabilities(): DeviceInferenceEngine.Capabilities =
        DeviceInferenceEngine.Capabilities(
            runtime = "reserved (no runtime wired yet)",
            accelerator = "CPU",
            modelName = "-",
            isAvailable = false,
        )

    override suspend fun runTextInference(draft: CaptureDraft): AIAnalysisResult? = null

    override suspend fun runVisionInference(imageBytes: ByteArray): String? = null
}

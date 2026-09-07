package com.flowos.app.di

import android.content.Context
import com.flowos.app.action.ActionEngine
import com.flowos.app.ai.AIEngine
import com.flowos.app.ai.LocalAIEngine
import com.flowos.app.ai.MockAIEngine
import com.flowos.app.calendar.CalendarIntelligenceEngine
import com.flowos.app.calendar.CalendarRepository
import com.flowos.app.capture.DocumentExtractor
import com.flowos.app.capture.OCRProcessor
import com.flowos.app.capture.SpeechToTextEngine
import com.flowos.app.capture.UnavailableSpeechToTextEngine
import com.flowos.app.context.ContextEngine
import com.flowos.app.crossdevice.CrossDeviceManager
import com.flowos.app.data.demo.DemoDataSeeder
import com.flowos.app.data.local.FlowOSDatabase
import com.flowos.app.data.repository.FlowOSRepository
import com.flowos.app.notifications.FlowOSNotifier
import com.flowos.app.pulse.NextBestActionEngine
import com.flowos.app.pulse.WorkStateEngine
import com.flowos.app.settings.SettingsStore

/**
 * Minimal hand-rolled container. Everything is created once and shared;
 * swapping the AI engine or speech provider happens here only.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val database: FlowOSDatabase by lazy { FlowOSDatabase.get(appContext) }
    val repository: FlowOSRepository by lazy { FlowOSRepository(database) }
    val settingsStore: SettingsStore by lazy { SettingsStore(appContext) }
    val demoDataSeeder: DemoDataSeeder by lazy { DemoDataSeeder(repository, settingsStore) }

    val mockAIEngine: AIEngine by lazy { MockAIEngine() }
    val localAIEngine: AIEngine by lazy { LocalAIEngine() }

    val contextEngine: ContextEngine by lazy { ContextEngine(repository) }

    // FlowPulse: work-state intelligence (deterministic, local-only).
    val nextBestActionEngine: NextBestActionEngine by lazy { NextBestActionEngine() }
    val workStateEngine: WorkStateEngine by lazy { WorkStateEngine(nextBestActionEngine) }
    val calendarRepository: CalendarRepository by lazy { CalendarRepository(appContext) }
    val calendarIntelligenceEngine: CalendarIntelligenceEngine by lazy { CalendarIntelligenceEngine() }

    val notifier: FlowOSNotifier by lazy {
        FlowOSNotifier(appContext).also { it.ensureChannel() }
    }
    val actionEngine: ActionEngine by lazy { ActionEngine(appContext, notifier) }
    val crossDeviceManager: CrossDeviceManager by lazy { CrossDeviceManager(appContext) }

    val ocrProcessor: OCRProcessor by lazy { OCRProcessor(appContext) }
    val documentExtractor: DocumentExtractor by lazy { DocumentExtractor(appContext, ocrProcessor) }

    /** Speech provider chosen at runtime; UI falls back to typing when unavailable. */
    fun speechEngine(): SpeechToTextEngine {
        val deviceEngine = com.flowos.app.capture.DeviceSpeechToTextEngine(appContext)
        return if (deviceEngine.isAvailable()) deviceEngine else UnavailableSpeechToTextEngine()
    }
}

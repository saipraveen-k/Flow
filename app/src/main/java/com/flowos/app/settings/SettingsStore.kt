package com.flowos.app.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "flowos_settings")

/** Which AI engine handles captures. DEMO is the honest default. */
enum class AiMode { DEMO, LOCAL }

/**
 * Small persistent settings surface. No accounts, no sync — everything stays
 * on the device by design.
 */
class SettingsStore(private val context: Context) {

    private val aiModeKey = stringPreferencesKey("ai_mode")
    private val demoSeededKey = booleanPreferencesKey("demo_seeded")
    private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")

    val aiMode: Flow<AiMode> = context.dataStore.data.map { prefs ->
        when (prefs[aiModeKey]) {
            AiMode.LOCAL.name -> AiMode.LOCAL
            else -> AiMode.DEMO
        }
    }

    suspend fun setAiMode(mode: AiMode) {
        context.dataStore.edit { it[aiModeKey] = mode.name }
    }

    val demoSeeded: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[demoSeededKey] ?: false
    }

    suspend fun markDemoSeeded() {
        context.dataStore.edit { it[demoSeededKey] = true }
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[onboardingCompletedKey] ?: false
    }

    suspend fun markOnboardingCompleted() {
        context.dataStore.edit { it[onboardingCompletedKey] = true }
    }

    suspend fun clearAll() {
        context.dataStore.edit { prefs ->
            prefs.remove(aiModeKey)
            prefs.remove(demoSeededKey)
        }
    }
}

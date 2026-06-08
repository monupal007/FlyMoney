package com.maka.flymoney.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    private val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
    private val LAST_BET_AMOUNT = doublePreferencesKey("last_bet_amount")

    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { it[SOUND_ENABLED] ?: true }
    val hapticsEnabled: Flow<Boolean> = context.dataStore.data.map { it[HAPTICS_ENABLED] ?: true }
    val lastBetAmount: Flow<Double> = context.dataStore.data.map { it[LAST_BET_AMOUNT] ?: 100.0 }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SOUND_ENABLED] = enabled }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[HAPTICS_ENABLED] = enabled }
    }

    suspend fun setLastBetAmount(amount: Double) {
        context.dataStore.edit { it[LAST_BET_AMOUNT] = amount }
    }
}

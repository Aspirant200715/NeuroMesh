package com.neuromesh.crisis.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideDeviceId(@ApplicationContext context: Context): String {
        val prefs = context.getSharedPreferences("neuromesh_device", Context.MODE_PRIVATE)
        return prefs.getString("device_id", null) ?: run {
            val id = UUID.randomUUID().toString().replace("-", "").take(16).uppercase()
            prefs.edit().putString("device_id", id).apply()
            id
        }
    }

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context
}

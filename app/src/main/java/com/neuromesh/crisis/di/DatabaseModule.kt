package com.neuromesh.crisis.di

import android.content.Context
import androidx.room.Room
import com.neuromesh.crisis.data.local.NeuroMeshDatabase
import com.neuromesh.crisis.data.local.dao.AlertDao
import com.neuromesh.crisis.data.local.dao.ObservationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NeuroMeshDatabase =
        Room.databaseBuilder(
            context,
            NeuroMeshDatabase::class.java,
            "neuromesh.db"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideObservationDao(db: NeuroMeshDatabase): ObservationDao = db.observationDao()

    @Provides
    fun provideAlertDao(db: NeuroMeshDatabase): AlertDao = db.alertDao()
}

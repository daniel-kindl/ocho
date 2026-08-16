package dev.danielkindl.ocho.di

import dev.danielkindl.ocho.DistributionStartup
import dev.danielkindl.ocho.data.update.PlayUpdateCoordinator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt bindings for the Play Store update distribution. */
@Module
@InstallIn(SingletonComponent::class)
object PlayDistributionModule {
    @Provides
    @Singleton
    /** Starts a non-blocking Play availability check when the app launches. */
    fun provideDistributionStartup(coordinator: PlayUpdateCoordinator): DistributionStartup =
        object : DistributionStartup {
            override fun start() = coordinator.checkForUpdates()
        }
}

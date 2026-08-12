package dev.danielkindl.ocho.di

import dev.danielkindl.ocho.DistributionStartup
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt bindings for the updater-free Play distribution. */
@Module
@InstallIn(SingletonComponent::class)
object PlayDistributionModule {
    @Provides
    @Singleton
    /** Supplies the no-op startup implementation used by the Play artifact. */
    fun provideDistributionStartup(): DistributionStartup = object : DistributionStartup {
        override fun start() = Unit
    }
}

package dev.danielkindl.ocho.di

import dev.danielkindl.ocho.DistributionStartup
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt bindings for the Play distribution, which has no in-app update runtime. */
@Module
@InstallIn(SingletonComponent::class)
object PlayDistributionModule {
    @Provides
    @Singleton
    /** Keeps the shared application entry point independent of distribution policy. */
    fun provideDistributionStartup(): DistributionStartup =
        object : DistributionStartup {
            override fun start() = Unit
        }
}

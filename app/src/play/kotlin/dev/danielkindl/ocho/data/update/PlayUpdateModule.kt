package dev.danielkindl.ocho.data.update

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Flavor binding for the Play Core update adapter. */
@Module
@InstallIn(SingletonComponent::class)
abstract class PlayUpdateModule {
    /** Binds the production Play Core adapter to its testable interface. */
    @Binds
    @Singleton
    abstract fun bindPlayUpdateClient(
        implementation: PlayUpdateManagerClient,
    ): PlayUpdateClient
}

package dev.danielkindl.ocho.data.update

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Flavor binding for updater persistence. */
@Module
@InstallIn(SingletonComponent::class)
abstract class GithubUpdateModule {
    /** Binds the DataStore-backed updater state to its testable interface. */
    @Binds
    @Singleton
    abstract fun bindPendingDownloadStore(
        implementation: DataStorePendingDownloadStore,
    ): PendingDownloadStore
}

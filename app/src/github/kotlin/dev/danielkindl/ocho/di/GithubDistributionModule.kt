package dev.danielkindl.ocho.di

import dev.danielkindl.ocho.BuildConfig
import dev.danielkindl.ocho.DistributionStartup
import dev.danielkindl.ocho.data.update.UpdateCheckCache
import dev.danielkindl.ocho.data.update.UpdateRepositoryImpl
import dev.danielkindl.ocho.domain.model.SemVer
import dev.danielkindl.ocho.domain.model.UpdateChannel
import dev.danielkindl.ocho.domain.model.UpdateConfig
import dev.danielkindl.ocho.domain.repository.UpdateRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/** Runs the GitHub build's launch-time update check without blocking startup. */
@Singleton
class GithubDistributionStartup @Inject constructor(
    private val updateRepository: UpdateRepository,
    private val updateCheckCache: UpdateCheckCache,
    private val updateConfig: UpdateConfig,
) : DistributionStartup {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun start() {
        val installedVersion = updateConfig.installedVersion ?: return
        appScope.launch {
            updateRepository.fetchLatestRelease().getOrNull()
                ?.takeIf { it.version > installedVersion }
                ?.let(updateCheckCache::set)
        }
    }
}

/** Hilt bindings that exist only in the GitHub distribution. */
@Module
@InstallIn(SingletonComponent::class)
abstract class GithubDistributionModule {
    @Binds
    @Singleton
    /** Supplies the GitHub Releases implementation to the updater. */
    abstract fun bindUpdateRepository(impl: UpdateRepositoryImpl): UpdateRepository

    @Binds
    @Singleton
    /** Supplies the GitHub startup check to the application root. */
    abstract fun bindDistributionStartup(impl: GithubDistributionStartup): DistributionStartup

    /** Provides the repository and channel settings for this build variant. */
    companion object {
        @Provides
        @Singleton
        /** Builds updater configuration from the generated variant metadata. */
        fun provideUpdateConfig(): UpdateConfig = UpdateConfig(
            repoSlug = BuildConfig.UPDATE_REPO,
            channel = UpdateChannel.fromId(if (BuildConfig.BUILD_TYPE == "dev") "dev" else "stable"),
            installedVersion = SemVer.parse(BuildConfig.VERSION_NAME),
        )
    }
}

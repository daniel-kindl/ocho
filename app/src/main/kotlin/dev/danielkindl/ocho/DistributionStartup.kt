package dev.danielkindl.ocho

/** Distribution-specific work performed when the application starts. */
interface DistributionStartup {
    /** Performs startup work for the selected distribution. */
    fun start()
}

package dev.danielkindl.ocho.domain.model

/**
 * A fixed number of work sets separated by optional rest phases.
 *
 * The final set ends the workout immediately; there is no trailing rest. Keeping
 * that rule in the config's contract lets the planner, setup preview, and engine
 * all describe the same finite session.
 *
 * @property setCount number of work sets to run.
 * @property workMillis length of every work set.
 * @property restMillis length of the rest between sets; zero means no rest phase.
 */
data class CustomConfig(
    val setCount: Int,
    val workMillis: Long,
    val restMillis: Long,
) {
    init {
        require(setCount > 0) { "setCount must be > 0" }
        require(workMillis > 0) { "workMillis must be > 0" }
        require(restMillis >= 0) { "restMillis must be >= 0" }
    }

    /** Total duration, excluding the shared prepare countdown. */
    val totalDurationMillis: Long
        get() = setCount * workMillis + (setCount - 1) * restMillis
}

package org.rsmod.content.skills.smithing.scripts

/** "a steel bar", "an iron bar". [name] is already lower case. */
internal fun withArticle(name: String): String =
    if (name.first() in "aeiou") "an $name" else "a $name"

/**
 * The weak-queue delay for an action every [ticks] ticks. RSMod ticks a weak queue down in the
 * cycle it is queued (input and queue scripts both run before that), so a delay of N fires N - 1
 * ticks later.
 */
internal fun queueDelay(ticks: Int): Int = ticks + 1

package snaykuu.gameLogic

import java.util.*

class GameResult(
    private val snakes: Set<Snake>,
    private val recordedGame: RecordedGame
) {
    /**
     * Return the final position of all snakes as a [SortedMap], where the
     * key is the position (1 == first place, 2 == second place, etc...) and the value
     * is the snake(s) on that position. Note that several snakes can end up on the same
     * position.
     */
    fun getWinners(): SortedMap<Int, List<Snake>> = snakes.getLeaderboard()

    fun getRecordedGame(): RecordedGame = recordedGame
}

fun Iterable<Snake>.getLeaderboard(): SortedMap<Int, List<Snake>> = this.asSequence()
    .groupBy { internalScore(it) }
    .asSequence()
    .mapIndexed { index, entry -> index + 1 to entry.value }
    .toMap()
    .toSortedMap()

private fun internalScore(snake: Snake): Long =
    (snake.getLifespan().toLong() shl 31) or snake.getScore().toLong()
package snaykuu.gameLogic

import java.lang.IllegalStateException
import java.util.concurrent.TimeoutException

class BrainDecision @JvmOverloads constructor(
    private val snake: Snake,
    private val currentState: GameState,
    private var nextMove: Direction? = null,
    private var exception: Throwable? = null
): Thread("Brain: $snake") {

    override fun run() {
        try {
            nextMove = snake.getBrain().getNextMove(snake, currentState)
        } catch(t: Throwable) {
            exception = t
        }
    }

    @Throws(TimeoutException::class)
    fun demandNextMove(): Direction {
        return when {
            isAlive -> {
                stop()
                throw TimeoutException("The brain has taken too long to decide. Summon the minions.")
            }
            exception != null -> throw exception!!
            nextMove != null -> nextMove!!
            else -> throw IllegalStateException()
        }
    }
}
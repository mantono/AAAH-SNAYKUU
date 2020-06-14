package snaykuu.gameLogic

/**
 * Outcome is used to check if a Brain has been doing something bad
 * the last turn or if the outcome was successful. ValidMove means that
 * there have not been any error. If an error occurs, the bot will
 * continue to move in the same direction as it did in the turn before.
 *
 * @author 	Sixten Hilborn
 * @author	Arian Jafari
 */
sealed class Outcome {
    fun validMoveOr(direction: Direction): Direction {
        return when(this) {
            is ValidMove -> this.move
            else -> direction
        }
    }
}

/**
 * ValidMove means that all is well.
 */
class ValidMove(val move: Direction): Outcome()

/**
 * Means that the Brain tries to make an invalid move. There are
 * three possible directions your snake can move in, and backwards
 * is not one of them. Actually, a 180 degree turn is the only
 * invalid move your snake can do.
 */
object InvalidMove: Outcome()

/**
 * This is what you would see if you check the last done move on
 * the very first turn of the game, before there are any previous
 * moves, or the game hasn't even started.
 */
object NotStarted: Outcome()

/**
 * Means that the Brain threw an exception to the game engine. The
 * engine will not receive your movement decisions if you throw,
 * so make sure to keep your exceptions for yourself.
 */
class ThrewException(val exception: Throwable): Outcome()

/**
 * Means that the Brain is taking too long time to think. If you get
 * this error, you should try to change your algorithms so they run
 * in less time.
 */
object TimeOut: Outcome()
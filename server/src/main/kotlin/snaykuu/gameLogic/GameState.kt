package snaykuu.gameLogic

import java.util.*
import kotlin.math.abs

class GameState(
    /**
     * A representation of the current game board
     */
    val board: Board,
    /**
     * A [Set] containing all snakes in the game, both dead ones and alive ones
     */
    val snakes: Set<Snake>,
    /**
     * Current game metadata, containing (among other things)
     * time until the next fruit spawns and time until snake growth
     */
    val metadata: Metadata,
    /**
     * The Outcome for the previous turn, for example telling a brain it
     * took too long to decide last turn.
     */
    val previousTurn: Outcome
) {
    /**
     * Returns the ErrorState for the previous turn, for example telling a brain it
     * took too long to decide last turn.
     *
     * @return    The ErrorState object for last turn.
     * @see ErrorState
     */
    @Deprecated(
        message = "Use method 'previousTurn' instead",
        replaceWith = ReplaceWith("previousTurn", "snaykuu.gameLogic.GameState.previousTurn")
    )
    fun getErrorState(): ErrorState? {
        return when(previousTurn) {
            TimeOut -> ErrorState.TOO_SLOW
            NotStarted -> ErrorState.NO_ERROR
            InvalidMove -> ErrorState.INVALID_MOVE
            is ThrewException -> ErrorState.EXCEPTION
            else -> ErrorState.NO_ERROR
        }
    }

    /**
     * This method can be used to help calculate whether or not a given snake will collide next
     * turn if it continues in a given direction. It looks at the square the snake will end up in,
     * and then checks if that square contains a lethal object. Note that this method returning false
     * does NOT guarantee that the snake will survive. For example, it is possible that a two
     * snakes will move into an empty square during the same turn, causing the death of
     * them both.
     *
     * @param    snake    The snake you wish you perform the check for.
     * @param    dir        The hypothetical direction in which the snake moves.
     * @return    True if the next position contains a lethal object, false if not.
     */
    fun willCollide(snake: Snake, dir: Direction): Boolean {
        val currentHeadPosition = snake.getHeadPosition()
        val nextHeadPosition = calculateNextPosition(dir, currentHeadPosition)
        return board.isLethal(nextHeadPosition)
    }

    /**
     * Gets a list containing the positions of all the fruits currently on the board. Note that
     * the list will be empty if the number of fruits on the board is 0.
     *
     * @return    The positions of the fruits currently on the board.
     */
    fun getFruits(): List<Position> = board.asSequence()
        .filter { it.second.hasFruit() }
        .map { it.first }
        .toList()

    /**
     * Gets a list containing the positions of all the walls currently on the board. Note that
     * the list will be empty if the number of walls on the board is 0.
     *
     * @return    The positions of the walls currently on the board.
     */
    fun getWalls(): List<Position> = board.asSequence()
        .filter { it.second.hasWall() }
        .map { it.first }
        .toList()

    /**
     * This method can be used to calculate the distance between two positions.
     *
     * @param    from        The position from which you wish to calculate the distance.
     * @param    to        The position to which you wish to calculate the distance.
     * @return    An integer representing the distance between two positions.
     */
    @Deprecated(message = "Use method on Position: 'getDistanceTo'")
    fun distanceBetween(from: Position, to: Position): Int = abs(from.x - to.x) + abs(from.y - to.y)

    companion object {
        /**
         * Gets the next position a snake would end up in if it continues in this direction.
         *
         * @param    direction        The current direction of the snake.
         * @param    oldPosition    The current position of the snake.
         * @return    The next position if movement continues in this direction.
         */
        @Deprecated(
            message = "Use method on Direction: 'calculateNextPosition'",
            replaceWith = ReplaceWith("snaykuu.gameLogic.Direction.calculateNextPosition(i)", "snaykuu.gameLogic.Direction")
        )
        @JvmStatic
        fun calculateNextPosition(
            direction: Direction,
            oldPosition: Position
        ): Position = direction.calculateNextPosition(oldPosition)

        /**
         * Returns in which direction one has to move in order to reach one
         * position from another one. Returns an ArrayList containing either zero, one
         * or two elements. For example, it might contain either only `WEST`
         * if the destination position is directly west of the starting position, or it
         * may contain both `WEST` and `NORTH` if
         * the destination is towards the northwest.
         *
         * @param    from        The starting position.
         * @param    to        The destination position.
         * @return    containing either zero, one or two Directions, pointing towards
         * the destination.
         */
        @JvmStatic
        fun getRelativeDirections(from: Position, to: Position): List<Direction> {
            val directions = ArrayList<Direction>(2)
            if(from.x < to.x) directions.add(Direction.EAST) else if(from.x > to.x) directions.add(Direction.WEST)
            if(from.y < to.y) directions.add(Direction.SOUTH) else if(from.y > to.y) directions.add(Direction.NORTH)
            return directions
        }
    }
}
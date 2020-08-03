package snaykuu.gameLogic

enum class Direction(private val x: Int, private val y: Int) {
    NORTH(0, -1),
    WEST(-1, 0),
    SOUTH(0, 1),
    EAST(1, 0);

    /**
     * Gets the next position a snake would end up in if it continues in this direction.
     *
     * @param	from The current position of the snake.
     * @return	The next position if movement continues in this direction.
     */
    fun calculateNextPosition(from: Position): Position = Position(from.x + this.x, from.y + this.y)

    /**
     * Returns a new direction that would be the same as turning left.
     *
     * @return	The direction towards which one would move if one turned left.
     */
    fun turnLeft(): Direction {
        val size: Int = values().size
        val leftIndex: Int = (ordinal - 1 + size) % size
        return values()[leftIndex]
    }

    /**
     * Returns a new direction that would be the same as turning right.
     *
     * @return	The direction towards which one would move if one turned right.
     */
    fun turnRight(): Direction {
        val size: Int = values().size
        val rightIndex: Int = (ordinal + 1) % size
        return values()[rightIndex]
    }

    /**
     * Checks that moving in a given direction is valid, e g that the snake
     * doesn't attempt to turn 180 degrees.
     *
     * @param	direction    The direction in which the snake is attempting to move.
     * @return	<code>true</code> if the attempted move is valid, <code>false</code> if not.
     */
    fun isValidTurn(direction: Direction): Boolean =
        this == direction || (this.ordinal xor direction.ordinal) % 2 != 0
}
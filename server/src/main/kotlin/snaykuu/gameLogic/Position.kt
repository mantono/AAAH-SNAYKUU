package snaykuu.gameLogic

import kotlin.math.abs

/**
 * Represents a coordinate (x and y position) on the game board. All methods requiring
 * coordinates as a parameter should be called using a Position object.
 *
 * @author	Sixten Hilborn
 * @author	Arian Jafari
 */
data class Position(
    val x: Int,
    val y: Int
) {
    fun getDistanceTo(other: Position): Int = abs(this.x - other.x) + abs(this.y - other.y)

    /**
     * Get a list of all neighbours to this position. One position may
     * have up to four neighbours (up, down, left and right).
     *
     * @return A list of neighbour positions.
     */
    fun getNeighbours(): List<Position> = Direction.values()
        .asSequence()
        .map { it.calculateNextPosition(this) }
        .filter { it.x >= 0 && it.y >= 0 }
        .toList()

    fun turn(direction: Direction): Position = direction.calculateNextPosition(this)
}
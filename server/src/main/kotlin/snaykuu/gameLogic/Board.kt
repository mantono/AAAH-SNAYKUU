package snaykuu.gameLogic

import java.util.*

/**
 * This class represents the entire game board through a 2D-array of Square objects.
 * Each Square, in turn, contains GameObjects, such as fruits, walls, or other snakes.
 *
 * @author 	Sixten Hilborn
 * @author	Arian Jafari
 * @see		Square
 */
data class Board(private val width: Int, private val height: Int) {

    private val board: Array<Int> = Array(width * height) { 0 }

    init {
        require(width >= 1) { "Width must be at least 1" }
        require(height >= 1) { "Height must be at least 1" }
    }

    fun getWidth(): Int = width
    fun getHeight(): Int = height

    /**
     * Returns whether or not the board contains any game object at the given position.
     * Doesn't perform any checks on what type of object it is, like if it is lethal or not.
     *
     * @param	position The position we want to check for game objects.
     * @return	Whether or not the board contains a game object at the given position.
     */
    fun hasGameObject(position: Position): Boolean = !getSquare(position).isEmpty()

    /**
     * Returns whether or not the board contains a fruit at the given position.
     *
     * @param	position The position we want to check for fruit.
     * @return	Whether or not the board contains a fruit at the given position.
     */
    fun hasFruit(position: Position): Boolean = getSquare(position).hasFruit()

    /**
     * Returns whether or not the board contains a wall at the given position.
     *
     * @param	position The position we want to check for walls.
     * @return	Whether or not the board contains a wall at the given position.
     */
    fun hasWall(position: Position): Boolean = getSquare(position).hasWall()

    /**
     * Returns whether or not the board contains a snake at the given position.
     *
     * @param	position The position we want to check for snakes.
     * @return	Whether or not the board contains a snake at the given position.
     * @see		Square
     */
    fun hasSnake(position: Position): Boolean = getSquare(position).hasSnake()

    /**
     * Returns whether or not the board contains a lethal game object at the given position.
     *
     * @param	position The position we want to check for lethal objects.
     * @return	Whether or not the board contains a lethal game object at the given position.
     * @see		Square
     */
    fun isLethal(position: Position): Boolean = getSquare(position).isLethal()

    fun getSquare(position: Position): Int = board[position]

    /**
     * Calculates whether or not the board contains a lethal object within a given radius of
     * a certain square. Works by using a depth-first search.
     *
     * @param    position        The position which we want to check.
     * @param    range    The number of squares we wish to examine, e.g.
     * the radius of the area we want to check.
     * @return    A boolean indicating whether or not there is a lethal object
     * within the given range of the specified position.
     */
    fun hasLethalObjectWithinRange(position: Position, range: Int): Boolean {
        val visited: MutableSet<Position> = HashSet()
        depthFirstSearch(position, visited, range)
        return containsLethalObject(visited)
    }

    private fun depthFirstSearch(
        from: Position,
        visited: MutableSet<Position>,
        range: Int
    ) {
        val newRange: Int = range - 1
        if(newRange < 0) {
            return
        }
        visited.add(from)
        from.neighbours.asSequence()
            .filter { !visited.contains(it) }
            .forEach { depthFirstSearch(it, visited, newRange) }
    }

    private fun containsLethalObject(positions: Set<Position>): Boolean {
        return positions.asSequence()
            .map { getSquare(it) }
            .any { it.hasSnake() || it.hasWall() }
    }

    fun addGameObject(obj: GameObject, position: Position) {
        board[position] = obj
    }

    internal fun clearSquare(position: Position) {
        board[index(position)] = 0
    }

    fun removeGameObject(obj: GameObject, position: Position) {
        board[position] -= obj
    }

    internal fun removeFruit(position: Position) {
        board[position] -= Fruit
    }

    internal operator fun Array<Int>.get(position: Position): Int {
        val index: Int = index(position)
        return board[index]
    }

    internal operator fun Array<Int>.set(position: Position, obj: GameObject) {
        board[position] += obj
    }

    private fun index(position: Position): Int = (position.y * width) + position.x
}
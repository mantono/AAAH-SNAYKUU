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
class Board internal constructor(
    private val width: Int,
    private val height: Int,
    private val squares: Array<Int>
): Iterable<Pair<Position, Square>> {
    constructor(width: Int, height: Int): this(width, height, createBoard(width, height))

    init {
        require(width >= MIN_SIZE) { "Width must be at least $MIN_SIZE (including walls)" }
        require(height >= MIN_SIZE) { "Height must be at least $MIN_SIZE (including walls)" }
        require(width * height == squares.size)
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

    fun getSquare(position: Position): Square = this[position]

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
        from.getNeighbours().asSequence()
            .filter { !visited.contains(it) }
            .forEach { depthFirstSearch(it, visited, newRange) }
    }

    private fun containsLethalObject(positions: Set<Position>): Boolean {
        return positions.asSequence()
            .map { getSquare(it) }
            .any { it.isLethal() }
    }

    internal fun removeFruit(position: Position): Boolean = remove(position, Fruit)

    internal fun add(position: Position, obj: GameObject): Boolean {
        val currentValue: Int = this[position].toInt()
        val newValue: Int = currentValue and (obj.value())
        this[position] = newValue
        return currentValue != newValue
    }

    internal fun clearSquare(position: Position): Boolean {
        val currentValue: Int = this[position].toInt()
        this[position] = 0
        return currentValue != 0
    }

    internal fun removeAllSnakes(): List<Position> {
        return asSequence()
            .filter { it.second.hasSnake() }
            .map { it.first }
            .onEach {
                val currentValue: Int = this[it].toInt()
                val newValue: Int = currentValue and (Wall + Fruit)
                squares[index(it)] = newValue
            }
            .toList()
    }

    internal fun getAllSnakes(): Map<Int, List<Position>> {
        return asSequence()
            .filter { it.second.hasSnake() }
            .map { it.second.snakeValues() to it.first }
            .map { it.first.map { x -> x to it.second } }
            .flatten()
            .groupBy { it.first }
            .map { it.key to it.value.map { x -> x.second } }
            .toMap()
    }

    internal fun addSnake(snake: Snake): Boolean {
        require(snake.size > 0) { "Cannot add an empty snake" }
        return snake.getSegments()
            .map { add(it, snake) }
            .any()
    }

    private fun remove(position: Position, obj: GameObject): Boolean {
        val currentValue: Int = this[position].toInt()
        val newValue: Int = currentValue and (obj.value().inv())
        this[position] = newValue
        return currentValue != newValue
    }

    override fun iterator(): Iterator<Pair<Position, Square>> = this.squares
        .asSequence()
        .mapIndexed { i: Int, value: Int -> Position(i % width, i / height) to Square(value) }
        .iterator()

    internal fun serialize(): List<Int> = this.squares.toList()

    internal fun squares(): Sequence<Square> = this.squares.asSequence().map { Square(it) }

    internal fun positions(): Sequence<Position> = this.squares
        .asSequence()
        .map { i -> Position(i % width, i / height) }

    private operator fun List<Int>.get(position: Position): Square {
        val index: Int = index(position)
        return Square(squares[index])
    }

    operator fun get(position: Position): Square = this[index(position)]
    internal operator fun get(index: Int): Square = Square(squares[index])
    private fun index(position: Position): Int = (position.y * width) + position.x

    private operator fun set(position: Position, value: Int) {
        val current: Int = squares[index(position)]
        val new: Int = current or value
        squares[index(position)] = new
    }

    companion object {
        private const val MIN_SIZE: Int = 3

        private fun createBoard(width: Int, height: Int): Array<Int> {
            val board = Array(width * height) { 0 }
            addWalls(board, width, height)
            return board
        }

        private fun addWalls(board: Array<Int>, width: Int, height: Int) {
            board.asSequence()
                .filter { i ->
                    when {
                        // North wall
                        i < width -> true
                        // South wall
                        i > width * (height - 1) -> true
                        // West wall
                        i % width == 0 -> true
                        // East wall
                        i % width == (width - 1) -> true
                        else -> false
                    }
                }
                .forEach { i: Int -> board[i] = Wall.value() }
        }
    }
}

internal operator fun GameObject.plus(other: GameObject): Int {
    return this.value() + other.value()
}
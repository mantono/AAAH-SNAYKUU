package snaykuu.gameLogic

import java.util.*
import kotlin.collections.ArrayList

/**
 * This class represents the entire game board through a 2D-array of Square objects.
 * Each Square, in turn, contains GameObjects, such as fruits, walls, or other snakes.
 *
 * @author 	Sixten Hilborn
 * @author	Arian Jafari
 * @see		Square
 */
class Board private constructor(
    private val width: Int,
    private val height: Int,
    private val squares: List<Int>
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

    fun getSquare(position: Position): Square = squares[position]

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
            .any { it.isLethal() }
    }

    internal fun removeFruit(position: Position): Board {
        return modify(position) { current: Int -> current - Fruit.value() }
    }

    internal fun add(position: Position, obj: GameObject): Board {
        return modify(position) { it + obj.value() }
    }

    internal fun clearSquare(position: Position): Board {
        return modify(position) { 0 }
    }

    internal fun remove(position: Position, obj: GameObject): Board {
        return modify(position) { current: Int -> current - obj.value() }
    }

    internal fun removeAll(positions: List<Position>, obj: GameObject): Board {
        return modify(positions) { current: Int -> current - obj.value() }
    }

    internal fun removeAllSnakes(): Board =
        Board(width, height, squares.map { it and (Wall.value() + Fruit.value()) })

    internal fun addAll(objects: Map<GameObject, List<Position>>): Board {
        val copy: MutableList<Int> = ArrayList(squares)
        objects.asSequence()
            .map { it.value.map { pos -> pos to it.key.value() } }
            .flatten()
            .forEach { (position, value) -> copy[index(position)] += value }

        return Board(width, height, copy)
    }

    private fun set(position: Position, newValue: Int): Board {
        val updatedBoard: List<Int> = squares.replace(index(position), newValue)
        return Board(width, height, updatedBoard)
    }

    private inline fun modify(
        position: Position,
        modify: (current: Int) -> Int
    ): Board {
        val currentValue: Int = squares[position].toInt()
        val newValue: Int = modify(currentValue)
        return if(newValue != currentValue) {
            val updatedBoard: List<Int> = squares.replace(index(position), newValue)
            return Board(width, height, updatedBoard)
        } else {
            this
        }
    }

    private inline fun modify(
        positions: List<Position>,
        crossinline modify: (current: Int) -> Int
    ): Board {
        val changes: Map<Int, Int> = positions
            .asSequence()
            .map { index(it) to squares[it] }
            .map { it.first to modify(it.second.toInt()) }
            .toMap()

        val updatedBoard: List<Int> = squares.replace(changes)
        return Board(width, height, updatedBoard)
    }

    override fun iterator(): Iterator<Pair<Position, Square>> = this.squares
        .asSequence()
        .mapIndexed { i: Int, value: Int -> Position(i % width, i / height) to Square(value) }
        .iterator()

    internal fun serialize(): List<Int> = this.squares

    internal fun squares(): Sequence<Square> = this.squares.asSequence().map { Square(it) }

    internal fun positions(): Sequence<Position> = this.squares
        .asSequence()
        .map { i -> Position(i % width, i / height) }

    internal operator fun List<Int>.get(position: Position): Square {
        val index: Int = index(position)
        return Square(squares[index])
    }

    private fun index(position: Position): Int = (position.y * width) + position.x

    companion object {
        private const val MIN_SIZE: Int = 3

        private fun createBoard(width: Int, height: Int): List<Int> {
            val board = Array(width * height) { 0 }
            addWalls(board, width, height)
            return board.toList()
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

private fun List<Int>.replace(i: Int, value: Int): List<Int> {
    val list = this.toMutableList()
    list[i] = value
    return list
}

private fun List<Int>.replace(changes: Map<Int, Int>): List<Int> {
    val list = this.toMutableList()
    changes.forEach { (i, value) -> list[i] = value }
    return list
}
package snaykuu.gameLogic

import java.awt.Color
import java.io.Serializable
import java.lang.IllegalStateException
import java.util.*

/**
 * The Snake class is a representation of each snake currently in the game,
 * including its name, its current direction, its position on the game board,
 * and its current statistics.
 *
 * It is a subclass of the GameObject class, in order for snakes to be able to
 * be inserted into Square objects.
 *
 * @author	Sixten Hilborn
 * @author	Arian Jafari
 */
data class Snake @JvmOverloads constructor(
    private val id: Int,
    private val name: String,
    private val brain: Brain,
    private val segments: Deque<Position> = LinkedList(),
    private val directionLog: Deque<SnakeSegment> = LinkedList(),
    private val score: Int = 0,
    private val lifespan: Int = 0,
    private val isDead: Boolean = false,
    private val color: Color
): GameObject, Serializable {

    /**
     * Get a list of all the squares this snake has occupied.
     *
     * @return A list of the positions of the occupied squares.
     */
    fun getSegments(): Deque<Position> = segments

    /**
     * Get a list of SnakeSegments, which represent each square this snake is
     * occupying together with its direction at each point.
     *
     * This method is mainly useful for drawing purposes, getSegments() is
     * recommended for most other uses.
     *
     * @return A list of this snake's positions and directions.
     */
    fun getDrawData(): Deque<SnakeSegment> = directionLog

    /**
     * Gets the current position of this snake's head.
     *
     * @return	The Position of the snake's head.
     * @see		Position
     */
    fun getHeadPosition(): Position = segments.first()
        ?: throw IllegalStateException("Function called before snake had a head position")

    /**
     * Gets the current position of the last segment of the snake's tail.
     *
     * @return	The Position of the snake's last tail segment.
     */
    fun getTailPosition(): Position = segments.last()
        ?: throw IllegalStateException("Function called before snake had a tail position")

    /**
     * Returns whether or not this snake is dead. Note that a dead snake won't be
     * removed from the board; it will only be unable to move and unable to win the
     * game.
     *
     * @return	A boolean; true if the snake is dead, false if not.
     */
    fun isDead(): Boolean = isDead

    /**
     * Gets the direction this snake moved in last turn. Note that it does not actually
     * get the direction the snake will move in next turn.
     *
     * @return	The Direction in which the snake moved last turn.
     */
    fun getCurrentDirection(): Direction = directionLog.first.dir
        ?: throw IllegalStateException("Function called before snake had an entry in direcitonLog")

    /**
     * Gets the number of fruits this snake has eaten.
     *
     * @return	The number of fruits eaten by this snake.
     */
    fun getScore(): Int = score

    /**
     * Gets the number of turns this snake has lived. If the snake is currently dead,
     * this method returns the number of turns it was alive before its death.
     *
     * @return 	Age of the snake, in turns.
     */
    fun getLifespan(): Int = lifespan

    override fun toString(): String = name

    protected fun placeOnBoard(segments: Deque<Position>, originalDirection: Direction): Snake {
        val newDirections: List<SnakeSegment> = segments.map { SnakeSegment(it, originalDirection) }
        return this.copy(segments = segments, directionLog = directionLog.append(newDirections))
    }

    internal fun moveHead(dir: Direction): Snake {
        val pos: Position = dir.calculateNextPosition(getHeadPosition())
        return this.copy(segments = segments.append(pos), directionLog = directionLog.append(SnakeSegment(pos, dir)))
    }

    internal fun removeTail(): Snake {
        return this.copy(directionLog = directionLog.also { it.removeLast() })
    }

    internal fun kill(): Snake = this.copy(isDead = true)
    internal fun getBrain(): Brain = brain
    internal fun addScore(): Snake = this.copy(score = score + 1)
    internal fun increaseLifespan(): Snake = this.copy(lifespan = lifespan + 1)
    internal fun removeBrain(): Snake = this.copy(brain = BrainDead)
    fun getColor(): Color = color
    fun getName(): String = name

    override fun getTypeName(): String = "Snake"
    override fun value(): Int = 1 shl(id + 2)
    override fun isLethal(): Boolean = true
}

private object BrainDead: Brain {
    override fun getNextMove(yourSnake: Snake, gameState: GameState): Nothing {
        throw IllegalStateException("This brain should not be called")
    }
}

fun <T> Deque<T>.append(other: T): Deque<T> {
    val new = LinkedList(this)
    new.addLast(other)
    return new
}

fun <T> Deque<T>.append(other: List<T>): Deque<T> {
    val new = LinkedList(this)
    new.addAll(other)
    return new
}
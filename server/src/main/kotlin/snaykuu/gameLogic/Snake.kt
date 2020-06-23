package snaykuu.gameLogic

import java.awt.Color
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
    private var brain: Brain = BrainDead,
    private val segments: LinkedList<Position> = LinkedList(),
    private val directionLog: LinkedList<SnakeSegment> = LinkedList(),
    private var score: Int = 0,
    private var lifespan: Int = 0,
    private var isDead: Boolean = false,
    private val color: Color
): GameObject, SerializableSnake {

    /**
     * Get the size of the snake
     */
    val size: Int = segments.size

    /**
     * Get a list of all the squares this snake has occupied.
     *
     * @return A list of the positions of the occupied squares.
     */
    fun getSegments(): List<Position> = segments

    /**
     * Get a list of SnakeSegments, which represent each square this snake is
     * occupying together with its direction at each point.
     *
     * This method is mainly useful for drawing purposes, getSegments() is
     * recommended for most other uses.
     *
     * @return A list of this snake's positions and directions.
     */
    fun getDrawData(): List<SnakeSegment> = directionLog

    /**
     * Gets the current position of this snake's head.
     *
     * @return	The Position of the snake's head.
     * @see		Position
     */
    fun getHeadPosition(): Position = segments.firstOrNull()
        ?: throw IllegalStateException("Function called before snake had a head position")

    /**
     * Gets the current position of the last segment of the snake's tail.
     *
     * @return	The Position of the snake's last tail segment.
     */
    fun getTailPosition(): Position = segments.firstOrNull()
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
    fun getCurrentDirection(): Direction = directionLog.firstOrNull()?.dir
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

//    protected fun placeOnBoard(segments: List<Position>, originalDirection: Direction): Snake {
//        val newDirections: List<SnakeSegment> = segments.map { SnakeSegment(it, originalDirection) }
//        return this.copy(segments = segments, directionLog = directionLog + newDirections)
//    }

    internal fun moveHead(dir: Direction) {
        val pos: Position = dir.calculateNextPosition(getHeadPosition())
        segments.push(pos)
        directionLog.push(SnakeSegment(pos, dir))
    }

    internal fun removeTail() {
        segments.removeLast()
    }

    internal fun initAt(head: Position, facing: Direction) {
        check(!isDead())
        check(lifespan == 0)
        check(segments.isEmpty())
        segments.push(head)
        directionLog.push(SnakeSegment(head, facing))
    }

    internal fun kill() {
        this.isDead = true
    }

    internal fun removeBrain(): Snake {
        this.brain = BrainDead
        return this
    }

    internal fun getBrain(): Brain = brain

    internal fun addScore(): Int {
        check(!isDead)
        return ++score
    }

    internal fun increaseLifespan(): Int {
        check(!isDead())
        return ++lifespan
    }

    fun getColor(): Color = color
    override fun getName(): String = name

    override fun getTypeName(): String = "Snake"
    override fun value(): Int = 1 shl(id + 2)
    override fun isLethal(): Boolean = true

    companion object {
        private fun colorForSnake(
            rand: Random,
            i: Int,
            stepSize: Float
        ): Color {
            val hue: Float = stepSize * i
            val saturation: Float = rand.nextFloat() / 2 + 0.5f
            val brightness: Float = rand.nextFloat() / 2 + 0.5f
            return Color.getHSBColor(hue, saturation, brightness)
        }

        @JvmOverloads
        fun create(
            snakeData: Map<String, Brain?>,
            random: Random = Random(4L)
        ): MutableSet<Snake> {
            val numSnakes: Int = snakeData.size
            val stepSize: Float = 0.8f / numSnakes
            return snakeData.asSequence()
                .sortedBy { it.key }
                .mapIndexed { index, entry ->
                    val brain: Brain = entry.value ?: BrainDead
                    val color = colorForSnake(random, index, stepSize)
                    Snake(index, entry.key, brain, color = color)
                }
                .toMutableSet()
        }
    }
}

private object BrainDead: Brain {
    override fun getNextMove(yourSnake: Snake, gameState: GameState): Nothing {
        throw IllegalStateException("This brain should not be called")
    }
}
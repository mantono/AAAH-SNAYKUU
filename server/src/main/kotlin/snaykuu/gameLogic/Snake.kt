package snaykuu.gameLogic

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
    private val name: String = "Snake $id",
    private var brain: Brain = BrainDead,
    private val segments: LinkedList<Position> = LinkedList(),
    private val directionLog: LinkedList<SnakeSegment> = LinkedList(),
    private var score: Int = 0,
    private var lifespan: Int = 0,
    private var isDead: Boolean = false,
    private val color: Color = Color()
): GameObject, SerializableSnake {

    /**
     * Get the size of the snake
     */
    fun size(): Int = segments.size

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
     * Returns true if a Snake has collided with itself, and is effectively dead.
     */
    fun hasOverlap(): Boolean = getSegments().distinct().size < getSegments().size

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

    internal fun moveHead(dir: Direction): Position {
        val pos: Position = dir.calculateNextPosition(getHeadPosition())
        return moveHead(pos, dir)
    }

    internal fun moveHead(pos: Position): Position {
        val dir: Direction = GameState.getRelativeDirections(getHeadPosition(), pos).first()
        return moveHead(pos, dir)
    }

    private fun moveHead(pos: Position, dir: Direction): Position {
        if(pos == getHeadPosition()) {
            return pos
        }
        require(getHeadPosition().getDistanceTo(pos) == 1) {
            "Cannot move the head more than one square at a time"
        }
        segments.push(pos)
        directionLog.push(SnakeSegment(pos, dir))
        return pos
    }

    internal fun removeTail(): Position = segments.removeLast()

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
        private fun colorForSnake(i: Int, stepSize: Float): Color {
            val red = Color(255, 0, 0)
            return red.changeHue(i * stepSize)
        }

        fun create(snakeData: Map<String, Brain?>): MutableSet<Snake> {
            val numSnakes: Int = snakeData.size
            val stepSize: Float = 360f / numSnakes
            return snakeData.asSequence()
                .sortedBy { it.key }
                .mapIndexed { index, entry ->
                    val brain: Brain = entry.value ?: BrainDead
                    val color = colorForSnake(index, stepSize)
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


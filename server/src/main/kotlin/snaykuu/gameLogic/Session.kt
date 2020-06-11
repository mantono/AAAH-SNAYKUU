package snaykuu.gameLogic

import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeoutException
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class Session(
    private val board: Board,
    private val metadata: Metadata,
    private val snakes: MutableSet<Snake> = HashSet(16),
    private val snakeErrors: MutableMap<Snake, ErrorState> = HashMap(16)
): Game {
    private val random: Random = Random()
    private var recordedGame: RecordedGame? = null

    init {
        perhapsSpawnFruint()
    }

    constructor(metadata: Metadata): this(createStandardBoard(metadata.boardWidth, metadata.boardHeight), metadata)

    override fun getCurrentState(): GameState = GameState(board, snakes, metadata, ErrorState.NO_ERROR)

    fun addSnake(newSnake: Snake) {
        snakes.add(newSnake)
    }

    override fun getGameResult(): GameResult = GameResult(snakes, metadata, recordedGame)
    override fun getMetadata(): Metadata = metadata

    fun getBoard(): Board = board
    fun getSnakes(): Set<Snake> = snakes

    fun prepareForStart() {
        placeSnakesOnBoard()
        recordedGame = RecordedGame(metadata, board, snakes)
    }

    /**
     * Checks whether the game has ended or not. If only one snake remains alive (and
     * the game was started using more than one snake), or if a snake has achieved the
     * minimum required score in order to win the game, this method returns true.
     *
     * @return	<code>true</code> if the game has ended, <code>false</code> if not.
     */
    fun hasEnded(): Boolean {
        val numberOfLivingSnakes: Int = snakes.count { !it.isDead() }
        val players: Int = snakes.size
        val maxScore: Int = snakes.maxBy { it.getScore() }?.getScore() ?: -1

        return when {
            numberOfLivingSnakes == 0 -> true
            numberOfLivingSnakes == 1 && numberOfLivingSnakes < players -> true
            maxScore >= metadata.fruitGoal -> true
            else -> false
        }
    }

    /**
     * Moves all the snakes simultaneously, checks for collision, kills colliding snakes,
     * adds point when fruit is eaten, and updates the gamestate.
     */
    fun tick() {
        val growth: Boolean = checkForGrowth()
        val moves: Map<Snake, Direction> = getDecisionsFromSnakes()
        moveAllSnakes(moves, growth)
        checkForCollision()
        perhapsSpawnFruit()
        val frame = Frame(board, snakes)
        recordedGame!!.addFrame(frame)
    }

    fun cleanup() {
        snakes.forEach { it.removeBrain() }
    }

    private fun checkForGrowth(): Boolean {
        val timeTillGrowth: Int = recordedGame!!.turnCount % metadata.growthFrequency
        return timeTillGrowth == 0
    }

    /**
     * Returns a HashMap, with each position containing a Snake object and
     * the Direction towards which the given snake wishes to move next turn.
     * Spawns a single thread for each participating snake, then waits until
     * their allotted time is up. If a snake hasn't responded yet, it's direction
     * is defaulted to Direction.FORWARD.
     *
     * @see		BrainDecision
     * @return 	The HashMap containing snakes and their next moves.
     */
    private fun getDecisionsFromSnakes(): Map<Snake, Direction> {
        val decisionThreads: Map<Snake, BrainDecision> = snakes.asSequence()
            .filter { !it.isDead() }
            .map {
                val error: ErrorState = snakeErrors.remove(it) ?: ErrorState.NO_ERROR
                val currentGameState = GameState(board, snakes, metadata, error)
                it to BrainDecision(it, currentGameState)
            }
            .toMap()

        // This is a bit ugly now, but all this will become so much better when we use coroutines
        decisionThreads.values.forEach { it.start() }
        val start = Instant.now()
        val thinkingTime = Duration.ofMillis(metadata.maximumThinkingTime.toLong())
        val waitTime: Duration = thinkingTime.dividedBy(10L).coerceAtLeast(Duration.ofMillis(1L))
        val stopAt: Instant = start.plus(thinkingTime)
        while(Instant.now() < stopAt && !decisionThreads.values.all { it.isAlive }) {
            Thread.sleep(waitTime.toMillis())
        }

        returdecisionThreads.asSequence()
            .map { it.key to parseMove(it.key, it.value) }
            .map { it.first to it.second.validMoveOr(it.first.getCurrentDirection()) }
            .toMap()
    }

    fun parseMove(snake: Snake, decision: BrainDecision): Decision {
        return try {
            val move: Direction = decision.demandNextMove()
            return if(isValidTurn(snake.getCurrentDirection(), move)) {
                ValidMove(move)
            } else {
                InvalidMove
            }
        } catch(t: TimeoutException) {
            println("$snake is too slow")
            TimeOut
        } catch(t: Throwable) {
            println("$snake is tossing an exception in our face: $t")
            ThrewException(t)
        }
    }

    private fun isValidTurn(current: Direction, next: Direction): Boolean {
        return when {
            current == next -> true
            current.ordinal % 2 != 0 && next.ordinal % 2 != 0 -> true
            else -> false
        }
    }

    companion object {
        /**
         * Generates a standard snake board, sized width x height, with lethal walls around the edges.
         * @param width        Desired board height.
         * @param height    Desired board width.
         * @return            The newly generated board.
         */
        private fun createStandardBoard(width: Int, height: Int): Board {
            val board = Board(width, height)

            for(x in 0 until width) {
                val bottomRowPos = Position(x, 0)
                val topRowPos = Position(x, height - 1)
                board.addGameObject(Wall, bottomRowPos)
                board.addGameObject(Wall, topRowPos)
            }

            for(y in 0 until height) {
                val leftmostColumnPos = Position(0, y)
                val rightmostColumnPos = Position(width - 1, y)
                board.addGameObject(Wall, leftmostColumnPos)
                board.addGameObject(Wall, rightmostColumnPos)
            }
            return board
        }
    }
}

sealed class Decision {
    fun validMoveOr(direction: Direction): Direction {
        when(this) {
            is ValidMove -> this.move
            else -> direction
        }
    }
}
class ValidMove(val move: Direction): Decision()
object InvalidMove: Decision()
class ThrewException(val exception: Throwable): Decision()
object TimeOut: Decision()
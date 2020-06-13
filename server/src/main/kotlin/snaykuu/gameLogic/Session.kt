package snaykuu.gameLogic

import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeoutException
import kotlin.collections.HashSet

class Session(
    private val board: Board,
    private val metadata: Metadata,
    private val snakes: Set<Snake> = HashSet(16),
    private val snakeErrors: Map<Snake, ErrorState> = HashMap(16)
): Game {
    private val random: Random = Random()
    private var recordedGame: RecordedGame? = null

    init {
        perhapsSpawnFruint()
    }

    constructor(metadata: Metadata): this(Board(metadata.boardWidth, metadata.boardHeight), metadata)

    override fun getCurrentState(): GameState = GameState(board, snakes, metadata, NotStarted)

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
    override fun hasEnded(): Boolean {
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
    override fun tick(): Game {
        check(!hasEnded()) { "Game has ended" }
        val growth: Boolean = checkForGrowth()
        val moves: Map<Snake, Direction> = getDecisionsFromSnakes()
        val updatedBoard: Board = updateBoardState(snakes, moves, board)
        val boardWithNewFruit: Board = perhapsSpawnFruit(updatedBoard)
        val updatedSnakeState: Set<Snake> = updateSnakeStates(snakes, board)
        val frame = Frame(board, snakes)
        recordedGame!!.addFrame(frame)
        return Session(boardWithNewFruit, metadata, updatedSnakeState)
    }

    fun cleanup() {
        snakes.forEach { it.removeBrain() }
    }

    private fun checkForGrowth(): Boolean {
        val timeTillGrowth: Int = recordedGame!!.turnCount % metadata.growthFrequency
        return timeTillGrowth == 0
    }

    private fun perhapsSpawnFruit(board: Board): Board {
        val timeTillFruitSpawn = recordedGame!!.turnCount % metadata.fruitFrequency
        if(timeTillFruitSpawn != 0) {
            return board
        }
        val (position, _) = board.asSequence()
            .filter { it.second.isEmpty() }
            .toList()
            .shuffled(random)
            .firstOrNull() ?: return board

        return board.add(position, Fruit)
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
                val currentGameState = GameState(board, snakes, metadata, NotStarted)
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

        return decisionThreads.asSequence()
            .map { it.key to parseMove(it.key, it.value) }
            .map { it.first to it.second.validMoveOr(it.first.getCurrentDirection()) }
            .toMap()
    }

    fun parseMove(snake: Snake, decision: BrainDecision): Outcome {
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

    // TODO: Make extension function on Direction
    /**
     * Checks that moving in a given direction is valid, e g that the snake
     * doesn't attempt to turn 180 degrees.
     *
     * @param	current	The current direction of the snake.
     * @param	next    The direction in which the snake is attempting to move.
     * @return	<code>true</code> if the attempted move is valid, <code>false</code> if not.
     */
    private fun isValidTurn(current: Direction, next: Direction): Boolean =
        current == next || (current.ordinal xor next.ordinal) % 2 != 0

    /**
     * Moves all the snakes by calling the <code>moveSnake</code> for each snake.
     *
     * @param	moves		Map of each snake to its desired movement.
     * @param	growSnakes	Whether or not snakes are supposed to grow this turn.
     */
    private fun moveAllSnakes(moves: Map<Snake, Direction>, growSnakes: Boolean): Pair<Board, Set<Snakes>> {
        moves.forEach { (snake, dir) -> moveSnake(snake, dir, growSnakes) }
    }

    /**
     * Moves a single snake in the specified direction and grows the snake if necessary.
     * Works by moving the position of the snake's head, and then also moving its tail
     * (unless growth is specified).
     *
     * @param	snake	The snake that is going to be moved.
     * @param	dir		The direction in which the snake is to be moved.
     * @param	grow	Whether or not the snake is supposed to grow this turn.
     */
    private fun moveSnake(snake: Snake, dir: Direction, grow: Boolean): Board {
        val updatedBoard: Board = board.add(snake.moveHead(dir).getHeadPosition(), snake)
        return if(!grow) {
            updatedBoard.remove(snake.getTailPosition(), snake)
        } else {
            updatedBoard
        }
    }
    /**
     * Checks if any collision has occurred, and performs necessary actions.
     * If the head of a snake has collided with a lethal object, that snake is
     * killed (e g marked as dead). If it collided with a fruit, the appropriate amount of points is
     * added to that snake's score.
     */
    private fun updateSnakeStates(snakes: Collection<Snake>, board: Board): Set<Snake> {
        return snakes.asSequence()
            .map { snake ->
                val head: Position = snake.getHeadPosition()
                val square: Square = board.getSquare(head)
                when {
                    square.hasMultipleSnakes() -> snake.kill()
                    square.hasWall() -> snake.kill()
                    square.hasFruit() -> snake.addScore().increaseLifespan()
                    else -> snake.increaseLifespan()
                }
            }
            .toSet()
    }

    private fun updateBoardState(
        snakes: Collection<Snake>,
        moves: Map<Snake, Direction>,
        board: Board
    ): Board {
        val eatenFruits: List<Position> = board.asSequence()
            .filter { it.second.hasSnakeEatingFruit() }
            .map { it.first }
            .toList()

        val movedSnakeHeads: List<Position> = snakes.asSequence()
            .map { snake -> snake.moveHead(snake.getCurrentDirection()) }
    }
}

sealed class Outcome {
    fun validMoveOr(direction: Direction): Direction {
        return when(this) {
            is ValidMove -> this.move
            else -> direction
        }
    }
}
class ValidMove(val move: Direction): Outcome()
object InvalidMove: Outcome()
object NotStarted: Outcome()
class ThrewException(val exception: Throwable): Outcome()
object TimeOut: Outcome()
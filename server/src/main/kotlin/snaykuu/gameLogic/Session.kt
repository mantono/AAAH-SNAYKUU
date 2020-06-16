package snaykuu.gameLogic

import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeoutException
import kotlin.collections.HashSet
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class Session(
    private val board: Board,
    private val metadata: Metadata,
    private val snakes: Set<Snake> = HashSet(16),
    private val recordedGame: GameRecorder = GameRecorder(metadata),
    private val random: Random = Random(),
    private val turn: Int = 0
): Game {
    constructor(metadata: Metadata): this(Board(metadata.boardWidth, metadata.boardHeight), metadata)

    override fun getCurrentState(): GameState = GameState(board, snakes, metadata, NotStarted)
    override fun getGameResult(): GameResult = GameResult(snakes, metadata, recordedGame)
    override fun getMetadata(): Metadata = metadata

    fun getBoard(): Board = board
    fun getSnakes(): Set<Snake> = snakes

    fun prepareForStart(): Session {
        return if(turn == 0) {
            val (boardWithSnakes: Board, snakesWithBodies: Set<Snake>) = placeSnakesOnBoard(board, snakes)
            recordedGame.start()
            Session(boardWithSnakes, metadata, snakesWithBodies, recordedGame, random, turn + 1)
        } else {
            this
        }
    }

    private fun placeSnakesOnBoard(board: Board, snakes: Collection<Snake>): Pair<Board, Set<Snake>> {
        val startingPositions: Map<Position, Direction> = startingHeadPositions(
            snakes = snakes.size,
            width = board.getWidth(),
            height = board.getHeight()
        )
            .shuffled(random)
            .map { it to faceInwards(it, board.getWidth(), board.getHeight()) }
            .toMap()

        check(snakes.size == startingPositions.size) {
            "Failed to generate unique positions for all snakes"
        }

        val snakesWithBodies: Set<Snake> = startingPositions.asSequence()
            .zip(snakes.asSequence())
            .map {
                val segment = SnakeSegment(it.first.key, it.first.value)
                it.second.copy(
                    segments = it.second.getSegments() + it.first.key,
                    directionLog = it.second.getDrawData() + segment
                )
            }
            .toSet()

        val boardWithSnakes: Board = snakesWithBodies.fold(board) { acc: Board, snake: Snake ->
            acc.addSnake(snake)
        }

        return boardWithSnakes to snakesWithBodies
    }

    /**
     * Gets appropriate starting positions for snake heads.
     *
     * @param	snakes	The number of snakes in the game.
     * @param	xSize	The width of the board.
     * @param	ySize	The height of the board.
     * @return	An array of starting positions with as many elements as the number of snakes in the game.
     */
    private fun startingHeadPositions(snakes: Int, width: Int, height: Int): List<Position> {
        val xCenter: Int = width/2
        val yCenter: Int = height/2
        val edgeOffset: Int = 2

        val angleStep: Double = 2 * Math.PI / snakes
        var nextStep: Double = random.nextDouble() * 2 * Math.PI

        return (0 until snakes)
            .map {
                val xRadius: Int = xCenter - edgeOffset
                val yRadius: Int = yCenter - edgeOffset

                val x: Int = (xRadius * cos(nextStep) + 0.5).roundToInt()
                val y: Int = (yRadius * sin(nextStep) - 0.5).roundToInt()
                nextStep += angleStep
                Position(xCenter + x, yCenter + y)
            }
    }

    private fun faceInwards(position: Position, width: Int, height: Int): Direction {
        val p = Position(position.x - width/2, position.y - height/2)

        return when {
            p.x < 0 && p.y < 0 -> Direction.NORTH
            p.x < 0 && p.y >= 0 -> Direction.WEST
            p.x >= 0 && p.y < 0 -> Direction.EAST
            p.x >= 0 && p.y >= 0 -> Direction.SOUTH
            else -> error("Cannot determine direction to face for position: $position")
        }
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
        val growth: Boolean = growSnakes()

        // Ask snake brains what moves they would like to do
        val moves: Map<Snake, Direction> = getDecisionsFromSnakes()

        // Update board and apply snake moves
        val (updatedBoard: Board, updatedSnakes: Set<Snake>) = updateBoardState(snakes, moves, board)

        // Add new fruits to board (when needed)
        val boardWithNewFruit: Board = perhapsSpawnFruit(updatedBoard)

        recordedGame.addBlocking(boardWithNewFruit)
        return Session(boardWithNewFruit, metadata, updatedSnakes, recordedGame, random, turn + 1)
    }

    fun cleanup() {
        snakes.forEach { it.removeBrain() }
    }

    private fun growSnakes(): Boolean = turn % metadata.growthFrequency == 0
    private fun spawnFruit(): Boolean = turn % metadata.fruitFrequency == 0

    private fun perhapsSpawnFruit(board: Board): Board {
        if(!spawnFruit()) {
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

    private fun parseMove(snake: Snake, decision: BrainDecision): Outcome {
        return try {
            val move: Direction = decision.demandNextMove()
            return if(snake.getCurrentDirection().isValidTurn(move)) {
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

    /**
     * Moves all the snakes by calling the <code>moveSnake</code> for each snake.
     *
     * @param	moves		Map of each snake to its desired movement.
     * @param	growSnakes	Whether or not snakes are supposed to grow this turn.
     */
    private fun moveAllSnakes(moves: Map<Snake, Direction>, growSnakes: Boolean): Set<Snake> =
        moves.map { moveSnake(it.key, it.value, growSnakes) }.toSet()

    /**
     * Moves a single snake in the specified direction and grows the snake if necessary.
     * Works by moving the position of the snake's head, and then also moving its tail
     * (unless growth is specified).
     *
     * @param	snake	The snake that is going to be moved.
     * @param	dir		The direction in which the snake is to be moved.
     * @param	grow	Whether or not the snake is supposed to grow this turn.
     */
    private fun moveSnake(snake: Snake, dir: Direction, grow: Boolean): Snake {
        return if(grow) {
            snake.moveHead(dir)
        } else {
            snake.moveHead(dir).removeTail()
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
    ): Pair<Board, Set<Snake>> {
        val eatenFruits: List<Position> = board.asSequence()
            .filter { it.second.hasSnakeEatingFruit() }
            .map { it.first }
            .toList()

        val movedSnakes: Set<Snake> = moveAllSnakes(moves, growSnakes())
        val updatedSnakeState: Set<Snake> = updateSnakeStates(movedSnakes, board)
        val allSnakes: Set<Snake> = (snakes + updatedSnakeState).asSequence().onlyLatestSnakes()
        val snakesWithPositions: Map<GameObject, List<Position>> = allSnakes.asSequence()
            .map { it to it.getSegments().toList() }
            .toMap()

        val updatedBoard: Board = board
            .removeAllSnakes()
            .removeAll(eatenFruits, Fruit)
            .addAll(snakesWithPositions)

        return updatedBoard to allSnakes
    }
}

private fun Sequence<Snake>.onlyLatestSnakes(): Set<Snake> = this
    .sortedByDescending { it.getLifespan() }
    .distinctBy { it.value() }
    .toSet()
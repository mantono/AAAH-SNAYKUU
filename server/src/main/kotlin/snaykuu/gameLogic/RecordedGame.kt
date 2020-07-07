package snaykuu.gameLogic

import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.swing.filechooser.FileNameExtensionFilter

class RecordedGame(
    private val metadata: Metadata,
    private val snakes: MutableSet<Snake>,
    private val frames: List<Board>
): Iterator<Board> by frames.iterator(), Game {
    private var turn: Int = -1

    constructor(metadata: Metadata, snakes: Collection<Snake>, frames: List<Board>):
        this(metadata, snakes.toMutableSet(), frames)

    override fun getMetadata(): Metadata = metadata

    override fun getGameResult(): GameResult = GameResult(snakes, this)

    override fun getCurrentState(): GameState {
        val i: Int = turn.coerceIn(0, frames.lastIndex)
        val currentBoard: Board = frames[i]
        val previousBoard: Board = frames.getOrNull(i - 1) ?: currentBoard
        val snakeMoves: Map<Position, Square> = previousBoard.changes(currentBoard).toMap()
        moveSnakes(snakeMoves)
        updateSnakeStates(currentBoard)
        return GameState(currentBoard, snakes, metadata, NotStarted)
    }

    private fun moveSnakes(changes: Map<Position, Square>) {
        snakes.forEach { applyChangesToSnake(it, changes) }
    }

    private fun applyChangesToSnake(snake: Snake, changes: Map<Position, Square>) {
        val head: Position = changes.asSequence()
            .filter { it.key !in snake.getSegments() }
            .firstOrNull { it.value.contains(snake) }
            ?.key ?: return

        snake.moveHead(head)

        val tailSquare: Square = changes[snake.getTailPosition()] ?: return
        if(snake !in tailSquare) {
            snake.removeTail()
        }
    }

    private fun updateSnakeStates(board: Board) {
        snakes.forEach { snake: Snake ->
            val head: Position = snake.getHeadPosition()
            val square: Square = board.getSquare(head)
            when {
                square.hasMultipleSnakes() -> snake.kill()
                square.hasWall() -> snake.kill()
                square.hasFruit() -> {
                    snake.addScore()
                    snake.increaseLifespan()
                }
                else -> snake.increaseLifespan()
            }
        }
    }

    override fun state(): State {
        return when(turn) {
            -1 -> State.NotStarted
            in 0..frames.lastIndex -> State.Playing
            else -> State.Finished
        }
    }

    override fun tick(): Game {
        if(state() != State.Finished) {
            turn++
        }
        return this
    }

    private data class CompactRepresentation(
        private val metadata: Metadata,
        private val snakes: Collection<String>,
        private val frames: List<Int>
    ) {
        fun asRecordedGame(): RecordedGame {
            val boards: List<Board> = frames.asSequence()
                .chunked(metadata.boardSize())
                .map { Board(metadata.boardWidth, metadata.boardHeight, it.toTypedArray()) }
                .toList()

            val recreatedSnakes: MutableSet<Snake> = Snake.create(snakes.sorted().map { it to null }.toMap())
            return RecordedGame(metadata, recreatedSnakes, boards)
        }

        companion object {
            fun fromRecordedGame(snapshot: RecordedGame): CompactRepresentation {
                val boardSize: Int = snapshot.metadata.boardWidth * snapshot.metadata.boardHeight
                val requiredCapacity: Int = boardSize * snapshot.frames.size
                val frames: MutableList<Int> = ArrayList(requiredCapacity)
                snapshot.asSequence()
                    .map { it.serialize() }
                    .flatten()
                    .forEach { square: Int -> frames.add(square) }

                val snakeNames = snapshot.snakes.map { it.getName() }
                return CompactRepresentation(snapshot.metadata, snakeNames, frames)
            }
        }
    }

    fun save(file: File = fileName()): Long {
        check(!file.exists()) { "Cannot save to file '$file': File already exists" }
        check(file.createNewFile()) { "Unable to create file: '$file" }
        check(file.canWrite()) { "Unable to write to file: '$file'" }

        val intermediateRepresentation = CompactRepresentation.fromRecordedGame(this)
        val serialized: String = DefaultMapper.writeValueAsString(intermediateRepresentation)
        val fileStream: BufferedOutputStream = FileOutputStream(file).buffered(BUFFER_SIZE)

        fileStream.use { stream ->
            stream.write(serialized.toByteArray())
        }

        return file.length()
    }

    companion object {
        private const val BUFFER_SIZE: Int = 128 * 128 * Int.SIZE_BYTES
        private const val SAVED_FILE_PREFIX = "snaykuu_"
        const val SAVED_FILE_SUFFIX = "sny"

        internal fun fileNameFilter(): FileNameExtensionFilter =
            FileNameExtensionFilter("Snaykuu Replay (.$SAVED_FILE_SUFFIX)", SAVED_FILE_SUFFIX)

        private fun fileName(): File {
            val timestamp = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yMd-kms")
            val date: String = formatter.format(timestamp)
            return File.createTempFile(SAVED_FILE_PREFIX + "_$date", SAVED_FILE_SUFFIX)
        }

        fun load(file: File): RecordedGame {
            check(file.exists()) { "Cannot load from file '$file': File does not exist" }
            check(file.canRead()) { "Cannot load from file: '$file': Unable to read file" }

            val fileStream: BufferedInputStream = FileInputStream(file).buffered(BUFFER_SIZE)
            val bytes = ByteArray(file.length().toInt())
            fileStream.use { stream ->
                stream.read(bytes)
            }

            val json = String(bytes, Charsets.UTF_8)
            val intermediateRepresentation: CompactRepresentation = DefaultMapper.readValue(json)
            return intermediateRepresentation.asRecordedGame()
        }
    }
}

internal object DefaultMapper: ObjectMapper() {
    init {
        registerKotlinModule()
    }
}
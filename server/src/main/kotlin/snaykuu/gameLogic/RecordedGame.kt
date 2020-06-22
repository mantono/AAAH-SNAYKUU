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

class RecordedGame(
    private val metadata: Metadata,
    private val snakes: Map<Int, String>,
    private val frames: List<Board>
): Iterator<Board> by frames.iterator() {

    private data class CompactRepresentation(
        private val metadata: Metadata,
        private val snakes: Map<Int, String>,
        private val frames: List<Int>
    ) {
        fun asRecordedGame(): RecordedGame {
            val boards: List<Board> = frames.asSequence()
                .chunked(metadata.boardSize())
                .map { Board(metadata.boardWidth, metadata.boardHeight, it.toTypedArray()) }
                .toList()

            return RecordedGame(metadata, snakes, boards)
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

                return CompactRepresentation(snapshot.metadata, snapshot.snakes, frames)
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
        private const val SAVED_FILE_SUFFIX = "sny"

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
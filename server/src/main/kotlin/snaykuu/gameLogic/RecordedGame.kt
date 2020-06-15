package snaykuu.gameLogic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.IntBuffer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicReference

class GameRecorder(
    private val metadata: Metadata
) {
    private var buffer: IntBuffer = IntBuffer.allocate(1)
    private val channel: Channel<Board> = Channel(100)
    private val started: Semaphore = Semaphore(1)

    fun start(scope: CoroutineScope = GlobalScope): Job? {
        return if(started.tryAcquire()) {
            scope.launch { consume(this) }
        } else {
            null
        }
    }

    private suspend fun consume(scope: CoroutineScope) {
        while(scope.coroutineContext.isActive) {
            val frame: Board = channel.receive()
            if(buffer.remaining() == 0) {
                val newSize: Int = buffer.capacity() * 2
                buffer = IntBuffer.wrap(buffer.array(), 0, newSize)
            }
            buffer.put(frame.serialize().toIntArray())
        }
    }

    suspend fun add(board: Board) {
        channel.send(board)
    }

    fun addBlocking(board: Board) {
        channel.sendBlocking(board)
    }

    fun save(file: File = fileName()): File {
        val bufferSize: Int = metadata.boardWidth * metadata.boardHeight * Int.SIZE_BYTES
        val fileStream: BufferedOutputStream = FileOutputStream(file).buffered(bufferSize)
        buffer.reset()
        fileStream.use { stream ->
            while(buffer.position() < buffer.limit()) {
                stream.write(buffer.get())
            }
        }
        return file
    }

    companion object {
        private const val SAVED_FILE_PREFIX = "snaykuu_"
        private const val SAVED_FILE_SUFFIX = "sny"

        private fun fileName(): File {
            val timestamp = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yMd-kms")
            val date: String = formatter.format(timestamp)
            return File.createTempFile(SAVED_FILE_PREFIX + "_$date", SAVED_FILE_SUFFIX)
        }

        private fun createBuffer(metadata: Metadata): IntBuffer {
            val frameSize: Int = metadata.boardWidth * metadata.boardHeight
            val estimatedTurns: Int = if(metadata.growthFrequency == 0) {
                2_000
            } else {
                200 * metadata.growthFrequency.coerceAtLeast(1)
            }
            val initialAllocation: Int = frameSize * estimatedTurns
            return IntBuffer.allocate(initialAllocation)
        }
    }
}
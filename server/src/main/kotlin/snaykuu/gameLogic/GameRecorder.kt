package snaykuu.gameLogic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.receiveOrNull
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
import java.util.*
import java.util.concurrent.atomic.AtomicReference

class GameRecorder(
    private val metadata: Metadata,
    private val snakes: Collection<Snake>
) {
    private var buffer: Deque<Board> = LinkedList()
    private val channel: Channel<Board> = Channel(100)
    private val started: Semaphore = Semaphore(1)
    private val coroutineJob: AtomicReference<Job> = AtomicReference()

    fun start(scope: CoroutineScope = GlobalScope): Job? {
        if(started.tryAcquire()) {
            val job: Job = scope.launch { consume(this) }
            coroutineJob.set(job)
        }
        return coroutineJob.get()
    }

    fun stop() {
        coroutineJob.get()?.cancel("GameRecorder stopped by normal Job cancellation")
    }

    fun isRecording(): Boolean = coroutineJob.get()?.isActive ?: false

    private suspend fun consume(scope: CoroutineScope) {
        while(scope.coroutineContext.isActive) {
            val frame: Board = channel.receive()
            buffer.push(frame)
        }
        channel.close()
    }

    suspend fun add(board: Board) {
        channel.send(board)
    }

    fun addBlocking(board: Board) {
        channel.sendBlocking(board)
    }

    fun save(): RecordedGame {
        val snakeNames: Map<Int, String> = snakes.map { it.value() to it.getName() }.toMap()
        return RecordedGame(metadata, snakeNames, buffer.toList())
    }
}
package snaykuu.gameLogic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.awt.Color
import java.lang.IllegalStateException

class GetLeaderBoardTest {
    private val templateSnake = Snake(0, "foo", BrainDead, color = Color.BLACK)

    @Test
    fun `verify correct order in leaderboard where all snakes has different lifespan and score`() {
        val snakes = listOf(
            templateSnake.copy(id = 1, name = "first", lifespan = 5, score = 5),
            templateSnake.copy(id = 3, name = "third", lifespan = 0, score = 0),
            templateSnake.copy(id = 2, name = "second", lifespan = 3, score = 3)
        )
        val leaderBoard = snakes.getLeaderboard()
        
        assertEquals(1, leaderBoard[1]!!.size)
        assertEquals("first", leaderBoard[1]!!.first().getName())

        assertEquals(1, leaderBoard[2]!!.size)
        assertEquals("second", leaderBoard[2]!!.first().getName())

        assertEquals(1, leaderBoard[3]!!.size)
        assertEquals("third", leaderBoard[3]!!.first().getName())
    }
}

private object BrainDead: Brain {
    override fun getNextMove(yourSnake: Snake, gameState: GameState): Nothing {
        throw IllegalStateException("This brain should not be called")
    }
}
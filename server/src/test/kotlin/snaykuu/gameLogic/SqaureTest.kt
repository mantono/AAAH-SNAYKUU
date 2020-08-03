package snaykuu.gameLogic

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SquareTest {
    @Test
    fun testMultipleSnakesPresentInSquare() {
        val square = Square(Snake(0), Snake(1))
        assertTrue(square.hasMultipleSnakes())
    }

    @Test
    fun testMultipleSnakesNotPresentInSquareWhenOnlyOneSnake() {
        val square = Square(Snake(0))
        assertFalse(square.hasMultipleSnakes())
    }

    @Test
    fun testMultipleSnakesNotPresentInSquareWhenSameSnakePresentMultipleTimes() {
        val square = Square(Snake(0), Snake(0))
        assertFalse(square.hasMultipleSnakes())
    }

    @Test
    fun testMultipleSnakesNotPresentInSquareWhenNoSnake() {
        val square = Square()
        assertFalse(square.hasMultipleSnakes())
    }
}
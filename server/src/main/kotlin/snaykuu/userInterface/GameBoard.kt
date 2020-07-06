package snaykuu.userInterface

import snaykuu.gameLogic.Direction
import snaykuu.gameLogic.Game
import snaykuu.gameLogic.Position
import snaykuu.gameLogic.Snake
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JComponent

class GameBoard(
    private val game: Game,
    pixelsPerUnit: Int
): JComponent() {
    private var pixelsPerXUnit: Int = pixelsPerUnit
    private var pixelsPerYUnit: Int = pixelsPerUnit
    private var boardWidth: Int = game.getMetadata().boardWidth
    private var boardHeight: Int = game.getMetadata().boardHeight
    private var graphicsWidth: Int = computeGraphics(boardWidth, pixelsPerXUnit)
    private var graphicsHeight: Int = computeGraphics(boardHeight, pixelsPerYUnit)

    /*
     * The grid needs space to be in that is not inside a game square.
     * Hence we add 1 pixel (for the first line), plus the number of squares
     * (for each subsequent line), and multiply the number of squares
     * by the size of each square.
     * The result is the total drawing area.
     */
    private fun computeGraphics(size: Int, pixelsPerUnit: Int): Int = 1 + size + (size * pixelsPerUnit)

    init {
        addComponentListener(CompLis())
        setSize(graphicsWidth, graphicsHeight)
        preferredSize = Dimension(graphicsWidth, graphicsHeight)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        val gs = game.getCurrentState()

        //Basic preparation.
        g.color = GameBoard.background
        g.fillRect(0, 0, graphicsWidth, graphicsHeight)

        g.color = GameBoard.grid

        var lineXpos = 0
        for(x in 0 until graphicsWidth)  //Vertical lines
        {
            g.drawLine(lineXpos, 0, lineXpos, graphicsHeight - 1)
            lineXpos += pixelsPerXUnit + 1
        }

        var lineYpos = 0
        for(y in 0 until graphicsHeight)  //Horizontal lines
        {
            g.drawLine(0, lineYpos, graphicsWidth - 1, lineYpos)
            lineYpos += pixelsPerYUnit + 1
        }

        //Image drawing.
        val g2d = g as Graphics2D

        for(wall in gs.walls) {
            val icon = GraphicsTile.WALL
            g2d.drawImage(icon.getImage(), icon.getTransformation(null, wall, pixelsPerXUnit, pixelsPerYUnit), null)
        }

        for(s in gs.snakes) {
            snakeTiles(s).forEach { (tile, direction, position) ->
                tile.getTransformation(direction, position, pixelsPerXUnit, pixelsPerYUnit)
            }
        }

        for(fruit in gs.fruits) {
            val icon = GraphicsTile.FRUIT
            g2d.drawImage(icon.getImage(), icon.getTransformation(null, fruit, pixelsPerXUnit, pixelsPerYUnit), null)
        }
    }

    private fun snakeTiles(snake: Snake): List<Triple<GraphicsTile, Direction, Position>> {
        val positions: List<Position> = snake.getDrawData().map { it.pos }.toList()
        val rawDirections: List<Direction> = snake.getDrawData().map { it.dir }.toList()
        return rawDirections.threeWayZip()
            .asSequence()
            .mapIndexed { i, (previous, current, next) ->
                val tile: GraphicsTile = resolveTile(previous, current, next, snake.isDead())
                val position: Position = positions[i]
                Triple(tile, current, position)
            }
            .toList()
    }

    private fun resolveTile(
        previous: Direction?,
        current: Direction,
        next: Direction?,
        isDead: Boolean
    ): GraphicsTile {
        val hasPrevious: Boolean = previous != null
        val hasNext: Boolean = next != null
        val isSingle: Boolean = !hasPrevious && !hasNext
        val isBody: Boolean = hasPrevious && hasNext

        return when {
            isSingle && !isDead -> GraphicsTile.SNAKEMONAD
            !hasPrevious && isDead -> GraphicsTile.SNAKEDEAD
            !hasPrevious && !isDead -> GraphicsTile.SNAKEHEAD
            !hasNext -> GraphicsTile.SNAKETAIL
            isBody && previous!!.turnLeft() == current -> GraphicsTile.SNAKERIGHT
            isBody && previous!!.turnRight() == current -> GraphicsTile.SNAKELEFT
            else -> GraphicsTile.SNAKEBODY
        }
    }

    companion object {
        private val background: Color = Color.WHITE
        private val wall: Color = Color.BLACK
        private val grid: Color = Color.GRAY
    }

    private inner class CompLis: ComponentAdapter() {
        override fun componentResized(e: ComponentEvent) {
            val size: Int = width.coerceAtMost(height)
            pixelsPerXUnit = (size - 1 - boardWidth) / boardWidth
            graphicsWidth = computeGraphics(boardWidth, pixelsPerXUnit)

            pixelsPerYUnit = (size - 1 - boardHeight) / boardHeight
            graphicsHeight = computeGraphics(boardHeight, pixelsPerYUnit)

            setSize(Dimension(graphicsWidth, graphicsHeight))
        }
    }
}

fun <T: Any> List<T>.threeWayZip(): List<Triple<T?, T, T?>> {
    return mapIndexed { index: Int, t: T ->
        Triple(
            first = getOrNull(index - 1),
            second = t,
            third = getOrNull(index + 1)
        )
    }
}
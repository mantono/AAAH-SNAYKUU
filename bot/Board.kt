package bot

import gameLogic.Board
import gameLogic.Position

fun Board.asSequence(): Sequence<Position>
{
	val xRange: IntRange = 0 .. (this.width - 1)
	val yRange: IntRange = 0 .. (this.height - 1)

	return xRange.map { x ->
		yRange.map { y ->
			Position(x, y)
		}
	}
			.flatten()
			.asSequence()
}
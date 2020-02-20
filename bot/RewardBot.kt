package bot

import gameLogic.Brain
import gameLogic.Direction
import gameLogic.GameState
import gameLogic.Position
import gameLogic.Snake
import java.util.*
import kotlin.collections.HashSet

class RewardBot : Brain
{
	private var maxDepth = 4

	init
	{
		System.out.println("\n\n*** BAM!! ***\n")
		System.out.println("*** Version ${this::class.java.hashCode()} ***\n")
	}

	override fun getNextMove(self: Snake, gameState: GameState): Direction
	{
		try
		{
			maxDepth = maxStackDepth(self.segments.size)
			val time = TimeTracker(gameState.metadata)
			val state: BoardState = BoardState(gameState, self)
			val graph = Graph(state)
			val matrix = Matrix<Byte>(gameState.board)
			populate(matrix, state)

			val fruits = rankFruits(state, self.headPosition)
			val firstDirectionToEvaluate: Direction = initialDirection(graph, fruits, state, self)

			val (computedPath: Deque<Position>, score: Int) = createPath(firstDirectionToEvaluate, matrix, self, time)
			return Direction.getDirectionFromPositionToPosition(self.headPosition, computedPath.first)
		}
		catch(e: Exception)
		{
			e.printStackTrace()
		}

		return self.currentDirection.turnLeft()
	}

	private fun createPath(
			direction: Direction,
			matrix: Matrix<Byte>,
			self: Snake,
			time: TimeTracker,
			path: Deque<Position> = LinkedList(),
			score: Int = 0,
			visited: MutableSet<Position> = HashSet(64)
	): Pair<Deque<Position>, Int>
	{
		if(path.size >= maxDepth || time.remaining() <= 10)
		{
			return path to score
		}

		val current: Position = path.peekLast() ?: self.headPosition
		val next: Position = current going direction

		if(next in visited)
		{
			return path to score
		}

		path.push(next)
		val newScore: Int = score + matrix[next]

		return sequenceOf(direction, direction.turnLeft(), direction.turnRight(), direction.opposite())
				.map { createPath(it, matrix, self, time, path, newScore, visited) }
				.sortedByDescending { it.second }
				.first()
				.also { visited.remove(current) }
//		val (score: Byte, bestPos: Position) = sequenceOf(direction, direction.turnLeft(), direction.turnRight(), direction.opposite())
//				.map { current going it }
//				.map { matrix[it] to it }
//				.sortedByDescending { it.first }
//				.first()

	}

	private fun bestDirection(initialDirection: Direction, currentDirection: Direction, current: Position, state: BoardState, time: TimeTracker): Direction
	{
		val directions: Queue<Direction> = listOfDirections(initialDirection, currentDirection)
		val scores = TreeMap<Int, Direction>()
		val visited: MutableSet<Position> = HashSet(maxDepth / 4)

		while(directions.isNotEmpty())
		{
			val nextDirection: Direction = directions.poll()
			val nextPosition: Position = current going nextDirection
			val score = getScore(visited, nextPosition, state, time)
			scores.put(score, nextDirection)
		}

		val highScore: Int = scores.lastKey()
		val bestDirection: Direction = scores[highScore] ?: initialDirection
		println("${this::class.java.canonicalName}: $bestDirection ($highScore)")

		return bestDirection
	}

	/**
	 * Create a list of all Direction values, where the initialDirection is first in the list.
	 */
	private fun listOfDirections(initialDirection: Direction, currentDirection: Direction): Queue<Direction>
	{
		val q: Queue<Direction> = LinkedList()
		val complement = Direction.values()
				.filter { it != initialDirection }
				.filter { it != currentDirection }

		q.add(initialDirection)
		q.addAll(complement)

		return q
	}

	private fun getScore(visited: MutableSet<Position>, current: Position, state: BoardState, time: TimeTracker, score: Int = 0): Int
	{
		if(current in visited)
			return score

		if(reachedComputationCapacity(time, visited.size))
			return score

		if(state.isLethal(current))
			return score - 10

		visited.add(current)

		val scoreForThis: Byte = state.score(current)
		val accumulatedScore: Int = score + scoreForThis

		val highScore: Int = Direction.values()
				.asSequence()
				.map { current going it }
				.map { getScore(visited, it, state, time, accumulatedScore) }
				.max() ?: accumulatedScore

		visited.remove(current)

		return highScore
	}

	private fun reachedComputationCapacity(time: TimeTracker, pathSize: Int): Boolean
	{
		if(time.remaining() < 15)
			return true

		if(pathSize >= maxDepth)
			return true

		return false
	}
}

private fun maxStackDepth(snakeSize: Int): Int = (4 + (snakeSize * 2)).coerceAtLeast(16)
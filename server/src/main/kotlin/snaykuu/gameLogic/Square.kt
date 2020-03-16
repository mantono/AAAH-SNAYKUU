package snaykuu.gameLogic

/**
 * Objects of this class represent a single square of the game board. Each square has a List<GameObject>
 * of all the GameObjects it contains, but it will usually only contain 0 or 1 GameObjects. The only time
 * it can contain more than one game object is when a snake collides, with either a wall or another
 * snake.
 *
 * @author	Sixten Hilborn
 * @author	Arian Jafari
 */

data class Square(private val value: Int = 0) {
    constructor(vararg values: GameObject): this(values.asIterable().sum())

    fun isEmpty(): Boolean = value == 0
    fun hasFruit(): Boolean = Fruit in this
    fun hasWall(): Boolean = Wall in this
    fun hasSnake(): Boolean = value shr 2 != 0
    fun hasMultipleSnakes(): Boolean = snakeValues().size > 1
    fun isLethal(): Boolean = value shr 1 != 0
    fun eatFruit(): Int = value and Fruit.value()

    internal fun Square.snakeValues(): List<Int> {
        val snakeBitIdRange: IntRange = 3..32
        return snakeBitIdRange.asSequence()
            .map { 1 shl it }
            .map { value and it }
            .filter { it != 0 }
            .toList()
    }

    /**
     * Generates a list of all snakes in this square. Usually there is zero or one
     * snake in the same square, but there can be more than one snake in the
     * same square if one snake collides with another. It is therefore guaranteed
     * that there cannot be more than one living snake in the same square.
     *
     * @return	An ArrayList containing the snakes in this square, might be empty.
     */
    fun getSnakes(state: GameState): List<Snake> {
        val snakeValues: List<Int> = snakeValues()
        return state.snakes.filter { it.value() in snakeValues }
    }

    operator fun Square.contains(obj: GameObject): Boolean = (value and obj.value()) != 0

    operator fun plus(obj: GameObject): Square = Square(value or obj.value())
    operator fun minus(obj: GameObject): Square = Square(value and obj.value().inv())

    fun toInt(): Int = value
}

fun Iterable<GameObject>.sum(): Int = this.fold(0) { acc: Int, gameObject: GameObject ->
    acc or gameObject.value()
}

fun Iterable<Square>.resolve(): Int = this.fold(0) { x, y -> x or y.toInt() }
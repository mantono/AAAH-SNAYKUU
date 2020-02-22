package snaykuu.gameLogic

import java.util.*

/**
 * Objects of this class represent a single square of the game board. Each square has a List<GameObject>
 * of all the GameObjects it contains, but it will usually only contain 0 or 1 GameObjects. The only time
 * it can contain more than one game object is when a snake collides, with either a wall or another
 * snake.
 *
 * @author	Sixten Hilborn
 * @author	Arian Jafari
 */

typealias Square = Int

fun Square.isEmpty(): Boolean = this == 0
fun Square.hasObjectType(typeName: String): Boolean {
    val gameObject = GameObject.values().firstOrNull { it.name == typeName } ?: return false
    return gameObject in this
}
fun Square.hasFruit(): Boolean = GameObject.Fruit in this
fun Square.hasWall(): Boolean = GameObject.Wall in this
fun Square.hasSnake(): Boolean = (this - GameObject.Fruit) - GameObject.Wall != 0
fun Square.isLethal(): Boolean = this - Fruit != 0

/**
 * Generates a list of all snakes in this square. Usually there is zero or one
 * snake in the same square, but there can be more than one snake in the
 * same square if one snake collides with another. It is therefore guaranteed
 * that there cannot be more than one living snake in the same square.
 *
 * @return	An ArrayList containing the snakes in this square, might be empty.
 */
fun Square.getSnakes(): List<Snake> {
    val snakeBitIdRange: IntRange = 3..32
    val snakeIds: List<Int> = snakeBitIdRange.asSequence()
        .map { 1 shl it }
        .map { this and it }
        .filter { it != 0 }
        .toList()
}

operator fun Square.contains(obj: GameObject): Boolean = (this and obj.value()) != 0

fun Iterable<GameObject>.sum(): Int = this.fold(0) { acc: Int, gameObject: GameObject ->
    acc or gameObject.value()
}

fun Iterable<Square>.resolve(): Int = this.reduce { x, y -> x or y }

operator fun Square.plus(obj: GameObject): Square = this or obj.value()
operator fun Square.minus(obj: GameObject): Square = this and obj.value().inv()

data class SquareOld(private val objects: MutableList<GameObject> = LinkedList()) {
    fun isEmpty(): Boolean = objects.isEmpty()
    fun hasObjectType(typeName: String): Boolean = objects.any { it.getTypeName() == typeName }
    fun hasFruit(): Boolean = objects.any { it is Fruit }
    fun hasSnake(): Boolean = objects.any { it is Snake }
    fun hasWall(): Boolean = objects.any { it is Wall }
    fun isLethal(): Boolean = objects.any { it.isLethal() }

    /**
     * Generates a list of all snakes in this square. Usually there is zero or one
     * snake in the same square, but there can be more than one snake in the
     * same square if one snake collides with another. It is therefore guaranteed
     * that there cannot be more than one living snake in the same square.
     *
     * @return	An ArrayList containing the snakes in this square, might be empty.
     */
    fun getSnakes(): List<Snake> = objects.asSequence()
        .filter { it is Snake }
        .map { it as Snake }
        .toList()


    internal fun eatFruit(): Int {
        val fruits: List<Fruit> = removeFruit()
        return fruits.sumBy { it.getValue() }
    }

    internal fun removeFruit(): List<Fruit> {
        val fruits: List<Fruit> = objects.filterIsInstance<Fruit>()
        objects.removeAll(fruits)
        return fruits
    }

    internal fun addGameObject(newObject: GameObject) {
        objects.add(newObject)
    }

    internal fun removeGameObject(obj: GameObject) {
        objects.remove(obj)
    }

    fun clear() {
        objects.clear()
    }
}
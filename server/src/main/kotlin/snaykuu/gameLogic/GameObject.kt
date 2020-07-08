package snaykuu.gameLogic

/**
 * This enum represents an object that exists on the game board, describing which
 * kind of object it is (e.g. Fruit, Wall etc).
 *
 * @author	Sixten Hilborn
 * @author	Arian Jafari
 * @see     snaykuu.gameLogic.GameObjectType
 */
interface GameObject {
    fun value(): Int
    fun isLethal(): Boolean
    fun getTypeName(): String
}
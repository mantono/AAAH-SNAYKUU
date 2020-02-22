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

//enum class GameObject {
//    Fruit,
//    Wall,
//    Snake0,
//    Snake1,
//    Snake2,
//    Snake3,
//    Snake4,
//    Snake5,
//    Snake6,
//    Snake7,
//    Snake8,
//    Snake9,
//    Snake10,
//    Snake11,
//    Snake12,
//    Snake13;
//
//    fun value(): Int = 1 shl this.ordinal
//
//    /**
//     * Gets the number of points objects of this type are worth. The default value
//     * for fruit is 1.
//     *
//     * @return	the number of points that are granted by "eating" objects of
//     *			this GameObject.
//     */
//    fun points(): Int = value() % 2
//    fun isLethal(): Boolean = points() == 0
//    fun getTypeName(): String = this.name
//
//    companion object {
//        /**
//         * Given a square with many objects, return true if *any* GameObject is
//         * lethal, else false
//         */
//        fun isLethal(objects: Int): Boolean = objects - Fruit != 0
//
//        fun hasSnake(objects: Int): Boolean = (objects - Fruit) - Wall != 0
//    }
//}
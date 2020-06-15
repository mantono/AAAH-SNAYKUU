package snaykuu.gameLogic

/**
 * This class contains all the metadata concerning the current game session.
 * such as the size of the map, the thinking time for the snakes, the frequency
 * at which snakes grow, the frequency at which fruit spawns and the number
 * of fruits snakes need to eat to win.
 *
 * @author	Sixten Hilborn
 * @author	Arian Jafari
 */
data class Metadata(
    /**
     * Gets the width of the game board.
     *
     * @return	The width of the board.
     */
    val boardWidth: Int,

    /**
     * Gets the height of the game board.
     *
     * @return	The height of the board.
     */
    val boardHeight: Int,

    /**
     * Gets the thinking time each snake has each turn, in milliseconds.
     *
     * @return	The thinking time in milliseconds.
     */
    val maximumThinkingTime: Int,

    /**
     * Gets the number of turns it takes for snakes to grow. Note that this is
     * not the same thing as the number of turns that remain until the next
     * time snakes grow.
     *
     * @return	The frequency (in turns) with which snakes grow.
     */
    val growthFrequency: Int,

    /**
     * Gets the number of turns it takes for fruit to spawn. Note that this is
     * not the same thing as the number of turns that remain until the next
     * time snakes grow.
     *
     * @return	The frequency (in turns) with which snakes grow.
     */
    val fruitFrequency: Int,

    /**
     * Gets the number of total fruit required to win the game.
     *
     * @return	The number of fruit required to win the game.
     */
    val fruitGoal: Int
)
package snaykuu.gameLogic

// TODO change to internal when possible
object SnakeComparator: Comparator<Snake> {
    override fun compare(first: Snake, second: Snake): Int =
        internalScore(first).compareTo(internalScore(second))
//        val comparedLifespan: Int = first.getLifespan() - second.getLifespan()
//        return if(comparedLifespan != 0) {
//            comparedLifespan
//        } else {
//            first.getScore() - second.getScore()
//        }
//    }

    private fun internalScore(snake: Snake): Long =
        (snake.getLifespan().toLong() shl 31) or snake.getScore().toLong()
}
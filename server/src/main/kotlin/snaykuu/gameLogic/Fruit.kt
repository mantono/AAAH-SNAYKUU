package snaykuu.gameLogic

object Fruit: GameObject {
    override fun getTypeName(): String = "Fruit"
    override fun value(): Int = 1
    override fun isLethal(): Boolean = false
}
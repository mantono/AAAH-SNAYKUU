package snaykuu.gameLogic

object Wall: GameObject {
    override fun getTypeName(): String = "Wall"
    override fun value(): Int = 2
    override fun isLethal(): Boolean = true
}
package snaykuu.gameLogic

data class Color(
    val hue: Float = 0f,
    val saturation: Float = 1.0f,
    val value: Float = 1.0f
) {
    init {
        require(hue in 0f..360f)
        require(saturation in 0f..1.0f)
        require(value in 0f..1.0f)
    }

    fun increaseSaturation(change: Float = STEP_SIZE): Color =
        this.copy(saturation = saturation.plus(change).coerceIn(0f..1.0f))

    fun decreaseSaturation(change: Float = STEP_SIZE): Color =
        this.copy(saturation = saturation.minus(change).coerceIn(0f..1.0f))

    fun increaseValue(change: Float = STEP_SIZE): Color =
        this.copy(value = value.plus(change).coerceIn(0f..1.0f))

    fun decreaseValue(change: Float = STEP_SIZE): Color =
        this.copy(value = value.minus(change).coerceIn(0f..1.0f))

    fun adjustHue(hue: Float): Color {
        val newHue: Float = (this.hue + hue) % 360f
        return this.copy(hue = newHue)
    }

    fun setHue(hue: Float): Color = this.copy(hue = hue % 360f)

    fun asAWTColor(): java.awt.Color = java.awt.Color.getHSBColor(hue/360f, saturation, value)

    companion object {
        private const val STEP_SIZE: Float = 0.05f
    }
}

fun distributedColors(colors: Int, generated: List<Color> = listOf(Color())): List<Color> {
    if(colors == generated.size) {
        return generated
    }
    val stepSize: Float = 360f / colors
    val nextColor: Color = generated.last().adjustHue(stepSize)
    return distributedColors(colors, generated + nextColor)
}
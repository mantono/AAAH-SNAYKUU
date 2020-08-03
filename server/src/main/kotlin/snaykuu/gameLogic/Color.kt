package snaykuu.gameLogic

import kotlin.math.roundToInt

data class Color(
    val red: Short = 0,
    val green: Short = 0,
    val blue: Short = 0
) {
    init {
        require(red in 0..255) { "Invalid value for red: $red" }
        require(green in 0..255) { "Invalid value for green: $green" }
        require(blue in 0..255) { "Invalid value for blue: $blue" }
    }

    constructor(red: Int, green: Int, blue: Int):
        this((red % 256).toShort(), (green % 256).toShort(), (blue % 256).toShort())

    fun changeHue(degrees: Float): Color {
        val r: Float = red / 255f
        val g: Float = green / 255f
        val b: Float = blue / 255f

        val min: Float = minOf(r, g, b)
        val max: Float = maxOf(r, g, b)

        val luminance: Float = (min + max) / 2
        val saturation: Float = if(luminance < 0.5f) {
            (max - min) / (max - min)
        } else {
            (max - min) / (2.0f - max - min)
        }

        val delta: Float = max - min
        val hue: Float = when(max) {
            r -> (g - b) / delta
            g -> 2.0f + (b - r) / delta
            b -> 4.0f + (r - g) / delta
            else -> error("$red, $green, $blue")
        }

        val hueDegrees: Float = ((hue * 60f) + 360f) % 360f
        val newHue: Float = (hueDegrees + degrees) % 360f

        val temp1: Float = if(luminance < 0.5f) {
            luminance * (1.0f + saturation)
        } else {
            luminance + saturation - (luminance * saturation)
        }

        val temp2: Float = 2f * luminance - temp1
        val hueFraction: Float = newHue / 360f

        val tempR = (hueFraction + 0.333f) % 1f
        val tempG = hueFraction
        val tempB = (hueFraction - 0.333f + 1f) % 1f

        val channels: List<Int> = sequenceOf(tempR, tempG, tempB).map { v: Float ->
            when {
                6f * v < 1f -> temp2 + (temp1 - temp2) * 6f * v
                2f * v < 1f -> temp1
                3f * v < 2f -> temp2 + (temp1 - temp2) * (0.666f - v) * 6f
                else -> temp2
            }
        }
            .map { (it * 255f).roundToInt().coerceIn(0..255) }
            .toList()

        return Color(channels[0], channels[1], channels[2])
    }

    fun asAWTColor(): java.awt.Color =
        java.awt.Color(red.toInt(), green.toInt(), blue.toInt())
}
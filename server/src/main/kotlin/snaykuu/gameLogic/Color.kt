package snaykuu.gameLogic

import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.random.Random

data class Color(
    val red: Int = 0,
    val green: Int = 0,
    val blue: Int = 0
) {
    init {
        require(red in 0..255) { "Invalid value for red: $red" }
        require(green in 0..255) { "Invalid value for green: $green" }
        require(blue in 0..255) { "Invalid value for blue: $blue" }
    }

    private fun rgb(): Triple<Float, Float, Float> {
        val r: Float = red / 255f
        val g: Float = green / 255f
        val b: Float = blue / 255f
        return Triple(r, g, b)
    }

    private fun minMax(): Pair<Float, Float> {
        val (r, g, b) = rgb()
        val min: Float = minOf(r, g, b)
        val max: Float = maxOf(r, g, b)
        return min to max
    }

    fun luminance(): Float {
        val (min: Float, max: Float) = minMax()
        return (min + max) / 2
    }

    fun saturation(): Float {
        val (min: Float, max: Float) = minMax()
        val luminance: Float = (min + max) / 2

        return if(luminance < 0.5f) {
            (max - min) / (max - min)
        } else {
            (max - min) / (2.0f - max - min)
        }
    }

    fun increaseSaturation(): Color {
        return when {
            blue == red && red == green -> {
                when(this.hashCode().absoluteValue % 3) {
                    0 -> copy(red = red + 1)
                    1 -> copy(green = green + 1)
                    2 -> copy(blue = blue + 1)
                    else -> error("This should never happen")
                }
            }
            blue in red..green -> copy(red = red - 1, green = green + 1)
            blue in green..red -> copy(green = green - 1, red = red + 1)
            green in blue..red -> copy(blue = blue - 1, red = red + 1)
            green in red..blue -> copy(red = red - 1, blue = blue + 1)
            red in blue..green -> copy(blue = blue - 1, green = green + 1)
            red in green..blue -> copy(green = green - 1, blue = blue + 1)
            else -> error("This should never happen")
        }
    }

    fun hue(): Float {
        val (r: Float, g: Float, b: Float) = rgb()
        val (min: Float, max: Float) = minMax()
        val delta: Float = max - min

        return when(max) {
            r -> (g - b) / delta
            g -> 2.0f + (b - r) / delta
            b -> 4.0f + (r - g) / delta
            else -> error("$red, $green, $blue")
        }
    }

    fun asAWTColor(): java.awt.Color = java.awt.Color(red, green, blue)

    companion object {
        val RED = Color(255, 0, 0)
        val GREEN = Color(0, 255, 0)
        val BLUE = Color(0, 0, 255)
        val YELLOW = Color(255, 255, 0)
    }
}

fun distributedColors(colors: Int, seed: Long): List<Color> {
    //return createColors(colors)
    return colorGenerator(colors, seed)
}

private tailrec fun createColors(
    limit: Int,
    colors: List<Color> = emptyList()// listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)
): List<Color> {
    if(colors.size == limit) {
        return colors
    }
    val stepSize: Int = 255 / (limit - 4).coerceAtLeast(1)
    val r: Int = findNextIndex(colors.map { it.red }, stepSize)
    val g: Int = findNextIndex(colors.map { it.green }, stepSize)
    val b: Int = findNextIndex(colors.map { it.blue }, stepSize)
    val color: Color = ensureMinSaturation(Color(r, g, b), 0.2f)
    return createColors(limit, colors + color)
}

private tailrec fun ensureMinSaturation(color: Color, minSaturation: Float): Color {
    return if(color.saturation() >= minSaturation) {
        color
    } else {
        ensureMinSaturation(color.increaseSaturation(), minSaturation)
    }
}

fun colorGenerator(colors: Int, seed: Long = colors.toLong()): List<Color> {
    val random = Random(seed)
    val stepSize: Int = 255 / (colors - 1).coerceAtLeast(1)
    val boundaries: IntProgression = 0..255 step stepSize
    val red = boundaries.toList().shuffled(random)
    val green = red.reversed()
    val blue = red.zip(green).asSequence().map { (red, green) ->
        boundaries.first { it != red && it != green }
    }.toList()
    var i: Int = 0

    return generateSequence { Color(red[i], green[i], blue[i++]) }
        .take(colors)
        .onEach { println("${it.red}|${it.green}|${it.blue}") }
        .toList()
}

fun findNextIndex(
    usedIndices: List<Int>,
    multiplier: Int,
    upperThreshold: Int = 255
): Int {
    val boundaries: IntProgression = 0..upperThreshold step multiplier
    return boundaries.asSequence()
        .filterNot { it in usedIndices }
        .toList()
        .random()
}

fun IntRange.middle(): Int {
    val diff: Int = ((last - first) / 2.0).roundToInt()
    return first + diff
}
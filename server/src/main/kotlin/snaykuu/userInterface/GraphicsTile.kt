package snaykuu.userInterface

import snaykuu.gameLogic.Direction
import snaykuu.gameLogic.Position
import java.awt.Color
import java.awt.Image
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO

enum class GraphicsTile(private val path: String) {
    SNAKEHEAD("snake_head.png"),
    SNAKETAIL("snake_tail.png"),
    SNAKEBODY("snake_body.png"),
    SNAKELEFT("snake_left.png"),
    SNAKERIGHT("snake_right.png"),
    SNAKEMONAD("snake_monad.png"),
    SNAKEDEAD("snake_dead.png"),
    FRUIT("fruit.png"),
    WALL("wall.png");

    private val image: Image
    private val imgWidth: Int
    private val imgHeight: Int

    init {
        image = loadImage(this.path)
        imgWidth = image.getWidth(null)
        imgHeight = image.getHeight(null)
        require(imgWidth > 0) { "Width must be greater than 0" }
        require(imgHeight > 0) { "Height must be greater than 0" }
    }

    private fun loadImage(path: String): Image {
        val temp: URL = javaClass.getResource("img/$path")
        return ImageIO.read(temp)
    }

    open fun getImage(c: Color): Image? {
        val outImage = BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB)
        val g = outImage.createGraphics()
        g.drawImage(image, 0, 0, null)
        g.dispose()
        val mask = outImage.getRGB(imgWidth / 2, imgHeight / 2)
        val replacement = c.rgb
        for(i in 0 until outImage.width) {
            for(j in 0 until outImage.height) {
                if(outImage.getRGB(i, j) == mask) {
                    outImage.setRGB(i, j, replacement)
                }
            }
        }
        return outImage
    }

    open fun getImage(): Image = image

    open fun getTransformation(
        dir: Direction?,
        pos: Position,
        pixelsPerXUnit: Int,
        pixelsPerYUnit: Int
    ): AffineTransform? {

        //~ See the java6 API spec for AffineTransform for details on syntax.
        val flatmatrix = floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f, pos.x.toFloat(), pos.y.toFloat())
        val translationCorrector = floatArrayOf(0f, 0f)
        val scaleAdjuster = floatArrayOf(0f, 0f)

        //~ Rotate around the image's own center and set a
        //  few variables for later fine-tuning.
        if(dir != null) {
            when(dir) {
                Direction.NORTH -> {
                    flatmatrix[1] += -1f
                    flatmatrix[2] += 1f
                    flatmatrix[5] += 1f
                    scaleAdjuster[1] = 1f
                }
                Direction.WEST -> {
                    flatmatrix[0] += -1f
                    flatmatrix[3] += -1f
                    flatmatrix[4] += 1f
                    flatmatrix[5] += 1f
                    translationCorrector[1] = -1f
                    scaleAdjuster[0] = 1f
                }
                Direction.SOUTH -> {
                    flatmatrix[1] += 1f
                    flatmatrix[2] += -1f
                    flatmatrix[4] += 1f
                    translationCorrector[0] = -1f
                    translationCorrector[1] = -1f
                    scaleAdjuster[1] = 1f
                }
                else -> {
                    flatmatrix[0] += 1f
                    flatmatrix[3] += 1f
                    translationCorrector[0] = -1f
                    scaleAdjuster[0] = 1f
                }
            }
        } else {
            flatmatrix[0] += 1f
            flatmatrix[3] += 1f
        }

        //~ Scale the image according to current window size.
        for(i in 0..3) {
            if(i % 2 == 0) {
                flatmatrix[i] = flatmatrix[i] * ((pixelsPerXUnit + scaleAdjuster[0]) / imgWidth.toFloat())
            } else {
                flatmatrix[i] = flatmatrix[i] * ((pixelsPerYUnit + scaleAdjuster[1]) / imgHeight.toFloat())
            }
        }

        //~ Translate to the correct point.
        flatmatrix[4] = 1 + flatmatrix[4] + flatmatrix[4] * pixelsPerXUnit
        flatmatrix[5] = 1 + flatmatrix[5] + flatmatrix[5] * pixelsPerYUnit

        //~ Adjust for rotational positioning artifacts.
        flatmatrix[4] += translationCorrector[0]
        flatmatrix[5] += translationCorrector[1]
        return AffineTransform(flatmatrix)
    }
}
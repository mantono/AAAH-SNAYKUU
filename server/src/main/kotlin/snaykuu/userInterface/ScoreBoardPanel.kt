package snaykuu.userInterface

import snaykuu.gameLogic.Game
import snaykuu.gameLogic.GameResult
import snaykuu.gameLogic.Snake
import java.awt.Color
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JLabel
import javax.swing.JPanel

class ScoreBoardPanel(game: Game): JPanel() {
    private val gbl: GridBagLayout = GridBagLayout()
    private val c = GridBagConstraints()
    private val insets: Insets = Insets(2, 4, 2, 4)

    init {
        this.layout = gbl
        updateScore(game.getGameResult())
    }

    override fun getInsets(): Insets = this.insets

    fun updateScore(gameResult: GameResult) {
        this.removeAll()

        var placedSnakes: Int = 0
        printLegend(placedSnakes++)

        gameResult.getWinners().forEach { (index: Int, snakes: List<Snake>) ->
            snakes.forEach { snake: Snake ->
                placeRow(placedSnakes++, index + 1, snake)
            }
        }

        preferredSize = this.preferredSize
        validate()
    }

    private fun placeRow(gridy: Int, p: Int, s: Snake) {
        c.anchor = GridBagConstraints.NORTHWEST
        c.fill = GridBagConstraints.HORIZONTAL
        c.weighty = 0.0
        c.gridy = gridy
        c.gridheight = 1
        c.gridwidth = 1
        c.insets = insets

        c.gridx = 0
        c.weightx = 0.0
        val place = JLabel("" + p)
        place.preferredSize = place.preferredSize
        gbl.setConstraints(place, c)
        add(place)

        c.gridx = 1
        c.weightx = 0.0
        val color = JLabel("   ")
        color.preferredSize = color.preferredSize
        color.isOpaque = true
        color.background = s.getColor().asAWTColor()
        gbl.setConstraints(color, c)
        add(color)

        c.gridx = 2
        c.weightx = 10.0
        val snake = JLabel(s.getName())

        if(s.isDead()) {
            snake.foreground = Color(0xD00000)
        }

        snake.preferredSize = snake.preferredSize
        gbl.setConstraints(snake, c)
        add(snake)

        c.gridx = 3
        c.weightx = 0.0
        val score = JLabel("" + s.getScore())
        score.preferredSize = score.preferredSize
        gbl.setConstraints(score, c)
        add(score)

        c.gridx = 4
        c.weightx = 0.0
        val age = JLabel("" + s.getLifespan())
        age.preferredSize = age.preferredSize
        gbl.setConstraints(age, c)
        add(age)
    }

    private fun printLegend(gridy: Int) {
        c.anchor = GridBagConstraints.NORTHWEST
        c.fill = GridBagConstraints.HORIZONTAL
        c.weighty = 0.0
        c.gridy = gridy
        c.gridheight = 1
        c.gridwidth = 1
        c.insets = insets

        c.gridx = 0
        c.weightx = 0.0
        val place = JLabel("Place")
        place.preferredSize = place.preferredSize
        gbl.setConstraints(place, c)
        add(place)

        c.gridx = 1
        c.weightx = 0.0
        val color = JLabel("Color")
        color.preferredSize = color.preferredSize
        gbl.setConstraints(color, c)
        add(color)

        c.gridx = 2
        c.weightx = 10.0
        val snake = JLabel("Name")
        snake.preferredSize = snake.preferredSize
        gbl.setConstraints(snake, c)
        add(snake)

        c.gridx = 3
        c.weightx = 0.0
        val score = JLabel("Score")
        score.preferredSize = score.preferredSize
        gbl.setConstraints(score, c)
        add(score)

        c.gridx = 4
        c.weightx = 0.0
        val age = JLabel("Age")
        age.preferredSize = age.preferredSize
        gbl.setConstraints(age, c)
        add(age)
    }
}
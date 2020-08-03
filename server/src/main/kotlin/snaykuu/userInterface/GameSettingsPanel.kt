package snaykuu.userInterface

import snaykuu.gameLogic.GameSettings
import snaykuu.gameLogic.Metadata
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.SwingConstants

class GameSettingsPanel(private val gameSettings: GameSettings): JPanel() {
    private val gridbag: GridBagLayout = GridBagLayout()
    private val fieldC: GridBagConstraints = GridBagConstraints()
    private val labelC: GridBagConstraints = GridBagConstraints()

    private val boardWidth: JTextField = addRow("Board width", gameSettings.boardWidth - 2)
    private val boardHeight: JTextField = addRow("Board height", gameSettings.boardHeight - 2)
    private val pixelsPerUnit: JTextField = addRow("Pixels per square", gameSettings.pixelsPerSquare)
    private val fruitFrequency: JTextField = addRow("Ticks between fruits", gameSettings.fruitFrequency)
    private val growthFrequency: JTextField = addRow("Ticks per unit of snayk growth", gameSettings.growthFrequency)
    private val fruitGoal: JTextField = addRow("Fruits to win", gameSettings.fruitGoal)
    private val thinkingTime: JTextField = addRow("Thinking time (ms/frame)", gameSettings.maximumThinkingTime)
    private val gameSpeed: JTextField = addRow("Game speed (ms/frame)", gameSettings.gameSpeed)

    init {
        layout = gridbag

        labelC.apply {
            fill = GridBagConstraints.HORIZONTAL
            insets = Insets(4, 4, 4, 4)
            gridwidth = 1
            gridheight = 1
            gridx = 0
            gridy = 0
            weightx = 0.0
            weighty = 0.0
        }

        fieldC.apply {
            fill = GridBagConstraints.NONE
            insets = Insets(4, 4, 4, 4)
            gridwidth = 1
            gridheight = 1
            gridx = 1
            gridy = 0
            weightx = 0.0
            weighty = 0.0
        }
    }

    private fun addRow(text: String, value: Any): JTextField {
        val label = JLabel(text)
        label.horizontalAlignment = SwingConstants.RIGHT

        gridbag.setConstraints(label, labelC)
        add(label)

        val field = JTextField(value.toString())
        field.columns = 4

        gridbag.setConstraints(field, fieldC)
        add(field)

        labelC.gridy++
        fieldC.gridy++

        return field
    }

    fun generateMetadata(): Metadata = gameSettings
    fun getGameSpeed(): Int = gameSettings.gameSpeed
    fun getPixelsPerUnit(): Int = gameSettings.pixelsPerSquare
}
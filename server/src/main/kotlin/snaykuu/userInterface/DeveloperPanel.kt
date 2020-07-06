package snaykuu.userInterface

import snaykuu.gameLogic.Session
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.JTextField

class DeveloperPanel(
    private val settingsWindow: SettingsWindow
): JPanel() {
    private val statsButton: JButton = JButton("Run test games")
    private val numberOfRuns: JTextField = JTextField("50")
    private val output: JTextArea = JTextArea()

    init {
        val gridbag = GridBagLayout()
        layout = gridbag

        val constraint = GridBagConstraints()

        constraint.fill = GridBagConstraints.NONE
        constraint.insets = Insets(4, 4, 4, 4)
        constraint.gridwidth = 1
        constraint.gridheight = 1
        constraint.gridx = 0
        constraint.gridy = 0
        constraint.weightx = 0.0
        constraint.weighty = 0.0

        val runLabel = JLabel("Number of runs:")
        gridbag.setConstraints(runLabel, constraint)
        add(runLabel)

        constraint.fill = GridBagConstraints.HORIZONTAL
        constraint.gridwidth = 1
        constraint.gridheight = 1
        constraint.gridx = 1
        constraint.gridy = 0
        constraint.weightx = 1.0
        constraint.weighty = 0.0

        numberOfRuns.preferredSize = numberOfRuns.preferredSize
        gridbag.setConstraints(numberOfRuns, constraint)
        add(numberOfRuns)

        constraint.fill = GridBagConstraints.NONE
        constraint.gridwidth = 1
        constraint.gridheight = 1
        constraint.gridx = 2
        constraint.gridy = 0
        constraint.weightx = 0.0
        constraint.weighty = 0.0

        statsButton.addActionListener { playOverNineThousandGames() }

        statsButton.preferredSize = statsButton.preferredSize
        gridbag.setConstraints(statsButton, constraint)
        add(statsButton)

        constraint.fill = GridBagConstraints.NONE
        constraint.insets = Insets(4, 4, 4, 4)
        constraint.gridwidth = 1
        constraint.gridheight = 1
        constraint.gridx = 0
        constraint.gridy = 1
        constraint.weightx = 0.0
        constraint.weighty = 0.0

        val outputLabel = JLabel("Output:")
        outputLabel.horizontalAlignment = JLabel.LEFT
        gridbag.setConstraints(outputLabel, constraint)
        add(outputLabel)

        constraint.fill = GridBagConstraints.BOTH
        constraint.gridwidth = 3
        constraint.gridheight = 1
        constraint.gridx = 0
        constraint.gridy = 2
        constraint.weightx = 1.0
        constraint.weighty = 1.0

        output.isEditable = false
        val jsp1 = JScrollPane(output)
        jsp1.preferredSize = jsp1.preferredSize
        gridbag.setConstraints(jsp1, constraint)
        add(jsp1)
    }

    private fun println(s: String) {
        output.append("$s\n")
        output.caretPosition = output.text.length - 1
    }

    private fun clear() {
        output.text = ""
        output.caretPosition = 0
    }

    private fun playOverNineThousandGames() {
        statsButton.isEnabled = false
        val gr = GameRunner()
        gr.start()
        statsButton.isEnabled = true
    }

    private inner class GameRunner : Thread() {
        override fun run() {
            try {
                clear()
                var session: Session = settingsWindow.generateSession()
                val scores = HashMap<String, Results>()
                val numSnakes = session.getSnakes().size
                for(s in session.getSnakes()) {
                    scores[s.getName()] = Results(numSnakes)
                }
                val numberOfGames: Int = numberOfRuns.text.toInt()
                for(currentGame in 0 until numberOfGames) {
                    println("Starting game #$currentGame")
                    repaint()
                    try {
                        session = settingsWindow.generateSession()
                        while(session.state() !== snaykuu.gameLogic.State.Finished) {
                            session.tick()
                        }
                        val result = session.getGameResult().getWinners()
                        for(i in 0 until result.size) {
                            for(s in result[i]!!) {
                                scores[s.getName()]!!.addResult(i)
                            }
                        }
                    } catch(e: Exception) {
                        println("Error: $e")
                    }
                }
                for((key, r) in scores) {
                    println("$key (place: frequency)")
                    for(i in 0 until numSnakes) {
                        println("\t" + (i + 1) + ": " + r.getFreq(i) + " times")
                    }
                }
                println("DONE")
            } catch(e: Exception) {
                println("Error: $e")
            }
        }
    }

    private class Results(numSnakes: Int) {
        private val placements: IntArray = IntArray(numSnakes)
        fun addResult(i: Int) {
            placements[i] += 1
        }

        fun getFreq(place: Int): Int {
            return placements[place]
        }

        init {
            for(i in placements.indices) {
                placements[i] = 0
            }
        }
    }
}
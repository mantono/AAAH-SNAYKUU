package snaykuu.userInterface

import snaykuu.gameLogic.GameResult
import snaykuu.gameLogic.RecordedGame
import snaykuu.gameLogic.Session
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.File
import java.util.concurrent.atomic.AtomicReference
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.WindowConstants

class PostGameWindow(session: Session): JFrame("SNAYKUU - results") {
    private val finalResult: GameResult = session.getGameResult()
    private val gameEndType: AtomicReference<GameEndType> = AtomicReference()
    val scoreBoardPanel: ScoreBoardPanel = ScoreBoardPanel(session)

    init {
        val newGameButton = JButton("New Game")
        newGameButton.addActionListener { gameEndType.set(GameEndType.NEW_GAME) }

        val rematchButton = JButton("Rematch")
        rematchButton.addActionListener { gameEndType.set(GameEndType.REMATCH) }

        val saveReplayButton = JButton("Save replay")
        saveReplayButton.addActionListener(SaveReplayButtonListener())

        val exitButton = JButton("Exit")
        exitButton.addActionListener { gameEndType.set(GameEndType.EXIT) }

        scoreBoardPanel.preferredSize = scoreBoardPanel.preferredSize
        add(scoreBoardPanel, BorderLayout.CENTER)

        val buttonPanel = JPanel()
        buttonPanel.add(newGameButton)
        buttonPanel.add(rematchButton)
        buttonPanel.add(saveReplayButton)
        buttonPanel.add(exitButton)
        buttonPanel.preferredSize = buttonPanel.preferredSize
        add(buttonPanel, BorderLayout.SOUTH)

        pack()
        setLocationRelativeTo(null)
        isVisible = true
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE

        printStandings()
        repaint()
    }

    private fun printStandings() {
        scoreBoardPanel.updateScore(finalResult)
    }

    private fun sleep(ms: Int) {
        try {
            Thread.sleep(ms.toLong())
        } catch(e: InterruptedException) {
            println(e)
        }
    }

    fun getGameEndType(): GameEndType {
        return try {
            resolveGameEndType()
        } finally {
            dispose()
        }
    }

    private tailrec fun resolveGameEndType(): GameEndType {
        sleep(1)
        return gameEndType.get() ?: resolveGameEndType()
    }

    private inner class SaveReplayButtonListener : ActionListener {
        override fun actionPerformed(event: ActionEvent) {
            val fileChooser = JFileChooser("./replays")
            fileChooser.fileFilter = RecordedGame.fileNameFilter()
            val returnValue = fileChooser.showSaveDialog(this@PostGameWindow)
            if(returnValue != JFileChooser.APPROVE_OPTION) return
            var file = fileChooser.selectedFile
            if(!file.name.endsWith(".${RecordedGame.SAVED_FILE_SUFFIX}")) {
                file = File(file.parent, file.name + ".${RecordedGame.SAVED_FILE_SUFFIX}")
            }
            try {
                finalResult.getRecordedGame().save(file)
            } catch(e: Exception) {
                e.printStackTrace()
                JOptionPane.showMessageDialog(this@PostGameWindow, e)
            }
        }
    }
}
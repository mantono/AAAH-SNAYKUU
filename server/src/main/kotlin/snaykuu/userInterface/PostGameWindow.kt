package snaykuu.userInterface

import snaykuu.gameLogic.GameResult
import snaykuu.gameLogic.RecordedGame
import snaykuu.gameLogic.Session
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.File
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.filechooser.FileNameExtensionFilter

class PostGameWindow(session: Session): JFrame("SNAYKUU - results") {
    private val finalResult: GameResult = session.getGameResult()
    private var gameEndType: GameEndType? = null

    init {
        val newGameButton = JButton("New Game")
        newGameButton.addActionListener(NewGa)
    }

    private inner class NewGameButtonListener : ActionListener {
        override fun actionPerformed(event: ActionEvent) {
            synchronized(this@PostGameWindow) { gameEndType = GameEndType.NEW_GAME }
        }
    }

    private inner class RematchButtonListener : ActionListener {
        override fun actionPerformed(event: ActionEvent) {
            synchronized(this@PostGameWindow) { gameEndType = GameEndType.REMATCH }
        }
    }

    private inner class SaveReplayButtonListener : ActionListener {
        override fun actionPerformed(event: ActionEvent) {
            val fileChooser = JFileChooser("./replays")
            fileChooser.fileFilter = RecordedGame.fileNameFilter()
            val returnValue = fileChooser.showSaveDialog(this@PostGameWindow)
            if(returnValue != JFileChooser.APPROVE_OPTION) return
            var file = fileChooser.selectedFile
            if(!file.name.endsWith(".srp")) {
                file = File(file.parent, file.name + ".srp")
            }
            try {
                finalResult.getRecordedGame().save(file)
            } catch(e: Exception) {
                e.printStackTrace()
                JOptionPane.showMessageDialog(this@PostGameWindow, e)
            }
        }
    }

    private inner class CloseButtonListener : ActionListener {
        override fun actionPerformed(event: ActionEvent) {
            synchronized(this@PostGameWindow) { gameEndType = GameEndType.EXIT }
        }
    }
}
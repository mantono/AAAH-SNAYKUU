package snaykuu.userInterface

import snaykuu.gameLogic.RecordedGame
import snaykuu.gameLogic.RecordedGame.Companion.load
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.JPanel

class ReplayPanel(
    private val settingsWindow: SettingsWindow
): JPanel() {

    init {
        val loadAndPlay = JButton("Load an old game and play it!")
        loadAndPlay.addActionListener(ReplayListener())
        add(loadAndPlay)
    }

    private fun startReplay(recordedGame: RecordedGame) {
        try {
            ReplayWindow(settingsWindow, recordedGame)
        } catch(e: NumberFormatException) {
            JOptionPane.showMessageDialog(parent, "You must enter a valid amount of pixels per square")
        }
    }

    private inner class ReplayListener: ActionListener {
        override fun actionPerformed(event: ActionEvent) {
            val fileChooser = JFileChooser("./replays")
            fileChooser.fileFilter = RecordedGame.fileNameFilter()

            val returnValue = fileChooser.showOpenDialog(parent)
            if(returnValue != JFileChooser.APPROVE_OPTION) return

            val file = fileChooser.selectedFile
            try {
                val recordedGame = load(file)
                startReplay(recordedGame)
            } catch(e: Exception) {
                e.printStackTrace()
                JOptionPane.showMessageDialog(parent, e)
            }
        }
    }
}
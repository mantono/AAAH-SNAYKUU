package snaykuu.userInterface

import snaykuu.gameLogic.RecordedGame
import snaykuu.gameLogic.State
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants

// TODO: Update all this to use coroutines
class ReplayWindow(
    private val settingsWindow: SettingsWindow,
    private val recordedGame: RecordedGame
): JFrame() {
    private val gameBoard: GameBoard = GameBoard(recordedGame, settingsWindow.pixelsPerUnit)
    private val scoreBoardPanel: ScoreBoardPanel = ScoreBoardPanel(recordedGame)
    private val controlPanel: ControlPanel = ControlPanel()
    private val replayThread: ReplayThread = ReplayThread()

    init {
        layout = BorderLayout()
        add(gameBoard, BorderLayout.CENTER)
        add(controlPanel, BorderLayout.SOUTH)
        add(scoreBoardPanel, BorderLayout.EAST)
        pack()

        addWindowListener(WindowListener())
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        setLocationRelativeTo(null)
        isVisible = true

        replayThread.start()
    }

    private fun updateGame() {
        recordedGame.tick()
        scoreBoardPanel.updateScore(recordedGame.getGameResult())
        repaint()
    }

    private inner class WindowListener: WindowAdapter() {
        override fun windowClosing(e: WindowEvent) {
            replayThread.stopRunning()
            dispose()
        }
    }

    private inner class ControlPanel: JPanel() {
        init {
            val beginButton = JButton("<<")
            val backOneFrame = JButton("<")
            val play = JButton("P")
            val forwardOneFrame = JButton(">")
            val endButton = JButton(">>")

            beginButton.addActionListener(BeginListener())
            backOneFrame.addActionListener(PreviousFrameListener())
            play.addActionListener(PlayListener())
            forwardOneFrame.addActionListener(NextFrameListener())
            endButton.addActionListener(EndListener())

            add(beginButton)
            add(backOneFrame)
            add(play)
            add(forwardOneFrame)
            add(endButton)
        }

        private inner class BeginListener: ActionListener {
            override fun actionPerformed(event: ActionEvent) {
                check(recordedGame.state() == State.Playing) {
                    "Invalid state " + recordedGame.state()
                }
                updateGame()
            }
        }

        private inner class PreviousFrameListener: ActionListener {
            override fun actionPerformed(event: ActionEvent) {
                updateGame()
            }
        }

        private inner class PlayListener: ActionListener {
            override fun actionPerformed(event: ActionEvent) {
                replayThread.togglePause()
            }
        }

        private inner class NextFrameListener: ActionListener {
            override fun actionPerformed(event: ActionEvent) {
                updateGame()
            }
        }

        private inner class EndListener: ActionListener {
            override fun actionPerformed(event: ActionEvent) {
                updateGame()
            }
        }
    }

    private inner class ReplayThread: Thread() {
        private val running: AtomicBoolean = AtomicBoolean(true)
        private val paused: AtomicBoolean = AtomicBoolean(true)

        fun isRunning(): Boolean = running.get()
        fun isPaused(): Boolean = paused.get()
        fun togglePause(): Boolean = paused.getAndSet(!paused.get())

        fun stopRunning() {
            running.set(false)
        }

        override fun run() {
            while(isRunning()) {
                updateGame()
                if(recordedGame.state() === snaykuu.gameLogic.State.Finished && !isPaused()) {
                    togglePause()
                }
                while(isPaused()) {
                    safeSleep(10)
                }
                try {
                    safeSleep(settingsWindow.gameSpeed.toLong())
                } catch(e: Exception) {
                    safeSleep(300)
                }
            }
        }
    }

    private fun safeSleep(ms: Long) {
        try {
            Thread.sleep(ms)
        } catch(e: InterruptedException) {
            System.err.println(e.message)
            e.printStackTrace()
        }
    }
}
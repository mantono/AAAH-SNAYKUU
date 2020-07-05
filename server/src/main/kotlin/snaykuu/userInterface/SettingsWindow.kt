package snaykuu.userInterface

import snaykuu.gameLogic.GameSettings
import snaykuu.gameLogic.Session
import snaykuu.gameLogic.Snake
import snaykuu.gameLogic.Snake.Companion.create
import java.awt.BorderLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JTabbedPane

class SettingsWindow(
    gameSettings: GameSettings = GameSettings.load()
): JFrame("SNAYKUU - settings") {
    @get:Synchronized
    var isDone = false
        private set
    private val tabbedPane: JTabbedPane
    private val snakeSettingsPanel: SnakeSettingsPanel
    private val gameSettingsPanel: GameSettingsPanel
    private val replayPanel: ReplayPanel
    private val developerPanel: DeveloperPanel
    private val startButton: JButton
    private val startButtonPanel: JPanel

    init {
        layout = BorderLayout()
        tabbedPane = JTabbedPane()

        snakeSettingsPanel = SnakeSettingsPanel()
        gameSettingsPanel = GameSettingsPanel(gameSettings)
        replayPanel = ReplayPanel(this)
        developerPanel = DeveloperPanel(this)

        tabbedPane.addTab("Snayks", snakeSettingsPanel)
        tabbedPane.addTab("Game settings", gameSettingsPanel)
        tabbedPane.addTab("Replay", replayPanel)
        tabbedPane.addTab("Developer", developerPanel)

        startButton = JButton("Start")
        startButton.addActionListener(StartButtonListener())
        startButtonPanel = JPanel()
        startButtonPanel.add(startButton)

        add(tabbedPane, BorderLayout.CENTER)
        add(startButtonPanel, BorderLayout.SOUTH)
        setSize(600, 400)
        defaultCloseOperation = EXIT_ON_CLOSE
    }

    fun putThisDamnWindowInMyFace() {
        isDone = false
        setLocationRelativeTo(null)
        isVisible = true
    }

    private inner class StartButtonListener : ActionListener {
        override fun actionPerformed(event: ActionEvent) {
            synchronized(this@SettingsWindow) { isDone = true }
        }
    }

    @Throws(Exception::class)
    fun generateSession(): Session {
        val metadata = gameSettingsPanel.generateMetadata()
        val snakes: Set<Snake> = create(snakeSettingsPanel.getSnakes()!!)
        val session = Session(metadata, HashSet(snakes))
        session.prepareForStart()
        return session
    }

    val gameSpeed: Int
        get() = gameSettingsPanel.getGameSpeed()

    val pixelsPerUnit: Int
        get() = gameSettingsPanel.getPixelsPerUnit()
}
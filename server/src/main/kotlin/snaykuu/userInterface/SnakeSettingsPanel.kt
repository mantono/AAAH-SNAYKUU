package snaykuu.userInterface

import snaykuu.gameLogic.Brain
import snaykuu.reflection.getBrains
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane

class SnakeSettingsPanel(
    private val brains: MutableMap<String, Class<out Brain?>> = TreeMap()
): JPanel() {
    private val snakeJList: JList<String> = JList()
    private val brainJList: JList<String> = JList()
    private val addSnakeButton: JButton = JButton("=>")
    private val removeSnakeButton: JButton = JButton("<=")
    private val reloadAllBrainsButton: JButton = JButton("Reload all brains")
    private val snakes: NavigableMap<String, String> = TreeMap<String, String>()

    init {
        val gridbag = GridBagLayout()
        layout = gridbag

        val constraint = GridBagConstraints()

        constraint.fill = GridBagConstraints.HORIZONTAL
        constraint.insets = Insets(4, 4, 4, 4)
        constraint.gridwidth = 1
        constraint.gridheight = 1
        constraint.gridx = 0
        constraint.gridy = 0

        val selected = JLabel("Available snakes:")
        selected.horizontalAlignment = JLabel.CENTER
        gridbag.setConstraints(selected, constraint)
        add(selected)

        constraint.fill = GridBagConstraints.HORIZONTAL
        constraint.insets = Insets(4, 4, 4, 4)
        constraint.gridwidth = 1
        constraint.gridheight = 1
        constraint.gridx = 2
        constraint.gridy = 0

        val available = JLabel("Snakes in game:")
        available.horizontalAlignment = JLabel.CENTER
        gridbag.setConstraints(available, constraint)
        add(available)

        constraint.fill = GridBagConstraints.BOTH
        constraint.gridwidth = 1
        constraint.gridheight = 8
        constraint.weightx = 0.5
        constraint.weighty = 0.5
        constraint.gridx = 0
        constraint.gridy = 1

        brainJList.addMouseListener(BrainMouseListener())

        val jsp2 = JScrollPane(brainJList)
        jsp2.preferredSize = jsp2.preferredSize
        gridbag.setConstraints(jsp2, constraint)
        add(jsp2)

        constraint.fill = GridBagConstraints.BOTH
        constraint.gridwidth = 1
        constraint.gridheight = 8
        constraint.weightx = 0.5
        constraint.weighty = 0.5
        constraint.gridx = 2
        constraint.gridy = 1

        snakeJList.addMouseListener(SnakeMouseListener())

        val jsp1 = JScrollPane(snakeJList)
        jsp1.preferredSize = jsp1.preferredSize
        gridbag.setConstraints(jsp1, constraint)
        add(jsp1)

        constraint.fill = GridBagConstraints.NONE
        constraint.weightx = 0.1
        constraint.weighty = 0.1
        constraint.insets = Insets(4, 4, 4, 4)
        constraint.gridheight = 4
        constraint.gridx = 1
        constraint.anchor = GridBagConstraints.SOUTH
        constraint.gridy = 0

        addSnakeButton.addActionListener(AddSnakeListener())
        gridbag.setConstraints(addSnakeButton, constraint)
        add(addSnakeButton)

        constraint.fill = GridBagConstraints.NONE
        constraint.weightx = 0.1
        constraint.weighty = 0.1
        constraint.insets = Insets(4, 4, 4, 4)
        constraint.gridheight = 4
        constraint.gridx = 1
        constraint.anchor = GridBagConstraints.NORTH
        constraint.gridy = 4

        removeSnakeButton.addActionListener(RemoveSnakeListener())
        gridbag.setConstraints(removeSnakeButton, constraint)
        add(removeSnakeButton)

        constraint.fill = GridBagConstraints.NONE
        constraint.weightx = 0.1
        constraint.weighty = 0.1
        constraint.insets = Insets(4, 4, 4, 4)
        constraint.anchor = GridBagConstraints.SOUTH
        constraint.gridx = 0
        constraint.gridy = 9
        constraint.gridwidth = 3
        constraint.gridheight = 1
        constraint.weighty = 0.0

        reloadAllBrainsButton.addActionListener(ReloadBrainsListener())
        gridbag.setConstraints(reloadAllBrainsButton, constraint)
        add(reloadAllBrainsButton)

        loadBrains()
    }

    fun getSnakes(): Map<String, Brain>? {
        return snakes.asSequence()
            .map { (key, value) -> key to brains.getValue(value).newInstance()!! }
            .toMap()
    }

    private fun loadBrains(): String {
        val brains: Set<Class<out Brain>> = getBrains()
        brainJList.setListData(brains.map { it.canonicalName }.toTypedArray())
        this.brains.putAll(brains.map { it.canonicalName to it })
        return brains
            .sortedBy { it.simpleName }
            .joinToString(separator = "\n") { " - ${it.simpleName}" }
    }

    private inner class AddSnakeListener : ActionListener {
        private fun generateSnakeName(name: String): String {
            var snakeName = name.split(".").last()
            var numberOfSnakesWithTheSameBrain = 1
            while(snakes.containsKey(snakeName)) {
                ++numberOfSnakesWithTheSameBrain
                snakeName = "$name#$numberOfSnakesWithTheSameBrain"
            }
            return snakeName
        }

        override fun actionPerformed(event: ActionEvent?) {
            val name: String = brainJList.selectedValue ?: return
            snakes[generateSnakeName(name)] = name
            snakeJList.setListData(snakes.keys.toTypedArray())
        }
    }

    private inner class RemoveSnakeListener : ActionListener {
        override fun actionPerformed(event: ActionEvent?) {
            val selectedObject: Any = snakeJList.selectedValue ?: return
            snakes.remove(selectedObject.toString())
            snakeJList.setListData(snakes.keys.toTypedArray())
        }
    }

    private inner class ReloadBrainsListener : ActionListener {
        override fun actionPerformed(event: ActionEvent) {
            val reloadedBrains: String = loadBrains()
            JOptionPane.showMessageDialog(this@SnakeSettingsPanel, "Successfully reloaded:\n$reloadedBrains")
        }
    }

    private inner class SnakeMouseListener : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
            if(e.clickCount % 2 == 0) {
                RemoveSnakeListener().actionPerformed(null)
            }
        }
    }

    private inner class BrainMouseListener : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
            if(e.clickCount % 2 == 0) {
                AddSnakeListener().actionPerformed(null)
            }
        }
    }
}
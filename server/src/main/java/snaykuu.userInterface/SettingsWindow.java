package snaykuu.userInterface;

import snaykuu.gameLogic.Metadata;
import snaykuu.gameLogic.Session;
import snaykuu.gameLogic.Snake;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

public class SettingsWindow extends JFrame
{
	private boolean done = false;
	private JTabbedPane tabbedPane;
	private SnakeSettingsPanel snakeSettingsPanel;
	private GameSettingsPanel gameSettingsPanel;
	private ReplayPanel replayPanel;
	private DeveloperPanel developerPanel;

	private JButton startButton;
	private JPanel startButtonPanel;

	public SettingsWindow()
	{
		super("SNAYKUU - settings");
		setLayout(new BorderLayout());

		tabbedPane = new JTabbedPane();

		snakeSettingsPanel = new SnakeSettingsPanel();
		gameSettingsPanel = new GameSettingsPanel();
		replayPanel = new ReplayPanel(this);
		developerPanel = new DeveloperPanel(this);

		tabbedPane.addTab("Snayks", snakeSettingsPanel);
		tabbedPane.addTab("Game settings", gameSettingsPanel);
		tabbedPane.addTab("Replay", replayPanel);
		tabbedPane.addTab("Developer", developerPanel);

		startButton = new JButton("Start");
		startButton.addActionListener(new StartButtonListener());
		startButtonPanel = new JPanel();
		startButtonPanel.add(startButton);

		add(tabbedPane, BorderLayout.CENTER);

		add(startButtonPanel, BorderLayout.SOUTH);

		setSize(600, 400);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	public void putThisDamnWindowInMyFace()
	{
		done = false;
		setLocationRelativeTo(null);
		setVisible(true);
	}


	private class StartButtonListener implements ActionListener
	{
		public void actionPerformed(ActionEvent event)
		{
			synchronized(SettingsWindow.this)
			{
				done = true;
			}
		}
	}

	public synchronized boolean isDone()
	{
		return done;
	}

	public Session generateSession() throws Exception
	{
		Metadata metadata = gameSettingsPanel.generateMetadata();
		Set<Snake> snakes = Snake.Companion.create(snakeSettingsPanel.getSnakes());

		Session session = new Session(metadata, new HashSet(snakes));
		session.prepareForStart();
		return session;
	}

	public int getGameSpeed()
	{
		return gameSettingsPanel.getGameSpeed();
	}

	public int getPixelsPerUnit()
	{
		return gameSettingsPanel.getPixelsPerUnit();
	}

}

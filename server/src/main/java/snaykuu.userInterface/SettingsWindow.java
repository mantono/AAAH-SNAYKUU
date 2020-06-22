package snaykuu.userInterface;

import snaykuu.gameLogic.Brain;
import snaykuu.gameLogic.Metadata;
import snaykuu.gameLogic.Session;
import snaykuu.gameLogic.Snake;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

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

		Random r = new Random(4L);
		int numSnakes = snakeSettingsPanel.getSnakes().size();
		float stepSize = 0.8f/numSnakes;
		int currentSnake = 0;

		HashSet<Snake> snakes = new HashSet<Snake>(16);
		for (Map.Entry<String, Brain> snakeEntry : snakeSettingsPanel.getSnakes().entrySet())
		{
			Snake snake = new Snake(currentSnake++, snakeEntry.getKey(), snakeEntry.getValue(), Color.getHSBColor(stepSize*currentSnake++, r.nextFloat()/2+0.5f, r.nextFloat()/2+0.5f));
			snakes.add(snake);
		}

		Session session = new Session(metadata, snakes);
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

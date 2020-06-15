package snaykuu;

import snaykuu.userInterface.GameEndType;
import snaykuu.userInterface.MainWindow;
import snaykuu.userInterface.PostGameWindow;
import snaykuu.userInterface.SettingsWindow;

import javax.swing.*;

class Main
{

	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e)
		{
		}
		SettingsWindow settingsWindow = new SettingsWindow();

		Session session = prepareSession(settingsWindow);

		//~ System.setSecurityManager(new ExitSecurityManager());

		GameEndType gameEndType;
		do
		{
			settingsWindow.dispose();

			int gameSpeed = settingsWindow.getGameSpeed();
			int pixelsPerUnit = settingsWindow.getPixelsPerUnit();
			gameEndType = runGame(session, gameSpeed, pixelsPerUnit);

			if (gameEndType == GameEndType.REMATCH)
			{
				try
				{
					session = settingsWindow.generateSession();
				}
				catch (Exception e)
				{
					javax.swing.JOptionPane.showMessageDialog(null, e);
					gameEndType = GameEndType.NEW_GAME;
				}
			}
			if (gameEndType == GameEndType.NEW_GAME)
				session = prepareSession(settingsWindow);

		}
		while (gameEndType != GameEndType.EXIT);
	}

	private static Session prepareSession(SettingsWindow settingsWindow)
	{
		try
		{
			settingsWindow.putThisDamnWindowInMyFace();

			while (!settingsWindow.isDone())
				sleep(10);

			return settingsWindow.generateSession();
		}
		catch (Exception e)
		{
			javax.swing.JOptionPane.showMessageDialog(settingsWindow, e);
			return prepareSession(settingsWindow);
		}
	}


	private static GameEndType runGame(Session session, int gameSpeed, int pixelsPerUnit)
	{
		MainWindow mainWindow = new MainWindow(session, pixelsPerUnit);
		session.tick();
		mainWindow.repaint();
		sleep(1000);

		while (!session.hasEnded())
		{
			session.tick();
			mainWindow.update();

			sleep(gameSpeed);
		}

		session.cleanup();

		PostGameWindow postGameWindow = new PostGameWindow(session);
		GameEndType gameEndType = postGameWindow.getGameEndType();
		mainWindow.dispose();

		return gameEndType;
	}


	private static void sleep(long ms)
	{
		try
		{
			Thread.currentThread().sleep(ms);
		}
		catch (InterruptedException e)
		{
		}
	}
}

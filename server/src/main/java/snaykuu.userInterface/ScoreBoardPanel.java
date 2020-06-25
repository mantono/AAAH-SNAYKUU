package snaykuu.userInterface;

import snaykuu.gameLogic.Game;
import snaykuu.gameLogic.GameResult;
import snaykuu.gameLogic.Snake;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.SortedMap;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NORTHWEST;

public class ScoreBoardPanel extends JPanel
{
	private GridBagLayout gbl = new GridBagLayout();
	private GridBagConstraints c = new GridBagConstraints();
	private Insets insets = new Insets(2,4,2,4);

	public ScoreBoardPanel(Game game)
	{
		setLayout(gbl);
		updateScore(game.getGameResult());
	}

	public void updateScore(GameResult gameResult)
	{
		this.removeAll();

		SortedMap<Integer, List<Snake>> placements = gameResult.getWinners();
		int placedSnakes = 0;

		printLegend(placedSnakes++);

		// Replace with placements.forEach { ... }
		for(int i = 0; i < placements.size(); ++i)
		{
			for(Snake snake : placements.get(i))
			{
				placeRow(placedSnakes++, i+1, snake);
			}
		}

		setPreferredSize(this.getPreferredSize());
		validate();
	}

	private void placeRow(int gridy, int p, Snake s)
	{
		c.anchor = NORTHWEST;
		c.fill = HORIZONTAL;
		c.weighty = 0.0;
		c.gridy = gridy;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = insets;

		c.gridx = 0;
		c.weightx = 0.0;
		JLabel place = new JLabel(""+p);
		place.setPreferredSize(place.getPreferredSize());
		gbl.setConstraints(place, c);
		add(place);

		c.gridx = 1;
		c.weightx = 0.0;
		JLabel color = new JLabel("   ");
		color.setPreferredSize(color.getPreferredSize());
		color.setOpaque(true);
		color.setBackground(s.getColor());
		gbl.setConstraints(color, c);
		add(color);

		c.gridx = 2;
		c.weightx = 10.0;
		JLabel snake = new JLabel(s.getName());

		if(s.isDead())
		{
			snake.setForeground(new Color(0xD00000));
		}

		snake.setPreferredSize(snake.getPreferredSize());
		gbl.setConstraints(snake, c);
		add(snake);

		c.gridx = 3;
		c.weightx = 0.0;
		JLabel score = new JLabel(""+s.getScore());
		score.setPreferredSize(score.getPreferredSize());
		gbl.setConstraints(score, c);
		add(score);

		c.gridx = 4;
		c.weightx = 0.0;
		JLabel age = new JLabel(""+s.getLifespan());
		age.setPreferredSize(age.getPreferredSize());
		gbl.setConstraints(age, c);
		add(age);
	}

	private void printLegend(int gridy)
	{
		c.anchor = NORTHWEST;
		c.fill = HORIZONTAL;
		c.weighty = 0.0;
		c.gridy = gridy;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.insets = insets;

		c.gridx = 0;
		c.weightx = 0.0;
		JLabel place = new JLabel("Place");
		place.setPreferredSize(place.getPreferredSize());
		gbl.setConstraints(place, c);
		add(place);

		c.gridx = 1;
		c.weightx = 0.0;
		JLabel color = new JLabel("Color");
		color.setPreferredSize(color.getPreferredSize());
		gbl.setConstraints(color, c);
		add(color);

		c.gridx = 2;
		c.weightx = 10.0;
		JLabel snake = new JLabel("Name");
		snake.setPreferredSize(snake.getPreferredSize());
		gbl.setConstraints(snake, c);
		add(snake);

		c.gridx = 3;
		c.weightx = 0.0;
		JLabel score = new JLabel("Score");
		score.setPreferredSize(score.getPreferredSize());
		gbl.setConstraints(score, c);
		add(score);

		c.gridx = 4;
		c.weightx = 0.0;
		JLabel age = new JLabel("Age");
		age.setPreferredSize(age.getPreferredSize());
		gbl.setConstraints(age, c);
		add(age);
	}
}

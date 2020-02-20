package bot;

import snaykuu.gameLogic.*;

public class Mad implements Brain
{
	public Direction getNextMove(Snake yourSnake, GameState gamestate)
	{
		return yourSnake.getCurrentDirection();
	}
}

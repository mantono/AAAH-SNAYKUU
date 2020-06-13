package snaykuu.gameLogic;

public interface Game
{
	public GameState getCurrentState();
	public Metadata getMetadata();
	public GameResult getGameResult();
	public Game tick();
	public boolean hasEnded();
}

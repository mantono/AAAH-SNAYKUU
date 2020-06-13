package snaykuu.gameLogic;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Frame implements Serializable
{
	private Board board;
	private Set<Snake> snakes = new HashSet<Snake>();

	public Frame(Board board, Set<Snake> snakes)
	{
		this.board = board;
		this.snakes.addAll(snakes);
	}

	public Board getBoard()
	{
		return board;
	}

	public Set<Snake> getSnakes()
	{
		return snakes;
	}
}

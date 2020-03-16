package snaykuu.gameLogic;

import snaykuu.gameLogic.Board;

import java.util.Set;
import java.util.HashSet;
import java.io.Serializable;

public class Frame implements Serializable
{
	private snaykuu.gameLogic.Board board;
	private Set<Snake> snakes = new HashSet<Snake>();
	
	public Frame(Board board, Set<Snake> snakes)
	{
		this.board = board.copy(board.getWidth(), board.getHeight());;

		for (Snake snake : snakes)
			this.snakes.add(new Snake(snake));
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

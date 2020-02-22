package snaykuu.gameLogic;

import snaykuu.gameLogic.snaykuu.Board;

import java.util.Set;
import java.util.HashSet;
import java.io.Serializable;

public class Frame implements Serializable
{
	private snaykuu.gameLogic.snaykuu.Board board;
	private Set<Snake> snakes = new HashSet<Snake>();
	
	public Frame(snaykuu.gameLogic.snaykuu.Board board, Set<Snake> snakes)
	{
		this.board = new snaykuu.gameLogic.snaykuu.Board(board);

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

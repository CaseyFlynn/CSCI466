package com.flynnovations.game.shared;

import java.io.Serializable;

public class PlayerAnswer implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public PlayerAnswer() {
		this(false, false);
	}
	
	public PlayerAnswer(boolean answered, boolean correct) {
		this(false,false,0,0);
	}
	
	public PlayerAnswer(boolean answered, boolean correct, int score, int runningStreak) {
		this.answered = answered;
		this.correct = correct;
		this.score = score;
		this.runningStreak = runningStreak;
	}
	
	/**
	 * Boolean indicating whether a players attempted to answer this question
	 */
	public boolean answered;
	
	/**
	 * Boolean indicating whether a player correctly answered the question
	 */
	public boolean correct;
	
	public int score;
	
	public int runningStreak;
}

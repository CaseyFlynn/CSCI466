package com.flynnovations.game.shared;

import java.io.*;

import com.flynnovations.game.server.Player;

public class PlayerInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	public String playerName;
	public int playerId;
	public String playerIP;
	public int questionsAnswered;
	public int questionsCorrect;
	public int score;
	public int runningStreak;
	
	public PlayerInfo(Player p) {
		this.playerName = p.playerName;
		this.playerId = p.getWorkstationId();
		this.playerIP = p.ipAddress;
		this.questionsAnswered = p.questionsAnswered;
		this.questionsCorrect = p.correctAnswers;
		this.score = p.score;
		this.runningStreak = p.runningStreak;
	}
}

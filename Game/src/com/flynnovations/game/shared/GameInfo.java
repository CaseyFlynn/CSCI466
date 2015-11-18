package com.flynnovations.game.shared;

import java.io.Serializable;
import com.flynnovations.game.server.Game;

public class GameInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	public String gameName;
	public int gameId;
	public int numberOfPlayers;
	
	public GameInfo(Game g) {
		this.gameName = g.gameName;
		this.gameId = g.getGameId();
		this.numberOfPlayers = g.getPlayers().size();
	}
}

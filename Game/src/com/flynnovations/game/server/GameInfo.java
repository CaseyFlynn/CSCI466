package com.flynnovations.game.server;

import java.io.Serializable;
import java.util.*;

import com.flynnovations.game.server.Game;

public class GameInfo implements Serializable{
	public String gameName;
	public int gameId;
	public int numberOfPlayers;
	
	public GameInfo(Game g) {
		this.gameName = g.gameName;
		this.gameId = g.getGameId();
		this.numberOfPlayers = g.getPlayers().size();
	}
}

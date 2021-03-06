package com.flynnovations.game.client;

import java.util.*;

import com.flynnovations.game.shared.GameInfo;
import com.flynnovations.game.shared.PlayerAnswer;
import com.flynnovations.game.shared.PlayerInfo;
import com.flynnovations.game.shared.Question;

public class Client {
	
	public String playerName = "Anon";
	private ServerCom serverCom;
	private ClientCom clientCom;
	private ClientUI ui;
	private Question question;
	private long serverOffset;
	private long rttStart;
	private long lastServerTime;
	private HashSet<PlayerInfo> playerInfo;
	
	public boolean hasBuzzed = false;
	public boolean hasAnswered = false;
	public long buzzOffset = Long.MAX_VALUE;
	
	
	public static void main(String args[]) {
		Client c = new Client();
		c.run();
	}
	
	
	public void run() {
		//create server communication thread
		serverCom = new ServerCom(this);
		serverCom.start();
		
		//establish server time
		rttStart = serverCom.getServerOffset();
		
		//create client communication thread
		clientCom = new ClientCom(this, 0);
		clientCom.start();
		
		ui = new ClientUI(this);
		ui.run();
	}
	
	public void answerQuestion(int answer, boolean answered) {
		PlayerAnswer pa = new PlayerAnswer(answered, false);
		PlayerInfo me = findMe();
		
		if (answer == 1) {
			pa.correct = true;
			if (ui.gameTimerCounter > ui.gameTimerMax - 50) {
				pa.score = 4;
			} else if (ui.gameTimerCounter > ui.gameTimerMax - 100) {
				pa.score = 3;
			} else if (ui.gameTimerCounter > ui.gameTimerMax - 150) {
				pa.score = 2;
			} else {
				pa.score = 1;
			}
			
			//get our running streak
			pa.runningStreak = (me.runningStreak < 0) ? 0 : me.runningStreak + 1;
			
			clientCom.sendAnswer(true);
		} else {
			clientCom.sendAnswer(false);
			pa.runningStreak = (me.runningStreak > 10) ? me.runningStreak - 10 - (me.runningStreak % 10) : 0;
		}

		serverCom.questionDone(pa);
		
		//update list of player names and scores
		serverCom.requestPlayersInGame();
	}
	
	public void endQuestion() {
		PlayerAnswer pa = new PlayerAnswer(false, false);	
		PlayerInfo me = findMe();
		pa.runningStreak = (me.runningStreak > 10) ? me.runningStreak - 10 - (me.runningStreak % 10) : 0;

		serverCom.questionDone(pa);		
		//update list of player names and scores
		serverCom.requestPlayersInGame();
	}
	
	
	
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
		serverCom.setNameOnServer(playerName);
	}
	
	public void requestGames() {
		serverCom.requestGames();
	}
	
	public void createGame(String gameName) {
		serverCom.createNewGame(gameName);
		serverCom.requestGames();
	}
	
	public void joinGame(int gameId) {
		serverCom.joinGame(gameId);
		serverCom.requestPlayersInGame();
		clientCom.setGameId(gameId);
	}
	
	public void leaveGame() {
		serverCom.leaveGame();
	}
	
	public void resetBuzz() {
		hasBuzzed = false;
		hasAnswered = false;
		buzzOffset = Long.MAX_VALUE;
		//TODO: clientUI reset
		ui.resetBuzz();
	}
	
	public void questionRecieved(Question q) {
		hasBuzzed = false;
		hasAnswered = false;
		buzzOffset = Long.MAX_VALUE;
		
		question = q;
		serverCom.ackQuestion();
	}
	
	public void displayQuestion(long serverTime) {
		lastServerTime = serverTime;
		Timer t = new Timer();
		TimerTask tt = new TimerTask() {
			@Override
			public void run() {
				ui.displayQuestion(question);
			}
		};

		t.schedule(tt, Math.abs((serverTime - System.currentTimeMillis() - serverOffset)));
		//t.schedule(tt, 5000);
		
		//MESS WITH UI TIMING
		PlayerInfo me = findMe();
		if (me.runningStreak > 0) {
			ui.gameTimerMax = Math.max(ui.gameTimerDefault - ((me.runningStreak / 10) * 10), 100);
		} else {
			ui.gameTimerMax = ui.gameTimerDefault;
		}
	}
	
	public void gamesRecieved(ArrayList<GameInfo> games) {
		System.out.println("# of games running: " + games.size());
		for (GameInfo g : games) {
			System.out.println(g.gameName);
		}
		
		//call client UI to update game list on display
		ui.loadGames(games);
	}
	
	public void sendBuzz() {
		clientCom.sendBuzz(playerName, lastServerTime, serverOffset);
	}
	
	public void buzzRecieved(String playerName) {
		ui.OtherPlayerBuzz(playerName);
	}
	
	public void answerRecieved(boolean isCorrect, String playerName) {
		if(this.playerName.equals(playerName)){
			// do nothing
		} else if (isCorrect) {
			//submit that we are ready for next question.
			PlayerInfo me = findMe();
			PlayerAnswer pa = new PlayerAnswer(false, false);
			pa.runningStreak = (me.runningStreak > 10) ? me.runningStreak - 10 - (me.runningStreak % 10) : 0;

			serverCom.questionDone(pa);			
			ui.OtherPlayerWin(playerName);
			
			//update list of player names and scores
			serverCom.requestPlayersInGame();
			
		} else if (!isCorrect) {
			ui.OtherPlayerFail();
		}
	}
	
	public void serverTicksRecieved(long ticks) {
		long rtt = System.currentTimeMillis() - rttStart;
		serverOffset = ticks - rtt - System.currentTimeMillis();
	}

	public void playersRecieved(HashSet<PlayerInfo> playerInfo) {
		ui.loadPlayers(playerInfo);
		this.playerInfo = playerInfo;
	}
	
	private PlayerInfo findMe() {
		for (PlayerInfo p : playerInfo) {
			if (p.playerName.equals(this.playerName)) {
				return p;
			}
		}
		return null;
	}
}

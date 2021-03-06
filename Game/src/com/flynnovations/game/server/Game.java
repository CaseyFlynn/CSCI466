package com.flynnovations.game.server;

import java.io.Serializable;
import java.util.*;

import com.flynnovations.game.shared.Question;
import com.flynnovations.serialization.Serializer;


public class Game implements Serializable {

	private static final long serialVersionUID = 1L;
	private int gameId;
	public String gameName;
	private HashSet<Player> players;
	private HashSet<Player> pendingAck;
	private HashSet<Player> pendingAnswer;
	private Server gameServer;
	//Game Communication Strings
	private final String COM_SEND_QUESTION = "Question";
	private final String COM_DISPLAY_QUESTION = "Display";
	private boolean hasStarted = false;
	public int totalQuestionsAsked = 0;
	
	//Game vars
	private final long displayOffset = 5000; //3 seconds

	//IData object to get questions from
	private IData dataAccess;
	
	/**
	 * get the Id of this game
	 * @return int containing id of this game
	 */
	public int getGameId() {
		return gameId;
	}
	
	/**
	 * Return all players subscribed to this game
	 * @return HashSet<Player> containing all players
	 */
	public synchronized HashSet<Player> getPlayers() {
		return players;
	}
	
	/**
	 * Create a new game
	 * @param gameId unique id of game
	 */
	public Game (int gameId, Server s) {
		this(gameId, "Crazy Trivia", s);
	}
	
	public Game (int gameId, String gameName, Server s) {
		//init vars
		players = new HashSet<Player>();
		pendingAck = new HashSet<Player>();
		pendingAnswer = new HashSet<Player>();
		this.gameId = gameId;
		this.gameName = gameName;
		this.gameServer = s;
		dataAccess = new FlatFileData();
	}
	
	/**
	 * Release all players from this game in preparation to close
	 */
	public void destroy(){
		//disconnect all players from game before closing this game session;
		players.clear();
		gameServer.removeGame(this.gameId);
	}
	
	/**
	 * Subscribe a player to this game state machine
	 * @param p Player to subscribe
	 */
	public void subscribe(Player p) {
		//inform users of player joining
		System.out.println("Player: " + p.getName() + " joined game: " + gameId);
		players.add(p);

		//check to see if this is our first subscription
		if (players.size() == 1 && !hasStarted) {
			hasStarted = true;
			runGame();
		}
	}
	
	
	/**
	 * Unsubscribe a player from this game
	 * @param p Player to unsubscribe
	 */
	public void unSubscribe(Player p) {
		players.remove(p);
		//remove pending ack and answers so the player doesn't hold up the game
		pendingAck.remove(p);
		pendingAnswer.remove(p);
		if (players.isEmpty()) {
			destroy();
		}
	}

	/**
	 * Begin running game state machine
	 */
	public void runGame() {
		//Inform user that game is running
		System.out.println("running game: " + gameId);
		sendQuestion();
	}
	
	/**
	 * Send a question to all users, and prepare for next states
	 */
	private void sendQuestion() {
		try {
			//get a question from data access layer
			Question q = dataAccess.getQuestion();
			pendingAck.clear();
			pendingAnswer.clear();
			//serialize the question
			String serializedQuestion = Serializer.serialize(q);

			for (Player p : players)
			{
				//add player to list of pending ack
				pendingAck.add(p);
				
	
			}
			
			//foreach player in players
			for (Player p : players)
			{
				//increment player question counter
				p.questionsAsked++;
				try {

					//add player to list of pending ack
					//pendingAck.add(p);
					
					//add player to list of pending answer
					//pendingAnswer.add(p);

					//send the question
					p.sendMessage(COM_SEND_QUESTION + " " + serializedQuestion);
					
				} catch (Exception e) {
					//can't send messages to user, kill em
					System.out.println("****************************NNNNNNNNNNNNNNN************************");
					e.printStackTrace();
					unSubscribe(p);
				}
			} 
			
			//increment game questions asked for statistics
			totalQuestionsAsked++;
			
		} catch (Exception e) {
			//problem getting or serializing question, we are hosed.
			e.printStackTrace();
			destroy();
		}
	}
	
	/**
	 * Inform all users to display the question they have ready
	 */
	private void sendDisplay() {
		//send display question message along with server time offset by displayOffset (3 seconds)
		String message = COM_DISPLAY_QUESTION + " " + (System.currentTimeMillis() + displayOffset);

		//inform each user to display the message at the same time
		for (Player p : pendingAnswer) {
			p.sendMessage(message);
		}
	}
	
	public synchronized void addAck(Player p) {
		//remove player from pending ack
		if (pendingAck.contains(p)) {
			//add player to list of pending answer
			pendingAnswer.add(p);			
			pendingAck.remove(p);
		}

		//if all players have ack'd, send display message
		if (pendingAck.isEmpty()) {
			sendDisplay();
		}
	}
	
	public synchronized void playerDone(Player p) {
		//remove player from list of pending answer
		if (pendingAnswer.contains(p)){
			pendingAnswer.remove(p);
		}
		
		//for fun, display the user has answered, this should be removed after testing
		System.out.println("Player: " + p.getWorkstationId() + " answered.");
		
		//if pending answer is empty
		if (pendingAnswer.isEmpty()) {
			sendQuestion();
		}
	}
}

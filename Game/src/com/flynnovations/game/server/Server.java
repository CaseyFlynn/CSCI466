package com.flynnovations.game.server;

import java.io.*;
import java.util.*;
import java.net.Socket;
import java.net.ServerSocket;

public class Server implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private ArrayList<Game> currentGames;
	private int playerCounter;
	private int gameCounter;
	
	//TODO: move to configuration class
	private final int listeningPort = 9991;
	private final int maximumGames = 100;
	
	/**
	 * Main entry point of program
	 * */
	public static void main(String args[]){
		//Create an instance of this to run non-static members
		Server s = new Server();
		try {
			//start listening
			s.runServer();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Setup and run a socket to listen for incoming connections
	 * @throws IOException exception thrown if socket cannot be created
	 */
	public void runServer() throws IOException {
		ServerSocket socket = new ServerSocket(listeningPort);
		//inform user running
		System.out.println("Server running on port " + listeningPort);

		playerCounter = 0;
		gameCounter = 1;
		currentGames = new ArrayList<>();

		try {
			//create initial game object
			addNewGame();
			
			while (true) {
				//inform user we are ready to accept
				System.out.println("waiting to accept");
				
				//user connected
				Socket playerSocket = socket.accept();
				
				//inform user that someone has connected
				System.out.println("Player connected.");
				
				//create and run player thread
				Player p = new Player(playerSocket, playerCounter, this);
				p.start();
				playerCounter++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("Ready to close connection");
			socket.close();
		}
	}

	/**
	 * Create a new game and add it to running games
	 * @throws Exception: if the gameId already exists, 
	 * or if the maximum mumber of games has been reached
	 */
	public int addNewGame() throws Exception {
		return addNewGame(gameCounter);
	}

	/**
	 * Create a new game and add it to running games
	 * @param gameId unique identifier of game
	 * @throws Exception: if the gameId already exists, 
	 * or if the maximum mumber of games has been reached
	 */
	public int addNewGame(int gameId) throws Exception {
		return addNewGame(gameId, "Crazy Trivia");
	}

	
	/**
	 * Create a new game and add it to running games
	 * @param gameId unique identifier of game
	 * @throws Exception: if the gameId already exists, 
	 * or if the maximum mumber of games has been reached
	 */
	public int addNewGame(String gameName) throws Exception {
		return addNewGame(gameCounter, gameName);
	}


	/**
	 * Create a new game and add it to running games
	 * @param gameId unique identifier of game
	 * @throws Exception: if the gameId already exists, 
	 * or if the maximum mumber of games has been reached
	 */
	public int addNewGame(int gameId, String gameName) throws Exception {
		//Check for maximum number of games running.
		if (currentGames.size() == maximumGames) {
			throw new Exception("Maximum games reached. Cannot create new game");
		}
		
		//verify unique gameId
		for (Game g : currentGames) {
			if (g.getGameId() == gameId) {
				throw new Exception("Provided game ID already exists.");
			}
		}
		
		//create the game and add it to running games
		Game curGame = new Game(gameId, gameName, this);
		currentGames.add(curGame);
		gameCounter++;
		if (gameCounter % 256 == 0) {
			gameCounter++;
		}
		return gameId;
	}
	
	/**
	 * Destroy a game with the given id
	 * @param gameId id of game to destroy
	 */
	public void removeGame(int gameId) {
		for (int i=0;i<currentGames.size();i++) {
			if (currentGames.get(i).getGameId() == gameId)
			{
				//destroy the game and remove it from running games
				//currentGames.get(i).destroy();
				currentGames.remove(i);
				//gameCounter--;
			}
		}
	}
	
	/**
	 * Get all running games
	 * @return ArrayList<Games> containing all running games
	 */
	public ArrayList<Game> getCurrentGames() {
		return currentGames;
	}
}

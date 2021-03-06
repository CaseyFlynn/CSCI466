package com.flynnovations.game.server;

import java.net.Socket;
import java.io.*;
import java.util.*;

import com.flynnovations.game.shared.GameInfo;
import com.flynnovations.game.shared.PlayerAnswer;
import com.flynnovations.game.shared.PlayerInfo;
import com.flynnovations.serialization.Serializer;

public class Player extends Thread  implements Serializable{
	//Advised for serialization
	private static final long serialVersionUID = 2L;
	private Socket socket;
	private int workstationId;
	private Game game;
	private BufferedReader inputReader;
	private PrintWriter outputWriter;
	private Server gameServer;

	//Game Statistics
	public int questionsAsked;
	public int questionsAnswered;
	public int correctAnswers;
	public int score;
	public int runningStreak;
	
	//workstationInfo
	public String ipAddress;
	public String playerName;

	//Communication from client
	private final String COM_GET_GAMES = "GetGames";
	private final String COM_JOIN_GAME = "JoinGame";
	private final String COM_LEAVE_GAME = "LeaveGame";
	private final String COM_QUESTION_ACK = "QuestionAck";
	private final String COM_QUESTION_DONE = "QuestionDone";
	private final String COM_GET_SERVER_TICKS = "GetServerTicks";
	private final String COM_SET_PLAYER_NAME = "SetPlayerName";
	private final String COM_CREATE_NEW_GAME = "CreateNewGame";
	private final String COM_GET_PLAYERS_IN_GAME = "GetPlayersInGame";

	//Server answers
	private final String COM_SEND_GAMES = "Games";
	private final String COM_SEND_SERVER_TICKS = "ServerTicks";
	private final String COM_SEND_PLAYERS_IN_GAME = "PlayersInGame";


	/**
	 * Get the workstationId of the player
	 * @return int containing workstationId
	 */
	public int getWorkstationId() {
		return workstationId;
	}

	/**
	 * Set the workstationId of this player
	 * @param workstationId to set
	 */
	public void setWorkstationId(int workstationId) {
		this.workstationId = workstationId;
	}

	/**
	 * Set the game that this player is subscribed to,
	 * if this is null, player will not be connected to a game server
	 * @param game Game to subscribe to
	 */
	public void setGame(Game game) {
		//if we are subscribed to a game, unsubscribe
		if (this.game != null) {
			this.game.unSubscribe(this);
		}

		//set the game and subscribe (if not null)
		this.game = game;
		if (game != null) {
			game.subscribe(this);
		}
	}

	/**
	 * Create a player thread that will communicate with client workstation
	 * @param s socket connection to the player
	 * @param workstationId workstationId of this player
	 * @param gameServer gameServer this player is assigned to
	 */
	public Player(Socket s, int workstationId, Server gameServer){
		//assign members
		this.socket = s;
		this.workstationId = workstationId;
		this.gameServer = gameServer;
		this.ipAddress = s.getInetAddress().getHostAddress();

		//initialize statistics for this game
		this.questionsAsked = 0;
		this.questionsAnswered = 0;
		this.correctAnswers = 0;

		//set up communication objects
		try {
			inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outputWriter = new PrintWriter(socket.getOutputStream(), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 
	 * Close all socket connections for the given user, and prepare
	 * the user for destruction
	 */
	public void destroy() {
		try {
			//if we are joined to a game, unsuscribe
			if (game != null) {
				game.unSubscribe(this);
			}
			//close communication channels
			inputReader.close();
			outputWriter.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Run communication loop for the client (this should be invoked by Thread.begin())
	 * */
	public void run() {
		try {
			//Notify that this player is running.
			System.out.println("Player: " + workstationId + " running");
			while (true) {
				String command = inputReader.readLine();

				//inform command received
				System.out.println("command recieved from Player: " + workstationId + " command: " + command);

				//Process command
				if (command.startsWith(COM_GET_GAMES)) {
					sendGames();
				} else if (command.startsWith(COM_JOIN_GAME)) {
					joinGame(command);
				} else if (command.startsWith(COM_LEAVE_GAME)) {
					leaveGame();
				} else if (command.startsWith(COM_CREATE_NEW_GAME)) {
					createNewGame(command);
				} else if (command.startsWith(COM_GET_PLAYERS_IN_GAME)) {
					getPlayersInGame(command);
				} else if (command.startsWith(COM_SET_PLAYER_NAME)) {
					setPlayersName(command);
				} else if (command.startsWith(COM_QUESTION_ACK)) {
					questionAcknowledged();
				} else if (command.startsWith(COM_QUESTION_DONE)) {
					questionDone(command);
				} else if (command.startsWith(COM_GET_SERVER_TICKS)) {
					sendServerTicks();
				}
			}
		} catch (Exception e) {
			//client disconnected
			destroy();
		}
	}


	private void setPlayersName(String command) {
		try {
			//parse command to get gameId we are joining,
			String playerName = command.substring(COM_SET_PLAYER_NAME.length() + 1);
			this.playerName = playerName;
			//testing
			//sendMessage("Hello " + playerName);
		} catch (Exception e) {
			//invalid name, cannot set
			e.printStackTrace();
		}
	}

	private void getPlayersInGame(String command) {
		try {
			//parse command to get gameId we are joining,
			String gameNumber = command.substring(COM_GET_PLAYERS_IN_GAME.length() + 1);

			int gameId = Integer.parseInt(gameNumber);
			HashSet<Player> players = null; 
			for (Game g : gameServer.getCurrentGames()) {
				if (g.getGameId() == gameId) {
					players = g.getPlayers();
					//players.remove(this);
				}
			}

			if (players != null) {
				HashSet<PlayerInfo> pi = new HashSet<>();
				System.out.println("Number of players in this game: " + players.size());
				for (Player p: players) {
					System.out.println("Player: " + p.playerName);
					pi.add(new PlayerInfo(p));
				}
				String serializedPlayers = Serializer.serialize(pi);
				sendMessage(COM_SEND_PLAYERS_IN_GAME + " " + serializedPlayers);
			} else {
				System.out.println("NO PLAYAZ");
			}
		} catch (Exception e) {
			//invalid name, cannot set
			e.printStackTrace();
		}
	}

	private void createNewGame(String command) {
		if (game != null) {
			game.unSubscribe(this);
		}
		try {
			String gameName;
			if (command.length() < COM_CREATE_NEW_GAME.length() + 1 ) {
				gameName = "Crazy Trivia - " + playerName;
			} else {
				gameName = command.substring(COM_CREATE_NEW_GAME.length() + 1);
			}
			
			gameServer.addNewGame(gameName); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Leave the current game
	 * @param command
	 */
	private void leaveGame() {
		//if we are subscribed to a a game
		if (game != null) {
			//unsubscribe from the current game
			game.unSubscribe(this);

			//reset statistics
			this.questionsAsked = 0;
			this.questionsAnswered = 0;
			this.correctAnswers = 0;

			game = null;
		}
	}


	/**
	 * Sends the current server time, used for synchronizing clients
	 */
	private void sendServerTicks() {
		String message = COM_SEND_SERVER_TICKS + " " + System.currentTimeMillis();
		sendMessage(message);
	}

	/**
	 * Send a message to the client
	 * @param message String containing message to send
	 */
	public void sendMessage(String message) {
		try {
			outputWriter.println(message);
		} catch (Exception e) {
			//can't communicate with client
			e.printStackTrace();
			//kill this client
			destroy();
		}
	}

	/**
	 * Send a list of running games that the player is not subscribed to back to client
	 */
	private void sendGames() {
		try {
			//Serialize a list of all running games and send to the client
			ArrayList<GameInfo> runningGames = new ArrayList<>();
			for (Game g : gameServer.getCurrentGames()) {
				runningGames.add(new GameInfo(g));
			}
			
			String serializedGames = Serializer.serialize(runningGames);
			sendMessage(COM_SEND_GAMES + " " + serializedGames);
		} catch (Exception e) {
			//unable to send list of games to client (hopefully this isn't reached)
			e.printStackTrace();
		}
	}

	/**
	 * Inform game that this player has received the question correctly
	 */
	private void questionAcknowledged() {
		if (game != null){
			//acknowledge ack to game
			game.addAck(this);
		}
	}

	/**
	 * Inform game that this player is finished with the current question
	 * @param command String containing the questionDone command followed by a serialized
	 * PlayerAnswer object
	 */
	private void questionDone(String command) {
		if (game != null) {
			//get serialized PlayerAnswer
			String serializedPlayerAnswer = command.substring(COM_QUESTION_DONE.length() + 1);

			//Uncomment to show the player answer
			System.out.println(serializedPlayerAnswer);

			PlayerAnswer answer = null;

			//deserialize the PlayerAnswer
			try {
				answer = Serializer.deserialize(serializedPlayerAnswer, PlayerAnswer.class);
			} catch (Exception e) {
				//we cannot update statistics, but we don't need to crash
				e.printStackTrace();
			}

			if (answer != null) {
				System.out.println("success.");
				if (answer.answered) questionsAnswered++;
				if (answer.correct) correctAnswers++;
				this.score += answer.score;
				this.runningStreak = answer.runningStreak;
			} else {
				System.out.println("AWW FUCK");
			}

			//inform game that this player is ready for the next question
			game.playerDone(this);
		}
	}

	/**
	 * Attempt to join a game
	 * @param command Command string followed by the gameId the player wishes to join
	 */
	private void joinGame(String command) {
		try {
			//parse command to get gameId we are joining,
			String gameNumber = command.substring(COM_JOIN_GAME.length() + 1);
			int gameId = Integer.parseInt(gameNumber);

			//find the game with requested id
			for (Game g : gameServer.getCurrentGames()) {
				if (g.getGameId() == gameId) {
					System.out.println("Joining a game!");
					g.subscribe(this);
					game = g;
				}
			}
		} catch (Exception e) {
			//invalid game id provided, cannot join the game.
			e.printStackTrace();
		}
	}
}

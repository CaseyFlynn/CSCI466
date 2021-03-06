package com.flynnovations.game.client;

import java.util.*;

import com.flynnovations.game.shared.GameInfo;
import com.flynnovations.game.shared.PlayerAnswer;
import com.flynnovations.game.shared.PlayerInfo;
import com.flynnovations.game.shared.Question;
import com.flynnovations.serialization.Serializer;

import java.io.*;
import java.net.*;

public class ServerCom extends Thread {

	private Socket serverSocket;
	public int gameId;
	private BufferedReader inputReader;
	private PrintWriter outputWriter;
	private Client client;

	//read settings from configuration file
	private final String serverIP = Config.Values().getServerIP();
	private final int serverPort = Config.Values().getServerPort();

	//Game Communication Strings from server
	private final String COM_QUESTION = "Question";
	private final String COM_DISPLAY_QUESTION = "Display";
	private final String COM_GAMES = "Games";
	private final String COM_SERVER_TICKS = "ServerTicks";
	private final String COM_PLAYERS_IN_GAME = "PlayersInGame";

	//Send strings to server
	private final String COM_GET_GAMES = "GetGames";
	private final String COM_JOIN_GAME = "JoinGame";
	private final String COM_LEAVE_GAME = "LeaveGame";
	private final String COM_QUESTION_ACK = "QuestionAck";
	private final String COM_QUESTION_DONE = "QuestionDone";
	private final String COM_GET_SERVER_TICKS = "GetServerTicks";
	private final String COM_SET_PLAYER_NAME = "SetPlayerName";
	private final String COM_CREATE_NEW_GAME = "CreateNewGame";
	private final String COM_GET_PLAYERS_IN_GAME = "GetPlayersInGame";

	private long rttStart;
	public ServerCom(Client client) {
		try {
			this.client = client;
			serverSocket = new Socket(InetAddress.getByName(serverIP), serverPort);
			inputReader = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
			outputWriter = new PrintWriter(serverSocket.getOutputStream(), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			//Notify that this player is running.
			System.out.println("Connection to server established, waiting on communication");

			while (true) {
				String command = inputReader.readLine();

				//inform command received
				System.out.println("command recieved from server, command: " + command);

				//Process command
				if (command.startsWith(COM_QUESTION)) {
					recieveQuestion(command);
				} else if (command.startsWith(COM_DISPLAY_QUESTION)) {
					recieveDisplayQuestion(command);
				} else if (command.startsWith(COM_GAMES)) {
					recieveGames(command);
				} else if (command.startsWith(COM_SERVER_TICKS)) {
					recieveServerTicks(command);
				} else if (command.startsWith(COM_PLAYERS_IN_GAME)) {
					recievePlayers(command);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send a message to the client
	 * @param message String containing message to send
	 */
	private void sendMessage(String message) {
		try {
			outputWriter.println(message);
		} catch (Exception e) {
			//can't communicate with client
			e.printStackTrace();
		}
	}

	public void recieveQuestion(String serializedQuestion) {
		try {
			String questionString = serializedQuestion.substring(COM_QUESTION.length() + 1);
			Question q = Serializer.deserialize(questionString, Question.class);
			client.questionRecieved(q);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void recieveDisplayQuestion(String serializedOffset) {
		try {
			String displayString = serializedOffset.substring(COM_DISPLAY_QUESTION.length() + 1);
			long offset = Long.parseLong(displayString);
			client.displayQuestion(offset);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void recieveGames(String serializedGames) {
		try {
			ArrayList<GameInfo> games = new ArrayList<>();
			String gamesString = serializedGames.substring(COM_GAMES.length() + 1);
			games = Serializer.deserialize(gamesString, games.getClass());
			client.gamesRecieved(games);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public long getServerOffset() {
		rttStart = System.currentTimeMillis();
		sendMessage(COM_GET_SERVER_TICKS);
		return rttStart;
	}
	
	public void recieveServerTicks(String serializedTimestamp) {
		try {
			String serverTicks = serializedTimestamp.substring(COM_SERVER_TICKS.length() + 1);
			long ticks = Long.parseLong(serverTicks);
			client.serverTicksRecieved(ticks);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void recievePlayers(String serializedPlayers) {
		try {
			HashSet<PlayerInfo> playerInfo = new HashSet<>();
			String gamesString = serializedPlayers.substring(COM_PLAYERS_IN_GAME.length() + 1);
			playerInfo = Serializer.deserialize(gamesString, playerInfo.getClass());
			client.playersRecieved(playerInfo);
		} catch (Exception e) {

		}
	}

	public void requestServerTicks() {
		sendMessage(COM_GET_SERVER_TICKS);
	}

	public void requestPlayersInGame() {
		sendMessage(COM_GET_PLAYERS_IN_GAME + " " + gameId);
	}

	public void requestGames() {
		sendMessage(COM_GET_GAMES);
	}

	public void joinGame(int gameId) {
		sendMessage(COM_JOIN_GAME + " " + gameId);
		this.gameId = gameId;
	}

	public void leaveGame() {
		sendMessage(COM_LEAVE_GAME);
	}
	
	public void createNewGame(String name) {
		sendMessage(COM_CREATE_NEW_GAME + " " + name);
	}

	public void ackQuestion() {
		sendMessage(COM_QUESTION_ACK);
	}

	public void questionDone(PlayerAnswer pa) {
		try {
			//serialize object
			String serializedAnswer = Serializer.serialize(pa);
			System.out.println(serializedAnswer);
			sendMessage(COM_QUESTION_DONE + " " + serializedAnswer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setNameOnServer(String name) {
		sendMessage(COM_SET_PLAYER_NAME + " " + name);
	}

}

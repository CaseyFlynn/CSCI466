package com.flynnovations.game.client;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class ClientCom extends Thread {

	private Client client;
	private MulticastSocket clientSocket;
	private InetAddress group;

	private String reservedIP;

	private final int clientPort = Config.Values().getClientPort();
	private final String reservedIPPrefix = "224";
	private final String BUZZ = "BUZZ";
	private final String ANSWER_CORRECT = "AnswerCorrect";
	private final String ANSWER_INCORRECT = "AnswerIncorrect";
	private int gameId;

	
	public ClientCom(Client client, int gameId) {
		
		this.client = client;
		this.gameId = gameId;
	}
	
	public void setGameId(int gameId) {
		ByteBuffer buf = ByteBuffer.allocate(4);
		buf.putInt(gameId);
		byte[] octets = buf.array();
		reservedIP = reservedIPPrefix;
		for (int i=1;i<octets.length;i++){
			reservedIP += "." + octets[i];
		}
		this.gameId = gameId;
		
		//update the multicast group we are listening on
		updateGroup();
	}

	public void leaveGame() {
		if (group != null && clientSocket != null && clientSocket.getInetAddress() != null) {
			try {
				clientSocket.leaveGroup(group);
			} catch (IOException e) {
				// TODO Auto-generated catch block 	
				e.printStackTrace();
			}
		}
	}
	
	private void updateGroup() {
		
		try {
			if (group != null && clientSocket != null) {
				clientSocket.leaveGroup(group);
				System.out.println("Successfully left");
			}
			group = InetAddress.getByName(reservedIP);
			if (clientSocket != null) {
				System.out.println("joining a group");
				clientSocket.joinGroup(group);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void run() {
		try {
			clientSocket = new MulticastSocket(clientPort);
			setGameId(gameId);

			byte[] readBuf = new byte[1024];

			//Main communication loop
			while (true) {
				DatagramPacket rcv = new DatagramPacket(readBuf, readBuf.length);
				clientSocket.receive(rcv);
				String msg = new String(rcv.getData());
				for(int i = 0; i < msg.length(); i++)
				{
					readBuf[i] = 0;
				}
				System.out.println("Message recieved: " + msg);
				if (msg.startsWith(BUZZ)) {
					buzzRecieved(msg);
				} else if (msg.startsWith(ANSWER_CORRECT)) {
					answerRecieved(true, msg);
				} else if (msg.startsWith(ANSWER_INCORRECT)) {
					answerRecieved(false, msg);
				} else {
					//TODO : exception handling.
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendBuzz(String playerName, long serverTime, long serverOffset) {
		client.hasBuzzed = true;
		client.buzzOffset = Math.abs(serverTime - System.currentTimeMillis() - serverOffset);
		String buzzTime = String.format("%019d", client.buzzOffset);
		String msg = BUZZ + " " + buzzTime + " " + playerName;
		sendMessage(msg);
	}

	public void sendAnswer(boolean correct) {
		System.out.println("SENDING answer.");
		client.hasAnswered = true;
		String answer = ((correct) ? ANSWER_CORRECT : ANSWER_INCORRECT) + " " +client.playerName;
		sendMessage(answer);
	}

	private void buzzRecieved(String msg) {
		String paddedBuzz = msg.substring(BUZZ.length() + 1, BUZZ.length() + 1 + 19);
		long buzzOffset = Long.parseLong(paddedBuzz);
		String playerName = msg.substring(BUZZ.length() + 20).trim();

		//do not lock ourselves out
		if (!playerName.equals(client.playerName)) {
			//we will lock out if we have not buzzed, or they buzzed before us
			if (!client.hasBuzzed || (client.buzzOffset > buzzOffset && !client.hasAnswered)) {
				this.client.buzzRecieved(playerName);
				if (client.hasBuzzed) {
					//they buzzed in first, reset buzz metrics
					client.resetBuzz();
				}
			}
		}
	}
	
	private void answerRecieved(boolean isCorrect, String msg) {
		System.out.println("ANSWER RECIEVED");

		int offset = ((isCorrect) ? ANSWER_CORRECT.length() : ANSWER_INCORRECT.length() ) + 1;
		String playerName = msg.substring(offset).trim();
		client.answerRecieved(isCorrect, playerName);
	}
	
	private void sendMessage(String msg) {
		try {
			byte[] msgBuf = msg.getBytes();
			DatagramPacket snd = new DatagramPacket(msgBuf, msgBuf.length, group, clientPort);
			clientSocket.send(snd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

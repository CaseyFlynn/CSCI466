package com.flynnovations.game.client;

import java.io.FileInputStream;
import java.util.*;

public class Config {
	private static Config values;
	private final String propertiesFile = "client.properties";
	private String serverIP;
	private int serverPort;
	private int clientPort;
	
	public String getServerIP() {
		return serverIP;
	}
	
	public int getServerPort() {
		return serverPort;
	}
	
	public int getClientPort() {
		return clientPort;
	}
	
	protected Config() {
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(propertiesFile));
			serverIP = p.getProperty("serverIP", "localhost");
			serverPort = Integer.parseInt(p.getProperty("serverPort", "9991"));
			clientPort = Integer.parseInt(p.getProperty("clientPort", "9992"));
		} catch (Exception e) {
			serverIP = "localhost";
			serverPort = 9991;
			clientPort = 9992;
		}
	}
	
	public static Config Values() {
		if (values == null) {
			values = new Config();
		}
		return values;
	}
}

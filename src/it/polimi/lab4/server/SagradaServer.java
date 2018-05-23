package it.polimi.lab4.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import it.polimi.lab4.server.game.match.Match;
import it.polimi.lab4.server.game.player.Player;

public class SagradaServer {
	
	private static SagradaServer sSagradaServer;
	
	private ServerSocket serverSocket;
	private int port;
	private int timeout;
	private boolean multiMatch;
	private boolean debug;
	private boolean stop = false;
	
	private ArrayList<Match> matches = new ArrayList<>();
	
	public static SagradaServer getInstance() {
		if (sSagradaServer == null) {
			sSagradaServer = new SagradaServer();
		}
		return sSagradaServer;
	}
	
	private SagradaServer() {
		
	}

	public SagradaServer setPort(int port) {
		this.port = port;
		return this;
	}
	
	public SagradaServer setTimeout(int timeout) {
		this.timeout = timeout;
		return this;
	}
	
	public SagradaServer setDebug(boolean debug) {
		this.debug = debug;
		return this;
	}
	
	public SagradaServer setMultiMatch(boolean multiMatch) {
		this.multiMatch = multiMatch;
		return this;
	}
	
	private void addPlayer(Player player) {
		try {
			matches.get(matches.size() -1).addPlayer(player);
		} catch (Exception e) {
			if(multiMatch) {
				matches.add(new Match(timeout, debug));
				addPlayer(player);
			} else {
				System.out.println("Max number of players reached!");
			}
		}
	}
	
	public void start() {
		System.out.println("Starting SagradaServer on port " + port);
		System.out.println("Press ctrl-c to exit");
		try {
			serverSocket = new ServerSocket(port);
		} catch(Exception e) {
			System.err.println("Failed to start the server: " + e.getMessage());
			System.exit(100);
		}
		while(!stop) {
			try {
				final Socket clientSocket = serverSocket.accept();
				if(debug)
					System.out.println("[" + clientSocket.getInetAddress().getHostAddress() + "] Client connected!" );
				if(matches.size() < 1) {
					matches.add(new Match(timeout, debug));
				}
				addPlayer(new Player(clientSocket, debug));
				
			} catch(Exception e) {
				System.err.println("Exeption in main loop: " + e.getMessage() );
			}
		}
		try {
			serverSocket.close();
		} catch(Exception e) {
			//
		}
		
		
	}
}

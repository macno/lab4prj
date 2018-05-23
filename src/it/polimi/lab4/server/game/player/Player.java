package it.polimi.lab4.server.game.player;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.UUID;

public class Player {
	private UUID uuid;
	private Socket socket;
	private BufferedWriter out;
	private BufferedReader in;
	private OnPlayerDisconnectListener disconnetListener;
	private boolean debug;
	private boolean active = false;
	
	private Thread inputReaderThread;
	private Runnable inputReaderRunner = new Runnable() {
		
		public void run() {
			boolean read = true;
			while(read) {
				try {
					String line = in.readLine();
					if(line == null) {
						read = false;
						break;
					}
					if(active) {
						if(debug) {
							System.out.println("[" + uuid.toString() + "] read: \"" + line + "\"");
						}
						parseInput(line);
					}
				} catch(IOException e) {
					System.err.println("Error reading input: " + e.getMessage());
					read = false;
				}
			}
			if(disconnetListener != null) {
				disconnetListener.onDisconnect();
			}
			try {
				socket.close();
			} catch(IOException e) {
				
			}
		}
	};
	
	public Player(Socket socket, boolean debug) {
		this.socket = socket;
		this.debug = debug;
		uuid = UUID.randomUUID();
		try {
			out = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
			in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			inputReaderThread = new Thread(inputReaderRunner);
			inputReaderThread.start();
		} catch(IOException e) {
			
		}

	}
	
	private void parseInput(String input) {
		
	}

	public void setOnPlayerDisconnectListener(OnPlayerDisconnectListener listner) {
		disconnetListener = listner;
	}
	
	public UUID getUUID() {
		return uuid;
	}
	
	synchronized public void send(String what) {
		try {
			out.write(what);
			out.newLine();
			out.flush();
		} catch (IOException e) {
			System.err.println("Error writing to player " + uuid.toString());
			if(disconnetListener != null) {
				disconnetListener.onDisconnect();
			}
		}
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public interface OnPlayerDisconnectListener {
		void onDisconnect();
	}
	
}

package it.polimi.lab4.server.game.match;

import java.util.ArrayList;
import java.util.UUID;

import it.polimi.lab4.server.game.player.Player;
import it.polimi.lab4.server.game.player.Player.OnPlayerDisconnectListener;

public class Match {

	public static final int STATE_IDLE = 0;
	public static final int STATE_WATING_FOR_PLAYERS = 10;
	public static final int STATE_PLAYING = 20;
	public static final int STATE_FINISHED = 90;

	private static final int MAX_PLAYERS = 4;
	private static final int ROUND_TIMEOUT = 10;
	
	private int STATE = STATE_IDLE;	
	
	private int CURRENT_PLAYER = -1;
	
	private UUID uuid;
	private ArrayList<Player> players = new ArrayList<>();
	private int timeout;
	private boolean debug;
	
	private Thread startMatchThread;
	private Runnable startMatchCounter = new Runnable() {
		
		public void run() {
			try {
				if(debug)
					System.out.println("[" + uuid.toString() + "] countdown to start match starts now!" );
				Thread.sleep(timeout*1000);
				if(debug)
					System.out.println("[" + uuid.toString() + "] starting match!" );
	            startMatch();
	        } catch (InterruptedException e) {
	        	System.out.println("[" + uuid.toString() + "] resetting wait timer!" );
	        } catch(Exception e) {
	        	e.printStackTrace();
	        	System.out.println("[" + uuid.toString() + "] exception in startMatchThread: " +e.getMessage()  );
	        }
		}
	};
	
	private Thread roundTimeoutThread;
	private Runnable roundTimeoutRunner = new Runnable() {
		
		public void run() {
			try {
				if(debug)
					System.out.println("[" + uuid.toString() + "] round timeout start" );
				Thread.sleep(ROUND_TIMEOUT*1000);
				if(debug)
					System.out.println("[" + uuid.toString() + "] round timeout expired" );
	            initNextRound();
	        } catch (InterruptedException e) {
	        	System.out.println("[" + uuid.toString() + "] round completed" );
	        } catch(Exception e) {
	        	System.out.println("[" + uuid.toString() + "] exception in roundTimeoutRunner: " +e.getMessage()  );
	        }
		}
	};
	
	public Match(int timeout, boolean debug) {
		this.timeout = timeout;
		this.debug = debug;
		uuid = UUID.randomUUID();
		STATE = STATE_WATING_FOR_PLAYERS;
	}
	
	public void addPlayer(Player player) throws Exception {
		if (STATE > STATE_WATING_FOR_PLAYERS) {
			throw new Exception("Match is started");
		}
		player.setOnPlayerDisconnectListener(new OnPlayerDisconnectListener() {
			
			@Override
			public void onDisconnect() {
				removePlayer(player);
			}
		});
		players.add(player);
		player.send("welcome " + player.getUUID().toString());
		if(debug)
			System.out.println("["+uuid.toString()+"] Player " + player.getUUID().toString()  + " joined");
		int playersCount = getPlayersCount();
		if(playersCount == MAX_PLAYERS) {
			startMatch();
		} else if(playersCount > 1) {
			startWaitForPlayers();
		}
	}
	
	public void removePlayer(Player player) {
		int playerToRemove = players.indexOf(player);
		if(playerToRemove < 0) return;
		players.remove(player);
		if(debug)
			System.out.println("["+uuid.toString()+"] Player " + player.getUUID().toString()  + " left");
		if(STATE < STATE_PLAYING) {
			if(getPlayersCount() < 2) {
				if(startMatchThread != null) {
					startMatchThread.interrupt();
					if(debug)
						System.out.println("["+uuid.toString()+"] stop countdown");
				}
			}
		} else if (STATE == STATE_PLAYING) {
			if(getPlayersCount() == 1) {
				endMatch(players.get(0));
			} else if(playerToRemove == CURRENT_PLAYER) {
				initNextRound();
			}
		}
	}
	
	private int getPlayersCount() {
		return players.size();
	}
	
	private void endMatch(Player winner) {
		if(roundTimeoutThread != null) {
			roundTimeoutThread.interrupt();
		}
		sendToPlayers("match end " + winner.getUUID().toString());
		STATE = STATE_FINISHED;
		if(debug) {
			System.out.println("["+uuid.toString()+"] match finished winner is " + winner.getUUID().toString());
		}
	}
	
	private void startMatch() {
		if(startMatchThread != null) {
			startMatchThread.interrupt();
		}
		STATE = STATE_PLAYING;
		sendToPlayers("match start");
		initNextRound();
	}
	
	private void initNextRound() {
		if(CURRENT_PLAYER >= 0) {
			players.get(CURRENT_PLAYER).setActive(false);
		}
		CURRENT_PLAYER++;
		if(CURRENT_PLAYER >= getPlayersCount()) {
			CURRENT_PLAYER=0;
		}
		Player nextPlayer = players.get(CURRENT_PLAYER);
		nextPlayer.setActive(true);
		String playerUUID = nextPlayer.getUUID().toString();
		sendToPlayers("round start " + playerUUID);
		if(roundTimeoutThread != null) {
			roundTimeoutThread.interrupt();
		}
		roundTimeoutThread = new Thread(roundTimeoutRunner);
		roundTimeoutThread.start();
		if(debug) {
			System.out.println("["+uuid.toString()+"] New round " + playerUUID);
		}
	}
	
	private void startWaitForPlayers() {
		if(startMatchThread != null) {
			startMatchThread.interrupt();
		}
		startMatchThread = new Thread(startMatchCounter);
		startMatchThread.start();
	}
	
	private void sendToPlayers(String what) {
		for(int i = players.size()-1;i>=0;i--) {
			Player player = players.get(i);
			player.send(what);
		}
	}
}

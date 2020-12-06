import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameEngineRepository {
	
	private Map<UUID,GameEngine> games;
	
	
	public GameEngineRepository() {
		this.games = new ConcurrentHashMap<UUID, GameEngine>();
		newGame();
		removeOldGameEngine();
	}
	private synchronized Map<UUID,GameEngine> getGames() {
		return games;
	}
	
	/**
	 * Create a new game
	 * @return UUID of the game created
	 */
	private UUID newGame() {
		GameEngine newGame = new GameEngine(25);
		UUID id = UUID.randomUUID();
		getGames().put(id,newGame);
		return id;
	}
	/**
	 * Check if a game is available to add a player
	 * @param idGame UUID of the game
	 * @return true if available, false if not
	 */
	private boolean isGameAvailable(UUID idGame) {
		GameEngine game = getGames().get(idGame);
		if(game!=null) {
			return game.isGameEngineAvailable();
		}
		return true;
	}

	
	/**
	 * Add a player to the first game available
	 * @param username the username of the player
	 * @return id of the game where the player has been added, null if a problem occured
	 */
	public UUID addPlayerToGame(UUID idPlayer, PlayerInfo player) {
		
		boolean playerAdded = false;
		
		for (UUID idGame : getGames().keySet()) {
			if(isGameAvailable(idGame) ) {
				
				getGames().get(idGame).addPlayer(idPlayer, player);
				return idGame;
			}
		}
		if(!playerAdded) {
			UUID idNewGame = newGame();
			synchronized(GameEngineRepository.this) {
				getGames().get(idNewGame).addPlayer(idPlayer, player);	
			}
			return idNewGame;
		}
		return null;
	}

	/**
	 * Get the PlayArea of a game
	 * @param idGame UUID of the game
	 * @return PlayArea or null if game not found
	 * @throws RemoteException
	 */
	public PlayArea GetPlayArea(UUID idGame){
		GameEngine game = getGames().get(idGame);
		
		if(game != null) {
			return game.getPlayArea();
		}else {
			return null;
		}
	}

	/**
	 * Remove a player from a game
	 * @param idPlayer UUID of the player
	 * @param idGame UUID of the game
	 * @throws RemoteException
	 */
	public void ExitGame(UUID idPlayer, UUID idGame){
		GameEngine game = getGames().get(idGame);
		if(game != null) {
			game.exit(idPlayer);
		}
	}
	
	/**
	 * Get the scoreboard of a game
	 * @param idGame UUID of the game 
	 * @return a Map that contains the scoreboard
	 */
	public Map<String, Integer> getScoreboard(UUID idGame){
		GameEngine game = getGames().get(idGame);
		return game == null ? new HashMap<String,Integer>() : getGames().get(idGame).getScoreboard();
	}


	/**
	 * Check if a game is over
	 * @param idGame UUID of the game
	 * @return true if is over, false if not
	 * @throws RemoteException
	 */
	public boolean isGameOver(UUID idGame){
		GameEngine game = getGames().get(idGame);
		return game.isGameOver();
	}
	
	/**
	 * Send a event to the GameEngine to be analyzed
	 * @param evt GameEvent
	 * @param idPlayer UUID of the player
	 * @param idGame UUID of the game
	 */
	public void treatEvent(GameEvent evt, UUID idPlayer, UUID idGame) {
		GameEngine selectedGame = getGames().get(idGame);
		if(selectedGame!= null) {
			selectedGame.treatEvent(evt, idPlayer);
		}
	}
	
	/**
	 * Change the state of a player
	 * @param idPlayer UUID of the player
	 * @param idGame UUID of the game
	 * @param ready true if the player is ready, false if not
	 */
	public void setPlayerReady(UUID idPlayer, UUID idGame, boolean ready) {
		GameEngine selectedGame = getGames().get(idGame);
		if(selectedGame!= null) {
			selectedGame.setPlayerReady(idPlayer, ready);
		}
	}
	
	/**
	 * Loop that checks the GameEngines and remove those who are useless
	 */
	private void removeOldGameEngine() {
		Runnable r = (()->{
			while(true) {
				for(UUID idGame : getGames().keySet()) {
					if(getGames().get(idGame).isGameEngineUseless()) {
						synchronized(GameEngineRepository.this) {
							getGames().remove(idGame);	
						}
					}
				}
				wait(15000);
			}
		});
		Thread t  = new Thread(r);
		t.start();
	}
	/**
	 * Sleep the current thread
	 * @param ms time to sleep the thread (milisecond)
	 */
	private void wait(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {}
	}

}

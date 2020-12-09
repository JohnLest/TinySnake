import java.nio.channels.SocketChannel;
import java.util.*;

public class GameEngine {
	private PlayArea playboard;
	private Map<UUID, Map<PlayerInfo, Snake>> players;

	// save data about how long the client has been disconnected
	private Map<SocketChannel, Integer> infoClientDc;

	// save data about the state of the players (if they are ready to play)
	private Map<UUID, Boolean> playersReady;

	private Random seed;
	private Coord currentFruit;
	private int nbEmptyCases;
	private boolean gameOver;

	private int timerValueChangeInterval;
	private int timerValue;
	private int minTimerValue;
	private float timerScaleFactor;

	private Timer timer;
	private TimerTask pendingTask;

	private int fruitValue;
	private boolean gameStarted = false;
	private Map<Snake, Boolean> growthManager;
	private List<UUID> initPosUsed;

	public GameEngine(int size) {
		this(size, size);
	}

	public GameEngine(int width, int length) {
		this.playboard = new PlayArea(width, length);
		this.seed = new Random(System.currentTimeMillis());

		initGame();
	}

	/**
	 * Initialization of the game
	 */
	protected void initGame() {
		playboard.init();

		fruitValue = Settings.INIT_FRUIT_VALUE;

		timer = new Timer();
		timerValueChangeInterval = Settings.NB_FRUIT_SPEEDUP;
		timerValue = Settings.INIT_TIMER_VALUE;
		timerScaleFactor = Settings.TIMER_SCALE;
		minTimerValue = Settings.MIN_TIMER_VALUE;
		nbEmptyCases = (playboard.getLength() - 2) * (playboard.getHeight() - 2);
		gameOver = false;
		gameStarted = false;

		infoClientDc = new HashMap<SocketChannel, Integer>();
		playersReady = new HashMap<UUID, Boolean>();
		players = new HashMap<UUID, Map<PlayerInfo, Snake>>();
		growthManager = new HashMap<Snake, Boolean>();

		initPosUsedSnake();

	}

	/**
	 * Add a player to the game
	 * 
	 * @param idPlayer   UUID of the player
	 * @param playerInfo PlayerInfo of the player
	 */
	public void addPlayer(UUID idPlayer, PlayerInfo playerInfo) {

		playerInfo.setScore(0);
		Map<PlayerInfo, Snake> newPlayer = new HashMap<PlayerInfo, Snake>();
		Snake newSnake = getNewSnake(idPlayer);

		if (newSnake != null) {
			for (Coord c : newSnake.getPosition()) {
				playboard.setCaseColour(c, newSnake.getBodyColour());
			}
			playboard.setCaseColour(newSnake.getHeadPosition(), newSnake.getHeadColour());

			nbEmptyCases -= newSnake.getSnakeLength();
			newPlayer.put(playerInfo, newSnake);
			infoClientDc.put(playerInfo.getClientInfo(), 0);
			growthManager.put(newSnake, false);
			playersReady.put(idPlayer, false);
			players.put(idPlayer, newPlayer);
			updateGUI();

			Runnable r = (() -> {
				gameStartAfterTimeout();
			});
			Thread t = new Thread(r);
			t.start();
		}

	}

	/**
	 * Start the game after a deadline if the game is full
	 */
	public void gameStartAfterTimeout() {
		wait(60000);
		if (!gameStarted && !gameOver) {
			if (isGameFull()) {
				start();
			} else {
				gameStartAfterTimeout();
			}
		}
	}

	/**
	 * Check if the game is full
	 * 
	 * @return true if full, false if not
	 */
	public boolean isGameFull() {
		return players.size() >= Settings.NB_PLAYERS;
	}

	/**
	 * Get a snake for a new player
	 * 
	 * @param idPlayer UUID of the player
	 * @return Snake of the new player
	 */
	private Snake getNewSnake(UUID idPlayer) {
		for (int i = 0; i < initPosUsed.size(); i++) {
			if (initPosUsed.get(i) == null) {
				switch (i) {
					case 0: {
						initPosUsed.set(0, idPlayer);
						return new Snake(new Coord(5, 5), Direction.RIGHT);
					}
					case 1: {
						initPosUsed.set(1, idPlayer);
						return new Snake(new Coord(5, 20), Direction.DOWN);
					}
					case 2: {
						initPosUsed.set(2, idPlayer);
						return new Snake(new Coord(20, 20), Direction.LEFT);
					}

					case 3: {
						initPosUsed.set(3, idPlayer);
						return new Snake(new Coord(20, 5), Direction.UP);
					}

					default:
						return null;

				}
			}
		}
		return null;

	}

	/**
	 * Initialize the list that contains the position used by the snakes
	 */
	private void initPosUsedSnake() {
		initPosUsed = new ArrayList<UUID>();
		for (int i = 0; i < Settings.NB_PLAYERS; i++) {
			initPosUsed.add(null);
		}
	}

	/**
	 * Update the gui of the client
	 */
	public void updateGUI() {
		List<SocketChannel> dcClient = new ArrayList<SocketChannel>();
		for (SocketChannel client : infoClientDc.keySet()) {

			Runnable r = (() -> {
				Connect.write(client, 4, Settings.FULL_REFRESH_INTERVAL);
				
				if (!gameStarted) {
					updateGUIPlayersWaitingState(client);
				}
			});

			Thread t = new Thread(r);
			t.start();

		}

		for (SocketChannel client : dcClient) {
			infoClientDc.remove(client);
		}
	}

	/**
	 * Move the snake
	 * 
	 * @param snake the Snake
	 * @return true if safe move, false if not
	 */
	public boolean moveForward(Snake snake) {
		boolean safeMove;
		boolean growth = growthManager.get(snake);

		playboard.setCaseColour(snake.getHeadPosition(), snake.getBodyColour());
		if (!growth)
			playboard.setCaseColour(snake.getTailPosition(), Colour.BACKGROUND);

		snake.moveForward(growth);
		safeMove = playboard.caseIsSafe(snake.getHeadPosition());

		if (!safeMove) {
			Colour colour = playboard.getCaseColour(snake.getHeadPosition());
			switch (colour) {
				case SNAKE_BODY: {
					Snake survivorSnake = checkSnake(snake);

					PlayerInfo survivorPlayer = getPlayerInfoBySnake(survivorSnake);
					PlayerInfo deadPlayer = getPlayerInfoBySnake(snake);
					// snake hits himself if null
					if (survivorPlayer != null) {
						survivorPlayer.addToScore(deadPlayer.getScore());
					}

					removeSnake(snake);
					snake.setIsAlive(false);
					break;
				}
				case SNAKE_HEAD: {
					// the two snakes die
					Snake adv = checkSnake(snake);
					removeSnake(snake);
					removeSnake(adv);
					snake.setIsAlive(false);
					adv.setIsAlive(false);
					break;
				}
				case WALL: {
					snake.setIsAlive(false);
					break;
				}
				default:
					break;
			}

		}
		growthManager.replace(snake, false);

		playboard.setCaseColour(snake.getHeadPosition(), snake.getHeadColour());
		return safeMove;
	}

	/**
	 * Method Check which snake the player eat
	 * 
	 * @param snake
	 */
	private Snake checkSnake(Snake snake) {

		for (UUID id : players.keySet()) {

			Snake otherSnake = getSnakeById(id);

			if (collisionSnake(snake, otherSnake))
				return otherSnake;
		}

		return null;
	}

	/**
	 * Check collision with snake
	 * 
	 * @param snake    first Snake
	 * @param snakeAdv second Snake
	 * @return true if the snakes collided, false if not
	 */
	private boolean collisionSnake(Snake snake, Snake snakeAdv) {
		if (snake.getPosition() == snakeAdv.getPosition())
			return false;

		Coord coordHead = snake.getHeadPosition();
		LinkedList<Coord> snakeAdvCoord = snakeAdv.getPosition();
		for (Coord coord : snakeAdvCoord) {
			if (coordHead.getCol() == coord.getCol() && coordHead.getLine() == coord.getLine())
				return true;
		}
		return false;
	}

	/**
	 * Remove a snake
	 * 
	 * @param snake
	 */
	private void removeSnake(Snake snake) {
		for (Coord coord : snake.getPosition()) {
			playboard.setCaseColour(coord, Colour.BACKGROUND);
		}
	}

	/**
	 * Spawn a new fruit
	 **/
	public void nextFruit() {
		int selectedCase = seed.nextInt(nbEmptyCases);
		int counter = 0;
		for (Coord c : playboard) {
			if (counter == selectedCase && playboard.caseIsEmpty(c)) {
				currentFruit = c;
				playboard.setCaseColour(c, Colour.FRUIT);
				break;
			}

			if (playboard.caseIsEmpty(c))
				counter++;
		}

		nbEmptyCases--;
		adjustTimerValue();
	}

	// game_engine_timer_methods
	private void adjustTimerValue() {
		if (getTotalSnakesSize() % timerValueChangeInterval == 0) {
			float nextTimerValue = (float) timerValue / timerScaleFactor;
			timerValue = nextTimerValue < minTimerValue ? minTimerValue : Math.round(nextTimerValue);
			fruitValue += Settings.FRUIT_INCREASE;
		}
	}

	/**
	 * Calculated the cumulated length of the snakes
	 * 
	 * @return the cumulated length of the snakes
	 */
	private int getTotalSnakesSize() {
		int total = 0;
		for (UUID id : players.keySet()) {
			Map<PlayerInfo, Snake> pData = players.get(id);
			for (PlayerInfo pInfo : pData.keySet()) {
				Snake player = pData.get(pInfo);
				total += player.getSnakeLength();
			}
		}
		return total;
	}

	private void rescheduleTimer() {
		stopTimer();
		pendingTask = new TimerTask() {
			public void run() {
				synchronized (GameEngine.this) {

					for (UUID id : players.keySet()) {
						Map<PlayerInfo, Snake> pData = players.get(id);
						for (PlayerInfo pInfo : pData.keySet()) {
							Snake player = pData.get(pInfo);
							if (player.getIsAlive()) {
								treatEvent(GameEvent.TIMEOUT, id);
							}
						}
					}
				}
			}
		};
		timer.schedule(pendingTask, timerValue);
	}

	public void stopTimer() {
		if (pendingTask != null)
			pendingTask.cancel();
	}

	/**
	 * Start the game engine
	 */
	public void start() {

		notifyGameStarted();

		gameStarted = true;
		nextFruit();
		updateGUI();
		rescheduleTimer();
	}

	/**
	 * Player exits a game
	 * 
	 * @param idPlayer UUID of the player
	 */
	public synchronized void exit(UUID idPlayer) {
		Snake giveUp = getSnakeById(idPlayer);

		if (giveUp != null) {
			if (!gameStarted) {
				players.remove(idPlayer);
				playersReady.remove(idPlayer);
				removeSnake(giveUp);

				for (int i = 0; i < initPosUsed.size(); i++) {
					if (initPosUsed.get(i) != null) {
						if (initPosUsed.get(i).equals(idPlayer))
							initPosUsed.set(i, null);
					}
				}
				updateGUIPlayersWaitingState(null);
				updateGUI();
			}

		}
	}

	// Events Management
	synchronized public void treatEvent(GameEvent event, UUID idPlayer) {
		Snake snake = getSnakeById(idPlayer);

		if (gameOver)
			return;

		switch (event) {
			case TIMEOUT:
				if (moveForward(snake)) {
					rescheduleTimer();
					if (currentFruit.equals(snake.getHeadPosition())) {
						updateScore(idPlayer);
						nextFruit();
						growthManager.replace(snake, true);
					}
				} else {
					if (isGameOver()) {
						timer.cancel();
					}
				}
				updateGUI();
				break;
			case UP:
				snake.setDirection(Direction.UP);
				break;
			case DOWN:
				snake.setDirection(Direction.DOWN);
				break;
			case RIGHT:
				snake.setDirection(Direction.RIGHT);
				break;
			case LEFT:
				snake.setDirection(Direction.LEFT);
				break;
			default:
				// should never occur
		}
	}

	/**
	 * Find a snake based on the id of the player
	 * 
	 * @param idPlayer UUID of the player
	 * @return Snake of the player if found, null otherwise
	 */
	private Snake getSnakeById(UUID idPlayer) {
		Map<PlayerInfo, Snake> playerData = players.get(idPlayer);
		if (playerData != null) {
			for (PlayerInfo pInfo : playerData.keySet()) {
				return playerData.get(pInfo);
			}
		}
		return null;
	}

	/**
	 * Find a PlayerInfo based on the id of the player
	 * 
	 * @param idPlayer UUID of the player
	 * @return PlayerInfo of the player if found, null otherwise
	 */
	private PlayerInfo getPlayerById(UUID idPlayer) {
		Map<PlayerInfo, Snake> playerData = players.get(idPlayer);
		if (playerData != null) {
			for (PlayerInfo pInfo : playerData.keySet()) {
				return pInfo;
			}
		}
		return null;
	}

	/**
	 * Find a PlayerInfo based on a Snake
	 * 
	 * @param snake
	 * @return PlayerInfo of the snake if found, null otherwise
	 */
	private PlayerInfo getPlayerInfoBySnake(Snake snake) {
		for (UUID id : players.keySet()) {
			Map<PlayerInfo, Snake> infoPlayerMap = players.get(id);
			if (infoPlayerMap != null) {
				for (PlayerInfo pInf : infoPlayerMap.keySet()) {
					if (infoPlayerMap.get(pInf) == snake)
						return pInf;
				}
			}
		}
		return null;
	}

	/**
	 * Update the score of a player
	 * 
	 * @param idPlayer UUID of a player
	 */
	private void updateScore(UUID idPlayer) {
		PlayerInfo player = getPlayerById(idPlayer);
		if (player != null) {
			player.addToScore(fruitValue);
		}
	}

	/**
	 * Check if the game engine is useless and can be deleted
	 * 
	 * @return true if can be deleted, false otherwise
	 */
	public boolean isGameEngineUseless() {
		return players.size() == 0 || gameOver;
	}

	/**
	 * Check if a player can be added to the game
	 * 
	 * @return true if it can, false if not
	 */
	public boolean isGameEngineAvailable() {
		return !isGameFull() && !gameOver;
	}

	// Getters

	public PlayArea getPlayArea() {
		return playboard;
	}

	/**
	 * Check if the game is over
	 * 
	 * @return true if the game is over, false if not
	 */
	@SuppressWarnings("unused")
	public boolean isGameOver() {
		if (gameOver)
			return true;

		int cpt = 0;
		int nbPlayerGameOver = 1;
		if (Settings.NB_PLAYERS == 1) {
			nbPlayerGameOver = 0;
		}

		for (UUID id : players.keySet()) {
			if (getSnakeById(id).getIsAlive())
				++cpt;
		}

		if (cpt <= nbPlayerGameOver && gameStarted) {
			for (UUID id : players.keySet()) {
				// add 20 point to the survivor
				if (getSnakeById(id).getIsAlive()) {
					getPlayerById(id).addToScore(20);
					gameOver = true;
					gameStarted = false;
					return true;
				}
			}
			gameOver = true;
			return true;
		}

		return false;
	}

	/**
	 * Get the scoreboard of the GameEngine
	 * 
	 * @return a map that contains the names and the scores of the players
	 */
	public Map<String, Integer> getScoreboard() {
		Map<String, Integer> scoreboard = new HashMap<String, Integer>();
		for (UUID idPlayer : players.keySet()) {
			String name = getPlayerById(idPlayer).getName();
			int score = getPlayerById(idPlayer).getScore();
			scoreboard.put(name, score);
		}
		return scoreboard;
	}

	/**
	 * Set a player ready and start the game if the game is full and all players are
	 * ready
	 * 
	 * @param idPlayer UUID of the player
	 * @param ready    true if the player is ready, false if not
	 */
	public void setPlayerReady(UUID idPlayer, boolean ready) {
		playersReady.replace(idPlayer, ready);
		updateGUIPlayersWaitingState(null);

		if (isGameFull() && areAllPlayersReady())
			start();
	}

	/**
	 * Check if all the players are ready
	 * 
	 * @return true if all the players are ready, false if not
	 */
	private boolean areAllPlayersReady() {
		for (UUID id : playersReady.keySet()) {
			if (playersReady.get(id) == false)
				return false;
		}

		return true;
	}

	/**
	 * Send a notification to the client to update the waiting panel
	 * 
	 * @param client
	 */
	private void updateGUIPlayersWaitingState(SocketChannel client) {

		String message = "";

		if (!areAllPlayersReady()){
			message = "All players are not ready";
		}
			

		if (!isGameFull()) {
			int missingPlayers = Settings.NB_PLAYERS - players.size();
			message = "Waiting for " + missingPlayers + " more player(s)";
		}

		if (client != null) {
			LinkedList body = new LinkedList<Object>();
			body.add(convertPlayerReadyIdToName());
			body.add(message);
			Connect.write(client, 2, body);
		} else {
			for (SocketChannel cli : infoClientDc.keySet()) {
				LinkedList body = new LinkedList<Object>();
				body.add(convertPlayerReadyIdToName());
				body.add(message);
				Connect.write(cli, 2, body);

			}

		}

	}

	/**
	 * Convert the ids of the players to their names
	 * 
	 * @return a Map that contains the names of the players and if they are ready
	 */
	private Map<String, Boolean> convertPlayerReadyIdToName() {
		Map<String, Boolean> data = new HashMap<String, Boolean>();

		// convert uuid to player name
		for (UUID id : playersReady.keySet()) {
			data.put(getPlayerById(id).getName(), playersReady.get(id));
		}
		return data;
	}

	/**
	 * Send a notification to the client to tell that the game has started
	 */
	private void notifyGameStarted() {
		for (SocketChannel cli : infoClientDc.keySet()) {
			Connect.write(cli, 3, true);
		}
	}

	/**
	 * Sleep the current thread
	 * 
	 * @param ms time to sleep the thread (milisecond)
	 */
	private void wait(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
		}
	}

	public Coord getCurrentFruit() {
		return currentFruit;
	}

	public String toString() {
		return playboard.toString();
	}
}

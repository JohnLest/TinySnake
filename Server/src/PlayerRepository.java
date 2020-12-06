import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerRepository {

	private Map<UUID, PlayerInfo> players;

	public PlayerRepository() {
		init();
	}

	/**
	 * Initialize the data
	 */
	private void init() {
		players = new ConcurrentHashMap<UUID, PlayerInfo>();
	}

	private synchronized Map<UUID, PlayerInfo> getPlayers() {
		return players;
	}

	/**
	 * Create new player and add it to the repository
	 * 
	 * @param id     uuid of the client
	 * @param client the callback interface of the client
	 * @param name   the name of the player
	 * @return Empty string if ok, string that explains the error otherwise
	 */
	public String newPlayer(UUID id, SocketChannel client, String name) {

		if (checkIfNameAlreadyUsed(name)) {
			return "Name already used";
		} else {
			PlayerInfo pInfo = new PlayerInfo(name);
			pInfo.setClientInfo(client);
			synchronized (PlayerRepository.this) {
				getPlayers().put(id, pInfo);
			}
			;

			return "";
		}
	}

	/**
	 * Check if the name of a player is already used
	 * 
	 * @param name the name of the player
	 * @return true if already used, false if not
	 */
	private boolean checkIfNameAlreadyUsed(String name) {
		for (UUID idPlayer : getPlayers().keySet()) {
			if (getPlayers().get(idPlayer).getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if an id match to a CallbackClientInterface
	 * 
	 * @param id     UUID of a player
	 * @param client CallbackClientInterface of a client
	 * @return true if it matches, false if not
	 */
	public boolean compareIdAndClient(UUID id, SocketChannel client) {

		PlayerInfo pInfo = getPlayers().get(id);
		if (pInfo != null) {
			return pInfo.getClientInfo() == client;
		}

		return false;
	}

	public PlayerInfo getPlayerInfo(UUID id) {
		return getPlayers().get(id);

	}

	/**
	 * Remove a player from the repository
	 * 
	 * @param idPlayer UUID of the player
	 */
	public void disconnectPlayer(UUID idPlayer) {
		synchronized (PlayerRepository.this) {
			getPlayers().remove(idPlayer);
		}
		;
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

}

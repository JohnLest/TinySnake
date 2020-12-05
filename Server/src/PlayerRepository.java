import java.rmi.RemoteException;
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
	 * @param id   uuid of the client
	 * @param name the name of the player
	 * @return Empty string if ok, string that explains the error otherwise
	 */
	public PlayerInfo newPlayer(UUID id, String name) {
		PlayerInfo pInfo = new PlayerInfo(name, id);
		synchronized (PlayerRepository.this) {
			getPlayers().put(id, pInfo);
		}
		;
		return pInfo;

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
}

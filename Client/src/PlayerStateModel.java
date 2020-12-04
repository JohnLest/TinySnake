
/**
 *  Model used to format information about the state of a player from the server
 * 	@author Louis
 */
public class PlayerStateModel {
	private String username;
	private boolean ready;

	public PlayerStateModel(String username, boolean ready) {
		this.username = username;
		this.ready = ready;
	}

	public String getUsername() {
		return username;
	}

	/**
	 * Convert the state from boolean to string
	 * @return a string that tells if a player is ready or not
	 */
	public String getState() {
		if(ready)
			return "Ready";
		return "Not ready";
	}

}

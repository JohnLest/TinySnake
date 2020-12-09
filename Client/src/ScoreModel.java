
/**
 *  Model used to format information about a score from the server
 *  @author Louis
 *
 */
public class ScoreModel {

	private String username;
	private int score;

	public ScoreModel(String username, int score) {
		this.username = username;
		this.score = score;
	}

	public String getUsername() {
		return username;
	}

	public int getScore() {
		return score;
	}

}

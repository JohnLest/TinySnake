import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

/**
 * Model used to format data about scores into an AbstractTableModel
 * @author louis
 *
 */
public class ScoreboardTableModel extends AbstractTableModel {

	private List<ScoreModel> scores;
	private final String[] entetes = { "Pseudo", "Points" };

	public ScoreboardTableModel(Map<String, Integer> scoresMap) {
		super();
		this.scores = new ArrayList<ScoreModel>();
		for(String username : scoresMap.keySet()) {
			int scorePlayer = scoresMap.get(username);
			this.scores.add(new ScoreModel(username, scorePlayer));
		}
		this.scores.sort((s1, s2) -> Integer.valueOf((s2.getScore())).compareTo(Integer.valueOf(s1.getScore())));
	}

	@Override
	public int getRowCount() {
		return scores.size();
	}

	@Override
	public int getColumnCount() {
		return entetes.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
			case 0:
				return scores.get(rowIndex).getUsername();
			case 1:
				return scores.get(rowIndex).getScore();
			default:
				return null;
		}
	}

}

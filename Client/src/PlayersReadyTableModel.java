import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

/**
 * Model used to format data about players states into an AbstractTableModel
 * @author Louis
 */
public class PlayersReadyTableModel extends AbstractTableModel{
	private List<PlayerStateModel> playersState;
	private final String[] entetes = { "Pseudo", "Points" };

	public PlayersReadyTableModel(Map<String, Boolean> playersStateMap) {
		super();
		this.playersState = new ArrayList<PlayerStateModel>();
		for(String username : playersStateMap.keySet()) {
			boolean statePlayer = playersStateMap.get(username);
			
			this.playersState.add(new PlayerStateModel(username, statePlayer));
		}
	}

	@Override
	public int getRowCount() {
		return playersState.size();
	}

	@Override
	public int getColumnCount() {
		return entetes.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
			case 0:
				return playersState.get(rowIndex).getUsername();
			case 1:
				return playersState.get(rowIndex).getState();
			default:
				return null;
		}
	}

}

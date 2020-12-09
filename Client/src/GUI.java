
//--> GUI_class
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

/**
 * Core Class of the GUI
 */
public class GUI {
	private Server server;
	private JFrame frame;
	private JPanel gamePane;
	private JPanel settingPane;
	private PlayBoard playBoard;
	private JButton doneButton;
	private JTextField nameField;
	private PlayArea board;
	private JTable scoreboard;
	private JPanel scorePanel;
	private WaitingPanel wp;
	private JButton newGameButton;
	private SocketChannel socket;
	private int caseSize;
	private int casesPerSide;
	private boolean gameOver = false;

	private int nbPartialRefresh = 0;

	/**
	 * Creates a new GUI based on a Game Engine, a size of a square and the number
	 * of cases in a side.
	 *
	 * @param ge           The GameEngine managing the events, the moves of the
	 *                     snake...
	 * @param caseSize     The size of a single square in the GUI
	 * @param casesPerSide The length of the PlqyArea side given in number of
	 *                     squqres
	 *
	 * @see GameEngine
	 * @see PlayArea
	 */
	public GUI(int caseSize, int casesPerSide, Server server) {
		this.caseSize = caseSize;
		this.casesPerSide = casesPerSide;
		this.server = server;
		try {
			this.socket = server.Connection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		board = new PlayArea(casesPerSide);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createGUI();
			}
		});
	}

	/**
	 * Refresh the GUI: mostly performs partial refresh, but refresh the full array
	 * at regular intervals
	 */
	public void update(int refreshInterval) {
		if (App.idGame != null && App.idUser != null) {
			Server.write(socket, 4, App.idGame);
			nbPartialRefresh = (nbPartialRefresh + 1) % refreshInterval;

			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					Server.write(socket, 5, App.idGame);
				}
			});
		}

	}

	private void updateScoreBoard(Map<String, Integer> scores) {
		ScoreboardTableModel sbModel = new ScoreboardTableModel(scores);
		scorePanel.removeAll();
		scoreboard = new JTable(sbModel);
		scorePanel.add(scoreboard);
		scorePanel.revalidate();
		scorePanel.repaint();
	}

	/**
	 * GUI creation, this method is called by the constructor
	 *
	 * @param caseSize     The size of a single square in the GUI
	 * @param casesPerSide The length of the PlayArea side given in number of
	 *                     squares
	 */
	private void createGUI() {
		frame = new JFrame("Tiny Snake");
		buildGamePaneWidgets();
		buildSettingPaneWidgets();

		showSettingPane();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation(100, 100);
		frame.setVisible(true);
	}

	/**
	 * Creates the pane that will display the board
	 *
	 * @param caseSize     The size of a single square in the GUI
	 * @param casesPerSide The length of the PlayArea side given in number of
	 *                     squares
	 */
	private void buildGamePaneWidgets() {
		playBoard = new PlayBoard(caseSize, casesPerSide, this, socket);

		playBoard.addKeyListener(new KeyboardHandler(socket));

		// init game pane
		scorePanel = new JPanel();
		scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.Y_AXIS));

		// other components for labels here
		scoreboard = new JTable();
		scorePanel.add(scoreboard);

		newGameButton = new JButton("New game");
		newGameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				manageRestartGameRequest();
			}
		});
		newGameButton.setVisible(false);

		JPanel bottomPanel = new JPanel(new FlowLayout());
		bottomPanel.add(new ExitButton(socket));
		bottomPanel.add(newGameButton);

		gamePane = new JPanel(new BorderLayout());
		gamePane.setOpaque(true);
		gamePane.add(playBoard, BorderLayout.CENTER);
		gamePane.add(scorePanel, BorderLayout.EAST);
		gamePane.add(new ExitButton(socket), BorderLayout.PAGE_END);
		gamePane.add(bottomPanel, BorderLayout.PAGE_END);

		wp = new WaitingPanel(socket);
	}

	/**
	 * Creates the Settings Pane
	 */
	private void buildSettingPaneWidgets() {
		// init setting/welcome pane
		settingPane = new JPanel(new GridLayout(0, 2));
		settingPane.setOpaque(true);

		nameField = new JTextField(15);
		doneButton = new JButton("Done");
		doneButton.setMnemonic(KeyEvent.VK_D);
		frame.getRootPane().setDefaultButton(doneButton);
		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (nameField.getText().length() != 0) {
					manageConnectionRequest();
				} else {
					showErrorMessage("Enter a name");
				}
			}
		});

		settingPane.add(new JLabel("TinySnake"));
		settingPane.add(new JLabel());
		settingPane.add(new JLabel("Your name:"));
		settingPane.add(nameField);
		settingPane.add(new ExitButton(socket));
		settingPane.add(doneButton);
	}

	/**
	 * Start a connection request and manage the interface while waiting for the
	 * response
	 */
	private void manageConnectionRequest() {
		doneButton.setEnabled(false);
		wp = new WaitingPanel(socket);
		Server.write(socket, 1, nameField.getText());

		Runnable r = (() -> {
			while (server != null) {
				Dictionary result = server.read(socket);
				analyseMsg(result);
			}
		});
		Thread t = new Thread(r);
		t.start();
		wp.showPanel(true);
		showGamePane();
		update(20);
	}

	/**
	 * Start a request to restart a game and manage the interface while waiting for
	 * the response
	 */
	private void manageRestartGameRequest() {
		Server.write(socket, 8, App.idUser);
		Runnable r = (() -> {
			newGameButton.setEnabled(false);
			wp.showPanel(true);
			showGamePane();
			update(20);
			showRestartButton(false);
		});

		Thread t = new Thread(r);
		t.start();

	}

	public void gameStarted() {
		playBoard.requestFocus();
	}

	/**
	 * Toggle display of the full game pane
	 */
	public void showGamePane() {
		frame.setContentPane(gamePane);

		playBoard.requestFocus();

		frame.setMinimumSize(new Dimension(644, 0));
		frame.pack();
	}

	/**
	 * Toggle display of the setting pane
	 */
	public void showSettingPane() {
		frame.setContentPane(settingPane);
		frame.getRootPane().setDefaultButton(doneButton);
		frame.pack();
	}

	/**
	 * Hide / show the restart button
	 * 
	 * @param visible true to show, false to hide
	 */
	public void showRestartButton(boolean visible) {
		newGameButton.setEnabled(visible);
		newGameButton.setVisible(visible);
	}

	/**
	 * Copy a String in a buffer and fill the buffer with padding
	 *
	 * @param origString The string to copy
	 * @param totalSize  The size of the buffer
	 * @param pad        The padding character
	 */
	private String fillString(String origString, int totalSize, char pad) {
		StringBuffer stringFill = new StringBuffer();
		for (int i = origString.length(); i <= totalSize; i++)
			stringFill.append(pad);

		return stringFill.toString().concat(origString);
	}

	/**
	 * Reset NbPartialRefresh to 0
	 */
	public void resetNbPartialRefresh() {
		nbPartialRefresh = 0;
	}

	public PlayArea getBoard() {
		return board;
	}

	public WaitingPanel getWaitingPanel() {
		return wp;
	}

	public Boolean isGameOver() {
		return gameOver;
	}

	/**
	 * Display a popup with a message
	 * 
	 * @param errorMessage the message to display
	 */
	private void showErrorMessage(String errorMessage) {
		if (!errorMessage.equals("")) {
			JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void analyseMsg(Dictionary stream) {
		int headVal = 0;
		for (Enumeration head = stream.keys(); head.hasMoreElements();)
			headVal = (int) head.nextElement();
		Object body = stream.get(headVal);
		switch (headVal) {
			case 1:
				Map<UUID, UUID> body1 = (Map) body;
				for (UUID first : body1.keySet()) {
					App.idUser = first;
					App.idGame = body1.get(first);
					break;
				}
				break;
			case 2:
				LinkedList<Object> body2 = (LinkedList) body;
				Map<String, Boolean> waiter = (Map) body2.get(0);
				String msg = (String) body2.get(1);
				wp.updatePlayersState(waiter, msg);
				break;
			case 3:
				if ((Boolean) body) {
					wp.showPanel(false);
					gameStarted();
				}
				break;
			case 4:
				update((int) body);
				break;
			case 5:
				PlayArea body5 = (PlayArea) body;
				if (nbPartialRefresh == 0) {
					board.copy(body5);
				} else {
					body5.updateCopy(board);
				}
				frame.repaint();
				break;
			case 6:
				Map<String, Integer> body6 = (Map) body;
				updateScoreBoard(body6);
				frame.repaint();
				break;
			case 7:
				gameOver = (boolean) body;
				break;
			case 8:
				App.idGame = (UUID) body;
				break;
		}
	}

}

// --<
// --> playboard_class

/**
 * The PlayBoard representation in the GUI
 */
class PlayBoard extends JPanel {
	private int caseSize;
	private int casesPerSide;
	private GUI gui;
	private SocketChannel socket;

	/**
	 * Creates the PlayBoard panel based on single square size and lengh of the
	 * PlayArea side in number of squares
	 *
	 * @param caseSize     The single square size
	 * @param casesPerSide The length of PlayArea side
	 */
	public PlayBoard(int caseSize, int casesPerSide, GUI gui, SocketChannel socket) {
		super();
		int sideSize = (caseSize - 1) * casesPerSide + 1;
		setPreferredSize(new Dimension(sideSize, sideSize));
		this.caseSize = caseSize;
		this.casesPerSide = casesPerSide;
		this.gui = gui;
		this.socket = socket;
	}

	/**
	 * Print the contents of the board on Graphic output
	 *
	 * @param g the graphic output
	 */
	public void paintComponent(Graphics g) {
		PlayArea playArea = gui.getBoard();
		super.paintComponent(g);
		if(App.idGame != null)
			Server.write(socket, 6, App.idGame);
		g.setColor(ColourMapper.map(Colour.BACKGROUND));
		g.fillRect(0, 0, (caseSize - 1) * casesPerSide + 1, (caseSize - 1) * casesPerSide + 1);

		int pixCoeff = caseSize - 1;

		for (Coord pos : playArea) {
			int pixX = pos.getCol() * pixCoeff;
			int pixY = pos.getLine() * pixCoeff;
			Colour caseColour = playArea.getCaseColour(pos);
			if (caseColour != Colour.BACKGROUND) {
				new BoardCase(pixX, pixY, ColourMapper.map(caseColour)).drawCase(g);
			}
		}
		if (gui.isGameOver()) {
			gameOver(g);
			gui.showRestartButton(true);
			gui.resetNbPartialRefresh();
		}
	}

	/**
	 * Print GameOver on screen
	 *
	 * @param g the graphic output
	 */
	private void gameOver(Graphics g) {
		g.setColor(Color.MAGENTA);
		g.setFont(new Font(null, Font.BOLD, 50));
		g.drawString("GAME OVER", (int) ((casesPerSide / 2.0f - 8.0) * (caseSize - 1)),
				(int) ((casesPerSide / 2.0f + 0.5) * (caseSize - 1)));
	}

	/**
	 * Represents a case to draw
	 */
	class BoardCase {
		int pixX;
		int pixY;
		Color fill;

		/**
		 * Create a new case base on top left corner coordinate and fill colour
		 *
		 * @param pixX the abscissa of the top left corner
		 * @param pixY The ordinate of the top left corner
		 * @param fill The fill colour of the square
		 */
		public BoardCase(int pixX, int pixY, Color fill) {
			this.pixX = pixX;
			this.pixY = pixY;
			this.fill = fill;
		}

		public void drawCase(Graphics g) {
			g.setColor(fill);
			g.fillRect(pixX + 1, pixY + 1, caseSize - 2, caseSize - 2);
		}

	}
}

// --<
// --> next_piece_panel_class

/**
 * The Exit Button
 */
class ExitButton extends JButton {

	ExitButton(SocketChannel socket) {
		super("Exit");
		setMnemonic(KeyEvent.VK_E);
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				LinkedList body = new LinkedList();
				body.add(App.idUser);
				body.add(App.idGame);
				Server.write(socket, 7, body);
				System.exit(0);
			}
		});
	}
}

/**
 * The Keyboard Handler
 */
class KeyboardHandler extends KeyAdapter {

	private SocketChannel socket;

	public KeyboardHandler(SocketChannel socket) {
		this.socket = socket;
	}

	/**
	 * Transforn a keyboard event into a GameEvent
	 *
	 * @param e The keyboard event
	 */
	public void keyPressed(KeyEvent e) {
		GameEvent event = null;
		switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
				event = GameEvent.UP;
				break;
			case KeyEvent.VK_DOWN:
				event = GameEvent.DOWN;
				break;
			case KeyEvent.VK_LEFT:
				event = GameEvent.LEFT;
				break;
			case KeyEvent.VK_RIGHT:
				event = GameEvent.RIGHT;
				break;
			default:
				return;
		}
		LinkedList<Object> bodyMsg = new LinkedList();
		bodyMsg.add(App.idUser);
		bodyMsg.add(App.idGame);
		bodyMsg.add(event);
		Server.write(socket, 3, bodyMsg);

	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}
}

/**
 * Simple Colour Mappep
 */
class ColourMapper {
	/**
	 * Return a GUI readable color based on Colour enumeration
	 *
	 * @param colour The value of the Colour Enumeration to translate
	 */
	public static Color map(Colour colour) {
		switch (colour) {
			case FRUIT:
				return Color.RED;
			case SNAKE_BODY:
				return Color.GREEN;
			case SNAKE_HEAD:
				return Color.YELLOW;
			case WALL:
				return Color.DARK_GRAY;
			case BACKGROUND:
			default:
				return Color.LIGHT_GRAY;
		}
	}
}

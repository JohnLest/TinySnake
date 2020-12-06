
//--> GUI_class
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

public class GUI {
    // #region GUI Part
    private JFrame frame;
    private JPanel settingPane;
    private JTextField nameField;
    private JButton doneButton;
    private WaitingPanel wp;
    private int caseSize; 
    private int casesPerSide;
    private PlayBoard playBoard;
    private PlayArea board;
    private JTable scoreboard;
    private JPanel scorePanel;
    private JButton newGameButton;
    private JPanel gamePane;
    // #endregion

    // #region Server Part
    private Server serv;
    private SocketChannel socket;
    // #endregion

    public GUI(Server serv, int caseSize, int casesPerSide) {
        this.serv = serv;
        this.caseSize = caseSize;
        this.casesPerSide = casesPerSide;
        board = new PlayArea(casesPerSide);
        createGUI();
    }

    // #region GUI Part
    /**
     * GUI creation, this method is called by the constructor
     *
     * @param caseSize     The size of a single square in the GUI
     * @param casesPerSide The length of the PlayArea side given in number of
     *                     squares
     */
    private void createGUI() {
        frame = new JFrame("Tiny Snake");
        buildSettingPaneWidgets();
        showSettingPane();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(100, 100);
        frame.setVisible(true);
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
        doneButton.addActionListener(event -> doneButtonClick());
        settingPane.add(new JLabel("TinySnake"));
        settingPane.add(new JLabel());
        settingPane.add(new JLabel("Your name:"));
        settingPane.add(nameField);
        // settingPane.add(new ExitButton("serverConnection"));
        settingPane.add(doneButton);
    }

    /**
     * Toggle display of the setting pane
     */
    public void showSettingPane() {
        frame.setContentPane(settingPane);
        frame.getRootPane().setDefaultButton(doneButton);
        frame.pack();
    }


    private void buildGamePaneWidgets() 
    {
        
        playBoard = new PlayBoard(caseSize, casesPerSide, this);
        
        
	    //playBoard.addKeyListener(new KeyboardHandler(this));

	    // init game pane
	    scorePanel = new JPanel();
	    scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.Y_AXIS));
	    
	    // other components for labels here
	    scoreboard = new JTable();
	    scorePanel.add(scoreboard);
	    
	    
        newGameButton = new JButton("New game");
        /*
	    newGameButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent event) 
            {
		    	manageRestartGameRequest();
		    }
        });
        */
	    newGameButton.setVisible(false);

	    JPanel bottomPanel = new JPanel(new FlowLayout());
	    //bottomPanel.add(new ExitButton(serverConnection));
	    bottomPanel.add(newGameButton);

	    gamePane = new JPanel(new BorderLayout());
	    gamePane.setOpaque(true);
	    gamePane.add(playBoard, BorderLayout.CENTER);
	    gamePane.add(scorePanel, BorderLayout.EAST);
	    //gamePane.add(new ExitButton(serverConnection), BorderLayout.PAGE_END);
	    gamePane.add(bottomPanel, BorderLayout.PAGE_END);
    }

    /**
     * Toggle display of the full game pane
     */
    public void showGamePane() 
    {
	    frame.setContentPane(gamePane);
	    
	    playBoard.requestFocus();
	   
	    frame.setMinimumSize(new Dimension(644, 0));
	    frame.pack();
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

    public PlayArea getBoard() 
    {
	    return board;
    }

    // #region Event Part
    private void doneButtonClick() {
        if (nameField.getText().length() != 0) {
            manageConnectionRequest();
            buildGamePaneWidgets();
            wp.showPanel(true);
            showGamePane();
        } else {
            showErrorMessage("Enter a name");
        }
    }
    // #endregion

    // #endregion

    // #region Server Part
    private void manageConnectionRequest() {
        try {
            this.socket = serv.Connection();
            wp = new WaitingPanel(serv, socket);
            serv.write(socket, 1, nameField.getText());

            Runnable r = (() -> {
                while (serv != null) {
                   Dictionary result =  serv.read(socket);
                   analyseMsg(result);
                }
            });
            Thread t = new Thread(r);
            t.start();
        } catch (IOException e) {
            showErrorMessage("Erreur de Connection");
            e.printStackTrace();
        }
    }

    public void analyseMsg(Dictionary stream) {
        int headVal = 0;
        for (Enumeration head = stream.keys(); head.hasMoreElements();)
            headVal = (int) head.nextElement();

        switch (headVal) {
            case 1:
                LinkedList<PlayerInfo> playerLst = (LinkedList) stream.get(1);
                Map waiter = new HashMap();
                for (PlayerInfo player : playerLst) {
                    waiter.put(player.getName(), player.getReady());
                }
                wp.updatePlayersState(waiter, "");
                break;
        }
    }
    // #endregion
}

/**
 * The PlayBoard representation in the GUI
 */
class PlayBoard extends JPanel 
{
    private int caseSize;
    private int casesPerSide;
    private GUI gui;

    /**
     * Creates the PlayBoard panel based on single square size and lengh of the PlayArea side
     * in number of squares
     *
     * @param caseSize The single square size
     * @param casesPerSide The length of PlayArea side
     */
    public PlayBoard(int caseSize, int casesPerSide, GUI gui) 
    {
	    super();
	    int sideSize = (caseSize - 1) * casesPerSide + 1;
	    setPreferredSize(new Dimension(sideSize, sideSize));
	    this.caseSize = caseSize;
	    this.casesPerSide = casesPerSide;
	    this.gui = gui;
    }

    /**
     * Print the contents of the board on Graphic output
     *
     * @param g the graphic output
     */
    public void paintComponent(Graphics g) 
    {
	    PlayArea playArea = gui.getBoard();
	    super.paintComponent(g);
	    g.setColor(ColourMapper.map(Colour.BACKGROUND));
	    g.fillRect(0, 
		        0, 
		        (caseSize - 1) * casesPerSide + 1, 
		        (caseSize - 1) * casesPerSide + 1);

	    int pixCoeff = caseSize - 1;

	    for (Coord pos : playArea) {
	        int pixX = pos.getCol() * pixCoeff;
	        int pixY = pos.getLine() * pixCoeff;
	        Colour caseColour = playArea.getCaseColour(pos);
	        if (caseColour != Colour.BACKGROUND) 
            {
		        new BoardCase(pixX, pixY, ColourMapper.map(caseColour)).drawCase(g);
	        }
        }
        /*
	    if (server.isGameOver()) {
	    	
	        gameOver(g);
	        gui.showRestartButton(true);
	        gui.resetNbPartialRefresh();
        }
        */
    }

    /**
     * Print GameOver on screen
     *
     * @param g the graphic output
     */
    private void gameOver(Graphics g) 
    {
	    g.setColor(Color.MAGENTA);
	    g.setFont(new Font(null, Font.BOLD, 50));
	    g.drawString("GAME OVER",
		        (int) ((casesPerSide / 2.0f - 8.0) * (caseSize - 1)),
		        (int) ((casesPerSide / 2.0f + 0.5) * (caseSize - 1)));
    }

    /**
     * Represents a case to draw
     */
    class BoardCase 
    {
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
	    switch(colour) {
	        case FRUIT: return Color.RED;
	        case SNAKE_BODY: return Color.GREEN;
	        case SNAKE_HEAD: return Color.YELLOW;
	        case WALL: return Color.DARK_GRAY;
	        case BACKGROUND: 
	        default: return Color.LIGHT_GRAY;
	    }
    }
}

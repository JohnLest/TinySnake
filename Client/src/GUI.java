
//--> GUI_class
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GUI {
    // #region GUI Part
    private JFrame frame;
    private JPanel settingPane;
    private JTextField nameField;
    private JButton doneButton;
    private WaitingPanel wp;
    // #endregion

    // #region Server Part
    private Server serv;
    private SocketChannel socket;
    // #endregion

    public GUI(Server serv) {
        this.serv = serv;
        createGUI(0, 0);
    }

    // #region GUI Part
    /**
     * GUI creation, this method is called by the constructor
     *
     * @param caseSize     The size of a single square in the GUI
     * @param casesPerSide The length of the PlayArea side given in number of
     *                     squares
     */
    private void createGUI(int caseSize, int casesPerSide) {
        frame = new JFrame("Tiny Snake");
        buildSettingPaneWidgets();
        showSettingPane();

        buildGamePaneWidgets(caseSize, casesPerSide);

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


    private void buildGamePaneWidgets(int caseSize, int casesPerSide) 
    {
        /*
        playBoard = new PlayBoard(caseSize, casesPerSide, this, serverConnection);
	    
	    playBoard.addKeyListener(new KeyboardHandler(serverConnection));

	    // init game pane
	    scorePanel = new JPanel();
	    scorePanel.setLayout(new BoxLayout(scorePanel, BoxLayout.Y_AXIS));
	    
	    // other components for labels here
	    scoreboard = new JTable();
	    scorePanel.add(scoreboard);
	    
	    
	    newGameButton = new JButton("New game");
	    newGameButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent event) 
            {
		    	manageRestartGameRequest();
		    }
	    });
	    newGameButton.setVisible(false);

	    JPanel bottomPanel = new JPanel(new FlowLayout());
	    bottomPanel.add(new ExitButton(serverConnection));
	    bottomPanel.add(newGameButton);

	    gamePane = new JPanel(new BorderLayout());
	    gamePane.setOpaque(true);
	    gamePane.add(playBoard, BorderLayout.CENTER);
	    gamePane.add(scorePanel, BorderLayout.EAST);
	    gamePane.add(new ExitButton(serverConnection), BorderLayout.PAGE_END);
	    gamePane.add(bottomPanel, BorderLayout.PAGE_END);
	    */
        wp = new WaitingPanel();
	    //serverConnection.setWaitingPanel(wp);
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

    // #region Event Part
    private void doneButtonClick() {
        if (nameField.getText().length() != 0) {
            manageConnectionRequest();
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

    public void analyseMsg(Dictionary result) {
        int headVal = 0;
        for (Enumeration k = result.keys(); k.hasMoreElements();)
            headVal = (int) k.nextElement();

        switch (headVal) {
            case 1:
                String body = result.get(1).toString();
                if(Objects.equals(body, nameField.getText()))
                {
                    Map player =new HashMap();
                    player.put(body, false);
                    wp.showPanel(true);
                    wp.updatePlayersState(player, "Test");
                }
                
                System.out.println("stop");
                break;
        }
    }
    // #endregion
}

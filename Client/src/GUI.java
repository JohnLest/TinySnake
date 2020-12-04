
//--> GUI_class
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GUI {
    // #region GUI Part
    private JFrame frame;
    private JPanel settingPane;
    private JTextField nameField;
    private JButton doneButton;
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

        // buildGamePaneWidgets(caseSize, casesPerSide);
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
                    serv.read(socket);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
            Thread t = new Thread(r);
            t.start();
        } catch (IOException e) {
            showErrorMessage("Erreur de Connection");
            e.printStackTrace();
        }
    }
    // #endregion
}

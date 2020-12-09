import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.Map;


/**
 * Waiting GUI 
 */
public class WaitingPanel extends JFrame{

    private JPanel waitingPanel;
    private JPanel globalPanel;
    private JPanel playersInfo;
    private JButton playBtn;
    private JLabel messageInfo;
    
    private JTable playersStateTable;
    private SocketChannel socket;
    private boolean firstUpdate = true;

    public WaitingPanel(SocketChannel socket) {
        this.socket = socket;
        initFrame();
    }

    /**
     * Initialize the components of the frame
     */
    private void initFrame(){
        setTitle("Waiting Players");
        initButtons();
        playersStateTable = new JTable();
        playersInfo = new JPanel();
        messageInfo = new JLabel();
        messageInfo.setForeground(Color.RED);
        messageInfo.setBounds(100, 10, 300, 30);
        playersInfo.add(playersStateTable);
        playersInfo.setBounds(10, 40, 300, 300);
        
        waitingPanel = new JPanel();
        waitingPanel.setPreferredSize(new Dimension (300, 200));
        waitingPanel.setLayout(null);

        waitingPanel.add(messageInfo);
        waitingPanel.add(playBtn);
        waitingPanel.add(playersInfo);
        
        globalPanel = new JPanel();
        globalPanel.add(waitingPanel);
        setContentPane(globalPanel);
        pack();

        setLocationRelativeTo(null);
    }

    /**
     * Init the buttons of the frame
     */
    private void initButtons(){
        initPlayButton();
    }

    /**
     * Init the play button
     */
    private void initPlayButton() {
    	playBtn = new JButton("Play");
    	playBtn.setMnemonic(KeyEvent.VK_P);
        playBtn.setBounds( 165, 150, 100, 30);
        playBtn.addActionListener(event->PlayBtnClick());
    }
    /**
     * When the user click on the play button
     */
    private void PlayBtnClick() {
    	playerReady();
    }
    
    /**
     * Send a request to the server when the player is ready and manager the interface while waiting for the response
     */
    private void playerReady() {
    	Runnable r = (()->{
            LinkedList body = new LinkedList();
            body.add(App.idUser);
            body.add(App.idGame);
            body.add(true);
    		Server.write(socket, 2, body);
    		playBtn.setEnabled(false);
    	});
    	Thread t = new Thread(r);
    	t.start();
    }
    
    /**
     * Update the table of the players states
     * @param playersState a Map that contains the states of the players
     * @param message string with information to be displayed
     */
    public void updatePlayersState(Map<String, Boolean> playersState, String message) {
    	if(firstUpdate) {
    		firstUpdate = false;
    	}else {
        	playersInfo.removeAll();
    	}
    	PlayersReadyTableModel prModel= new PlayersReadyTableModel(playersState);
    	playersStateTable = new JTable(prModel);
    	playersInfo.add(playersStateTable);
    	messageInfo.setText(message);
    	waitingPanel.revalidate();
    	waitingPanel.repaint();
    }
    
    /**
     * Show or hide the pannel
     * @param visible true to show, false to hide
     */
    public void showPanel(boolean visible) {
    	playBtn.setEnabled(true);
    	setVisible(visible);
    	
    }
}

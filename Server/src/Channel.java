import java.nio.channels.SocketChannel;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.UUID;

public class Channel {
    public Dictionary channelSockets;
    public Dictionary channelUsers;

    public Channel() {
        channelSockets = new Hashtable();
        channelUsers = new Hashtable();
        newChannel();
    }

    public UUID newChannel (){
        UUID key = UUID.randomUUID();
        channelSockets.put(key, new LinkedList<SocketChannel>());
        channelUsers.put(key, new LinkedList<PlayerInfo>());
        return key;
    } 
}

import java.nio.channels.SocketChannel;
import java.security.PublicKey;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.UUID;

public class Channel {
    public Dictionary channels;

    public Channel() {
        channels = new Hashtable();
        newChannel();
    }

    public UUID newChannel (){
        UUID key = UUID.randomUUID();
        channels.put(key, new LinkedList<SocketChannel>());
        return key;
    } 
}

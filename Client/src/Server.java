import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SocketChannel;
import java.util.Dictionary;
import java.util.Hashtable;

public class Server {
    private InetSocketAddress addr;

    public Server(String url, int port) {
        this.addr = new InetSocketAddress(url, port);
    }

    public SocketChannel Connection() throws IOException {
        return SocketChannel.open(addr);
    }

    public Dictionary read(SocketChannel socket) {
        ScatteringByteChannel scatter = socket.socket().getChannel();
        Dictionary result = new Hashtable<>();
        byte[] headSize = new byte[81];
        byte[] bodySize = new byte[10000];
        ByteBuffer head = ByteBuffer.wrap(headSize);
        ByteBuffer body = ByteBuffer.wrap(bodySize);

        try {
            scatter.read(new ByteBuffer[] {head, body});
            head.flip();
            body.flip();
            result.put(Tools.deserialize(headSize), Tools.deserialize(bodySize));
            
        } catch (java.io.IOException e) { // Client probably closed connection
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    public static void write(SocketChannel socket, int headVal, Object bodyMsg) {
        try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        GatheringByteChannel gather = socket.socket().getChannel();
        ByteBuffer head; 
        ByteBuffer body; 
        try {
            head = ByteBuffer.wrap(Tools.serialize(headVal));
            body = ByteBuffer.wrap((Tools.serialize(bodyMsg)));
            gather.write(new ByteBuffer[] {head, body});
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //#region Game Part
    //#endregion
}
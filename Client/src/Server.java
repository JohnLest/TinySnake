import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Server extends Thread {
    private Object msg;
    private ByteBuffer buf;
    private InetSocketAddress addr;
    private SocketChannel socket = null;
    private byte[] bytes = new byte[1024];

    public Server(String url,  int port, Object obj) {
        msg = obj;
        buf = ByteBuffer.wrap(bytes);
        addr = new InetSocketAddress(url, port);
        try {
            socket = SocketChannel.open(addr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void finalize() {
        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            System.out.println("Error Destruction : " + e.getMessage());
        }
    }

    public void run() {
        try {
            for (int i = 0; i < 10; i++) {
                buf.clear();
                buf.put(Utils.serialize(msg));
                buf.flip();
                socket.write(buf);
                this.sleep(500);
                buf.clear();
                socket.read(buf);
                buf.flip();
                Object obj;
                obj = Utils.deserialize(bytes);
                System.out.println(obj.getClass());
                System.out.println(obj.toString());
            }
        } catch (IOException e) {
            System.out.println("Error connection Close : " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("Error Interruptiom : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Server {
    //private Object msg;
    //private ByteBuffer buf;
    private InetSocketAddress addr;
    //private byte[] bytes = new byte[1024];

    public Server(String url,  int port) {
        //msg = obj;
        //buf = ByteBuffer.wrap(bytes);
        this.addr = new InetSocketAddress(url, port);
    }

    public SocketChannel Connection() throws IOException {
        return SocketChannel.open(addr);
    }
    
    /*
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
    */
}

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.LinkedList;

public class Connect {
    private final int ServerPort = 8888;
    private final long timeout = 1000;
    private LinkedList<SocketChannel> clientsLst;
    private Selector select; 
    private ServerSocketChannel server; 

    public Connect() {
        Connection();
    }

    private void Connection(){
        clientsLst = new LinkedList<SocketChannel>();
        try {
            // #region Connection
            server = ServerSocketChannel.open();
            server.socket().bind(new InetSocketAddress(ServerPort));
            System.out.println("Port: " + server.socket().getLocalPort());

            server.configureBlocking(false); // Non-blocking
            select = Selector.open();
            server.register(select, SelectionKey.OP_ACCEPT);
            // #endregion
        } catch (IOException e) {
            System.out.println("Erreur connection : " + e.getMessage());
        } 
    }

    public void Communicate(){
        try {
            while (server != null) {
                isConnected(select);
                if (server == null)
                    break;
            }
        } catch (IOException e) {
            System.out.println("Erreur connection : " + e.getMessage());
        }
    }

    private void isConnected(Selector select) throws IOException {
        System.out.println("Connected: " + clientsLst.size());

        if (select.select(timeout) > 0) {
            Iterator i = select.selectedKeys().iterator();
            while (i.hasNext()) {
                getClient(select, i);
            }
        }
    }

    private void getClient(Selector select, Iterator i) {
        SelectionKey key = (SelectionKey) i.next();
        i.remove();
        if (key.isAcceptable()) {
            accept(select, key);
        } else if (key.isReadable()) {
            read(key);
        } else if (key.isWritable()) {
            write(key);
        } else {
            SocketChannel socket = (SocketChannel) key.channel();
            clientsLst.remove(socket);
            close(socket);
        }
    }

    private void write(SelectionKey key) {
        String s = "From Server";
        Object msg = s;
        SocketChannel socket = (SocketChannel) key.channel();
        ByteBuffer buf = ByteBuffer.allocate(1024);
        try {
            buf.clear();
            buf.put(Utils.serialize(msg));
            buf.flip();
            socket.write(buf);
            Thread.sleep(500);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void read(SelectionKey key) {
        SocketChannel socket = (SocketChannel) key.channel();
        byte[] bytes = new byte[1024];
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        try {
            socket.read(buf);
            buf.flip();
            Object obj = Utils.deserialize(bytes);
            System.out.println(obj.getClass());
            System.out.println(obj.toString());
            buf.clear();
        } catch (java.io.IOException e) { // Client probably closed connection
            clientsLst.remove(socket);
            close(socket);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void accept(Selector select, SelectionKey key) {
        ServerSocketChannel socket = (ServerSocketChannel) key.channel();
        try {
            SocketChannel newClient = socket.accept();
            newClient.configureBlocking(false);
            clientsLst.add(newClient);
            newClient.register(select, 5);
            //newClient.register(select, SelectionKey.OP_READ);
            
        } catch (java.io.IOException e) {
            close(socket);
        }
    }

    private void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                System.out.println("Ereur Deconnection : " + e.getMessage());
            }
        }
    }
}

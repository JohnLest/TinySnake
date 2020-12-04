import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
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
    private byte[] headSize = new byte[81];
    byte[] bodySize = new byte[1024];

    public Connect() {
        Connection();
    }

    private void Connection() {
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

    public void Communicate() {
        try {
            while (server != null) {
                isConnected(select);
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
        }else if (key.isReadable()) {
            read(key);
        }

    }

    private void write(SelectionKey key, int headVal, Object boddMsg) {
        SocketChannel socket = (SocketChannel) key.channel();
        GatheringByteChannel gather = socket.socket().getChannel();
        ByteBuffer head;
        ByteBuffer body;
        try {
            head = ByteBuffer.wrap(Utils.serialize(headVal));
            body = ByteBuffer.wrap((Utils.serialize(boddMsg)));
            gather.write(new ByteBuffer[] { head, body });
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void read(SelectionKey key) {
        SocketChannel socket = (SocketChannel) key.channel();
        ScatteringByteChannel scatter = socket.socket().getChannel();
        byte[] headSize = new byte[81];
        byte[] bodySize = new byte[1024];
        ByteBuffer head = ByteBuffer.wrap(headSize);
        ByteBuffer body = ByteBuffer.wrap(bodySize);
        try {
            scatter.read(new ByteBuffer[] { head, body });
            head.flip();
            body.flip();
            analyseMsg(Utils.deserialize(headSize), Utils.deserialize(bodySize), key);
        } catch (java.io.IOException e) { // Client probably closed connection
            clientsLst.remove(socket);
            close(socket);
            System.err.println(e.getMessage());
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
        } catch (java.io.IOException e) {
            close(socket);
        }
    }

    private void close(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                System.out.println("Ereur Déconnection : " + e.getMessage());
            }
        }
    }

    private void analyseMsg(Object head, Object body, SelectionKey key) {
        int headVal = (int) head;
        switch (headVal) {
            case 1:
                write(key, headVal, String.format("%s vient de ce connecter", body.toString()));
                break;

            default:
                break;
        }
    }
}

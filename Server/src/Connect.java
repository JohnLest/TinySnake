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
                isConnected();
            }
        } catch (IOException e) {
            System.out.println("Erreur connection : " + e.getMessage());
        }
    }

    private void isConnected() throws IOException {
        System.out.println("Connected: " + clientsLst.size());

        if (select.select(timeout) > 0) {
            Iterator i = select.selectedKeys().iterator();
            while (i.hasNext()) {
                getClient(i);
            }
        }
    }

    private void getClient(Iterator i) {
        SelectionKey key = (SelectionKey) i.next();
        i.remove();
        if (key.isAcceptable()) {
            accept(select, key);
        } else if (key.isReadable()) {
            read(key);
        }

    }

    private void write(SocketChannel socket, int headVal, Object boddMsg) {
        //SocketChannel socket = (SocketChannel) key.channel();
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
        } catch (IOException e) { // Client probably closed connection
            clientsLst.remove(socket);
            close(socket);
            System.err.println(e.getMessage());
        }
        Runnable r = (() -> {
            try {
                head.flip();
                body.flip();
                analyseMsg(Utils.deserialize(headSize), Utils.deserialize(bodySize));
            } catch (ClassNotFoundException | IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        Thread t = new Thread(r);
        t.start();
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

    private void analyseMsg(Object head, Object body) {
        int headVal = (int) head;
        switch (headVal) {
            case 1:
                String message = String.format("%s vient de ce connecter", body.toString());
                sendAll(headVal, message);
                break;
        }
    }

    private void sendAll(int headVal, Object body) {
        for (SocketChannel socket : clientsLst) {
            write(socket, headVal, body);
        }
        /*
        Iterator i = select.selectedKeys().iterator();
        while (i.hasNext()) {
            SelectionKey key = (SelectionKey) i.next();
            i.remove();
            if (key.isWritable())
                write(key, headVal, body);
        }*/
    }
}

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;

public class Connect {
    private final int ServerPort = 8888;
    private final long timeout = 1000;
    private LinkedList<SocketChannel> clientsLst;
    private Selector select;
    private ServerSocketChannel server;
    private Channel canal; 
    private Dictionary clientChanel;
	private PlayerRepository playerRepository;

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
            canal = new Channel();
            clientChanel = new Hashtable();
            playerRepository = new PlayerRepository();

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
        UUID channel = (UUID) clientChanel.get(socket);
        byte[] headSize = new byte[81];
        byte[] bodySize = new byte[1024];
        ByteBuffer head = ByteBuffer.wrap(headSize);
        ByteBuffer body = ByteBuffer.wrap(bodySize);
        try {
            scatter.read(new ByteBuffer[] { head, body });
        } catch (IOException e) { // Client probably closed connection
			LinkedList lst = (LinkedList) canal.channelSockets.get(channel);
            lst.remove(socket);
            close(socket);
            System.err.println(e.getMessage());
        }
        Runnable r = (() -> {
            try {
                head.flip();
                body.flip();
                analyseMsg(Utils.deserialize(headSize), Utils.deserialize(bodySize), channel);
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
            UUID last = null;
            for (Enumeration e = canal.channelSockets.keys(); e.hasMoreElements();){
                last = (UUID) e.nextElement();
            }
            if(Utils.isNullOrEmpty(last))
                last = canal.newChannel();
            LinkedList lst = (LinkedList) canal.channelSockets.get(last);
            if (lst.size() >= 4){
                last = canal.newChannel();
                lst = (LinkedList) canal.channelSockets.get(last);
            }
            SocketChannel newClient = socket.accept();
            newClient.configureBlocking(false);
            lst.add(newClient);
            newClient.register(select, 5);
            clientChanel.put(newClient, last);
            System.out.println("Stop");
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

    private void analyseMsg(Object head, Object body, UUID channel) {
        int headVal = (int) head;
        switch (headVal) {
            case 1:
                Dictionary userDico = (Dictionary) canal.channelUsers.get(channel);
                String userName = body.toString();
                UUID playerID = UUID.randomUUID();
                playerRepository.newPlayer(playerID,userName);
                userDico.put(playerID, userName);
                sendAll(headVal, userDico, channel);
                break;
        }
    }

    private void sendAll(int headVal, Object body, UUID channel) {
        LinkedList lst = (LinkedList) canal.channelSockets.get(channel);
        for (Object socket : lst) {
            write((SocketChannel) socket, headVal, body);
        }
    }
}

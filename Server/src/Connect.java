import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

public class Connect {
    private final int ServerPort = 8888;
    private final long timeout = 1000;
    private Selector select;
    private ServerSocketChannel server;
    private LinkedList<SocketChannel> clients;
    private PlayerRepository playerRepository;
    private GameEngineRepository gameEngineRepository;

    public Connect() {
        playerRepository = new PlayerRepository();
        gameEngineRepository = new GameEngineRepository();
        clients = new LinkedList<SocketChannel>();
        Connection();
    }

    private void Connection() {
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

    public static void write(SocketChannel socket, int headVal, Object boddMsg) {
        GatheringByteChannel gather = socket.socket().getChannel();
        ByteBuffer head;
        ByteBuffer body;
        try {
            head = ByteBuffer.wrap(Tools.serialize(headVal));
            body = ByteBuffer.wrap((Tools.serialize(boddMsg)));
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
        byte[] bodySize = new byte[10000];
        ByteBuffer head = ByteBuffer.wrap(headSize);
        ByteBuffer body = ByteBuffer.wrap(bodySize);
        try {
            scatter.read(new ByteBuffer[] { head, body });
        } catch (IOException e) { // Client probably closed connection
            clients.remove(socket);
            close(socket);
            System.err.println(e.getMessage());
        }
        Runnable r = (() -> {
            try {
                head.flip();
                body.flip();
                analyseMsg(Tools.deserialize(headSize), Tools.deserialize(bodySize), socket);
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
            clients.add(newClient);
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

    private void analyseMsg(Object head, Object body, SocketChannel socket) {
        int headVal = (int) head;
        switch (headVal) {
            case 1:
                String userName = body.toString();
                write(socket, 1, NewPlayer(socket, userName));
                break;
            case 2:
                LinkedList body2 = (LinkedList) body;
                SetPlayerReady((UUID) body2.get(0), (UUID) body2.get(1), (Boolean) body2.get(2));
                break;
            case 3:
                LinkedList body3 = (LinkedList) body;
                TreatEvent((UUID) body3.get(0), (UUID) body3.get(1), (GameEvent) body3.get(2));
                break;
            case 4:
                write(socket, 5, GetPlayArea((UUID) body));
                break;
            case 5:
                write(socket, 6, GetScoreboard((UUID) body));
                break;
            case 6:
                write(socket, 7, IsGameOver((UUID) body));
                break;
            case 7:
                LinkedList body7 = (LinkedList) body;
                ExitGame((UUID)body7.get(0), (UUID)body7.get(1), socket);
                break;
            case 8:
                write(socket, 8, GetNewGame((UUID)body));
                break;
        }
    }

    // #region Game Part
    private Map<UUID, UUID> NewPlayer(SocketChannel socket, String username) {
        UUID idPlayer = UUID.randomUUID();
        UUID idGame;
        String nameFormatted = Tools.formatStringLengthMax(username);

        String newPlayerAddedResult = playerRepository.newPlayer(idPlayer, socket, nameFormatted);
        if (newPlayerAddedResult.equals("")) {
            idGame = gameEngineRepository.addPlayerToGame(idPlayer, playerRepository.getPlayerInfo(idPlayer));
            if (idGame != null) {
                Map<UUID, UUID> result = new HashMap<UUID, UUID>();
                result.put(idPlayer, idGame);
                return result;
            }
        }
        return null;
    }

    private void SetPlayerReady(UUID idPlayer, UUID idGame, boolean ready) {
        gameEngineRepository.setPlayerReady(idPlayer, idGame, ready);
    }

    private void TreatEvent(UUID idPlayer, UUID idGame, GameEvent evt) {
        gameEngineRepository.treatEvent(evt, idPlayer, idGame);
    }

    private PlayArea GetPlayArea(UUID idGame) {
        return gameEngineRepository.GetPlayArea(idGame);
    }

    private Map<String, Integer> GetScoreboard(UUID idGame) {
        return gameEngineRepository.getScoreboard(idGame);
    }
    
    private boolean IsGameOver(UUID idGame) {
		return gameEngineRepository.isGameOver(idGame);
	}
    
    private void ExitGame(UUID idPlayer, UUID idGame, SocketChannel socket) {
		gameEngineRepository.ExitGame(idPlayer, idGame);
        playerRepository.disconnectPlayer(idPlayer);
        clients.remove(socket);
    }
    
    private UUID GetNewGame(UUID idPlayer) {
		UUID idGame = null;
		PlayerInfo pInf = playerRepository.getPlayerInfo(idPlayer);
		if(pInf!=null) {
			idGame = gameEngineRepository.addPlayerToGame(idPlayer, pInf);
		}
		return idGame;
	}
    
    // #endregion
}

import java.util.UUID;

public class App {
    public static UUID idUser;
    public static UUID idGame;

    public static void main(String[] args) throws Exception {
        Server serv = new Server(args[0], 8888);
        new GUI(20, 25, serv);
    }
}

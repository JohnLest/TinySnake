// TODO Faire attention au exeptions
public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
        Server serv = new Server("10.15.0.1", 8888);
        new GUI(serv, 20, 25);
    }
}

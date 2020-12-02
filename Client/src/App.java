public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
        new Server(8888, "From Client").start();
    }
}

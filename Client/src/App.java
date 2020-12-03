public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
        new Server("15.10.0.1", 8888, "From Client").start();
    }
}

package pl.dziennik.virtualgradebookfx.network.server;

public class ServerMain {
    public static void main(String[] args) {
        new GradebookServer(5000).start();
    }
}
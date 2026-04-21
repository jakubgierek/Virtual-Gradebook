package pl.dziennik.virtualgradebookfx.network.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GradebookServer {

    private final int port;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public GradebookServer(int port) {
        this.port = port;
    }

    public void start() {
        System.out.println("Start serwera na porcie: " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nowe połączenie: " + clientSocket.getInetAddress());
                executorService.submit(new ClientHandler(clientSocket));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
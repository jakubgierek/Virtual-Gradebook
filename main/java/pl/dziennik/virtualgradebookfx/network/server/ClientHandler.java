package pl.dziennik.virtualgradebookfx.network.server;

import pl.dziennik.virtualgradebookfx.network.common.Request;
import pl.dziennik.virtualgradebookfx.network.common.Response;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ServerRequestDispatcher dispatcher = new ServerRequestDispatcher();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (Socket clientSocket = socket;
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

            Request request = (Request) in.readObject();
            Response response = dispatcher.handle(request);

            out.writeObject(response);
            out.flush();
            out.reset();

        } catch (Exception e) {
            System.err.println("Błąd obsługi klienta: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
}
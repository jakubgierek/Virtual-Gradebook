package pl.dziennik.virtualgradebookfx.network.client;

import pl.dziennik.virtualgradebookfx.network.common.Request;
import pl.dziennik.virtualgradebookfx.network.common.Response;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class NetworkClient {

    public Response send(Request request) {
        try (
                Socket socket = new Socket(NetworkConfig.SERVER_HOST, NetworkConfig.SERVER_PORT);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            out.writeObject(request);
            out.flush();

            return (Response) in.readObject();

        } catch (Exception e) {
            return Response.error("Błąd połączenia z serwerem: " + e.getMessage());
        }
    }
}
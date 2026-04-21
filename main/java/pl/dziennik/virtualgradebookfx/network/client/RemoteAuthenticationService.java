package pl.dziennik.virtualgradebookfx.network.client;

import pl.dziennik.virtualgradebookfx.model.user.User;
import pl.dziennik.virtualgradebookfx.network.common.Request;
import pl.dziennik.virtualgradebookfx.network.common.RequestType;
import pl.dziennik.virtualgradebookfx.network.common.Response;
import pl.dziennik.virtualgradebookfx.service.interfaces.AuthenticationService;

public class RemoteAuthenticationService implements AuthenticationService {

    private final NetworkClient networkClient = new NetworkClient();

    @Override
    public User authenticate(String login, String password) {
        Request request = new Request(RequestType.LOGIN)
                .add("login", login)
                .add("password", password);

        Response response = networkClient.send(request);

        if (!response.isSuccess()) {
            return null;
        }

        return (User) response.getData();
    }
}
package pl.dziennik.virtualgradebookfx.service.interfaces;

import pl.dziennik.virtualgradebookfx.model.user.User;

public interface AuthenticationService {
    User authenticate(String login, String password);
}
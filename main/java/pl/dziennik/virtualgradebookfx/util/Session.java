package pl.dziennik.virtualgradebookfx.util;

import pl.dziennik.virtualgradebookfx.model.user.User;

public class Session {
    private static User loggedUser;

    private Session() {
    }

    public static void setLoggedUser(User user) {
        loggedUser = user;
    }

    public static User getLoggedUser() {
        return loggedUser;
    }

    public static void clear() {
        loggedUser = null;
    }
}
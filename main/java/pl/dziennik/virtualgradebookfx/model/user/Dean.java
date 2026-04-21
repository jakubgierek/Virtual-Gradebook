package pl.dziennik.virtualgradebookfx.model.user;
import java.io.Serializable;

public class Dean extends User implements Serializable {
    private static final long serialVersionUID = 1L;

    public Dean() {
        setRole(Role.DEAN);
    }

    public Dean(String login, String password, String firstName, String lastName) {
        super(login, password, firstName, lastName, Role.DEAN);
    }
}
package pl.dziennik.virtualgradebookfx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import pl.dziennik.virtualgradebookfx.app.SceneManager;
import pl.dziennik.virtualgradebookfx.model.user.Role;
import pl.dziennik.virtualgradebookfx.model.user.User;
import pl.dziennik.virtualgradebookfx.network.client.RemoteAuthenticationService;
import pl.dziennik.virtualgradebookfx.service.interfaces.AuthenticationService;
import pl.dziennik.virtualgradebookfx.util.Session;
import pl.dziennik.virtualgradebookfx.app.AppServices;

public class LoginController {

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private final AuthenticationService authenticationService = new RemoteAuthenticationService();

    @FXML
    private void handleLogin() {
        String login = loginField.getText();
        String password = passwordField.getText();

        if (login.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Wypełnij login i hasło.");
            AppServices.getAuditLogService().logEvent("unknown", "LOGOWANIE_BŁĄD", "Nie podano loginu lub hasła");
            return;
        }

        User user = authenticationService.authenticate(login, password);

        if (user == null) {
            messageLabel.setText("Nieprawidłowy login lub hasło.");
            AppServices.getAuditLogService().logEvent(login, "LOGOWANIE_BŁĄD", "Nieudana próba logowania");
            return;
        }

        Session.setLoggedUser(user);
        AppServices.getAuditLogService().logEvent(user.getLogin(), "LOGOWANIE", "Zalogowano pomyślnie");


        if (user.getRole() == Role.STUDENT) {
            SceneManager.switchTo("/fxml/student/student-dashboard-view.fxml", "Panel ucznia");
        } else if (user.getRole() == Role.TEACHER) {
            SceneManager.switchTo("/fxml/teacher/teacher-dashboard-view.fxml", "Panel nauczyciela");
        } else if (user.getRole() == Role.DEAN) {
            SceneManager.switchTo("/fxml/dean/dean-dashboard-view.fxml", "Panel dyrektora");
        }
    }
}
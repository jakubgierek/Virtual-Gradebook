package pl.dziennik.virtualgradebookfx.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import pl.dziennik.virtualgradebookfx.app.AppServices;
import pl.dziennik.virtualgradebookfx.app.SceneManager;
import pl.dziennik.virtualgradebookfx.model.communication.Message;
import pl.dziennik.virtualgradebookfx.model.user.Dean;
import pl.dziennik.virtualgradebookfx.model.user.Student;
import pl.dziennik.virtualgradebookfx.model.user.Teacher;
import pl.dziennik.virtualgradebookfx.model.user.User;
import pl.dziennik.virtualgradebookfx.network.client.RemoteMessageService;
import pl.dziennik.virtualgradebookfx.network.client.RemoteUserService;
import pl.dziennik.virtualgradebookfx.service.impl.ClassManagementServiceImpl;
import pl.dziennik.virtualgradebookfx.service.impl.StudentManagementServiceImpl;
import pl.dziennik.virtualgradebookfx.service.interfaces.ClassManagementService;
import pl.dziennik.virtualgradebookfx.service.interfaces.MessageService;
import pl.dziennik.virtualgradebookfx.service.interfaces.StudentManagementService;
import pl.dziennik.virtualgradebookfx.service.interfaces.UserService;
import pl.dziennik.virtualgradebookfx.util.Session;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DeanDashboardController {

    private final ClassManagementService classManagementService = new ClassManagementServiceImpl();
    private final StudentManagementService studentManagementService = new StudentManagementServiceImpl();
    private final MessageService messageService = new RemoteMessageService();
    private final UserService userService = new RemoteUserService();

    private Timeline messageRefreshTimeline;
    private Runnable currentMessagesRefreshAction;

    @FXML
    private Label welcomeLabel;

    @FXML
    private StackPane contentPane;

    @FXML
    public void initialize() {
        User user = Session.getLoggedUser();

        if (user instanceof Dean dean) {
            welcomeLabel.setText("Witaj, " + dean.getFullName());
            showDeanInfo();
        }
    }

    private void refreshMessagesSilently() {
        try {
            if (currentMessagesRefreshAction != null) {
                currentMessagesRefreshAction.run();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startMessageAutoRefresh() {
        if (messageRefreshTimeline != null) {
            messageRefreshTimeline.stop();
        }

        messageRefreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(2), event -> refreshMessagesSilently())
        );
        messageRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        messageRefreshTimeline.play();
    }

    private void stopMessageAutoRefresh() {
        if (messageRefreshTimeline != null) {
            messageRefreshTimeline.stop();
        }
        currentMessagesRefreshAction = null;
    }

    @FXML
    private void showDeanInfo() {
        stopMessageAutoRefresh();

        User user = Session.getLoggedUser();

        if (user instanceof Dean dean) {
            VBox box = new VBox(12);
            box.getStyleClass().addAll("content-area", "info-card");

            Label title = new Label("Dane dziekana");
            title.getStyleClass().add("section-title");

            Label fullName = new Label("Imię i nazwisko: " + dean.getFullName());
            fullName.getStyleClass().add("info-text");

            Label login = new Label("Login: " + dean.getLogin());
            login.getStyleClass().add("info-text");

            box.getChildren().addAll(title, fullName, login);
            contentPane.getChildren().setAll(box);

            AppServices.getAuditLogService().logEvent(
                    dean.getLogin(),
                    "PANEL_DZIEKANA",
                    "Otwarto dane dziekana"
            );
        }
    }

    private void refreshTeachersList(
            VBox listBox,
            Label listTitle,
            Dean dean,
            TextField loginField,
            TextField passwordField,
            TextField firstNameField,
            TextField lastNameField,
            TextField subjectField,
            Button saveButton,
            Button cancelEditButton,
            Label statusLabel,
            String[] editingTeacherLogin
    ) {
        listBox.getChildren().clear();
        listBox.getChildren().add(listTitle);

        List<Teacher> teachers = userService.getAllTeachers();

        if (teachers.isEmpty()) {
            Label emptyLabel = new Label("Brak nauczycieli w systemie.");
            emptyLabel.getStyleClass().add("info-text");
            listBox.getChildren().add(emptyLabel);
            return;
        }

        for (Teacher teacher : teachers) {
            VBox teacherCard = new VBox(6);
            teacherCard.getStyleClass().add("teacher-grade-card");
            teacherCard.setMaxWidth(Double.MAX_VALUE);

            Label title = new Label(teacher.getFullName());
            title.getStyleClass().add("teacher-grade-title");

            Label loginLabel = new Label("Login: " + teacher.getLogin());
            loginLabel.getStyleClass().add("teacher-grade-text");

            Label subjectLabel = new Label("Przedmiot: " + teacher.getSubject());
            subjectLabel.getStyleClass().add("teacher-grade-text");

            HBox buttonsBox = new HBox(10);

            Button editButton = new Button("Edytuj");
            editButton.getStyleClass().add("teacher-action-button");

            Button deleteButton = new Button("Usuń");
            deleteButton.getStyleClass().add("teacher-delete-button");

            editButton.setOnAction(event -> {
                loginField.setText(teacher.getLogin());
                passwordField.clear();
                firstNameField.setText(teacher.getFirstName());
                lastNameField.setText(teacher.getLastName());
                subjectField.setText(teacher.getSubject());

                loginField.setDisable(true);
                saveButton.setText("Zapisz zmiany");
                cancelEditButton.setVisible(true);
                cancelEditButton.setManaged(true);
                editingTeacherLogin[0] = teacher.getLogin();

                statusLabel.setText("Tryb edycji nauczyciela: " + teacher.getLogin());
            });

            deleteButton.setOnAction(event -> {
                userService.deleteTeacher(teacher.getLogin());

                AppServices.getAuditLogService().logEvent(
                        dean.getLogin(),
                        "NAUCZYCIEL_USUNIĘCIE",
                        "Usunięto nauczyciela " + teacher.getLogin()
                );

                if (teacher.getLogin().equals(editingTeacherLogin[0])) {
                    loginField.clear();
                    passwordField.clear();
                    firstNameField.clear();
                    lastNameField.clear();
                    subjectField.clear();
                    loginField.setDisable(false);
                    saveButton.setText("Dodaj nauczyciela");
                    cancelEditButton.setVisible(false);
                    cancelEditButton.setManaged(false);
                    editingTeacherLogin[0] = null;
                    statusLabel.setText("");
                }

                refreshTeachersList(
                        listBox, listTitle, dean,
                        loginField, passwordField, firstNameField, lastNameField, subjectField,
                        saveButton, cancelEditButton, statusLabel, editingTeacherLogin
                );
            });

            buttonsBox.getChildren().addAll(editButton, deleteButton);
            teacherCard.getChildren().addAll(title, loginLabel, subjectLabel, buttonsBox);
            listBox.getChildren().add(teacherCard);
        }
    }

    @FXML
    private void showTeachers() {
        stopMessageAutoRefresh();

        User user = Session.getLoggedUser();

        if (user instanceof Dean dean) {
            HBox mainLayout = new HBox(16);
            mainLayout.getStyleClass().add("content-area");
            mainLayout.setFillHeight(true);
            mainLayout.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            VBox formBox = new VBox(10);
            formBox.getStyleClass().add("teacher-form-panel");
            formBox.setPrefWidth(360);
            formBox.setMinWidth(340);
            formBox.setMaxWidth(380);

            Label title = new Label("Zarządzanie nauczycielami");
            title.getStyleClass().add("section-title");
            title.setWrapText(true);

            TextField loginField = new TextField();
            loginField.setPromptText("Login");
            loginField.getStyleClass().add("compose-field");

            TextField passwordField = new TextField();
            passwordField.setPromptText("Hasło");
            passwordField.getStyleClass().add("compose-field");

            TextField firstNameField = new TextField();
            firstNameField.setPromptText("Imię");
            firstNameField.getStyleClass().add("compose-field");

            TextField lastNameField = new TextField();
            lastNameField.setPromptText("Nazwisko");
            lastNameField.getStyleClass().add("compose-field");

            TextField subjectField = new TextField();
            subjectField.setPromptText("Przedmiot");
            subjectField.getStyleClass().add("compose-field");

            Label statusLabel = new Label();
            statusLabel.getStyleClass().add("message-meta");
            statusLabel.setWrapText(true);

            Button saveButton = new Button("Dodaj nauczyciela");
            saveButton.getStyleClass().add("teacher-action-button");

            Button cancelEditButton = new Button("Anuluj edycję");
            cancelEditButton.getStyleClass().add("teacher-delete-button");
            cancelEditButton.setVisible(false);
            cancelEditButton.setManaged(false);

            HBox buttonsBox = new HBox(10, saveButton, cancelEditButton);

            VBox listBox = new VBox(12);
            listBox.getStyleClass().add("teacher-grades-list");
            listBox.setFillWidth(true);
            listBox.setMaxWidth(Double.MAX_VALUE);

            Label listTitle = new Label("Lista nauczycieli");
            listTitle.getStyleClass().add("section-title");
            listTitle.setWrapText(true);

            ScrollPane scrollPane = new ScrollPane(listBox);
            scrollPane.setFitToWidth(true);
            scrollPane.getStyleClass().add("scroll-clean");
            scrollPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            HBox.setHgrow(scrollPane, Priority.ALWAYS);

            String[] editingTeacherLogin = {null};

            Runnable resetForm = () -> {
                loginField.clear();
                passwordField.clear();
                firstNameField.clear();
                lastNameField.clear();
                subjectField.clear();
                loginField.setDisable(false);
                saveButton.setText("Dodaj nauczyciela");
                cancelEditButton.setVisible(false);
                cancelEditButton.setManaged(false);
                editingTeacherLogin[0] = null;
                statusLabel.setText("");
            };

            cancelEditButton.setOnAction(event -> resetForm.run());

            saveButton.setOnAction(event -> {
                String login = loginField.getText();
                String password = passwordField.getText();
                String firstName = firstNameField.getText();
                String lastName = lastNameField.getText();
                String subject = subjectField.getText();

                if (login == null || login.isBlank()) {
                    statusLabel.setText("Podaj login.");
                    return;
                }

                if (firstName == null || firstName.isBlank()) {
                    statusLabel.setText("Podaj imię.");
                    return;
                }

                if (lastName == null || lastName.isBlank()) {
                    statusLabel.setText("Podaj nazwisko.");
                    return;
                }

                if (subject == null || subject.isBlank()) {
                    statusLabel.setText("Podaj przedmiot.");
                    return;
                }

                if (editingTeacherLogin[0] == null) {
                    if (password == null || password.isBlank()) {
                        statusLabel.setText("Podaj hasło.");
                        return;
                    }

                    userService.addTeacher(login, password, firstName, lastName, subject);

                    AppServices.getAuditLogService().logEvent(
                            dean.getLogin(),
                            "NAUCZYCIEL_DODANIE",
                            "Dodano nauczyciela " + login + " | przedmiot: " + subject
                    );

                    statusLabel.setText("Nauczyciel został dodany.");
                } else {
                    userService.updateTeacher(editingTeacherLogin[0], password, firstName, lastName, subject);

                    AppServices.getAuditLogService().logEvent(
                            dean.getLogin(),
                            "NAUCZYCIEL_EDYCJA",
                            "Zedytowano nauczyciela " + editingTeacherLogin[0]
                    );

                    statusLabel.setText("Dane nauczyciela zostały zaktualizowane.");
                }

                resetForm.run();

                refreshTeachersList(
                        listBox, listTitle, dean,
                        loginField, passwordField, firstNameField, lastNameField, subjectField,
                        saveButton, cancelEditButton, statusLabel, editingTeacherLogin
                );
            });

            formBox.getChildren().addAll(
                    title,
                    loginField,
                    passwordField,
                    firstNameField,
                    lastNameField,
                    subjectField,
                    buttonsBox,
                    statusLabel
            );

            mainLayout.getChildren().addAll(formBox, scrollPane);
            contentPane.getChildren().setAll(mainLayout);

            refreshTeachersList(
                    listBox, listTitle, dean,
                    loginField, passwordField, firstNameField, lastNameField, subjectField,
                    saveButton, cancelEditButton, statusLabel, editingTeacherLogin
            );

            AppServices.getAuditLogService().logEvent(
                    dean.getLogin(),
                    "NAUCZYCIELE",
                    "Otwarto moduł zarządzania nauczycielami"
            );
        }
    }

    @FXML
    private void showClasses() {
        stopMessageAutoRefresh();

        User user = Session.getLoggedUser();

        if (user instanceof Dean dean) {
            HBox mainLayout = new HBox(16);
            mainLayout.getStyleClass().add("content-area");

            VBox formBox = new VBox(10);
            formBox.getStyleClass().add("teacher-form-panel");

            Label title = new Label("Zarządzanie klasami");
            title.getStyleClass().add("section-title");

            TextField classNameField = new TextField();
            classNameField.setPromptText("Nazwa klasy, np. 2C");
            classNameField.getStyleClass().add("compose-field");

            TextField descriptionField = new TextField();
            descriptionField.setPromptText("Opis klasy");
            descriptionField.getStyleClass().add("compose-field");

            Label statusLabel = new Label();
            statusLabel.getStyleClass().add("message-meta");

            Button addButton = new Button("Dodaj klasę");
            addButton.getStyleClass().add("teacher-action-button");

            VBox listBox = new VBox(10);
            listBox.getStyleClass().add("teacher-grades-list");
            HBox.setHgrow(listBox, Priority.ALWAYS);

            Label listTitle = new Label("Lista klas");
            listTitle.getStyleClass().add("section-title");

            addButton.setOnAction(event -> {
                String className = classNameField.getText();
                String description = descriptionField.getText();

                if (className == null || className.isBlank()) {
                    statusLabel.setText("Podaj nazwę klasy.");
                    return;
                }

                classManagementService.addClass(className, description);
                statusLabel.setText("Klasa została dodana.");
                classNameField.clear();
                descriptionField.clear();

                AppServices.getAuditLogService().logEvent(
                        dean.getLogin(),
                        "KLASA_DODANIE",
                        "Dodano klasę " + className
                );

                refreshClassesList(listBox, listTitle, dean);
            });

            formBox.getChildren().addAll(title, classNameField, descriptionField, addButton, statusLabel);

            ScrollPane scrollPane = new ScrollPane(listBox);
            scrollPane.setFitToWidth(true);
            scrollPane.getStyleClass().add("scroll-clean");

            mainLayout.getChildren().addAll(formBox, scrollPane);
            contentPane.getChildren().setAll(mainLayout);

            refreshClassesList(listBox, listTitle, dean);
        }
    }

    @FXML
    private void showStudents() {
        stopMessageAutoRefresh();

        User user = Session.getLoggedUser();

        if (user instanceof Dean dean) {
            HBox mainLayout = new HBox(16);
            mainLayout.getStyleClass().add("content-area");
            mainLayout.setFillHeight(true);
            mainLayout.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            VBox formBox = new VBox(18);
            formBox.getStyleClass().add("teacher-form-panel");
            formBox.setPrefWidth(360);
            formBox.setMinWidth(340);
            formBox.setMaxWidth(380);

            Label title = new Label("Przypisywanie i przenoszenie studentów");
            title.getStyleClass().add("section-title");
            title.setWrapText(true);

            Label studentLabel = new Label("Student");
            studentLabel.getStyleClass().add("compose-label");

            ComboBox<String> studentBox = new ComboBox<>();
            studentBox.setMaxWidth(Double.MAX_VALUE);
            studentBox.getStyleClass().add("compose-field");

            Label classLabel = new Label("Klasa docelowa");
            classLabel.getStyleClass().add("compose-label");

            ComboBox<String> classBox = new ComboBox<>();
            classBox.setMaxWidth(Double.MAX_VALUE);
            classBox.getStyleClass().add("compose-field");

            Label moveStatusLabel = new Label();
            moveStatusLabel.getStyleClass().add("message-meta");
            moveStatusLabel.setWrapText(true);

            Button assignButton = new Button("Przypisz / przenieś");
            assignButton.getStyleClass().add("teacher-action-button");

            VBox addStudentSection = new VBox(10);
            addStudentSection.getStyleClass().add("teacher-grade-card");

            Label addStudentTitle = new Label("Dodawanie studenta");
            addStudentTitle.getStyleClass().add("teacher-grade-title");

            TextField loginField = new TextField();
            loginField.setPromptText("Login");
            loginField.getStyleClass().add("compose-field");

            TextField passwordField = new TextField();
            passwordField.setPromptText("Hasło");
            passwordField.getStyleClass().add("compose-field");

            TextField firstNameField = new TextField();
            firstNameField.setPromptText("Imię");
            firstNameField.getStyleClass().add("compose-field");

            TextField lastNameField = new TextField();
            lastNameField.setPromptText("Nazwisko");
            lastNameField.getStyleClass().add("compose-field");

            ComboBox<String> newStudentClassBox = new ComboBox<>();
            newStudentClassBox.setMaxWidth(Double.MAX_VALUE);
            newStudentClassBox.getStyleClass().add("compose-field");
            newStudentClassBox.setPromptText("Klasa opcjonalna");

            Label addStudentStatusLabel = new Label();
            addStudentStatusLabel.getStyleClass().add("message-meta");
            addStudentStatusLabel.setWrapText(true);

            Button addStudentButton = new Button("Dodaj studenta");
            addStudentButton.getStyleClass().add("teacher-action-button");

            addStudentSection.getChildren().addAll(
                    addStudentTitle,
                    loginField,
                    passwordField,
                    firstNameField,
                    lastNameField,
                    newStudentClassBox,
                    addStudentButton,
                    addStudentStatusLabel
            );

            VBox listBox = new VBox(12);
            listBox.getStyleClass().add("teacher-grades-list");
            listBox.setFillWidth(true);
            listBox.setMaxWidth(Double.MAX_VALUE);

            Label listTitle = new Label("Lista studentów według klas");
            listTitle.getStyleClass().add("section-title");
            listTitle.setWrapText(true);

            ScrollPane scrollPane = new ScrollPane(listBox);
            scrollPane.setFitToWidth(true);
            scrollPane.getStyleClass().add("scroll-clean");
            scrollPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            HBox.setHgrow(scrollPane, Priority.ALWAYS);
            HBox.setHgrow(listBox, Priority.ALWAYS);
            VBox.setVgrow(listBox, Priority.ALWAYS);

            assignButton.setOnAction(event -> {
                String selectedStudent = studentBox.getValue();
                String selectedClass = classBox.getValue();

                if (selectedStudent == null || selectedStudent.isBlank()) {
                    moveStatusLabel.setText("Wybierz studenta.");
                    return;
                }

                if (selectedClass == null || selectedClass.isBlank()) {
                    moveStatusLabel.setText("Wybierz klasę.");
                    return;
                }

                int start = selectedStudent.lastIndexOf('(');
                int end = selectedStudent.lastIndexOf(')');
                String studentLogin = selectedStudent.substring(start + 1, end);

                studentManagementService.assignStudentToClass(studentLogin, selectedClass);

                moveStatusLabel.setText("Student został przypisany / przeniesiony do klasy " + selectedClass + ".");

                AppServices.getAuditLogService().logEvent(
                        dean.getLogin(),
                        "STUDENT_KLASA",
                        "Przypisano/przeniesiono studenta " + studentLogin + " do klasy " + selectedClass
                );

                refreshStudentsList(listBox, listTitle, dean, studentBox, classBox);
                refreshStudentClassOptions(newStudentClassBox);
            });

            addStudentButton.setOnAction(event -> {
                String login = loginField.getText();
                String password = passwordField.getText();
                String firstName = firstNameField.getText();
                String lastName = lastNameField.getText();
                String selectedClass = newStudentClassBox.getValue();

                if (login == null || login.isBlank()) {
                    addStudentStatusLabel.setText("Podaj login.");
                    return;
                }

                if (password == null || password.isBlank()) {
                    addStudentStatusLabel.setText("Podaj hasło.");
                    return;
                }

                if (firstName == null || firstName.isBlank()) {
                    addStudentStatusLabel.setText("Podaj imię.");
                    return;
                }

                if (lastName == null || lastName.isBlank()) {
                    addStudentStatusLabel.setText("Podaj nazwisko.");
                    return;
                }

                studentManagementService.addStudent(login, password, firstName, lastName, selectedClass);

                addStudentStatusLabel.setText("Student został dodany.");

                AppServices.getAuditLogService().logEvent(
                        dean.getLogin(),
                        "STUDENT_DODANIE",
                        "Dodano studenta " + login + (selectedClass == null || selectedClass.isBlank() ? " bez klasy" : " do klasy " + selectedClass)
                );

                loginField.clear();
                passwordField.clear();
                firstNameField.clear();
                lastNameField.clear();
                newStudentClassBox.setValue(null);

                refreshStudentsList(listBox, listTitle, dean, studentBox, classBox);
                refreshStudentClassOptions(newStudentClassBox);
            });

            formBox.getChildren().addAll(
                    title,
                    studentLabel, studentBox,
                    classLabel, classBox,
                    assignButton,
                    moveStatusLabel,
                    addStudentSection
            );

            mainLayout.getChildren().addAll(formBox, scrollPane);
            contentPane.getChildren().setAll(mainLayout);

            refreshStudentsList(listBox, listTitle, dean, studentBox, classBox);
            refreshStudentClassOptions(newStudentClassBox);
        }
    }

    @FXML
    private void showDeanMessages() {
        stopMessageAutoRefresh();

        User user = Session.getLoggedUser();

        if (user instanceof Dean dean) {
            List<User> users = userService.getAllUsersExcept(dean.getLogin());

            HBox mainLayout = new HBox(16);
            mainLayout.getStyleClass().addAll("content-area", "messages-layout");
            mainLayout.setFillHeight(true);

            VBox messagesListBox = new VBox(10);
            messagesListBox.getStyleClass().add("messages-list-panel");

            Label listTitle = new Label("Wiadomości");
            listTitle.getStyleClass().add("section-title");

            HBox toggleBar = new HBox(10);
            toggleBar.getStyleClass().add("message-toggle-bar");

            Button inboxButton = new Button("Odebrane");
            Button sentButton = new Button("Wysłane");
            Button newMessageButton = new Button("Nowa wiadomość");

            inboxButton.getStyleClass().add("message-toggle-button-active");
            sentButton.getStyleClass().add("message-toggle-button");
            newMessageButton.getStyleClass().add("compose-button");

            toggleBar.getChildren().addAll(inboxButton, sentButton);

            VBox detailPanel = new VBox(10);
            detailPanel.getStyleClass().add("message-detail-panel");
            detailPanel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(detailPanel, Priority.ALWAYS);

            Label placeholderTitle = new Label("Wybierz wiadomość");
            placeholderTitle.getStyleClass().add("message-detail-subject");

            Label placeholderText = new Label("Kliknij wiadomość z listy po lewej stronie, aby odczytać pełną treść.");
            placeholderText.getStyleClass().add("message-detail-content");
            placeholderText.setWrapText(true);

            detailPanel.getChildren().addAll(placeholderTitle, placeholderText);

            VBox messageItemsContainer = new VBox(10);

            final Runnable[] showReceived = new Runnable[1];
            final Runnable[] showSent = new Runnable[1];

            showReceived[0] = () -> {
                List<Message> receivedMessages = messageService.getMessagesForUser(dean.getLogin());

                messageItemsContainer.getChildren().clear();
                inboxButton.getStyleClass().setAll("message-toggle-button-active");
                sentButton.getStyleClass().setAll("message-toggle-button");

                if (receivedMessages.isEmpty()) {
                    Label emptyLabel = new Label("Brak wiadomości odebranych.");
                    emptyLabel.getStyleClass().add("info-text");
                    messageItemsContainer.getChildren().add(emptyLabel);
                } else {
                    for (Message message : receivedMessages) {
                        messageItemsContainer.getChildren().add(createDeanMessageItem(message, detailPanel, true));
                    }
                }

                currentMessagesRefreshAction = showReceived[0];
            };

            showSent[0] = () -> {
                List<Message> sentMessages = messageService.getSentMessagesForUser(dean.getLogin());

                messageItemsContainer.getChildren().clear();
                inboxButton.getStyleClass().setAll("message-toggle-button");
                sentButton.getStyleClass().setAll("message-toggle-button-active");

                if (sentMessages.isEmpty()) {
                    Label emptyLabel = new Label("Brak wiadomości wysłanych.");
                    emptyLabel.getStyleClass().add("info-text");
                    messageItemsContainer.getChildren().add(emptyLabel);
                } else {
                    for (Message message : sentMessages) {
                        messageItemsContainer.getChildren().add(createDeanMessageItem(message, detailPanel, false));
                    }
                }

                currentMessagesRefreshAction = showSent[0];
            };

            inboxButton.setOnAction(event -> showReceived[0].run());
            sentButton.setOnAction(event -> showSent[0].run());

            newMessageButton.setOnAction(event -> {
                VBox composePanel = new VBox(12);
                composePanel.getStyleClass().add("compose-panel");

                Label composeTitle = new Label("Nowa wiadomość");
                composeTitle.getStyleClass().add("message-detail-subject");

                Label receiverLabel = new Label("Odbiorca");
                receiverLabel.getStyleClass().add("compose-label");

                ComboBox<String> receiverBox = new ComboBox<>();
                receiverBox.setMaxWidth(Double.MAX_VALUE);
                receiverBox.getStyleClass().add("compose-field");

                for (User receiver : users) {
                    receiverBox.getItems().add(receiver.getLogin());
                }

                Label subjectLabel = new Label("Temat");
                subjectLabel.getStyleClass().add("compose-label");

                TextField subjectField = new TextField();
                subjectField.setPromptText("Wpisz temat wiadomości");
                subjectField.getStyleClass().add("compose-field");

                Label contentLabel = new Label("Treść");
                contentLabel.getStyleClass().add("compose-label");

                TextArea contentArea = new TextArea();
                contentArea.setPromptText("Wpisz treść wiadomości");
                contentArea.setWrapText(true);
                contentArea.setPrefHeight(220);
                contentArea.getStyleClass().add("compose-field");

                Label statusLabel = new Label();
                statusLabel.getStyleClass().add("message-meta");
                statusLabel.setWrapText(true);

                Button sendButton = new Button("Wyślij");
                sendButton.getStyleClass().add("compose-button");

                sendButton.setOnAction(sendEvent -> {
                    String receiverLogin = receiverBox.getValue();
                    String subject = subjectField.getText();
                    String content = contentArea.getText();

                    if (receiverLogin == null || receiverLogin.isBlank()) {
                        statusLabel.setText("Wybierz odbiorcę.");
                        return;
                    }

                    if (subject == null || subject.isBlank()) {
                        statusLabel.setText("Wpisz temat wiadomości.");
                        return;
                    }

                    if (content == null || content.isBlank()) {
                        statusLabel.setText("Wpisz treść wiadomości.");
                        return;
                    }

                    String sentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                    statusLabel.setText("Wysyłanie wiadomości...");
                    sendButton.setDisable(true);

                    Task<Void> sendTask = new Task<>() {
                        @Override
                        protected Void call() {
                            messageService.sendMessage(dean.getLogin(), receiverLogin, subject, content, sentDate);
                            return null;
                        }
                    };

                    sendTask.setOnSucceeded(e -> {
                        statusLabel.setText("Wiadomość została wysłana.");
                        receiverBox.setValue(null);
                        subjectField.clear();
                        contentArea.clear();
                        sendButton.setDisable(false);

                        AppServices.getAuditLogService().logEvent(
                                dean.getLogin(),
                                "WIADOMOŚĆ",
                                "Dziekan wysłał wiadomość do " + receiverLogin + " | temat: " + subject
                        );

                        showSent[0].run();
                    });

                    sendTask.setOnFailed(e -> {
                        statusLabel.setText("Błąd podczas wysyłania wiadomości.");
                        sendButton.setDisable(false);
                        if (sendTask.getException() != null) {
                            sendTask.getException().printStackTrace();
                        }
                    });

                    Thread thread = new Thread(sendTask);
                    thread.setDaemon(true);
                    thread.start();
                });

                composePanel.getChildren().addAll(
                        composeTitle,
                        receiverLabel, receiverBox,
                        subjectLabel, subjectField,
                        contentLabel, contentArea,
                        sendButton,
                        statusLabel
                );

                detailPanel.getChildren().setAll(composePanel);
            });

            messagesListBox.getChildren().addAll(listTitle, toggleBar, newMessageButton, messageItemsContainer);

            ScrollPane listScrollPane = new ScrollPane(messagesListBox);
            listScrollPane.setFitToWidth(true);
            listScrollPane.getStyleClass().add("scroll-clean");
            listScrollPane.setPrefWidth(400);

            mainLayout.getChildren().addAll(listScrollPane, detailPanel);
            contentPane.getChildren().setAll(mainLayout);

            showReceived[0].run();
            startMessageAutoRefresh();

            AppServices.getAuditLogService().logEvent(
                    dean.getLogin(),
                    "WIADOMOŚCI_DZIEKANA",
                    "Otwarto moduł wiadomości dziekana"
            );
        }
    }


    @FXML
    private void handleLogout() {
        stopMessageAutoRefresh();

        User user = Session.getLoggedUser();

        if (user != null) {
            AppServices.getAuditLogService().logEvent(
                    user.getLogin(),
                    "WYLOGOWANIE",
                    "Użytkownik wylogował się"
            );
        }

        Session.clear();
        SceneManager.switchTo("/fxml/login-view.fxml", "Logowanie");
    }

    private void refreshClassesList(VBox listBox, Label listTitle, Dean dean) {
        listBox.getChildren().clear();
        listBox.getChildren().add(listTitle);

        List<String> classes = classManagementService.getAllClassNames();

        for (String className : classes) {
            HBox row = new HBox(10);

            Label label = new Label(className);
            label.getStyleClass().add("teacher-grade-title");

            Button deleteButton = new Button("Usuń");
            deleteButton.getStyleClass().add("teacher-delete-button");

            deleteButton.setOnAction(event -> {
                classManagementService.deleteClass(className);

                AppServices.getAuditLogService().logEvent(
                        dean.getLogin(),
                        "KLASA_USUNIĘCIE",
                        "Usunięto klasę " + className
                );

                refreshClassesList(listBox, listTitle, dean);
            });

            row.getChildren().addAll(label, deleteButton);
            listBox.getChildren().add(row);
        }
    }

    private void refreshStudentClassOptions(ComboBox<String> classBox) {
        classBox.getItems().clear();
        classBox.getItems().addAll(classManagementService.getAllClassNames());
    }

    private void refreshStudentsList(
            VBox listBox,
            Label listTitle,
            Dean dean,
            ComboBox<String> studentBox,
            ComboBox<String> classBox
    ) {
        studentBox.getItems().clear();
        classBox.getItems().clear();
        listBox.getChildren().clear();
        listBox.getChildren().add(listTitle);

        List<Student> students = studentManagementService.getAllStudents();
        List<String> classes = classManagementService.getAllClassNames();

        classBox.getItems().addAll(classes);

        for (Student student : students) {
            String currentClass = (student.getSchoolClass() == null || student.getSchoolClass().isBlank())
                    ? "brak klasy"
                    : student.getSchoolClass();

            studentBox.getItems().add(student.getFullName() + " (" + student.getLogin() + ") [" + currentClass + "]");
        }

        for (String className : classes) {
            VBox classSection = new VBox(8);
            classSection.getStyleClass().add("dean-student-section");
            classSection.setFillWidth(true);
            classSection.setMaxWidth(Double.MAX_VALUE);

            Label classHeader = new Label("Klasa: " + className);
            classHeader.getStyleClass().add("teacher-grade-title");

            classSection.getChildren().add(classHeader);

            boolean hasStudents = false;

            for (Student student : students) {
                if (className.equals(student.getSchoolClass())) {
                    hasStudents = true;

                    HBox studentRow = new HBox(10);
                    studentRow.setMaxWidth(Double.MAX_VALUE);

                    Label studentLabel = new Label(student.getFullName() + " | login: " + student.getLogin());
                    studentLabel.getStyleClass().add("teacher-grade-text");
                    studentLabel.setMaxWidth(Double.MAX_VALUE);
                    HBox.setHgrow(studentLabel, Priority.ALWAYS);

                    Button removeButton = new Button("Wypisz z klasy");
                    removeButton.getStyleClass().add("teacher-delete-button");
                    studentLabel.setMaxWidth(Double.MAX_VALUE);
                    HBox.setHgrow(studentLabel, Priority.ALWAYS);

                    removeButton.setOnAction(event -> {
                        studentManagementService.removeStudentFromClass(student.getLogin());

                        AppServices.getAuditLogService().logEvent(
                                dean.getLogin(),
                                "STUDENT_WYPISANIE",
                                "Wypisano studenta " + student.getLogin() + " z klasy " + className
                        );

                        refreshStudentsList(listBox, listTitle, dean, studentBox, classBox);
                    });

                    studentRow.getChildren().addAll(studentLabel, removeButton);
                    classSection.getChildren().add(studentRow);
                }
            }

            if (!hasStudents) {
                Label emptyLabel = new Label("Brak studentów w tej klasie.");
                emptyLabel.getStyleClass().add("info-text");
                classSection.getChildren().add(emptyLabel);
            }

            listBox.getChildren().add(classSection);
        }

        VBox noClassSection = new VBox(8);
        noClassSection.getStyleClass().add("teacher-grade-card");

        Label noClassHeader = new Label("Studenci bez klasy");
        noClassHeader.getStyleClass().add("teacher-grade-title");

        noClassSection.getChildren().add(noClassHeader);

        boolean hasStudentsWithoutClass = false;

        for (Student student : students) {
            if (student.getSchoolClass() == null || student.getSchoolClass().isBlank()) {
                hasStudentsWithoutClass = true;

                Label studentLabel = new Label(student.getFullName() + " | login: " + student.getLogin());
                studentLabel.getStyleClass().add("teacher-grade-text");

                noClassSection.getChildren().add(studentLabel);
            }
        }

        if (!hasStudentsWithoutClass) {
            Label emptyLabel = new Label("Brak studentów bez klasy.");
            emptyLabel.getStyleClass().add("info-text");
            noClassSection.getChildren().add(emptyLabel);
        }

        listBox.getChildren().add(noClassSection);
    }

    private VBox createDeanMessageItem(Message message, VBox detailPanel, boolean receivedBox) {
        VBox messageItem = new VBox(4);

        if (receivedBox && !message.isRead()) {
            messageItem.getStyleClass().add("message-list-item-unread");
        } else {
            messageItem.getStyleClass().add("message-list-item");
        }

        Label subjectLabel = new Label(message.getSubject());
        subjectLabel.getStyleClass().add("message-preview-subject");
        subjectLabel.setWrapText(true);

        String metaText;
        if (receivedBox) {
            metaText = "Od: " + message.getSenderLogin() +
                    " | " + message.getSentDate() +
                    (message.isRead() ? "" : " | NOWA");
        } else {
            metaText = "Do: " + message.getReceiverLogin() +
                    " | " + message.getSentDate();
        }

        Label metaLabel = new Label(metaText);
        metaLabel.getStyleClass().add("message-preview-meta");
        metaLabel.setWrapText(true);

        messageItem.getChildren().addAll(subjectLabel, metaLabel);

        messageItem.setOnMouseClicked(event -> {
            if (receivedBox && !message.isRead()) {
                messageService.markMessageAsRead(message.getId());
                message.setRead(true);

                messageItem.getStyleClass().clear();
                messageItem.getStyleClass().add("message-list-item");
                metaLabel.setText("Od: " + message.getSenderLogin() + " | " + message.getSentDate());
            }

            Label detailSubject = new Label(message.getSubject());
            detailSubject.getStyleClass().add("message-detail-subject");
            detailSubject.setWrapText(true);

            String details;
            if (receivedBox) {
                details = "Od: " + message.getSenderLogin() +
                        "\nDo: " + message.getReceiverLogin() +
                        "\nData: " + message.getSentDate() +
                        "\nStatus: " + (message.isRead() ? "Przeczytana" : "Nieprzeczytana");
            } else {
                details = "Od: " + message.getSenderLogin() +
                        "\nDo: " + message.getReceiverLogin() +
                        "\nData: " + message.getSentDate();
            }

            Label detailMeta = new Label(details);
            detailMeta.getStyleClass().add("message-detail-meta");
            detailMeta.setWrapText(true);

            Label detailContent = new Label(message.getContent());
            detailContent.getStyleClass().add("message-detail-content");
            detailContent.setWrapText(true);

            detailPanel.getChildren().setAll(detailSubject, detailMeta, detailContent);

            User loggedUser = Session.getLoggedUser();
            if (loggedUser != null) {
                AppServices.getAuditLogService().logEvent(
                        loggedUser.getLogin(),
                        "WIADOMOŚĆ_ODCZYT",
                        "Dziekan odczytał wiadomość o ID " + message.getId()
                );
            }
        });

        return messageItem;
    }
}
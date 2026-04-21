package pl.dziennik.virtualgradebookfx.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import pl.dziennik.virtualgradebookfx.app.AppServices;
import pl.dziennik.virtualgradebookfx.app.SceneManager;
import pl.dziennik.virtualgradebookfx.model.communication.Message;
import pl.dziennik.virtualgradebookfx.model.school.Grade;
import pl.dziennik.virtualgradebookfx.model.school.TimetableEntry;
import pl.dziennik.virtualgradebookfx.model.user.Student;
import pl.dziennik.virtualgradebookfx.model.user.Teacher;
import pl.dziennik.virtualgradebookfx.model.user.User;
import pl.dziennik.virtualgradebookfx.network.client.RemoteMessageService;
import pl.dziennik.virtualgradebookfx.network.client.RemoteUserService;
import pl.dziennik.virtualgradebookfx.service.impl.GradeServiceImpl;
import pl.dziennik.virtualgradebookfx.service.impl.TeacherTimetableServiceImpl;
import pl.dziennik.virtualgradebookfx.service.interfaces.GradeService;
import pl.dziennik.virtualgradebookfx.service.interfaces.MessageService;
import pl.dziennik.virtualgradebookfx.service.interfaces.TeacherTimetableService;
import pl.dziennik.virtualgradebookfx.service.interfaces.UserService;
import pl.dziennik.virtualgradebookfx.util.Session;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TeacherDashboardController {

    private final TeacherTimetableService teacherTimetableService = new TeacherTimetableServiceImpl();
    private final GradeService gradeService = new GradeServiceImpl();
    private final UserService userService = new RemoteUserService();
    private final MessageService messageService = new RemoteMessageService();

    private Timeline messageRefreshTimeline;
    private Runnable currentMessagesRefreshAction;

    @FXML
    private Label welcomeLabel;

    @FXML
    private StackPane contentPane;

    @FXML
    public void initialize() {
        User user = Session.getLoggedUser();

        if (user instanceof Teacher teacher) {
            welcomeLabel.setText("Witaj, " + teacher.getFullName());
            showTeacherInfo();
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
    private void showTeacherInfo() {
        stopMessageAutoRefresh();

        User user = Session.getLoggedUser();

        if (user instanceof Teacher teacher) {
            VBox box = new VBox(12);
            box.getStyleClass().addAll("content-area", "info-card");

            Label title = new Label("Dane nauczyciela");
            title.getStyleClass().add("section-title");

            Label fullName = new Label("Imię i nazwisko: " + teacher.getFullName());
            fullName.getStyleClass().add("info-text");

            Label login = new Label("Login: " + teacher.getLogin());
            login.getStyleClass().add("info-text");

            Label subject = new Label("Przedmiot: " + teacher.getSubject());
            subject.getStyleClass().add("info-text");

            box.getChildren().addAll(title, fullName, login, subject);
            contentPane.getChildren().setAll(box);

            AppServices.getAuditLogService().logEvent(
                    teacher.getLogin(),
                    "PANEL_NAUCZYCIELA",
                    "Otwarto dane nauczyciela"
            );
        }
    }

    @FXML
    private void showTeacherTimetable() {
        stopMessageAutoRefresh();

        User user = Session.getLoggedUser();

        if (user instanceof Teacher teacher) {
            List<TimetableEntry> entries = teacherTimetableService.getTimetableForTeacher(teacher.getLogin());

            String[] days = {"Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek"};
            String[] timeSlots = {"08:00-09:30", "10:00-11:30", "12:00-13:30", "14:00-15:30", "16:00-17:30"};

            VBox mainBox = new VBox(16);
            mainBox.getStyleClass().addAll("content-area", "timetable-wrapper");
            VBox.setVgrow(mainBox, Priority.ALWAYS);
            mainBox.setFillWidth(true);
            mainBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            Label title = new Label("Plan zajęć nauczyciela");
            title.getStyleClass().add("section-title");

            GridPane grid = new GridPane();
            grid.getStyleClass().add("timetable-grid");
            grid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            ColumnConstraints timeColumn = new ColumnConstraints();
            timeColumn.setMinWidth(120);
            timeColumn.setPrefWidth(140);
            timeColumn.setHgrow(Priority.NEVER);
            timeColumn.setFillWidth(true);
            grid.getColumnConstraints().add(timeColumn);

            for (int i = 0; i < days.length; i++) {
                ColumnConstraints dayColumn = new ColumnConstraints();
                dayColumn.setHgrow(Priority.ALWAYS);
                dayColumn.setFillWidth(true);
                dayColumn.setMinWidth(140);
                grid.getColumnConstraints().add(dayColumn);
            }

            RowConstraints headerRow = new RowConstraints();
            headerRow.setMinHeight(50);
            headerRow.setPrefHeight(50);
            headerRow.setVgrow(Priority.NEVER);
            grid.getRowConstraints().add(headerRow);

            for (int i = 0; i < timeSlots.length; i++) {
                RowConstraints row = new RowConstraints();
                row.setMinHeight(110);
                row.setPrefHeight(120);
                row.setVgrow(Priority.ALWAYS);
                grid.getRowConstraints().add(row);
            }

            Label emptyCorner = new Label("Godzina");
            emptyCorner.getStyleClass().addAll("timetable-header", "timetable-header-label");
            emptyCorner.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            emptyCorner.setAlignment(Pos.CENTER);
            grid.add(emptyCorner, 0, 0);

            for (int i = 0; i < days.length; i++) {
                Label dayLabel = new Label(days[i]);
                dayLabel.getStyleClass().addAll("timetable-header", "timetable-header-label");
                dayLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                dayLabel.setAlignment(Pos.CENTER);
                dayLabel.setWrapText(true);
                grid.add(dayLabel, i + 1, 0);
            }

            for (int row = 0; row < timeSlots.length; row++) {
                VBox timeBox = new VBox();
                timeBox.getStyleClass().add("time-cell");
                timeBox.setAlignment(Pos.CENTER);
                timeBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                Label timeLabel = new Label(timeSlots[row]);
                timeLabel.getStyleClass().add("time-label");
                timeLabel.setWrapText(true);

                timeBox.getChildren().add(timeLabel);
                grid.add(timeBox, 0, row + 1);

                for (int col = 0; col < days.length; col++) {
                    VBox cell = new VBox(6);
                    cell.getStyleClass().add("schedule-cell");
                    cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    cell.setFillWidth(true);
                    cell.setAlignment(Pos.CENTER);

                    GridPane.setHgrow(cell, Priority.ALWAYS);
                    GridPane.setVgrow(cell, Priority.ALWAYS);

                    String currentDay = days[col];
                    String currentSlot = timeSlots[row];
                    boolean found = false;

                    for (TimetableEntry entry : entries) {
                        String entrySlot = entry.getStartTime() + "-" + entry.getEndTime();

                        if (entry.getDayOfWeek().equals(currentDay) && entrySlot.equals(currentSlot)) {
                            VBox lessonTile = new VBox(4);
                            lessonTile.getStyleClass().add("schedule-entry");
                            lessonTile.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                            lessonTile.setFillWidth(true);

                            Label subjectLabel = new Label(entry.getSubjectName());
                            subjectLabel.getStyleClass().add("schedule-subject");
                            subjectLabel.setWrapText(true);

                            Label classLabel = new Label("Klasa: " + entry.getClassName());
                            classLabel.getStyleClass().add("schedule-details");
                            classLabel.setWrapText(true);

                            Label roomLabel = new Label("Sala: " + entry.getRoom());
                            roomLabel.getStyleClass().add("schedule-details");
                            roomLabel.setWrapText(true);

                            Label timeEntryLabel = new Label("Godzina: " + entry.getStartTime() + " - " + entry.getEndTime());
                            timeEntryLabel.getStyleClass().add("schedule-details");
                            timeEntryLabel.setWrapText(true);

                            lessonTile.getChildren().addAll(subjectLabel, classLabel, roomLabel, timeEntryLabel);
                            cell.getChildren().add(lessonTile);
                            cell.setAlignment(Pos.TOP_LEFT);
                            found = true;
                        }
                    }

                    if (!found) {
                        Label emptyLabel = new Label("-");
                        emptyLabel.getStyleClass().add("empty-schedule");
                        cell.getChildren().add(emptyLabel);
                        cell.setAlignment(Pos.CENTER);
                    }

                    grid.add(cell, col + 1, row + 1);
                }
            }

            VBox.setVgrow(grid, Priority.ALWAYS);

            mainBox.getChildren().addAll(title, grid);
            contentPane.getChildren().setAll(mainBox);

            AppServices.getAuditLogService().logEvent(
                    teacher.getLogin(),
                    "PLAN_NAUCZYCIELA",
                    "Otwarto plan zajęć nauczyciela"
            );
        }
    }

    @FXML
    private void showTeacherGrades() {
        stopMessageAutoRefresh();

        User user = Session.getLoggedUser();

        if (user instanceof Teacher teacher) {
            HBox mainLayout = new HBox(16);
            mainLayout.getStyleClass().addAll("content-area", "teacher-grades-layout");

            VBox formPanel = new VBox(10);
            formPanel.getStyleClass().add("teacher-form-panel");

            Label formTitle = new Label("Wystaw / edytuj ocenę");
            formTitle.getStyleClass().add("section-title");

            Label studentLabel = new Label("Student");
            studentLabel.getStyleClass().add("compose-label");

            ComboBox<String> studentBox = new ComboBox<>();
            studentBox.setMaxWidth(Double.MAX_VALUE);
            studentBox.getStyleClass().add("compose-field");

            List<User> users = userService.getAllUsers();
            for (User u : users) {
                if (u instanceof Student student) {
                    String className = student.getSchoolClass() == null ? "brak klasy" : student.getSchoolClass();
                    studentBox.getItems().add(student.getFullName() + " (" + student.getLogin() + ") [" + className + "]");
                }
            }

            Label gradeValueLabel = new Label("Ocena");
            gradeValueLabel.getStyleClass().add("compose-label");

            TextField gradeValueField = new TextField();
            gradeValueField.setPromptText("Np. 4.5");
            gradeValueField.getStyleClass().add("compose-field");

            Label gradeWeightLabel = new Label("Waga");
            gradeWeightLabel.getStyleClass().add("compose-label");

            TextField gradeWeightField = new TextField();
            gradeWeightField.setPromptText("Np. 2");
            gradeWeightField.getStyleClass().add("compose-field");

            Label descriptionLabel = new Label("Opis");
            descriptionLabel.getStyleClass().add("compose-label");

            TextArea descriptionArea = new TextArea();
            descriptionArea.setPromptText("Np. Kartkówka, sprawdzian, projekt...");
            descriptionArea.setWrapText(true);
            descriptionArea.setPrefHeight(120);
            descriptionArea.getStyleClass().add("compose-field");

            Label statusLabel = new Label();
            statusLabel.getStyleClass().add("message-meta");
            statusLabel.setWrapText(true);

            Button addButton = new Button("Dodaj ocenę");
            addButton.getStyleClass().add("teacher-action-button");

            Button cancelEditButton = new Button("Anuluj edycję");
            cancelEditButton.getStyleClass().add("teacher-delete-button");
            cancelEditButton.setVisible(false);
            cancelEditButton.setManaged(false);

            HBox formButtons = new HBox(10, addButton, cancelEditButton);

            formPanel.getChildren().addAll(
                    formTitle,
                    studentLabel, studentBox,
                    gradeValueLabel, gradeValueField,
                    gradeWeightLabel, gradeWeightField,
                    descriptionLabel, descriptionArea,
                    formButtons,
                    statusLabel
            );

            VBox gradesListBox = new VBox(12);
            gradesListBox.getStyleClass().add("teacher-grades-list");
            HBox.setHgrow(gradesListBox, Priority.ALWAYS);

            Label gradesTitle = new Label("Oceny wybranego studenta");
            gradesTitle.getStyleClass().add("section-title");
            gradesListBox.getChildren().add(gradesTitle);

            final int[] editingGradeId = {-1};

            Runnable resetForm = () -> {
                studentBox.setValue(null);
                gradeValueField.clear();
                gradeWeightField.clear();
                descriptionArea.clear();
                statusLabel.setText("");
                addButton.setText("Dodaj ocenę");
                editingGradeId[0] = -1;
                cancelEditButton.setVisible(false);
                cancelEditButton.setManaged(false);
            };

            studentBox.setOnAction(event -> refreshTeacherGradesForSelectedStudent(
                    studentBox,
                    gradesListBox,
                    gradesTitle,
                    teacher,
                    users,
                    gradeValueField,
                    gradeWeightField,
                    descriptionArea,
                    addButton,
                    cancelEditButton,
                    statusLabel,
                    editingGradeId,
                    resetForm
            ));

            cancelEditButton.setOnAction(event -> resetForm.run());

            addButton.setOnAction(event -> {
                try {
                    String selectedStudent = studentBox.getValue();

                    if (selectedStudent == null || selectedStudent.isBlank()) {
                        statusLabel.setText("Wybierz studenta.");
                        return;
                    }

                    int start = selectedStudent.lastIndexOf('(');
                    int end = selectedStudent.lastIndexOf(')');
                    String studentLogin = selectedStudent.substring(start + 1, end);

                    double gradeValue = Double.parseDouble(gradeValueField.getText().replace(",", "."));
                    int gradeWeight = Integer.parseInt(gradeWeightField.getText());
                    String description = descriptionArea.getText();

                    if (description == null || description.isBlank()) {
                        statusLabel.setText("Wpisz opis oceny.");
                        return;
                    }

                    if (editingGradeId[0] == -1) {
                        gradeService.addGrade(
                                studentLogin,
                                teacher.getSubject(),
                                gradeValue,
                                gradeWeight,
                                description,
                                teacher.getLogin()
                        );

                        statusLabel.setText("Ocena została dodana.");

                        AppServices.getAuditLogService().logEvent(
                                teacher.getLogin(),
                                "OCENA_DODANIE",
                                "Dodano ocenę z przedmiotu " + teacher.getSubject() + " dla studenta " + studentLogin
                        );
                    } else {
                        gradeService.updateGrade(editingGradeId[0], gradeValue, gradeWeight, description);

                        statusLabel.setText("Ocena została zaktualizowana.");

                        AppServices.getAuditLogService().logEvent(
                                teacher.getLogin(),
                                "OCENA_EDYCJA",
                                "Zedytowano ocenę ID " + editingGradeId[0]
                        );
                    }

                    gradeValueField.clear();
                    gradeWeightField.clear();
                    descriptionArea.clear();
                    addButton.setText("Dodaj ocenę");
                    editingGradeId[0] = -1;
                    cancelEditButton.setVisible(false);
                    cancelEditButton.setManaged(false);

                    refreshTeacherGradesForSelectedStudent(
                            studentBox,
                            gradesListBox,
                            gradesTitle,
                            teacher,
                            users,
                            gradeValueField,
                            gradeWeightField,
                            descriptionArea,
                            addButton,
                            cancelEditButton,
                            statusLabel,
                            editingGradeId,
                            resetForm
                    );

                } catch (Exception ex) {
                    statusLabel.setText("Błąd danych przy zapisie oceny.");
                }
            });

            ScrollPane gradesScroll = new ScrollPane(gradesListBox);
            gradesScroll.setFitToWidth(true);
            gradesScroll.getStyleClass().add("scroll-clean");

            mainLayout.getChildren().addAll(formPanel, gradesScroll);
            contentPane.getChildren().setAll(mainLayout);

            AppServices.getAuditLogService().logEvent(
                    teacher.getLogin(),
                    "OCENY_NAUCZYCIELA",
                    "Otwarto moduł ocen nauczyciela"
            );
        }
    }

    @FXML
    private void showTeacherMessages() {
        stopMessageAutoRefresh();

        User user = Session.getLoggedUser();

        if (user instanceof Teacher teacher) {
            List<User> users = userService.getAllUsersExcept(teacher.getLogin());

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
                List<Message> receivedMessages = messageService.getMessagesForUser(teacher.getLogin());

                messageItemsContainer.getChildren().clear();
                inboxButton.getStyleClass().setAll("message-toggle-button-active");
                sentButton.getStyleClass().setAll("message-toggle-button");

                if (receivedMessages.isEmpty()) {
                    Label emptyLabel = new Label("Brak wiadomości odebranych.");
                    emptyLabel.getStyleClass().add("info-text");
                    messageItemsContainer.getChildren().add(emptyLabel);
                } else {
                    for (Message message : receivedMessages) {
                        messageItemsContainer.getChildren().add(createTeacherMessageItem(message, detailPanel, true));
                    }
                }

                currentMessagesRefreshAction = showReceived[0];
            };

            showSent[0] = () -> {
                List<Message> sentMessages = messageService.getSentMessagesForUser(teacher.getLogin());

                messageItemsContainer.getChildren().clear();
                inboxButton.getStyleClass().setAll("message-toggle-button");
                sentButton.getStyleClass().setAll("message-toggle-button-active");

                if (sentMessages.isEmpty()) {
                    Label emptyLabel = new Label("Brak wiadomości wysłanych.");
                    emptyLabel.getStyleClass().add("info-text");
                    messageItemsContainer.getChildren().add(emptyLabel);
                } else {
                    for (Message message : sentMessages) {
                        messageItemsContainer.getChildren().add(createTeacherMessageItem(message, detailPanel, false));
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
                            messageService.sendMessage(teacher.getLogin(), receiverLogin, subject, content, sentDate);
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
                                teacher.getLogin(),
                                "WIADOMOŚĆ",
                                "Nauczyciel wysłał wiadomość do " + receiverLogin + " | temat: " + subject
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
                    teacher.getLogin(),
                    "WIADOMOŚCI_NAUCZYCIELA",
                    "Otwarto moduł wiadomości nauczyciela"
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

    private VBox createTeacherMessageItem(Message message, VBox detailPanel, boolean receivedBox) {
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
                        "Nauczyciel odczytał wiadomość o ID " + message.getId()
                );
            }
        });

        return messageItem;
    }

    private void refreshTeacherGradesForSelectedStudent(
            ComboBox<String> studentBox,
            VBox gradesListBox,
            Label gradesTitle,
            Teacher teacher,
            List<User> users,
            TextField gradeValueField,
            TextField gradeWeightField,
            TextArea descriptionArea,
            Button addButton,
            Button cancelEditButton,
            Label statusLabel,
            int[] editingGradeId,
            Runnable resetForm
    ) {
        gradesListBox.getChildren().clear();
        gradesListBox.getChildren().add(gradesTitle);

        String selectedStudent = studentBox.getValue();

        if (selectedStudent == null || selectedStudent.isBlank()) {
            Label infoLabel = new Label("Wybierz studenta, aby zobaczyć jego oceny z przedmiotu: " + teacher.getSubject());
            infoLabel.getStyleClass().add("info-text");
            infoLabel.setWrapText(true);
            gradesListBox.getChildren().add(infoLabel);
            return;
        }

        int start = selectedStudent.lastIndexOf('(');
        int end = selectedStudent.lastIndexOf(')');
        String studentLogin = selectedStudent.substring(start + 1, end);

        List<Grade> studentGrades = gradeService.getGradesForStudentSubjectAndTeacher(
                studentLogin,
                teacher.getSubject(),
                teacher.getLogin()
        );

        if (studentGrades.isEmpty()) {
            Label emptyLabel = new Label("Brak ocen dla wybranego studenta z przedmiotu: " + teacher.getSubject());
            emptyLabel.getStyleClass().add("info-text");
            gradesListBox.getChildren().add(emptyLabel);
            return;
        }

        for (Grade grade : studentGrades) {
            VBox gradeCard = new VBox(6);
            gradeCard.getStyleClass().add("teacher-grade-card");

            Label title = new Label(grade.getSubjectName() + " | student: " + grade.getStudentLogin());
            title.getStyleClass().add("teacher-grade-title");

            Label value = new Label("Ocena: " + grade.getGradeValue() + " | Waga: " + grade.getGradeWeight());
            value.getStyleClass().add("teacher-grade-text");

            Label description = new Label("Opis: " + grade.getDescription());
            description.getStyleClass().add("teacher-grade-text");
            description.setWrapText(true);

            HBox buttonsBox = new HBox(10);

            Button editButton = new Button("Edytuj");
            editButton.getStyleClass().add("teacher-action-button");

            Button deleteButton = new Button("Usuń");
            deleteButton.getStyleClass().add("teacher-delete-button");

            editButton.setOnAction(event -> {
                gradeValueField.setText(String.valueOf(grade.getGradeValue()));
                gradeWeightField.setText(String.valueOf(grade.getGradeWeight()));
                descriptionArea.setText(grade.getDescription());

                addButton.setText("Zapisz zmiany");
                editingGradeId[0] = grade.getId();
                cancelEditButton.setVisible(true);
                cancelEditButton.setManaged(true);

                statusLabel.setText("Tryb edycji oceny ID " + grade.getId());
            });

            deleteButton.setOnAction(event -> {
                gradeService.deleteGrade(grade.getId());

                AppServices.getAuditLogService().logEvent(
                        teacher.getLogin(),
                        "OCENA_USUNIĘCIE",
                        "Usunięto ocenę ID " + grade.getId()
                );

                if (editingGradeId[0] == grade.getId()) {
                    resetForm.run();
                }

                refreshTeacherGradesForSelectedStudent(
                        studentBox,
                        gradesListBox,
                        gradesTitle,
                        teacher,
                        users,
                        gradeValueField,
                        gradeWeightField,
                        descriptionArea,
                        addButton,
                        cancelEditButton,
                        statusLabel,
                        editingGradeId,
                        resetForm
                );
            });

            buttonsBox.getChildren().addAll(editButton, deleteButton);
            gradeCard.getChildren().addAll(title, value, description, buttonsBox);
            gradesListBox.getChildren().add(gradeCard);
        }
    }
}
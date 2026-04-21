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
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import pl.dziennik.virtualgradebookfx.app.AppServices;
import pl.dziennik.virtualgradebookfx.app.SceneManager;
import pl.dziennik.virtualgradebookfx.model.communication.Message;
import pl.dziennik.virtualgradebookfx.model.school.Consultation;
import pl.dziennik.virtualgradebookfx.model.school.Grade;
import pl.dziennik.virtualgradebookfx.model.school.StudentSubject;
import pl.dziennik.virtualgradebookfx.model.school.TimetableEntry;
import pl.dziennik.virtualgradebookfx.model.user.Student;
import pl.dziennik.virtualgradebookfx.model.user.User;
import pl.dziennik.virtualgradebookfx.network.client.RemoteGradeService;
import pl.dziennik.virtualgradebookfx.network.client.RemoteMessageService;
import pl.dziennik.virtualgradebookfx.network.client.RemoteTimetableService;
import pl.dziennik.virtualgradebookfx.network.client.RemoteUserService;
import pl.dziennik.virtualgradebookfx.service.impl.ConsultationServiceImpl;
import pl.dziennik.virtualgradebookfx.service.interfaces.ConsultationService;
import pl.dziennik.virtualgradebookfx.service.interfaces.GradeService;
import pl.dziennik.virtualgradebookfx.service.interfaces.MessageService;
import pl.dziennik.virtualgradebookfx.service.interfaces.TimetableService;
import pl.dziennik.virtualgradebookfx.service.interfaces.UserService;
import pl.dziennik.virtualgradebookfx.util.Session;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StudentDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private StackPane contentPane;

    private final GradeService gradeService = new RemoteGradeService();
    private final TimetableService timetableService = new RemoteTimetableService();
    private final ConsultationService consultationService = new ConsultationServiceImpl();
    private final MessageService messageService = new RemoteMessageService();
    private final UserService userService = new RemoteUserService();

    private Timeline messageRefreshTimeline;
    private Runnable currentMessagesRefreshAction;

    @FXML
    public void initialize() {
        User user = Session.getLoggedUser();

        if (user instanceof Student student) {
            welcomeLabel.setText("Witaj, " + student.getFullName());
            showStudentInfo();
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
    private void showStudentInfo() {
        stopMessageAutoRefresh();

        User user = Session.getLoggedUser();

        if (user instanceof Student student) {
            VBox box = new VBox(12);
            box.getStyleClass().addAll("content-area", "info-card");

            Label title = new Label("Dane studenta");
            title.getStyleClass().add("section-title");

            Label fullName = new Label("Imię i nazwisko: " + student.getFullName());
            fullName.getStyleClass().add("info-text");

            Label login = new Label("Login: " + student.getLogin());
            login.getStyleClass().add("info-text");

            Label schoolClass = new Label("Klasa: " + student.getSchoolClass());
            schoolClass.getStyleClass().add("info-text");

            box.getChildren().addAll(title, fullName, login, schoolClass);
            contentPane.getChildren().setAll(box);
        }
    }

    @FXML
    private void showGrades() {
        stopMessageAutoRefresh();

        User user = Session.getLoggedUser();

        AppServices.getAuditLogService().logEvent(
                Session.getLoggedUser().getLogin(),
                "OCENY",
                "Otwarto moduł ocen"
        );

        if (user instanceof Student student) {
            List<StudentSubject> subjects = gradeService.getStudentSubjects(student.getLogin());
            double overallEctsAverage = gradeService.calculateOverallEctsAverage(student.getLogin());

            VBox mainBox = new VBox(15);
            mainBox.getStyleClass().add("content-area");

            Label title = new Label("Oceny studenta");
            title.getStyleClass().add("section-title");

            TilePane tilePane = new TilePane();
            tilePane.setHgap(16);
            tilePane.setVgap(16);
            tilePane.setPrefColumns(3);
            tilePane.getStyleClass().add("subjects-wrapper");

            for (StudentSubject subject : subjects) {
                List<Grade> subjectGrades = gradeService.getGradesForStudentAndSubject(
                        student.getLogin(),
                        subject.getSubjectName()
                );

                VBox subjectTile = new VBox(8);
                subjectTile.getStyleClass().add("subject-tile");

                Label subjectTitle = new Label(subject.getSubjectName() + " (" + subject.getEcts() + " ECTS)");
                subjectTitle.getStyleClass().add("subject-title");

                subjectTile.getChildren().add(subjectTitle);

                if (subjectGrades.isEmpty()) {
                    Label noGradesLabel = new Label("Brak ocen");
                    noGradesLabel.getStyleClass().add("no-grades-label");
                    subjectTile.getChildren().add(noGradesLabel);
                } else {
                    for (Grade grade : subjectGrades) {
                        Label gradeLabel = new Label(
                                "Ocena: " + grade.getGradeValue() +
                                        " | Waga: " + grade.getGradeWeight() +
                                        "\nOpis: " + grade.getDescription()
                        );
                        gradeLabel.getStyleClass().add("subject-grade");
                        subjectTile.getChildren().add(gradeLabel);
                    }

                    double subjectAverage = gradeService.calculateWeightedAverage(subjectGrades);
                    Label averageLabel = new Label(String.format("Średnia przedmiotu: %.2f", subjectAverage));
                    averageLabel.getStyleClass().add("subject-average");
                    subjectTile.getChildren().add(averageLabel);
                }

                tilePane.getChildren().add(subjectTile);
            }

            ScrollPane scrollPane = new ScrollPane(tilePane);
            scrollPane.setFitToWidth(true);
            scrollPane.getStyleClass().add("scroll-clean");

            Label overallAverageLabel = new Label(
                    String.format("Ogólna średnia ważona ECTS: %.2f", overallEctsAverage)
            );
            overallAverageLabel.getStyleClass().add("average-label");

            mainBox.getChildren().addAll(title, scrollPane, overallAverageLabel);
            contentPane.getChildren().setAll(mainBox);
        }
    }

    @FXML
    private void showTimetable() {
        stopMessageAutoRefresh();

        User user = Session.getLoggedUser();

        AppServices.getAuditLogService().logEvent(
                Session.getLoggedUser().getLogin(),
                "PLAN_ZAJĘĆ",
                "Otwarto plan zajęć"
        );

        if (user instanceof Student student) {
            if (student.getSchoolClass() == null || student.getSchoolClass().isBlank()) {
                VBox box = new VBox(12);
                box.getStyleClass().add("content-area");

                Label title = new Label("Plan zajęć studenta");
                title.getStyleClass().add("section-title");

                Label info = new Label("Student nie ma obecnie przypisanej klasy, więc plan zajęć nie jest dostępny.");
                info.getStyleClass().add("info-text");
                info.setWrapText(true);

                box.getChildren().addAll(title, info);
                contentPane.getChildren().setAll(box);
                return;
            }

            List<TimetableEntry> entries = timetableService.getTimetableForClass(student.getSchoolClass());

            String[] days = {"Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek"};
            String[] timeSlots = {"08:00-09:30", "10:00-11:30", "12:00-13:30", "14:00-15:30", "16:00-17:30"};

            VBox mainBox = new VBox(16);
            mainBox.getStyleClass().addAll("content-area", "timetable-wrapper");
            VBox.setVgrow(mainBox, Priority.ALWAYS);
            mainBox.setFillWidth(true);
            mainBox.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            Label title = new Label("Plan zajęć studenta");
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

                            Label teacherLabel = new Label("Prowadzący: " + entry.getTeacherName());
                            teacherLabel.getStyleClass().add("schedule-details");
                            teacherLabel.setWrapText(true);

                            Label roomLabel = new Label("Sala: " + entry.getRoom());
                            roomLabel.getStyleClass().add("schedule-details");
                            roomLabel.setWrapText(true);

                            Label timeEntryLabel = new Label("Godzina: " + entry.getStartTime() + " - " + entry.getEndTime());
                            timeEntryLabel.getStyleClass().add("schedule-details");
                            timeEntryLabel.setWrapText(true);

                            lessonTile.getChildren().addAll(subjectLabel, teacherLabel, roomLabel, timeEntryLabel);
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
        }
    }

    @FXML
    private void showMessages() {
        stopMessageAutoRefresh();

        User user = Session.getLoggedUser();

        if (user instanceof Student student) {
            List<User> users = userService.getAllUsersExcept(student.getLogin());

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
                List<Message> receivedMessages = messageService.getMessagesForUser(student.getLogin());

                messageItemsContainer.getChildren().clear();
                inboxButton.getStyleClass().setAll("message-toggle-button-active");
                sentButton.getStyleClass().setAll("message-toggle-button");

                if (receivedMessages.isEmpty()) {
                    Label emptyLabel = new Label("Brak wiadomości odebranych.");
                    emptyLabel.getStyleClass().add("info-text");
                    messageItemsContainer.getChildren().add(emptyLabel);
                } else {
                    for (Message message : receivedMessages) {
                        messageItemsContainer.getChildren().add(createMessageItem(message, detailPanel, true));
                    }
                }

                currentMessagesRefreshAction = showReceived[0];
            };

            showSent[0] = () -> {
                List<Message> sentMessages = messageService.getSentMessagesForUser(student.getLogin());

                messageItemsContainer.getChildren().clear();
                inboxButton.getStyleClass().setAll("message-toggle-button");
                sentButton.getStyleClass().setAll("message-toggle-button-active");

                if (sentMessages.isEmpty()) {
                    Label emptyLabel = new Label("Brak wiadomości wysłanych.");
                    emptyLabel.getStyleClass().add("info-text");
                    messageItemsContainer.getChildren().add(emptyLabel);
                } else {
                    for (Message message : sentMessages) {
                        messageItemsContainer.getChildren().add(createMessageItem(message, detailPanel, false));
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

                    String sentDate = LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                    statusLabel.setText("Wysyłanie wiadomości...");
                    sendButton.setDisable(true);

                    Task<Void> sendTask = new Task<>() {
                        @Override
                        protected Void call() {
                            messageService.sendMessage(student.getLogin(), receiverLogin, subject, content, sentDate);
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
                                student.getLogin(),
                                "WIADOMOŚĆ",
                                "Wysłano wiadomość do " + receiverLogin + " | temat: " + subject
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
        }
    }

    @FXML
    private void showConsultations() {
        stopMessageAutoRefresh();

        List<Consultation> consultations = consultationService.getAllConsultations();

        AppServices.getAuditLogService().logEvent(
                Session.getLoggedUser().getLogin(),
                "KONSULTACJE",
                "Otwarto moduł konsultacji"
        );

        VBox mainBox = new VBox(14);
        mainBox.getStyleClass().addAll("content-area", "messages-wrapper");

        Label title = new Label("Konsultacje prowadzących");
        title.getStyleClass().add("section-title");

        if (consultations.isEmpty()) {
            Label emptyLabel = new Label("Brak konsultacji.");
            emptyLabel.getStyleClass().add("info-text");
            mainBox.getChildren().addAll(title, emptyLabel);
        } else {
            mainBox.getChildren().add(title);

            for (Consultation consultation : consultations) {
                VBox consultationCard = new VBox(6);
                consultationCard.getStyleClass().add("consultation-card");

                Label teacherLabel = new Label(consultation.getTeacherName());
                teacherLabel.getStyleClass().add("consultation-title");

                Label subjectLabel = new Label("Przedmiot: " + consultation.getSubjectName());
                subjectLabel.getStyleClass().add("consultation-text");

                Label timeLabel = new Label(
                        "Termin: " + consultation.getDayOfWeek() + ", " +
                                consultation.getStartTime() + " - " + consultation.getEndTime()
                );
                timeLabel.getStyleClass().add("consultation-text");

                Label roomLabel = new Label("Sala: " + consultation.getRoom());
                roomLabel.getStyleClass().add("consultation-text");

                consultationCard.getChildren().addAll(teacherLabel, subjectLabel, timeLabel, roomLabel);
                mainBox.getChildren().add(consultationCard);
            }
        }

        ScrollPane scrollPane = new ScrollPane(mainBox);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-clean");

        contentPane.getChildren().setAll(scrollPane);
    }

    @FXML
    private void handleLogout() {
        stopMessageAutoRefresh();

        User user = Session.getLoggedUser();

        if (user != null) {
            AppServices.getAuditLogService().logEvent(user.getLogin(), "WYLOGOWANIE", "Użytkownik wylogował się");
        }

        Session.clear();
        SceneManager.switchTo("/fxml/login-view.fxml", "Logowanie");
    }

    private VBox createMessageItem(Message message, VBox detailPanel, boolean receivedBox) {
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

            AppServices.getAuditLogService().logEvent(
                    Session.getLoggedUser().getLogin(),
                    "WIADOMOŚĆ_ODCZYT",
                    "Odczytano wiadomość o ID " + message.getId()
            );
        });

        return messageItem;
    }
}
package pl.dziennik.virtualgradebookfx.network.server;

import pl.dziennik.virtualgradebookfx.model.communication.Message;
import pl.dziennik.virtualgradebookfx.model.school.Grade;
import pl.dziennik.virtualgradebookfx.model.school.StudentSubject;
import pl.dziennik.virtualgradebookfx.model.school.TimetableEntry;
import pl.dziennik.virtualgradebookfx.model.user.User;
import pl.dziennik.virtualgradebookfx.network.common.Request;
import pl.dziennik.virtualgradebookfx.network.common.RequestType;
import pl.dziennik.virtualgradebookfx.network.common.Response;
import pl.dziennik.virtualgradebookfx.service.impl.AuthenticationServiceImpl;
import pl.dziennik.virtualgradebookfx.service.impl.GradeServiceImpl;
import pl.dziennik.virtualgradebookfx.service.impl.MessageServiceImpl;
import pl.dziennik.virtualgradebookfx.service.impl.TimetableServiceImpl;
import pl.dziennik.virtualgradebookfx.service.interfaces.AuthenticationService;
import pl.dziennik.virtualgradebookfx.service.interfaces.GradeService;
import pl.dziennik.virtualgradebookfx.service.interfaces.MessageService;
import pl.dziennik.virtualgradebookfx.service.interfaces.TimetableService;
import pl.dziennik.virtualgradebookfx.service.impl.UserServiceImpl;
import pl.dziennik.virtualgradebookfx.service.interfaces.UserService;
import java.util.List;

public class ServerRequestDispatcher  {
    private final UserService userService = new UserServiceImpl();
    private final AuthenticationService authenticationService = new AuthenticationServiceImpl();
    private final GradeService gradeService = new GradeServiceImpl();
    private final MessageService messageService = new MessageServiceImpl();
    private final TimetableService timetableService = new TimetableServiceImpl();

    public Response handle(Request request) {
        try {
            RequestType type = request.getType();

            switch (type) {
                case LOGIN:
                    return handleLogin(request);

                case GET_STUDENT_SUBJECTS:
                    return handleGetStudentSubjects(request);

                case GET_STUDENT_GRADES:
                    return handleGetStudentGrades(request);

                case GET_TIMETABLE_FOR_CLASS:
                    return handleGetTimetableForClass(request);

                case GET_MESSAGES_FOR_USER:
                    return handleGetMessagesForUser(request);

                case GET_SENT_MESSAGES_FOR_USER:
                    return handleGetSentMessagesForUser(request);

                case SEND_MESSAGE:
                    return handleSendMessage(request);

                case MARK_MESSAGE_AS_READ:
                    return handleMarkMessageAsRead(request);

                case GET_ALL_USERS_EXCEPT:
                    return handleGetAllUsersExcept(request);

                default:
                    return Response.error("Nieobsługiwany typ żądania");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("Błąd serwera: " + e.getMessage());
        }
    }


    private Response handleGetAllUsersExcept(Request request) {
        String currentLogin = (String) request.getData().get("currentLogin");
        return Response.ok("Pobrano użytkowników", userService.getAllUsersExcept(currentLogin));
    }

    private Response handleLogin(Request request) {
        String login = (String) request.getData().get("login");
        String password = (String) request.getData().get("password");

        User user = authenticationService.authenticate(login, password);

        if (user == null) {
            return Response.error("Nieprawidłowy login lub hasło");
        }

        return Response.ok("Logowanie poprawne", user);
    }

    private Response handleGetStudentSubjects(Request request) {
        String studentLogin = (String) request.getData().get("studentLogin");
        List<StudentSubject> subjects = gradeService.getStudentSubjects(studentLogin);
        return Response.ok("Pobrano przedmioty", subjects);
    }

    private Response handleGetStudentGrades(Request request) {
        String studentLogin = (String) request.getData().get("studentLogin");
        String subjectName = (String) request.getData().get("subjectName");

        List<Grade> grades = gradeService.getGradesForStudentAndSubject(studentLogin, subjectName);
        return Response.ok("Pobrano oceny", grades);
    }

    private Response handleGetTimetableForClass(Request request) {
        String className = (String) request.getData().get("className");
        List<TimetableEntry> timetable = timetableService.getTimetableForClass(className);
        return Response.ok("Pobrano plan zajęć", timetable);
    }

    private Response handleGetMessagesForUser(Request request) {
        String receiverLogin = (String) request.getData().get("receiverLogin");
        List<Message> messages = messageService.getMessagesForUser(receiverLogin);
        return Response.ok("Pobrano wiadomości odebrane", messages);
    }

    private Response handleGetSentMessagesForUser(Request request) {
        String senderLogin = (String) request.getData().get("senderLogin");
        List<Message> messages = messageService.getSentMessagesForUser(senderLogin);
        return Response.ok("Pobrano wiadomości wysłane", messages);
    }

    private Response handleSendMessage(Request request) {
        String senderLogin = (String) request.getData().get("senderLogin");
        String receiverLogin = (String) request.getData().get("receiverLogin");
        String subject = (String) request.getData().get("subject");
        String content = (String) request.getData().get("content");
        String sentDate = (String) request.getData().get("sentDate");

        messageService.sendMessage(senderLogin, receiverLogin, subject, content, sentDate);
        return Response.ok("Wiadomość wysłana", null);
    }

    private Response handleMarkMessageAsRead(Request request) {
        Integer messageId = (Integer) request.getData().get("messageId");
        messageService.markMessageAsRead(messageId);
        return Response.ok("Wiadomość oznaczona jako przeczytana", null);
    }
}
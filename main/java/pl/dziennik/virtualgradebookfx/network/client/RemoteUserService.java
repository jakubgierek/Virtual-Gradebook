package pl.dziennik.virtualgradebookfx.network.client;

import pl.dziennik.virtualgradebookfx.model.user.Teacher;
import pl.dziennik.virtualgradebookfx.model.user.User;
import pl.dziennik.virtualgradebookfx.network.common.Request;
import pl.dziennik.virtualgradebookfx.network.common.RequestType;
import pl.dziennik.virtualgradebookfx.network.common.Response;
import pl.dziennik.virtualgradebookfx.service.impl.UserServiceImpl;
import pl.dziennik.virtualgradebookfx.service.interfaces.UserService;

import java.util.Collections;
import java.util.List;

public class RemoteUserService implements UserService {

    private final NetworkClient networkClient = new NetworkClient();
    private final UserService localUserService = new UserServiceImpl();

    @Override
    public List<User> getAllUsers() {
        return localUserService.getAllUsers();
    }

    @Override
    public List<User> getAllUsersExcept(String currentLogin) {
        Request request = new Request(RequestType.GET_ALL_USERS_EXCEPT)
                .add("currentLogin", currentLogin);

        Response response = networkClient.send(request);

        if (!response.isSuccess()) {
            return Collections.emptyList();
        }

        return (List<User>) response.getData();
    }

    @Override
    public List<Teacher> getAllTeachers() {
        return localUserService.getAllTeachers();
    }

    @Override
    public void addTeacher(String login, String password, String firstName, String lastName, String subject) {
        localUserService.addTeacher(login, password, firstName, lastName, subject);
    }

    @Override
    public void updateTeacher(String login, String password, String firstName, String lastName, String subject) {
        localUserService.updateTeacher(login, password, firstName, lastName, subject);
    }

    @Override
    public void deleteTeacher(String login) {
        localUserService.deleteTeacher(login);
    }
}
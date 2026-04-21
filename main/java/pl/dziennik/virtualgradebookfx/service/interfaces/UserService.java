package pl.dziennik.virtualgradebookfx.service.interfaces;

import pl.dziennik.virtualgradebookfx.model.user.Teacher;
import pl.dziennik.virtualgradebookfx.model.user.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    List<User> getAllUsersExcept(String currentLogin);
    List<Teacher> getAllTeachers();
    void addTeacher(String login, String password, String firstName, String lastName, String subjectName);
    void updateTeacher(String login, String password, String firstName, String lastName, String subjectName);
    void deleteTeacher(String login);

}
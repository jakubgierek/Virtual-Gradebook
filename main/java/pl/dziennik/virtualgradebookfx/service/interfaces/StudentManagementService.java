package pl.dziennik.virtualgradebookfx.service.interfaces;

import pl.dziennik.virtualgradebookfx.model.user.Student;

import java.util.List;

public interface StudentManagementService {
    List<Student> getAllStudents();
    List<Student> getStudentsWithoutClass();
    void assignStudentToClass(String studentLogin, String className);
    void removeStudentFromClass(String studentLogin);
    void addStudent(String login, String password, String firstName, String lastName, String schoolClass);
}
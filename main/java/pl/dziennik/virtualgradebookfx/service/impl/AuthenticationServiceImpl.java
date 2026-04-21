package pl.dziennik.virtualgradebookfx.service.impl;

import pl.dziennik.virtualgradebookfx.model.user.Dean;
import pl.dziennik.virtualgradebookfx.model.user.Role;
import pl.dziennik.virtualgradebookfx.model.user.Student;
import pl.dziennik.virtualgradebookfx.model.user.Teacher;
import pl.dziennik.virtualgradebookfx.model.user.User;
import pl.dziennik.virtualgradebookfx.persistence.DatabaseConnection;
import pl.dziennik.virtualgradebookfx.service.interfaces.AuthenticationService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthenticationServiceImpl implements AuthenticationService {

    @Override
    public User authenticate(String login, String password) {
        String sql = "SELECT * FROM users WHERE login = ? AND password = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, login);
            statement.setString(2, password);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String roleText = resultSet.getString("role");
                    Role role = Role.valueOf(roleText);

                    String userLogin = resultSet.getString("login");
                    String userPassword = resultSet.getString("password");
                    String firstName = resultSet.getString("first_name");
                    String lastName = resultSet.getString("last_name");

                    if (role == Role.STUDENT) {
                        String schoolClass = resultSet.getString("school_class");
                        return new Student(userLogin, userPassword, firstName, lastName, schoolClass);
                    }

                    if (role == Role.TEACHER) {
                        String subjectName = resultSet.getString("subject_name");
                        return new Teacher(userLogin, userPassword, firstName, lastName, subjectName);
                    }

                    if (role == Role.DEAN) {
                        return new Dean(userLogin, userPassword, firstName, lastName);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
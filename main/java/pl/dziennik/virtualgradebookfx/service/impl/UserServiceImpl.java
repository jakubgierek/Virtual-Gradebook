package pl.dziennik.virtualgradebookfx.service.impl;

import pl.dziennik.virtualgradebookfx.model.user.Dean;
import pl.dziennik.virtualgradebookfx.model.user.Role;
import pl.dziennik.virtualgradebookfx.model.user.Student;
import pl.dziennik.virtualgradebookfx.model.user.Teacher;
import pl.dziennik.virtualgradebookfx.model.user.User;
import pl.dziennik.virtualgradebookfx.persistence.DatabaseConnection;
import pl.dziennik.virtualgradebookfx.service.interfaces.UserService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserServiceImpl implements UserService {

    @Override
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users ORDER BY role, login";
        List<User> users = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                users.add(mapUser(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    @Override
    public List<User> getAllUsersExcept(String currentLogin) {
        String sql = "SELECT * FROM users WHERE login <> ? ORDER BY role, login";
        List<User> users = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, currentLogin);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    private User mapUser(ResultSet rs) throws Exception {
        String login = rs.getString("login");
        String password = rs.getString("password");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        Role role = Role.valueOf(rs.getString("role"));

        if (role == Role.STUDENT) {
            String schoolClass = rs.getString("school_class");
            return new Student(login, password, firstName, lastName, schoolClass);
        }

        if (role == Role.TEACHER) {
            String subjectName = rs.getString("subject_name");
            return new Teacher(login, password, firstName, lastName, subjectName);
        }

        return new Dean(login, password, firstName, lastName);
    }

    @Override
    public List<Teacher> getAllTeachers() {
        List<Teacher> teachers = new ArrayList<>();

        String sql = "SELECT * FROM users WHERE role = 'TEACHER' ORDER BY last_name, first_name";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                teachers.add(new Teacher(
                        resultSet.getString("login"),
                        resultSet.getString("password"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getString("subject_name")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return teachers;
    }

    @Override
    public void addTeacher(String login, String password, String firstName, String lastName, String subjectName) {
        String sql = "INSERT INTO users (login, password, first_name, last_name, role, school_class, subject_name) VALUES (?, ?, ?, ?, 'TEACHER', NULL, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, login);
            statement.setString(2, password);
            statement.setString(3, firstName);
            statement.setString(4, lastName);
            statement.setString(5, subjectName);

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTeacher(String login, String password, String firstName, String lastName, String subjectName) {
        String sql;
        boolean updatePassword = password != null && !password.isBlank();

        if (updatePassword) {
            sql = "UPDATE users SET password = ?, first_name = ?, last_name = ?, subject_name = ? WHERE login = ? AND role = 'TEACHER'";
        } else {
            sql = "UPDATE users SET first_name = ?, last_name = ?, subject_name = ? WHERE login = ? AND role = 'TEACHER'";
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (updatePassword) {
                statement.setString(1, password);
                statement.setString(2, firstName);
                statement.setString(3, lastName);
                statement.setString(4, subjectName);
                statement.setString(5, login);
            } else {
                statement.setString(1, firstName);
                statement.setString(2, lastName);
                statement.setString(3, subjectName);
                statement.setString(4, login);
            }

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTeacher(String login) {
        String sql = "DELETE FROM users WHERE login = ? AND role = 'TEACHER'";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, login);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
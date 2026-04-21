package pl.dziennik.virtualgradebookfx.service.impl;

import pl.dziennik.virtualgradebookfx.persistence.DatabaseConnection;
import pl.dziennik.virtualgradebookfx.service.interfaces.ClassManagementService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClassManagementServiceImpl implements ClassManagementService {

    @Override
    public List<String> getAllClassNames() {
        List<String> classes = new ArrayList<>();

        String sql = "SELECT class_name FROM school_classes ORDER BY class_name";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                classes.add(resultSet.getString("class_name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return classes;
    }

    @Override
    public void addClass(String className, String description) {
        String sql = "INSERT INTO school_classes (class_name, description) VALUES (?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, className);
            statement.setString(2, description);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteClass(String className) {
        String sql = "DELETE FROM school_classes WHERE class_name = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, className);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
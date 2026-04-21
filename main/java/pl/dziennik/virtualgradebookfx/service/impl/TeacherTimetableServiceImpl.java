package pl.dziennik.virtualgradebookfx.service.impl;

import pl.dziennik.virtualgradebookfx.model.school.TimetableEntry;
import pl.dziennik.virtualgradebookfx.persistence.DatabaseConnection;
import pl.dziennik.virtualgradebookfx.service.interfaces.TeacherTimetableService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TeacherTimetableServiceImpl implements TeacherTimetableService {

    @Override
    public List<TimetableEntry> getTimetableForTeacher(String teacherLogin) {
        List<TimetableEntry> entries = new ArrayList<>();

        String sql = "SELECT * FROM timetable WHERE teacher_login = ? ORDER BY " +
                "FIELD(day_of_week, 'Poniedziałek', 'Wtorek', 'Środa', 'Czwartek', 'Piątek'), start_time";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, teacherLogin);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    TimetableEntry entry = new TimetableEntry(
                            resultSet.getInt("id"),
                            resultSet.getString("class_name"),
                            resultSet.getString("day_of_week"),
                            resultSet.getString("start_time"),
                            resultSet.getString("end_time"),
                            resultSet.getString("subject_name"),
                            resultSet.getString("teacher_name"),
                            resultSet.getString("room")
                    );
                    entries.add(entry);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return entries;
    }
}
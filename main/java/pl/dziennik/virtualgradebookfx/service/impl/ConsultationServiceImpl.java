package pl.dziennik.virtualgradebookfx.service.impl;

import pl.dziennik.virtualgradebookfx.model.school.Consultation;
import pl.dziennik.virtualgradebookfx.persistence.DatabaseConnection;
import pl.dziennik.virtualgradebookfx.service.interfaces.ConsultationService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConsultationServiceImpl implements ConsultationService {

    @Override
    public List<Consultation> getAllConsultations() {
        List<Consultation> consultations = new ArrayList<>();

        String sql = "SELECT * FROM consultations ORDER BY day_of_week, start_time";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Consultation consultation = new Consultation(
                        resultSet.getInt("id"),
                        resultSet.getString("teacher_name"),
                        resultSet.getString("subject_name"),
                        resultSet.getString("day_of_week"),
                        resultSet.getString("start_time"),
                        resultSet.getString("end_time"),
                        resultSet.getString("room")
                );
                consultations.add(consultation);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return consultations;
    }
}
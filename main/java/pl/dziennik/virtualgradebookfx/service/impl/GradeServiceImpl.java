package pl.dziennik.virtualgradebookfx.service.impl;

import pl.dziennik.virtualgradebookfx.model.school.Grade;
import pl.dziennik.virtualgradebookfx.model.school.StudentSubject;
import pl.dziennik.virtualgradebookfx.persistence.DatabaseConnection;
import pl.dziennik.virtualgradebookfx.service.interfaces.GradeService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GradeServiceImpl implements GradeService {

    @Override
    public List<Grade> getGradesForStudent(String studentLogin) {
        List<Grade> grades = new ArrayList<>();

        String sql = "SELECT * FROM grades WHERE student_login = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, studentLogin);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Grade grade = new Grade(
                            resultSet.getInt("id"),
                            resultSet.getString("student_login"),
                            resultSet.getString("subject_name"),
                            resultSet.getDouble("grade_value"),
                            resultSet.getInt("grade_weight"),
                            resultSet.getString("description"),
                            resultSet.getString("teacher_login")
                    );
                    grades.add(grade);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return grades;
    }

    @Override
    public List<Grade> getGradesForStudentAndSubject(String studentLogin, String subjectName) {
        List<Grade> grades = new ArrayList<>();

        String sql = "SELECT * FROM grades WHERE student_login = ? AND subject_name = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, studentLogin);
            statement.setString(2, subjectName);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Grade grade = new Grade(
                            resultSet.getInt("id"),
                            resultSet.getString("student_login"),
                            resultSet.getString("subject_name"),
                            resultSet.getDouble("grade_value"),
                            resultSet.getInt("grade_weight"),
                            resultSet.getString("description"),
                            resultSet.getString("teacher_login")
                    );
                    grades.add(grade);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return grades;
    }

    @Override
    public List<String> getSubjectsForStudent(String studentLogin) {
        List<String> subjects = new ArrayList<>();

        String sql = "SELECT subject_name FROM student_subjects WHERE student_login = ? ORDER BY subject_name";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, studentLogin);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    subjects.add(resultSet.getString("subject_name"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return subjects;
    }

    @Override
    public List<StudentSubject> getStudentSubjects(String studentLogin) {
        List<StudentSubject> subjects = new ArrayList<>();

        String sql = "SELECT * FROM student_subjects WHERE student_login = ? ORDER BY subject_name";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, studentLogin);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    StudentSubject subject = new StudentSubject(
                            resultSet.getInt("id"),
                            resultSet.getString("student_login"),
                            resultSet.getString("subject_name"),
                            resultSet.getInt("ects")
                    );
                    subjects.add(subject);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return subjects;
    }

    @Override
    public double calculateWeightedAverage(List<Grade> grades) {
        if (grades.isEmpty()) {
            return 0.0;
        }

        double weightedSum = 0.0;
        int totalWeight = 0;

        for (Grade grade : grades) {
            weightedSum += grade.getGradeValue() * grade.getGradeWeight();
            totalWeight += grade.getGradeWeight();
        }

        if (totalWeight == 0) {
            return 0.0;
        }

        return weightedSum / totalWeight;
    }

    @Override
    public double calculateOverallEctsAverage(String studentLogin) {
        List<StudentSubject> subjects = getStudentSubjects(studentLogin);

        double weightedSum = 0.0;
        int totalEcts = 0;

        for (StudentSubject subject : subjects) {
            List<Grade> grades = getGradesForStudentAndSubject(studentLogin, subject.getSubjectName());

            if (!grades.isEmpty()) {
                double subjectAverage = calculateWeightedAverage(grades);
                weightedSum += subjectAverage * subject.getEcts();
                totalEcts += subject.getEcts();
            }
        }

        if (totalEcts == 0) {
            return 0.0;
        }

        return weightedSum / totalEcts;
    }

    @Override
    public List<Grade> getGradesByTeacher(String teacherLogin) {
        List<Grade> grades = new ArrayList<>();

        String sql = "SELECT * FROM grades WHERE teacher_login = ? ORDER BY student_login, subject_name, id DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, teacherLogin);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Grade grade = new Grade(
                            resultSet.getInt("id"),
                            resultSet.getString("student_login"),
                            resultSet.getString("subject_name"),
                            resultSet.getDouble("grade_value"),
                            resultSet.getInt("grade_weight"),
                            resultSet.getString("description"),
                            resultSet.getString("teacher_login")
                    );
                    grades.add(grade);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return grades;
    }

    @Override
    public void addGrade(String studentLogin, String subjectName, double gradeValue, int gradeWeight, String description, String teacherLogin) {
        String sql = "INSERT INTO grades (student_login, subject_name, grade_value, grade_weight, description, teacher_login) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, studentLogin);
            statement.setString(2, subjectName);
            statement.setDouble(3, gradeValue);
            statement.setInt(4, gradeWeight);
            statement.setString(5, description);
            statement.setString(6, teacherLogin);

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateGrade(int gradeId, double gradeValue, int gradeWeight, String description) {
        String sql = "UPDATE grades SET grade_value = ?, grade_weight = ?, description = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, gradeValue);
            statement.setInt(2, gradeWeight);
            statement.setString(3, description);
            statement.setInt(4, gradeId);

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteGrade(int gradeId) {
        String sql = "DELETE FROM grades WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, gradeId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Grade> getGradesForStudentSubjectAndTeacher(String studentLogin, String subjectName, String teacherLogin) {
        List<Grade> grades = new ArrayList<>();

        String sql = "SELECT * FROM grades WHERE student_login = ? AND subject_name = ? AND teacher_login = ? ORDER BY id DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, studentLogin);
            statement.setString(2, subjectName);
            statement.setString(3, teacherLogin);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Grade grade = new Grade(
                            resultSet.getInt("id"),
                            resultSet.getString("student_login"),
                            resultSet.getString("subject_name"),
                            resultSet.getDouble("grade_value"),
                            resultSet.getInt("grade_weight"),
                            resultSet.getString("description"),
                            resultSet.getString("teacher_login")
                    );
                    grades.add(grade);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return grades;
    }
}
package pl.dziennik.virtualgradebookfx.network.client;

import pl.dziennik.virtualgradebookfx.model.school.Grade;
import pl.dziennik.virtualgradebookfx.model.school.StudentSubject;
import pl.dziennik.virtualgradebookfx.network.common.Request;
import pl.dziennik.virtualgradebookfx.network.common.RequestType;
import pl.dziennik.virtualgradebookfx.network.common.Response;
import pl.dziennik.virtualgradebookfx.service.interfaces.GradeService;

import java.util.Collections;
import java.util.List;

public class RemoteGradeService implements GradeService {

    private final NetworkClient networkClient = new NetworkClient();

    @Override
    public List<Grade> getGradesForStudent(String studentLogin) {
        throw new UnsupportedOperationException("Nieużywane w wersji sieciowej");
    }

    @Override
    public List<Grade> getGradesForStudentAndSubject(String studentLogin, String subjectName) {
        Request request = new Request(RequestType.GET_STUDENT_GRADES)
                .add("studentLogin", studentLogin)
                .add("subjectName", subjectName);

        Response response = networkClient.send(request);

        if (!response.isSuccess()) {
            return Collections.emptyList();
        }

        return (List<Grade>) response.getData();
    }

    @Override
    public List<String> getSubjectsForStudent(String studentLogin) {
        throw new UnsupportedOperationException("Nieużywane w wersji sieciowej");
    }

    @Override
    public List<StudentSubject> getStudentSubjects(String studentLogin) {
        Request request = new Request(RequestType.GET_STUDENT_SUBJECTS)
                .add("studentLogin", studentLogin);

        Response response = networkClient.send(request);

        if (!response.isSuccess()) {
            return Collections.emptyList();
        }

        return (List<StudentSubject>) response.getData();
    }

    @Override
    public double calculateWeightedAverage(List<Grade> grades) {
        if (grades == null || grades.isEmpty()) {
            return 0.0;
        }

        double weightedSum = 0.0;
        int totalWeight = 0;

        for (Grade grade : grades) {
            weightedSum += grade.getGradeValue() * grade.getGradeWeight();
            totalWeight += grade.getGradeWeight();
        }

        return totalWeight == 0 ? 0.0 : weightedSum / totalWeight;
    }

    @Override
    public double calculateOverallEctsAverage(String studentLogin) {
        List<StudentSubject> subjects = getStudentSubjects(studentLogin);

        if (subjects.isEmpty()) {
            return 0.0;
        }

        double weightedSum = 0.0;
        int totalEcts = 0;

        for (StudentSubject subject : subjects) {
            List<Grade> grades = getGradesForStudentAndSubject(studentLogin, subject.getSubjectName());
            if (!grades.isEmpty()) {
                double avg = calculateWeightedAverage(grades);
                weightedSum += avg * subject.getEcts();
                totalEcts += subject.getEcts();
            }
        }

        return totalEcts == 0 ? 0.0 : weightedSum / totalEcts;
    }

    @Override
    public List<Grade> getGradesByTeacher(String teacherLogin) {
        throw new UnsupportedOperationException("Na razie nie przepinamy tego przez sieć");
    }

    @Override
    public void addGrade(String studentLogin, String subjectName, double gradeValue, int gradeWeight, String description, String teacherLogin) {
        throw new UnsupportedOperationException("Na razie nie przepinamy tego przez sieć");
    }

    @Override
    public void updateGrade(int gradeId, double gradeValue, int gradeWeight, String description) {
        throw new UnsupportedOperationException("Na razie nie przepinamy tego przez sieć");
    }

    @Override
    public void deleteGrade(int gradeId) {
        throw new UnsupportedOperationException("Na razie nie przepinamy tego przez sieć");
    }

    @Override
    public List<Grade> getGradesForStudentSubjectAndTeacher(String studentLogin, String subjectName, String teacherLogin) {
        throw new UnsupportedOperationException("Na razie nie przepinamy tego przez sieć");
    }
}
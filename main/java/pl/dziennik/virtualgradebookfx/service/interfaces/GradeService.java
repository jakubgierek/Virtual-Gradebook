package pl.dziennik.virtualgradebookfx.service.interfaces;

import pl.dziennik.virtualgradebookfx.model.school.Grade;
import pl.dziennik.virtualgradebookfx.model.school.StudentSubject;

import java.util.List;

public interface GradeService {
    List<Grade> getGradesForStudent(String studentLogin);
    List<Grade> getGradesForStudentAndSubject(String studentLogin, String subjectName);
    List<String> getSubjectsForStudent(String studentLogin);
    List<StudentSubject> getStudentSubjects(String studentLogin);
    double calculateWeightedAverage(List<Grade> grades);
    double calculateOverallEctsAverage(String studentLogin);

    List<Grade> getGradesByTeacher(String teacherLogin);
    void addGrade(String studentLogin, String subjectName, double gradeValue, int gradeWeight, String description, String teacherLogin);
    void updateGrade(int gradeId, double gradeValue, int gradeWeight, String description);
    void deleteGrade(int gradeId);
    List<Grade> getGradesForStudentSubjectAndTeacher(String studentLogin, String subjectName, String teacherLogin);
}
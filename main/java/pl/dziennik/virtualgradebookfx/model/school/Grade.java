package pl.dziennik.virtualgradebookfx.model.school;
import java.io.Serializable;

public class Grade implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String studentLogin;
    private String subjectName;
    private double gradeValue;
    private int gradeWeight;
    private String description;
    private String teacherLogin;

    public Grade() {
    }

    public Grade(int id, String studentLogin, String subjectName, double gradeValue, int gradeWeight, String description, String teacherLogin) {
        this.id = id;
        this.studentLogin = studentLogin;
        this.subjectName = subjectName;
        this.gradeValue = gradeValue;
        this.gradeWeight = gradeWeight;
        this.description = description;
        this.teacherLogin = teacherLogin;
    }

    public int getId() {
        return id;
    }

    public String getStudentLogin() {
        return studentLogin;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public double getGradeValue() {
        return gradeValue;
    }

    public int getGradeWeight() {
        return gradeWeight;
    }

    public String getDescription() {
        return description;
    }

    public String getTeacherLogin() {
        return teacherLogin;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStudentLogin(String studentLogin) {
        this.studentLogin = studentLogin;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public void setGradeValue(double gradeValue) {
        this.gradeValue = gradeValue;
    }

    public void setGradeWeight(int gradeWeight) {
        this.gradeWeight = gradeWeight;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTeacherLogin(String teacherLogin) {
        this.teacherLogin = teacherLogin;
    }
}
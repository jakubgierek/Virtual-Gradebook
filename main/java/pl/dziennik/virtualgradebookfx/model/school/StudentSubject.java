package pl.dziennik.virtualgradebookfx.model.school;
import java.io.Serializable;

public class StudentSubject implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String studentLogin;
    private String subjectName;
    private int ects;

    public StudentSubject() {
    }

    public StudentSubject(int id, String studentLogin, String subjectName, int ects) {
        this.id = id;
        this.studentLogin = studentLogin;
        this.subjectName = subjectName;
        this.ects = ects;
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

    public int getEcts() {
        return ects;
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

    public void setEcts(int ects) {
        this.ects = ects;
    }
}
package pl.dziennik.virtualgradebookfx.model.school;
import java.io.Serializable;

public class Consultation implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String teacherName;
    private String subjectName;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private String room;

    public Consultation() {
    }

    public Consultation(int id, String teacherName, String subjectName, String dayOfWeek, String startTime, String endTime, String room) {
        this.id = id;
        this.teacherName = teacherName;
        this.subjectName = subjectName;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
    }

    public int getId() {
        return id;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getRoom() {
        return room;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setRoom(String room) {
        this.room = room;
    }
}
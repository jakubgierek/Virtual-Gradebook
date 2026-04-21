package pl.dziennik.virtualgradebookfx.model.school;
import java.io.Serializable;

public class TimetableEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String className;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private String subjectName;
    private String teacherName;
    private String room;

    public TimetableEntry() {
    }

    public TimetableEntry(int id, String className, String dayOfWeek, String startTime, String endTime,
                          String subjectName, String teacherName, String room) {
        this.id = id;
        this.className = className;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.subjectName = subjectName;
        this.teacherName = teacherName;
        this.room = room;
    }

    public int getId() {
        return id;
    }

    public String getClassName() {
        return className;
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

    public String getSubjectName() {
        return subjectName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public String getRoom() {
        return room;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setClassName(String className) {
        this.className = className;
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

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public void setRoom(String room) {
        this.room = room;
    }
}
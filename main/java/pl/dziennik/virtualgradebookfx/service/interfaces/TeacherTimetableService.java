package pl.dziennik.virtualgradebookfx.service.interfaces;

import pl.dziennik.virtualgradebookfx.model.school.TimetableEntry;

import java.util.List;

public interface TeacherTimetableService {
    List<TimetableEntry> getTimetableForTeacher(String teacherLogin);
}
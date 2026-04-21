package pl.dziennik.virtualgradebookfx.service.interfaces;

import pl.dziennik.virtualgradebookfx.model.school.TimetableEntry;

import java.util.List;

public interface TimetableService {
    List<TimetableEntry> getTimetableForClass(String className);
}
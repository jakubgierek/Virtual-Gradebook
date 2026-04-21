package pl.dziennik.virtualgradebookfx.service.interfaces;

import java.util.List;

public interface ClassManagementService {
    List<String> getAllClassNames();
    void addClass(String className, String description);
    void deleteClass(String className);
}
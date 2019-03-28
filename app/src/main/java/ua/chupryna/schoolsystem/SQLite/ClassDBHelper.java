package ua.chupryna.schoolsystem.SQLite;

import ua.chupryna.schoolsystem.model.SchoolClass;

public interface ClassDBHelper {
    long addClass(SchoolClass schoolClass);
    boolean deleteClass(long id);
    long getClassID(SchoolClass schoolClass);
    SchoolClass getClassByID(long id);
}

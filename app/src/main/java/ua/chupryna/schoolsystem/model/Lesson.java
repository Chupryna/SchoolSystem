package ua.chupryna.schoolsystem.model;


public class Lesson
{
    private String subject;
    private String teacherLastName;
    private String teacherName;
    private String teacherSurName;
    private int dayID;
    private String day;
    private int number;
    private String beginTime;
    private String endTime;
    private SchoolClass schoolClass;

    public Lesson() {
    }

    public Lesson(String subject, String teacherLastName, String teacherName, String teacherSurName, String day, int number, String beginTime, String endTime, SchoolClass schoolClass) {
        this.subject = subject;
        this.teacherLastName = teacherLastName;
        this.teacherName = teacherName;
        this.teacherSurName = teacherSurName;
        this.day = day;
        this.number = number;
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.schoolClass = schoolClass;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTeacherLastName() {
        return teacherLastName;
    }

    public void setTeacherLastName(String teacherLastName) {
        this.teacherLastName = teacherLastName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getTeacherSurName() {
        return teacherSurName;
    }

    public void setTeacherSurName(String teacherSurName) {
        this.teacherSurName = teacherSurName;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public SchoolClass getSchoolClass() {
        return schoolClass;
    }

    public void setSchoolClass(SchoolClass schoolClass) {
        this.schoolClass = schoolClass;
    }

    public int getDayID() {
        return dayID;
    }

    public void setDayID(int dayID) {
        this.dayID = dayID;
    }
}
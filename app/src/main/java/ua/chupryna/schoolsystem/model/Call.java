package ua.chupryna.schoolsystem.model;


public class Call
{
    private int number;
    private String beginTime;
    private String endTime;
    private String breakBetweenLesson;
    private int schoolID;

    public Call() {
        breakBetweenLesson = "-";
    }

    public Call(int number, String beginTime, String endTime, String breakBetweenLesson, int schoolID) {
        this.number = number;
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.breakBetweenLesson = breakBetweenLesson;
        this.schoolID = schoolID;
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

    public int getSchoolID() {
        return schoolID;
    }

    public void setSchoolID(int schoolID) {
        this.schoolID = schoolID;
    }

    public String getBreakBetweenLesson() {
        return breakBetweenLesson;
    }

    public void setBreakBetweenLesson(String breakBetweenLesson) {
        this.breakBetweenLesson = breakBetweenLesson;
    }
}

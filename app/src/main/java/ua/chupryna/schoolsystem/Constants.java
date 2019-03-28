package ua.chupryna.schoolsystem;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ru.tinkoff.decoro.slots.PredefinedSlots;
import ru.tinkoff.decoro.slots.Slot;


public class Constants
{
    public static final String UTF_8 = "UTF-8";
    public static final String URL_AUTHORIZATION = "http://android-test.ho.ua/android/scripts/authorization.php";
    public static final String URL_RECOVERY = "http://android-test.ho.ua/android/scripts/password-recovery.php";
    public static final String URL_CODE_VERIFICATION = "http://android-test.ho.ua/android/scripts/code-verification.php";
    public static final String URL_NEW_PASSWORD = "http://android-test.ho.ua/android/scripts/new-password.php";
    public static final String URL_GET_SCHOOLS = "http://android-test.ho.ua/android/scripts/get-schools.php";
    public static final String URL_GET_NEWS = "http://android-test.ho.ua/android/scripts/get-news.php";
    public static final String URL_GET_CALLS = "http://android-test.ho.ua/android/scripts/get-calls.php";
    public static final String URL_GET_TEACHERS = "http://android-test.ho.ua/android/scripts/get-teachers.php";
    static final String URL_GET_CLASS = "http://android-test.ho.ua/android/scripts/get-class.php";
    public static final String URL_GET_CLASSES = "http://android-test.ho.ua/android/scripts/get-classes.php";
    public static final String URL_GET_LESSONS_BY_CLASS = "http://android-test.ho.ua/android/scripts/get-lessons-by-class.php";
    public static final String URL_GET_LESSONS_BY_TEACHER = "http://android-test.ho.ua/android/scripts/get-lessons-by-teacher.php";
    public static final String URL_GET_SUBJECTS = "http://android-test.ho.ua/android/scripts/get-subjects.php";
    public static final String URL_GET_SUCCESS_BY_DATE = "http://android-test.ho.ua/android/scripts/get-success-by-date.php";
    public static final String URL_GET_SUCCESS_BY_SUBJECT = "http://android-test.ho.ua/android/scripts/get-success-by-subject.php";
    public static final String URL_CHANGE_PHONE = "http://android-test.ho.ua/android/scripts/update-telephone.php";
    public static final String URL_GET_SCHOOL_INFO = "http://android-test.ho.ua/android/scripts/get-school-info.php";
    public static final String URL_CHANGE_PASSWORD = "http://android-test.ho.ua/android/scripts/update-password.php";
    public static final String URL_CHANGE_PHOTO = "http://android-test.ho.ua/android/scripts/update-photo.php";
    public static final String URL_DOWNLOAD_IMAGE = "http://android-test.ho.ua/android/scripts/download-image.php";

    public static final int PUPIL_ID = 1;
    public static final int PARENT_ID = 2;
    public static final int TEACHER_ID = 3;
    public static final int DIRECTOR_ID = 4;
    public static final int ADMIN_ID = 5;

    public static final Slot[] UA_PHONE_NUMBER = {
            PredefinedSlots.hardcodedSlot('+').withTags(Slot.TAG_DECORATION),
            PredefinedSlots.hardcodedSlot('3'),
            PredefinedSlots.hardcodedSlot('8'),
            PredefinedSlots.hardcodedSlot(' ').withTags(Slot.TAG_DECORATION),
            PredefinedSlots.hardcodedSlot('0'),
            PredefinedSlots.digit(),
            PredefinedSlots.digit(),
            PredefinedSlots.hardcodedSlot(' ').withTags(Slot.TAG_DECORATION),
            PredefinedSlots.digit(),
            PredefinedSlots.digit(),
            PredefinedSlots.digit(),
            PredefinedSlots.hardcodedSlot('-').withTags(Slot.TAG_DECORATION),
            PredefinedSlots.digit(),
            PredefinedSlots.digit(),
            PredefinedSlots.hardcodedSlot('-').withTags(Slot.TAG_DECORATION),
            PredefinedSlots.digit(),
            PredefinedSlots.digit(),
    };

    public static boolean isNetworkAvailable(Activity activity)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null)
            return false;
        else {
            NetworkInfo networkInfo[] = connectivityManager.getAllNetworkInfo();
            if (networkInfo != null)
            {
                for (NetworkInfo info : networkInfo)
                    if (info.getState() == NetworkInfo.State.CONNECTED)
                        return true;
            }
        }
        return false;
    }

    public static String parseTelephone(String telephone)
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < telephone.length(); i++) {
            char symbol = telephone.charAt(i);
            if (Character.isDigit(symbol))
                stringBuilder.append(symbol);
        }
        return stringBuilder.toString();
    }

    public static String parseDate(Date date, Context context) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        String[] days = context.getResources().getStringArray(R.array.days_abbreviated);
        String months[] = context.getResources().getStringArray(R.array.months);

        int day = calendar.get(Calendar.DAY_OF_WEEK)==1 ? 7 : calendar.get(Calendar.DAY_OF_WEEK)-1;
        String dayOfWeek = days[day-1];
        String month = months[calendar.get(Calendar.MONTH)];

        return String.format(Locale.getDefault(), "%s %s, %s, %02d:%02d", calendar.get(Calendar.DAY_OF_MONTH), month,
                dayOfWeek, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
    }

    public static String getFileNameFromPath(String path)
    {
        String fileName;
        int index = path.lastIndexOf('/');
        if (index >= 0)
            fileName = path.substring(index+1);
        else
            fileName = path;

        return fileName;
    }
}
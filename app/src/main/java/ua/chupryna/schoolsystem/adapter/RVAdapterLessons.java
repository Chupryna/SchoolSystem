package ua.chupryna.schoolsystem.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ua.chupryna.schoolsystem.model.Lesson;
import ua.chupryna.schoolsystem.R;


public class RVAdapterLessons extends RecyclerView.Adapter<RVAdapterLessons.LessonViewHolder>
{
    private ArrayList<Lesson> listLessons;

    public RVAdapterLessons(ArrayList<Lesson> listLessons) {
        this.listLessons = listLessons;
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.timetable_lesson_item, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int i)
    {
        String time = String.format("%s - %s", listLessons.get(i).getBeginTime(), listLessons.get(i).getEndTime());
        String subtitle;
        if (listLessons.get(i).getSchoolClass() != null)
            subtitle = listLessons.get(i).getSchoolClass().toString();
        else
            subtitle = String.format("%s %s. %s.", listLessons.get(i).getTeacherLastName(),
                    listLessons.get(i).getTeacherName().substring(0,1), listLessons.get(i).getTeacherSurName().substring(0,1));

        holder.textLessonNumberView.setText(String.valueOf(listLessons.get(i).getNumber()));
        holder.textLessonTimeView.setText(time);
        holder.textLessonSubjectView.setText(listLessons.get(i).getSubject());
        holder.textLessonTeacherView.setText(subtitle);
    }

    @Override
    public int getItemCount() {
        return listLessons.size();
    }

    class LessonViewHolder extends RecyclerView.ViewHolder
    {
        private TextView textLessonNumberView;
        private TextView textLessonTimeView;
        private TextView textLessonSubjectView;
        private TextView textLessonTeacherView;

        LessonViewHolder(View itemView) {
            super(itemView);
            textLessonNumberView = itemView.findViewById(R.id.text_lesson_number);
            textLessonTimeView = itemView.findViewById(R.id.text_lesson_time);
            textLessonSubjectView = itemView.findViewById(R.id.text_lesson_subject);
            textLessonTeacherView = itemView.findViewById(R.id.text_lesson_teacher);
        }
    }
}
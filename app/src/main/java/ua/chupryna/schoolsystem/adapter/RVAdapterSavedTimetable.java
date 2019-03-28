package ua.chupryna.schoolsystem.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ua.chupryna.schoolsystem.activity.TimetableLessonActivity;
import ua.chupryna.schoolsystem.model.SavedTimetable;
import ua.chupryna.schoolsystem.R;
import ua.chupryna.schoolsystem.SQLite.DBDataSource;

public class RVAdapterSavedTimetable extends RecyclerView.Adapter<RVAdapterSavedTimetable.SavedTimetableViewHolder>
{
    private ArrayList<SavedTimetable> listSavedTimetable;
    private Context context;

    public RVAdapterSavedTimetable(ArrayList<SavedTimetable> listSavedTimetable, Context context) {
        this.listSavedTimetable = listSavedTimetable;
        this.context = context;
    }

    public void setListSavedTimetable(ArrayList<SavedTimetable> listSavedTimetable) {
        this.listSavedTimetable = listSavedTimetable;
    }

    @NonNull
    @Override
    public SavedTimetableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.saved_timetables_item, parent, false);
        return new SavedTimetableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SavedTimetableViewHolder holder, int position) {
        final SavedTimetable savedTimetable = listSavedTimetable.get(position);
        String categoryTimetable;
        if (savedTimetable.getCategoryTimetable() == SavedTimetable.SCHOOL_CLASS)
            categoryTimetable = "Розклад для учнів";
        else
            categoryTimetable = "Розклад для вчителів";

        holder.textCategoryTimetable.setText(categoryTimetable);
        holder.textNameTimetable.setText(savedTimetable.getNameTimetable());
        holder.textDateSaved.setText(savedTimetable.getSavedDate());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TimetableLessonActivity.class);
                intent.putExtra("timetableID", savedTimetable.getId());
                intent.putExtra("isSavedTimetable", true);
                intent.putExtra("selectedCriterion", savedTimetable.getCategoryTimetable());
                context.startActivity(intent);
            }
        });

        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return listSavedTimetable.size();
    }

    boolean remove(int position) {
        boolean result = false;
        DBDataSource dbDataSource = new DBDataSource(context);
        dbDataSource.open();
        if (dbDataSource.deleteTimetable(listSavedTimetable.get(position).getId())) {
            listSavedTimetable.remove(position);
            notifyItemRemoved(position);
            result = true;
        }
        dbDataSource.close();
        return result;
    }

    class SavedTimetableViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView textCategoryTimetable;
        private TextView textNameTimetable;
        private TextView textDateSaved;

        SavedTimetableViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_saved_timetable);
            textCategoryTimetable = itemView.findViewById(R.id.text_category_timetable);
            textNameTimetable = itemView.findViewById(R.id.text_name_timetable);
            textDateSaved = itemView.findViewById(R.id.text_date_saved_timetable);
        }
    }
}
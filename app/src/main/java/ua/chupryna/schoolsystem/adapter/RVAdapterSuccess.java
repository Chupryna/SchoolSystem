package ua.chupryna.schoolsystem.adapter;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ua.chupryna.schoolsystem.activity.ChoiceSuccessActivity;
import ua.chupryna.schoolsystem.model.Success;
import ua.chupryna.schoolsystem.R;

public class RVAdapterSuccess extends RecyclerView.Adapter<RVAdapterSuccess.SuccessViewHolder>
{
    private ArrayList<Success> listSuccess;
    private Context context;
    private int selectedCriterion;

    public RVAdapterSuccess(ArrayList<Success> listSuccess, Context context, int selectedCriterion) {
        this.listSuccess = listSuccess;
        this.context = context;
        this.selectedCriterion = selectedCriterion;
    }

    @Override
    public RVAdapterSuccess.SuccessViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.success_item, parent, false);
        return new SuccessViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RVAdapterSuccess.SuccessViewHolder holder, int position)
    {
        Success success = listSuccess.get(position);
        holder.textNumberLesson.setText(String.valueOf(success.getNumberLesson()));
        holder.textPresence.setText(success.getPresenceStatus());

        if (selectedCriterion == ChoiceSuccessActivity.DATE)
            holder.textInfoLesson.setText(success.getSubject());
        else
            holder.textInfoLesson.setText(success.getDate());

        int color;
        int rating = success.getRating();

        if (rating == 0) {
            holder.textRating.setText("-");
            return;
        } else if (rating < 4)
            color = context.getResources().getColor(R.color.rating_1_3);
        else if (rating < 7)
            color = context.getResources().getColor(R.color.rating_4_6);
        else if (rating < 10)
            color = context.getResources().getColor(R.color.rating_7_9);
        else
            color = context.getResources().getColor(R.color.rating_10_12);

        holder.textRating.setText(String.valueOf(rating));
        holder.textRating.setBackgroundColor(color);
    }

    @Override
    public int getItemCount() {
        return listSuccess.size();
    }

    class SuccessViewHolder extends RecyclerView.ViewHolder
    {
        private TextView textNumberLesson;
        private TextView textInfoLesson;
        private TextView textPresence;
        private TextView textRating;

        SuccessViewHolder(View itemView) {
            super(itemView);
            textNumberLesson = itemView.findViewById(R.id.text_success_number_lesson);
            textInfoLesson = itemView.findViewById(R.id.text_success_info_lesson);
            textPresence = itemView.findViewById(R.id.text_success_presence);
            textRating = itemView.findViewById(R.id.text_success_rating);
        }
    }
}

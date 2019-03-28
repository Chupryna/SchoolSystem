package ua.chupryna.schoolsystem.adapter;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ua.chupryna.schoolsystem.model.Call;
import ua.chupryna.schoolsystem.R;

public class RVAdapterCalls extends RecyclerView.Adapter<RVAdapterCalls.CallsViewHolder>
{
    private List<Call> list;

    public RVAdapterCalls(List<Call> list) {
        this.list = list;
    }

    @Override
    public RVAdapterCalls.CallsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.timetable_calls_item, parent, false);
        return new CallsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RVAdapterCalls.CallsViewHolder holder, int position) {
        holder.textNumberView.setText(String.valueOf(list.get(position).getNumber()));
        String time = String.format("%s - %s", list.get(position).getBeginTime(), list.get(position).getEndTime());
        holder.textTimeView.setText(time);
        holder.textBreakView.setText(list.get(position).getBreakBetweenLesson());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    class CallsViewHolder extends RecyclerView.ViewHolder
    {
        private TextView textNumberView;
        private TextView textTimeView;
        private TextView textBreakView;

        CallsViewHolder(View itemView)
        {
            super(itemView);
            textNumberView = itemView.findViewById(R.id.text_calls_number);
            textTimeView = itemView.findViewById(R.id.text_calls_time);
            textBreakView = itemView.findViewById(R.id.text_calls_break);
        }
    }
}

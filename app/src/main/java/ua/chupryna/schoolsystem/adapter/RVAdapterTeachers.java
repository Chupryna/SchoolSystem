package ua.chupryna.schoolsystem.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ua.chupryna.schoolsystem.activity.ProfileActivity;
import ua.chupryna.schoolsystem.model.User;
import ua.chupryna.schoolsystem.R;


public class RVAdapterTeachers extends RecyclerView.Adapter<RVAdapterTeachers.TeachersViewHolder> implements Filterable
{
    private ArrayList<User> originalListTeachers;
    private ArrayList<User> listTeachers;
    private Context context;

    public RVAdapterTeachers(ArrayList<User> listTeachers, Context context) {
        this.originalListTeachers = listTeachers;
        this.listTeachers = listTeachers;
        this.context = context;
    }

    @NonNull
    @Override
    public TeachersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.teachers_item, parent, false);
        return new TeachersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final TeachersViewHolder holder, int i)
    {
        String name = String.format("%s %s %s", listTeachers.get(i).getLastName(), listTeachers.get(i).getName(),
                listTeachers.get(i).getSurName());
        holder.textNameTeacherView.setText(name);

        String photoPath = listTeachers.get(i).getPhotoPathOnDevice();
        if (photoPath == null)
            holder.imageView.setImageResource(R.drawable.profile);
        else
            holder.imageView.setImageURI(Uri.parse(photoPath));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("user", listTeachers.get(holder.getAdapterPosition()));
                intent.putExtra("myProfile", false);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listTeachers.size();
    }

    @Override
    public Filter getFilter()
    {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                ArrayList<User> filteredList = new ArrayList<>();
                String queryString = charSequence.toString();

                if (queryString.isEmpty()) {
                    filteredList = originalListTeachers;
                } else {
                    for (User teacher : originalListTeachers) {
                        String name = String.format("%s %s %s", teacher.getLastName(), teacher.getName(), teacher.getSurName());
                        if (name.toLowerCase().contains(queryString.toLowerCase())) {
                            filteredList.add(teacher);
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                listTeachers = (ArrayList<User>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    class TeachersViewHolder extends RecyclerView.ViewHolder
    {
        private ImageView imageView;
        private TextView textNameTeacherView;
        TeachersViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_teacher);
            textNameTeacherView = itemView.findViewById(R.id.text_teacher_name);
        }
    }
}

package ua.chupryna.schoolsystem.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ua.chupryna.schoolsystem.activity.NewsActivity;
import ua.chupryna.schoolsystem.Constants;
import ua.chupryna.schoolsystem.model.News;
import ua.chupryna.schoolsystem.R;


public class RVAdapterNews extends RecyclerView.Adapter<RVAdapterNews.NewsViewHolder>
{
    private List<News> listNews;
    private Context context;

    public RVAdapterNews(List<News> listNews, Context context) {
        this.listNews = listNews;
        this.context = context;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.news_item, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NewsViewHolder holder, final int position)
    {
        String date = Constants.parseDate(listNews.get(position).getDate(), context);
        holder.textDateView.setText(date);
        holder.textTitleView.setText(listNews.get(position).getTitle());

        String imagePath = listNews.get(position).getImagePathOnDevice();
        if (imagePath == null)
            holder.imageNews.setImageResource(R.drawable.news);
        else
            holder.imageNews.setImageURI(Uri.parse(imagePath));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, NewsActivity.class);
                intent.putExtra("news", listNews.get(holder.getAdapterPosition()));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listNews.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    class NewsViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageNews;
        private TextView textTitleView;
        private TextView textDateView;

        NewsViewHolder(View itemView) {
            super(itemView);
            imageNews = itemView.findViewById(R.id.image_news);
            textTitleView = itemView.findViewById(R.id.text_news_title);
            textDateView = itemView.findViewById(R.id.text_news_date);
        }
    }
}
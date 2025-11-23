package com.example.umeventplanner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umeventplanner.Post;
import com.example.umeventplanner.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ForumAdapter extends RecyclerView.Adapter<ForumAdapter.ForumViewHolder> {

    private Context context;
    private List<Post> postList;

    public ForumAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public ForumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new ForumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForumViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.tvUserName.setText(post.getUserName());
        holder.tvContent.setText(post.getContent());
        if (post.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
            holder.tvTimestamp.setText(sdf.format(post.getTimestamp().toDate()));
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ForumViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvTimestamp, tvContent;

        public ForumViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvContent = itemView.findViewById(R.id.tvContent);
        }
    }
}

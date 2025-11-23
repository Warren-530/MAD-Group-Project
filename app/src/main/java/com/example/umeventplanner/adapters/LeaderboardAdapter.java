package com.example.umeventplanner.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.umeventplanner.LeaderboardEntry;
import com.example.umeventplanner.R;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {

    private Context context;
    private List<LeaderboardEntry> leaderboardEntries;
    private OnLeaderboardClickListener listener;

    public interface OnLeaderboardClickListener {
        void onEventClick(String eventId);
    }

    public LeaderboardAdapter(Context context, List<LeaderboardEntry> leaderboardEntries, OnLeaderboardClickListener listener) {
        this.context = context;
        this.leaderboardEntries = leaderboardEntries;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.leaderboard_item, parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        LeaderboardEntry entry = leaderboardEntries.get(position);

        holder.tvRank.setText(String.valueOf(entry.getRank()));
        holder.tvEventName.setText(entry.getEventName());
        holder.tvScores.setText(String.format("Green: %.1f ★ | User: %.1f ★", entry.getGreenScore(), entry.getUserRating()));
        holder.tvFinalScore.setText(String.format("%.1f", entry.getFinalScore()));

        if (entry.getBannerUrl() != null && !entry.getBannerUrl().isEmpty()) {
            Glide.with(context).load(entry.getBannerUrl()).into(holder.ivEventBanner);
        } else {
            holder.ivEventBanner.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(entry.getEventId());
            }
        });

        switch (entry.getRank()) {
            case 1:
                holder.tvRank.setTextColor(Color.parseColor("#FFD700")); // Gold
                break;
            case 2:
                holder.tvRank.setTextColor(Color.parseColor("#C0C0C0")); // Silver
                break;
            case 3:
                holder.tvRank.setTextColor(Color.parseColor("#CD7F32")); // Bronze
                break;
            default:
                holder.tvRank.setTextColor(Color.BLACK);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return leaderboardEntries.size();
    }

    public static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvEventName, tvScores, tvFinalScore;
        ImageView ivEventBanner;

        public LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvScores = itemView.findViewById(R.id.tvScores);
            tvFinalScore = itemView.findViewById(R.id.tvFinalScore);
            ivEventBanner = itemView.findViewById(R.id.ivEventBanner);
        }
    }
}

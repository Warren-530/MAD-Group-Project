package com.example.umeventplanner.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umeventplanner.LeaderboardEntry;
import com.example.umeventplanner.R;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {

    private Context context;
    private List<LeaderboardEntry> leaderboardEntries;

    public LeaderboardAdapter(Context context, List<LeaderboardEntry> leaderboardEntries) {
        this.context = context;
        this.leaderboardEntries = leaderboardEntries;
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
        holder.tvOrganizerName.setText(entry.getEventName());
        holder.tvScore.setText(String.format("%.1f pts", entry.getScore()));
        holder.tvRank.setText(String.valueOf(entry.getRank()));

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
        TextView tvRank, tvOrganizerName, tvScore;

        public LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvOrganizerName = itemView.findViewById(R.id.tvOrganizerName);
            tvScore = itemView.findViewById(R.id.tvScore);
        }
    }
}

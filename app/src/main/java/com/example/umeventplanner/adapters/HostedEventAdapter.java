package com.example.umeventplanner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.umeventplanner.Event;
import com.example.umeventplanner.R;
import java.util.List;

public class HostedEventAdapter extends RecyclerView.Adapter<HostedEventAdapter.HostedEventViewHolder> {

    private final Context context;
    private final List<Event> eventList;
    private final OnHostedEventClickListener onHostedEventClickListener;

    public interface OnHostedEventClickListener {
        void onEventClick(Event event);
        void onEventLongClick(Event event);
        void onOpenForum(Event event);
    }

    public HostedEventAdapter(Context context, List<Event> eventList, OnHostedEventClickListener onHostedEventClickListener) {
        this.context = context;
        this.eventList = eventList;
        this.onHostedEventClickListener = onHostedEventClickListener;
    }

    @NonNull
    @Override
    public HostedEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_hosted_event, parent, false);
        return new HostedEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HostedEventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.tvEventTitle.setText(event.getTitle());
        holder.tvEventDate.setText(event.getDate());
        holder.tvEventLocation.setText(event.getLocation());
        holder.tvGreenScore.setText(String.format("%.1f", event.getSustainabilityScore()));

        if (event.getBannerUrl() != null && !event.getBannerUrl().isEmpty()) {
            Glide.with(context)
                .load(event.getBannerUrl())
                .into(holder.ivEventImage);
        } else {
            holder.ivEventImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onHostedEventClickListener != null) {
                onHostedEventClickListener.onEventClick(event);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onHostedEventClickListener != null) {
                onHostedEventClickListener.onEventLongClick(event);
            }
            return true;
        });

        holder.btnForum.setOnClickListener(v -> {
            if (onHostedEventClickListener != null) {
                onHostedEventClickListener.onOpenForum(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class HostedEventViewHolder extends RecyclerView.ViewHolder {
        ImageView ivEventImage;
        TextView tvEventTitle, tvEventDate, tvEventLocation, tvGreenScore;
        Button btnForum;

        HostedEventViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEventImage = itemView.findViewById(R.id.ivEventImage);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);
            tvGreenScore = itemView.findViewById(R.id.tvGreenScore);
            btnForum = itemView.findViewById(R.id.btnForum);
        }
    }
}

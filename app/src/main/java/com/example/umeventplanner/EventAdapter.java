package com.example.umeventplanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_STANDARD = 0;
    private static final int VIEW_TYPE_COMPACT = 1;

    private Context context;
    private List<Event> eventList; // This list will be filtered
    private List<Event> allEvents; // Backup of all events
    private OnEventClickListener onEventClickListener;
    private boolean isCompactView = false;

    public EventAdapter(Context context, List<Event> eventList, OnEventClickListener onEventClickListener) {
        this.context = context;
        this.eventList = eventList;
        this.allEvents = new ArrayList<>(eventList);
        this.onEventClickListener = onEventClickListener;
    }

    public void setCompactView(boolean isCompact) {
        isCompactView = isCompact;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return isCompactView ? VIEW_TYPE_COMPACT : VIEW_TYPE_STANDARD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_COMPACT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_event_compact, parent, false);
            return new CompactEventViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
            return new EventViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Event event = eventList.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_COMPACT) {
            CompactEventViewHolder compactHolder = (CompactEventViewHolder) holder;
            compactHolder.tvTitle.setText(event.getTitle());
            compactHolder.tvDate.setText(event.getDate());
            compactHolder.tvLocation.setText(event.getLocation());
            compactHolder.tvGreenScore.setText(String.format("%.1f ★", event.getSustainabilityScore()));
            if (event.getBannerUrl() != null && !event.getBannerUrl().isEmpty()) {
                Glide.with(context).load(event.getBannerUrl()).into(compactHolder.ivEventImage);
            }
        } else {
            EventViewHolder standardHolder = (EventViewHolder) holder;
            standardHolder.tvEventTitle.setText(event.getTitle());
            standardHolder.tvEventDate.setText(event.getDate());
            standardHolder.tvEventLocation.setText(event.getLocation());
            standardHolder.tvGreenScore.setText(String.format("%.1f", event.getSustainabilityScore()));
            if (event.getBannerUrl() != null && !event.getBannerUrl().isEmpty()) {
                Glide.with(context).load(event.getBannerUrl()).into(standardHolder.ivEventImage);
            }
        }
        holder.itemView.setOnClickListener(v -> {
            if (onEventClickListener != null) {
                onEventClickListener.onEventClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public void filter(String query, List<String> categories) {
        List<Event> filteredList = new ArrayList<>();
        for (Event event : allEvents) {
            boolean categoryMatch = categories.isEmpty() || (event.getCategories() != null && event.getCategories().stream().anyMatch(categories::contains));
            boolean queryMatch = event.getTitle().toLowerCase().contains(query.toLowerCase());
            if (categoryMatch && queryMatch) {
                filteredList.add(event);
            }
        }
        eventList.clear();
        eventList.addAll(filteredList);
        notifyDataSetChanged();
    }

    public void setAllEvents(List<Event> events) {
        allEvents.clear();
        allEvents.addAll(events);
        filter("", new ArrayList<>()); // Apply initial empty filter
    }
    
    // Standard ViewHolder
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView ivEventImage;
        TextView tvEventTitle, tvEventDate, tvEventLocation, tvGreenScore;
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEventImage = itemView.findViewById(R.id.ivEventImage);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);
            tvGreenScore = itemView.findViewById(R.id.tvGreenScore);
        }
    }

    // Compact ViewHolder
    public static class CompactEventViewHolder extends RecyclerView.ViewHolder {
        ImageView ivEventImage;
        TextView tvTitle, tvDate, tvLocation, tvGreenScore;
        public CompactEventViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEventImage = itemView.findViewById(R.id.ivEventImageCompact);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvGreenScore = itemView.findViewById(R.id.tvGreenScore);
        }
    }
}

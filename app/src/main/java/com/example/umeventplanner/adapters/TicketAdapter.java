package com.example.umeventplanner.adapters;

import android.content.Context;
import android.graphics.Color;
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
import com.example.umeventplanner.Ticket;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private Context context;
    private List<Ticket> ticketList;
    private OnTicketActionListener listener;

    public interface OnTicketActionListener {
        void onScanQr(Ticket ticket);
        void onRateEvent(Ticket ticket);
        void onOpenForum(Ticket ticket);
        void onEventClick(Ticket ticket);
    }

    public TicketAdapter(Context context, List<Ticket> ticketList, OnTicketActionListener listener) {
        this.context = context;
        this.ticketList = ticketList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ticket_card, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = ticketList.get(position);
        Event event = ticket.getEvent();

        holder.tvTitle.setText(event.getTitle());
        holder.tvDateLocation.setText(String.format("%s | %s", event.getDate(), event.getLocation()));

        if (event.getBannerUrl() != null && !event.getBannerUrl().isEmpty()) {
            Glide.with(context).load(event.getBannerUrl()).into(holder.ivBanner);
        }

        String status = ticket.getRegistrationStatus();
        holder.tvStatus.setText(status);

        // Determine if the event is in the past
        boolean isPastEvent = false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date eventDate = sdf.parse(event.getDate());

            // Normalize today to midnight to compare dates only
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            Date today = c.getTime();

            // If eventDate is before today (normalized), it's a past event
            isPastEvent = eventDate.before(today);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Logic for the action button
        if ("Attended".equalsIgnoreCase(status)) {
            holder.tvStatus.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
            holder.btnAction.setText("Rate Event");
            holder.btnAction.setVisibility(View.VISIBLE);
            holder.btnAction.setOnClickListener(v -> listener.onRateEvent(ticket));
        } else { // Registered
            holder.tvStatus.setBackgroundColor(Color.parseColor("#80000000"));
            if (isPastEvent) {
                // If it's a past event and the user didn't attend, hide the action button.
                holder.btnAction.setVisibility(View.GONE);
            } else {
                // It's an ongoing/future event, show Check-in button.
                holder.btnAction.setText("Check-in");
                holder.btnAction.setVisibility(View.VISIBLE);
                holder.btnAction.setOnClickListener(v -> listener.onScanQr(ticket));
            }
        }

        holder.btnForum.setOnClickListener(v -> listener.onOpenForum(ticket));

        // Make the whole card clickable
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(ticket);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBanner;
        TextView tvStatus, tvTitle, tvDateLocation;
        Button btnAction, btnForum;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBanner = itemView.findViewById(R.id.ivBanner);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDateLocation = itemView.findViewById(R.id.tvDateLocation);
            btnAction = itemView.findViewById(R.id.btnAction);
            btnForum = itemView.findViewById(R.id.btnForum);
        }
    }
}

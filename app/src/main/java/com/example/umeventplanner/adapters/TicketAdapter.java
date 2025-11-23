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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private Context context;
    private List<Event> ticketList;
    private OnTicketActionListener listener;

    public interface OnTicketActionListener {
        void onScanQr(Event event);
        void onRateEvent(Event event);
    }

    public TicketAdapter(Context context, List<Event> ticketList, OnTicketActionListener listener) {
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
        Event event = ticketList.get(position);

        holder.tvTitle.setText(event.getTitle());
        holder.tvDateLocation.setText(String.format("%s | %s", event.getDate(), event.getLocation()));

        if (event.getBannerUrl() != null && !event.getBannerUrl().isEmpty()) {
            Glide.with(context).load(event.getBannerUrl()).into(holder.ivBanner);
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date eventDate = sdf.parse(event.getDate());
            Date today = new Date();

            if (eventDate != null && !eventDate.before(today)) {
                holder.tvStatus.setText("Upcoming");
                holder.btnAction.setText("Scan Check-in QR");
                holder.btnAction.setOnClickListener(v -> listener.onScanQr(event));
            } else {
                holder.tvStatus.setText("Completed");
                holder.btnAction.setText("Rate Event");
                holder.btnAction.setOnClickListener(v -> listener.onRateEvent(event));
            }
        } catch (ParseException e) {
            e.printStackTrace();
            holder.tvStatus.setText("Date Error");
        }
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBanner;
        TextView tvStatus, tvTitle, tvDateLocation;
        Button btnAction;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBanner = itemView.findViewById(R.id.ivBanner);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDateLocation = itemView.findViewById(R.id.tvDateLocation);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
    }
}

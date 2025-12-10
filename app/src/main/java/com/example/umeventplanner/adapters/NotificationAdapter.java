package com.example.umeventplanner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umeventplanner.R;
import com.example.umeventplanner.models.Notification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notificationList;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(Context context, List<Notification> notificationList, OnNotificationClickListener listener) {
        this.context = context;
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        holder.bind(notification, listener);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivNotificationIcon;
        private TextView tvNotificationMessage, tvNotificationTimestamp;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            ivNotificationIcon = itemView.findViewById(R.id.ivNotificationIcon);
            tvNotificationMessage = itemView.findViewById(R.id.tvNotificationMessage);
            tvNotificationTimestamp = itemView.findViewById(R.id.tvNotificationTimestamp);
        }

        void bind(final Notification notification, final OnNotificationClickListener listener) {
            tvNotificationMessage.setText(notification.getMessage());
            if (notification.getTimestamp() != null) {
                tvNotificationTimestamp.setText(notification.getTimestamp().toDate().toString());
            } else {
                tvNotificationTimestamp.setVisibility(View.GONE);
            }

            switch (notification.getType()) {
                case NEW_ANNOUNCEMENT:
                    ivNotificationIcon.setImageResource(R.drawable.ic_announcement);
                    break;
                case EVENT_REMINDER:
                    ivNotificationIcon.setImageResource(R.drawable.ic_event_reminder);
                    break;
                default:
                    ivNotificationIcon.setImageResource(R.drawable.ic_notifications);
            }

            itemView.setOnClickListener(v -> listener.onNotificationClick(notification));
        }
    }
}

package com.example.umeventplanner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umeventplanner.R;
import com.example.umeventplanner.models.Notification;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notificationList;
    private OnNotificationClickListener listener;
    private OnInvitationClickListener invitationListener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public interface OnInvitationClickListener {
        void onAccept(Notification notification);
        void onReject(Notification notification);
    }

    public NotificationAdapter(Context context, List<Notification> notificationList, OnNotificationClickListener listener, OnInvitationClickListener invitationListener) {
        this.context = context;
        this.notificationList = notificationList;
        this.listener = listener;
        this.invitationListener = invitationListener;
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
        holder.bind(notification, listener, invitationListener);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTimestamp;
        LinearLayout invitationActions;
        Button btnAccept, btnReject;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
            invitationActions = itemView.findViewById(R.id.invitation_actions);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnReject = itemView.findViewById(R.id.btn_reject);
        }

        void bind(final Notification notification, final OnNotificationClickListener listener, final OnInvitationClickListener invitationListener) {
            tvMessage.setText(notification.getMessage());
            if (notification.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
                tvTimestamp.setText(sdf.format(notification.getTimestamp()));
            }

            if (notification.getType() == Notification.NotificationType.EVENT_INVITATION) {
                invitationActions.setVisibility(View.VISIBLE);
                btnAccept.setOnClickListener(v -> invitationListener.onAccept(notification));
                btnReject.setOnClickListener(v -> invitationListener.onReject(notification));
            } else {
                invitationActions.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(notification);
                }
            });
        }
    }
}

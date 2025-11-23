package com.example.umeventplanner.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umeventplanner.R;

import java.util.List;
import java.util.Map;

public class GuestAdapter extends RecyclerView.Adapter<GuestAdapter.GuestViewHolder> {

    private Context context;
    private List<Map<String, Object>> guestList;

    public GuestAdapter(Context context, List<Map<String, Object>> guestList) {
        this.context = context;
        this.guestList = guestList;
    }

    @NonNull
    @Override
    public GuestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_guest, parent, false);
        return new GuestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GuestViewHolder holder, int position) {
        Map<String, Object> guest = guestList.get(position);
        holder.tvGuestName.setText((String) guest.get("userName"));
        holder.tvGuestEmail.setText((String) guest.get("userEmail"));

        String status = (String) guest.get("status");
        if ("Attended".equalsIgnoreCase(status)) {
            holder.tvStatus.setText("Present");
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.green_primary));
            holder.tvStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_circle, 0, 0, 0);
        } else {
            holder.tvStatus.setText("Registered");
            holder.tvStatus.setTextColor(Color.GRAY);
            holder.tvStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
    }

    @Override
    public int getItemCount() {
        return guestList.size();
    }

    public static class GuestViewHolder extends RecyclerView.ViewHolder {
        TextView tvGuestName, tvGuestEmail, tvStatus;

        public GuestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGuestName = itemView.findViewById(R.id.tvGuestName);
            tvGuestEmail = itemView.findViewById(R.id.tvGuestEmail);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}

package com.example.umeventplanner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.umeventplanner.R;
import com.example.umeventplanner.models.User;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class GuestAdapter extends RecyclerView.Adapter<GuestAdapter.GuestViewHolder> {

    private Context context;
    private List<User> guestList;
    private OnGuestListener listener;

    public interface OnGuestListener {
        void onGuestClicked(User user);
        void onRemoveGuestClicked(User user);
    }

    public GuestAdapter(Context context, List<User> guestList, OnGuestListener listener) {
        this.context = context;
        this.guestList = guestList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GuestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_guest, parent, false);
        return new GuestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GuestViewHolder holder, int position) {
        User guest = guestList.get(position);
        holder.bind(guest, listener);
    }

    @Override
    public int getItemCount() {
        return guestList.size();
    }

    static class GuestViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvName, tvStatus;
        MaterialButton btnRemove;

        public GuestViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.iv_guest_profile);
            tvName = itemView.findViewById(R.id.tv_guest_name);
            tvStatus = itemView.findViewById(R.id.tv_guest_status);
            btnRemove = itemView.findViewById(R.id.btn_remove_guest);
        }

        void bind(final User user, final OnGuestListener listener) {
            tvName.setText(user.getName());
            tvStatus.setText(user.getRegistrationStatus());

            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                Glide.with(itemView.getContext()).load(user.getProfileImageUrl()).circleCrop().into(ivProfile);
            } else {
                Glide.with(itemView.getContext()).load(R.drawable.ic_default_profile).circleCrop().into(ivProfile);
            }

            itemView.setOnClickListener(v -> listener.onGuestClicked(user));
            btnRemove.setOnClickListener(v -> listener.onRemoveGuestClicked(user));
        }
    }
}

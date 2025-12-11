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
import com.example.umeventplanner.R;
import com.example.umeventplanner.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private Context context;
    private List<User> userList;
    private List<User> selectedUsers = new ArrayList<>();
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserAdapter(Context context, List<User> userList, OnUserClickListener listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
    }

    public void setSelectedUsers(List<User> selectedUsers) {
        this.selectedUsers = selectedUsers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_search, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvName, tvUserId, tvStatus;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.iv_profile);
            tvName = itemView.findViewById(R.id.tv_name);
            tvUserId = itemView.findViewById(R.id.tv_user_id);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }

        void bind(final User user) {
            tvName.setText(user.getName());
            tvUserId.setText(user.getUserId());

            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                Glide.with(context).load(user.getProfileImageUrl()).circleCrop().into(ivProfile);
            } else {
                Glide.with(context).load(R.drawable.ic_default_profile).circleCrop().into(ivProfile);
            }

            boolean isRegistered = "registered".equalsIgnoreCase(user.getRegistrationStatus()) || "invited".equalsIgnoreCase(user.getRegistrationStatus());

            if (isRegistered) {
                tvStatus.setText(user.getRegistrationStatus());
                tvStatus.setVisibility(View.VISIBLE);
                itemView.setEnabled(false);
                itemView.setBackgroundColor(Color.parseColor("#f0f0f0"));
            } else {
                tvStatus.setVisibility(View.GONE);
                itemView.setEnabled(true);
                itemView.setBackgroundColor(selectedUsers.contains(user) ? Color.LTGRAY : Color.TRANSPARENT);
            }

            itemView.setOnClickListener(v -> {
                if (itemView.isEnabled()) {
                    listener.onUserClick(user);
                }
            });
        }
    }
}

package com.example.umeventplanner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.umeventplanner.R;
import java.util.List;

public class SelectedPosterAdapter extends RecyclerView.Adapter<SelectedPosterAdapter.PosterViewHolder> {

    private final Context context;
    private final List<Object> posterItems;
    private final OnPosterRemoveListener removeListener;

    public interface OnPosterRemoveListener {
        void onPosterRemoved(int position);
    }

    public SelectedPosterAdapter(Context context, List<Object> posterItems, OnPosterRemoveListener removeListener) {
        this.context = context;
        this.posterItems = posterItems;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public PosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_selected_poster, parent, false);
        return new PosterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PosterViewHolder holder, int position) {
        Object item = posterItems.get(position);
        Glide.with(context)
                .load(item)
                .into(holder.ivPosterThumbnail);

        holder.btnRemovePoster.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onPosterRemoved(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return posterItems.size();
    }

    static class PosterViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPosterThumbnail;
        ImageView btnRemovePoster;

        PosterViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPosterThumbnail = itemView.findViewById(R.id.ivPosterThumbnail);
            btnRemovePoster = itemView.findViewById(R.id.btnRemovePoster);
        }
    }
}

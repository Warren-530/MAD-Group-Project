package com.example.umeventplanner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umeventplanner.R;
import com.example.umeventplanner.models.Feedback;

import java.util.List;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {

    private Context context;
    private List<Feedback> feedbackList;

    public FeedbackAdapter(Context context, List<Feedback> feedbackList) {
        this.context = context;
        this.feedbackList = feedbackList;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_feedback, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        Feedback feedback = feedbackList.get(position);
        holder.bind(feedback);
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    static class FeedbackViewHolder extends RecyclerView.ViewHolder {

        private TextView tvAuthorName, tvFeedbackComment, tvTimestamp;
        private RatingBar rbFeedbackRating;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAuthorName = itemView.findViewById(R.id.tvAuthorName);
            tvFeedbackComment = itemView.findViewById(R.id.tvFeedbackComment);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            rbFeedbackRating = itemView.findViewById(R.id.rbFeedbackRating);
        }

        void bind(Feedback feedback) {
            tvAuthorName.setText(feedback.getAuthorName() != null ? feedback.getAuthorName() : "Anonymous");
            tvFeedbackComment.setText(feedback.getComment());
            rbFeedbackRating.setRating(feedback.getRating());

            if (feedback.getComment() == null || feedback.getComment().isEmpty()) {
                tvFeedbackComment.setVisibility(View.GONE);
            } else {
                tvFeedbackComment.setVisibility(View.VISIBLE);
            }

            if (feedback.getTimestamp() != null) {
                tvTimestamp.setText(feedback.getTimestamp().toDate().toString());
            } else {
                tvTimestamp.setVisibility(View.GONE);
            }
        }
    }
}

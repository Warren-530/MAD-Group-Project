package com.example.umeventplanner.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umeventplanner.R;
import com.example.umeventplanner.models.Announcement;
import com.example.umeventplanner.models.Comment;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder> {

    private Context context;
    private List<Announcement> announcementList;
    private String eventId;
    private String eventHostId;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public AnnouncementAdapter(Context context, List<Announcement> announcementList, String eventId, String eventHostId) {
        this.context = context;
        this.announcementList = announcementList;
        this.eventId = eventId;
        this.eventHostId = eventHostId;
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public AnnouncementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_announcement, parent, false);
        return new AnnouncementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnouncementViewHolder holder, int position) {
        Announcement announcement = announcementList.get(position);
        holder.bind(announcement);
    }

    @Override
    public int getItemCount() {
        return announcementList.size();
    }

    class AnnouncementViewHolder extends RecyclerView.ViewHolder {

        private TextView tvAuthor, tvMessage, tvTimestamp;
        private RecyclerView rvComments;
        private CommentAdapter commentAdapter;
        private List<Comment> commentList;
        private EditText etComment;
        private ImageButton btnSendComment;

        public AnnouncementViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            rvComments = itemView.findViewById(R.id.rvComments);
            etComment = itemView.findViewById(R.id.etComment);
            btnSendComment = itemView.findViewById(R.id.btnSendComment);
        }

        void bind(Announcement announcement) {
            tvAuthor.setText(announcement.getAuthorName());
            tvMessage.setText(announcement.getMessage());
            tvTimestamp.setText(announcement.getTimestamp().toDate().toString());

            commentList = new ArrayList<>();
            commentAdapter = new CommentAdapter(context, commentList, eventId, announcement.getAnnouncementId(), eventHostId);
            rvComments.setLayoutManager(new LinearLayoutManager(context));
            rvComments.setAdapter(commentAdapter);

            loadComments(announcement.getAnnouncementId());

            btnSendComment.setOnClickListener(v -> postComment(announcement.getAnnouncementId()));

            itemView.setOnClickListener(v -> {
                toggleCommentsVisibility();
            });
        }

        private void toggleCommentsVisibility() {
            if (rvComments.getVisibility() == View.GONE) {
                rvComments.setVisibility(View.VISIBLE);
            } else {
                rvComments.setVisibility(View.GONE);
            }
        }

        private void loadComments(String announcementId) {
            getCommentsCollection(announcementId).orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            return;
                        }
                        commentList.clear();
                        if (snapshots != null) {
                            for (Comment comment : snapshots.toObjects(Comment.class)) {
                                commentList.add(comment);
                            }
                        }
                        commentAdapter.notifyDataSetChanged();
                    });
        }

        private void postComment(String announcementId) {
            String message = etComment.getText().toString().trim();
            if (TextUtils.isEmpty(message)) {
                return;
            }

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) return;

            db.collection("users").document(currentUser.getUid()).get().addOnSuccessListener(userDoc -> {
                if (userDoc.exists()) {
                    String userName = userDoc.getString("name");
                    String commentId = UUID.randomUUID().toString();

                    Comment comment = new Comment(commentId, currentUser.getUid(), userName, message, Timestamp.now());

                    getCommentsCollection(announcementId).document(commentId).set(comment).addOnSuccessListener(aVoid -> {
                        etComment.setText("");
                        if (rvComments.getVisibility() == View.GONE) {
                            toggleCommentsVisibility();
                        }
                    });
                }
            });
        }

        private CollectionReference getCommentsCollection(String announcementId) {
            return db.collection("events").document(eventId).collection("announcements").document(announcementId).collection("comments");
        }
    }
}

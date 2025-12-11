package com.example.umeventplanner.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umeventplanner.R;
import com.example.umeventplanner.models.Comment;
import com.example.umeventplanner.models.Reply;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Final, corrected version with nested replies
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context context;
    private List<Comment> commentList;
    private String eventId;
    private String announcementId;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public CommentAdapter(Context context, List<Comment> commentList, String eventId, String announcementId, String eventHostId) {
        this.context = context;
        this.commentList = commentList;
        this.eventId = eventId;
        this.announcementId = announcementId;
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {

        private TextView tvAuthor, tvMessage, tvTimestamp;
        private Button btnReply;
        private RecyclerView rvReplies;
        private LinearLayout replyLayout;
        private EditText etReply;
        private ImageButton btnSendReply;
        private ReplyAdapter replyAdapter;
        private List<Reply> replyList;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            btnReply = itemView.findViewById(R.id.btnReply);
            rvReplies = itemView.findViewById(R.id.rvReplies);
            replyLayout = itemView.findViewById(R.id.reply_layout);
            etReply = itemView.findViewById(R.id.etReply);
            btnSendReply = itemView.findViewById(R.id.btnSendReply);
        }

        void bind(Comment comment) {
            tvAuthor.setText(comment.getAuthorName());
            tvMessage.setText(comment.getMessage());
            if (comment.getTimestamp() != null) {
                tvTimestamp.setText(comment.getTimestamp().toDate().toString());
            } else {
                tvTimestamp.setVisibility(View.GONE);
            }

            replyList = new ArrayList<>();
            replyAdapter = new ReplyAdapter(context, replyList);
            rvReplies.setLayoutManager(new LinearLayoutManager(context));
            rvReplies.setAdapter(replyAdapter);

            loadReplies(comment.getCommentId());

            // Toggle visibility of the reply input section
            btnReply.setOnClickListener(v -> {
                if (replyLayout.getVisibility() == View.GONE) {
                    replyLayout.setVisibility(View.VISIBLE);
                } else {
                    replyLayout.setVisibility(View.GONE);
                }
            });

            // The whole comment item is clickable to show/hide replies
            itemView.setOnClickListener(v -> {
                 if (rvReplies.getVisibility() == View.GONE) {
                    rvReplies.setVisibility(View.VISIBLE);
                } else {
                    rvReplies.setVisibility(View.GONE);
                }
            });

            btnSendReply.setOnClickListener(v -> postReply(comment.getCommentId()));
        }

        private void loadReplies(String commentId) {
            getRepliesCollection(commentId).orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) return;
                        replyList.clear();
                        if (snapshots != null) {
                            replyList.addAll(snapshots.toObjects(Reply.class));
                        }
                        replyAdapter.notifyDataSetChanged();
                    });
        }

        private void postReply(String commentId) {
            String message = etReply.getText().toString().trim();
            if (TextUtils.isEmpty(message)) return;

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) return;

            db.collection("users").document(currentUser.getUid()).get().addOnSuccessListener(userDoc -> {
                if (userDoc.exists()) {
                    String userName = userDoc.getString("name");
                    String replyId = UUID.randomUUID().toString();

                    Reply reply = new Reply(replyId, currentUser.getUid(), userName, message, Timestamp.now());

                    getRepliesCollection(commentId).document(replyId).set(reply).addOnSuccessListener(aVoid -> {
                        etReply.setText("");
                        replyLayout.setVisibility(View.GONE);
                        // Make sure the replies list is visible to show the new reply
                        if (rvReplies.getVisibility() == View.GONE) {
                            rvReplies.setVisibility(View.VISIBLE);
                        }
                    });
                }
            });
        }

        private CollectionReference getRepliesCollection(String commentId) {
            return db.collection("events").document(eventId)
                    .collection("announcements").document(announcementId)
                    .collection("comments").document(commentId)
                    .collection("replies");
        }
    }
}

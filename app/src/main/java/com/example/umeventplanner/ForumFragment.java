package com.example.umeventplanner;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umeventplanner.adapters.ForumAdapter;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ForumFragment extends Fragment {

    private String eventId;
    private RecyclerView rvPosts;
    private ForumAdapter adapter;
    private List<Post> postList;
    private EditText etMessage;
    private ImageButton btnSend;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public static ForumFragment newInstance(String eventId) {
        ForumFragment fragment = new ForumFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forum, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        rvPosts = view.findViewById(R.id.rvPosts);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);

        postList = new ArrayList<>();
        adapter = new ForumAdapter(getContext(), postList);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPosts.setAdapter(adapter);

        loadPosts();

        btnSend.setOnClickListener(v -> postMessage());

        return view;
    }

    private void loadPosts() {
        if (eventId == null) return;

        getPostsCollection().orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        return;
                    }
                    postList.clear();
                    for (Post post : snapshots.toObjects(Post.class)) {
                        postList.add(post);
                    }
                    adapter.notifyDataSetChanged();
                    rvPosts.scrollToPosition(postList.size() - 1);
                });
    }

    private void postMessage() {
        String message = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("users").document(currentUser.getUid()).get().addOnSuccessListener(userDoc -> {
            if (userDoc.exists()) {
                String userName = userDoc.getString("name");
                String postId = UUID.randomUUID().toString();

                Post post = new Post(postId, currentUser.getUid(), userName, message, Timestamp.now());

                getPostsCollection().document(postId).set(post).addOnSuccessListener(aVoid -> {
                    etMessage.setText("");
                });
            }
        });
    }

    private CollectionReference getPostsCollection() {
        return db.collection("events").document(eventId).collection("posts");
    }
}

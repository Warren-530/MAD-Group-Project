package com.example.umeventplanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umeventplanner.adapters.CommentAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FeedbackFragment extends Fragment {

    private String eventId;
    private RecyclerView rvFeedback;
    private CommentAdapter adapter;
    private List<Comment> commentList;
    private FirebaseFirestore db;
    private TextView tvNoFeedback;

    public static FeedbackFragment newInstance(String eventId) {
        FeedbackFragment fragment = new FeedbackFragment();
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
        View view = inflater.inflate(R.layout.fragment_feedback, container, false);

        db = FirebaseFirestore.getInstance();
        rvFeedback = view.findViewById(R.id.rvFeedback);
        tvNoFeedback = view.findViewById(R.id.tvNoFeedback);
        commentList = new ArrayList<>();

        rvFeedback.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CommentAdapter(getContext(), commentList);
        rvFeedback.setAdapter(adapter);

        loadFeedback();

        return view;
    }

    private void loadFeedback() {
        if (eventId == null) return;

        db.collection("events").document(eventId).collection("comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        tvNoFeedback.setVisibility(View.VISIBLE);
                    } else {
                        commentList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Comment comment = document.toObject(Comment.class);
                            commentList.add(comment);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}

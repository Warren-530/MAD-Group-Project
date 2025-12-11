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

import com.example.umeventplanner.adapters.FeedbackAdapter;
import com.example.umeventplanner.models.Feedback;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

// Corrected version of FeedbackFragment
public class FeedbackFragment extends Fragment {

    private String eventId;
    private RecyclerView rvFeedback;
    private FeedbackAdapter adapter;
    private List<Feedback> feedbackList;
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
        feedbackList = new ArrayList<>();

        rvFeedback.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FeedbackAdapter(getContext(), feedbackList);
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
                        List<Task<DocumentSnapshot>> userTasks = new ArrayList<>();
                        List<Feedback> pendingFeedback = new ArrayList<>();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Feedback feedback = document.toObject(Feedback.class);
                            pendingFeedback.add(feedback);
                            if (feedback.getUserId() != null && !feedback.getUserId().isEmpty()) {
                                userTasks.add(db.collection("users").document(feedback.getUserId()).get());
                            } else {
                                // Add a placeholder task for feedbacks with no user to maintain list alignment
                                userTasks.add(Tasks.forResult(null));
                            }
                        }

                        Tasks.whenAllSuccess(userTasks).addOnSuccessListener(userSnapshots -> {
                            feedbackList.clear();
                            for (int i = 0; i < pendingFeedback.size(); i++) {
                                DocumentSnapshot userSnapshot = (DocumentSnapshot) userSnapshots.get(i);
                                Feedback feedback = pendingFeedback.get(i);
                                if (userSnapshot != null && userSnapshot.exists()) {
                                    feedback.setAuthorName(userSnapshot.getString("name"));
                                } else {
                                    feedback.setAuthorName("Anonymous");
                                }
                                feedbackList.add(feedback);
                            }
                            adapter.notifyDataSetChanged();
                        });
                    }
                });
    }
}

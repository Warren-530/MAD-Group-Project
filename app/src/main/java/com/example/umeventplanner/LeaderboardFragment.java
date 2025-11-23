package com.example.umeventplanner;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umeventplanner.adapters.LeaderboardAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardFragment extends Fragment {

    private static final String TAG = "LeaderboardFragment";

    private RecyclerView rvLeaderboard;
    private LeaderboardAdapter adapter;
    private List<LeaderboardEntry> leaderboardEntries;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        // Initialization
        db = FirebaseFirestore.getInstance();
        rvLeaderboard = view.findViewById(R.id.rvLeaderboard);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        leaderboardEntries = new ArrayList<>();

        // Setup RecyclerView
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LeaderboardAdapter(getContext(), leaderboardEntries);
        rvLeaderboard.setAdapter(adapter);

        loadLeaderboardData();

        return view;
    }

    private void loadLeaderboardData() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        db.collection("events")
                .whereEqualTo("status", "Published")
                .orderBy("sustainabilityScore", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        return;
                    }
                    leaderboardEntries.clear();
                    int rank = 1;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        if (event != null) {
                            LeaderboardEntry entry = new LeaderboardEntry(
                                    document.getId(),
                                    event.getTitle(),
                                    event.getSustainabilityScore()
                            );
                            entry.setRank(rank++);
                            leaderboardEntries.add(entry);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading leaderboard", e);
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                });
    }
}

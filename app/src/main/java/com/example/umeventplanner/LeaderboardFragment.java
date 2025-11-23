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
import java.util.Collections;
import java.util.List;

public class LeaderboardFragment extends Fragment implements LeaderboardAdapter.OnLeaderboardClickListener {

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
        adapter = new LeaderboardAdapter(getContext(), leaderboardEntries, this);
        rvLeaderboard.setAdapter(adapter);

        loadLeaderboardData();

        return view;
    }

    private void loadLeaderboardData() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        db.collection("events")
                .whereEqualTo("status", "Published")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        return;
                    }

                    List<LeaderboardEntry> entries = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        if (event != null) {
                            double greenScore = event.getSustainabilityScore();
                            double avgRating = event.getAverageRating();

                            // Formula: (green * 0.6) + (rating * 0.4)
                            double finalScore = (greenScore * 0.6) + (avgRating * 0.4);

                            entries.add(new LeaderboardEntry(
                                    document.getId(),
                                    event.getTitle(),
                                    event.getBannerUrl(),
                                    greenScore,
                                    avgRating,
                                    finalScore
                            ));
                        }
                    }

                    // Sort list by finalScore descending
                    Collections.sort(entries, (o1, o2) -> Double.compare(o2.getFinalScore(), o1.getFinalScore()));

                    // Assign ranks
                    for (int i = 0; i < entries.size(); i++) {
                        entries.get(i).setRank(i + 1);
                    }

                    leaderboardEntries.clear();
                    leaderboardEntries.addAll(entries);
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);

                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading leaderboard", e);
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                });
    }

    @Override
    public void onEventClick(String eventId) {
        if (eventId != null && getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, EventDetailsFragment.newInstance(eventId))
                    .addToBackStack(null)
                    .commit();
        }
    }
}

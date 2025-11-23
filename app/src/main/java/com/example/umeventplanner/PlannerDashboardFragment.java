package com.example.umeventplanner;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PlannerDashboardFragment extends Fragment {

    private static final String TAG = "PlannerDashboard";

    private TextView tvWelcome, tvTotalEvents, tvAvgScore, tvResourcesSaved;
    private TextView tvUpcomingEventTitle, tvUpcomingEventDate, tvUpcomingEventTime, tvUpcomingEventLocation, tvNoEvents;
    private Button btnCreateEvent;
    private CardView cardUpcomingEvent;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_planner_dashboard, container, false);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        initViews(view);

        btnCreateEvent.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new CreateEventFragment())
                .addToBackStack(null)
                .commit());

        loadDashboardData();

        return view;
    }

    private void initViews(View view) {
        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvTotalEvents = view.findViewById(R.id.tvTotalEvents);
        tvAvgScore = view.findViewById(R.id.tvAvgScore);
        tvResourcesSaved = view.findViewById(R.id.tvResourcesSaved);
        tvUpcomingEventTitle = view.findViewById(R.id.tvUpcomingEventTitle);
        tvUpcomingEventDate = view.findViewById(R.id.tvUpcomingEventDate);
        tvUpcomingEventTime = view.findViewById(R.id.tvUpcomingEventTime);
        tvUpcomingEventLocation = view.findViewById(R.id.tvUpcomingEventLocation);
        tvNoEvents = view.findViewById(R.id.tvNoEvents);
        btnCreateEvent = view.findViewById(R.id.btnCreateEvent);
        cardUpcomingEvent = view.findViewById(R.id.cardUpcomingEvent);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void loadDashboardData() {
        progressBar.setVisibility(View.VISIBLE);

        if (currentUser == null) return;

        // Set welcome message immediately
        db.collection("users").document(currentUser.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                tvWelcome.setText("Welcome, " + name + "!");
            }
        });

        // Fetch all events by the planner
        db.collection("events").whereArrayContains("plannerUIDs", currentUser.getUid()).get()
            .addOnSuccessListener(this::processEventResults)
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error fetching events", e);
                progressBar.setVisibility(View.GONE);
                tvNoEvents.setVisibility(View.VISIBLE);
                cardUpcomingEvent.setVisibility(View.GONE);
            });
    }

    private void processEventResults(QuerySnapshot querySnapshot) {
        if (querySnapshot == null || querySnapshot.isEmpty()) {
            tvTotalEvents.setText("0");
            tvAvgScore.setText("0.0");
            tvResourcesSaved.setText("0");
            tvNoEvents.setVisibility(View.VISIBLE);
            cardUpcomingEvent.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            return;
        }

        // 1. Dynamic Stats Calculation
        int totalEvents = querySnapshot.size();
        double totalScore = 0;
        tvTotalEvents.setText(String.valueOf(totalEvents));

        Event nearestEvent = null;
        Date nearestDate = null;
        Date today = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (QueryDocumentSnapshot doc : querySnapshot) {
            Event event = doc.toObject(Event.class);
            totalScore += event.getSustainabilityScore();

            // 2. Find Next Upcoming Event
            try {
                Date eventDate = sdf.parse(event.getDate());
                if (eventDate != null && (eventDate.after(today) || sdf.format(eventDate).equals(sdf.format(today)))) {
                    if (nearestEvent == null || eventDate.before(nearestDate)) {
                        nearestDate = eventDate;
                        nearestEvent = event;
                    }
                }
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing date for event: " + event.getTitle(), e);
            }
        }

        double avgScore = totalEvents > 0 ? totalScore / totalEvents : 0.0;
        tvAvgScore.setText(String.format(Locale.US, "%.1f", avgScore));
        tvResourcesSaved.setText(String.valueOf(totalEvents * 20)); // Example calculation

        // 3. Update UI
        if (nearestEvent != null) {
            tvUpcomingEventTitle.setText(nearestEvent.getTitle());
            tvUpcomingEventDate.setText(nearestEvent.getDate());
            tvUpcomingEventTime.setText(String.format("%s - %s", nearestEvent.getStartTime(), nearestEvent.getEndTime()));
            tvUpcomingEventLocation.setText(nearestEvent.getLocation());
            cardUpcomingEvent.setVisibility(View.VISIBLE);
            tvNoEvents.setVisibility(View.GONE);
        } else {
            tvNoEvents.setVisibility(View.VISIBLE);
            cardUpcomingEvent.setVisibility(View.GONE);
        }

        progressBar.setVisibility(View.GONE);
    }
}

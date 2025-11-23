package com.example.umeventplanner;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umeventplanner.adapters.HostedEventAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyHostedEventsFragment extends Fragment implements HostedEventAdapter.OnHostedEventClickListener {

    private TabLayout tabLayout;
    private RecyclerView rvHostedEvents;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    private HostedEventAdapter adapter;
    private List<Event> eventList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_hosted_events, container, false);
        initViews(view);
        setupTabLayout();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loadHostedEvents("Upcoming");

        return view;
    }

    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.tabLayout);
        rvHostedEvents = view.findViewById(R.id.rvHostedEvents);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        rvHostedEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        eventList = new ArrayList<>();
        adapter = new HostedEventAdapter(getContext(), eventList, this);
        rvHostedEvents.setAdapter(adapter);
    }

    private void setupTabLayout() {
        // The tabs are defined in the XML, so we only add the listener.
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText() != null) {
                    loadHostedEvents(tab.getText().toString());
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) { }
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void loadHostedEvents(String statusFilter) {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        if (mAuth.getCurrentUser() == null) return;
        String currentUserId = mAuth.getCurrentUser().getUid();

        Query query = db.collection("events").whereArrayContains("plannerUIDs", currentUserId);

        // This query now matches the index you created: plannerUIDs (array) and date (ascending)
        query.orderBy("date").get().addOnSuccessListener(queryDocumentSnapshots -> {
            eventList.clear();
            Date today = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Event event = document.toObject(Event.class);
                if (event != null) {
                    event.setEventId(document.getId());
                    boolean addEvent = false;
                    try {
                        Date eventDate = sdf.parse(event.getDate());
                        switch (statusFilter) {
                            case "Upcoming":
                                if ("Published".equals(event.getStatus()) && (eventDate.after(today) || sdf.format(eventDate).equals(sdf.format(today)))) {
                                    addEvent = true;
                                }
                                break;
                            case "Past":
                                if ("Published".equals(event.getStatus()) && eventDate.before(today) && !sdf.format(eventDate).equals(sdf.format(today))) {
                                    addEvent = true;
                                }
                                break;
                            case "Drafts":
                                if ("Draft".equals(event.getStatus())) {
                                    addEvent = true;
                                }
                                break;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (addEvent) {
                        eventList.add(event);
                    }
                }
            }
            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
            tvEmptyState.setVisibility(eventList.isEmpty() ? View.VISIBLE : View.GONE);
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "Error loading events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onEventClick(Event event) {
        if (event != null && event.getEventId() != null) {
            EventManagementFragment fragment = new EventManagementFragment();
            Bundle args = new Bundle();
            args.putString("eventId", event.getEventId());
            fragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onEventLongClick(Event event) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent(event))
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void deleteEvent(Event event) {
        if (event == null || event.getEventId() == null) return;
        db.collection("events").document(event.getEventId()).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Event deleted successfully", Toast.LENGTH_SHORT).show();
                    int position = eventList.indexOf(event);
                    if (position != -1) {
                        eventList.remove(position);
                        adapter.notifyItemRemoved(position);
                    }
                    tvEmptyState.setVisibility(eventList.isEmpty() ? View.VISIBLE : View.GONE);
                    // TODO: Add logic to delete associated images from Cloudinary
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error deleting event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

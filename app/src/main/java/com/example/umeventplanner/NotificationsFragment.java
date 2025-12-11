package com.example.umeventplanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umeventplanner.adapters.NotificationAdapter;
import com.example.umeventplanner.models.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationsFragment extends Fragment implements NotificationAdapter.OnNotificationClickListener, NotificationAdapter.OnInvitationClickListener {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notification> notificationList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private Toolbar toolbar;

    public static NotificationsFragment newInstance() {
        return new NotificationsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        initViews(view);
        loadNotifications();
        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar_notifications);
        recyclerView = view.findViewById(R.id.rv_notifications);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);

        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new NotificationAdapter(getContext(), notificationList, this, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadNotifications() {
        if (currentUser == null) return;

        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);

        db.collection("users").document(currentUser.getUid()).collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notificationList.clear();
                    if (queryDocumentSnapshots.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        tvEmptyState.setVisibility(View.VISIBLE);
                        return;
                    }
                    for (Notification notification : queryDocumentSnapshots.toObjects(Notification.class)) {
                        notificationList.add(notification);
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmptyState.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Error loading notifications: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onNotificationClick(Notification notification) {
        // Handle notification click (e.g., navigate to event details)
        if (notification.getEventId() != null && !notification.getEventId().isEmpty()) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, EventDetailsFragment.newInstance(notification.getEventId()))
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onAccept(Notification notification) {
        if (currentUser == null) return;

        WriteBatch batch = db.batch();

        // Create event registration with "registered" status
        DocumentReference eventRegRef = db.collection("events").document(notification.getEventId())
                .collection("registrations").document(currentUser.getUid());
        Map<String, Object> registration = new HashMap<>();
        registration.put("status", "registered");
        batch.set(eventRegRef, registration);

        // Create user registration
        DocumentReference userRegRef = db.collection("users").document(currentUser.getUid())
                .collection("registrations").document(notification.getEventId());
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", notification.getEventId());
        batch.set(userRegRef, event);

        // Increment event participant count
        DocumentReference eventRef = db.collection("events").document(notification.getEventId());
        batch.update(eventRef, "currentParticipants", FieldValue.increment(1));

        // Delete the notification
        DocumentReference notificationRef = db.collection("users").document(currentUser.getUid())
                .collection("notifications").document(notification.getNotificationId());
        batch.delete(notificationRef);

        batch.commit().addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Invitation accepted", Toast.LENGTH_SHORT).show();
            notificationList.remove(notification);
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error accepting invitation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onReject(Notification notification) {
        if (currentUser == null) return;

        // Delete the notification
        DocumentReference notificationRef = db.collection("users").document(currentUser.getUid())
                .collection("notifications").document(notification.getNotificationId());
        notificationRef.delete().addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Invitation rejected", Toast.LENGTH_SHORT).show();
            notificationList.remove(notification);
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error rejecting invitation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}

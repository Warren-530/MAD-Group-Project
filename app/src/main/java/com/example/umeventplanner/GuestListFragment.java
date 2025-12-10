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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umeventplanner.adapters.GuestAdapter;
import com.example.umeventplanner.models.Notification;
import com.example.umeventplanner.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GuestListFragment extends Fragment implements GuestAdapter.OnGuestListener, UserSearchDialogFragment.OnUsersSelectedListener {

    private static final String ARG_EVENT_ID = "eventId";

    private String eventId;
    private RecyclerView recyclerView;
    private GuestAdapter adapter;
    private List<User> guestList = new ArrayList<>();
    private List<User> filteredGuestList = new ArrayList<>();
    private FirebaseFirestore db;

    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private Toolbar toolbar;
    private FloatingActionButton fabInvite;
    private SearchView searchView;

    public static GuestListFragment newInstance(String eventId) {
        GuestListFragment fragment = new GuestListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
        }
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guest_list, container, false);
        initViews(view);
        setupToolbar();
        setupSearchView();
        loadGuests();
        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar_guest_list);
        recyclerView = view.findViewById(R.id.rv_guest_list);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
        fabInvite = view.findViewById(R.id.fab_invite);
        searchView = view.findViewById(R.id.search_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GuestAdapter(getContext(), filteredGuestList, this);
        recyclerView.setAdapter(adapter);

        fabInvite.setOnClickListener(v -> {
            UserSearchDialogFragment dialog = UserSearchDialogFragment.newInstance(eventId);
            dialog.setOnUsersSelectedListener(this);
            dialog.show(getParentFragmentManager(), "UserSearchDialog");
        });
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterGuests(newText);
                return true;
            }
        });
    }

    private void loadGuests() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);

        db.collection("events").document(eventId).collection("registrations").get().addOnSuccessListener(queryDocumentSnapshots -> {
            guestList.clear();
            if (queryDocumentSnapshots.isEmpty()) {
                progressBar.setVisibility(View.GONE);
                tvEmptyState.setVisibility(View.VISIBLE);
                return;
            }
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                String userId = doc.getId();
                String status = doc.getString("status");
                db.collection("users").document(userId).get().addOnSuccessListener(userDoc -> {
                    if (userDoc.exists()) {
                        User user = userDoc.toObject(User.class);
                        if (user != null) {
                            user.setUserId(userDoc.getId());
                            user.setRegistrationStatus(status);
                            guestList.add(user);
                        }
                    }
                    if (guestList.size() == queryDocumentSnapshots.size()) {
                        filterGuests("");
                        progressBar.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                });
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
            Toast.makeText(getContext(), "Error loading guests: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void filterGuests(String query) {
        filteredGuestList.clear();
        if (query.isEmpty()) {
            filteredGuestList.addAll(guestList);
        } else {
            for (User user : guestList) {
                if (user.getName().toLowerCase().contains(query.toLowerCase()) || user.getUserId().toLowerCase().contains(query.toLowerCase())) {
                    filteredGuestList.add(user);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onGuestClicked(User user) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, ProfileFragment.newInstance(user.getUserId()));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onRemoveGuestClicked(User user) {
        new AlertDialog.Builder(getContext())
                .setTitle("Remove Participant")
                .setMessage("Are you sure you want to remove " + user.getName() + " from the event?")
                .setPositiveButton("Remove", (dialog, which) -> removeGuest(user))
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void removeGuest(User user) {
        db.collection("events").document(eventId).get().addOnSuccessListener(eventDocument -> {
            if (!eventDocument.exists()) {
                Toast.makeText(getContext(), "Failed to remove guest: Event not found.", Toast.LENGTH_SHORT).show();
                return;
            }
            String eventTitle = eventDocument.getString("title");

            WriteBatch batch = db.batch();

            // 1. Remove registration from event
            DocumentReference eventRegRef = db.collection("events").document(eventId).collection("registrations").document(user.getUserId());
            batch.delete(eventRegRef);

            // 2. Remove registration from user
            DocumentReference userRegRef = db.collection("users").document(user.getUserId()).collection("registrations").document(eventId);
            batch.delete(userRegRef);

            // 3. Decrement participants count
            DocumentReference eventRef = db.collection("events").document(eventId);
            batch.update(eventRef, "currentParticipants", FieldValue.increment(-1));

            // 4. Send notification
            DocumentReference notificationRef = db.collection("users").document(user.getUserId()).collection("notifications").document();
            String message = "You have been removed from the event: " + eventTitle;
            Notification notification = new Notification();
            notification.setNotificationId(notificationRef.getId());
            notification.setEventId(eventId);
            notification.setMessage(message);
            notification.setRead(false);
            notification.setType(Notification.NotificationType.EVENT_REMOVAL);
            batch.set(notificationRef, notification);

            batch.commit().addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), user.getName() + " has been removed.", Toast.LENGTH_SHORT).show();
                guestList.remove(user);
                adapter.notifyDataSetChanged();
                tvEmptyState.setVisibility(guestList.isEmpty() ? View.VISIBLE : View.GONE);
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Failed to remove guest: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to get event details for notification.", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onUsersSelected(List<User> users) {
        if (users.isEmpty()) return;

        db.collection("events").document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String eventTitle = documentSnapshot.getString("title");

                WriteBatch batch = db.batch();

                for (User user : users) {
                    // Send notification to user
                    DocumentReference notificationRef = db.collection("users").document(user.getUserId()).collection("notifications").document();
                    String message = "You have been invited to the event: " + eventTitle;
                    Notification notification = new Notification();
                    notification.setNotificationId(notificationRef.getId());
                    notification.setEventId(eventId);
                    notification.setMessage(message);
                    notification.setTimestamp(new Date());
                    notification.setRead(false);
                    notification.setType(Notification.NotificationType.EVENT_INVITATION);
                    batch.set(notificationRef, notification);
                }

                batch.commit().addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Users invited successfully", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error inviting users: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(getContext(), "Error: Event not found.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

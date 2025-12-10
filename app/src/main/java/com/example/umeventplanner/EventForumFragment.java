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

import com.example.umeventplanner.adapters.AnnouncementAdapter;
import com.example.umeventplanner.models.Announcement;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Re-creating this file to ensure it's correctly compiled
public class EventForumFragment extends Fragment {

    private String eventId;
    private RecyclerView rvAnnouncements;
    private AnnouncementAdapter adapter;
    private List<Announcement> announcementList;
    private EditText etAnnouncement;
    private ImageButton btnSend;
    private View bottomLayout;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private String eventHostId;
    private boolean isPlannerView;

    public static EventForumFragment newInstance(String eventId, String eventHostId, boolean isPlannerView) {
        EventForumFragment fragment = new EventForumFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        args.putString("eventHostId", eventHostId);
        args.putBoolean("isPlannerView", isPlannerView);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            eventHostId = getArguments().getString("eventHostId");
            isPlannerView = getArguments().getBoolean("isPlannerView", false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_forum, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }

        rvAnnouncements = view.findViewById(R.id.rvAnnouncements);
        bottomLayout = view.findViewById(R.id.bottom_layout);

        announcementList = new ArrayList<>();
        adapter = new AnnouncementAdapter(getContext(), announcementList, eventId, eventHostId);
        rvAnnouncements.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAnnouncements.setAdapter(adapter);

        loadAnnouncements();

        if (isPlannerView) {
            bottomLayout.setVisibility(View.VISIBLE);
            etAnnouncement = view.findViewById(R.id.etAnnouncement);
            btnSend = view.findViewById(R.id.btnSend);
            btnSend.setOnClickListener(v -> postAnnouncement());
        } else {
            bottomLayout.setVisibility(View.GONE);
        }

        return view;
    }

    private void loadAnnouncements() {
        if (eventId == null) return;

        getAnnouncementsCollection().orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        return;
                    }
                    announcementList.clear();
                    if (snapshots != null) {
                        announcementList.addAll(snapshots.toObjects(Announcement.class));
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void postAnnouncement() {
        String message = etAnnouncement.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || !isPlannerView || !currentUser.getUid().equals(eventHostId)) {
            Toast.makeText(getContext(), "Only the host can post announcements.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(currentUser.getUid()).get().addOnSuccessListener(userDoc -> {
            if (userDoc.exists()) {
                String userName = userDoc.getString("name");
                String announcementId = UUID.randomUUID().toString();

                Announcement announcement = new Announcement(announcementId, currentUser.getUid(), userName, message, Timestamp.now());

                getAnnouncementsCollection().document(announcementId).set(announcement).addOnSuccessListener(aVoid -> {
                    etAnnouncement.setText("");
                });
            }
        });
    }

    private CollectionReference getAnnouncementsCollection() {
        return db.collection("events").document(eventId).collection("announcements");
    }
}

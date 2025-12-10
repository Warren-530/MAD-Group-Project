package com.example.umeventplanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umeventplanner.adapters.NotificationAdapter;
import com.example.umeventplanner.models.Announcement;
import com.example.umeventplanner.models.Notification;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class NotificationsFragment extends Fragment implements NotificationAdapter.OnNotificationClickListener {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        rvNotifications = view.findViewById(R.id.rvNotifications);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(getContext(), notificationList, this);
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNotifications.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotifications();
    }

    private void loadNotifications() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyState.setVisibility(View.GONE);

        if (mAuth.getCurrentUser() == null) {
            progressBar.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).collection("registrations").get()
            .addOnSuccessListener(registrations -> {
                if (registrations.isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    tvEmptyState.setVisibility(View.VISIBLE);
                    return;
                }

                List<Task<DocumentSnapshot>> eventTasks = new ArrayList<>();
                for (QueryDocumentSnapshot doc : registrations) {
                    eventTasks.add(db.collection("events").document(doc.getId()).get());
                }

                Tasks.whenAllSuccess(eventTasks).addOnSuccessListener(eventSnapshots -> {
                    notificationList.clear();
                    List<Task<?>> notificationTasks = new ArrayList<>();

                    for (Object snapshot : eventSnapshots) {
                        DocumentSnapshot eventDoc = (DocumentSnapshot) snapshot;
                        if (eventDoc.exists()) {
                            Event event = eventDoc.toObject(Event.class);
                            if (event != null) {
                                event.setEventId(eventDoc.getId());

                                // 1. Check for upcoming events
                                addEventReminderNotification(event);

                                // 2. Check for new announcements
                                Task<QuerySnapshot> announcementTask = getLatestAnnouncement(event);
                                notificationTasks.add(announcementTask);
                            }
                        }
                    }

                    Tasks.whenAllComplete(notificationTasks).addOnCompleteListener(allTasks -> {
                         // Sort notifications by timestamp
                        Collections.sort(notificationList, (o1, o2) -> o2.getTimestamp().compareTo(o1.getTimestamp()));
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                        if (notificationList.isEmpty()) {
                            tvEmptyState.setVisibility(View.VISIBLE);
                        } else {
                            tvEmptyState.setVisibility(View.GONE);
                        }
                    });
                });
            });
    }

    private void addEventReminderNotification(Event event) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date eventDate = sdf.parse(event.getDate());
            Date today = new Date();

            Calendar cal = Calendar.getInstance();
            cal.setTime(today);
            cal.add(Calendar.DATE, 1);
            Date tomorrow = cal.getTime();

            if (sdf.format(eventDate).equals(sdf.format(tomorrow))) {
                String message = "Reminder: Your event \"" + event.getTitle() + "\" is tomorrow!";
                Notification notif = new Notification(UUID.randomUUID().toString(), event.getEventId(), event.getTitle(), message, new Timestamp(eventDate), Notification.NotificationType.EVENT_REMINDER);
                notificationList.add(notif);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private Task<QuerySnapshot> getLatestAnnouncement(Event event) {
        return db.collection("events").document(event.getEventId()).collection("announcements")
                .orderBy("timestamp", Query.Direction.DESCENDING).limit(1).get()
                .addOnSuccessListener(announcements -> {
                    if (!announcements.isEmpty()) {
                        Announcement announcement = announcements.getDocuments().get(0).toObject(Announcement.class);
                        if (announcement != null) {
                            String message = "New announcement in \"" + event.getTitle() + "\": " + announcement.getMessage();
                            Notification notif = new Notification(UUID.randomUUID().toString(), event.getEventId(), event.getTitle(), message, announcement.getTimestamp(), Notification.NotificationType.NEW_ANNOUNCEMENT);
                            notificationList.add(notif);
                        }
                    }
                });
    }

    @Override
    public void onNotificationClick(Notification notification) {
        // On click, navigate to the specific event's forum
        db.collection("events").document(notification.getEventId()).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Event event = doc.toObject(Event.class);
                if (event != null) {
                     FragmentManager fragmentManager = getParentFragmentManager();
                     FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                     fragmentTransaction.replace(R.id.fragment_container, com.example.umeventplanner.EventForumFragment.newInstance(notification.getEventId(), event.getPlannerId(), false));
                     fragmentTransaction.addToBackStack(null);
                     fragmentTransaction.commit();
                }
            }
        });
    }
}

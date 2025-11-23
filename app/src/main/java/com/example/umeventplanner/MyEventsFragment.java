package com.example.umeventplanner;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umeventplanner.adapters.TicketAdapter;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MyEventsFragment extends Fragment implements TicketAdapter.OnTicketActionListener {

    private static final String TAG = "MyEventsFragment";

    private RecyclerView rvMyTickets;
    private TicketAdapter adapter;
    private List<Event> ticketList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_events, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        rvMyTickets = view.findViewById(R.id.rvMyTickets);
        ticketList = new ArrayList<>();

        rvMyTickets.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TicketAdapter(getContext(), ticketList, this);
        rvMyTickets.setAdapter(adapter);

        loadMyTickets();

        return view;
    }

    private void loadMyTickets() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).collection("registrations").get()
                .addOnSuccessListener(registrations -> {
                    List<Task<DocumentSnapshot>> eventTasks = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : registrations) {
                        String eventId = doc.getId();
                        eventTasks.add(db.collection("events").document(eventId).get());
                    }

                    Tasks.whenAllSuccess(eventTasks).addOnSuccessListener(snapshots -> {
                        ticketList.clear();
                        for (Object snapshot : snapshots) {
                            DocumentSnapshot docSnap = (DocumentSnapshot) snapshot;
                            Event event = docSnap.toObject(Event.class);
                            if (event != null) {
                                event.setEventId(docSnap.getId()); // Fix: Set the eventId
                                ticketList.add(event);
                            }
                        }
                        // Sort by date descending
                        Collections.sort(ticketList, (e1, e2) -> {
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                Date date1 = sdf.parse(e1.getDate());
                                Date date2 = sdf.parse(e2.getDate());
                                return date2.compareTo(date1);
                            } catch (ParseException e) {
                                return 0;
                            }
                        });
                        adapter.notifyDataSetChanged();
                    });
                });
    }

    @Override
    public void onScanQr(Event event) {
        Toast.makeText(getContext(), "Opening Scanner...", Toast.LENGTH_SHORT).show();
        // Scanner logic will be added here
    }

    @Override
    public void onRateEvent(Event event) {
        showRatingDialog(event);
    }

    private void showRatingDialog(Event event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_rate_event, null);

        final RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        final EditText etComment = dialogView.findViewById(R.id.etComment);
        final Button btnSubmitRating = dialogView.findViewById(R.id.btnSubmitRating);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        btnSubmitRating.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comment = etComment.getText().toString().trim();
            if (rating > 0) {
                submitRating(event, rating, comment, dialog);
            }
        });

        dialog.show();
    }

    private void submitRating(Event event, float rating, String comment, AlertDialog dialog) {
        final DocumentReference eventRef = db.collection("events").document(event.getEventId());

        // Transaction to update the average rating
        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot snapshot = transaction.get(eventRef);
            double currentSum = snapshot.contains("ratingSum") ? snapshot.getDouble("ratingSum") : 0.0;
            long currentCount = snapshot.contains("ratingCount") ? snapshot.getLong("ratingCount") : 0;

            double newSum = currentSum + rating;
            long newCount = currentCount + 1;

            transaction.update(eventRef, "ratingSum", newSum);
            transaction.update(eventRef, "ratingCount", newCount);
            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Rating Submitted!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();

            // Save the comment if it exists
            if (!comment.isEmpty()) {
                Map<String, Object> commentData = new HashMap<>();
                commentData.put("userId", mAuth.getCurrentUser().getUid());
                commentData.put("comment", comment);
                commentData.put("rating", rating);
                commentData.put("timestamp", FieldValue.serverTimestamp());

                eventRef.collection("comments").add(commentData);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to submit rating.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Rating submission failed", e);
        });
    }
}

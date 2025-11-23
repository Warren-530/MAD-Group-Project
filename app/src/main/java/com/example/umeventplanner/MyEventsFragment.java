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

import androidx.activity.result.ActivityResultLauncher;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Transaction;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

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
    private List<Ticket> ticketList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String targetEventId;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    markAttendance(result.getContents());
                }
            });

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
                    Map<String, String> registrationStatusMap = new HashMap<>();

                    for (QueryDocumentSnapshot doc : registrations) {
                        String eventId = doc.getId();
                        eventTasks.add(db.collection("events").document(eventId).get());
                        registrationStatusMap.put(eventId, doc.getString("status"));
                    }

                    Tasks.whenAllSuccess(eventTasks).addOnSuccessListener(snapshots -> {
                        ticketList.clear();
                        for (Object snapshot : snapshots) {
                            DocumentSnapshot docSnap = (DocumentSnapshot) snapshot;
                            Event event = docSnap.toObject(Event.class);
                            if (event != null) {
                                event.setEventId(docSnap.getId());
                                String status = registrationStatusMap.get(docSnap.getId());
                                ticketList.add(new Ticket(event, status != null ? status : "Registered"));
                            }
                        }
                        Collections.sort(ticketList, (t1, t2) -> {
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                Date date1 = sdf.parse(t1.getEvent().getDate());
                                Date date2 = sdf.parse(t2.getEvent().getDate());
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
    public void onScanQr(Ticket ticket) {
        this.targetEventId = ticket.getEvent().getEventId();
        ScanOptions options = new ScanOptions();
        options.setOrientationLocked(true);
        options.setPrompt("Scan Event QR Code");
        barcodeLauncher.launch(options);
    }

    private void markAttendance(String scannedEventId) {
        if (!scannedEventId.equals(targetEventId)) {
            Toast.makeText(getContext(), "Incorrect Event QR Code.", Toast.LENGTH_LONG).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference userRegRef = db.collection("users").document(userId).collection("registrations").document(scannedEventId);
        DocumentReference eventRegRef = db.collection("events").document(scannedEventId).collection("registrations").document(userId);

        db.runTransaction(transaction -> {
            transaction.update(userRegRef, "status", "Attended");
            transaction.update(eventRegRef, "status", "Attended");
            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Welcome! Checked in successfully.", Toast.LENGTH_SHORT).show();
            loadMyTickets(); // Refresh the list
        }).addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to mark attendance.", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRateEvent(Ticket ticket) {
        showRatingDialog(ticket.getEvent());
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
            if (!comment.isEmpty()) {
                Map<String, Object> commentData = new HashMap<>();
                commentData.put("userId", mAuth.getCurrentUser().getUid());
                commentData.put("comment", comment);
                commentData.put("rating", rating);
                commentData.put("timestamp", FieldValue.serverTimestamp());

                eventRef.collection("comments").add(commentData).addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Rating and comment submitted!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }).addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Rating submitted, but comment failed.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
            } else {
                Toast.makeText(getContext(), "Rating Submitted!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to submit rating.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Rating submission failed", e);
        });
    }
}

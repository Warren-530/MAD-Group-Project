package com.example.umeventplanner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.card.MaterialCardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class EventManagementFragment extends Fragment {

    private String eventId;
    private FirebaseFirestore db;

    private ImageView ivBanner;
    private TextView tvTitle, tvDateLocation;
    private com.google.android.material.chip.Chip chipStatus;
    private MaterialCardView cardEdit, cardQrCode, cardGuests, cardForum;
    private Button btnDeleteEvent;

    public static EventManagementFragment newInstance(String eventId) {
        EventManagementFragment fragment = new EventManagementFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_management, container, false);
        initViews(view);
        loadEventData();
        setupClickListeners();
        return view;
    }

    private void initViews(View view) {
        ivBanner = view.findViewById(R.id.ivBanner);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvDateLocation = view.findViewById(R.id.tvDateLocation);
        chipStatus = view.findViewById(R.id.chipStatus);
        cardEdit = view.findViewById(R.id.cardEdit);
        cardQrCode = view.findViewById(R.id.cardQrCode);
        cardGuests = view.findViewById(R.id.cardGuests);
        cardForum = view.findViewById(R.id.cardForum);
        btnDeleteEvent = view.findViewById(R.id.btnDeleteEvent);
    }

    private void loadEventData() {
        if (eventId == null) return;
        db.collection("events").document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Event event = documentSnapshot.toObject(Event.class);
                if (event != null) {
                    tvTitle.setText(event.getTitle());
                    tvDateLocation.setText(String.format("%s - %s", event.getDate(), event.getLocation()));
                    chipStatus.setText(event.getStatus());
                    if (getContext() != null && event.getBannerUrl() != null && !event.getBannerUrl().isEmpty()) {
                        Glide.with(getContext()).load(event.getBannerUrl()).into(ivBanner);
                    }
                }
            }
        });
    }

    private void setupClickListeners() {
        cardEdit.setOnClickListener(v -> {
            CreateEventFragment fragment = new CreateEventFragment();
            Bundle args = new Bundle();
            args.putString("eventId", eventId);
            fragment.setArguments(args);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        cardQrCode.setOnClickListener(v -> showQrCodeDialog());

        cardGuests.setOnClickListener(v -> {
            // TODO: Navigate to Guest List Fragment
        });

        cardForum.setOnClickListener(v -> {
            ForumFragment fragment = new ForumFragment();
            Bundle args = new Bundle();
            args.putString("eventId", eventId);
            fragment.setArguments(args);
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        btnDeleteEvent.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void showQrCodeDialog() {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(eventId, BarcodeFormat.QR_CODE, 800, 800);

            Dialog dialog = new Dialog(getContext());
            dialog.setContentView(R.layout.dialog_poster_zoom); // Re-using the zoom dialog layout
            ImageView ivZoomedPoster = dialog.findViewById(R.id.ivZoomedPoster);
            ivZoomedPoster.setImageBitmap(bitmap);
            dialog.findViewById(R.id.btnCloseZoom).setOnClickListener(v -> dialog.dismiss());
            dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Could not generate QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to permanently delete this event?")
                .setPositiveButton("Delete", (dialog, which) -> deleteEvent())
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void deleteEvent() {
        db.collection("events").document(eventId).delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Event deleted successfully", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error deleting event: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

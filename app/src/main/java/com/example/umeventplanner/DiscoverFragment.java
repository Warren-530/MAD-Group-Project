package com.example.umeventplanner;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class DiscoverFragment extends Fragment implements OnEventClickListener {

    private static final String TAG = "DiscoverFragment";
    private RecyclerView rvEvents;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private FirebaseFirestore db;

    private EditText etSearch;
    private ImageButton btnFilter, btnToggleView;
    private boolean isCompact = false;
    private List<String> activeFilters = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover, container, false);
        initViews(view);
        setupListeners();

        db = FirebaseFirestore.getInstance();
        loadEvents();

        return view;
    }

    private void initViews(View view) {
        rvEvents = view.findViewById(R.id.rvEvents);
        etSearch = view.findViewById(R.id.etSearch);
        btnFilter = view.findViewById(R.id.btnFilter);
        btnToggleView = view.findViewById(R.id.btnToggleView);

        rvEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(getContext(), eventList, this);
        rvEvents.setAdapter(eventAdapter);
    }

    private void setupListeners() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                eventAdapter.filter(s.toString(), activeFilters);
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        btnFilter.setOnClickListener(v -> showFilterBottomSheet());

        btnToggleView.setOnClickListener(v -> {
            isCompact = !isCompact;
            btnToggleView.setImageResource(isCompact ? android.R.drawable.ic_menu_sort_by_size : android.R.drawable.ic_menu_agenda);
            eventAdapter.setCompactView(isCompact);
        });
    }

    private void showFilterBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_filter, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        ChipGroup cgFilterCategories = bottomSheetView.findViewById(R.id.cgFilterCategories);
        for (int i = 0; i < cgFilterCategories.getChildCount(); i++) {
            Chip chip = (Chip) cgFilterCategories.getChildAt(i);
            if (activeFilters.contains(chip.getText().toString())) {
                chip.setChecked(true);
            }
        }

        Button btnApply = bottomSheetView.findViewById(R.id.btnApply);
        btnApply.setOnClickListener(v -> {
            activeFilters.clear();
            for (int id : cgFilterCategories.getCheckedChipIds()) {
                Chip chip = cgFilterCategories.findViewById(id);
                activeFilters.add(chip.getText().toString());
            }
            eventAdapter.filter(etSearch.getText().toString(), activeFilters);
            bottomSheetDialog.dismiss();
        });

        Button btnReset = bottomSheetView.findViewById(R.id.btnReset);
        btnReset.setOnClickListener(v -> {
            activeFilters.clear();
            cgFilterCategories.clearCheck();
            eventAdapter.filter(etSearch.getText().toString(), activeFilters);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void loadEvents() {
        db.collection("events")
                .whereEqualTo("status", "Published")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Successfully fetched " + queryDocumentSnapshots.size() + " events.");
                    List<Event> fetchedEvents = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots) {
                        Event event = document.toObject(Event.class);
                        if (event != null) {
                            event.setEventId(document.getId());
                            fetchedEvents.add(event);
                            Log.d(TAG, "Event added: " + event.getTitle() + " with ID: " + event.getEventId());
                        }
                    }
                    eventAdapter.setAllEvents(fetchedEvents);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading events", e);
                });
    }

    @Override
    public void onEventClick(Event event) {
        if (event != null && event.getEventId() != null) {
            Log.d(TAG, "Clicked event with ID: " + event.getEventId());
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, EventDetailsFragment.newInstance(event.getEventId()))
                    .addToBackStack(null)
                    .commit();
        } else {
            Log.e(TAG, "Clicked event is null or has no ID!");
        }
    }
}

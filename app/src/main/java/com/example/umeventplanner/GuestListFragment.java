package com.example.umeventplanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umeventplanner.adapters.GuestAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class GuestListFragment extends Fragment {

    private RecyclerView rvGuests;
    private GuestAdapter adapter;
    private List<User> guestList;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private String eventId;

    public static GuestListFragment newInstance(String eventId) {
        GuestListFragment fragment = new GuestListFragment();
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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guest_list, container, false);

        db = FirebaseFirestore.getInstance();
        rvGuests = view.findViewById(R.id.rvGuests);
        progressBar = view.findViewById(R.id.progressBar);
        guestList = new ArrayList<>();

        rvGuests.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GuestAdapter(getContext(), guestList);
        rvGuests.setAdapter(adapter);

        loadGuests();

        return view;
    }

    private void loadGuests() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("events").document(eventId).collection("registrations")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    guestList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        guestList.add(user);
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                });
    }
}

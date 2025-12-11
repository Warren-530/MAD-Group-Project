package com.example.umeventplanner;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umeventplanner.adapters.UserAdapter;
import com.example.umeventplanner.models.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InviteDialogFragment extends DialogFragment {

    public static final String TAG = "InviteDialogFragment";
    private static final String ARG_EVENT_ID = "eventId";

    private String eventId;
    private EditText etSearch;
    private RecyclerView rvUsers;
    private Button btnInvite;
    private UserAdapter adapter;
    private List<User> userList = new ArrayList<>();
    private List<User> selectedUsers = new ArrayList<>();
    private FirebaseFirestore db;

    public static InviteDialogFragment newInstance(String eventId) {
        InviteDialogFragment fragment = new InviteDialogFragment();
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
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_UMEventPlanner_FullScreenDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_invite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        etSearch = view.findViewById(R.id.et_search_user);
        rvUsers = view.findViewById(R.id.rv_users);
        btnInvite = view.findViewById(R.id.btn_invite_selected);

        setupRecyclerView();
        setupSearch();

        btnInvite.setOnClickListener(v -> inviteSelectedUsers());

        searchUsers(""); // Initial load
    }

    private void setupRecyclerView() {
        adapter = new UserAdapter(getContext(), userList, new UserAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(User user) {
                if (selectedUsers.contains(user)) {
                    selectedUsers.remove(user);
                } else {
                    selectedUsers.add(user);
                }
                adapter.setSelectedUsers(selectedUsers);
            }
        });
        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvUsers.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString());
            }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    private void searchUsers(String query) {
        db.collection("users").orderBy("name").startAt(query).endAt(query + "\uf8ff").get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                userList.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    User user = document.toObject(User.class);
                    user.setUserId(document.getId());

                    db.collection("events").document(eventId).collection("registrations").document(user.getUserId()).get()
                        .addOnSuccessListener(regDoc -> {
                            if (regDoc.exists()) {
                                user.setRegistrationStatus(regDoc.getString("status"));
                            }
                            userList.add(user);
                            adapter.notifyDataSetChanged();
                        });
                }
            });
    }

    private void inviteSelectedUsers() {
        if (selectedUsers.isEmpty()) {
            Toast.makeText(getContext(), "No users selected", Toast.LENGTH_SHORT).show();
            return;
        }

        for (User user : selectedUsers) {
            // Add to event registrations
            Map<String, Object> registration = new HashMap<>();
            registration.put("status", "invited");
            db.collection("events").document(eventId).collection("registrations").document(user.getUserId()).set(registration);

            // Add to user's registrations
            Map<String, Object> event = new HashMap<>();
            event.put("eventId", eventId);
            db.collection("users").document(user.getUserId()).collection("registrations").document(eventId).set(event);

            // Create a notification
            db.collection("events").document(eventId).get().addOnSuccessListener(eventDoc -> {
                String eventName = eventDoc.getString("title");
                String message = "You have been invited to the event: " + eventName;
                sendNotification(user.getUserId(), message);
            });
        }

        Toast.makeText(getContext(), "Invitations sent.", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    private void sendNotification(String userId, String message) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("message", message);
        notification.put("timestamp", new Date());
        notification.put("read", false);

        db.collection("users").document(userId).collection("notifications").add(notification);
    }
}

package com.example.umeventplanner;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.umeventplanner.adapters.UserAdapter;
import com.example.umeventplanner.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserSearchDialogFragment extends DialogFragment {

    private static final String ARG_EVENT_ID = "eventId";

    private String eventId;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList = new ArrayList<>();
    private List<User> selectedUsers = new ArrayList<>();
    private FirebaseFirestore db;
    private MaterialButton btnInvite;
    private Toolbar toolbar;

    public interface OnUsersSelectedListener {
        void onUsersSelected(List<User> users);
    }

    private OnUsersSelectedListener listener;

    public static UserSearchDialogFragment newInstance(String eventId) {
        UserSearchDialogFragment fragment = new UserSearchDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnUsersSelectedListener(OnUsersSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_UMEventPlanner_FullScreenDialog);
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_user_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (eventId == null) {
            Toast.makeText(getContext(), "Error: Event ID is missing.", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        db = FirebaseFirestore.getInstance();
        toolbar = view.findViewById(R.id.toolbar);
        searchView = view.findViewById(R.id.search_view);
        recyclerView = view.findViewById(R.id.rv_users);
        btnInvite = view.findViewById(R.id.btn_invite);

        toolbar.setNavigationOnClickListener(v -> dismiss());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserAdapter(getContext(), userList, user -> {
            String status = user.getRegistrationStatus();
            if ("registered".equals(status) || "invited".equals(status)) {
                Toast.makeText(getContext(), "This user is already registered or invited.", Toast.LENGTH_SHORT).show();
            } else {
                if (selectedUsers.contains(user)) {
                    selectedUsers.remove(user);
                } else {
                    selectedUsers.add(user);
                }
                adapter.setSelectedUsers(selectedUsers);
            }
        });
        recyclerView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchUsers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchUsers(newText);
                return true;
            }
        });

        btnInvite.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUsersSelected(selectedUsers);
            }
            dismiss();
        });
    }

    private void searchUsers(String query) {
        if (eventId == null) {
            return;
        }
        if (query.isEmpty()) {
            userList.clear();
            adapter.notifyDataSetChanged();
            return;
        }
        db.collection("users").whereGreaterThanOrEqualTo("name", query).whereLessThanOrEqualTo("name", query + "\uf8ff").get()
                .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
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
            } else {
                Toast.makeText(getContext(), "Error getting users", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

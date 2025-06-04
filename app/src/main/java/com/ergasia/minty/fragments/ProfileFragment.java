package com.ergasia.minty.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ergasia.minty.R;
import com.ergasia.minty.views.UserViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileFragment extends Fragment {

    private UserViewModel userViewModel;
    private TextInputEditText usernameEdit;
    private TextInputEditText emailEditText;
    private MaterialButton updateProfileButton;
    private final String TAG = "ProfileFragment";

    public ProfileFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        usernameEdit = view.findViewById(R.id.usernameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        updateProfileButton = view.findViewById(R.id.saveProfileButton);

        setupObservers();
        setupListeners();


//        mAuth = FirebaseAuth.getInstance();
//        db = FirebaseFirestore.getInstance();
//        FirebaseUser user = mAuth.getCurrentUser();
//
//        if (user != null) {
//            emailEditText.setText(user.getEmail());
//
//            DocumentReference userReference = db.collection("users").document(user.getUid());
//            Query transactionsQuery = userReference.collection("transactions");
//
//            // count documents in the transactions subcollection
//            AggregateQuery countQuery = transactionsQuery.count();
//            countQuery.get(AggregateSource.SERVER).addOnCompleteListener(new OnCompleteListener<AggregateQuerySnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<AggregateQuerySnapshot> task) {
//                    if (task.isSuccessful()) {
//                        AggregateQuerySnapshot snapshot = task.getResult();
//                        int transactionsCount = (int) snapshot.getCount(); // get the count
//                        Log.d("Firestore", "Total transactions: " + transactionsCount);
//
//                    } else {
//                        Log.d("Firestore", "Count failed: " + task.getException());
//                    }
//                }
//            });
//
//
//            db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
//                if (documentSnapshot.exists()) {
//                    usernameEdit.setText(documentSnapshot.getString("username"));
//                    Log.d(TAG, "Username set");
//                } else {
//                    Log.w(TAG, "User not found");
//                }
//            });
//
//            updateProfileButton.setOnClickListener(v -> {
//                updateProfile(user, Objects.requireNonNull(usernameEdit.getText()).toString().trim());
//            });
//
//        }
        return view;
    }


    private void setupListeners() {
        updateProfileButton.setOnClickListener(v -> {
            String newUsername = usernameEdit.getText().toString().trim();
            if (!newUsername.isEmpty()) {
                userViewModel.updateUsername(newUsername);
                Toast.makeText(requireContext(), "Username updated", Toast.LENGTH_SHORT).show();
            } else {
                usernameEdit.setError("Username cannot be empty");
            }
        });
    }

    private void setupObservers() {
        userViewModel.getUsername().observe(getViewLifecycleOwner(), username -> {
            if (username != null) {
                usernameEdit.setText(username);
            }
        });

        userViewModel.getEmail().observe(getViewLifecycleOwner(), email -> {
            if (email != null) {
                emailEditText.setText(email);
            }
        });

        userViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        userViewModel.getTransactionsCount().observe(getViewLifecycleOwner(), count -> {
            // You can display this count somewhere in your UI if needed
        });
    }


    /**
     * Update the profile's username
     */
//    private void updateProfile(FirebaseUser user, String username) {
//        DocumentReference docRef = db.collection("users").document(user.getUid());
//        docRef.update("username", username).addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void unused) {
//                Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
//                Log.d(TAG, "Profile updated");
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.w(TAG, "Error updating document ", e);
//                Toast.makeText(getContext(), "Profile failed to update", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
    @Override
    public void onResume() {
        super.onResume();
        userViewModel.getProfile();
    }
}

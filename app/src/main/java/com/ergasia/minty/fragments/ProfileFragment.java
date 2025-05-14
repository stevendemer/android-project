package com.ergasia.minty.fragments;

import static android.app.Activity.RESULT_OK;

import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.ergasia.minty.R;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

public class ProfileFragment extends Fragment {

    private TextInputEditText usernameEdit;

    private TextView transactionsAmount;
    private TextInputEditText emailEditText;
    private MaterialButton updateProfileButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private final String TAG = "ProfileFragment";

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        usernameEdit = view.findViewById(R.id.usernameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        updateProfileButton = view.findViewById(R.id.saveProfileButton);
        transactionsAmount = view.findViewById(R.id.transactionsText);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            emailEditText.setText(user.getEmail());

            DocumentReference userReference = db.collection("users").document(user.getUid());
            Query transactionsQuery = userReference.collection("transactions");

            // count documents in the transactions subcollection
            AggregateQuery countQuery = transactionsQuery.count();
            countQuery.get(AggregateSource.SERVER).addOnCompleteListener(new OnCompleteListener<AggregateQuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<AggregateQuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        AggregateQuerySnapshot snapshot = task.getResult();
                        int transactionsCount = (int) snapshot.getCount(); // get the count
                        Log.d("Firestore", "Total transactions: " + transactionsCount);

                        transactionsAmount.setText("Total transactions: " + transactionsCount);
                    } else {
                        Log.d("Firestore", "Count failed: " + task.getException());
                    }
                }
            });


            db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    usernameEdit.setText(documentSnapshot.getString("username"));
                    Log.d(TAG, "Username set");
                } else {
                    Log.w(TAG, "User not found");
                }
            });

            updateProfileButton.setOnClickListener(v -> {
                updateProfile(user, Objects.requireNonNull(usernameEdit.getText()).toString().trim());
            });

        }
        return view;
    }

    /**
     * Update the profile's username
     */
    private void updateProfile(FirebaseUser user, String username) {
        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.update("username", username).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Profile updated");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error updating document ", e);
                Toast.makeText(getContext(), "Profile failed to update", Toast.LENGTH_SHORT).show();
            }
        });
    }

}

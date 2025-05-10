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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfileFragment  extends Fragment {

    private TextInputEditText usernameEdit;
    private ShapeableImageView imageView;
    private TextInputEditText emailEditText;
    private MaterialButton selectImageButton;
    private MaterialButton uploadImageButton;
    private CircularProgressIndicator progressIndicator;

    private MaterialButton updateProfileButton;
    private Uri profileImage;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private final String TAG = "ProfileFragment";
    StorageReference reference;

    public ProfileFragment() {
    }

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                if (result.getData() != null) {
                    profileImage = result.getData().getData();
                    Glide.with(getContext()).load(profileImage).into(imageView);
                    uploadImageButton.setVisibility(View.VISIBLE);
                }
            } else {
                Toast.makeText(getContext(), "Please select an image", Toast.LENGTH_SHORT).show();
            }
        }
    });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imageView = view.findViewById(R.id.profileImageView);
        usernameEdit = view.findViewById(R.id.usernameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        updateProfileButton = view.findViewById(R.id.saveProfileButton);
        selectImageButton = view.findViewById(R.id.selectImageButton);
        uploadImageButton = view.findViewById(R.id.uploadImageButton);
        progressIndicator = view.findViewById(R.id.progress);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();

        uploadImageButton.setVisibility(View.GONE);
        progressIndicator.setVisibility(View.GONE);

        if (user != null) {
            emailEditText.setText(user.getEmail());

            db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    usernameEdit.setText(documentSnapshot.getString("username"));
                    Log.d(TAG, "Username set");
                } else {
                    Log.w(TAG, "User not found");
                }
            });

        selectImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            activityResultLauncher.launch(intent);
        });

        uploadImageButton.setOnClickListener(v -> {
            progressIndicator.setVisibility(View.VISIBLE);
            uploadImage(profileImage, user.getUid());
        });

        updateProfileButton.setOnClickListener(v -> {
            String newUsername = usernameEdit.getText().toString().trim();
            updateProfile(user, newUsername);
        });
        Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
        }
        return view;
    }
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
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

    private void uploadImage(Uri image, String uid) {
        StorageReference ref = reference.child("profile_images/" + uid);
        ref.putFile(image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getContext(), "Image uploaded!", Toast.LENGTH_SHORT).show();
                progressIndicator.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
                progressIndicator.setVisibility(View.GONE);
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                progressIndicator.setMax(Math.toIntExact(snapshot.getTotalByteCount()));
                progressIndicator.setProgress(Math.toIntExact(snapshot.getBytesTransferred()));
            }
        });
    }
}

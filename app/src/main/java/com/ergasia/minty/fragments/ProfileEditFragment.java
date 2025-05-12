package com.ergasia.minty.fragments;

import static android.app.Activity.RESULT_OK;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.ergasia.minty.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
public class ProfileEditFragment extends Fragment {

    private TextInputEditText usernameEdit;
    private TextInputEditText emailEdit;
    private ShapeableImageView imageView;
    private MaterialButton selectImageButton;
    private MaterialButton uploadImageButton;
    private MaterialButton saveProfileButton;
    private MaterialButton cancelButton;

    private CircularProgressIndicator progressIndicator;

    private Uri profileImage;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private final String TAG = "ProfileEditFragment";
    private StorageReference reference;

    public ProfileEditFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_edit, container, false);

        imageView = view.findViewById(R.id.profileImageView);
        usernameEdit = view.findViewById(R.id.usernameEditText);
        emailEdit = view.findViewById(R.id.emailEditText);
        selectImageButton = view.findViewById(R.id.selectImageButton);
        uploadImageButton = view.findViewById(R.id.uploadImageButton);
        saveProfileButton = view.findViewById(R.id.saveProfileButton);
        progressIndicator = view.findViewById(R.id.progress);
        cancelButton = view.findViewById(R.id.cancelButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        reference = storage.getReference();

        uploadImageButton.setVisibility(View.GONE);
        progressIndicator.setVisibility(View.GONE);

        if (user != null) {db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            usernameEdit.setText(documentSnapshot.getString("username"));
                            emailEdit.setText(documentSnapshot.getString("email"));
                            String imageUrl = documentSnapshot.getString("profileImageUrl");
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Glide.with(getContext()).load(imageUrl).into(imageView);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to load user profile: " + e.getMessage());
                        Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                    });

            selectImageButton.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1001);
            });

            uploadImageButton.setOnClickListener(v -> {
                progressIndicator.setVisibility(View.VISIBLE);
                uploadImage(profileImage, user.getUid());
            });
            saveProfileButton.setOnClickListener(v -> {
                String newUsername = usernameEdit.getText().toString().trim();
                if (!newUsername.isEmpty()) {
                    progressIndicator.setVisibility(View.VISIBLE);

                    if (profileImage != null) {
                        uploadImage(profileImage, user.getUid());
                    } else {
                        db.collection("users").document(user.getUid())
                                .update("username", newUsername)
                                .addOnSuccessListener(unused -> {
                                    progressIndicator.setVisibility(View.GONE);
                                    Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                                    requireActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
                                })
                                .addOnFailureListener(e -> {
                                    progressIndicator.setVisibility(View.GONE);
                                    Toast.makeText(getContext(), "Failed to update", Toast.LENGTH_SHORT).show();
                                });
                    }
                } else {
                    usernameEdit.setError("Username cannot be empty");
                    return;
                }

            });
            cancelButton.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
            });
        }

        return view;
    }

    private void uploadImage(Uri image, String uid) {
        if (image == null) {
            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference ref = reference.child("profile_images/" + uid);
        ref.putFile(image).addOnSuccessListener(taskSnapshot -> {
            ref.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();

                db.collection("users").document(uid)
                        .update("profileImageUrl", imageUrl)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(getContext(), "Image uploaded!", Toast.LENGTH_SHORT).show();
                            Glide.with(getContext()).load(imageUrl).into(imageView);
                            progressIndicator.setVisibility(View.GONE);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to save image URL", Toast.LENGTH_SHORT).show();
                            progressIndicator.setVisibility(View.GONE);
                        });
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
            progressIndicator.setVisibility(View.GONE);
        }).addOnProgressListener(snapshot -> {
            progressIndicator.setMax(Math.toIntExact(snapshot.getTotalByteCount()));
            progressIndicator.setProgress(Math.toIntExact(snapshot.getBytesTransferred()));
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            profileImage = data.getData();
            Glide.with(getContext()).load(profileImage).into(imageView);
            uploadImageButton.setVisibility(View.VISIBLE);
            uploadImageButton.setEnabled(true);
        }
    }
}

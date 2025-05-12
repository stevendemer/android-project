package com.ergasia.minty.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.ergasia.minty.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ergasia.minty.LoginActivity;
public class ProfileFragment  extends Fragment {

    private TextView usernameView;
    private ShapeableImageView imageView;
    private TextView emailView;
    private MaterialButton editProfileButton;
    private MaterialButton logoutButton;
    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imageView = view.findViewById(R.id.profileImageView);
        usernameView = view.findViewById(R.id.usernameTextView);
        emailView = view.findViewById(R.id.emailTextView);
        editProfileButton = view.findViewById(R.id.editProfileButton);
        logoutButton = view.findViewById(R.id.logoutButton);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (user != null) {
            emailView.setText(user.getEmail());

            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            usernameView.setText(username != null ? username : "No username");

                            String imageUrl = documentSnapshot.getString("profileImageUrl");
                            if (imageUrl == null) {
                                Toast.makeText(getContext(), "No image", Toast.LENGTH_SHORT).show();
                                imageUrl = "";
                            }
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Glide.with(getContext()).load(imageUrl).into(imageView);
                                imageView.setVisibility(View.VISIBLE);
                            }
                        }
                    });

            editProfileButton.setOnClickListener(v -> {
                ProfileEditFragment profileEditFragment = new ProfileEditFragment();
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.add(R.id.fragment_container, profileEditFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            });
            logoutButton.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                requireActivity().finish();
            });

        }
        return view;
    }
}

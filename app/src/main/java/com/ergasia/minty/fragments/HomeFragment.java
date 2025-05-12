package com.ergasia.minty.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.ergasia.minty.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

public class HomeFragment  extends Fragment {

    private TextView usernameTextView;

    private TextView incomeTextView;

    private final String TAG = "HomeFragment";

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        usernameTextView = view.findViewById(R.id.usernameTextView);
        incomeTextView = view.findViewById(R.id.incomeTextView);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        if (user != null) {
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    usernameTextView.setText(documentSnapshot.getString("username"));
                    incomeTextView.setText(documentSnapshot.getString("income"));
                    Log.d(TAG, "User found");
                } else {
                    Log.d(TAG, "User not found");
                }
            }).addOnFailureListener(v -> {
                Log.d(TAG, "Error finding document");
            });

        } else {
            Log.d(TAG, "User not logged in");
        }

        return view;
    }
}

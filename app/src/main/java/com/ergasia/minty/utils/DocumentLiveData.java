package com.ergasia.minty.utils;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

public class DocumentLiveData extends LiveData<DocumentSnapshot> {
    private final DocumentReference docRef;
    private ListenerRegistration registration;

    public DocumentLiveData(DocumentReference docRef) {
        this.docRef = docRef;
    }

    @Override
    protected void onActive() {
        registration = docRef.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                Log.e("LiveData", "Listen failed: ", error);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                setValue(snapshot);
            }
        });
    }

    @Override
    protected void onInactive() {
        if (registration != null) {
            registration.remove();
        }
    }
}

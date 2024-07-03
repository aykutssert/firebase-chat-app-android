package com.example.realtimechatapp.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.realtimechatapp.R;
import com.example.realtimechatapp.utils.Constans;
import com.example.realtimechatapp.utils.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class BaseActivity extends AppCompatActivity {
    private DocumentReference documentReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        documentReference = db.collection(Constans.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constans.KEY_USER_ID));
    }

    @Override
    protected void onPause() {
        super.onPause();
        documentReference.update(Constans.KEY_AVAILABILITY,0);

    }

    @Override
    protected void onResume() {
        super.onResume();
        documentReference.update(Constans.KEY_AVAILABILITY,1);
    }
}
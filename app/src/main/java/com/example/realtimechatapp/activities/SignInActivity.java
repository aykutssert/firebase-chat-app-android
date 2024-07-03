package com.example.realtimechatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.realtimechatapp.databinding.ActivitySignInBinding;
import com.example.realtimechatapp.utils.Constans;
import com.example.realtimechatapp.utils.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class SignInActivity extends AppCompatActivity {
    private ActivitySignInBinding activitySignInBinding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(Constans.KEY_IS_SIGNED_IN)){
            long signInTime = preferenceManager.getLong(Constans.KEY_SIGN_IN_TIME);
            long currentTime = System.currentTimeMillis();
            long sessionDuration = 24 * 60  * 60 * 1000; //24 hours (1 * 60 * 1000 = 1 dakika)
            if((currentTime - signInTime) < sessionDuration){
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
            else{
                preferenceManager.clear();
                showToast("Session expired. Please sign in again.");
            }

        }
        activitySignInBinding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(activitySignInBinding.getRoot());

        setListeners();

    }
    private void setListeners(){
        activitySignInBinding.textCreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        //activitySignInBinding.buttonSignIn.setOnClickListener(v -> addDataToFireStore());
        activitySignInBinding.buttonSignIn.setOnClickListener(v -> {
            if(isValidSignUpDetails()){
                signIn();
            }
        });

    }
    //SHOW TOAST
    private void showToast(String message){

        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();

    }
    private void signIn(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constans.KEY_COLLECTION_USERS)
                .whereEqualTo(Constans.KEY_EMAIL,activitySignInBinding.inputEmail.getText().toString())
                .whereEqualTo(Constans.KEY_PASSWORD,activitySignInBinding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task ->{
                    if(task.isSuccessful() && task.getResult() !=null && task.getResult().getDocuments().size()>0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);

                        preferenceManager.putBoolean(Constans.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constans.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constans.KEY_NAME, documentSnapshot.getString(Constans.KEY_NAME));
                        preferenceManager.putString(Constans.KEY_IMAGE, documentSnapshot.getString(Constans.KEY_IMAGE));
                        preferenceManager.putLong(Constans.KEY_SIGN_IN_TIME, System.currentTimeMillis());

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    else{
                        loading(false);
                        showToast("Unable to sign in");
                    }
                });

    }
    private Boolean isValidSignUpDetails() {
        if (activitySignInBinding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter an email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(activitySignInBinding.inputEmail.getText().toString()).matches()) {
            showToast("Enter an valid email");
            return false;
        } else if (activitySignInBinding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;
        }
        else {
            return true;
        }
    }
    private void loading(Boolean isLoading){
        if(isLoading){
            activitySignInBinding.buttonSignIn.setVisibility(View.INVISIBLE);
            activitySignInBinding.progressBar.setVisibility(View.VISIBLE);
        }
        else{

            activitySignInBinding.buttonSignIn.setVisibility(View.VISIBLE);
            activitySignInBinding.progressBar.setVisibility(View.INVISIBLE);

        }
    }

}
package com.example.realtimechatapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.realtimechatapp.databinding.ActivitySignUpBinding;
import com.example.realtimechatapp.utils.Constans;
import com.example.realtimechatapp.utils.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding activitySignUpBinding;
    private String encodedImage;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySignUpBinding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(activitySignUpBinding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }
    private void setListeners(){
        activitySignUpBinding.textSignIn.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignInActivity.class)));
        activitySignUpBinding.buttonSignUp.setOnClickListener(v -> {
            if(isValidSignUpDetails()){
                signUp();
            }
        });
        activitySignUpBinding.layoutImage.setOnClickListener(v ->{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);

        });
    }

    //SHOW TOAST
    private void showToast(String message){

        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();

    }

    //REGISTER
    private void signUp() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        // Kullanıcı girişlerini al
        String email = activitySignUpBinding.inputEmail.getText().toString();
        String username = activitySignUpBinding.inputUsername.getText().toString();
        String password = activitySignUpBinding.inputPassword.getText().toString();

        // E-posta ve kullanıcı adının benzersiz olup olmadığını kontrol et
        database.collection(Constans.KEY_COLLECTION_USERS)
                .whereEqualTo(Constans.KEY_EMAIL, email)
                .get()
                .addOnCompleteListener(emailTask -> {
                    if (emailTask.isSuccessful() && !emailTask.getResult().isEmpty()) {
                        loading(false);
                        showToast("This email is already in use.");
                    } else {
                        // Eğer e-posta benzersizse, kullanıcı adı kontrolünü yap
                        database.collection(Constans.KEY_COLLECTION_USERS)
                                .whereEqualTo(Constans.KEY_NAME, username)
                                .get()
                                .addOnCompleteListener(usernameTask -> {
                                    if (usernameTask.isSuccessful() && !usernameTask.getResult().isEmpty()) {
                                        loading(false);
                                        showToast("This username is already taken.");
                                    } else {
                                        // E-posta ve kullanıcı adı benzersizse, kullanıcıyı kaydet
                                        HashMap<String, Object> user = new HashMap<>();
                                        user.put(Constans.KEY_NAME, username);
                                        user.put(Constans.KEY_EMAIL, email);
                                        user.put(Constans.KEY_PASSWORD, password);
                                        user.put(Constans.KEY_IMAGE, encodedImage);
                                        database.collection(Constans.KEY_COLLECTION_USERS)
                                                .add(user)
                                                .addOnSuccessListener(ref -> {
                                                    loading(false);
                                                    preferenceManager.putBoolean(Constans.KEY_PREFERENCE_NAME, true);
                                                    preferenceManager.putString(Constans.KEY_USER_ID, ref.getId());
                                                    preferenceManager.putString(Constans.KEY_NAME, username);
                                                    preferenceManager.putString(Constans.KEY_IMAGE, encodedImage);

                                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);
                                                })
                                                .addOnFailureListener(exception -> {
                                                    loading(false);
                                                    showToast(exception.getMessage());
                                                });
                                    }
                                });
                    }
                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage());
                });
    }

    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight()  *   previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),result ->{
                if(result.getResultCode() == RESULT_OK){
                    if(result.getData()!=null){
                        Uri imageUri = result.getData().getData();
                        try{
                            assert imageUri != null;
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            activitySignUpBinding.imageProfile.setImageBitmap(bitmap);
                            activitySignUpBinding.textAddImage.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private Boolean isValidSignUpDetails() {
        if (encodedImage == null) {
            showToast("Select profile image");
            return false;
        } else if (activitySignUpBinding.inputUsername.getText().toString().trim().isEmpty()) {
            showToast("Enter an username");
            return false;
        } else if (activitySignUpBinding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter an email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(activitySignUpBinding.inputEmail.getText().toString()).matches()) {
            showToast("Enter an valid email");
            return false;
        } else if (activitySignUpBinding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;
        } else if (activitySignUpBinding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Confirm your password");
            return false;
        } else if (!activitySignUpBinding.inputConfirmPassword.getText().toString().equals(activitySignUpBinding.inputPassword.getText().toString())) {
            showToast("Passwords must be matched");
            return false;
        } else {
            return true;
        }
    }
    private void loading(Boolean isLoading){
        if(isLoading){
            activitySignUpBinding.buttonSignUp.setVisibility(View.INVISIBLE);
            activitySignUpBinding.progressBar.setVisibility(View.VISIBLE);
        }
        else{

                activitySignUpBinding.buttonSignUp.setVisibility(View.VISIBLE);
                activitySignUpBinding.progressBar.setVisibility(View.INVISIBLE);

        }
    }
}
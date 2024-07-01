package com.example.realtimechatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.realtimechatapp.adapters.UserAdapter;
import com.example.realtimechatapp.databinding.ActivityUsersBinding;
import com.example.realtimechatapp.listeners.UserListener;
import com.example.realtimechatapp.models.User;
import com.example.realtimechatapp.utils.Constans;
import com.example.realtimechatapp.utils.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity  implements UserListener {
    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        getUsers();
    }
    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),MainActivity.class)));
    }
    private void getUsers(){
        loading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constans.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task ->{
                    loading(false);
                    String currentUserId=preferenceManager.getString(Constans.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult()!=null){
                        List<User> users = new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()){
                            if(currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constans.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constans.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constans.KEY_IMAGE);
                            user.fcmToken = queryDocumentSnapshot.getString(Constans.KEY_FCM_TOKEN);
                            user.id =queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if(users.size() > 0){
                            UserAdapter userAdapter = new UserAdapter(users,this);
                            binding.usersRecyclerView.setAdapter(userAdapter);
                            binding.usersRecyclerView.setVisibility(View.VISIBLE);
                        }
                        else{
                            showErrorMessage();
                        }
                    }
                    else{
                        showErrorMessage();
                    }
                });
    }
    private void showErrorMessage(){
        binding.textErrorMessage.setText(String.format("%s","No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }
    private void loading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);

        }
        else{

            binding.progressBar.setVisibility(View.INVISIBLE);


        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constans.KEY_USER,user);
        startActivity(intent);
        finish();
    }
}
package com.example.realtimechatapp.activities;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.example.realtimechatapp.adapters.RecentConversationsAdapter;
import com.example.realtimechatapp.databinding.ActivityMainBinding;
import com.example.realtimechatapp.listeners.ConversionListener;
import com.example.realtimechatapp.models.ChatMessage;
import com.example.realtimechatapp.models.User;
import com.example.realtimechatapp.utils.Constans;
import com.example.realtimechatapp.utils.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends BaseActivity implements ConversionListener {
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private RecentConversationsAdapter conversationsAdapter;
    private FirebaseFirestore db;
    private String covId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        loadUserDetails();
        getToken();
        setListeners();
        listenConversations();
    }
    private void listenConversations(){
        db.collection(Constans.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constans.KEY_SENDER_ID,preferenceManager.getString(Constans.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        db.collection(Constans.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constans.KEY_RECEIVER_ID,preferenceManager.getString(Constans.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }
    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
      if(error!=null){
          return;
      }
      if(value!=null){

          for(DocumentChange documentChange: value.getDocumentChanges()){
              if(documentChange.getType() == DocumentChange.Type.ADDED){
                    Log.d("eklendi","");
                    covId = documentChange.getDocument().getId();
                  String senderId = documentChange.getDocument().getString(Constans.KEY_SENDER_ID);
                  String receiverId = documentChange.getDocument().getString(Constans.KEY_RECEIVER_ID);
                  ChatMessage chatMessage = new ChatMessage();
                  chatMessage.senderId = senderId;
                  chatMessage.receiverId = receiverId;
                  if(preferenceManager.getString(Constans.KEY_USER_ID).equals(senderId)){
                      chatMessage.conversionImage = documentChange.getDocument().getString(Constans.KEY_RECEIVER_IMAGE);
                      chatMessage.ConversionName = documentChange.getDocument().getString(Constans.KEY_RECEIVER_NAME);
                      chatMessage.conversionId = documentChange.getDocument().getString(Constans.KEY_RECEIVER_ID);

                  }
                  else{
                      chatMessage.conversionImage = documentChange.getDocument().getString(Constans.KEY_SENDER_IMAGE);
                      chatMessage.ConversionName = documentChange.getDocument().getString(Constans.KEY_SENDER_NAME);
                      chatMessage.conversionId = documentChange.getDocument().getString(Constans.KEY_SENDER_ID);
                  }
                  chatMessage.message = documentChange.getDocument().getString(Constans.KEY_LAST_MESSAGE);
                  chatMessage.dateObject = documentChange.getDocument().getDate(Constans.KEY_TIMESTAMP);
                  chatMessage.isRead = documentChange.getDocument().getString("receiverRead");
                  Log.d("isRead",""+chatMessage.isRead);
                  conversations.add(chatMessage);
              }
              else if(documentChange.getType() == DocumentChange.Type.MODIFIED){
                  Log.d("modifiye","");
                  for (int i=0;i<conversations.size();i++){
                      String senderId = documentChange.getDocument().getString(Constans.KEY_SENDER_ID);
                      String receiverId = documentChange.getDocument().getString(Constans.KEY_RECEIVER_ID);
                      if(conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)){
                          conversations.get(i).message = documentChange.getDocument().getString(Constans.KEY_LAST_MESSAGE);
                          conversations.get(i).dateObject = documentChange.getDocument().getDate(Constans.KEY_TIMESTAMP);
                          conversations.get(i).isRead = "false";
                          break;
                      }
                  }
              }
          }
          conversations.sort((obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
          conversationsAdapter.notifyDataSetChanged();
          binding.conversationRecView.smoothScrollToPosition(0);
          binding.conversationRecView.setVisibility(View.VISIBLE);
          binding.progressBar.setVisibility(View.GONE);
      }
    };
    private void init(){
        conversations = new ArrayList<>();
        conversationsAdapter = new RecentConversationsAdapter(conversations,this);
        binding.conversationRecView.setAdapter(conversationsAdapter);
        db = FirebaseFirestore.getInstance();
    }
    private void setListeners() {
        binding.imageSignOut.setOnClickListener(v-> signOut());
        binding.fabNewChat.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), UsersActivity.class)));
    }

    private void loadUserDetails(){
        binding.textName.setText(preferenceManager.getString(Constans.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constans.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }
    //SHOW TOAST
    private void showToast(String message){

        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();

    }
    private void getToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    updateToken(token);
                    Log.d("MainFcm", "FCM Token: " + token);
                })
                .addOnFailureListener(e -> Log.e("FCM", "Failed to get FCM token", e));
    }


    private void updateToken(String token) {
        preferenceManager.putString(Constans.KEY_FCM_TOKEN, token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        String userId = preferenceManager.getString(Constans.KEY_USER_ID);

        if (userId == null || userId.isEmpty()) {
            showToast("User ID is missing");
            return;
        }
        DocumentReference documentReference = database.collection(Constans.KEY_COLLECTION_USERS)
                .document(userId);

        documentReference.update(Constans.KEY_FCM_TOKEN, token)
                .addOnSuccessListener(aVoid -> {})
                .addOnFailureListener(e -> {});
    }

    private void signOut(){
        showToast("signing out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constans.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constans.KEY_USER_ID));

        HashMap<String,Object> updates = new HashMap<>();
        updates.put(Constans.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                        .addOnSuccessListener(unused ->{
                            preferenceManager.clear();
                            startActivity(new Intent(getApplicationContext(),SignInActivity.class));
                            finish();
                        })
                                .addOnFailureListener(e -> showToast("unable to sign out"));

    }
    @Override
    public void onConversionClicked(User user) {
        Log.d("tıkladık","");
        DocumentReference documentReference = db.collection(Constans.KEY_COLLECTION_CONVERSATIONS).document(covId);
        documentReference.update(
                    "receiverRead","true"
        );
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constans.KEY_USER,user);
        startActivity(intent);
        finish();


    }
}
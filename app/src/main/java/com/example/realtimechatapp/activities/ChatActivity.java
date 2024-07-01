package com.example.realtimechatapp.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.realtimechatapp.adapters.ChatAdapter;
import com.example.realtimechatapp.databinding.ActivityChatBinding;
import com.example.realtimechatapp.models.ChatMessage;
import com.example.realtimechatapp.models.User;
import com.example.realtimechatapp.utils.Constans;
import com.example.realtimechatapp.utils.ImageEncoder;
import com.example.realtimechatapp.utils.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ChatActivity extends BaseActivity {



    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessageList;

    private PreferenceManager preferenceManager;
    private ChatAdapter chatAdapter;

    private FirebaseFirestore db;
    private  String encodedImage = null;

    private String conversionId = null;
    private Boolean isReceiverOnline =false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiverDetails();
        init();
        listenMessages();

    }
    private void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessageList,getBitmapFromEncodedString(receiverUser.image),preferenceManager.getString(Constans.KEY_USER_ID)
        );
        binding.chatRecView.setAdapter(chatAdapter);
        db = FirebaseFirestore.getInstance();
    }
    private void sendImage(String encoded) {
        // Öncelikle resmin boyutunu kontrol edin
        if (encoded.length() > 1048487) {
            Toast.makeText(getApplicationContext(), "Resim boyutu çok büyük!", Toast.LENGTH_SHORT).show();
            return; // İşlemi durdur
        }

        // Resmin boyutu sınırlar içinde ise mesajı Firestore'a ekle
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constans.KEY_SENDER_ID, preferenceManager.getString(Constans.KEY_USER_ID));
        message.put(Constans.KEY_RECEIVER_ID, receiverUser.id);
        message.put(Constans.KEY_MESSAGE, encoded);
        message.put(Constans.KEY_TIMESTAMP, new Date());
        message.put(Constans.KEY_MESSAGE_TYPE, "image");

        db.collection(Constans.KEY_COLLECTION_CHAT)
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Success", "Mesaj gönderildi.");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Mesaj gönderilirken hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FAIL", "Mesaj gönderilirken hata: " + e.getMessage());
                });
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result ->{
                if(result.getResultCode() == RESULT_OK){
                    if(result.getData()!=null){
                        Uri imageUri = result.getData().getData();
                        try{
                            assert imageUri != null;
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            encodedImage = ImageEncoder.encodeImage(bitmap);
                            showSendImageDialog();
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );
    private void showSendImageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Resmi Gönder");
        builder.setMessage("Seçtiğiniz resmi göndermek istiyor musunuz?");
        builder.setPositiveButton("Gönder", (dialog, which) -> {
            // Resmi gönderme işlemleri burada yapılır
            sendImage(encodedImage);
        });
        builder.setNegativeButton("İptal", (dialog, which) -> {
            // İptal edildiğinde yapılacak işlemler buraya yazılır (Opsiyonel)
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void sendMessage(){

        HashMap<String,Object> message = new HashMap<>();
        message.put(Constans.KEY_SENDER_ID,preferenceManager.getString(Constans.KEY_USER_ID));
        message.put(Constans.KEY_RECEIVER_ID,receiverUser.id);
        message.put(Constans.KEY_MESSAGE,binding.inputMessage.getText().toString());
        message.put(Constans.KEY_TIMESTAMP,new Date());
        message.put(Constans.KEY_MESSAGE_TYPE,"text");
        db.collection(Constans.KEY_COLLECTION_CHAT).add(message);
        if(conversionId !=null){
            updateConversion(binding.inputMessage.getText().toString());
        }
        else{
            HashMap<String,Object> conversion = new HashMap<>();
            conversion.put(Constans.KEY_SENDER_ID,preferenceManager.getString(Constans.KEY_USER_ID));
            conversion.put(Constans.KEY_SENDER_NAME,preferenceManager.getString(Constans.KEY_NAME));
            conversion.put(Constans.KEY_SENDER_IMAGE,preferenceManager.getString(Constans.KEY_IMAGE));
            conversion.put(Constans.KEY_RECEIVER_ID,receiverUser.id);
            conversion.put(Constans.KEY_RECEIVER_NAME,receiverUser.name);
            conversion.put(Constans.KEY_RECEIVER_IMAGE,receiverUser.image);
            conversion.put(Constans.KEY_LAST_MESSAGE,binding.inputMessage.getText().toString());
            conversion.put(Constans.KEY_TIMESTAMP,new Date());
            addConversion(conversion);
        }
        binding.inputMessage.setText(null);

    }


    private void listenOnlineReceiver(){
        db.collection(Constans.KEY_COLLECTION_USERS).document(receiverUser.id)
                .addSnapshotListener(ChatActivity.this,((value, error) -> {
            if(error!=null){
                return;
            }
            if(value!=null){
                if(value.getLong(Constans.KEY_AVAILABILITY)!=null){
                    int online = Objects.requireNonNull(
                            value.getLong(Constans.KEY_AVAILABILITY)
                    ).intValue();
                    isReceiverOnline = online == 1;
                }
                receiverUser.fcmToken = value.getString(Constans.KEY_FCM_TOKEN);
            }
            if(isReceiverOnline){
                binding.textAvailability.setVisibility(View.VISIBLE);
            }
            else{
                binding.textAvailability.setVisibility(View.GONE);
            }

        }));
    }

    private Bitmap getBitmapFromEncodedString(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }
    private void loadReceiverDetails(){
        receiverUser = (User) getIntent().getSerializableExtra(Constans.KEY_USER);
        assert receiverUser != null;
        binding.textName.setText(receiverUser.name);

    }
    private void listenMessages(){
        db.collection(Constans.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constans.KEY_SENDER_ID,preferenceManager.getString(Constans.KEY_USER_ID))
                .whereEqualTo(Constans.KEY_RECEIVER_ID,receiverUser.id)
                .addSnapshotListener(eventListener);
        db.collection(Constans.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constans.KEY_SENDER_ID,receiverUser.id)
                .whereEqualTo(Constans.KEY_RECEIVER_ID,preferenceManager.getString(Constans.KEY_USER_ID))
                .addSnapshotListener(eventListener);

    }
    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error)-> {
      if(error !=null){
          return;
      }
      if(value!=null){
          int count = chatMessageList.size();
          for(DocumentChange documentChange : value.getDocumentChanges()){
              if(documentChange.getType() == DocumentChange.Type.ADDED){
                  ChatMessage chatMessage = new ChatMessage();
                  chatMessage.senderId = documentChange.getDocument().getString(Constans.KEY_SENDER_ID);
                  chatMessage.receiverId= documentChange.getDocument().getString(Constans.KEY_RECEIVER_ID);
                  chatMessage.message = documentChange.getDocument().getString(Constans.KEY_MESSAGE);
                  chatMessage.messageType = documentChange.getDocument().getString(Constans.KEY_MESSAGE_TYPE);
                  chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constans.KEY_TIMESTAMP));
                  chatMessage.dateObject = documentChange.getDocument().getDate(Constans.KEY_TIMESTAMP);
                  chatMessageList.add(chatMessage);
              }
          }
          chatMessageList.sort(Comparator.comparing(obj -> obj.dateObject));

            if(count == 0){
                chatAdapter.notifyDataSetChanged();
            }
            else{
                chatAdapter.notifyItemRangeInserted(chatMessageList.size(),chatMessageList.size());
                binding.chatRecView.smoothScrollToPosition(chatMessageList.size()-1);

            }
            binding.chatRecView.setVisibility(View.VISIBLE);
      }
      binding.progressBar.setVisibility(View.GONE);
      if(conversionId == null){
          checkForConversion();
      }
    };
    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),UsersActivity.class)));
        binding.layoutSend.setOnClickListener(v -> sendMessage());
        binding.layoutSendImage.setOnClickListener(v ->{
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);

        });

    }

    private void addConversion(HashMap<String ,Object> conversion){
        db.collection(Constans.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(v -> conversionId = v.getId());
    }
    private void updateConversion(String message){
        DocumentReference documentReference = db.collection(Constans.KEY_COLLECTION_CONVERSATIONS).document(conversionId);
        documentReference.update(
                Constans.KEY_LAST_MESSAGE,message,Constans.KEY_TIMESTAMP,new Date()
        );
    }
    private void checkForConversion(){
        if(chatMessageList.size() !=0){
            checkForConversionRemotely(preferenceManager.getString(Constans.KEY_USER_ID), receiverUser.id);
            checkForConversionRemotely(receiverUser.id,preferenceManager.getString(Constans.KEY_USER_ID));
        }

    }
    private void checkForConversionRemotely(String senderId,String receiverId){
        db.collection(Constans.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constans.KEY_SENDER_ID,senderId)
                .whereEqualTo(Constans.KEY_RECEIVER_ID,receiverId)
                .get().addOnCompleteListener(conversionOnCompleteListener);
    }
    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }
    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task ->{
        if(task.isSuccessful() && task.getResult()!=null && task.getResult().getDocuments().size() > 0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenOnlineReceiver();
    }


}
package com.example.realtimechatapp.adapters;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.realtimechatapp.R;
import com.example.realtimechatapp.databinding.ItemContainerReceivedMessageBinding;
import com.example.realtimechatapp.databinding.ItemContainerSentMessageBinding;
import com.example.realtimechatapp.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessageList;
    private final Bitmap receiverProfileImage;
    private final String senderId;

    public static final int VIEW_TYPE_SENT=1;
    public static final int VIEW_TYPE_RECEIVED=2;
    public ChatAdapter(List<ChatMessage> chatMessageList, Bitmap receiverProfileImage, String senderId) {
        this.chatMessageList = chatMessageList;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SENT){
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding
                            .inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }
        else {
            return new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding
                            .inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    if(getItemViewType(position) == VIEW_TYPE_SENT){
        ((SentMessageViewHolder) holder).setData(chatMessageList.get(position));

    }
    else{
        ((ReceivedMessageViewHolder) holder).setData(chatMessageList.get(position),receiverProfileImage);
    }
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessageList.get(position).senderId.equals((senderId))){
            return  VIEW_TYPE_SENT;
        }
        else{
            return  VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerSentMessageBinding binding;

        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding){
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }
        void setData(ChatMessage message) {
            if (message != null) {
                if ("text".equals(message.messageType)) {
                    binding.textMessage.setText(message.message);
                    binding.textMessage.setVisibility(View.VISIBLE);
                    binding.imageMessage.setVisibility(View.GONE);  // Hide image view if text message
                } else if ("image".equals(message.messageType)) {
                    if (message.message != null) {
                        binding.imageMessage.setImageBitmap(getBitmapFromEncodedString(message.message));
                        binding.imageMessage.setVisibility(View.VISIBLE);
                        binding.textMessage.setVisibility(View.GONE);  // Hide text view if image message
                    } else {
                        binding.imageMessage.setVisibility(View.GONE);  // Hide image view if image message is null
                    }
                } else {
                    binding.textMessage.setVisibility(View.GONE);
                    binding.imageMessage.setVisibility(View.GONE);
                }

                binding.textDateTime.setText(message.dateTime != null ? message.dateTime : "");  // Add null check for dateTime
            }
        }

        private Bitmap getBitmapFromEncodedString(String encodedImage) {
            if (encodedImage == null || encodedImage.isEmpty()) {
                return null;
            }
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
    }
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding){
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(ChatMessage message,Bitmap receiverProfileImage) {
            if (message != null) {
                if ("text".equals(message.messageType)) {
                    binding.textMessage.setText(message.message);
                    binding.textMessage.setVisibility(View.VISIBLE);
                    binding.imageMessage.setVisibility(View.GONE);  // Hide image view if text message
                } else if ("image".equals(message.messageType)) {
                    if (message.message != null) {
                        binding.imageMessage.setImageBitmap(getBitmapFromEncodedString(message.message));
                        binding.imageMessage.setVisibility(View.VISIBLE);
                        binding.textMessage.setVisibility(View.GONE);  // Hide text view if image message


                        // Resme tıklama olayı ekleme
                        binding.imageMessage.setOnClickListener(view -> {
                            downloadImage(message.message); // Resmi indirme metodu çağrılıyor
                        });
                    } else {
                        binding.imageMessage.setVisibility(View.GONE);  // Hide image view if image message is null
                    }
                } else {
                    binding.textMessage.setVisibility(View.GONE);
                    binding.imageMessage.setVisibility(View.GONE);
                }

                binding.textDateTime.setText(message.dateTime != null ? message.dateTime : "");  // Add null check for dateTime
                binding.imageProfile.setImageBitmap(receiverProfileImage);

            }
        }
        private void downloadImage(String encodedImage) {
            Bitmap bitmap = getBitmapFromEncodedString(encodedImage);
            if (bitmap != null) {
                showImageDialog(bitmap); // Resmi gösteren bir dialog göster
            }
        }

        private void showImageDialog(Bitmap bitmap) {
            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
            View dialogView = LayoutInflater.from(itemView.getContext()).inflate(R.layout.item_container_received_message, null);
            ImageView imageView = dialogView.findViewById(R.id.imageMessage);
            imageView.setImageBitmap(bitmap);

            builder.setView(dialogView);
            builder.setPositiveButton("İndir", (dialogInterface, i) -> {
                saveImageToDevice(bitmap); // Resmi cihaza kaydetme işlemini çağır
            });
            builder.setNegativeButton("Kapat", (dialogInterface, i) -> dialogInterface.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();
        }

        private void saveImageToDevice(Bitmap bitmap) {
            String savedImagePath = MediaStore.Images.Media.insertImage(itemView.getContext().getContentResolver(),
                    bitmap, "image_" + System.currentTimeMillis(), "Image from chat app");

            if (savedImagePath != null) {
                Toast.makeText(itemView.getContext(), "Resim başarıyla indirildi", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(itemView.getContext(), "Resim indirilirken hata oluştu", Toast.LENGTH_SHORT).show();
            }
        }
        private Bitmap getBitmapFromEncodedString(String encodedImage) {
            if (encodedImage == null || encodedImage.isEmpty()) {
                return null;
            }
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
    }
}

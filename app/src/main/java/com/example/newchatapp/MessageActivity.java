package com.example.newchatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.example.newchatapp.Adapter.MessageAdapter;
import com.example.newchatapp.model.Chat;
import com.example.newchatapp.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    CircleImageView profil_image;
    TextView username;
    ImageButton buttonSend;
    EditText textSend;

    FirebaseUser fuser;
    DatabaseReference reference;

    MessageAdapter messageAdapter;
    List<Chat> mChat;
    MessageAdapter.ViewHolder holder;
    RecyclerView recyclerView;

    String userid;

    //e2ee
    private byte encryptionKey[] = {9, 115, 51, 86, 105, 4, -31, -23, -68, 88, 17, 20, 3, -105, 119, -53};
    private Cipher cipher, decipher;
    private SecretKeySpec secretKeySpec;
    //e2ee

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.mainToolbar_message);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.recyclerMessage);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.scrollToPosition(0);

        profil_image = findViewById(R.id.main_image_message);
        username = findViewById(R.id.mainText_message);
        textSend = findViewById(R.id.textSendMessage);
        buttonSend = findViewById(R.id.buttonSendMessage);

        //e2ee
        try {
            cipher = Cipher.getInstance("AES");
            decipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        secretKeySpec = new SecretKeySpec(encryptionKey,"AES");
        //e2ee

        Intent intent = getIntent();
        userid = intent.getStringExtra("userid");

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String msg = textSend.getText().toString();

                if(!msg.isEmpty()){

                    int comparison = fuser.getUid().toUpperCase().compareTo(userid.toUpperCase());
                    String first, second;
                    if (comparison > 0){
                        first = fuser.getUid();
                        second = userid;
                    }
                    else{
                        first = userid;
                        second = fuser.getUid();
                    }
                    sendMessage(first, second, msg);
                }
                else{
                    Toast.makeText(MessageActivity.this, "You cant send empty message", Toast.LENGTH_SHORT).show();
                }
                textSend.setText("");
            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid); // eminiz.

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                username.setText(user.getUsername());

                if ("default".equals(user.getImage())) {

                    profil_image.setImageResource(R.drawable.ic_launcher_foreground);

                } else {
                    Glide.with(MessageActivity.this)
                            .load(user.getImage())
                            .into(profil_image);
                }

                int comparison = fuser.getUid().toUpperCase().compareTo(userid.toUpperCase());
                String first, second;
                if (comparison > 0){
                    first = fuser.getUid();
                    second = userid;
                }
                else{
                    first = userid;
                    second = fuser.getUid();
                }
                readMessages(first , second, user.getImage());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void sendMessage(String first, String second, String message){

        DatabaseReference reference =FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        message = AESEncryptionMethod(message);
        hashMap.put("sender",fuser.getUid());
        hashMap.put("receiver",userid);
        hashMap.put("message",message);
        //hashMap.put("read", false);

        reference.child("Chats").child(first+second).push().setValue(hashMap);

        DatabaseReference chatListRef = FirebaseDatabase.getInstance().getReference("ChatList")
                                        .child(fuser.getUid())
                                        .child(userid);

        chatListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatListRef.child("id").setValue(userid);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void readMessages(String first,String second, String imageurl){

        mChat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats").child(first+second);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               mChat.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){

                    Chat chat = dataSnapshot.getValue(Chat.class);
                    //e2ee
                    try {
                        chat.setMessage(AESDecryptionMethod(chat.getMessage()));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                  //e2ee
                  /*  if (chat.getReceiver().equals(fuser.getUid())){
                        chat.setRead(true);
                        dataSnapshot.child("read").getRef().setValue(true);
                    }*/

                    mChat.add(chat);

                    messageAdapter = new MessageAdapter(MessageActivity.this, mChat, imageurl);
                    recyclerView.setAdapter(messageAdapter);
                    recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String AESEncryptionMethod(String string){

        byte[] stringByte = string.getBytes();
        byte[] encryptedByte = new byte[stringByte.length];

        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            encryptedByte = cipher.doFinal(stringByte);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        String returnString = null;
        returnString = new String(encryptedByte, StandardCharsets.ISO_8859_1);
        return returnString;
    }

    public  String AESDecryptionMethod(String string) throws UnsupportedEncodingException {

        byte[] EncryptedByte = string.getBytes(StandardCharsets.ISO_8859_1);
        String decryptedString = string;
        byte[] decryption;

        try {
            decipher.init(cipher.DECRYPT_MODE, secretKeySpec);
            decryption = decipher.doFinal(EncryptedByte);
            decryptedString = new String(decryption);
        } catch (InvalidKeyException e) {
            e.printStackTrace();

        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return  decryptedString;
    }
}
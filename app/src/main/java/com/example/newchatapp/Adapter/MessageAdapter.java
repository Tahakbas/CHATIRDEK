package com.example.newchatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.newchatapp.MessageActivity;
import com.example.newchatapp.R;
import com.example.newchatapp.model.Chat;
import com.example.newchatapp.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<Chat> mChat;
    private String imageUrl;
    FirebaseUser firebaseUser;

    public MessageAdapter(Context mContext, List<Chat> mChat,String imageUrl){
        this.mChat = mChat;
        this.imageUrl = imageUrl;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right,parent,false);
            return new ViewHolder(view);
        }
        else{
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left,parent,false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {

        Chat chat = mChat.get(position);
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" + position + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        holder.show_message.setText(chat.getMessage());

       /* if (mChat.get(position).isRead() && (holder.getItemViewType() == 1)){
            holder.read_info.setVisibility(View.VISIBLE);
        }
        else{
            holder.read_info.setVisibility(View.GONE);
        }*/

        if("default".equals(imageUrl)){
            holder.profil_image.setImageResource(R.drawable.ic_launcher_foreground);
        }
        else{
            Glide.with(mContext)
                    .load(imageUrl)
                    .into(holder.profil_image);
        }

    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView show_message;
        public ImageView profil_image;
        public TextView read_info;

        public ViewHolder(View itemView){
            super(itemView);
            show_message = itemView.findViewById(R.id.show_message);
            profil_image = itemView.findViewById(R.id.profil_image);
          //  read_info = itemView.findViewById(R.id.read_info);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mChat.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else{
            return  MSG_TYPE_LEFT;
        }
    }
}

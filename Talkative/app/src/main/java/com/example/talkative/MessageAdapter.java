package com.example.talkative;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private ArrayList<Message> mMessageData;
    private Context mContext;
    private int lastPosition = -1;

    public MessageAdapter(ArrayList<Message> mMessageData, Context mContext) {
        this.mMessageData = mMessageData;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MessageAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MessageAdapter.MessageViewHolder(LayoutInflater.from(mContext).inflate(R.layout.message_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.MessageViewHolder holder, int position) {
        Message current = mMessageData.get(position);
        holder.bindTo(current);
        if (holder.getAbsoluteAdapterPosition() > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim2);
            holder.itemView.startAnimation(animation);
            lastPosition = holder.getAbsoluteAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return mMessageData.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView mUsernameTV;
        TextView mMessageTV;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            mUsernameTV = itemView.findViewById(R.id.usernameTV);
            mMessageTV = itemView.findViewById(R.id.messageTV);
        }

        public void bindTo(Message current) {
            String name = current.getSender() == 1 ? current.getName1() : current.getName2();
            mUsernameTV.setText(name);
            mMessageTV.setText(current.getContent());
        }
    }
}

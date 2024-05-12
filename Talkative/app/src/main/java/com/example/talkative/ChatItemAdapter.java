package com.example.talkative;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ChatItemAdapter extends RecyclerView.Adapter<ChatItemAdapter.ChatItemViewHolder> {
    private static String LOG_TAG = ChatItemAdapter.class.getName();
    private ArrayList<ChatItem> mChatData;
    private Context mContext;
    private int lastPosition = -1;

    ChatItemAdapter(Context context, ArrayList<ChatItem> data) {
        this.mChatData = data;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ChatItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ChatItemViewHolder holder = new ChatItemViewHolder(LayoutInflater.from(mContext).inflate(R.layout.chat_list_item, parent, false));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatItem chat = mChatData.get(holder.getBindingAdapterPosition());
                Intent intent = new Intent(v.getContext(), ChatActivity.class);
                intent.putExtra("email1", chat.getEmail1());
                intent.putExtra("email2", chat.getEmail2());
                intent.putExtra("name", chat.getName1().equals(FirebaseAuth.getInstance().getCurrentUser().getDisplayName()) ? chat.getName2() : chat.getName1());
                v.getContext().startActivity(intent);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ChatItemAdapter.ChatItemViewHolder holder, int position) {
        ChatItem current = mChatData.get(position);

        holder.bindTo(current);

        if (holder.getAbsoluteAdapterPosition() > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim1);
            holder.itemView.startAnimation(animation);
            lastPosition = holder.getAbsoluteAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return mChatData.size();
    }

    class ChatItemViewHolder extends RecyclerView.ViewHolder {
        private TextView mUsernameTV;

        public ChatItemViewHolder(@NonNull View itemView) {
            super(itemView);

            mUsernameTV = itemView.findViewById(R.id.usernameTV);
        }

        public void bindTo(ChatItem current) {
            String name = current.getEmail1().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail()) ? current.getName2() : current.getName1();
            mUsernameTV.setText(name);
        }
    }
}
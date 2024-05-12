package com.example.talkative;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.auth.User;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {
    private static final String LOG_TAG = RequestAdapter.class.getName();

    private ArrayList<Request> mRequestData;
    private Context mContext;
    private int lastPosition = -1;

    RequestAdapter(Context context, ArrayList<Request> data) {
        this.mRequestData = data;
        this.mContext = context;
    }

    @NonNull
    @Override
    public RequestAdapter.RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RequestViewHolder holder = new RequestAdapter.RequestViewHolder(LayoutInflater.from(mContext).inflate(R.layout.request_list_item, parent, false));
        holder.mAcceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String email1 = user
                        .getEmail()
                        .compareTo(mRequestData.get(holder.getBindingAdapterPosition()).getSenderEmail()) < 0 ?
                        user.getEmail() : mRequestData.get(holder.getBindingAdapterPosition()).getSenderEmail();
                String email2 = user
                        .getEmail()
                        .equals(email1) ?
                        mRequestData.get(holder.getBindingAdapterPosition()).getSenderEmail() : user.getEmail();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                ChatItem chatItem = new ChatItem(
                        email1,
                        email1.equals(user.getEmail()) ? user.getDisplayName() : mRequestData.get(holder.getBindingAdapterPosition()).getSenderName(),
                        email2,
                        email2.equals(user.getEmail()) ? user.getDisplayName() : mRequestData.get(holder.getBindingAdapterPosition()).getSenderName(),
                        Calendar.getInstance().getTime().toString()
                    );
                db.collection("chats").add(chatItem);
                HomeActivity.accepts.add(chatItem);
                db.collection("requests").whereEqualTo("senderEmail", mRequestData.get(holder.getBindingAdapterPosition()).getSenderEmail())
                        .whereEqualTo("receiverEmail", user.getEmail())
                        .get().addOnSuccessListener(e -> {
                            for (QueryDocumentSnapshot d : e) {
                                db.document("requests/" + d.getId()).delete();
                                mRequestData.remove(holder.getBindingAdapterPosition());
                                notifyItemRemoved(holder.getBindingAdapterPosition());
                            }
                        });
                Toast.makeText(v.getContext(), "Sikeres hozzáadás", Toast.LENGTH_LONG).show();
            }
        });
        holder.mRejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore.getInstance().collection("requests").whereEqualTo("senderEmail", mRequestData.get(holder.getBindingAdapterPosition()).getSenderEmail())
                        .whereEqualTo("receiverEmail", FirebaseAuth.getInstance().getCurrentUser().getEmail())
                        .get().addOnSuccessListener(e -> {
                            for (QueryDocumentSnapshot d : e) {
                                FirebaseFirestore.getInstance().document("requests/" + d.getId()).delete();
                                mRequestData.remove(holder.getBindingAdapterPosition());
                                notifyItemRemoved(holder.getBindingAdapterPosition());
                            }
                        });
                Toast.makeText(v.getContext(), "Sikeres törlés", Toast.LENGTH_LONG).show();
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RequestAdapter.RequestViewHolder holder, int position) {
        Request current = mRequestData.get(position);
        holder.bindTo(current);
        if (holder.getAbsoluteAdapterPosition() > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim2);
            holder.itemView.startAnimation(animation);
            lastPosition = holder.getAbsoluteAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return mRequestData.size();
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {

        private TextView senderTV;
        private Button mAcceptButton;
        private Button mRejectButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            senderTV = itemView.findViewById(R.id.usernameTV);
            mAcceptButton = itemView.findViewById(R.id.acceptButton);
            mRejectButton = itemView.findViewById(R.id.deleteButton);
        }

        public void bindTo(Request current) {
            senderTV.setText(current.getSenderEmail());
        }
    }
}

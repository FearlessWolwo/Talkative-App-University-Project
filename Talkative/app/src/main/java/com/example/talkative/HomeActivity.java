package com.example.talkative;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Comparator;

public class HomeActivity extends AppCompatActivity {
    private static final String LOG_TAG = HomeActivity.class.getName();
    private static final int SECRET_KEY = 374191;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private CollectionReference mItems;
    private RecyclerView mRecyclerView;
    private ArrayList<ChatItem> mItemList;
    private ChatItemAdapter mAdapter;
    private FrameLayout circle;
    private TextView contextTextView;
    private int gridNumber = 1;
    public static ChatItem lastOpen = null;
    public static ArrayList<ChatItem> accepts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
        }

        db = FirebaseFirestore.getInstance();
        mItems = db.collection("chats");

        updateAlertIcon();

        mRecyclerView = findViewById(R.id.chatRV);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, gridNumber));
        mItemList = new ArrayList<>();
        mAdapter = new ChatItemAdapter(this, mItemList);
        mRecyclerView.setAdapter(mAdapter);
        initializeData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAlertIcon();
        if (lastOpen != null) {
            int i = 0;
            for (; i < mItemList.size(); i++) {
                ChatItem chatItem = mItemList.get(i);
                if (lastOpen.getEmail1().equals(chatItem.getEmail1()) && lastOpen.getEmail2().equals(chatItem.getEmail2())) {
                    break;
                }
            }
            mItemList.remove(i);
            mAdapter.notifyItemRemoved(i);
            lastOpen = null;
        }
        if (!accepts.isEmpty()) {
            int i = mItemList.size();
            mItemList.addAll(accepts);
            mAdapter.notifyItemRangeInserted(i, accepts.size());
            accepts.clear();
        }
    }

    private void initializeData() {
        mItemList.clear();

        mItems.whereEqualTo("email1", user.getEmail()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                ChatItem item = document.toObject(ChatItem.class);
                mItemList.add(item);
            }
            mItems.whereEqualTo("email2", user.getEmail()).get().addOnSuccessListener(queryDocumentSnapshots2 -> {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots2) {
                    ChatItem item = document.toObject(ChatItem.class);
                    mItemList.add(item);
                }
                mItemList.sort(Comparator.comparing(ChatItem::getLastOpened));
                mAdapter.notifyItemRangeInserted(0, mItemList.size());
            });
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.requests) {
            startActivity(new Intent(this, RequestsActivity.class));
            return true;
        }
        else if (id == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem alertMenuItem = menu.findItem(R.id.requests);
        alertMenuItem.setActionView(R.layout.friend_menu_item);
        FrameLayout rootView = (FrameLayout) alertMenuItem.getActionView();

        circle = rootView.findViewById(R.id.view_alert_circle);
        contextTextView = rootView.findViewById(R.id.view_alert_count_textview);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(alertMenuItem);
            }
        });

        return super.onPrepareOptionsMenu(menu);
    }

    public void updateAlertIcon() {
        db.collection("requests").whereEqualTo("receiverEmail", user.getEmail()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                contextTextView.setText(String.valueOf(queryDocumentSnapshots.size()));
                circle.setVisibility(View.VISIBLE);
            }
            else {
                circle.setVisibility(View.GONE);
            }
        });
    }
}
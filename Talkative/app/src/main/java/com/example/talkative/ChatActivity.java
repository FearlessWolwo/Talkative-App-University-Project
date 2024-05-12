package com.example.talkative;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;

public class ChatActivity extends AppCompatActivity {
    private static final String LOG_TAG = ChatActivity.class.getName();
    private static final int SECRET_KEY = 374191;
    private EditText messageET;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private CollectionReference mItems;
    private RecyclerView mRecyclerView;
    private ArrayList<Message> mItemList;
    private MessageAdapter mAdapter;
    private int gridNumber= 1;
    private String email1;
    private String email2;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            finish();
        }

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            email1 = bundle.getString("email1");
            email2 = bundle.getString("email2");
            name = bundle.getString("name");
        }

        if (bundle == null || email1 == null | email2 == null | name == null) {
            Log.d(LOG_TAG, "email1: " + email1 + " email2: " + email2 + " name: " + name);
            finish();
        }

        getSupportActionBar().setTitle("Chat: " + name);

        messageET = findViewById(R.id.messageET);

        db = FirebaseFirestore.getInstance();
        mItems = db.collection("messages");

        mRecyclerView = findViewById(R.id.messagesRV);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, gridNumber));
        mItemList = new ArrayList<>();
        mAdapter = new MessageAdapter(mItemList, this);
        mRecyclerView.setAdapter(mAdapter);
        initializeData();
    }

    private void initializeData() {
        mItemList.clear();
        Log.d(LOG_TAG, "email1: " + email1 + " email2: " + email2);
        mItems.whereEqualTo("email1", email1).whereEqualTo("email2", email2).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Message message = doc.toObject(Message.class);
                    mItemList.add(message);
                }
                mItemList.sort(Comparator.comparing(Message::getDate));
                mAdapter.notifyItemRangeChanged(0, mItemList.size());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.back) {
            finish();
            return true;
        }
        else if (id == R.id.delete) {
            new AlertDialog.Builder(this)
                    .setMessage("Biztos törlöd a barátaid közül? Az üzenetek is törlődnek!")
                    .setPositiveButton("Törlés", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            db.collection("chats").whereEqualTo("email1", email1).whereEqualTo("email2", email2)
                                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                                db.document("chats/" + doc.getId()).delete();
                                            }
                                        }
                                    });
                            db.collection("messages").whereEqualTo("email1", email1).whereEqualTo("email2", email2)
                                            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                                db.document("messages/" + doc.getId()).delete();
                                            }
                                        }
                                    });
                            Toast.makeText(ChatActivity.this, "Sikeres törlés", Toast.LENGTH_SHORT).show();
                            HomeActivity.lastOpen = new ChatItem(email1, "", email2, "", "");
                            finish();
                        }})
                    .setNegativeButton("Mégse", null).show();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    public void sendMessage(View view) {
        String content = messageET.getText().toString();
        if (content.isEmpty()) {
           return;
        }
        boolean userIs1 = email1.equals(user.getEmail());
        Message message = new Message(
                content,
                Calendar.getInstance().getTime().toString(),
                email1,
                userIs1 ? user.getDisplayName() : name,
                email2,
                userIs1 ? name : user.getDisplayName(),
                userIs1 ? 1 : 2);
        mItems.add(message);
        mItemList.add(message);
        mAdapter.notifyItemInserted(mItemList.size() - 1);
        messageET.setText("");
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
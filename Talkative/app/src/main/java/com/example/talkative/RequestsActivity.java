package com.example.talkative;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class RequestsActivity extends AppCompatActivity {
    private static final String LOG_TAG = RequestsActivity.class.getName();
    private static final int SECRET_KEY = 374191;
    private FirebaseUser user;
    private RecyclerView mRecyclerView;
    private ArrayList<Request> mItemList;
    private RequestAdapter mAdapter;
    private int gridNumber = 1;
    private EditText friendET;

    private FirebaseFirestore mFirestore;
    private CollectionReference mItems;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            finish();
        }

        friendET = findViewById(R.id.friendET);

        mRecyclerView = findViewById(R.id.requestsRV);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, gridNumber));
        mItemList = new ArrayList<>();
        mAdapter = new RequestAdapter(this, mItemList);
        mRecyclerView.setAdapter(mAdapter);

        mFirestore = FirebaseFirestore.getInstance();
        mItems = mFirestore.collection("requests");

        queryData();
    }

    private void queryData() {
        mItemList.clear();

        mItems.whereEqualTo("receiverEmail", user.getEmail()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            int counter = 0;
           for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
               Request item = document.toObject(Request.class);
               mItemList.add(item);
               mAdapter.notifyItemInserted(counter++);
           }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.request_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.back) {
            finish();
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

    public void sendRequest(View view) {
        String email = friendET.getText().toString();
        if (email.trim().isEmpty()) {
            Toast.makeText(this, "A mező üres", Toast.LENGTH_LONG).show();
            return;
        }
        if (email.trim().equals(user.getEmail())) {
            Toast.makeText(this, "Magadat nem jelölheted be", Toast.LENGTH_LONG).show();
            return;
        }

        List<Object> list = new ArrayList<>();
        Task<QuerySnapshot> t1 = mItems.whereEqualTo("receiverEmail", email).whereEqualTo("senderEmail", user.getEmail()).get();
        Task<QuerySnapshot> t2 = mItems.whereEqualTo("senderEmail", email).whereEqualTo("receiverEmail", user.getEmail()).get();
        String email1 = user.getEmail().compareTo(email) < 0 ? user.getEmail() : email;
        String email2 = user.getEmail().equals(email1) ? email : user.getEmail();
        Task<QuerySnapshot> t3 = mFirestore.collection("chats").whereEqualTo("email1", email1).whereEqualTo("email2", email2).get();
        t1.addOnSuccessListener(e -> {
            for (QueryDocumentSnapshot q : e) {
                list.add(q);
            }
            if (!list.isEmpty()) {
                Toast.makeText(this, "Már küldötél jelölést ennek a felhasználónak", Toast.LENGTH_LONG).show();
                return;
            }
            t2.addOnSuccessListener(f -> {
                for (QueryDocumentSnapshot q : f) {
                    list.add(q);
                }
                if (!list.isEmpty()) {
                    Toast.makeText(this, "Már küldött jelölést ez a felhasználó", Toast.LENGTH_LONG).show();
                    return;
                }
                t3.addOnSuccessListener(g -> {
                    for (QueryDocumentSnapshot q : g) {
                        list.add(q);
                    }
                    if (list.isEmpty()) {
                        mItems.add(new Request(user.getEmail(), user.getDisplayName(), email, null));
                        Toast.makeText(this, "Meghívó elküldve", Toast.LENGTH_LONG).show();
                        friendET.setText("");
                    }
                    else {
                        Toast.makeText(this, "Már barátok vagytok", Toast.LENGTH_LONG).show();
                    }
                });
            });
        });
    }
}

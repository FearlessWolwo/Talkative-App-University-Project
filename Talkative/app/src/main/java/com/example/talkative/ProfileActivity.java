package com.example.talkative;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ProfileActivity extends AppCompatActivity {
    private static final String LOG_TAG = ProfileActivity.class.getName();
    private static final int SECRET_KEY = 374191;

    private FirebaseUser user;
    EditText usernameET;
    EditText passwordET;
    EditText newPasswordET;
    EditText newPasswordAgainET;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            finish();
        }

        usernameET = findViewById(R.id.usernameET);
        passwordET = findViewById(R.id.passwordET);
        newPasswordET = findViewById(R.id.newPasswordET);
        newPasswordAgainET = findViewById(R.id.newPasswordAgainET);

        usernameET.setText(user.getDisplayName());
    }

    public void cancel(View view) {
        finish();
    }

    public void update(View view) {
        String username = usernameET.getText().toString();
        String password = passwordET.getText().toString();
        String newPassword = newPasswordET.getText().toString();
        String newPasswordAgain = newPasswordAgainET.getText().toString();

        if (!newPassword.isEmpty() && (newPassword.length() < 8 || !newPassword.equals(newPasswordAgain))) {
            Log.e(LOG_TAG, "A jelszó túl rövid vagy nem egyezik az ellenőrző jelszóval.");
            return;
        }

        if (username.length() < 5) {
            Log.e(LOG_TAG, "A név legalább 5 karakter hosszú kell hogy legyen.");
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!username.equals(user.getDisplayName())) {
                    user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(username).build());
                }
                if (!newPassword.isEmpty()) {
                    user.updatePassword(newPassword);
                }
            }
        });

        finish();
    }
}

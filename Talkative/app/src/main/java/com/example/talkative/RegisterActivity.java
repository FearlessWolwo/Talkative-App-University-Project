package com.example.talkative;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {
    private static final String LOG_TAG = RegisterActivity.class.getName();
    private static final int SECRET_KEY = 374191;
    private static final String PREF_STR = "talkative_app_preferences";

    EditText usernameET;
    EditText passwordET;
    EditText repeatPasswordET;
    EditText emailET;
    private SharedPreferences preferences;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Bundle bundle = getIntent().getExtras();
        int secret_key = 0;
        if (bundle != null) {
            secret_key = bundle.getInt("SECRET_KEY");
        }

        if (secret_key != SECRET_KEY) {
            finish();
        }

        usernameET = findViewById(R.id.usernameET);
        passwordET = findViewById(R.id.passwordET);
        repeatPasswordET = findViewById(R.id.repeatPasswordET);
        emailET = findViewById(R.id.emailET);

        preferences = getSharedPreferences(PREF_STR, MODE_PRIVATE);
        String username = preferences.getString("username", null);
        String password = preferences.getString("password", null);

        usernameET.setText(username);
        passwordET.setText(password);

        mAuth = FirebaseAuth.getInstance();
    }

    public void register(View view) {
        String username = usernameET.getText().toString();
        String password = passwordET.getText().toString();
        String repeatPassword = repeatPasswordET.getText().toString();
        String email = emailET.getText().toString();

        if (username.length() < 5) {
            Log.e(LOG_TAG, "A felhasználónévnek legalább 5 karakter hosszúnak kell lennie!");
        }
        if (password.length() < 8) {
            Log.e(LOG_TAG, "A jelszónak legalább 8 karakter hosszúnak kell lennie!");
        }
        if (!password.equals(repeatPassword)) {
            Log.e(LOG_TAG, "A két jelszó nem egyezik!");
        }
        if (email.isEmpty()) {
            Log.e(LOG_TAG, "E-mail cím megadása közelező!");
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseAuth.getInstance().getCurrentUser().updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(username).build());
                    startChatting();
                }
                else {
                    Toast.makeText(RegisterActivity.this, "Sikertelen regisztráció\n" + task.getException().getMessage() ,Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void cancel(View view) {
        finish();
    }

    private void startChatting() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
}
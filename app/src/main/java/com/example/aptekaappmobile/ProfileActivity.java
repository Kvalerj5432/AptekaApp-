package com.example.aptekaappmobile;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {
    private TextView textLogin, textPhone;
    private SupabaseClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        textLogin = findViewById(R.id.text_login);
        textPhone = findViewById(R.id.text_phone);

        client = new SupabaseClient(this);

        client.getUser(client.getCurrentUserId(), new SupabaseClient.Callback<User>() {
            @Override
            public void onSuccess(User user) {
                textLogin.setText("Login: " + user.getLogin());
                textPhone.setText("Phone: " + user.getPhone());
            }

            @Override
            public void onFailure(Exception error) {
                Toast.makeText(ProfileActivity.this, "Failed to load profile: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

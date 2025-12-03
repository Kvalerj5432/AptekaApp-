package com.example.aptekaappmobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    private EditText editLogin, editPassword, editPhone;
    private Button btnRegister;
    private SupabaseClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editLogin = findViewById(R.id.edit_login);
        editPassword = findViewById(R.id.edit_password);
        editPhone = findViewById(R.id.edit_phone);
        btnRegister = findViewById(R.id.btn_register);

        client = new SupabaseClient(this);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String login = editLogin.getText().toString();
                String password = editPassword.getText().toString();
                String phone = editPhone.getText().toString();
                client.signUp(login, password, phone, new SupabaseClient.Callback<User>() {
                    @Override
                    public void onSuccess(User result) {
                        Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finish();
                    }

                    @Override
                    public void onFailure(Exception error) {
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
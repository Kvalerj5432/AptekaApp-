package com.example.aptekaappmobile;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editLogin;
    private TextInputEditText editPassword;
    private MaterialButton btnLogin;
    private TextView textRegister;

    private SupabaseClient client;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        client = new SupabaseClient(this);

        if (client.getCurrentUserId() != -1) {
            goToMain();
            return;
        }

        editLogin = findViewById(R.id.edit_login);
        editPassword = findViewById(R.id.edit_password);
        btnLogin = findViewById(R.id.btn_login);
        textRegister = findViewById(R.id.text_register);

        btnLogin.setOnClickListener(v -> attemptLogin());

        textRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
    }

    private void attemptLogin() {
        String login = getTextFrom(editLogin);
        String password = getTextFrom(editPassword);

        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Вход...");

        client.signIn(login, password, new SupabaseClient.Callback<User>() {
            @Override
            public void onSuccess(User user) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Добро пожаловать, " + user.getLogin() + "!", Toast.LENGTH_SHORT).show();
                    goToMain();
                });
            }

            @Override
            public void onFailure(Exception error) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Ошибка входа: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Войти");
                });
            }
        });
    }

    private void goToMain() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private String getTextFrom(TextInputEditText editText) {
        return editText != null ? editText.getText().toString().trim() : "";
    }
}
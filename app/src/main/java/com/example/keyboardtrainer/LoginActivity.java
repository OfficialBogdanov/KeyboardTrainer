package com.example.keyboardtrainer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private EditText email, password, username, passwordConfirm;
    private Button btnAction;
    private TextInputLayout usernameContainer, passwordConfirmContainer;
    private FirebaseAuth auth;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        passwordConfirm = findViewById(R.id.passwordConfirm);
        btnAction = findViewById(R.id.btn_action);
        Button btnLogin = findViewById(R.id.btn_login);
        Button btnSignUp = findViewById(R.id.btn_signup);

        usernameContainer = findViewById(R.id.username_container);
        passwordConfirmContainer = findViewById(R.id.password_confirm_container);

        btnLogin.setOnClickListener(v -> switchToLoginMode());
        btnSignUp.setOnClickListener(v -> switchToRegisterMode());

        btnAction.setOnClickListener(v -> {
            if (isLoginMode) {
                loginUser();
            } else {
                registerUser();
            }
        });

        switchToLoginMode();
    }

    private void switchToLoginMode() {
        isLoginMode = true;
        usernameContainer.setVisibility(View.GONE);
        passwordConfirmContainer.setVisibility(View.GONE);
        btnAction.setText("Войти");
    }

    private void switchToRegisterMode() {
        isLoginMode = false;
        usernameContainer.setVisibility(View.VISIBLE);
        passwordConfirmContainer.setVisibility(View.VISIBLE);
        btnAction.setText("Зарегистрироваться");
    }

    private void loginUser() {
        String emailText = email.getText().toString().trim();
        String passwordText = password.getText().toString().trim();

        if (emailText.isEmpty() || passwordText.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Вход успешен!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        String errorMessage = "Ошибка входа";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser() {
        String emailText = email.getText().toString().trim();
        String usernameText = username.getText().toString().trim();
        String passwordText = password.getText().toString().trim();
        String confirmPasswordText = passwordConfirm.getText().toString().trim();

        if (emailText.isEmpty() || usernameText.isEmpty() || passwordText.isEmpty() || confirmPasswordText.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (passwordText.length() < 6 || passwordText.length() > 12) {
            Toast.makeText(this, "Пароль должен быть от 6 до 12 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!passwordText.equals(confirmPasswordText)) {
            Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!usernameText.matches("^[a-zA-Z0-9_]+$")) {
            Toast.makeText(this, "Никнейм может содержать только буквы, цифры и подчеркивание", Toast.LENGTH_SHORT).show();
            return;
        }

        if (usernameText.length() < 3 || usernameText.length() > 20) {
            Toast.makeText(this, "Никнейм должен быть от 3 до 20 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance().collection("usernames")
                .document(usernameText.toLowerCase())
                .get()
                .addOnCompleteListener(usernameTask -> {
                    if (usernameTask.isSuccessful()) {
                        if (usernameTask.getResult().exists()) {
                            Toast.makeText(this, "Этот никнейм уже занят", Toast.LENGTH_SHORT).show();
                        } else {
                            auth.createUserWithEmailAndPassword(emailText, passwordText)
                                    .addOnCompleteListener(authTask -> {
                                        if (authTask.isSuccessful()) {
                                            FirebaseUser user = auth.getCurrentUser();
                                            if (user != null) {
                                                Map<String, Object> userData = new HashMap<>();
                                                userData.put("username", usernameText);
                                                userData.put("email", emailText);
                                                userData.put("createdAt", Timestamp.now());

                                                FirebaseFirestore.getInstance().collection("users")
                                                        .document(user.getUid())
                                                        .set(userData)
                                                        .addOnSuccessListener(aVoid -> {
                                                            Map<String, Object> usernameReservation = new HashMap<>();
                                                            usernameReservation.put("userId", user.getUid());

                                                            FirebaseFirestore.getInstance().collection("usernames")
                                                                    .document(usernameText.toLowerCase())
                                                                    .set(usernameReservation)
                                                                    .addOnSuccessListener(aVoid1 -> {
                                                                        Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();
                                                                        switchToLoginMode();
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        user.delete();
                                                                        Toast.makeText(this, "Ошибка резервирования никнейма", Toast.LENGTH_SHORT).show();
                                                                    });
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            user.delete();
                                                            Toast.makeText(this, "Ошибка сохранения данных пользователя", Toast.LENGTH_SHORT).show();
                                                        });
                                            }
                                        } else {
                                            handleRegistrationError(authTask.getException());
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Ошибка проверки никнейма", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleRegistrationError(Exception exception) {
        String errorMessage = "Ошибка регистрации";
        if (exception instanceof FirebaseAuthWeakPasswordException) {
            errorMessage = "Слабый пароль. Пароль должен быть не менее 6 символов.";
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = "Некорректный email.";
        } else if (exception instanceof FirebaseAuthUserCollisionException) {
            errorMessage = "Пользователь с таким email уже существует.";
        } else if (exception != null) {
            errorMessage = exception.getMessage();
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}
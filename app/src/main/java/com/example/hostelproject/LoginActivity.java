package com.example.hostelproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText loginInput, passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI components
        loginInput = findViewById(R.id.login_input);
        passwordInput = findViewById(R.id.password_input);
        Button submitButton = findViewById(R.id.register_button);
        TextView registerText = findViewById(R.id.register_link);

        // Set up the submit button click listener
        submitButton.setOnClickListener(v -> validateAndLogin());

        // Set up the registration link click listener
        registerText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });
    }

    private void validateAndLogin() {
        // Fetch input data
        String login = loginInput.getText().toString();
        String password = passwordInput.getText().toString();

        // Basic validation
        if (TextUtils.isEmpty(login)) {
            loginInput.setError("Registration Number or Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return;
        }

        // Execute task to authenticate the user
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new AuthenticateUserTask(login, password, this));
    }

    private static class AuthenticateUserTask implements Runnable {
        private final String login;
        private final String password;
        private final LoginActivity activity;

        AuthenticateUserTask(String login, String password, LoginActivity activity) {
            this.login = login;
            this.password = password;
            this.activity = activity;
        }

        @Override
        public void run() {
            try (MongoClient mongoClient = MongoClients.create("mongodb://10.0.2.2:27017")) {
                MongoDatabase database = mongoClient.getDatabase("HostelManagement");
                MongoCollection<Document> usersCollection = database.getCollection("Users");
                Document query = new Document("$or", Arrays.asList(
                        new Document("registrationNumber", login),
                        new Document("email", login)
                )).append("password", password);
                Document user = usersCollection.find(query).first();
                if (user != null) {
                    activity.runOnUiThread(() -> {
                        // Save user details in SharedPreferences
                        SharedPreferences sharedPreferences = activity.getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("name", user.getString("name"));
                        editor.putString("registrationNumber", user.getString("registrationNumber"));
                        editor.putString("email", user.getString("email"));
                        editor.putString("contact", user.getString("contact"));
                        editor.putString("address", user.getString("address"));
                        editor.putBoolean("isLoggedIn", true); // Set the isLoggedIn flag to true
                        editor.apply();

                        Toast.makeText(activity, "Login Successful!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(activity, MainActivity.class);
                        activity.startActivity(intent);
                        activity.finish();
                    });
                } else {
                    activity.runOnUiThread(() -> Toast.makeText(activity, "Invalid Credentials!", Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                activity.runOnUiThread(() -> Toast.makeText(activity, "Login Failed!", Toast.LENGTH_LONG).show());
            }
        }
    }
}
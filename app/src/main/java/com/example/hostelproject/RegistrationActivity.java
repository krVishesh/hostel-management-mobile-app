package com.example.hostelproject;

import android.content.Intent;
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

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RegistrationActivity extends AppCompatActivity {

    private EditText nameInput, passwordInput, registrationNumberInput, emailInput, contactInput, addressInput, pinInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize UI components
        nameInput = findViewById(R.id.name_input);
        passwordInput = findViewById(R.id.password_input);
        registrationNumberInput = findViewById(R.id.registration_number_input);
        emailInput = findViewById(R.id.email_input);
        contactInput = findViewById(R.id.contact_input);
        addressInput = findViewById(R.id.address_input);
        pinInput = findViewById(R.id.pin_input);
        Button registerButton = findViewById(R.id.register_button);
        TextView loginText = findViewById(R.id.login_link);

        // Set up the submit button click listener
        registerButton.setOnClickListener(v -> validateAndSubmitForm());

        loginText.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void validateAndSubmitForm() {
        // Fetch input data
        String name = nameInput.getText().toString();
        String password = passwordInput.getText().toString();
        String registrationNumber = registrationNumberInput.getText().toString();
        String email = emailInput.getText().toString();
        String contact = contactInput.getText().toString();
        String address = addressInput.getText().toString();
        String pin = pinInput.getText().toString();

        // Basic validation
        if (TextUtils.isEmpty(name)) {
            nameInput.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return;
        }
        if (TextUtils.isEmpty(registrationNumber)) {
            registrationNumberInput.setError("Registration Number is required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(contact)) {
            contactInput.setError("Contact is required");
            return;
        }
        if (TextUtils.isEmpty(pin)) {
            pinInput.setError("PIN is required");
            return;
        }

        // Create a document to insert into the collection
        Document user = new Document("name", name)
                .append("password", password)
                .append("registrationNumber", registrationNumber)
                .append("email", email)
                .append("contact", contact)
                .append("address", address)
                .append("pin", pin);

        // Execute task to insert the document
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new InsertUserTask(user, this));
    }

    private static class InsertUserTask implements Runnable {
        private final Document user;
        private final RegistrationActivity activity;

        InsertUserTask(Document user, RegistrationActivity activity) {
            this.user = user;
            this.activity = activity;
        }

        @Override
        public void run() {
            try (MongoClient mongoClient = MongoClients.create("mongodb://10.0.2.2:27017")) {
                MongoDatabase database = mongoClient.getDatabase("HostelManagement");
                MongoCollection<Document> usersCollection = database.getCollection("Users");
                usersCollection.insertOne(user);
                activity.runOnUiThread(() -> {
                    Toast.makeText(activity, "Registration Successful!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(activity, LoginActivity.class);
                    activity.startActivity(intent);
                });
            } catch (Exception e) {
                activity.runOnUiThread(() -> Toast.makeText(activity, "Registration Failed!", Toast.LENGTH_LONG).show());
            }
        }
    }
}
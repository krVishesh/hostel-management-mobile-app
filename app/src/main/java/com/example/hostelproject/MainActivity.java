package com.example.hostelproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText pinInput;
    private GridLayout gridLayout;
    private final StringBuilder enteredPin = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        pinInput = findViewById(R.id.pinInput);
        gridLayout = findViewById(R.id.gridLayout);
        ImageView fingerprintIcon = findViewById(R.id.fingerprintIcon);

        // Setup number pad buttons and handle input
        setupNumberPad();

        // Setup fingerprint authentication
        fingerprintIcon.setOnClickListener(v -> setupFingerprintAuthentication());

        TextView registerOrLogin = findViewById(R.id.registerOrLogin);
        registerOrLogin.setOnClickListener(v -> {
            // Start the LoginActivity or RegistrationActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void setupNumberPad() {
        // Loop through the GridLayout to find the buttons
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View view = gridLayout.getChildAt(i);
            if (view instanceof Button) {
                Button button = (Button) view;

                button.setOnClickListener(v -> {
                    // If backspace (⌫) button is clicked
                    if (button.getText().equals("⌫")) {
                        handleBackspace();
                    } else {
                        handlePinInput(button.getText().toString());
                    }
                });
            }
        }
    }

    private void handlePinInput(String input) {
        // Limit the entered PIN to 6 digits
        int maxPinLength = 6;
        if (enteredPin.length() < maxPinLength) {
            enteredPin.append(input);
            updatePinDisplay();

            // Check if PIN has reached 6 digits
            if (enteredPin.length() == maxPinLength) {
                verifyPin();
            }
        }
    }

    private void handleBackspace() {
        // Remove the last digit from the entered PIN
        if (enteredPin.length() > 0) {
            enteredPin.deleteCharAt(enteredPin.length() - 1);
            updatePinDisplay();
        }
    }

    private void updatePinDisplay() {
        // Display the entered PIN as "•" symbols (hidden password style)
        pinInput.setText(new String(new char[enteredPin.length()]).replace("\0", "•"));
    }

    private void verifyPin() {
        // Check if the user has logged in before
        SharedPreferences sharedPreferences = getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            enteredPin.setLength(0);  // Clear the entered PIN
            updatePinDisplay();  // Clear the displayed dots
            return;
        }

        // Execute task to authenticate the user using the PIN
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new AuthenticatePinTask(enteredPin.toString(), this));
    }

    private static class AuthenticatePinTask implements Runnable {
        private final String enteredPin;
        private final MainActivity activity;

        AuthenticatePinTask(String enteredPin, MainActivity activity) {
            this.enteredPin = enteredPin;
            this.activity = activity;
        }

        @Override
        public void run() {
            try (MongoClient mongoClient = MongoClients.create("mongodb://10.0.2.2:27017")) {
                MongoDatabase database = mongoClient.getDatabase("HostelManagement");
                MongoCollection<Document> usersCollection = database.getCollection("Users");
                Document query = new Document("pin", enteredPin);
                Document user = usersCollection.find(query).first();
                if (user != null) {
                    String storedPin = user.getString("pin");
                    if (enteredPin.equals(storedPin)) {
                        activity.runOnUiThread(() -> {
                            Toast.makeText(activity, "Login Successful!", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(activity, MainScreenActivity.class);
                            activity.startActivity(intent);
                        });
                    } else {
                        activity.runOnUiThread(() -> {
                            Toast.makeText(activity, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                            activity.enteredPin.setLength(0);  // Clear the entered PIN after wrong attempt
                            activity.updatePinDisplay();  // Clear the displayed dots
                        });
                    }
                } else {
                    activity.runOnUiThread(() -> {
                        Toast.makeText(activity, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                        activity.enteredPin.setLength(0);  // Clear the entered PIN after wrong attempt
                        activity.updatePinDisplay();  // Clear the displayed dots
                    });
                }
            } catch (Exception e) {
                activity.runOnUiThread(() -> Toast.makeText(activity, "Login Failed!", Toast.LENGTH_LONG).show());
            }
        }
    }

    private void setupFingerprintAuthentication() {
        // Check if biometric authentication is available and supported
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                // Biometric features are available and ready to use
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(this, "No biometric hardware found on this device", Toast.LENGTH_SHORT).show();
                return;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(this, "Biometric hardware is currently unavailable", Toast.LENGTH_SHORT).show();
                return;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(this, "No biometric credentials are currently enrolled", Toast.LENGTH_SHORT).show();
                return;
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                Toast.makeText(this, "Security update is required for biometric authentication", Toast.LENGTH_SHORT).show();
                return;
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                Toast.makeText(this, "Biometric authentication is not supported on this device", Toast.LENGTH_SHORT).show();
                return;
            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
                Toast.makeText(this, "Biometric status is unknown", Toast.LENGTH_SHORT).show();
                return;
        }

        // Set up the biometric prompt
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(MainActivity.this, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(MainActivity.this, "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                // Proceed to the next activity after successful authentication
                Intent intent = new Intent(MainActivity.this, MainScreenActivity.class);
                startActivity(intent);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        // Create the prompt info
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Login")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Cancel")
                .build();

        // Trigger the biometric prompt
        biometricPrompt.authenticate(promptInfo);
    }
}
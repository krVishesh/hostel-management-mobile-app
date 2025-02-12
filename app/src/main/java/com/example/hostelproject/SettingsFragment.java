package com.example.hostelproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SettingsFragment extends Fragment {

    private RatingBar ratingBar;
    private CheckBox autofillCheckbox, notificationsCheckbox;
    private Button saveButton, logoutButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        ratingBar = view.findViewById(R.id.ratingBar);
        autofillCheckbox = view.findViewById(R.id.autofillCheckbox);
        notificationsCheckbox = view.findViewById(R.id.notificationsCheckbox);
        saveButton = view.findViewById(R.id.saveButton);
        logoutButton = view.findViewById(R.id.logoutButton);

        saveButton.setOnClickListener(v -> saveSettings());
        logoutButton.setOnClickListener(v -> logout());

        // Fetch and display user settings
        fetchAndDisplaySettings();

        return view;
    }

    private void fetchAndDisplaySettings() {
        // Fetch user details from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        String registrationNumber = sharedPreferences.getString("registrationNumber", null);

        if (registrationNumber == null) {
            Toast.makeText(getContext(), "User details not found", Toast.LENGTH_LONG).show();
            return;
        }

        // Execute task to fetch the settings from MongoDB
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try (MongoClient mongoClient = MongoClients.create("mongodb://10.0.2.2:27017")) {
                MongoDatabase database = mongoClient.getDatabase("HostelManagement");
                MongoCollection<Document> settingsCollection = database.getCollection("Settings");

                Document query = new Document("registrationNumber", registrationNumber);
                Document settings = settingsCollection.find(query).first();

                if (settings != null) {
                    getActivity().runOnUiThread(() -> {
                        ratingBar.setRating(((Double) settings.get("rating")).floatValue());
                        autofillCheckbox.setChecked(settings.getBoolean("autofillEnabled"));
                        notificationsCheckbox.setChecked(settings.getBoolean("notificationsEnabled"));
                    });
                } else {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "No settings found for user", Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to fetch settings", Toast.LENGTH_LONG).show());
            }
        });
    }

    private void saveSettings() {
        float rating = ratingBar.getRating();
        boolean isAutofillEnabled = autofillCheckbox.isChecked();
        boolean isNotificationsEnabled = notificationsCheckbox.isChecked();

        // Fetch user details from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        String registrationNumber = sharedPreferences.getString("registrationNumber", null);
        String email = sharedPreferences.getString("email", null);

        if (registrationNumber == null || email == null) {
            Toast.makeText(getContext(), "User details not found", Toast.LENGTH_LONG).show();
            return;
        }

        // Create a document with the settings and user details
        Document settings = new Document("registrationNumber", registrationNumber)
                .append("email", email)
                .append("rating", rating)
                .append("autofillEnabled", isAutofillEnabled)
                .append("notificationsEnabled", isNotificationsEnabled);

        // Execute task to insert or update the document in MongoDB
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try (MongoClient mongoClient = MongoClients.create("mongodb://10.0.2.2:27017")) {
                MongoDatabase database = mongoClient.getDatabase("HostelManagement");
                MongoCollection<Document> settingsCollection = database.getCollection("Settings");

                // Upsert the document (insert if not exists, update if exists)
                settingsCollection.updateOne(
                        new Document("registrationNumber", registrationNumber),
                        new Document("$set", settings),
                        new com.mongodb.client.model.UpdateOptions().upsert(true)
                );

                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Settings Saved", Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Failed to save settings", Toast.LENGTH_LONG).show());
            }
        });
    }

    private void logout() {
        // Clear SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Redirect to LoginActivity
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }
}
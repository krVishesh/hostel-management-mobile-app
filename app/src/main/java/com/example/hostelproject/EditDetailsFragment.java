// EditDetailsFragment.java
package com.example.hostelproject;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class EditDetailsFragment extends Fragment {

    private static final int CAMERA_REQUEST_CODE = 100;

    private EditText nameEditText, emailEditText, registrationNumberEditText, phoneNumberEditText, addressEditText;
    private Button saveButton, updateImageButton;
    private ImageView accountImageView;
    private Bitmap accountImageBitmap;

    private ActivityResultLauncher<Intent> cameraResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == FragmentActivity.RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    accountImageBitmap = (Bitmap) extras.get("data");
                    accountImageView.setImageBitmap(accountImageBitmap);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_details, container, false);

        nameEditText = view.findViewById(R.id.nameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        registrationNumberEditText = view.findViewById(R.id.registrationNumberEditText);
        phoneNumberEditText = view.findViewById(R.id.phoneNumberEditText);
        addressEditText = view.findViewById(R.id.addressEditText);
        saveButton = view.findViewById(R.id.saveButton);
        updateImageButton = view.findViewById(R.id.updateImageButton);
        accountImageView = view.findViewById(R.id.accountImageView);

        // Fetch user details from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("name", "");
        String email = sharedPreferences.getString("email", "");
        String registrationNumber = sharedPreferences.getString("registrationNumber", "");
        String phoneNumber = sharedPreferences.getString("contact", "");
        String address = sharedPreferences.getString("address", "");

        // Set user details to EditTexts
        nameEditText.setText(name);
        emailEditText.setText(email);
        registrationNumberEditText.setText(registrationNumber);
        phoneNumberEditText.setText(phoneNumber);
        addressEditText.setText(address);

        saveButton.setOnClickListener(v -> saveDetails());
        updateImageButton.setOnClickListener(v -> checkCameraPermission());

        return view;
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraResultLauncher.launch(cameraIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getContext(), "Camera permission is required to update the image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveDetails() {
        String name = nameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String registrationNumber = registrationNumberEditText.getText().toString();
        String phoneNumber = phoneNumberEditText.getText().toString();
        String address = addressEditText.getText().toString();

        if (name.isEmpty() || email.isEmpty() || registrationNumber.isEmpty() || phoneNumber.isEmpty() || address.isEmpty()) {
            Toast.makeText(getActivity(), "All fields are required", Toast.LENGTH_LONG).show();
            return;
        }

        // Save updated details to SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("registrationNumber", registrationNumber);
        editor.putString("contact", phoneNumber);
        editor.putString("address", address);
        editor.apply();

        // Convert the image to Base64
        String imageBase64;
        if (accountImageBitmap != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            accountImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            imageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            editor.putString("image", imageBase64);
            editor.apply();
        } else {
            imageBase64 = null;
        }

        // Update details in MongoDB
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try (MongoClient mongoClient = MongoClients.create("mongodb://10.0.2.2:27017")) {
                MongoDatabase database = mongoClient.getDatabase("HostelManagement");
                MongoCollection<Document> usersCollection = database.getCollection("Users");

                Document query = new Document("registrationNumber", registrationNumber);
                Document update = new Document("$set", new Document("name", name)
                        .append("email", email)
                        .append("contact", phoneNumber)
                        .append("address", address)
                        .append("image", imageBase64));

                usersCollection.updateOne(query, update);

                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> Toast.makeText(activity, "Details Updated", Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(() -> Toast.makeText(activity, "Failed to update details", Toast.LENGTH_LONG).show());
                }
            }
        });

        // Navigate back to AccountFragment
        getParentFragmentManager().popBackStack();
    }
}
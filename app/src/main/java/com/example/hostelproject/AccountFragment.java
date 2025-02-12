// AccountFragment.java
package com.example.hostelproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class AccountFragment extends Fragment {

    private TextView nameTextView, emailTextView, registrationNumberTextView, phoneNumberTextView, addressTextView;
    private Button editDetailsButton, trackingButton;
    private ImageView accountImageView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        nameTextView = view.findViewById(R.id.nameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        registrationNumberTextView = view.findViewById(R.id.registrationNumberTextView);
        phoneNumberTextView = view.findViewById(R.id.phoneNumberTextView);
        addressTextView = view.findViewById(R.id.addressTextView);
        accountImageView = view.findViewById(R.id.accountImageView);
        editDetailsButton = view.findViewById(R.id.editDetailsButton);
        trackingButton = view.findViewById(R.id.trackingButton);

        // Fetch user details from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("name", "N/A");
        String email = sharedPreferences.getString("email", "N/A");
        String registrationNumber = sharedPreferences.getString("registrationNumber", "N/A");
        String phoneNumber = sharedPreferences.getString("contact", "N/A");
        String address = sharedPreferences.getString("address", "N/A");

        // Set user details to TextViews
        nameTextView.setText("Name: " + name);
        emailTextView.setText("Email: " + email);
        registrationNumberTextView.setText("Registration Number: " + registrationNumber);
        phoneNumberTextView.setText("Phone Number: " + phoneNumber);
        addressTextView.setText("Address: " + address);

        editDetailsButton.setOnClickListener(v -> openEditDetailsFragment());
        trackingButton.setOnClickListener(v -> goToTracking());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Fetch user details from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("name", "N/A");
        String email = sharedPreferences.getString("email", "N/A");
        String registrationNumber = sharedPreferences.getString("registrationNumber", "N/A");
        String phoneNumber = sharedPreferences.getString("contact", "N/A");
        String address = sharedPreferences.getString("address", "N/A");
        String imageBase64 = sharedPreferences.getString("image", null);

        // Set user details to TextViews
        nameTextView.setText("Name: " + name);
        emailTextView.setText("Email: " + email);
        registrationNumberTextView.setText("Registration Number: " + registrationNumber);
        phoneNumberTextView.setText("Phone Number: " + phoneNumber);
        addressTextView.setText("Address: " + address);

        // Update the image
        if (imageBase64 != null) {
            byte[] imageBytes = Base64.decode(imageBase64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            accountImageView.setImageBitmap(bitmap);
        }
    }

    private void openEditDetailsFragment() {
        EditDetailsFragment editDetailsFragment = new EditDetailsFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, editDetailsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void goToTracking() {
        TrackingFragment trackingFragment = new TrackingFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, trackingFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
package com.example.hostelproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ContactUsFragment extends Fragment {

    public ContactUsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contact_us, container, false);

        // Set up click listeners
        TextView linkedIn = view.findViewById(R.id.linkedIn);
        TextView github = view.findViewById(R.id.github);
        TextView website = view.findViewById(R.id.website);

        linkedIn.setOnClickListener(v -> openLinkedIn());
        github.setOnClickListener(v -> openGitHub());
        website.setOnClickListener(v -> openWebsite());

        return view;
    }

    private void openWebsite() {
        String url = "https://www.vit.ac.in";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void openLinkedIn() {
        String url = "https://www.linkedin.com/";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void openGitHub() {
        String url = "https://github.com/";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}

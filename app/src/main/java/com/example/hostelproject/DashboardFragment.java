package com.example.hostelproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class DashboardFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        Button buttonComplaints = view.findViewById(R.id.button_complaints);
        Button buttonServices = view.findViewById(R.id.button_services);
        Button buttonAccount = view.findViewById(R.id.button_account);
        Button buttonTracking = view.findViewById(R.id.button_tracking);
        Button buttonAboutUs = view.findViewById(R.id.button_about_us);
        Button buttonSettings = view.findViewById(R.id.button_settings);
        Button buttonPreviousComplaints = view.findViewById(R.id.button_previous_complaints);
        Button buttonPreviousServices = view.findViewById(R.id.button_previous_services);

        buttonComplaints.setOnClickListener(v -> navigateToFragment(new ComplaintsFragment()));
        buttonServices.setOnClickListener(v -> navigateToFragment(new ServicesFragment()));
        buttonAccount.setOnClickListener(v -> navigateToFragment(new AccountFragment()));
        buttonTracking.setOnClickListener(v -> navigateToFragment(new TrackingFragment()));
        buttonAboutUs.setOnClickListener(v -> navigateToFragment(new AboutUsFragment()));
        buttonSettings.setOnClickListener(v -> navigateToFragment(new SettingsFragment()));
        buttonPreviousComplaints.setOnClickListener(v -> navigateToFragment(new ComplaintsFragment()));
        buttonPreviousServices.setOnClickListener(v -> navigateToFragment(new ServicesFragment()));

        return view;
    }

    private void navigateToFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
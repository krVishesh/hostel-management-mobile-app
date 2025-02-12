package com.example.hostelproject;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;

public class MainScreenActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageView menuIcon, logoutIcon;
    private LinearLayout toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        drawerLayout = findViewById(R.id.drawer_layout);
        menuIcon = findViewById(R.id.menu_icon);
        logoutIcon = findViewById(R.id.logout_icon);
        toolbar = findViewById(R.id.toolbar);

        // Default fragment when activity starts
        loadFragment(new DashboardFragment());

        // Set menu icon click listener to toggle drawer
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        // Handle navigation menu item clicks
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment = null;
                int id = menuItem.getItemId();

                if (id == R.id.nav_dashboard) {
                    fragment = new DashboardFragment();
                } else if (id == R.id.nav_complaints) {
                    fragment = new ComplaintsFragment();
                } else if (id == R.id.nav_services) {
                    fragment = new ServicesFragment();
                } else if (id == R.id.nav_tracking) {
                    fragment = new TrackingFragment();
                }

                if (fragment != null) {
                    loadFragment(fragment);
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        // Handle account menu items (Account section at the bottom)
        NavigationView accountNavView = findViewById(R.id.nav_view_account);
        accountNavView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment = null;
                int id = menuItem.getItemId();

                if (id == R.id.nav_account) {
                    fragment = new AccountFragment();
                } else if (id == R.id.nav_settings) {
                    fragment = new SettingsFragment();
                } else if (id == R.id.nav_about_us) {
                    fragment = new AboutUsFragment();
                } else if (id == R.id.nav_contact_us) {
                    fragment = new ContactUsFragment();
                }

                if (fragment != null) {
                    loadFragment(fragment);
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        // Logout icon logic (similar to earlier)
        logoutIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
            }
        });
    }

    // Method to load a fragment into the content_frame
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showLogoutDialog() {
        // Similar logout logic as before
        new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    finishAffinity(); // Close all activities
                    System.exit(0); // Fully exit the app
                })
                .setNegativeButton("No", null)
                .show();
    }
}
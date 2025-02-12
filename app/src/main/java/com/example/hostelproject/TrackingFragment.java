package com.example.hostelproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.lang.ref.WeakReference;

public class TrackingFragment extends Fragment {

    private LinearLayout complainHistoryLayout, serviceHistoryLayout;
    private static final String MONGO_URI = "mongodb://10.0.2.2:27017";
    private MongoClient mongoClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracking, container, false);

        complainHistoryLayout = view.findViewById(R.id.complainHistoryLayout);
        serviceHistoryLayout = view.findViewById(R.id.serviceHistoryLayout);

        mongoClient = MongoClients.create(MONGO_URI);
        new FetchDataAsyncTask(this).execute();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    private String getUserId() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserDetails", Context.MODE_PRIVATE);
        return sharedPreferences.getString("registrationNumber", "defaultUserId");
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    private static class FetchDataAsyncTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<TrackingFragment> fragmentReference;

        FetchDataAsyncTask(TrackingFragment fragment) {
            fragmentReference = new WeakReference<>(fragment);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            TrackingFragment fragment = fragmentReference.get();
            if (fragment != null) {
                try {
                    MongoDatabase database = fragment.mongoClient.getDatabase("HostelManagement");
                    fragment.fetchComplainHistory(database);
                    fragment.fetchServiceHistory(database);
                } catch (Exception e) {
                    if (fragment.getActivity() != null) {
                        fragment.getActivity().runOnUiThread(() -> fragment.showToast("Error connecting to database"));
                    }
                }
            }
            return null;
        }
    }

    private void fetchComplainHistory(MongoDatabase database) {
        try {
            MongoCollection<Document> complaintsCollection = database.getCollection("Complaints");
            long completedComplaintCount = complaintsCollection.countDocuments(new Document("regNo", getUserId()).append("status", "completed"));
            long ongoingComplaintCount = complaintsCollection.countDocuments(new Document("regNo", getUserId()).append("status", "ongoing"));

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    TextView completedTextView = new TextView(getContext());
                    completedTextView.setText("Completed Complaints: " + completedComplaintCount);
                    completedTextView.setTextColor(getResources().getColor(R.color.white));
                    complainHistoryLayout.addView(completedTextView);

                    TextView ongoingTextView = new TextView(getContext());
                    ongoingTextView.setText("Ongoing Complaints: " + ongoingComplaintCount);
                    ongoingTextView.setTextColor(getResources().getColor(R.color.white));
                    complainHistoryLayout.addView(ongoingTextView);
                });
            }
        } catch (Exception e) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> showToast("Failed to fetch complaints"));
            }
        }
    }

    private void fetchServiceHistory(MongoDatabase database) {
        try {
            MongoCollection<Document> servicesCollection = database.getCollection("Services");
            long completedServiceCount = servicesCollection.countDocuments(new Document("regNo", getUserId()).append("status", "completed"));
            long ongoingServiceCount = servicesCollection.countDocuments(new Document("regNo", getUserId()).append("status", "ongoing"));

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    TextView completedTextView = new TextView(getContext());
                    completedTextView.setText("Completed Services: " + completedServiceCount);
                    completedTextView.setTextColor(getResources().getColor(R.color.white));
                    serviceHistoryLayout.addView(completedTextView);

                    TextView ongoingTextView = new TextView(getContext());
                    ongoingTextView.setText("Ongoing Services: " + ongoingServiceCount);
                    ongoingTextView.setTextColor(getResources().getColor(R.color.white));
                    serviceHistoryLayout.addView(ongoingTextView);
                });
            }
        } catch (Exception e) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> showToast("Failed to fetch services"));
            }
        }
    }
}
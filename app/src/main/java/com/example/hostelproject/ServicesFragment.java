package com.example.hostelproject;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

public class ServicesFragment extends Fragment {

    private EditText nameEditText, regNoEditText, addressEditText, contactEditText, complaintDetailsEditText;
    private Spinner servicesTypeSpinner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_services, container, false);

        // Initialize views
        nameEditText = view.findViewById(R.id.nameEditText);
        regNoEditText = view.findViewById(R.id.regNoEditText);
        addressEditText = view.findViewById(R.id.addressEditText);
        contactEditText = view.findViewById(R.id.contactEditText);
        complaintDetailsEditText = view.findViewById(R.id.complaintDetailsEditText);
        servicesTypeSpinner = view.findViewById(R.id.servicesTypeSpinner);
        Button submitServicesButton = view.findViewById(R.id.submitServicesButton);

        // Set up the Spinner (the entries are defined in XML under @array/service_types)
        setupSpinner();

        // Set up submit button click listener
        submitServicesButton.setOnClickListener(v -> submitForm());

        return view;
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.service_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        servicesTypeSpinner.setAdapter(adapter);
    }

    private void submitForm() {
        // Retrieve form data
        String name = nameEditText.getText().toString().trim();
        String regNo = regNoEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String contact = contactEditText.getText().toString().trim();
        String serviceDetails = complaintDetailsEditText.getText().toString().trim();
        String serviceType = servicesTypeSpinner.getSelectedItem().toString();

        // Validate required fields
        if (name.isEmpty() || address.isEmpty() || contact.isEmpty() || serviceDetails.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a document to insert into the collection
        Document serviceRequest = new Document("name", name)
                .append("regNo", regNo)
                .append("address", address)
                .append("contact", contact)
                .append("serviceDetails", serviceDetails)
                .append("serviceType", serviceType)
                .append("status", "ongoing");

        // Execute task to insert the document
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new InsertServiceRequestTask(serviceRequest, this));
    }

    private static class InsertServiceRequestTask implements Runnable {
        private final Document serviceRequest;
        private final ServicesFragment fragment;

        InsertServiceRequestTask(Document serviceRequest, ServicesFragment fragment) {
            this.serviceRequest = serviceRequest;
            this.fragment = fragment;
        }

        @Override
        public void run() {
            try (MongoClient mongoClient = MongoClients.create("mongodb://10.0.2.2:27017")) {
                MongoDatabase database = mongoClient.getDatabase("HostelManagement");
                MongoCollection<Document> servicesCollection = database.getCollection("Services");
                servicesCollection.insertOne(serviceRequest);
                fragment.requireActivity().runOnUiThread(() -> {
                    // Clear form fields
                    fragment.nameEditText.setText("");
                    fragment.regNoEditText.setText("");
                    fragment.addressEditText.setText("");
                    fragment.contactEditText.setText("");
                    fragment.complaintDetailsEditText.setText("");
                    // Show success message
                    Toast.makeText(fragment.requireContext(), "Service request submitted successfully!", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e("ServicesFragment", "Error inserting service request", e);
                fragment.requireActivity().runOnUiThread(() ->
                        Toast.makeText(fragment.requireContext(), "Failed to submit service request.", Toast.LENGTH_SHORT).show()
                );
            }
        }
    }
}
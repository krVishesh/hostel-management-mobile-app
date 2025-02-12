package com.example.hostelproject;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

public class ComplaintsFragment extends Fragment {

    private EditText regNoEditText, emailEditText, addressEditText, contactEditText, complaintDetailsEditText;
    private Spinner complaintTypeSpinner;
    private AutoCompleteTextView nameEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_complaints, container, false);

        // Initialize views
        nameEditText = view.findViewById(R.id.nameEditText);
        regNoEditText = view.findViewById(R.id.regNoEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        addressEditText = view.findViewById(R.id.addressEditText);
        contactEditText = view.findViewById(R.id.contactEditText);
        complaintDetailsEditText = view.findViewById(R.id.complaintDetailsEditText);
        complaintTypeSpinner = view.findViewById(R.id.complaintTypeSpinner);
        Button submitButton = view.findViewById(R.id.submitComplaintButton);

        // Set up the Spinner (you will need to provide an adapter and data for it)
        setupSpinner();

        // Set up submit button click listener
        submitButton.setOnClickListener(v -> submitForm());

        return view;
    }

    private void setupSpinner() {
        String[] complaintTypes = getResources().getStringArray(R.array.complaint_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, complaintTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        complaintTypeSpinner.setAdapter(adapter);
    }

    private void submitForm() {
        // Retrieve form data
        String name = nameEditText.getText().toString().trim();
        String regNo = regNoEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();
        String contact = contactEditText.getText().toString().trim();
        String complaintDetails = complaintDetailsEditText.getText().toString().trim();
        String complaintType = complaintTypeSpinner.getSelectedItem().toString();

        // Validate required fields
        if (name.isEmpty() || regNo.isEmpty() || address.isEmpty() || contact.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a document to insert into the collection
        Document complaint = new Document("name", name)
                .append("regNo", regNo)
                .append("email", email)
                .append("address", address)
                .append("contact", contact)
                .append("complaintDetails", complaintDetails)
                .append("complaintType", complaintType)
                .append("status", "ongoing");

        // Execute task to insert the document
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(new InsertComplaintTask(complaint, this));
    }

    private static class InsertComplaintTask implements Runnable {
        private final Document complaint;
        private final ComplaintsFragment fragment;

        InsertComplaintTask(Document complaint, ComplaintsFragment fragment) {
            this.complaint = complaint;
            this.fragment = fragment;
        }

        @Override
        public void run() {
            try (MongoClient mongoClient = MongoClients.create("mongodb://10.0.2.2:27017")) {
                MongoDatabase database = mongoClient.getDatabase("HostelManagement");
                MongoCollection<Document> complaintsCollection = database.getCollection("Complaints");
                complaintsCollection.insertOne(complaint);
                fragment.requireActivity().runOnUiThread(() -> {
                    // Clear form fields
                    fragment.nameEditText.setText("");
                    fragment.regNoEditText.setText("");
                    fragment.emailEditText.setText("");
                    fragment.addressEditText.setText("");
                    fragment.contactEditText.setText("");
                    fragment.complaintDetailsEditText.setText("");
                    fragment.complaintTypeSpinner.setSelection(0);
                    // Show success message
                    Toast.makeText(fragment.requireContext(), "Complaint submitted successfully!", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e("ComplaintsFragment", "Error inserting complaint", e);
                fragment.requireActivity().runOnUiThread(() ->
                        Toast.makeText(fragment.requireContext(), "Failed to submit complaint.", Toast.LENGTH_SHORT).show()
                );
            }
        }
    }
}
package com.leadstepapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.type.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class login extends AppCompatActivity {
    EditText PatientN, PatientID, DoctorN, DoctorID, Email;
    Button StartBtn;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // DB test
        // Write a message to the database
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        DatabaseReference myRef = database.getReference("message");
//// ...
//        myRef.setValue("Hello, World!");
        // Get a reference to the database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Write data to the database
        writeData();

        // Read data from the database
        readData();

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
//            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        Log.d(String.valueOf(pairedDevices.size()), "pairedDevices.size(): ");
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                Log.d("getName", device.getName());
                Log.d("getAddress", device.getAddress());
            }
        }

        PatientN = findViewById(R.id.patient_name);
        PatientID = findViewById(R.id.patient_id);
        DoctorN = findViewById(R.id.doctor_name);
        DoctorID = findViewById(R.id.doctor_id);
        StartBtn = findViewById(R.id.start_button);

        StartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(login.this, "Login successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), Visualization.class);
                Bundle extras = new Bundle();
                extras.putString("PATIENT_NAME", PatientN.getText().toString());
                extras.putString("PATIENT_ID", PatientID.getText().toString());
                extras.putString("DOCTOR_NAME", DoctorN.getText().toString());
                extras.putString("DOCTOR_ID", DoctorID.getText().toString());
                intent.putExtras(extras);
                startActivity(intent);

            }
        });


    }

    private void writeData() {
        // Create a new "users" child in the database
        DatabaseReference usersRef = mDatabase.child("users"); // user name
//        Double[] left = new Double[89];
//        Double[] right = new Double[89];
        ArrayList<Double> left = new ArrayList<>();
        ArrayList<Double> right = new ArrayList<>();
        left.add(0.1);
        right.add(0.2);
        // Add a new user with some data
        User newUser = new User("User", left, right);
        Log.d(String.valueOf(newUser.getLeft()), "newUser: ");
        usersRef.child(usersRef.push().getKey()).setValue(newUser); // collected data(unique)
    }

    private void readData() {
        // Read data from the "users" child in the database
        DatabaseReference usersRef = mDatabase.child("users");

        // Attach a listener to read the data
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and whenever data at this location is updated
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    User user = new User();
                    Log.d(userSnapshot.getKey(), "userSnapshot.getKey(): ");
                    Log.d(String.valueOf(userSnapshot.child(userSnapshot.getKey())), "userSnapshot.child(): ");
                    user.setName(userSnapshot.getKey());

//                    User user = userSnapshot.child("user1").getValue(User.class);
//                    Log.d(user.getName(), "user1: ");
                    if (user != null) {
                        Log.d("Firebase", "User: " + user.getName() );
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Failed to read value
                Log.e("Firebase", "Failed to read value.", databaseError.toException());
            }
        });
    }
}
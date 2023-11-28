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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Set;


public class login extends AppCompatActivity {
    EditText PatientN, PatientID, DoctorN, DoctorID, Email;
    Button StartBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
}
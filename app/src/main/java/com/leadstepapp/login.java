package com.leadstepapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class login extends AppCompatActivity {
    EditText PatientN, PatientID, DoctorN, DoctorID, Email;
    Button StartBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


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
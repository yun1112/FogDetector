package com.leadstepapp;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class login extends AppCompatActivity {
    EditText PatientN, PatientID, DoctorN, DoctorID, Email;
    Button StartBtn;
    private DatabaseReference mDatabase;
    private Timer timer = new Timer();
    DatabaseReference usersRef;

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
//        writeData();

        // Read data from the database
//        readData();
        requestBluetoothPermission(this, 1); // Request permission for Bluetooth(above API30?)

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
//        PatientID = findViewById(R.id.patient_id);
//        DoctorN = findViewById(R.id.doctor_name);
//        DoctorID = findViewById(R.id.doctor_id);
        StartBtn = findViewById(R.id.start_button);

        usersRef = mDatabase.child("user"); // user name
        StartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                    test();
                Toast.makeText(login.this, "Login successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), Visualization.class);
                Bundle extras = new Bundle();
                extras.putString("PATIENT_NAME", PatientN.getText().toString());
//                extras.putString("PATIENT_ID", PatientID.getText().toString());
//                extras.putString("DOCTOR_NAME", DoctorN.getText().toString());
//                extras.putString("DOCTOR_ID", DoctorID.getText().toString());
                intent.putExtras(extras);
                startActivity(intent);
                }

        });


    }

    private static final String[] BLE_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    private static final String[] ANDROID_12_BLE_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    public static void requestBluetoothPermission(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            ActivityCompat.requestPermissions(activity, ANDROID_12_BLE_PERMISSIONS, requestCode);
        else
            ActivityCompat.requestPermissions(activity, BLE_PERMISSIONS, requestCode);
    }

    private void test() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                writeData();
//                                sendToFirebase(rightDataDict,"Right_insole");
//                                Log.d(TAG, "jinkatama: " + RListDict.size());
//                                RListDict.clear();
//                                RList.clear();
//                                Log.d(TAG, "jinkatama: " + RListDict.size());
            }
        }, 1000, 1000);
        Toast.makeText(getApplicationContext(), "DB.", Toast.LENGTH_SHORT).show();

    }
    private void writeData() {
        Date today = new Date();

        // 포맷 지정 후: 2022-11-16 19:15:44
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        System.out.println("포맷 지정 후: " + dateFormat.format(today));

        // Create a new "users" child in the database
//        DatabaseReference usersRef = mDatabase.child(dateFormat.format(today)); // user name
        String currentDate = dateFormat.format(today);
//        usersRef.child(currentDate);
//        usersRef.child(currentDate); // collected data(unique)

//        DatabaseReference usersRef = mDatabase.child("users"); // user name
//        usersRef.child("left").setValue(left); // collected data(unique)

//        Double[] left = new Double[89];
//        Double[] right = new Double[89];
        List<Double> left = new ArrayList<>();
        List<Double> right = new ArrayList<>();
        Random random = new Random();

        // 랜덤 ArrayList의 크기
        int arrayListSize = 89;

        // 랜덤 ArrayList 선언
        ArrayList<Double> randomArrayList = new ArrayList<>();

        // ArrayList에 랜덤 숫자 추가
        for (int i = 0; i < arrayListSize; i++) {
            // 0.0부터 1.0 사이의 랜덤 실수 생성
            double randomNumber = random.nextDouble();
            randomArrayList.add((double) (Math.round(randomNumber) ));

            // 만약 -1.0에서 1.0 사이의 랜덤 실수를 생성하려면 아래 주석을 해제하세요.
            // double randomNumber = random.nextDouble() * 2 - 1.0;
            // randomArrayList.add(randomNumber);
        }

        left = randomArrayList;
        randomArrayList.clear();

        // ArrayList에 랜덤 숫자 추가
        for (int i = 0; i < arrayListSize; i++) {
            // 0.0부터 1.0 사이의 랜덤 실수 생성
            double randomNumber = random.nextDouble();
            randomArrayList.add((double) (Math.round(randomNumber) ));


            // 만약 -1.0에서 1.0 사이의 랜덤 실수를 생성하려면 아래 주석을 해제하세요.
            // double randomNumber = random.nextDouble() * 2 - 1.0;
            // randomArrayList.add(randomNumber);
        }
//        right.add(new Random().nextDouble());
        right = randomArrayList;
        Log.d(left.toString(), "writeData: left");
        Log.d(right.toString(), "writeData: right");
        System.out.println("left:"+left.toString());
        System.out.println("right:"+right.toString());
//        usersRef.child("left").setValue(left);
//        usersRef.child("right").setValue(left);

        usersRef.child(currentDate).child("left").setValue(left.toString());
        usersRef.child(currentDate).child("right").setValue(right.toString());
//        usersRef.child("left").setValue(left); // collected data(unique)
//        usersRef.child("right").setValue(right); // collected data(unique)
//        usersRef.child("date").setValue(dateFormat.format(today)); // collected data(unique)

        // Add a new user with some data
        // username_date_time
        // --- left
        // ------- getKey(): [0.0,.....,0.0]
        // ------- getKey(): [0.0,.....,0.0]
        // ------- getKey(): [0.0,.....,0.0]
        // --- right

        User newUser = new User("User", left, right);
        Log.d(String.valueOf(newUser.getLeft()), "newUser: ");
        // dateFormat
//        usersRef.child("unser_"+dateFormat.format(today)).setValue(newUser); // collected data(unique)
//        usersRef.child(usersRef.push().getKey()).setValue(newUser); // collected data(unique)
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
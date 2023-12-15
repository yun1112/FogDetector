package com.leadstepapp;


import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.leadstepapp.bluetoothseriallibrary.BluetoothManager;
import com.leadstepapp.bluetoothseriallibrary.BluetoothSerialDevice;
import com.leadstepapp.bluetoothseriallibrary.SimpleBluetoothDeviceInterface;

import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.hss.heatmaplib.HeatMap;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.Date;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Stream;

public class Visualization extends BlunoLibrary {

    private String modelFile = "mymodelConvert.tflite";
    Interpreter tflite;

    private TextView text;
    private TextView userName;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Button startBtn;
    private Button startLeftBtn, startRightBtn;
    private Button connectLeftBtn, connectRightBtn;
    //    private String L_insole_mac = "20:17:12:04:04:57";
//    private String R_insole_mac = "20:17:12:04:19:55";
//    private String L_insole_mac = "20:19:06:20:08:07";
//    private String R_insole_mac = "20:19:06:20:06:72";

    private String L_insole_mac = "20:19:06:20:08:97";
    private String R_insole_mac = "20:17:12:04:19:37";
    private String start_bytes = "0 254 128";
    private String left_data, right_data, left_temp_bytes, right_temp_bytes;
    private String USER_NAME, PATIENT_ID, DOCTOR_NAME, DOCTOR_ID;
    private boolean is_L_insole_started = false;
    private boolean is_R_insole_started = false;
    private boolean is_L_insole_connected = false;
    private boolean is_R_insole_connected = false;
    private boolean tesTestAsync_r, isTestAsync_l = false;
    private int right_data_len, left_data_len = 0;
    private int right_sensor_data_count, right_data_index, right_package_count = 0;
    private int left_sensor_data_count, left_data_index, left_package_count = 0;
    private int max_sensor_data_count = 89;
    private int max_data_len = 114;
    private int ns_list[] = {58, 76, 77, 78, 79, 80, 81, 82, 83, 84, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 112, 113, 114};
    private Double r_data_double_arr[] = new Double[89];

    private Double l_data_double_arr[] = new Double[89];
    private Double data_double_arr[] = new Double[178];

    private List<Integer> non_sensor_indeces = new ArrayList<Integer>(ns_list.length + 1);
    private SimpleBluetoothDeviceInterface left_insole_device_interface;
    private SimpleBluetoothDeviceInterface right_insole_device_interface;
    private TextView leftDataTv, rightDataTv;           //just put  leftDataTv, rightDataTv here for test
    private BluetoothManager bluetoothManager = BluetoothManager.getInstance();
    Map<String, Object> patientRecord = new HashMap<>();
    private HeatMap heatMapLeft, heatMapRight;
    private Timer right_timer = new Timer();
    private Timer left_timer = new Timer();

    private SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    float x_R[] = {0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.25f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f,
            0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.4f, 0.35f, 0.35f, 0.35f, 0.35f, 0.35f, 0.35f, 0.35f, 0.35f, 0.35f,
            0.55f, 0.55f, 0.55f, 0.55f, 0.55f, 0.55f, 0.55f, 0.55f, 0.55f, 0.55f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
            0.7f, 0.7f, 0.7f, 0.7f, 0.7f, 0.7f, 0.7f, 0.7f, 0.7f, 0.65f, 0.65f, 0.65f, 0.65f, 0.65f, 0.65f, 0.65f, 0.65f,
            0.85f, 0.85f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
            0.95f, 0.95f, 0.95f, 0.95f, 0.95f, 0.95f};

    float x_L[] = {0.75f, 0.75f, 0.75f, 0.75f, 0.75f, 0.75f, 0.75f, 0.75f, 0.75f, 0.75f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f,
            0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.65f, 0.65f, 0.65f, 0.65f, 0.65f, 0.65f, 0.65f, 0.65f, 0.65f,
            0.45f, 0.45f, 0.45f, 0.45f, 0.45f, 0.45f, 0.45f, 0.45f, 0.45f, 0.45f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
            0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.3f, 0.35f, 0.35f, 0.35f, 0.35f, 0.35f, 0.35f, 0.35f, 0.35f,
            0.15f, 0.15f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f, 0.2f,
            0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f};


    float y[] = {0.95f, 0.9f, 0.85f, 0.8f, 0.75f, 0.7f, 0.65f, 0.6f, 0.55f, 0.50f, 0.45f, 0.40f, 0.35f, 0.30f, 0.25f, 0.20f, 0.15f, 0.10f, 0.05f,
            0.95f, 0.9f, 0.85f, 0.8f, 0.75f, 0.7f, 0.65f, 0.6f, 0.55f, 0.50f, 0.45f, 0.40f, 0.35f, 0.30f, 0.25f, 0.20f, 0.15f, 0.10f, 0.05f,
            0.95f, 0.9f, 0.85f, 0.8f, 0.75f, 0.7f, 0.65f, 0.6f, 0.55f, 0.50f, 0.45f, 0.40f, 0.35f, 0.30f, 0.25f, 0.20f, 0.15f, 0.10f, 0.05f,
            0.9f, 0.85f, 0.8f, 0.75f, 0.7f, 0.65f, 0.6f, 0.55f, 0.50f, 0.45f, 0.40f, 0.35f, 0.30f, 0.25f, 0.20f, 0.15f, 0.10f,
            0.55f, 0.50f, 0.45f, 0.40f, 0.35f, 0.30f, 0.25f, 0.20f, 0.15f,
            0.45f, 0.40f, 0.35f, 0.30f, 0.25f, 0.20f};

    List LList = new ArrayList();
    List RList = new ArrayList();

    List LListDict = new ArrayList();
    List RListDict = new ArrayList();


    List StatusDict = new ArrayList();

//    ArrayList<String> LList = new ArrayList<String>();
//    ArrayList<String> RList = new ArrayList<String>();

    List ListData = new ArrayList();
//    List ListDataDict = new ArrayList();
//    ArrayList<String> ListData = new ArrayList<String>();

//    List LListDict = new ArrayList();
//    List RListDict = new ArrayList();

    private Button buttonScanL, buttonScanR, buttonScanV;

    private TextView coyText;

    private boolean is_sound_on = false;
    private int bpm = 60;
    private int vib_duration = 0;
    final ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
    final Handler handler = new Handler(Looper.getMainLooper());

    static boolean active = false;

    private boolean isCheckedL = false;
    private boolean isCheckedR = false;
    private String uid;
    private BluetoothDevice bleDevice;
    private ArrayAdapter<String> deviceName;
    private ArrayAdapter<String> deviceID;
    private Set<BluetoothDevice> pairedDevices;
    private String choseID;
    private DatabaseReference mDatabase;
    private Timer timer = new Timer();
    private DatabaseReference usersRef;

    private List<List<Double>> LDataListPerSec = Collections.synchronizedList(new ArrayList<>());
    private List<List<Double>> RDataListPerSec = Collections.synchronizedList(new ArrayList<>());
//    private List<List<Double>> dataList = Collections.synchronizedList(new ArrayList<>());
    private List<List<Double>> dataListPerSec = Collections.synchronizedList(new ArrayList<>());
    Timer gaitTimer = new Timer();
    private TimerTask task;
    private Boolean hasReceivedLData = false;
    private Boolean hasReceivedRData = false;

    private int dataListPerSecLen = 0;
    private static final int SAMPLE_RATE = 20; // milliseconds
    private static final int NUM_SAMPLES = 50;

    private TimerTask sampleTask;
    private int sampleCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualization);

        request(1000, new OnPermissionsResult() {
            @Override
            public void OnSuccess() {
                Toast.makeText(Visualization.this,"权限请求成功",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void OnFail(List<String> noPermissions) {
                Toast.makeText(Visualization.this,"权限请求失败",Toast.LENGTH_SHORT).show();
            }
        });


        onCreateProcess();                                                        //onCreate Process by BlunoLibrary

        serialBeginL(115200);                                                    //set the Uart Baudrate on BLE chip to 115200
        serialBeginR(115200);
        serialBeginV(115200);

        Date date = new Date();
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        USER_NAME = extras.getString("userName");
        PATIENT_ID = extras.getString("PATIENT_ID");
        DOCTOR_NAME = extras.getString("DOCTOR_NAME");
        DOCTOR_ID = extras.getString("DOCTOR_ID");
        patientRecord.put("USER_NAME", USER_NAME);
        patientRecord.put("PATIENT_ID", PATIENT_ID);
        patientRecord.put("DOCTOR_NAME", DOCTOR_NAME);
        patientRecord.put("DOCTOR_ID", DOCTOR_ID);
        patientRecord.put("Date", formatter.format(date));

        deviceName = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1);
        deviceID = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1);

        Arrays.fill(r_data_double_arr, 0.0);
        Arrays.fill(l_data_double_arr, 0.0);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        String androidId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        Date today = new Date();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

        String currentDate = dateFormat.format(today);

        usersRef = mDatabase.child(currentDate+"_"+USER_NAME); // user name

        if (bluetoothManager == null) {
            // Bluetooth unavailable on this device :( tell the user
            Toast.makeText(getApplication(), "Bluetooth not available.", Toast.LENGTH_LONG).show(); // Replace context with your context instance.
            finish();
        }

        connectRightBtn = findViewById(R.id.connectRight_button);
        connectLeftBtn = findViewById(R.id.connectLeft_button);
//        startLeftBtn = (Button) findViewById(R.id.startLeft_button);
//        startRightBtn = (Button) findViewById(R.id.startRight_button);
        startBtn = (Button) findViewById(R.id.start_button);
//        startBtn.setClickable(false);

        coyText = (TextView) findViewById(R.id.t1);

        text = (TextView) findViewById(R.id.t1);
        userName = (TextView) findViewById(R.id.user_name);
        userName.setText(USER_NAME);

        heatMapRight = (HeatMap) findViewById(R.id.heatmapRight);
        heatMapLeft = (HeatMap) findViewById(R.id.heatmapLeft);

        //Set the range that you want the heat maps gradient to cover
        heatMapRight.setMinimum(0);
        heatMapRight.setMaximum(64);

        heatMapLeft.setMinimum(0);
        heatMapLeft.setMaximum(64);

        Map<String, Object> leftDataDict = new HashMap<>();
        Map<String, Object> rightDataDict = new HashMap<>();

        //make the colour gradient from yellow to red
        Map<Float, Integer> colorStops = new ArrayMap<>();
        colorStops.put(0.3f, 0xFFDACF03);
        colorStops.put(0.4f, 0xFFDA7203);
        colorStops.put(1.0f, 0xFFDA031C);
        heatMapRight.setColorStops(colorStops);
        heatMapRight.setRadius(180);

        heatMapLeft.setColorStops(colorStops);
        heatMapLeft.setRadius(180);

        for (int i = 0; i < ns_list.length; i++) {
            non_sensor_indeces.add(ns_list[i]);
        }

        sampleTask = new TimerTask() {
            @Override
            public void run() {
                sampleCount = 0;

                List<Double> list = new ArrayList<>();
                list.addAll(Arrays.asList(l_data_double_arr));
                list.addAll(Arrays.asList(r_data_double_arr));
                dataListPerSec.add(list);
                dataListPerSecLen++;
                System.out.println("dataListPerSec:"+dataListPerSec);
                System.out.println("dataListPerSecLen:"+dataListPerSecLen);

                sampleCount++;
                if (sampleCount >= NUM_SAMPLES) {
                    timer.cancel();
                }
            }
        };

        connectLeftBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(String.valueOf(isCheckedL), "isChecked: ");


                if (!isCheckedL) {
                    showBTDList(getBluetoothAdapterL());
//                        Log.d(getMAC(), "connectLeftBtn mac: ");
//                        connectDevice(mac);
                } else {
                    if (is_L_insole_connected)
                        if (is_L_insole_started)
                            left_insole_device_interface.stopInsole();
//                        startLeftBtn.setText("Start Left");
                    is_L_insole_started = false;
                    bluetoothManager.closeDevice(left_insole_device_interface);
                    is_L_insole_connected = false;
                    Toast.makeText(Visualization.this, "Left Insole Disconnected.", Toast.LENGTH_SHORT).show();

                }

            }
            public void showBTDList(BluetoothAdapter mBluetoothAdapter) {
                if (ActivityCompat.checkSelfPermission(Visualization.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    pairedDevices = mBluetoothAdapter.getBondedDevices();

                    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            Visualization.this,
                            R.layout.bluetooth_list);

                    if (pairedDevices.size() > 0) {
                        for (BluetoothDevice device : pairedDevices) {
                            String str = "已配對完成的裝置有 " + device.getName() + " " + device.getAddress() + "\n";
                            uid = device.getAddress();
                            Log.d("selectBTDevice: ", str);
                            bleDevice = device;
                            deviceName.add(str);//將以配對的裝置名稱儲存，並顯示於LIST清單中
                            deviceID.add(uid); //好像沒用到
                            adapter.add(str);
                        }

                        chooseBTD(adapter);


                    }
                    return;
                }

            }

            private void chooseBTD(ArrayAdapter<String> adapter) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(
                        Visualization.this);
                alertBuilder.setTitle("Choose a Bluetooth Device");

                alertBuilder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        });

                alertBuilder.setAdapter(adapter,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                System.out.println("device:"+deviceID);
                                System.out.println("id:"+id);
                                String strName = adapter.getItem(id);
                                choseID = deviceID.getItem(id);
                                Toast.makeText(Visualization.this, "Device:" + choseID, Toast.LENGTH_SHORT).show();
                                deviceName.clear();
//
                                //                                    connectBT(choseID);
                                connectDevice(choseID);

                            }
                        });
                alertBuilder.show();
            }


            private String connectBT(String choseID) throws IOException {
//		UUID uuid = UUID.fromString(_UUID); //藍芽模組UUID好像都是這樣

                if (pairedDevices != null) {
                    for (BluetoothDevice device : pairedDevices) {
                        bleDevice = device;
                        System.out.println("connectBT device:"+device);
                        System.out.println("connectBT device:"+device.getAddress());
                        System.out.println("connectBT device:"+choseID);
                        connectDevice(choseID);
                        if (device.getAddress().equals(choseID))
                            break;
                    }
                }

                return choseID;
            }


            public void connectDevice(String mac) {                bluetoothManager.openSerialDevice(mac)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::onConnected, this::onError);
            }

            private void onConnected(BluetoothSerialDevice connectedDevice) {
                Log.d(connectedDevice.getMac(), "onConnected: connectedDevice");
                left_insole_device_interface = connectedDevice.toSimpleDeviceInterface();
//
//                Runnable timerThread = new Runnable() {
//                    @Override
//                    public void run() {
//
//                    }
//                };
//                timerThread.run();

                left_insole_device_interface.setListeners(message -> onMessageReceived(message), this::onMessageSent, this::onError);
                left_insole_device_interface.stopInsole();

                is_L_insole_connected = true;
                Toast.makeText(getApplication(), "Connected to Left Insole.", Toast.LENGTH_SHORT).show();
                if(is_L_insole_connected && is_R_insole_connected) {
                    startBtn.setBackgroundResource(R.drawable.rounded_corner);
                    startBtn.setEnabled(true);
                }

                connectLeftBtn.setBackgroundResource(R.drawable.rounded_corner_gray);
                connectLeftBtn.setEnabled(true);
                connectLeftBtn.setText("DISCONNECT LEFT");
            }

            private void onMessageReceived(String message) {
                hasReceivedLData = true;
//                System.out.println("데이터받음");
                //store incoming bytes temporarily
                if (!is_L_insole_started) {
                    left_temp_bytes += message + " ";
                }
                //check whether the start_bytes exits in the temporary buffer
                if (!is_L_insole_started && left_temp_bytes.contains(start_bytes)) {
                    is_L_insole_started = true;
                    left_temp_bytes = "";
                }
                //if the start_bytes are found in the temporary buffer, start storing the incoming messages in the actual buffer
                if (is_L_insole_started) {
//                    int cnt = Collections.frequency(Arrays.asList(l_data_double_arr, 0.0));

                    left_data_len++;
                    if (left_data_len > 15) {

                        Runnable timerThread = new Runnable() {
                            @Override
                            public void run() {
                                left_sensor_data_count++;
                                if (!non_sensor_indeces.contains(left_sensor_data_count)) {
                                    l_data_double_arr[left_data_index] = Double.parseDouble(message);
//                            System.out.println("NON SENSOR INDEX:" + left_data_index + " "+ message);
                                    left_data_index++;
                                }
//                                System.out.println("l_data_double_arr: "+l_data_double_arr);
//                            writeData(l_data_double_arr, r_data_double_arr);

//                                synchronized (dataListPerSec) {
//                                    LDataListPerSec.add(Arrays.asList(l_data_double_arr));

                                    Runnable timerThread = new Runnable() {
                                        @Override
                                        public void run() {
                                            new Timer().scheduleAtFixedRate(sampleTask, 0, SAMPLE_RATE);

                                            }
                                        };
                                    timerThread.run();
                                // Schedule the task to run at a fixed rate


//                                    System.out.println("dataList[0] len:"+dataList.get(0).size());
//                                    System.out.println("dataList:"+dataList);

//                                    if(!hasReceivedRData)
//                                        r_data_double_arr[r_data_double_arr.length-1] = 0.0;
//                                }
//                                LDataListPerSec.add(l_data_double_arr);
//                                System.out.println("L데이터: "+LDataListPerSec.toString());



                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        if (left_data_len >= max_data_len + 15) {
                                            heatMapLeft.clearData();
                                            for (int i = 0; i < x_L.length; i++) {
                                                HeatMap.DataPoint point = new HeatMap.DataPoint(x_L[i], y[i], l_data_double_arr[i]);
                                                heatMapLeft.addData(point);
                                                heatMapLeft.forceRefresh();
                                            }
                                            Date date = new Date();
//                        leftDataDict.put(String.valueOf(formatter.format(date)),Arrays.toString(l_data_double_arr));
//                        LList.add(Arrays.toString(l_data_double_arr).replace("]", ""));
//                        Log.d("TAG", "onMessageReceived: " + LList.size());
//                        if (LList.size() == 1) {
//                            LListDict.add(LList);
//                        }
                                            left_package_count++;
                                            left_data_index = 0;
                                            left_sensor_data_count = 0;
                                            left_data_len = 0;
                                            is_L_insole_started = false;
                                        }
                                    }
                                });

                            }
                        };
                        timerThread.run();


                    }

                    // 1초마다 서버로 전송
//                    TimerTask task = new TimerTask() {
//                        @Override
//                        public void run() {
//
//                        }
//                    };
//
//                    new Timer().scheduleAtFixedRate(task, 0l, 1000);




//                    LList.add(Arrays.toString(l_data_double_arr).replace("]", ""));
//                    if (LList.size() == 1){
//                        LListDict.addAll(LList);
////                        Log.d(TAG, "onMessageReceived: " + LListDict.toString());
////                        LListDict.clear();
////                        LList.clear();
//                    }

                    //if the data length reach the max_data_length, release the buffer and invert the start flag
//                    Visualization.this.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            System.out.println("mLeScanCallback onLeScan run ");
//
//                            if (left_data_len >= max_data_len + 15) {
//                                heatMapLeft.clearData();
//                                for (int i = 0; i < x_L.length; i++) {
//                                    HeatMap.DataPoint point = new HeatMap.DataPoint(x_L[i], y[i], l_data_double_arr[i]);
//                                    heatMapLeft.addData(point);
//                                    heatMapLeft.forceRefresh();
//                                }
//                                Date date = new Date();
////                        leftDataDict.put(String.valueOf(formatter.format(date)),Arrays.toString(l_data_double_arr));
////                        LList.add(Arrays.toString(l_data_double_arr).replace("]", ""));
////                        Log.d("TAG", "onMessageReceived: " + LList.size());
////                        if (LList.size() == 1) {
////                            LListDict.add(LList);
////                        }
//                                left_package_count++;
//                                left_data_index = 0;
//                                left_sensor_data_count = 0;
//                                left_data_len = 0;
//                                is_L_insole_started = false;
//                            }
//
//                        }
//                    });


//                    Visualization.this.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            System.out.println("mLeScanCallback onLeScan run ");
//                            List<Double> list = Arrays.asList(l_data_double_arr);
//                            int count = Collections.frequency(list,0.0);
//                            Log.d(String.valueOf(count), "count: ");
//
//                            Log.d(Arrays.toString(l_data_double_arr), "onMessageReceived l_data_double_arr: ");
//                            if(count<89)
////                                writeData(l_data_double_arr);
//                                System.out.println("데이터");
//                                writeData(l_data_double_arr, r_data_double_arr);
//
//                        }
//                    });


                }
            }

            private void onMessageSent(String message) {
                // We sent splitedValue[] message! Handle it here.
//                Toast.makeText(getApplication(), "Sent splitedValue[] message! Message was: " + message, Toast.LENGTH_LONG).show(); // Replace context with your context instance.
            }

            private void onError(Throwable error) {
                // Handle the error
                Log.d(String.valueOf(error), "onError: ");
            }
        });

        connectRightBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(String.valueOf(is_R_insole_connected), "is_R_insole_connected: ");
                Log.d(String.valueOf(isCheckedR), "isChecked: ");
//                showBTDList(getBluetoothAdapterR());

                if (!isCheckedR) {
                    showBTDList(getBluetoothAdapterR());

//                    connectDevice(R_insole_mac);

                } else {
                    if (is_R_insole_connected) {
                        if (is_R_insole_started) {
                            right_insole_device_interface.stopInsole();
                            startRightBtn.setText("Start Right");
                            is_R_insole_started = false;
                        }
                        bluetoothManager.closeDevice(right_insole_device_interface);
                        is_R_insole_connected = false;
                        Toast.makeText(Visualization.this, "Right Insole Disconnected.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            public void showBTDList(BluetoothAdapter mBluetoothAdapter) {
                if (ActivityCompat.checkSelfPermission(Visualization.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    pairedDevices = mBluetoothAdapter.getBondedDevices();

                    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            Visualization.this,
                            R.layout.bluetooth_list);

                    if (pairedDevices.size() > 0) {
                        for (BluetoothDevice device : pairedDevices) {

                            String str = "已配對完成的裝置有 " + device.getName() + " " + device.getAddress() + "\n";
                            uid = device.getAddress();
                            Log.d("selectBTDevice: ", str);
                            bleDevice = device;
                            deviceName.add(str);//將以配對的裝置名稱儲存，並顯示於LIST清單中
                            deviceID.add(uid); //好像沒用到
                            adapter.add(str);
                        }

                        chooseBTD(adapter);


                    }
                    return;
                }

            }

            private void chooseBTD(ArrayAdapter<String> adapter) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(
                        Visualization.this);
                alertBuilder.setTitle("Choose a Bluetooth Device");

                alertBuilder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        });

                alertBuilder.setAdapter(adapter,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                System.out.println("device:"+deviceID);
                                System.out.println("id:"+id);
                                String strName = adapter.getItem(id);
                                choseID = deviceID.getItem(id);
                                Toast.makeText(Visualization.this, "Device:" + choseID, Toast.LENGTH_SHORT).show();
                                deviceName.clear();
//
                                //                                    connectBT(choseID);
                                connectDevice(choseID);
                            }
                        });
                alertBuilder.show();
            }


            private String connectBT(String choseID) throws IOException {
//		UUID uuid = UUID.fromString(_UUID); //藍芽模組UUID好像都是這樣

                if (pairedDevices != null) {
                    for (BluetoothDevice device : pairedDevices) {
                        bleDevice = device;
//                        System.out.println("connectBT device:"+device);
//                        System.out.println("connectBT device:"+device.getAddress());
                        System.out.println("connectBT device:"+choseID);
                        connectDevice(choseID);
                        if (device.getAddress().equals(choseID))
                            break;
                    }
                }

                return choseID;
            }


            //            @SuppressLint("CheckResult")
            private void connectDevice(String mac) {
                System.out.println("check mac:"+mac); // "20:17:12:04:19:37"
                bluetoothManager.openSerialDevice(mac) // R_insole_mac
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::onConnected, this::onError);
            }

            private void onConnected(BluetoothSerialDevice connectedDevice) {
                System.out.println("??????? connected");
                // You are now connected to this device!
                // Here you may want to retain an instance to your device:
                right_insole_device_interface = connectedDevice.toSimpleDeviceInterface();

                // Listen to bluetooth events
                right_insole_device_interface.setListeners(message -> onMessageReceived(message), this::onMessageSent, this::onError);
                right_insole_device_interface.stopInsole();

                is_R_insole_connected = true;
                Toast.makeText(getApplication(), "Connected to Right Insole.", Toast.LENGTH_SHORT).show();

                if(is_L_insole_connected && is_R_insole_connected) {
                    startBtn.setBackgroundResource(R.drawable.rounded_corner);
                    startBtn.setEnabled(true);
                }
                connectRightBtn.setBackgroundResource(R.drawable.rounded_corner_gray);
                connectRightBtn.setEnabled(true);
                connectRightBtn.setText("DISCONNECT RIGHT");
            }

            private void onMessageSent(String message) {
                // We sent splitedValue[] message! Handle it here.
            }

            private void onMessageReceived(String message) {
                hasReceivedRData = true;
                //store incoming bytes temporarily
                if (!is_R_insole_started) {
                    right_temp_bytes += message + " ";
                }
                //check whether the start_bytes exits in the temporary buffer
                if (!is_R_insole_started && right_temp_bytes.contains(start_bytes)) {
                    is_R_insole_started = true;
                    right_temp_bytes = "";
                }
                //if the start_bytes are found in the temporary buffer, start storing the incoming messages in the actual buffer
                if (is_R_insole_started) {
                    right_data_len++;
                    if (right_data_len > 15) {
                        Runnable timerThread = new Runnable() {
                            @Override
                            public void run() {
                                right_sensor_data_count++;
                                if (!non_sensor_indeces.contains(right_sensor_data_count)) {
                                    r_data_double_arr[right_data_index] = Double.parseDouble(message);
//                                System.out.println("NON SENSOR INDEX:" + right_data_index + " "+ message);
                                    right_data_index++;
//                            writeData(l_data_double_arr, r_data_double_arr);

                                    synchronized (dataListPerSec) {

                                        Timer timer = new Timer();

                                        // Schedule the task to run at a fixed rate
                                        timer.scheduleAtFixedRate(new TimerTask() {
                                            int sampleCount = 0;

                                            @Override
                                            public void run() {

                                                List<Double> list = new ArrayList<>();
                                                list.addAll(Arrays.asList(l_data_double_arr));
                                                list.addAll(Arrays.asList(r_data_double_arr));
                                                dataListPerSec.add(list);
                                                dataListPerSecLen++;
                                                sampleCount++;
                                                if (sampleCount >= NUM_SAMPLES) {
                                                    timer.cancel();
                                                }
                                            }
                                        }, 0, SAMPLE_RATE);




//                                        System.out.println("dataList[0] len:"+dataList.get(0).size());
//                                        System.out.println("dataList:"+dataList);

//                                        if(!hasReceivedLData)
//                                            l_data_double_arr[l_data_double_arr.length-1] = 0.0;
                                    }
//                                    System.out.println("R데이터: "+RDataListPerSec.toString());
//
                                }
                            }
                        };
                        timerThread.run();


                    }

                    LList.add(Arrays.toString(l_data_double_arr).replace("]", ""));
                    RList.add(Arrays.toString(r_data_double_arr).replace("[", ""));
//                    Log.d(RList.toString(), "RList: ");

                    if (RList.size()== 1 && LList.size() == 1){
                        ListData.addAll(Collections.singleton(LList.get(0)));
                        ListData.addAll(Collections.singleton(RList.get(0)));
                        LList.clear();
                        RList.clear();
                        if (ListData.size() == 100){
                            LListDict.clear();
                            LListDict.addAll(ListData);
                            ListData.clear();
                        }
                    }

                    //if the data length reach the max_data_length, release the buffer and invert the start flag

//                    ((Activity) mainContext).runOnUiThread(new Runnable() {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (right_data_len >= max_data_len + 15) {
                                heatMapRight.clearData();
                                for (int i = 0; i < x_R.length; i++) {
                                    HeatMap.DataPoint point = new HeatMap.DataPoint(x_R[i], y[i], r_data_double_arr[i]);
                                    heatMapRight.addData(point);
                                    heatMapRight.forceRefresh();
                                }
//                        Log.d(r_data_double_arr.toString(), "r_data_double_arr: ");

                                Date date = new Date();
//                            rightDataDict.put(String.valueOf(formatter.format(date)), Arrays.toString(r_data_double_arr));
//                        String list = Arrays.toString(customers.toArray()).replace("[", "").replace("]", "");
//                        RList.add(Arrays.toString(r_data_double_arr).replace("[", ""));
//                        if (RList.size() == 1) {
//                            RListDict.add(RList);
//                        }
//                        Log.d("TAG", "onMessageReceived: " + RList);
                                right_data_index = 0;
                                right_sensor_data_count = 0;
                                right_data_len = 0;
                                is_R_insole_started = false;
                            }
                        }
                    });


                }
            }

            private void onError(Throwable error) {
                Log.e(TAG, "onError: ", error);
                // Handle the error
            }
        });

/*
        startLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(String.valueOf(is_L_insole_connected), "is_L_insole_connected: ");

                if (is_L_insole_connected) {
                    if (is_L_insole_started) {
                        left_insole_device_interface.stopInsole();
                        is_L_insole_started = false;
                        startLeftBtn.setText("Start Left");
                        left_timer.cancel();

                    } else {
                        left_insole_device_interface.startInsole();
                        is_L_insole_started = true;
                        startLeftBtn.setText("Stop Left");

                        left_timer = new Timer(); // At this line a new Thread will be created
                        left_timer.scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
                                //DO YOUR THINGS
                                runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {

//                                        String ListDataDicts = ListData.toString();
//                                        Log.d("TAG", "onMessageReceived: " + LListDict.size());

//                                        ListData.addAll(LListDict);
//                                        ListData.addAll(RListDict);
                                        String ListDataDicts = LListDict.toString();
//                                        LeftData(ListDataDicts.replace("],", "];"), "Left_Insole");
//
//                                        Log.d(TAG, "jinkatama: " + ListDataDicts);
//
//                                        ListData.clear();
//                                        LListDict.clear();
//                                        RListDict.clear();
//                                        LList.clear();
//                                        RList.clear();

                                        int n_L=0;
                                        int n_R=0;
                                        for (int i = 0; i < 89; i++) {
                                            if(l_data_double_arr[i]>r_data_double_arr[i]){
                                                n_L=n_L+1;
                                            }
                                            else{
                                                n_R=n_R+1;
                                            }
                                        }
                                        if(n_L>n_R){
                                            if (active == false){
                                                active=true;
                                                Log.d(TAG, "ucokbaba: " + "Pertama Nyala");
                                                serialSendV("1");
                                            } else {
                                                Log.d(TAG, "ucokbaba: " + "Sudah Nyala");
                                            }
                                        } else if(n_L==n_R){
                                            Log.d(TAG, "ucokbaba: " + "FOG");
                                        }
                                        else{
                                            serialSendV("0");
                                            active=false;
                                        }


                                    }

                                });

                            }
                        }, 1000, 1000); // delay

//                        left_timer = new Timer(); // At this line a new Thread will be created
//                        left_timer.scheduleAtFixedRate(new TimerTask() {
//                            @Override
//                            public void run() {
//                                //DO YOUR THINGS
//                                runOnUiThread(new Runnable() {
//
//                                    @Override
//                                    public void run() {
//
//                                        ListData.addAll(Collections.singleton(LList.get(0)));
//                                        ListData.addAll(Collections.singleton(RList.get(0)));
//
////                                        ListData.addAll(LList);
////                                        ListData.addAll(RList);
//
////                                        ListData.add(Arrays.toString(l_data_double_arr).replace("]", ", ") + Arrays.toString(r_data_double_arr).replace("[", ""));
//
////                                        Log.d(TAG, "ucokbaba: " + ListData);
//                                        Log.d(TAG, "ucokbaba: " + LList.size());
//                                        Log.d(TAG, "ucokbaba: " + RList.size());
//
//                                        String ListDataDicts = ListData.toString();
//
//                                        Log.d(TAG, "ucokbaba: " + ListDataDicts);
//
//                                        String[] ListDatas = ListDataDicts.split(",");
//
//
//                                        try {
//                                            tflite = new Interpreter(loadModelFile(Visualization.this, modelFile));
//
//                                            String ListData0 = ListDatas[0].replace("[", ""); // 004
//                                            String ListData1 = ListDatas[1];
//                                            String ListData2 = ListDatas[2];
//                                            String ListData3 = ListDatas[3];
//                                            String ListData4 = ListDatas[4];
//                                            String ListData5 = ListDatas[5];
//                                            String ListData6 = ListDatas[6];
//                                            String ListData7 = ListDatas[7];
//                                            String ListData8 = ListDatas[8];
//                                            String ListData9 = ListDatas[9];
//                                            String ListData10 = ListDatas[10];
//                                            String ListData11 = ListDatas[11];
//                                            String ListData12 = ListDatas[12];
//                                            String ListData13 = ListDatas[13];
//                                            String ListData14 = ListDatas[14];
//                                            String ListData15 = ListDatas[15];
//                                            String ListData16 = ListDatas[16];
//                                            String ListData17 = ListDatas[17];
//                                            String ListData18 = ListDatas[18];
//                                            String ListData19 = ListDatas[19];
//                                            String ListData20 = ListDatas[20];
//                                            String ListData21 = ListDatas[21];
//                                            String ListData22 = ListDatas[22];
//                                            String ListData23 = ListDatas[23];
//                                            String ListData24 = ListDatas[24];
//                                            String ListData25 = ListDatas[25];
//                                            String ListData26 = ListDatas[26];
//                                            String ListData27 = ListDatas[27];
//                                            String ListData28 = ListDatas[28];
//                                            String ListData29 = ListDatas[29];
//                                            String ListData30 = ListDatas[30];
//                                            String ListData31 = ListDatas[31];
//                                            String ListData32 = ListDatas[32];
//                                            String ListData33 = ListDatas[33];
//                                            String ListData34 = ListDatas[34];
//                                            String ListData35 = ListDatas[35];
//                                            String ListData36 = ListDatas[36];
//                                            String ListData37 = ListDatas[37];
//                                            String ListData38 = ListDatas[38];
//                                            String ListData39 = ListDatas[39];
//                                            String ListData40 = ListDatas[40];
//                                            String ListData41 = ListDatas[41];
//                                            String ListData42 = ListDatas[42];
//                                            String ListData43 = ListDatas[43];
//                                            String ListData44 = ListDatas[44];
//                                            String ListData45 = ListDatas[45];
//                                            String ListData46 = ListDatas[46];
//                                            String ListData47 = ListDatas[47];
//                                            String ListData48 = ListDatas[48];
//                                            String ListData49 = ListDatas[49];
//                                            String ListData50 = ListDatas[50];
//                                            String ListData51 = ListDatas[51];
//                                            String ListData52 = ListDatas[52];
//                                            String ListData53 = ListDatas[53];
//                                            String ListData54 = ListDatas[54];
//                                            String ListData55 = ListDatas[55];
//                                            String ListData56 = ListDatas[56];
//                                            String ListData57 = ListDatas[57];
//                                            String ListData58 = ListDatas[58];
//                                            String ListData59 = ListDatas[59];
//                                            String ListData60 = ListDatas[60];
//                                            String ListData61 = ListDatas[61];
//                                            String ListData62 = ListDatas[62];
//                                            String ListData63 = ListDatas[63];
//                                            String ListData64 = ListDatas[64];
//                                            String ListData65 = ListDatas[65];
//                                            String ListData66 = ListDatas[66];
//                                            String ListData67 = ListDatas[67];
//                                            String ListData68 = ListDatas[68];
//                                            String ListData69 = ListDatas[69];
//                                            String ListData70 = ListDatas[70];
//                                            String ListData71 = ListDatas[71];
//                                            String ListData72 = ListDatas[72];
//                                            String ListData73 = ListDatas[73];
//                                            String ListData74 = ListDatas[74];
//                                            String ListData75 = ListDatas[75];
//                                            String ListData76 = ListDatas[76];
//                                            String ListData77 = ListDatas[77];
//                                            String ListData78 = ListDatas[78];
//                                            String ListData79 = ListDatas[79];
//                                            String ListData80 = ListDatas[80];
//                                            String ListData81 = ListDatas[81];
//                                            String ListData82 = ListDatas[82];
//                                            String ListData83 = ListDatas[83];
//                                            String ListData84 = ListDatas[84];
//                                            String ListData85 = ListDatas[85];
//                                            String ListData86 = ListDatas[86];
//                                            String ListData87 = ListDatas[87];
//                                            String ListData88 = ListDatas[88];
//                                            String ListData89 = ListDatas[89];
//                                            String ListData90 = ListDatas[90];
//                                            String ListData91 = ListDatas[91];
//                                            String ListData92 = ListDatas[92];
//                                            String ListData93 = ListDatas[93];
//                                            String ListData94 = ListDatas[94];
//                                            String ListData95 = ListDatas[95];
//                                            String ListData96 = ListDatas[96];
//                                            String ListData97 = ListDatas[97];
//                                            String ListData98 = ListDatas[98];
//                                            String ListData99 = ListDatas[99];
//                                            String ListData100 = ListDatas[100];
//                                            String ListData101 = ListDatas[101];
//                                            String ListData102 = ListDatas[102];
//                                            String ListData103 = ListDatas[103];
//                                            String ListData104 = ListDatas[104];
//                                            String ListData105 = ListDatas[105];
//                                            String ListData106 = ListDatas[106];
//                                            String ListData107 = ListDatas[107];
//                                            String ListData108 = ListDatas[108];
//                                            String ListData109 = ListDatas[109];
//                                            String ListData110 = ListDatas[110];
//                                            String ListData111 = ListDatas[111];
//                                            String ListData112 = ListDatas[112];
//                                            String ListData113 = ListDatas[113];
//                                            String ListData114 = ListDatas[114];
//                                            String ListData115 = ListDatas[115];
//                                            String ListData116 = ListDatas[116];
//                                            String ListData117 = ListDatas[117];
//                                            String ListData118 = ListDatas[118];
//                                            String ListData119 = ListDatas[119];
//                                            String ListData120 = ListDatas[120];
//                                            String ListData121 = ListDatas[121];
//                                            String ListData122 = ListDatas[122];
//                                            String ListData123 = ListDatas[123];
//                                            String ListData124 = ListDatas[124];
//                                            String ListData125 = ListDatas[125];
//                                            String ListData126 = ListDatas[126];
//                                            String ListData127 = ListDatas[127];
//                                            String ListData128 = ListDatas[128];
//                                            String ListData129 = ListDatas[129];
//                                            String ListData130 = ListDatas[130];
//                                            String ListData131 = ListDatas[131];
//                                            String ListData132 = ListDatas[132];
//                                            String ListData133 = ListDatas[133];
//                                            String ListData134 = ListDatas[134];
//                                            String ListData135 = ListDatas[135];
//                                            String ListData136 = ListDatas[136];
//                                            String ListData137 = ListDatas[137];
//                                            String ListData138 = ListDatas[138];
//                                            String ListData139 = ListDatas[139];
//                                            String ListData140 = ListDatas[140];
//                                            String ListData141 = ListDatas[141];
//                                            String ListData142 = ListDatas[142];
//                                            String ListData143 = ListDatas[143];
//                                            String ListData144 = ListDatas[144];
//                                            String ListData145 = ListDatas[145];
//                                            String ListData146 = ListDatas[146];
//                                            String ListData147 = ListDatas[147];
//                                            String ListData148 = ListDatas[148];
//                                            String ListData149 = ListDatas[149];
//                                            String ListData150 = ListDatas[150];
//                                            String ListData151 = ListDatas[151];
//                                            String ListData152 = ListDatas[152];
//                                            String ListData153 = ListDatas[153];
//                                            String ListData154 = ListDatas[154];
//                                            String ListData155 = ListDatas[155];
//                                            String ListData156 = ListDatas[156];
//                                            String ListData157 = ListDatas[157];
//                                            String ListData158 = ListDatas[158];
//                                            String ListData159 = ListDatas[159];
//                                            String ListData160 = ListDatas[160];
//                                            String ListData161 = ListDatas[161];
//                                            String ListData162 = ListDatas[162];
//                                            String ListData163 = ListDatas[163];
//                                            String ListData164 = ListDatas[164];
//                                            String ListData165 = ListDatas[165];
//                                            String ListData166 = ListDatas[166];
//                                            String ListData167 = ListDatas[167];
//                                            String ListData168 = ListDatas[168];
//                                            String ListData169 = ListDatas[169];
//                                            String ListData170 = ListDatas[170];
//                                            String ListData171 = ListDatas[171];
//                                            String ListData172 = ListDatas[172];
//                                            String ListData173 = ListDatas[173];
//                                            String ListData174 = ListDatas[174];
//                                            String ListData175 = ListDatas[175];
//                                            String ListData176 = ListDatas[176];
//                                            String ListData177 = ListDatas[177].replace("]", "");
//
//                                            HashMap<String, String> predResult = new HashMap<String, String>();
//
//                                            NumberFormat f = NumberFormat.getInstance();
//
////                                            double InsoleVal0 = f.parse(ListData0.replace("[", "")).doubleValue();
//                                            double InsoleVal0 = f.parse(ListData0).doubleValue();
//                                            double InsoleVal1 = f.parse(ListData1).doubleValue();
//                                            double InsoleVal2 = f.parse(ListData2).doubleValue();
//                                            double InsoleVal3 = f.parse(ListData3).doubleValue();
//                                            double InsoleVal4 = f.parse(ListData4).doubleValue();
//                                            double InsoleVal5 = f.parse(ListData5).doubleValue();
//                                            double InsoleVal6 = f.parse(ListData6).doubleValue();
//                                            double InsoleVal7 = f.parse(ListData7).doubleValue();
//                                            double InsoleVal8 = f.parse(ListData8).doubleValue();
//                                            double InsoleVal9 = f.parse(ListData9).doubleValue();
//                                            double InsoleVal10 = f.parse(ListData10).doubleValue();
//                                            double InsoleVal11 = f.parse(ListData11).doubleValue();
//                                            double InsoleVal12 = f.parse(ListData12).doubleValue();
//                                            double InsoleVal13 = f.parse(ListData13).doubleValue();
//                                            double InsoleVal14 = f.parse(ListData14).doubleValue();
//                                            double InsoleVal15 = f.parse(ListData15).doubleValue();
//                                            double InsoleVal16 = f.parse(ListData16).doubleValue();
//                                            double InsoleVal17 = f.parse(ListData17).doubleValue();
//                                            double InsoleVal18 = f.parse(ListData18).doubleValue();
//                                            double InsoleVal19 = f.parse(ListData19).doubleValue();
//                                            double InsoleVal20 = f.parse(ListData20).doubleValue();
//                                            double InsoleVal21 = f.parse(ListData21).doubleValue();
//                                            double InsoleVal22 = f.parse(ListData22).doubleValue();
//                                            double InsoleVal23 = f.parse(ListData23).doubleValue();
//                                            double InsoleVal24 = f.parse(ListData24).doubleValue();
//                                            double InsoleVal25 = f.parse(ListData25).doubleValue();
//                                            double InsoleVal26 = f.parse(ListData26).doubleValue();
//                                            double InsoleVal27 = f.parse(ListData27).doubleValue();
//                                            double InsoleVal28 = f.parse(ListData28).doubleValue();
//                                            double InsoleVal29 = f.parse(ListData29).doubleValue();
//                                            double InsoleVal30 = f.parse(ListData30).doubleValue();
//                                            double InsoleVal31 = f.parse(ListData31).doubleValue();
//                                            double InsoleVal32 = f.parse(ListData32).doubleValue();
//                                            double InsoleVal33 = f.parse(ListData33).doubleValue();
//                                            double InsoleVal34 = f.parse(ListData34).doubleValue();
//                                            double InsoleVal35 = f.parse(ListData35).doubleValue();
//                                            double InsoleVal36 = f.parse(ListData36).doubleValue();
//                                            double InsoleVal37 = f.parse(ListData37).doubleValue();
//                                            double InsoleVal38 = f.parse(ListData38).doubleValue();
//                                            double InsoleVal39 = f.parse(ListData39).doubleValue();
//                                            double InsoleVal40 = f.parse(ListData40).doubleValue();
//                                            double InsoleVal41 = f.parse(ListData41).doubleValue();
//                                            double InsoleVal42 = f.parse(ListData42).doubleValue();
//                                            double InsoleVal43 = f.parse(ListData43).doubleValue();
//                                            double InsoleVal44 = f.parse(ListData44).doubleValue();
//                                            double InsoleVal45 = f.parse(ListData45).doubleValue();
//                                            double InsoleVal46 = f.parse(ListData46).doubleValue();
//                                            double InsoleVal47 = f.parse(ListData47).doubleValue();
//                                            double InsoleVal48 = f.parse(ListData48).doubleValue();
//                                            double InsoleVal49 = f.parse(ListData49).doubleValue();
//                                            double InsoleVal50 = f.parse(ListData50).doubleValue();
//                                            double InsoleVal51 = f.parse(ListData51).doubleValue();
//                                            double InsoleVal52 = f.parse(ListData52).doubleValue();
//                                            double InsoleVal53 = f.parse(ListData53).doubleValue();
//                                            double InsoleVal54 = f.parse(ListData54).doubleValue();
//                                            double InsoleVal55 = f.parse(ListData55).doubleValue();
//                                            double InsoleVal56 = f.parse(ListData56).doubleValue();
//                                            double InsoleVal57 = f.parse(ListData57).doubleValue();
//                                            double InsoleVal58 = f.parse(ListData58).doubleValue();
//                                            double InsoleVal59 = f.parse(ListData59).doubleValue();
//                                            double InsoleVal60 = f.parse(ListData60).doubleValue();
//                                            double InsoleVal61 = f.parse(ListData61).doubleValue();
//                                            double InsoleVal62 = f.parse(ListData62).doubleValue();
//                                            double InsoleVal63 = f.parse(ListData63).doubleValue();
//                                            double InsoleVal64 = f.parse(ListData64).doubleValue();
//                                            double InsoleVal65 = f.parse(ListData65).doubleValue();
//                                            double InsoleVal66 = f.parse(ListData66).doubleValue();
//                                            double InsoleVal67 = f.parse(ListData67).doubleValue();
//                                            double InsoleVal68 = f.parse(ListData68).doubleValue();
//                                            double InsoleVal69 = f.parse(ListData69).doubleValue();
//                                            double InsoleVal70 = f.parse(ListData70).doubleValue();
//                                            double InsoleVal71 = f.parse(ListData71).doubleValue();
//                                            double InsoleVal72 = f.parse(ListData72).doubleValue();
//                                            double InsoleVal73 = f.parse(ListData73).doubleValue();
//                                            double InsoleVal74 = f.parse(ListData74).doubleValue();
//                                            double InsoleVal75 = f.parse(ListData75).doubleValue();
//                                            double InsoleVal76 = f.parse(ListData76).doubleValue();
//                                            double InsoleVal77 = f.parse(ListData77).doubleValue();
//                                            double InsoleVal78 = f.parse(ListData78).doubleValue();
//                                            double InsoleVal79 = f.parse(ListData79).doubleValue();
//                                            double InsoleVal80 = f.parse(ListData80).doubleValue();
//                                            double InsoleVal81 = f.parse(ListData81).doubleValue();
//                                            double InsoleVal82 = f.parse(ListData82).doubleValue();
//                                            double InsoleVal83 = f.parse(ListData83).doubleValue();
//                                            double InsoleVal84 = f.parse(ListData84).doubleValue();
//                                            double InsoleVal85 = f.parse(ListData85).doubleValue();
//                                            double InsoleVal86 = f.parse(ListData86).doubleValue();
//                                            double InsoleVal87 = f.parse(ListData87).doubleValue();
//                                            double InsoleVal88 = f.parse(ListData88).doubleValue();
//                                            double InsoleVal89 = f.parse(ListData89).doubleValue();
//                                            double InsoleVal90 = f.parse(ListData90).doubleValue();
//                                            double InsoleVal91 = f.parse(ListData91).doubleValue();
//                                            double InsoleVal92 = f.parse(ListData92).doubleValue();
//                                            double InsoleVal93 = f.parse(ListData93).doubleValue();
//                                            double InsoleVal94 = f.parse(ListData94).doubleValue();
//                                            double InsoleVal95 = f.parse(ListData95).doubleValue();
//                                            double InsoleVal96 = f.parse(ListData96).doubleValue();
//                                            double InsoleVal97 = f.parse(ListData97).doubleValue();
//                                            double InsoleVal98 = f.parse(ListData98).doubleValue();
//                                            double InsoleVal99 = f.parse(ListData99).doubleValue();
//                                            double InsoleVal100 = f.parse(ListData100).doubleValue();
//                                            double InsoleVal101 = f.parse(ListData101).doubleValue();
//                                            double InsoleVal102 = f.parse(ListData102).doubleValue();
//                                            double InsoleVal103 = f.parse(ListData103).doubleValue();
//                                            double InsoleVal104 = f.parse(ListData104).doubleValue();
//                                            double InsoleVal105 = f.parse(ListData105).doubleValue();
//                                            double InsoleVal106 = f.parse(ListData106).doubleValue();
//                                            double InsoleVal107 = f.parse(ListData107).doubleValue();
//                                            double InsoleVal108 = f.parse(ListData108).doubleValue();
//                                            double InsoleVal109 = f.parse(ListData109).doubleValue();
//                                            double InsoleVal110 = f.parse(ListData110).doubleValue();
//                                            double InsoleVal111 = f.parse(ListData111).doubleValue();
//                                            double InsoleVal112 = f.parse(ListData112).doubleValue();
//                                            double InsoleVal113 = f.parse(ListData113).doubleValue();
//                                            double InsoleVal114 = f.parse(ListData114).doubleValue();
//                                            double InsoleVal115 = f.parse(ListData115).doubleValue();
//                                            double InsoleVal116 = f.parse(ListData116).doubleValue();
//                                            double InsoleVal117 = f.parse(ListData117).doubleValue();
//                                            double InsoleVal118 = f.parse(ListData118).doubleValue();
//                                            double InsoleVal119 = f.parse(ListData119).doubleValue();
//                                            double InsoleVal120 = f.parse(ListData120).doubleValue();
//                                            double InsoleVal121 = f.parse(ListData121).doubleValue();
//                                            double InsoleVal122 = f.parse(ListData122).doubleValue();
//                                            double InsoleVal123 = f.parse(ListData123).doubleValue();
//                                            double InsoleVal124 = f.parse(ListData124).doubleValue();
//                                            double InsoleVal125 = f.parse(ListData125).doubleValue();
//                                            double InsoleVal126 = f.parse(ListData126).doubleValue();
//                                            double InsoleVal127 = f.parse(ListData127).doubleValue();
//                                            double InsoleVal128 = f.parse(ListData128).doubleValue();
//                                            double InsoleVal129 = f.parse(ListData129).doubleValue();
//                                            double InsoleVal130 = f.parse(ListData130).doubleValue();
//                                            double InsoleVal131 = f.parse(ListData131).doubleValue();
//                                            double InsoleVal132 = f.parse(ListData132).doubleValue();
//                                            double InsoleVal133 = f.parse(ListData133).doubleValue();
//                                            double InsoleVal134 = f.parse(ListData134).doubleValue();
//                                            double InsoleVal135 = f.parse(ListData135).doubleValue();
//                                            double InsoleVal136 = f.parse(ListData136).doubleValue();
//                                            double InsoleVal137 = f.parse(ListData137).doubleValue();
//                                            double InsoleVal138 = f.parse(ListData138).doubleValue();
//                                            double InsoleVal139 = f.parse(ListData139).doubleValue();
//                                            double InsoleVal140 = f.parse(ListData140).doubleValue();
//                                            double InsoleVal141 = f.parse(ListData141).doubleValue();
//                                            double InsoleVal142 = f.parse(ListData142).doubleValue();
//                                            double InsoleVal143 = f.parse(ListData143).doubleValue();
//                                            double InsoleVal144 = f.parse(ListData144).doubleValue();
//                                            double InsoleVal145 = f.parse(ListData145).doubleValue();
//                                            double InsoleVal146 = f.parse(ListData146).doubleValue();
//                                            double InsoleVal147 = f.parse(ListData147).doubleValue();
//                                            double InsoleVal148 = f.parse(ListData148).doubleValue();
//                                            double InsoleVal149 = f.parse(ListData149).doubleValue();
//                                            double InsoleVal150 = f.parse(ListData150).doubleValue();
//                                            double InsoleVal151 = f.parse(ListData151).doubleValue();
//                                            double InsoleVal152 = f.parse(ListData152).doubleValue();
//                                            double InsoleVal153 = f.parse(ListData153).doubleValue();
//                                            double InsoleVal154 = f.parse(ListData154).doubleValue();
//                                            double InsoleVal155 = f.parse(ListData155).doubleValue();
//                                            double InsoleVal156 = f.parse(ListData156).doubleValue();
//                                            double InsoleVal157 = f.parse(ListData157).doubleValue();
//                                            double InsoleVal158 = f.parse(ListData158).doubleValue();
//                                            double InsoleVal159 = f.parse(ListData159).doubleValue();
//                                            double InsoleVal160 = f.parse(ListData160).doubleValue();
//                                            double InsoleVal161 = f.parse(ListData161).doubleValue();
//                                            double InsoleVal162 = f.parse(ListData162).doubleValue();
//                                            double InsoleVal163 = f.parse(ListData163).doubleValue();
//                                            double InsoleVal164 = f.parse(ListData164).doubleValue();
//                                            double InsoleVal165 = f.parse(ListData165).doubleValue();
//                                            double InsoleVal166 = f.parse(ListData166).doubleValue();
//                                            double InsoleVal167 = f.parse(ListData167).doubleValue();
//                                            double InsoleVal168 = f.parse(ListData168).doubleValue();
//                                            double InsoleVal169 = f.parse(ListData169).doubleValue();
//                                            double InsoleVal170 = f.parse(ListData170).doubleValue();
//                                            double InsoleVal171 = f.parse(ListData171).doubleValue();
//                                            double InsoleVal172 = f.parse(ListData172).doubleValue();
//                                            double InsoleVal173 = f.parse(ListData173).doubleValue();
//                                            double InsoleVal174 = f.parse(ListData174).doubleValue();
//                                            double InsoleVal175 = f.parse(ListData175).doubleValue();
//                                            double InsoleVal176 = f.parse(ListData176).doubleValue();
//                                            double InsoleVal177 = f.parse(ListData177).doubleValue();
////                                            double InsoleVal177 = f.parse(ListData177.replace("]", "")).doubleValue();
//
//
////                    float[][][] inp = new float[1][2][1];
//                                            float[][][] inp = {
//                                                    {
//                                                            {(float) InsoleVal0}, {(float) InsoleVal1}, {(float) InsoleVal2}, {(float) InsoleVal3}, {(float) InsoleVal4},
//                                                            {(float) InsoleVal5}, {(float) InsoleVal6}, {(float) InsoleVal7}, {(float) InsoleVal8}, {(float) InsoleVal9},
//                                                            {(float) InsoleVal10}, {(float) InsoleVal11}, {(float) InsoleVal12}, {(float) InsoleVal13}, {(float) InsoleVal14},
//                                                            {(float) InsoleVal15}, {(float) InsoleVal16}, {(float) InsoleVal17}, {(float) InsoleVal18}, {(float) InsoleVal19},
//                                                            {(float) InsoleVal20}, {(float) InsoleVal21}, {(float) InsoleVal22}, {(float) InsoleVal23}, {(float) InsoleVal24},
//                                                            {(float) InsoleVal25}, {(float) InsoleVal26}, {(float) InsoleVal27}, {(float) InsoleVal28}, {(float) InsoleVal29},
//                                                            {(float) InsoleVal30}, {(float) InsoleVal31}, {(float) InsoleVal32}, {(float) InsoleVal33}, {(float) InsoleVal34},
//                                                            {(float) InsoleVal35}, {(float) InsoleVal36}, {(float) InsoleVal37}, {(float) InsoleVal38}, {(float) InsoleVal39},
//                                                            {(float) InsoleVal40}, {(float) InsoleVal41}, {(float) InsoleVal42}, {(float) InsoleVal43}, {(float) InsoleVal44},
//                                                            {(float) InsoleVal45}, {(float) InsoleVal46}, {(float) InsoleVal47}, {(float) InsoleVal48}, {(float) InsoleVal49},
//                                                            {(float) InsoleVal50}, {(float) InsoleVal51}, {(float) InsoleVal52}, {(float) InsoleVal53}, {(float) InsoleVal54},
//                                                            {(float) InsoleVal55}, {(float) InsoleVal56}, {(float) InsoleVal57}, {(float) InsoleVal58}, {(float) InsoleVal59},
//                                                            {(float) InsoleVal60}, {(float) InsoleVal61}, {(float) InsoleVal62}, {(float) InsoleVal63}, {(float) InsoleVal64},
//                                                            {(float) InsoleVal65}, {(float) InsoleVal66}, {(float) InsoleVal67}, {(float) InsoleVal68}, {(float) InsoleVal69},
//                                                            {(float) InsoleVal70}, {(float) InsoleVal71}, {(float) InsoleVal72}, {(float) InsoleVal73}, {(float) InsoleVal74},
//                                                            {(float) InsoleVal75}, {(float) InsoleVal76}, {(float) InsoleVal77}, {(float) InsoleVal78}, {(float) InsoleVal79},
//                                                            {(float) InsoleVal80}, {(float) InsoleVal81}, {(float) InsoleVal82}, {(float) InsoleVal83}, {(float) InsoleVal84},
//                                                            {(float) InsoleVal85}, {(float) InsoleVal86}, {(float) InsoleVal87}, {(float) InsoleVal88}, {(float) InsoleVal89},
//                                                            {(float) InsoleVal90}, {(float) InsoleVal91}, {(float) InsoleVal92}, {(float) InsoleVal93}, {(float) InsoleVal94},
//                                                            {(float) InsoleVal95}, {(float) InsoleVal96}, {(float) InsoleVal97}, {(float) InsoleVal98}, {(float) InsoleVal99},
//                                                            {(float) InsoleVal100}, {(float) InsoleVal101}, {(float) InsoleVal102}, {(float) InsoleVal103}, {(float) InsoleVal104},
//                                                            {(float) InsoleVal105}, {(float) InsoleVal106}, {(float) InsoleVal107}, {(float) InsoleVal108}, {(float) InsoleVal109},
//                                                            {(float) InsoleVal110}, {(float) InsoleVal111}, {(float) InsoleVal112}, {(float) InsoleVal113}, {(float) InsoleVal114},
//                                                            {(float) InsoleVal115}, {(float) InsoleVal116}, {(float) InsoleVal117}, {(float) InsoleVal118}, {(float) InsoleVal119},
//                                                            {(float) InsoleVal120}, {(float) InsoleVal121}, {(float) InsoleVal122}, {(float) InsoleVal123}, {(float) InsoleVal124},
//                                                            {(float) InsoleVal125}, {(float) InsoleVal126}, {(float) InsoleVal127}, {(float) InsoleVal128}, {(float) InsoleVal129},
//                                                            {(float) InsoleVal130}, {(float) InsoleVal131}, {(float) InsoleVal132}, {(float) InsoleVal133}, {(float) InsoleVal134},
//                                                            {(float) InsoleVal135}, {(float) InsoleVal136}, {(float) InsoleVal137}, {(float) InsoleVal138}, {(float) InsoleVal139},
//                                                            {(float) InsoleVal140}, {(float) InsoleVal141}, {(float) InsoleVal142}, {(float) InsoleVal143}, {(float) InsoleVal144},
//                                                            {(float) InsoleVal145}, {(float) InsoleVal146}, {(float) InsoleVal147}, {(float) InsoleVal148}, {(float) InsoleVal149},
//                                                            {(float) InsoleVal150}, {(float) InsoleVal151}, {(float) InsoleVal152}, {(float) InsoleVal153}, {(float) InsoleVal154},
//                                                            {(float) InsoleVal155}, {(float) InsoleVal156}, {(float) InsoleVal157}, {(float) InsoleVal158}, {(float) InsoleVal159},
//                                                            {(float) InsoleVal160}, {(float) InsoleVal161}, {(float) InsoleVal162}, {(float) InsoleVal163}, {(float) InsoleVal164},
//                                                            {(float) InsoleVal165}, {(float) InsoleVal166}, {(float) InsoleVal167}, {(float) InsoleVal168}, {(float) InsoleVal169},
//                                                            {(float) InsoleVal170}, {(float) InsoleVal171}, {(float) InsoleVal172}, {(float) InsoleVal173}, {(float) InsoleVal174},
//                                                            {(float) InsoleVal175}, {(float) InsoleVal176}, {(float) InsoleVal177}
//                                                    }
//                                            };
//
//                                            float[][] out = new float[][]{{0, 0}};
////                                            float[][] out = new float[][]{{0, 0, 0, 0, 0, 0}};
//                                            Log.d("TAG", "ancok: " + Arrays.deepToString(inp));
////                    Log.d("TAG", "ancok1: " + Arrays.deepToString(test));
//                                            tflite.run(inp, out);
//
//
//
//                                            float res1 = out[0][0];
//                                            float res2 = out[0][1];
////                                            float res3 = out[0][2];
////                                            float res4 = out[0][3];
////                                            float res5 = out[0][4];
////                                            float res6 = out[0][5];
//
//
////                                            ArrayList<String> spinnerArrayList;
////                                        spinnerArrayList = new ArrayList<String>();
////                                        spinnerArrayList.add(a);
//
////                                            int maxAt = 0;
////
////                                            for (int i = 0; i < out[0].length; i++) {
////                                                maxAt = out[0][i] > out[0][maxAt] ? i : maxAt;
//////                                                Log.d(TAG, "kol: " + out[0][i]);
////                                            }
////
////                                            int a = maxAt+1;
////
////                                            if (a == 1){
////                                                predResult.put("Status ", "HSR");
////                                            }else if(a == 4){
////                                                predResult.put("Status ", "HSL");
////                                            }
////                                            Log.d(TAG, "kol: " + a);
////
////                                            Log.d(TAG, "kol: " + predResult);
////
////                                            Toast.makeText(Visualization.this, predResult.toString(), Toast.LENGTH_SHORT).show();
////
////                                            predResult.clear();
////
////                                            Log.d(TAG, "kol: " + predResult);
//////
////                                            List big = new ArrayList();
////                                            big.add(res1);
////                                            big.add(res2);
////                                            big.add(res3);
////                                            big.add(res4);
////                                            big.add(res5);
////                                            big.add(res6);
//
////                                            String a = Collections.max(big).toString();
////                                            int a = Collections.max(big);
////                                            myList.indexOf(maxVal)
//
////                                            for (int i = 0; i < big.size(); i++) {
////                                                String[] splitedValue = big.get(i);
////                                                for (int j = 0; j < splitedValue.length; j++) {
//////                                                Log.i(i + " at ArrayIndex " + j + " at splitedIndex Value is >> ", splitedValue[j]);
////                                                }
////                                            }
//
////                                            Log.d(TAG, "kol: " + a);
////                                            big.clear();
//
////                                            List b = Arrays.
//                                            if (res2>0.0) {
////                                                predResult.clear();
////                                                predResult.put("Status ", "FOG");
//                                                text.setText("FOG");
////                                                serialSendV(String.valueOf("60"));
////                                                is_sound_on = true;
////                                                Toast.makeText(Visualization.this, "FOG", Toast.LENGTH_SHORT).show();
////                                                predResult.clear();
//                                            } else {
//                                                text.setText("NORMAL");
//                                                is_sound_on = false;
//                                                serialSendV("s");
//                                                toneGen1.stopTone();
////                                                predResult.clear();
////                                                predResult.put("Status ", "NORMAL");
////                                                Toast.makeText(Visualization.this, "NORMAL", Toast.LENGTH_SHORT).show();
////                                                predResult.clear();
//                                            }
//                                            serialSendV("\n");
//
////                                            Log.d("TAG", "ucokbaba: " + res1 + " " + res2);
//
//
////                                            inPut.setText("Input : " + Arrays.deepToString(inp));
////                                            outPut.setText("Prediction : " + predResult + " " + resL + " " + resR);
//
//                                            Log.d(TAG, "ucokbaba: " + Arrays.deepToString(inp));
//                                            Log.d(TAG, "ucokbaba: " + predResult + " " + res1 + " " + res2);
//
////                                            Toast.makeText(Visualization.this, predResult.toString(), Toast.LENGTH_SHORT).show();
////
////                                            predResult.clear();
////                                            Log.d(TAG, "ucokbaba: " + predResult + " " + res1 + " " + res2);
//
////                    showDialog(MainActivity.this,predResult);
//
//
//                                        } catch (IOException | ParseException e) {
//                                            e.printStackTrace();
//                                        }
//
//                                        ListData.clear();
//                                        LList.clear();
//                                        RList.clear();
//
//
////                                        ArrayList<String> spinnerArrayList;
////                                        spinnerArrayList = new ArrayList<String>();
////                                        spinnerArrayList.add(a);
//
////                                        Log.d(TAG, "soak: " + spinnerArrayList.size());
////                                        Log.d(TAG, "soak: " + spinnerArrayList.toString());
//
////                                        -------------------------------------------------------
////                                        for (int i = 0; i < spinnerArrayList.size(); i++) {
////                                            String[] splitedValue = spinnerArrayList.get(i).split(",");
////                                            for (int j = 0; j < splitedValue.length; j++) {
//////                                                Log.i(i + " at ArrayIndex " + j + " at splitedIndex Value is >> ", splitedValue[j]);
//////                                            Log.d("TAG", "Cok: " + j +" : "+ splitedValue[j]);
////                                                try {
////                                                    tflite = new Interpreter(loadModelFile(Visualization.this, modelFile));
//////
////                                                    HashMap<String, String> predResult = new HashMap<String, String>();
//////
////                                                    NumberFormat f = NumberFormat.getInstance();
//////
////                                                    double InsoleVal0 = f.parse(splitedValue[0].replace("[", "")).doubleValue();
//////                                                    double InsoleVal0 = f.parse(splitedValue[0]).doubleValue();
////                                                    double InsoleVal1 = f.parse(splitedValue[1]).doubleValue();
////                                                    double InsoleVal2 = f.parse(splitedValue[2]).doubleValue();
////                                                    double InsoleVal3 = f.parse(splitedValue[3]).doubleValue();
////                                                    double InsoleVal4 = f.parse(splitedValue[4]).doubleValue();
////                                                    double InsoleVal5 = f.parse(splitedValue[5]).doubleValue();
////                                                    double InsoleVal6 = f.parse(splitedValue[6]).doubleValue();
////                                                    double InsoleVal7 = f.parse(splitedValue[7]).doubleValue();
////                                                    double InsoleVal8 = f.parse(splitedValue[8]).doubleValue();
////                                                    double InsoleVal9 = f.parse(splitedValue[9]).doubleValue();
////                                                    double InsoleVal10 = f.parse(splitedValue[10]).doubleValue();
////                                                    double InsoleVal11 = f.parse(splitedValue[11]).doubleValue();
////                                                    double InsoleVal12 = f.parse(splitedValue[12]).doubleValue();
////                                                    double InsoleVal13 = f.parse(splitedValue[13]).doubleValue();
////                                                    double InsoleVal14 = f.parse(splitedValue[14]).doubleValue();
////                                                    double InsoleVal15 = f.parse(splitedValue[15]).doubleValue();
////                                                    double InsoleVal16 = f.parse(splitedValue[16]).doubleValue();
////                                                    double InsoleVal17 = f.parse(splitedValue[17]).doubleValue();
////                                                    double InsoleVal18 = f.parse(splitedValue[18]).doubleValue();
////                                                    double InsoleVal19 = f.parse(splitedValue[19]).doubleValue();
////                                                    double InsoleVal20 = f.parse(splitedValue[20]).doubleValue();
////                                                    double InsoleVal21 = f.parse(splitedValue[21]).doubleValue();
////                                                    double InsoleVal22 = f.parse(splitedValue[22]).doubleValue();
////                                                    double InsoleVal23 = f.parse(splitedValue[23]).doubleValue();
////                                                    double InsoleVal24 = f.parse(splitedValue[24]).doubleValue();
////                                                    double InsoleVal25 = f.parse(splitedValue[25]).doubleValue();
////                                                    double InsoleVal26 = f.parse(splitedValue[26]).doubleValue();
////                                                    double InsoleVal27 = f.parse(splitedValue[27]).doubleValue();
////                                                    double InsoleVal28 = f.parse(splitedValue[28]).doubleValue();
////                                                    double InsoleVal29 = f.parse(splitedValue[29]).doubleValue();
////                                                    double InsoleVal30 = f.parse(splitedValue[30]).doubleValue();
////                                                    double InsoleVal31 = f.parse(splitedValue[31]).doubleValue();
////                                                    double InsoleVal32 = f.parse(splitedValue[32]).doubleValue();
////                                                    double InsoleVal33 = f.parse(splitedValue[33]).doubleValue();
////                                                    double InsoleVal34 = f.parse(splitedValue[34]).doubleValue();
////                                                    double InsoleVal35 = f.parse(splitedValue[35]).doubleValue();
////                                                    double InsoleVal36 = f.parse(splitedValue[36]).doubleValue();
////                                                    double InsoleVal37 = f.parse(splitedValue[37]).doubleValue();
////                                                    double InsoleVal38 = f.parse(splitedValue[38]).doubleValue();
////                                                    double InsoleVal39 = f.parse(splitedValue[39]).doubleValue();
////                                                    double InsoleVal40 = f.parse(splitedValue[40]).doubleValue();
////                                                    double InsoleVal41 = f.parse(splitedValue[41]).doubleValue();
////                                                    double InsoleVal42 = f.parse(splitedValue[42]).doubleValue();
////                                                    double InsoleVal43 = f.parse(splitedValue[43]).doubleValue();
////                                                    double InsoleVal44 = f.parse(splitedValue[44]).doubleValue();
////                                                    double InsoleVal45 = f.parse(splitedValue[45]).doubleValue();
////                                                    double InsoleVal46 = f.parse(splitedValue[46]).doubleValue();
////                                                    double InsoleVal47 = f.parse(splitedValue[47]).doubleValue();
////                                                    double InsoleVal48 = f.parse(splitedValue[48]).doubleValue();
////                                                    double InsoleVal49 = f.parse(splitedValue[49]).doubleValue();
////                                                    double InsoleVal50 = f.parse(splitedValue[50]).doubleValue();
////                                                    double InsoleVal51 = f.parse(splitedValue[51]).doubleValue();
////                                                    double InsoleVal52 = f.parse(splitedValue[52]).doubleValue();
////                                                    double InsoleVal53 = f.parse(splitedValue[53]).doubleValue();
////                                                    double InsoleVal54 = f.parse(splitedValue[54]).doubleValue();
////                                                    double InsoleVal55 = f.parse(splitedValue[55]).doubleValue();
////                                                    double InsoleVal56 = f.parse(splitedValue[56]).doubleValue();
////                                                    double InsoleVal57 = f.parse(splitedValue[57]).doubleValue();
////                                                    double InsoleVal58 = f.parse(splitedValue[58]).doubleValue();
////                                                    double InsoleVal59 = f.parse(splitedValue[59]).doubleValue();
////                                                    double InsoleVal60 = f.parse(splitedValue[60]).doubleValue();
////                                                    double InsoleVal61 = f.parse(splitedValue[61]).doubleValue();
////                                                    double InsoleVal62 = f.parse(splitedValue[62]).doubleValue();
////                                                    double InsoleVal63 = f.parse(splitedValue[63]).doubleValue();
////                                                    double InsoleVal64 = f.parse(splitedValue[64]).doubleValue();
////                                                    double InsoleVal65 = f.parse(splitedValue[65]).doubleValue();
////                                                    double InsoleVal66 = f.parse(splitedValue[66]).doubleValue();
////                                                    double InsoleVal67 = f.parse(splitedValue[67]).doubleValue();
////                                                    double InsoleVal68 = f.parse(splitedValue[68]).doubleValue();
////                                                    double InsoleVal69 = f.parse(splitedValue[69]).doubleValue();
////                                                    double InsoleVal70 = f.parse(splitedValue[70]).doubleValue();
////                                                    double InsoleVal71 = f.parse(splitedValue[71]).doubleValue();
////                                                    double InsoleVal72 = f.parse(splitedValue[72]).doubleValue();
////                                                    double InsoleVal73 = f.parse(splitedValue[73]).doubleValue();
////                                                    double InsoleVal74 = f.parse(splitedValue[74]).doubleValue();
////                                                    double InsoleVal75 = f.parse(splitedValue[75]).doubleValue();
////                                                    double InsoleVal76 = f.parse(splitedValue[76]).doubleValue();
////                                                    double InsoleVal77 = f.parse(splitedValue[77]).doubleValue();
////                                                    double InsoleVal78 = f.parse(splitedValue[78]).doubleValue();
////                                                    double InsoleVal79 = f.parse(splitedValue[79]).doubleValue();
////                                                    double InsoleVal80 = f.parse(splitedValue[80]).doubleValue();
////                                                    double InsoleVal81 = f.parse(splitedValue[81]).doubleValue();
////                                                    double InsoleVal82 = f.parse(splitedValue[82]).doubleValue();
////                                                    double InsoleVal83 = f.parse(splitedValue[83]).doubleValue();
////                                                    double InsoleVal84 = f.parse(splitedValue[84]).doubleValue();
////                                                    double InsoleVal85 = f.parse(splitedValue[85]).doubleValue();
////                                                    double InsoleVal86 = f.parse(splitedValue[86]).doubleValue();
////                                                    double InsoleVal87 = f.parse(splitedValue[87]).doubleValue();
////                                                    double InsoleVal88 = f.parse(splitedValue[88]).doubleValue();
////                                                    double InsoleVal89 = f.parse(splitedValue[89]).doubleValue();
////                                                    double InsoleVal90 = f.parse(splitedValue[90]).doubleValue();
////                                                    double InsoleVal91 = f.parse(splitedValue[91]).doubleValue();
////                                                    double InsoleVal92 = f.parse(splitedValue[92]).doubleValue();
////                                                    double InsoleVal93 = f.parse(splitedValue[93]).doubleValue();
////                                                    double InsoleVal94 = f.parse(splitedValue[94]).doubleValue();
////                                                    double InsoleVal95 = f.parse(splitedValue[95]).doubleValue();
////                                                    double InsoleVal96 = f.parse(splitedValue[96]).doubleValue();
////                                                    double InsoleVal97 = f.parse(splitedValue[97]).doubleValue();
////                                                    double InsoleVal98 = f.parse(splitedValue[98]).doubleValue();
////                                                    double InsoleVal99 = f.parse(splitedValue[99]).doubleValue();
////                                                    double InsoleVal100 = f.parse(splitedValue[100]).doubleValue();
////                                                    double InsoleVal101 = f.parse(splitedValue[101]).doubleValue();
////                                                    double InsoleVal102 = f.parse(splitedValue[102]).doubleValue();
////                                                    double InsoleVal103 = f.parse(splitedValue[103]).doubleValue();
////                                                    double InsoleVal104 = f.parse(splitedValue[104]).doubleValue();
////                                                    double InsoleVal105 = f.parse(splitedValue[105]).doubleValue();
////                                                    double InsoleVal106 = f.parse(splitedValue[106]).doubleValue();
////                                                    double InsoleVal107 = f.parse(splitedValue[107]).doubleValue();
////                                                    double InsoleVal108 = f.parse(splitedValue[108]).doubleValue();
////                                                    double InsoleVal109 = f.parse(splitedValue[109]).doubleValue();
////                                                    double InsoleVal110 = f.parse(splitedValue[110]).doubleValue();
////                                                    double InsoleVal111 = f.parse(splitedValue[111]).doubleValue();
////                                                    double InsoleVal112 = f.parse(splitedValue[112]).doubleValue();
////                                                    double InsoleVal113 = f.parse(splitedValue[113]).doubleValue();
////                                                    double InsoleVal114 = f.parse(splitedValue[114]).doubleValue();
////                                                    double InsoleVal115 = f.parse(splitedValue[115]).doubleValue();
////                                                    double InsoleVal116 = f.parse(splitedValue[116]).doubleValue();
////                                                    double InsoleVal117 = f.parse(splitedValue[117]).doubleValue();
////                                                    double InsoleVal118 = f.parse(splitedValue[118]).doubleValue();
////                                                    double InsoleVal119 = f.parse(splitedValue[119]).doubleValue();
////                                                    double InsoleVal120 = f.parse(splitedValue[120]).doubleValue();
////                                                    double InsoleVal121 = f.parse(splitedValue[121]).doubleValue();
////                                                    double InsoleVal122 = f.parse(splitedValue[122]).doubleValue();
////                                                    double InsoleVal123 = f.parse(splitedValue[123]).doubleValue();
////                                                    double InsoleVal124 = f.parse(splitedValue[124]).doubleValue();
////                                                    double InsoleVal125 = f.parse(splitedValue[125]).doubleValue();
////                                                    double InsoleVal126 = f.parse(splitedValue[126]).doubleValue();
////                                                    double InsoleVal127 = f.parse(splitedValue[127]).doubleValue();
////                                                    double InsoleVal128 = f.parse(splitedValue[128]).doubleValue();
////                                                    double InsoleVal129 = f.parse(splitedValue[129]).doubleValue();
////                                                    double InsoleVal130 = f.parse(splitedValue[130]).doubleValue();
////                                                    double InsoleVal131 = f.parse(splitedValue[131]).doubleValue();
////                                                    double InsoleVal132 = f.parse(splitedValue[132]).doubleValue();
////                                                    double InsoleVal133 = f.parse(splitedValue[133]).doubleValue();
////                                                    double InsoleVal134 = f.parse(splitedValue[134]).doubleValue();
////                                                    double InsoleVal135 = f.parse(splitedValue[135]).doubleValue();
////                                                    double InsoleVal136 = f.parse(splitedValue[136]).doubleValue();
////                                                    double InsoleVal137 = f.parse(splitedValue[137]).doubleValue();
////                                                    double InsoleVal138 = f.parse(splitedValue[138]).doubleValue();
////                                                    double InsoleVal139 = f.parse(splitedValue[139]).doubleValue();
////                                                    double InsoleVal140 = f.parse(splitedValue[140]).doubleValue();
////                                                    double InsoleVal141 = f.parse(splitedValue[141]).doubleValue();
////                                                    double InsoleVal142 = f.parse(splitedValue[142]).doubleValue();
////                                                    double InsoleVal143 = f.parse(splitedValue[143]).doubleValue();
////                                                    double InsoleVal144 = f.parse(splitedValue[144]).doubleValue();
////                                                    double InsoleVal145 = f.parse(splitedValue[145]).doubleValue();
////                                                    double InsoleVal146 = f.parse(splitedValue[146]).doubleValue();
////                                                    double InsoleVal147 = f.parse(splitedValue[147]).doubleValue();
////                                                    double InsoleVal148 = f.parse(splitedValue[148]).doubleValue();
////                                                    double InsoleVal149 = f.parse(splitedValue[149]).doubleValue();
////                                                    double InsoleVal150 = f.parse(splitedValue[150]).doubleValue();
////                                                    double InsoleVal151 = f.parse(splitedValue[151]).doubleValue();
////                                                    double InsoleVal152 = f.parse(splitedValue[152]).doubleValue();
////                                                    double InsoleVal153 = f.parse(splitedValue[153]).doubleValue();
////                                                    double InsoleVal154 = f.parse(splitedValue[154]).doubleValue();
////                                                    double InsoleVal155 = f.parse(splitedValue[155]).doubleValue();
////                                                    double InsoleVal156 = f.parse(splitedValue[156]).doubleValue();
////                                                    double InsoleVal157 = f.parse(splitedValue[157]).doubleValue();
////                                                    double InsoleVal158 = f.parse(splitedValue[158]).doubleValue();
////                                                    double InsoleVal159 = f.parse(splitedValue[159]).doubleValue();
////                                                    double InsoleVal160 = f.parse(splitedValue[160]).doubleValue();
////                                                    double InsoleVal161 = f.parse(splitedValue[161]).doubleValue();
////                                                    double InsoleVal162 = f.parse(splitedValue[162]).doubleValue();
////                                                    double InsoleVal163 = f.parse(splitedValue[163]).doubleValue();
////                                                    double InsoleVal164 = f.parse(splitedValue[164]).doubleValue();
////                                                    double InsoleVal165 = f.parse(splitedValue[165]).doubleValue();
////                                                    double InsoleVal166 = f.parse(splitedValue[166]).doubleValue();
////                                                    double InsoleVal167 = f.parse(splitedValue[167]).doubleValue();
////                                                    double InsoleVal168 = f.parse(splitedValue[168]).doubleValue();
////                                                    double InsoleVal169 = f.parse(splitedValue[169]).doubleValue();
////                                                    double InsoleVal170 = f.parse(splitedValue[170]).doubleValue();
////                                                    double InsoleVal171 = f.parse(splitedValue[171]).doubleValue();
////                                                    double InsoleVal172 = f.parse(splitedValue[172]).doubleValue();
////                                                    double InsoleVal173 = f.parse(splitedValue[173]).doubleValue();
////                                                    double InsoleVal174 = f.parse(splitedValue[174]).doubleValue();
////                                                    double InsoleVal175 = f.parse(splitedValue[175]).doubleValue();
////                                                    double InsoleVal176 = f.parse(splitedValue[176]).doubleValue();
//////                                                    double InsoleVal177 = f.parse(splitedValue[177]).doubleValue();
////                                                    double InsoleVal177 = f.parse(splitedValue[177].replace("]", "")).doubleValue();
////
////                                                    float[][][] inp = {
////                                                            {
////                                                                    {(float) InsoleVal0}, {(float) InsoleVal1}, {(float) InsoleVal2}, {(float) InsoleVal3}, {(float) InsoleVal4},
////                                                                    {(float) InsoleVal5}, {(float) InsoleVal6}, {(float) InsoleVal7}, {(float) InsoleVal8}, {(float) InsoleVal9},
////                                                                    {(float) InsoleVal10}, {(float) InsoleVal11}, {(float) InsoleVal12}, {(float) InsoleVal13}, {(float) InsoleVal14},
////                                                                    {(float) InsoleVal15}, {(float) InsoleVal16}, {(float) InsoleVal17}, {(float) InsoleVal18}, {(float) InsoleVal19},
////                                                                    {(float) InsoleVal20}, {(float) InsoleVal21}, {(float) InsoleVal22}, {(float) InsoleVal23}, {(float) InsoleVal24},
////                                                                    {(float) InsoleVal25}, {(float) InsoleVal26}, {(float) InsoleVal27}, {(float) InsoleVal28}, {(float) InsoleVal29},
////                                                                    {(float) InsoleVal30}, {(float) InsoleVal31}, {(float) InsoleVal32}, {(float) InsoleVal33}, {(float) InsoleVal34},
////                                                                    {(float) InsoleVal35}, {(float) InsoleVal36}, {(float) InsoleVal37}, {(float) InsoleVal38}, {(float) InsoleVal39},
////                                                                    {(float) InsoleVal40}, {(float) InsoleVal41}, {(float) InsoleVal42}, {(float) InsoleVal43}, {(float) InsoleVal44},
////                                                                    {(float) InsoleVal45}, {(float) InsoleVal46}, {(float) InsoleVal47}, {(float) InsoleVal48}, {(float) InsoleVal49},
////                                                                    {(float) InsoleVal50}, {(float) InsoleVal51}, {(float) InsoleVal52}, {(float) InsoleVal53}, {(float) InsoleVal54},
////                                                                    {(float) InsoleVal55}, {(float) InsoleVal56}, {(float) InsoleVal57}, {(float) InsoleVal58}, {(float) InsoleVal59},
////                                                                    {(float) InsoleVal60}, {(float) InsoleVal61}, {(float) InsoleVal62}, {(float) InsoleVal63}, {(float) InsoleVal64},
////                                                                    {(float) InsoleVal65}, {(float) InsoleVal66}, {(float) InsoleVal67}, {(float) InsoleVal68}, {(float) InsoleVal69},
////                                                                    {(float) InsoleVal70}, {(float) InsoleVal71}, {(float) InsoleVal72}, {(float) InsoleVal73}, {(float) InsoleVal74},
////                                                                    {(float) InsoleVal75}, {(float) InsoleVal76}, {(float) InsoleVal77}, {(float) InsoleVal78}, {(float) InsoleVal79},
////                                                                    {(float) InsoleVal80}, {(float) InsoleVal81}, {(float) InsoleVal82}, {(float) InsoleVal83}, {(float) InsoleVal84},
////                                                                    {(float) InsoleVal85}, {(float) InsoleVal86}, {(float) InsoleVal87}, {(float) InsoleVal88}, {(float) InsoleVal89},
////                                                                    {(float) InsoleVal90}, {(float) InsoleVal91}, {(float) InsoleVal92}, {(float) InsoleVal93}, {(float) InsoleVal94},
////                                                                    {(float) InsoleVal95}, {(float) InsoleVal96}, {(float) InsoleVal97}, {(float) InsoleVal98}, {(float) InsoleVal99},
////                                                                    {(float) InsoleVal100}, {(float) InsoleVal101}, {(float) InsoleVal102}, {(float) InsoleVal103}, {(float) InsoleVal104},
////                                                                    {(float) InsoleVal105}, {(float) InsoleVal106}, {(float) InsoleVal107}, {(float) InsoleVal108}, {(float) InsoleVal109},
////                                                                    {(float) InsoleVal110}, {(float) InsoleVal111}, {(float) InsoleVal112}, {(float) InsoleVal113}, {(float) InsoleVal114},
////                                                                    {(float) InsoleVal115}, {(float) InsoleVal116}, {(float) InsoleVal117}, {(float) InsoleVal118}, {(float) InsoleVal119},
////                                                                    {(float) InsoleVal120}, {(float) InsoleVal121}, {(float) InsoleVal122}, {(float) InsoleVal123}, {(float) InsoleVal124},
////                                                                    {(float) InsoleVal125}, {(float) InsoleVal126}, {(float) InsoleVal127}, {(float) InsoleVal128}, {(float) InsoleVal129},
////                                                                    {(float) InsoleVal130}, {(float) InsoleVal131}, {(float) InsoleVal132}, {(float) InsoleVal133}, {(float) InsoleVal134},
////                                                                    {(float) InsoleVal135}, {(float) InsoleVal136}, {(float) InsoleVal137}, {(float) InsoleVal138}, {(float) InsoleVal139},
////                                                                    {(float) InsoleVal140}, {(float) InsoleVal141}, {(float) InsoleVal142}, {(float) InsoleVal143}, {(float) InsoleVal144},
////                                                                    {(float) InsoleVal145}, {(float) InsoleVal146}, {(float) InsoleVal147}, {(float) InsoleVal148}, {(float) InsoleVal149},
////                                                                    {(float) InsoleVal150}, {(float) InsoleVal151}, {(float) InsoleVal152}, {(float) InsoleVal153}, {(float) InsoleVal154},
////                                                                    {(float) InsoleVal155}, {(float) InsoleVal156}, {(float) InsoleVal157}, {(float) InsoleVal158}, {(float) InsoleVal159},
////                                                                    {(float) InsoleVal160}, {(float) InsoleVal161}, {(float) InsoleVal162}, {(float) InsoleVal163}, {(float) InsoleVal164},
////                                                                    {(float) InsoleVal165}, {(float) InsoleVal166}, {(float) InsoleVal167}, {(float) InsoleVal168}, {(float) InsoleVal169},
////                                                                    {(float) InsoleVal170}, {(float) InsoleVal171}, {(float) InsoleVal172}, {(float) InsoleVal173}, {(float) InsoleVal174},
////                                                                    {(float) InsoleVal175}, {(float) InsoleVal176}, {(float) InsoleVal177}
////                                                            }
////                                                    };
////
//////
////                                                    Log.d("TAG", "balem: " + Arrays.deepToString(inp));
//////
////                                                    float[][] out = new float[][]{{0, 0}};
////                                                    tflite.run(inp, out);
//////
////                                                    float resL = out[0][0];
////                                                    float resR = out[0][1];
////
////                                                    if (resL > 0.5 && resR < 0.5) {
////                                                        predResult.put("Status ", "FOG");
////                                                    } else {
////                                                        predResult.put("Status ", "NORMALL");
////                                                    }
////
////                                                    Log.d("TAG", "ucokbaba: " + resL + " " + resR);
////                                                    predResult.clear();
//////
//////                                                    Toast.makeText(Visualization.this, predResult.toString(), Toast.LENGTH_SHORT).show();
//////
//////
////                                                } catch (ParseException | IOException e) {
////                                                    e.printStackTrace();
////                                                }
////
////
//////                                        String[] splitedValue1 = splitedValue[j].split("~");
//////                                        if (splitedValue1.length == 1) {
//////                                            continue;
//////                                        }
//////                                        for (int k = 0; k < splitedValue1.length; k++) {
////////                                            ArrayList<String> coy = new ArrayList<String>();
////////                                            coy.add(splitedValue1[k]);
////////                                            Log.i("TAG", "runData: " + coy.size());
////////                                            Log.("TAG", "runData: " + splitedValue1[k]);
////////                                            Log.d("TAG", "Cok: " + splitedValue1[k]);
////////                                            Log.i(j+" at splitedIndex "+k+" at splited1Index Value is >> ",splitedValue1[k]);
//////                                        }
////
////                                            }
//////
////                                        }
////                                        ListData.clear();
////                                        --------------------------------------------------------
////                                        Log.d(TAG, "soak: " + a);
//
//
//                                        //                                Left and Right Condition
////                                int n_L=0;
////                                int n_R=0;
////                                for (int i = 0; i < 89; i++) {
////                                    if(l_data_double_arr[i]>r_data_double_arr[i]){
////                                        n_L=n_L+1;
////                                    }
////                                    else{
////                                        n_R=n_R+1;
////                                    }
////                                }
////                                if(n_L>n_R){
//////                                            Toast.makeText(Visualization.this, "Left.", Toast.LENGTH_SHORT).show();
////                                    Log.d(TAG, "ucokbaba: " + "Left");
////                                }
////                                else{
//////                                            Toast.makeText(Visualization.this, "Right.", Toast.LENGTH_SHORT).show();
////                                    Log.d(TAG, "ucokbaba: " + "Right");
////                                }
////                                            double InsoleVal0 = f.parse(ListData0.replace("[", "")).doubleValue();
//
//
//                                    }
//
//                                });
//
//                            }
//                        }, 1000, 1000); // delay


                        Toast.makeText(Visualization.this, "Left Insole Started.", Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Toast.makeText(Visualization.this, "Left Insole Not Connected!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        startRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (is_R_insole_connected) {
                    if (is_R_insole_started) {
                        right_insole_device_interface.stopInsole();
                        is_R_insole_started = false;
                        startRightBtn.setText("Start Right");
                        right_timer.cancel();
                    } else {
                        right_insole_device_interface.startInsole();
                        is_R_insole_started = true;
                        right_timer.scheduleAtFixedRate(new TimerTask() {
                            @Override
                            public void run() {
//                                sendToFirebase(rightDataDict,"Right_insole");
//                                Log.d(TAG, "jinkatama: " + RListDict.size());
//                                RListDict.clear();
//                                RList.clear();
//                                Log.d(TAG, "jinkatama: " + RListDict.size());
                            }
                        }, 1000, 1000);
                        startRightBtn.setText("Stop Right");
                        Toast.makeText(Visualization.this, "Right Insole Started.", Toast.LENGTH_SHORT).show();

                    }
                } else {
                    Toast.makeText(Visualization.this, "Right Insole Not Connected!", Toast.LENGTH_SHORT).show();
                }
            }
        });
*/

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (!is_L_insole_connected || !is_R_insole_connected){
//                    Toast.makeText(Visualization.this, "Connect both insoles.", Toast.LENGTH_SHORT).show();
//                    return;
//                }

                Toast.makeText(Visualization.this, "Gait measurement started.", Toast.LENGTH_SHORT).show();

                task = new TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("Task executed at: " + System.currentTimeMillis());

                        System.out.println("1초마다 데이터 전송");
                        System.out.println("dataListPerSec: "+dataListPerSec);

                        writeData2();
                        synchronized (dataListPerSec) {
                            dataListPerSec.clear();
                        }

                    }
                };
                new Timer().scheduleAtFixedRate(task, 1000, 1000);

                if (is_L_insole_connected) {
                    if (is_L_insole_started) {
                        left_insole_device_interface.stopInsole();
                        is_L_insole_started = false;
//                        startBtn.setText("Start");
//                        left_timer.cancel();
//                        startBtn.setBackgroundResource(R.drawable.rounded_corner);
                        startBtn.setEnabled(true);
                        task.cancel();
                    } else {
                        left_insole_device_interface.startInsole();
                        is_L_insole_started = true;
                        startBtn.setBackgroundResource(R.drawable.rounded_corner);
                        startBtn.setEnabled(true);
//                        left_timer = new Timer(); // At this line a new Thread will be created
//                        left_timer.scheduleAtFixedRate(new TimerTask() {
//                            @Override
//                            public void run() {
//                                //DO YOUR THINGS
//                                runOnUiThread(new Runnable() {
//
//                                    @Override
//                                    public void run() {
////                                        Log.d(Arrays.toString(l_data_double_arr), "@@@l_data_double_arr: ");
//                                        // 여기서 1초마다 데이터 전송함
//
////                                        writeData(l_data_double_arr, r_data_double_arr);
////                                        String ListDataDicts = ListData.toString();
////                                        Log.d("TAG", "onMessageReceived: " + LListDict.size());
//
////                                        ListData.addAll(LListDict);
////                                        ListData.addAll(RListDict);
//                                        String ListDataDicts = LListDict.toString();
////                                        LeftData(ListDataDicts.replace("],", "];"), "Left_Insole");
////
////                                        Log.d(TAG, "jinkatama: " + ListDataDicts);
////
////                                        ListData.clear();
////                                        LListDict.clear();
////                                        RListDict.clear();
////                                        LList.clear();
////                                        RList.clear();
//
//                                        int n_L=0;
//                                        int n_R=0;
//                                        for (int i = 0; i < 89; i++) {
////                                            Log.d(Arrays.toString(l_data_double_arr), "l_data_double_arr: ");
////                                            Log.d(Arrays.toString(r_data_double_arr), "r_data_double_arr: ");
//                                            if(l_data_double_arr[i]>r_data_double_arr[i]){
//                                                n_L=n_L+1;
//                                            }
//                                            else{
//                                                n_R=n_R+1;
//                                            }
//                                        }
//                                        if(n_L>n_R){
//                                            if (active == false){
//                                                active=true;
//                                                Log.d(TAG, "ucokbaba: " + "Pertama Nyala");
//                                                serialSendV("1");
//                                            } else {
//                                                Log.d(TAG, "ucokbaba: " + "Sudah Nyala");
//                                            }
//                                        } else if(n_L==n_R){
//                                            Log.d(TAG, "ucokbaba: " + "FOG");
//                                        }
//                                        else{
//                                            serialSendV("0");
//                                            active=false;
//                                        }
//
//
//                                    }
//
//                                });
//
//                            }
//                        }, 1000, 1000); // delay



                    }
                }
//                else {
//                    Toast.makeText(Visualization.this, "Left Insole Not Connected!", Toast.LENGTH_SHORT).show();
//                }

                if (is_R_insole_connected) {
                    if (is_R_insole_started) {
                        right_insole_device_interface.stopInsole();
                        is_R_insole_started = false;
                        startBtn.setText("Start");
                        startBtn.setBackgroundResource(R.drawable.rounded_corner);
//                        right_timer.cancel();
                        task.cancel();
                    } else {
                        startBtn.setText("Stop");
                        startBtn.setBackgroundResource(R.drawable.rounded_corner_gray);
                        right_insole_device_interface.startInsole();
                        is_R_insole_started = true;
//                        right_timer.scheduleAtFixedRate(new TimerTask() {
//                            @Override
//                            public void run() {
////                                sendToFirebase(rightDataDict,"Right_insole");
////                                Log.d(TAG, "jinkatama: " + RListDict.size());
////                                RListDict.clear();
////                                RList.clear();
////                                Log.d(TAG, "jinkatama: " + RListDict.size());
//                            }
//                        }, 1000, 1000);
//                        startRightBtn.setText("Stop Right");

                    }
                }
//                else {
//                    Toast.makeText(Visualization.this, "Right Insole Not Connected!", Toast.LENGTH_SHORT).show();
//                }

//
//
//                Runnable timerThread = new Runnable() {
//                    @Override
//                    public void run() {
                // 측정 타이머
//                if(is_L_insole_started && is_R_insole_started) {

                // Sleep for a while to allow scheduled tasks to run (in a real application, this wouldn't be necessary)
//                    try {
//                        Thread.sleep(10000); // Sleep for 10 seconds
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
            }
//                else {
//                    gaitTimer.cancel();
//                }
//
////                    }
////                };
////                timerThread.run();
//
//            }
            // startBtn.setClickable(true);

//                if (is_R_insole_connected) {
//                    if (is_R_insole_started) {
//                        right_insole_device_interface.stopInsole();
//                        is_R_insole_started = false;
//                        startRightBtn.setText("Start Right");
//                        right_timer.cancel();
//                    } else {
//                        right_insole_device_interface.startInsole();
//                        is_R_insole_started = true;
//                        right_timer.scheduleAtFixedRate(new TimerTask() {
//                            @Override
//                            public void run() {
////                                sendToFirebase(rightDataDict,"Right_insole");
////                                Log.d(TAG, "jinkatama: " + RListDict.size());
////                                RListDict.clear();
////                                RList.clear();
////                                Log.d(TAG, "jinkatama: " + RListDict.size());
//                            }
//                        }, 1000, 1000);
//                        startRightBtn.setText("Stop Right");
//                        Toast.makeText(Visualization.this, "Right Insole Started.", Toast.LENGTH_SHORT).show();
//
//                    }
//                } else {
//                    Toast.makeText(Visualization.this, "Right Insole Not Connected!", Toast.LENGTH_SHORT).show();
//                }
        });

        /*
        //Vibrator
        buttonScanL = (Button) findViewById(R.id.btnScanL);					//initial the button for scanning the BLE device
        buttonScanL.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                buttonScanOnClickProcessL();										//Alert Dialog for selecting the BLE device
            }
        });

        buttonScanV = (Button) findViewById(R.id.btnScanVib);					//initial the button for scanning the BLE device
        buttonScanV.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                buttonScanOnClickProcessV();										//Alert Dialog for selecting the BLE device
            }
        });

        buttonScanR = (Button) findViewById(R.id.btnScanR);					//initial the button for scanning the BLE device
        buttonScanR.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                buttonScanOnClickProcessR();										//Alert Dialog for selecting the BLE device
            }
        });

        */
    }


//    public void RightData(Map<String,Object>  data, String of_insole){
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                File file = new File(Visualization.this.getFilesDir(), "text");
//                if (!file.exists()) {
//                    file.mkdir();
//                }
//                File gpxfile = new File(file, "Right_Insole.txt");
//                if (gpxfile.length() > 235000){
//                    connectServer();
//                    gpxfile.delete();
//                } else{
//                    try {
//                        FileWriter writer = new FileWriter(gpxfile, true);
//                        for (Map.Entry<String, Object> entry : data.entrySet()) {
//                            writer.append(entry.getKey() + ":"  + entry.getValue() + System.lineSeparator());
//                        }
//                        writer.flush();
//                        writer.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }, 10000);
//
//    }

    public void LeftData(String  data, String of_insole){
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                File file = new File(Visualization.this.getFilesDir(), "text");
                if (!file.exists()) {
                    file.mkdir();
                }
                try {
                    File gpxfile = new File(file, "Left_Insole.txt");
                    FileWriter writer = new FileWriter(gpxfile, true);
//					writer.append(LaserDataL + System.lineSeparator());
                    writer.write(data);
                    writer.flush();
                    connectServerL();
                    gpxfile.delete();
//                    LList.clear();
//                    RList.clear();
//                    ListData.clear();
                } catch (IOException e) {
                    e.printStackTrace();
                }


//                if (!file.exists()) {
//                    file.mkdir();
//                }
//                File gpxfile = new File(file, "Left_Insole.txt");
//
//                try {
//                    FileWriter writer = new FileWriter(gpxfile, true);
//                    for (Map.Entry<String, Object> entry : data.entrySet()) {
//                        writer.append(entry.getKey() + ":" + entry.getValue() + System.lineSeparator());
//                    }
//                    writer.flush();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                if (gpxfile.length() > 240000){
//                    Log.d("TAG", "Left: " + gpxfile.length());
////                    connectServer();
//                    gpxfile.delete();
//                }else{
//                    try {
//                        FileWriter writer = new FileWriter(gpxfile, true);
//                        for (Map.Entry<String, Object> entry : data.entrySet()) {
//                            writer.append(entry.getKey() + ":" + entry.getValue() + System.lineSeparator());
//                        }
//                        writer.flush();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }


            }
        }, 1500);
    }
    //
//    public void connectServer() {
////       EditText edtServerURL = findViewById(R.id.edtServerURL);
//        String server = "http://192.168.1.232:5000";
//        String postUrl = server.toString() + "/predict/";
//
//        File file = new File(Visualization.this.getFilesDir(), "text");
//        File rightfile = new File(file, "Right_Insole.txt");
//        File leftfile = new File(file, "Left_Insole.txt");
//
//        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
//
//        byte[] byteArrayR = null;
//        byte[] byteArrayL = null;
//        try {
//            InputStream inputStreamR = getContentResolver().openInputStream(Uri.fromFile(rightfile));
//            InputStream inputStreamL = getContentResolver().openInputStream(Uri.fromFile(leftfile));
//            ByteArrayOutputStream byteBufferR = new ByteArrayOutputStream();
//            ByteArrayOutputStream byteBufferL = new ByteArrayOutputStream();
//            int bufferSizeR = 1024;
//            int bufferSizeL = 1024;
//            byte[] bufferR = new byte[bufferSizeR];
//            byte[] bufferL = new byte[bufferSizeL];
//
//            int lenR = 100;
//            int lenL = 100;
//            while ((lenR = inputStreamR.read(bufferR)) != -1 && (lenL = inputStreamL.read(bufferL)) != -1) {
//                byteBufferR.write(bufferR, 0, lenR);
//                byteBufferL.write(bufferR, 0, lenL);
//            }
//            byteArrayR = byteBufferR.toByteArray();
//            byteArrayL = byteBufferL.toByteArray();
//
//        }catch(Exception e) {
//            Toast.makeText(Visualization.this, "Please Make Sure the Selected File is an Text file.", Toast.LENGTH_SHORT).show();
//        }
//        multipartBodyBuilder.addFormDataPart("Rtext", "Rdata.txt", RequestBody.create(MediaType.parse("*/*"), byteArrayR));
//        multipartBodyBuilder.addFormDataPart("Ltext", "Ldata.txt", RequestBody.create(MediaType.parse("*/*"), byteArrayL));
//
//        RequestBody postBodyData = multipartBodyBuilder.build();
//
//        postRequest(postUrl, postBodyData);
//    }
//
//    void postRequest(String postUrl, RequestBody postBody) {
//
//        OkHttpClient client = new OkHttpClient();
//
//        Request request = new Request.Builder()
//                .url(postUrl)
//                .post(postBody)
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                call.cancel();
//                Log.d("FAIL", e.getMessage());
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(Visualization.this, "Failed to Connect to Server. Please Try Again.", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//
//            @Override
//            public void onResponse(Call call, final Response response) throws IOException {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            String[] res = response.toString().split(",");
//                            if(res[1].trim().equals("code=200")) {
//                                if (response.body().string().equals("FOG")) {
//                                    Toast.makeText(Visualization.this, "FoG", Toast.LENGTH_SHORT).show();
//                                    serialSend(String.valueOf("60"));
//                                    is_sound_on = true;
//                                } else {
//                                    Toast.makeText(Visualization.this, "Normall", Toast.LENGTH_SHORT).show();
//                                    is_sound_on = false;
//                                    serialSend("s");
//                                    toneGen1.stopTone();
//                                }
//                                serialSend("\n");
//                            }else {
//                                Toast.makeText(Visualization.this, "Oops! Something went wrong. \nPlease try again.", Toast.LENGTH_SHORT).show();
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//            }
//        });
//    }
//
    public void connectServerL() {
//       EditText edtServerURL = findViewById(R.id.edtServerURL);
        String server = "http://192.168.1.232:5000";
        String postUrl = server.toString() + "/predict/";

        File file = new File(Visualization.this.getFilesDir(), "text");
        File gpxfile = new File(file, "Left_Insole.txt");

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        byte[] byteArray = null;
        try {
            InputStream inputStream = getContentResolver().openInputStream(Uri.fromFile(gpxfile));
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
            byteArray = byteBuffer.toByteArray();

        }catch(Exception e) {
            Toast.makeText(Visualization.this, "Please Make Sure the Selected File is an Text file.", Toast.LENGTH_SHORT).show();
        }
        multipartBodyBuilder.addFormDataPart("Ltext", "Ldata.txt", RequestBody.create(MediaType.parse("*/*"), byteArray));

        RequestBody postBodyRData = multipartBodyBuilder.build();

        postRequestL(postUrl, postBodyRData);
    }
    //
    void postRequestL(String postUrl, RequestBody postBody) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                Log.d("FAIL", e.getMessage());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Visualization.this, "Failed to Connect to Server. Please Try Again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String[] res = response.toString().split(",");
                            if(res[1].trim().equals("code=200")) {
                                if (response.body().string().equals("FOG")) {
//                                    Toast.makeText(Visualization.this, "FoG", Toast.LENGTH_SHORT).show();
//                                    serialSend(String.valueOf("60"));
//                                    is_sound_on = true;
//                                    Log.d(TAG, "statusvib: " + "FOG");
                                    coyText.setText("FOG");
                                    serialSendL("1");
                                    serialSendR("1");
//                                    StatusDict.add("FOG");
//                                    Log.d(TAG, "statusvib: " + StatusDict.size());
//                                    if (StatusDict.size() == 3){
//                                        int FOGseries = Collections.frequency(StatusDict, "FOG");
//                                        int NORseries = Collections.frequency(StatusDict, "NORMAL");
//                                        String stat = "";
//
//                                        if (FOGseries==3){
//                                            stat = "FOG";
//                                            coyText.setText(stat);
//                                        } else {
//                                            stat = "NORMAL";
//                                            coyText.setText(stat);
//                                        }
//                                        Log.d(TAG, "statusvib: " + FOGseries);
//                                        Log.d(TAG, "statusvib: " + stat);
//                                        Log.d(TAG, "statusvib: " + StatusDict);
//                                        StatusDict.remove(0);
//                                        Log.d(TAG, "statusvib: " + StatusDict);
////                                        Log.d(TAG, "statusvib: " + StatusDict.size());
//                                    }
//                                    Log.d(TAG, "statusvib: " + "1");
//                                    if (!coyText.equals("FOG")){
//                                        serialSendV("1");
//                                        serialSendL("1");
//                                        serialSendR("1");
//                                    }else {
//                                        serialSendL("1");
//                                        serialSendR("1");
//                                    }
                                } else {
//                                    Toast.makeText(Visualization.this, "Normall", Toast.LENGTH_SHORT).show();
//                                    is_sound_on = false;
//                                    serialSend("s");
//                                    toneGen1.stopTone();
                                    coyText.setText("NORMAL");
//                                    StatusDict.add("NORMAL");
//                                    StatusDict.remove(0);
//                                    Log.d(TAG, "statusvib: " + StatusDict.size());
//                                    if (StatusDict.size() == 3){
////                                        int FOGseries = Collections.frequency(StatusDict, "FOG");
////                                        int NORseries = Collections.frequency(StatusDict, "NORMAL");
////                                        String stat = "";
//
////                                        if (NORseries==3){
////                                            stat = "NORMAL";
////                                            coyText.setText(stat);
////                                        } else {
////                                            stat = "FOG";
////                                            coyText.setText(stat);
////                                        }
////                                        coyText.setText("NORMAL");
////                                        Log.d(TAG, "statusvib: " + Collections.frequency(StatusDict, "NORMAL"));
//                                        Log.d(TAG, "statusvib: " + StatusDict);
////                                        Log.d(TAG, "statusvib: " + stat);
//                                        StatusDict.remove(0);
//                                        Log.d(TAG, "statusvib: " + StatusDict);
////                                        Log.d(TAG, "statusvib: " + StatusDict.size());
//                                    }
                                    serialSendV("0");
                                    serialSendL("0");
                                    serialSendR("0");
                                }
//                                serialSend("\n");
                            }else {
                                Toast.makeText(Visualization.this, "Oops! Something went wrong. \nPlease try again.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
//
//    public void connectServerL() {
////       EditText edtServerURL = findViewById(R.id.edtServerURL);
//        String server = "http://192.168.1.232:5000";
//        String postUrl = server.toString() + "/predict/";
//
//        File file = new File(Visualization.this.getFilesDir(), "text");
//        File gpxfile = new File(file, "Left_Insole.txt");
//
//        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
//
//        byte[] byteArray = null;
//        try {
//            InputStream inputStream = getContentResolver().openInputStream(Uri.fromFile(gpxfile));
//            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
//            int bufferSize = 1024;
//            byte[] buffer = new byte[bufferSize];
//
//            int len = 0;
//            while ((len = inputStream.read(buffer)) != -1) {
//                byteBuffer.write(buffer, 0, len);
//            }
//            byteArray = byteBuffer.toByteArray();
//
//        }catch(Exception e) {
//            Toast.makeText(Visualization.this, "Please Make Sure the Selected File is an Text file.", Toast.LENGTH_SHORT).show();
//        }
//        multipartBodyBuilder.addFormDataPart("Ltext", "Ldata.txt", RequestBody.create(MediaType.parse("*/*"), byteArray));
//
//        RequestBody postBodyLData = multipartBodyBuilder.build();
//
//        postRequestL(postUrl, postBodyLData);
//    }
//
//    void postRequestL(String postUrl, RequestBody postBody) {
//
//        OkHttpClient client = new OkHttpClient();
//
//        Request request = new Request.Builder()
//                .url(postUrl)
//                .post(postBody)
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                call.cancel();
//                Log.d("FAIL", e.getMessage());
//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(Visualization.this, "Failed to Connect to Server. Please Try Again.", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//
//            @Override
//            public void onResponse(Call call, final Response response) throws IOException {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
////                        try {
////                            String[] res = response.toString().split(",");
////                            if(res[1].trim().equals("code=200"))
//////                            Toast.makeText(Visualization.this, "Server's Response\n" + response.body().string(), Toast.LENGTH_SHORT).show();
////                                if (response.body().string().equals("FOG")){
////                                    Toast.makeText(Visualization.this, "FoG", Toast.LENGTH_SHORT).show();
////                                    serialSend(String.valueOf("60"));
////                                    serialSend("\n");
////                                    is_sound_on = true;
////                                } else {
////                                    Toast.makeText(Visualization.this, "Normall", Toast.LENGTH_SHORT).show();
////                                    is_sound_on = false;
////                                    serialSend("s");
////                                    toneGen1.stopTone();
////                                }
////                            else
////                                Toast.makeText(Visualization.this, "Oops! Something went wrong. \nPlease try again.", Toast.LENGTH_SHORT).show();
////                        } catch (Exception e) {
////                            e.printStackTrace();
////                        }
//                    }
//                });
//            }
//        });
//    }

    private MappedByteBuffer loadModelFile(Activity activity, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public void sendToFirebase(Map<String, Object> data, String of_insole) {
        Date date = new Date();
        db.collection(PATIENT_ID).document("Patient_Data").set(patientRecord);
        db.collection(PATIENT_ID)
                .document("Patient_Data")
                .collection(of_insole)
                .document(String.valueOf(formatter.format(date)))
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        data.clear();
                        Toast.makeText(Visualization.this, "Succesfully saved to Firebase", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Visualization.this, "Failed to saved to Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
//        left_insole_device_interface.stopInsole();
//        right_insole_device_interface.stopInsole();
//        bluetoothManager.close();
//        is_L_insole_connected = false;
//        is_R_insole_connected = false;
        onStopProcess();
    }

    protected void onResume() {
        super.onResume();
        System.out.println("BlUNOActivity onResume");
        onResumeProcess();                                                        //onResume Process by BlunoLibrary
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        onActivityResultProcess(requestCode, resultCode, data);                    //onActivityResult Process by BlunoLibrary
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
//		onPauseProcess();														//onPause Process by BlunoLibrary
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//		onDestroyProcess();														//onDestroy Process by BlunoLibrary
    }

    @Override
    public void onConectionStateChangeL(connectionStateEnumL theConnectionStateL) {//Once connection state changes, this function will be called
        switch (theConnectionStateL) {											//Four connection state
            case isConnected:
                buttonScanL.setText("L Connected");
                break;
            case isConnecting:
                buttonScanL.setText("Connecting L");
                break;
            case isToScan:
                buttonScanL.setText("Scan L");
                break;
            case isScanning:
                buttonScanL.setText("Scanning L");
                break;
            case isDisconnecting:
                buttonScanL.setText("isDisconnecting L");
                break;
            default:
                break;
        }
    }

    @Override
    public void onConectionStateChangeR(connectionStateEnumR theConnectionStateR) {
        switch (theConnectionStateR) {											//Four connection state
            case isConnected:
                buttonScanR.setText("R Connected");
                break;
            case isConnecting:
                buttonScanR.setText("Connecting L");
                break;
            case isToScan:
                buttonScanR.setText("Scan R");
                break;
            case isScanning:
                buttonScanR.setText("Scanning");
                break;
            case isDisconnecting:
                buttonScanR.setText("isDisconnecting");
                break;
            default:
                break;
        }
    }

    @Override
    public void onConectionStateChangeV(connectionStateEnumV theConnectionStateV) {
        switch (theConnectionStateV) {											//Four connection state
            case isConnected:
                buttonScanV.setText("Vib Connected");
                break;
            case isConnecting:
                buttonScanV.setText("Connecting Vib");
                break;
            case isToScan:
                buttonScanV.setText("Scan Vib");
                break;
            case isScanning:
                buttonScanV.setText("Scanning");
                break;
            case isDisconnecting:
                buttonScanV.setText("isDisconnecting");
                break;
            default:
                break;
        }
    }

    @Override
    public String onSerialReceivedL(String theString) {
        return null;
    }

    @Override
    public String onSerialReceivedR(String theString) {
        return null;
    }

    @Override
    public String onSerialReceivedV(String theString) {
        return null;
    }

    private void writeData(Double[] LArr, Double[] RArr) {
        Date today = new Date();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

        String currentDate = dateFormat.format(today);

        usersRef.child(currentDate).child("left").setValue(Arrays.toString(LArr));
        usersRef.child(currentDate).child("right").setValue(Arrays.toString(RArr));
//        usersRef.child(currentDate).child("left").child(usersRef.push().getKey()).setValue(LArr.toString());
//        usersRef.child(currentDate).child("right").child(usersRef.push().getKey()).setValue(RArr.toString());

//        User newUser = new User("User", LList, RList);
    }

    private void writeData2() {
        Date today = new Date();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

        String currentDate = dateFormat.format(today);

        if(dataListPerSecLen==0) return;
        System.out.println("dataListPerSec:"+dataListPerSec);
        System.out.println("dataListPerSec[0]:"+dataListPerSec.get(0));
        System.out.println("dataListPerSec[0]:"+dataListPerSec.size());
        System.out.println("dataListPerSec[0] len:"+dataListPerSec.get(0).size());
//        usersRef.child(currentDate).child("left").setValue(LDataListPerSec.toString());
//        usersRef.child(currentDate).child("right").setValue(RDataListPerSec.toString());
        usersRef.child(currentDate).child("both").setValue(dataListPerSec.toString());



//        usersRef.child(currentDate).child("left").child(usersRef.push().getKey()).setValue(LArr.toString());
//        usersRef.child(currentDate).child("right").child(usersRef.push().getKey()).setValue(RArr.toString());

//        User newUser = new User("User", LList, RList);
    }
    private static double[] concatenateArrays(double[] array1, double[] array2) {
        int length1 = array1.length;
        int length2 = array2.length;

        // Create a new array with the combined length
        double[] result = new double[length1 + length2];

        // Copy elements from array1
        System.arraycopy(array1, 0, result, 0, length1);

        // Copy elements from array2
        System.arraycopy(array2, 0, result, length1, length2);

        return result;
    }
}



package com.leadstepapp;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

/**
 * Created by USER on 2019/1/17.
 */

public class BLEDeviceAdapter extends BaseAdapter {
    ArrayList<BluetoothDevice> bluetoothDevices;
//    ArrayList<String> deviceNames,address;
//    ArrayList<Integer> states;

    LayoutInflater inflater = null;
    Context mContext;


    public BLEDeviceAdapter(Context c) {
        super();
        mContext = c;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        bluetoothDevices = new ArrayList<BluetoothDevice>();
//        this.bluetoothDevices = (ArrayList<BluetoothDevice>)bluetoothDevices.clone();
//        deviceNames = new ArrayList<String>();
//        address = new ArrayList<String>();
//        states = new ArrayList<Integer>();
    }

    public void addDevice(BluetoothDevice device) {
        if (!bluetoothDevices.contains(device)) {
            bluetoothDevices.add(device);
        }
    }

//    public void setBluetoothDevices(ArrayList<BluetoothDevice> bluetoothDevices){
//        if(this.bluetoothDevices != null){
//            this.bluetoothDevices = (ArrayList<BluetoothDevice>)bluetoothDevices.clone();
//            initList();
//            notifyDataSetChanged();
//        }
//    }

    public BluetoothDevice getDevice(int position) {
        return bluetoothDevices.get(position);
    }

    public void clear() {
        bluetoothDevices.clear();
    }

    @Override
    public int getCount() {
        return bluetoothDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return bluetoothDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        view = inflater.inflate(R.layout.listitem_device, null);
        TextView deviceName = (TextView) view.findViewById(R.id.device_name);
        TextView deviceAddress = (TextView) view.findViewById(R.id.device_address);
//        TextView deviceState = (TextView) view.findViewById(R.id.deviceState);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
//            return TODO;
        }
        if (bluetoothDevices.get(position).getName() != null && bluetoothDevices.get(position).getName().length() > 0)
            deviceName.setText(bluetoothDevices.get(position).getName());
        else
            deviceName.setText("UnKnow Name");
        //https://developer.android.com/reference/android/bluetooth/BluetoothDevice#getBondState()
        // 10 : device is not bonded, 11 bonding (pairing) is in progress with the remote device,12  Indicates the remote device is bonded (paired).
        //未配對(表示於裝置清單中未配對)，已配對(於裝置清單中有紀錄)，連接中(代表傳送資料中)
        deviceAddress.setText(bluetoothDevices.get(position).getAddress());
        String stateStr = (bluetoothDevices.get(position).getBondState() == 10)?"未配對":(bluetoothDevices.get(position).getBondState() == 11)?"連接中":"已配對";
        //關閉配對狀態，因為怕干擾使用者使用(因為配對狀態為硬體是否連接過，為存在清單，並非真正的連接狀態，真正連接狀態須直接看圖表是否有資料近來)，所以直接設定空字串
//        deviceState.setText(stateStr);
//        deviceState.setText("");

        return view;
    }

}

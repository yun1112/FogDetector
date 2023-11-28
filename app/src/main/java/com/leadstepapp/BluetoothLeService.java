/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.leadstepapp;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.List;


/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManagerL;
    private BluetoothManager mBluetoothManagerR;
    private BluetoothManager mBluetoothManagerV;
    private BluetoothAdapter mBluetoothAdapterL;
    private BluetoothAdapter mBluetoothAdapterR;
    private BluetoothAdapter mBluetoothAdapterV;
    BluetoothGatt mBluetoothGattL;
    BluetoothGatt mBluetoothGattR;
    BluetoothGatt mBluetoothGattV;
    public String mBluetoothDeviceAddressL;
    public String mBluetoothDeviceAddressR;
    public String mBluetoothDeviceAddressV;
    
    private static final int STATE_DISCONNECTEDL = 0;
    private static final int STATE_CONNECTINGL = 1;
    private static final int STATE_CONNECTEDL = 2;
    public int mConnectionStateL = STATE_DISCONNECTEDL;

    private static final int STATE_DISCONNECTEDR = 0;
    private static final int STATE_CONNECTINGR = 1;
    private static final int STATE_CONNECTEDR = 2;
    public int mConnectionStateR = STATE_DISCONNECTEDR;

    private static final int STATE_DISCONNECTEDV = 0;
    private static final int STATE_CONNECTINGV = 1;
    private static final int STATE_CONNECTEDV = 2;
    public int mConnectionStateV = STATE_DISCONNECTEDV;

    
    //To tell the onCharacteristicWrite call back function that this is a new characteristic, 
    //not the Write Characteristic to the device successfully.
    private static final int WRITE_NEW_CHARACTERISTICL = -1;
    private static final int WRITE_NEW_CHARACTERISTICR = -1;
    private static final int WRITE_NEW_CHARACTERISTICV = -1;
    //define the limited length of the characteristic.
    private static final int MAX_CHARACTERISTIC_LENGTHL = 13;
    private static final int MAX_CHARACTERISTIC_LENGTHR = 13;
    private static final int MAX_CHARACTERISTIC_LENGTHV = 13;
    //Show that Characteristic is writing or not.
    private boolean mIsWritingCharacteristicL=false;
    private boolean mIsWritingCharacteristicR=false;
    private boolean mIsWritingCharacteristicV=false;

    //class to store the Characteristic and content string push into the ring buffer.
    private class BluetoothGattCharacteristicHelperL{
    	BluetoothGattCharacteristic mCharacteristicL;
    	String mCharacteristicValueL;
    	BluetoothGattCharacteristicHelperL(BluetoothGattCharacteristic characteristicL, String characteristicValueL){
    		mCharacteristicL=characteristicL;
    		mCharacteristicValueL=characteristicValueL;
    	}
    }

    private class BluetoothGattCharacteristicHelperR{
        BluetoothGattCharacteristic mCharacteristicR;
        String mCharacteristicValueR;
        BluetoothGattCharacteristicHelperR(BluetoothGattCharacteristic characteristicR, String characteristicValueR){
            mCharacteristicR=characteristicR;
            mCharacteristicValueR=characteristicValueR;
        }
    }

    private class BluetoothGattCharacteristicHelperV{
        BluetoothGattCharacteristic mCharacteristicV;
        String mCharacteristicValueV;
        BluetoothGattCharacteristicHelperV(BluetoothGattCharacteristic characteristicV, String characteristicValueV){
            mCharacteristicV=characteristicV;
            mCharacteristicValueV=characteristicValueV;
        }
    }
    //ring buffer
    private RingBufferL<BluetoothGattCharacteristicHelperL> mCharacteristicRingBufferL = new RingBufferL<BluetoothGattCharacteristicHelperL>(8);

    private RingBufferR<BluetoothGattCharacteristicHelperR> mCharacteristicRingBufferR = new RingBufferR<BluetoothGattCharacteristicHelperR>(8);

    private RingBufferV<BluetoothGattCharacteristicHelperV> mCharacteristicRingBufferV = new RingBufferV<BluetoothGattCharacteristicHelperV>(8);
    
    public final static String ACTION_GATT_CONNECTEDL =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTEDR";
    public final static String ACTION_GATT_CONNECTEDR =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTEDR";
    public final static String ACTION_GATT_CONNECTEDV =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTEDV";
    public final static String ACTION_GATT_DISCONNECTEDL =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTEDL";
    public final static String ACTION_GATT_DISCONNECTEDR =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTEDR";
    public final static String ACTION_GATT_DISCONNECTEDV =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTEDV";
    public final static String ACTION_GATT_SERVICES_DISCOVEREDL =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVEREDL";
    public final static String ACTION_GATT_SERVICES_DISCOVEREDR =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVEREDR";
    public final static String ACTION_GATT_SERVICES_DISCOVEREDV =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVEREDV";
    public final static String LaserL =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLEL";
    public final static String LaserR =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLER";
    public final static String Vibrator =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLEV";
    public final static String EXTRA_DATAL =
            "com.example.bluetooth.le.EXTRA_DATAL";
    public final static String EXTRA_DATAR =
            "com.example.bluetooth.le.EXTRA_DATAR";
    public final static String EXTRA_DATAV =
            "com.example.bluetooth.le.EXTRA_DATAV";
//    public final static UUID UUID_HEART_RATE_MEASUREMENT =
//            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallbackL = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gattL, int statusL, int newStateL) {
            String intentActionL;
            System.out.println("BluetoothGattCallback----onConnectionStateChange"+newStateL);
            if (newStateL == BluetoothProfile.STATE_CONNECTED) {
                intentActionL = ACTION_GATT_CONNECTEDL;
                mConnectionStateL = STATE_CONNECTEDL;
                broadcastUpdateL(intentActionL);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                if(mBluetoothGattL.discoverServices())
                {
                    Log.i(TAG, "Attempting to start service discovery:");

                }
                else{
                    Log.i(TAG, "Attempting to start service discovery:not success");

                }


            } else if (newStateL == BluetoothProfile.STATE_DISCONNECTED) {
                intentActionL = ACTION_GATT_DISCONNECTEDL;
                mConnectionStateL = STATE_DISCONNECTEDL;
                Log.i(TAG, "Disconnected from GATTL server.");
                broadcastUpdateL(intentActionL);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gattL, int statusL) {
        	System.out.println("onServicesDiscovered "+statusL);
            if (statusL == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdateL(ACTION_GATT_SERVICES_DISCOVEREDL);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + statusL);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gattL, BluetoothGattCharacteristic characteristicL, int statusL)
        {
        	//this block should be synchronized to prevent the function overloading
			synchronized(this)
			{
				//CharacteristicWrite success
	        	if(statusL == BluetoothGatt.GATT_SUCCESS)
	        	{
	        		System.out.println("onCharacteristicWrite success:"+ new String(characteristicL.getValue()));
            		if(mCharacteristicRingBufferL.isEmpty())
            		{
    	        		mIsWritingCharacteristicL = false;
            		}
            		else
	            	{
	            		BluetoothGattCharacteristicHelperL bluetoothGattCharacteristicHelperL = mCharacteristicRingBufferL.next();
	            		if(bluetoothGattCharacteristicHelperL.mCharacteristicValueL.length() > MAX_CHARACTERISTIC_LENGTHL)
	            		{
	            	        try {
		            			bluetoothGattCharacteristicHelperL.mCharacteristicL.setValue(bluetoothGattCharacteristicHelperL.mCharacteristicValueL.substring(0, MAX_CHARACTERISTIC_LENGTHL).getBytes("ISO-8859-1"));

	            	        } catch (UnsupportedEncodingException e) {
	            	            // this should never happen because "US-ASCII" is hard-coded.
	            	            throw new IllegalStateException(e);
	            	        }


	            	        if(mBluetoothGattL.writeCharacteristic(bluetoothGattCharacteristicHelperL.mCharacteristicL))
	            	        {
	            	        	System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelperL.mCharacteristicL.getValue())+ ":success");
	            	        }else{
	            	        	System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelperL.mCharacteristicL.getValue())+ ":failure");
	            	        }
	            			bluetoothGattCharacteristicHelperL.mCharacteristicValueL = bluetoothGattCharacteristicHelperL.mCharacteristicValueL.substring(MAX_CHARACTERISTIC_LENGTHL);
	            		}
	            		else
	            		{
	            	        try {
	            	        	bluetoothGattCharacteristicHelperL.mCharacteristicL.setValue(bluetoothGattCharacteristicHelperL.mCharacteristicValueL.getBytes("ISO-8859-1"));
	            	        } catch (UnsupportedEncodingException e) {
	            	            // this should never happen because "US-ASCII" is hard-coded.
	            	            throw new IllegalStateException(e);
	            	        }

	            	        if(mBluetoothGattL.writeCharacteristic(bluetoothGattCharacteristicHelperL.mCharacteristicL))
	            	        {
	            	        	System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelperL.mCharacteristicL.getValue())+ ":success");
	            	        }else{
	            	        	System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelperL.mCharacteristicL.getValue())+ ":failure");
	            	        }
	            			bluetoothGattCharacteristicHelperL.mCharacteristicValueL = "";

//	            			System.out.print("before pop:");
//	            			System.out.println(mCharacteristicRingBuffer.size());
	            			mCharacteristicRingBufferL.pop();
//	            			System.out.print("after pop:");
//	            			System.out.println(mCharacteristicRingBuffer.size());
	            		}
	            	}
	        	}
	        	//WRITE a NEW CHARACTERISTIC
	        	else if(statusL == WRITE_NEW_CHARACTERISTICL)
	        	{
	        		if((!mCharacteristicRingBufferL.isEmpty()) && mIsWritingCharacteristicL==false)
	            	{
	            		BluetoothGattCharacteristicHelperL bluetoothGattCharacteristicHelperL = mCharacteristicRingBufferL.next();
	            		if(bluetoothGattCharacteristicHelperL.mCharacteristicValueL.length() > MAX_CHARACTERISTIC_LENGTHL)
	            		{

	            	        try {
		            			bluetoothGattCharacteristicHelperL.mCharacteristicL.setValue(bluetoothGattCharacteristicHelperL.mCharacteristicValueL.substring(0, MAX_CHARACTERISTIC_LENGTHL).getBytes("ISO-8859-1"));
	            	        } catch (UnsupportedEncodingException e) {
	            	            // this should never happen because "US-ASCII" is hard-coded.
	            	            throw new IllegalStateException(e);
	            	        }

	            	        if(mBluetoothGattL.writeCharacteristic(bluetoothGattCharacteristicHelperL.mCharacteristicL))
	            	        {
	            	        	System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelperL.mCharacteristicL.getValue())+ ":success");
	            	        }else{
	            	        	System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelperL.mCharacteristicL.getValue())+ ":failure");
	            	        }
	            			bluetoothGattCharacteristicHelperL.mCharacteristicValueL = bluetoothGattCharacteristicHelperL.mCharacteristicValueL.substring(MAX_CHARACTERISTIC_LENGTHL);
	            		}
	            		else
	            		{
	            	        try {
		            			bluetoothGattCharacteristicHelperL.mCharacteristicL.setValue(bluetoothGattCharacteristicHelperL.mCharacteristicValueL.getBytes("ISO-8859-1"));
	            	        } catch (UnsupportedEncodingException e) {
	            	            // this should never happen because "US-ASCII" is hard-coded.
	            	            throw new IllegalStateException(e);
	            	        }


	            	        if(mBluetoothGattL.writeCharacteristic(bluetoothGattCharacteristicHelperL.mCharacteristicL))
	            	        {
	            	        	System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelperL.mCharacteristicL.getValue())+ ":success");
//	            	        	System.out.println((byte)bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[0]);
//	            	        	System.out.println((byte)bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[1]);
//	            	        	System.out.println((byte)bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[2]);
//	            	        	System.out.println((byte)bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[3]);
//	            	        	System.out.println((byte)bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[4]);
//	            	        	System.out.println((byte)bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[5]);

	            	        }else{
	            	        	System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelperL.mCharacteristicL.getValue())+ ":failure");
	            	        }
	            			bluetoothGattCharacteristicHelperL.mCharacteristicValueL = "";

//		            			System.out.print("before pop:");
//		            			System.out.println(mCharacteristicRingBuffer.size());
		            			mCharacteristicRingBufferL.pop();
//		            			System.out.print("after pop:");
//		            			System.out.println(mCharacteristicRingBuffer.size());
	            		}
	            	}

    	        	mIsWritingCharacteristicL = true;

    	        	//clear the buffer to prevent the lock of the mIsWritingCharacteristic
    	        	if(mCharacteristicRingBufferL.isFull())
    	        	{
    	        		mCharacteristicRingBufferL.clear();
        	        	mIsWritingCharacteristicL = false;
    	        	}
	        	}
	        	else
					//CharacteristicWrite fail
	        	{
	        		mCharacteristicRingBufferL.clear();
	        		System.out.println("onCharacteristicWrite fail:"+ new String(characteristicL.getValue()));
	        		System.out.println(statusL);
	        	}
			}
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gattL,
                                         BluetoothGattCharacteristic characteristicL,
                                         int statusL) {
            if (statusL == BluetoothGatt.GATT_SUCCESS) {
            	System.out.println("onCharacteristicRead  "+characteristicL.getUuid().toString());
                broadcastUpdateL(LaserL, characteristicL);
            }
        }
        @Override
        public void  onDescriptorWrite(BluetoothGatt gattL,
        								BluetoothGattDescriptor characteristicL,
        								int statusL){
        	System.out.println("onDescriptorWrite  "+characteristicL.getUuid().toString()+" "+statusL);
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gattL,
                                            BluetoothGattCharacteristic characteristicL) {
        	System.out.println("onCharacteristicChanged  "+new String(characteristicL.getValue()));
            broadcastUpdateL(LaserL, characteristicL);
        }
    };

    private final BluetoothGattCallback mGattCallbackR = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gattR, int statusR, int newStateR) {
            String intentActionR;
            System.out.println("BluetoothGattCallback----onConnectionStateChange"+newStateR);
            if (newStateR == BluetoothProfile.STATE_CONNECTED) {
                intentActionR = ACTION_GATT_CONNECTEDR;
                mConnectionStateR = STATE_CONNECTEDR;
                broadcastUpdateR(intentActionR);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                if(mBluetoothGattR.discoverServices())
                {
                    Log.i(TAG, "Attempting to start service discovery:");

                }
                else{
                    Log.i(TAG, "Attempting to start service discovery:not success");

                }


            } else if (newStateR == BluetoothProfile.STATE_DISCONNECTED) {
                intentActionR = ACTION_GATT_DISCONNECTEDR;
                mConnectionStateR = STATE_DISCONNECTEDR;
                Log.i(TAG, "Disconnected from GATTL server.");
                broadcastUpdateR(intentActionR);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gattR, int statusR) {
            System.out.println("onServicesDiscovered "+statusR);
            if (statusR == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdateR(ACTION_GATT_SERVICES_DISCOVEREDR);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + statusR);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gattR, BluetoothGattCharacteristic characteristicR, int statusR)
        {
            //this block should be synchronized to prevent the function overloading
            synchronized(this)
            {
                //CharacteristicWrite success
                if(statusR == BluetoothGatt.GATT_SUCCESS)
                {
                    System.out.println("onCharacteristicWrite success:"+ new String(characteristicR.getValue()));
                    if(mCharacteristicRingBufferR.isEmpty())
                    {
                        mIsWritingCharacteristicR = false;
                    }
                    else
                    {
                        BluetoothGattCharacteristicHelperR bluetoothGattCharacteristicHelperR = mCharacteristicRingBufferR.next();
                        if(bluetoothGattCharacteristicHelperR.mCharacteristicValueR.length() > MAX_CHARACTERISTIC_LENGTHR)
                        {
                            try {
                                bluetoothGattCharacteristicHelperR.mCharacteristicR.setValue(bluetoothGattCharacteristicHelperR.mCharacteristicValueR.substring(0, MAX_CHARACTERISTIC_LENGTHR).getBytes("ISO-8859-1"));

                            } catch (UnsupportedEncodingException e) {
                                // this should never happen because "US-ASCII" is hard-coded.
                                throw new IllegalStateException(e);
                            }


                            if(mBluetoothGattR.writeCharacteristic(bluetoothGattCharacteristicHelperR.mCharacteristicR))
                            {
                                System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelperR.mCharacteristicR.getValue())+ ":success");
                            }else{
                                System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelperR.mCharacteristicR.getValue())+ ":failure");
                            }
                            bluetoothGattCharacteristicHelperR.mCharacteristicValueR = bluetoothGattCharacteristicHelperR.mCharacteristicValueR.substring(MAX_CHARACTERISTIC_LENGTHR);
                        }
                        else
                        {
                            try {
                                bluetoothGattCharacteristicHelperR.mCharacteristicR.setValue(bluetoothGattCharacteristicHelperR.mCharacteristicValueR.getBytes("ISO-8859-1"));
                            } catch (UnsupportedEncodingException e) {
                                // this should never happen because "US-ASCII" is hard-coded.
                                throw new IllegalStateException(e);
                            }

                            if(mBluetoothGattR.writeCharacteristic(bluetoothGattCharacteristicHelperR.mCharacteristicR))
                            {
                                System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelperR.mCharacteristicR.getValue())+ ":success");
                            }else{
                                System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelperR.mCharacteristicR.getValue())+ ":failure");
                            }
                            bluetoothGattCharacteristicHelperR.mCharacteristicValueR = "";

//	            			System.out.print("before pop:");
//	            			System.out.println(mCharacteristicRingBuffer.size());
                            mCharacteristicRingBufferR.pop();
//	            			System.out.print("after pop:");
//	            			System.out.println(mCharacteristicRingBuffer.size());
                        }
                    }
                }
                //WRITE a NEW CHARACTERISTIC
                else if(statusR == WRITE_NEW_CHARACTERISTICR)
                {
                    if((!mCharacteristicRingBufferR.isEmpty()) && mIsWritingCharacteristicR==false)
                    {
                        BluetoothGattCharacteristicHelperR bluetoothGattCharacteristicHelperR = mCharacteristicRingBufferR.next();
                        if(bluetoothGattCharacteristicHelperR.mCharacteristicValueR.length() > MAX_CHARACTERISTIC_LENGTHR)
                        {

                            try {
                                bluetoothGattCharacteristicHelperR.mCharacteristicR.setValue(bluetoothGattCharacteristicHelperR.mCharacteristicValueR.substring(0, MAX_CHARACTERISTIC_LENGTHR).getBytes("ISO-8859-1"));
                            } catch (UnsupportedEncodingException e) {
                                // this should never happen because "US-ASCII" is hard-coded.
                                throw new IllegalStateException(e);
                            }

                            if(mBluetoothGattR.writeCharacteristic(bluetoothGattCharacteristicHelperR.mCharacteristicR))
                            {
                                System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelperR.mCharacteristicR.getValue())+ ":success");
                            }else{
                                System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelperR.mCharacteristicR.getValue())+ ":failure");
                            }
                            bluetoothGattCharacteristicHelperR.mCharacteristicValueR = bluetoothGattCharacteristicHelperR.mCharacteristicValueR.substring(MAX_CHARACTERISTIC_LENGTHR);
                        }
                        else
                        {
                            try {
                                bluetoothGattCharacteristicHelperR.mCharacteristicR.setValue(bluetoothGattCharacteristicHelperR.mCharacteristicValueR.getBytes("ISO-8859-1"));
                            } catch (UnsupportedEncodingException e) {
                                // this should never happen because "US-ASCII" is hard-coded.
                                throw new IllegalStateException(e);
                            }


                            if(mBluetoothGattR.writeCharacteristic(bluetoothGattCharacteristicHelperR.mCharacteristicR))
                            {
                                System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelperR.mCharacteristicR.getValue())+ ":success");
//	            	        	System.out.println((byte)bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[0]);
//	            	        	System.out.println((byte)bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[1]);
//	            	        	System.out.println((byte)bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[2]);
//	            	        	System.out.println((byte)bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[3]);
//	            	        	System.out.println((byte)bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[4]);
//	            	        	System.out.println((byte)bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[5]);

                            }else{
                                System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelperR.mCharacteristicR.getValue())+ ":failure");
                            }
                            bluetoothGattCharacteristicHelperR.mCharacteristicValueR = "";

//		            			System.out.print("before pop:");
//		            			System.out.println(mCharacteristicRingBuffer.size());
                            mCharacteristicRingBufferR.pop();
//		            			System.out.print("after pop:");
//		            			System.out.println(mCharacteristicRingBuffer.size());
                        }
                    }

                    mIsWritingCharacteristicR = true;

                    //clear the buffer to prevent the lock of the mIsWritingCharacteristic
                    if(mCharacteristicRingBufferR.isFull())
                    {
                        mCharacteristicRingBufferR.clear();
                        mIsWritingCharacteristicR = false;
                    }
                }
                else
                //CharacteristicWrite fail
                {
                    mCharacteristicRingBufferR.clear();
                    System.out.println("onCharacteristicWrite fail:"+ new String(characteristicR.getValue()));
                    System.out.println(statusR);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gattR,
                                         BluetoothGattCharacteristic characteristicR,
                                         int statusR) {
            if (statusR == BluetoothGatt.GATT_SUCCESS) {
                System.out.println("onCharacteristicRead  "+characteristicR.getUuid().toString());
                broadcastUpdateR(LaserR, characteristicR);
            }
        }
        @Override
        public void  onDescriptorWrite(BluetoothGatt gattR,
                                       BluetoothGattDescriptor characteristicR,
                                       int statusR){
            System.out.println("onDescriptorWrite  "+characteristicR.getUuid().toString()+" "+statusR);
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gattR,
                                            BluetoothGattCharacteristic characteristicR) {
            System.out.println("onCharacteristicChanged  "+new String(characteristicR.getValue()));
            broadcastUpdateR(LaserR, characteristicR);
        }
    };

//Mulai

    private final BluetoothGattCallback mGattCallbackV = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gattV, int statusV, int newStateV) {
            String intentActionV;
            System.out.println("BluetoothGattCallback----onConnectionStateChange"+newStateV);
            if (newStateV == BluetoothProfile.STATE_CONNECTED) {
                intentActionV = ACTION_GATT_CONNECTEDV;
                mConnectionStateV = STATE_CONNECTEDV;
                broadcastUpdateV(intentActionV);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                if(mBluetoothGattV.discoverServices())
                {
                    Log.i(TAG, "Attempting to start service discovery:");

                }
                else{
                    Log.i(TAG, "Attempting to start service discovery:not success");

                }


            } else if (newStateV == BluetoothProfile.STATE_DISCONNECTED) {
                intentActionV = ACTION_GATT_DISCONNECTEDV;
                mConnectionStateV = STATE_DISCONNECTEDV;
                Log.i(TAG, "Disconnected from GATTV server.");
                broadcastUpdateV(intentActionV);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gattV, int statusV) {
            System.out.println("onServicesDiscovered "+statusV);
            if (statusV == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdateV(ACTION_GATT_SERVICES_DISCOVEREDV);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + statusV);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gattV, BluetoothGattCharacteristic characteristicV, int statusV)
        {
            //this block should be synchronized to prevent the function overloading
            synchronized(this)
            {
                //CharacteristicWrite success
                if(statusV == BluetoothGatt.GATT_SUCCESS)
                {
                    System.out.println("onCharacteristicWrite success:"+ new String(characteristicV.getValue()));
                    if(mCharacteristicRingBufferV.isEmpty())
                    {
                        mIsWritingCharacteristicV = false;
                    }
                    else
                    {
                        BluetoothGattCharacteristicHelperV bluetoothGattCharacteristicHelperV = mCharacteristicRingBufferV.next();
                        if(bluetoothGattCharacteristicHelperV.mCharacteristicValueV.length() > MAX_CHARACTERISTIC_LENGTHV)
                        {
                            try {
                                bluetoothGattCharacteristicHelperV.mCharacteristicV.setValue(bluetoothGattCharacteristicHelperV.mCharacteristicValueV.substring(0, MAX_CHARACTERISTIC_LENGTHV).getBytes("ISO-8859-1"));

                            } catch (UnsupportedEncodingException e) {
                                // this should never happen because "US-ASCII" is hard-coded.
                                throw new IllegalStateException(e);
                            }


                            if(mBluetoothGattV.writeCharacteristic(bluetoothGattCharacteristicHelperV.mCharacteristicV))
                            {
                                System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelperV.mCharacteristicV.getValue())+ ":success");
                            }else{
                                System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelperV.mCharacteristicV.getValue())+ ":failure");
                            }
                            bluetoothGattCharacteristicHelperV.mCharacteristicValueV = bluetoothGattCharacteristicHelperV.mCharacteristicValueV.substring(MAX_CHARACTERISTIC_LENGTHV);
                        }
                        else
                        {
                            try {
                                bluetoothGattCharacteristicHelperV.mCharacteristicV.setValue(bluetoothGattCharacteristicHelperV.mCharacteristicValueV.getBytes("ISO-8859-1"));
                            } catch (UnsupportedEncodingException e) {
                                // this should never happen because "US-ASCII" is hard-coded.
                                throw new IllegalStateException(e);
                            }

                            if(mBluetoothGattV.writeCharacteristic(bluetoothGattCharacteristicHelperV.mCharacteristicV))
                            {
                                System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelperV.mCharacteristicV.getValue())+ ":success");
                            }else{
                                System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelperV.mCharacteristicV.getValue())+ ":failure");
                            }
                            bluetoothGattCharacteristicHelperV.mCharacteristicValueV = "";

//	            			System.out.print("before pop:");
//	            			System.out.println(mCharacteristicRingBuffer.size());
                            mCharacteristicRingBufferV.pop();
//	            			System.out.print("after pop:");
//	            			System.out.println(mCharacteristicRingBuffer.size());
                        }
                    }
                }
                //WRITE a NEW CHARACTERISTIC
                else if(statusV == WRITE_NEW_CHARACTERISTICV)
                {
                    if((!mCharacteristicRingBufferV.isEmpty()) && mIsWritingCharacteristicV==false)
                    {
                        BluetoothGattCharacteristicHelperV bluetoothGattCharacteristicHelperV = mCharacteristicRingBufferV.next();
                        if(bluetoothGattCharacteristicHelperV.mCharacteristicValueV.length() > MAX_CHARACTERISTIC_LENGTHV)
                        {

                            try {
                                bluetoothGattCharacteristicHelperV.mCharacteristicV.setValue(bluetoothGattCharacteristicHelperV.mCharacteristicValueV.substring(0, MAX_CHARACTERISTIC_LENGTHV).getBytes("ISO-8859-1"));
                            } catch (UnsupportedEncodingException e) {
                                // this should never happen because "US-ASCII" is hard-coded.
                                throw new IllegalStateException(e);
                            }

                            if(mBluetoothGattV.writeCharacteristic(bluetoothGattCharacteristicHelperV.mCharacteristicV))
                            {
                                System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelperV.mCharacteristicV.getValue())+ ":success");
                            }else{
                                System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelperV.mCharacteristicV.getValue())+ ":failure");
                            }
                            bluetoothGattCharacteristicHelperV.mCharacteristicValueV = bluetoothGattCharacteristicHelperV.mCharacteristicValueV.substring(MAX_CHARACTERISTIC_LENGTHV);
                        }
                        else
                        {
                            try {
                                bluetoothGattCharacteristicHelperV.mCharacteristicV.setValue(bluetoothGattCharacteristicHelperV.mCharacteristicValueV.getBytes("ISO-8859-1"));
                            } catch (UnsupportedEncodingException e) {
                                // this should never happen because "US-ASCII" is hard-coded.
                                throw new IllegalStateException(e);
                            }


                            if(mBluetoothGattV.writeCharacteristic(bluetoothGattCharacteristicHelperV.mCharacteristicV))
                            {
                                System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelperV.mCharacteristicV.getValue())+ ":success");
//	            	        	System.out.println((byte)bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[0]);
//	            	        	System.out.println((byte)bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[1]);
//	            	        	System.out.println((byte)bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[2]);
//	            	        	System.out.println((byte)bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[3]);
//	            	        	System.out.println((byte)bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[4]);
//	            	        	System.out.println((byte)bluetoothGattCharacteristicHelper.mCharacteristic.getValue()[5]);

                            }else{
                                System.out.println("writeCharacteristic init "+new String(bluetoothGattCharacteristicHelperV.mCharacteristicV.getValue())+ ":failure");
                            }
                            bluetoothGattCharacteristicHelperV.mCharacteristicValueV = "";

//		            			System.out.print("before pop:");
//		            			System.out.println(mCharacteristicRingBuffer.size());
                            mCharacteristicRingBufferV.pop();
//		            			System.out.print("after pop:");
//		            			System.out.println(mCharacteristicRingBuffer.size());
                        }
                    }

                    mIsWritingCharacteristicV = true;

                    //clear the buffer to prevent the lock of the mIsWritingCharacteristic
                    if(mCharacteristicRingBufferV.isFull())
                    {
                        mCharacteristicRingBufferV.clear();
                        mIsWritingCharacteristicV = false;
                    }
                }
                else
                //CharacteristicWrite fail
                {
                    mCharacteristicRingBufferV.clear();
                    System.out.println("onCharacteristicWrite fail:"+ new String(characteristicV.getValue()));
                    System.out.println(statusV);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gattV,
                                         BluetoothGattCharacteristic characteristicV,
                                         int statusV) {
            if (statusV == BluetoothGatt.GATT_SUCCESS) {
                System.out.println("onCharacteristicRead  "+characteristicV.getUuid().toString());
                broadcastUpdateV(Vibrator, characteristicV);
            }
        }
        @Override
        public void  onDescriptorWrite(BluetoothGatt gattV,
                                       BluetoothGattDescriptor characteristicV,
                                       int statusV){
            System.out.println("onDescriptorWrite  "+characteristicV.getUuid().toString()+" "+statusV);
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gattV,
                                            BluetoothGattCharacteristic characteristicV) {
            System.out.println("onCharacteristicChanged  "+new String(characteristicV.getValue()));
            broadcastUpdateV(Vibrator, characteristicV);
        }
    };

//    batas

    private void broadcastUpdateL(final String actionL) {
        final Intent intent = new Intent(actionL);
        sendBroadcast(intent);
    }

    private void broadcastUpdateL(final String actionL,
                                 final BluetoothGattCharacteristic characteristicL) {
        final Intent intent = new Intent(actionL);
        System.out.println("BluetoothLeService broadcastUpdate");
        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
//        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
//            int flag = characteristic.getProperties();
//            int format = -1;
//            if ((flag & 0x01) != 0) {
//                format = BluetoothGattCharacteristic.FORMAT_UINT16;
//                Log.d(TAG, "Heart rate format UINT16.");
//            } else {
//                format = BluetoothGattCharacteristic.FORMAT_UINT8;
//                Log.d(TAG, "Heart rate format UINT8.");
//            }
//            final int heartRate = characteristic.getIntValue(format, 1);
//            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
//            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
//        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] dataL = characteristicL.getValue();
            if (dataL != null && dataL.length > 0) {
                intent.putExtra(EXTRA_DATAL, new String(dataL));
//                Log.d(TAG, "broadcastUpdateL: " + dataL.length);
        		sendBroadcast(intent);
            }
//        }
    }

    private void broadcastUpdateR(final String actionR) {
        final Intent intent = new Intent(actionR);
        sendBroadcast(intent);
    }

    private void broadcastUpdateR(final String actionR,
                                  final BluetoothGattCharacteristic characteristicR) {
        final Intent intent = new Intent(actionR);
        System.out.println("BluetoothLeService broadcastUpdate");
        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
//        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
//            int flag = characteristic.getProperties();
//            int format = -1;
//            if ((flag & 0x01) != 0) {
//                format = BluetoothGattCharacteristic.FORMAT_UINT16;
//                Log.d(TAG, "Heart rate format UINT16.");
//            } else {
//                format = BluetoothGattCharacteristic.FORMAT_UINT8;
//                Log.d(TAG, "Heart rate format UINT8.");
//            }
//            final int heartRate = characteristic.getIntValue(format, 1);
//            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
//            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
//        } else {
        // For all other profiles, writes the data formatted in HEX.
        final byte[] dataR = characteristicR.getValue();
        if (dataR != null && dataR.length > 0) {
            intent.putExtra(EXTRA_DATAR, new String(dataR));
            Log.d(TAG, "broadcastUpdateR: " + dataR.length);
            sendBroadcast(intent);
        }
//        }
    }

    private void broadcastUpdateV(final String actionV) {
        final Intent intent = new Intent(actionV);
        sendBroadcast(intent);
    }

    private void broadcastUpdateV(final String actionV,
                                  final BluetoothGattCharacteristic characteristicV) {
        final Intent intent = new Intent(actionV);
        System.out.println("BluetoothLeService broadcastUpdate");
        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
//        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
//            int flag = characteristic.getProperties();
//            int format = -1;
//            if ((flag & 0x01) != 0) {
//                format = BluetoothGattCharacteristic.FORMAT_UINT16;
//                Log.d(TAG, "Heart rate format UINT16.");
//            } else {
//                format = BluetoothGattCharacteristic.FORMAT_UINT8;
//                Log.d(TAG, "Heart rate format UINT8.");
//            }
//            final int heartRate = characteristic.getIntValue(format, 1);
//            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
//            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
//        } else {
        // For all other profiles, writes the data formatted in HEX.
        final byte[] dataV = characteristicV.getValue();
        if (dataV != null && dataV.length > 0) {
            intent.putExtra(EXTRA_DATAV, new String(dataV));
            Log.d(TAG, "broadcastUpdateR: " + dataV.length);
            sendBroadcast(intent);
        }
//        }
    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        closeL();
        closeR();
        closeV();
        return super.onUnbind(intent);
    }


    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initializeL() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
    	System.out.println("BluetoothLeService initialize"+mBluetoothManagerL);
        if (mBluetoothManagerL == null) {
            mBluetoothManagerL = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManagerL == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapterL = mBluetoothManagerL.getAdapter();
        if (mBluetoothAdapterL == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    public boolean initializeR() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        System.out.println("BluetoothLeService initialize"+mBluetoothManagerR);
        if (mBluetoothManagerR == null) {
            mBluetoothManagerR = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManagerR == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapterR = mBluetoothManagerR.getAdapter();
        if (mBluetoothAdapterR == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    public boolean initializeV() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        System.out.println("BluetoothLeService initialize"+mBluetoothManagerV);
        if (mBluetoothManagerV == null) {
            mBluetoothManagerV = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManagerV == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapterV = mBluetoothManagerV.getAdapter();
        if (mBluetoothAdapterV == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }


    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connectL(final String addressL) {
    	System.out.println("BluetoothLeService connect"+addressL+mBluetoothGattL);
        if (mBluetoothAdapterL == null || addressL == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
//        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
//                && mBluetoothGatt != null) {
//            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
//            if (mBluetoothGatt.connect()) {
//            	System.out.println("mBluetoothGatt connect");
//                mConnectionState = STATE_CONNECTING;
//                return true;
//            } else {
//            	System.out.println("mBluetoothGatt else connect");
//                return false;
//            }
//        }

        final BluetoothDevice deviceL = mBluetoothAdapterL.getRemoteDevice(addressL);
        if (deviceL == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }

        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        System.out.println("device.connectGatt connect");
		synchronized(this)
		{
			mBluetoothGattL = deviceL.connectGatt(this, false, mGattCallbackL);
		}
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddressL = addressL;
        mConnectionStateL = STATE_CONNECTINGL;
        return true;
    }

    public boolean connectR(final String addressR) {
        System.out.println("BluetoothLeService connect"+addressR+mBluetoothGattR);
        if (mBluetoothAdapterR == null || addressR == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
//        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
//                && mBluetoothGatt != null) {
//            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
//            if (mBluetoothGatt.connect()) {
//            	System.out.println("mBluetoothGatt connect");
//                mConnectionState = STATE_CONNECTING;
//                return true;
//            } else {
//            	System.out.println("mBluetoothGatt else connect");
//                return false;
//            }
//        }
        final BluetoothDevice deviceR = mBluetoothAdapterR.getRemoteDevice(addressR);
        if (deviceR == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        System.out.println("device.connectGatt connect");
        synchronized(this)
        {
            mBluetoothGattR = deviceR.connectGatt(this, false, mGattCallbackR);
        }
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddressR = addressR;
        mConnectionStateR = STATE_CONNECTINGR;
        return true;
    }

    public boolean connectV(final String addressV) {
        System.out.println("BluetoothLeService connect"+addressV+mBluetoothGattV);
        if (mBluetoothAdapterV == null || addressV == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
//        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
//                && mBluetoothGatt != null) {
//            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
//            if (mBluetoothGatt.connect()) {
//            	System.out.println("mBluetoothGatt connect");
//                mConnectionState = STATE_CONNECTING;
//                return true;
//            } else {
//            	System.out.println("mBluetoothGatt else connect");
//                return false;
//            }
//        }
        final BluetoothDevice deviceV = mBluetoothAdapterV.getRemoteDevice(addressV);
        if (deviceV == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        System.out.println("device.connectGatt connect");
        synchronized(this)
        {
            mBluetoothGattV = deviceV.connectGatt(this, false, mGattCallbackV);
        }
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddressV = addressV;
        mConnectionStateV = STATE_CONNECTINGV;
        return true;
    }
    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnectL() {
    	System.out.println("BluetoothLeService disconnect");
        if (mBluetoothAdapterL == null || mBluetoothGattL == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGattL.disconnect();
    }

    public void disconnectR() {
        System.out.println("BluetoothLeService disconnect");
        if (mBluetoothAdapterR == null || mBluetoothGattR == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGattR.disconnect();
    }

    public void disconnectV() {
        System.out.println("BluetoothLeService disconnect");
        if (mBluetoothAdapterV == null || mBluetoothGattV == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGattV.disconnect();
    }
    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void closeL() {
    	System.out.println("BluetoothLeService close");
        if (mBluetoothGattL == null) {
            return;
        }
        mBluetoothGattL.close();
        mBluetoothGattL = null;
    }

    public void closeR() {
        System.out.println("BluetoothLeService close");
        if (mBluetoothGattR == null) {
            return;
        }
        mBluetoothGattR.close();
        mBluetoothGattR = null;
    }

    public void closeV() {
        System.out.println("BluetoothLeService close");
        if (mBluetoothGattV == null) {
            return;
        }
        mBluetoothGattV.close();
        mBluetoothGattV = null;
    }
    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristicL(BluetoothGattCharacteristic characteristicL) {
        if (mBluetoothAdapterL == null || mBluetoothGattL == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGattL.readCharacteristic(characteristicL);
    }

    public void readCharacteristicR(BluetoothGattCharacteristic characteristicR) {
        if (mBluetoothAdapterR == null || mBluetoothGattR == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGattR.readCharacteristic(characteristicR);
    }

    public void readCharacteristicV(BluetoothGattCharacteristic characteristicV) {
        if (mBluetoothAdapterV == null || mBluetoothGattV == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGattV.readCharacteristic(characteristicV);
    }
    

    /**
     * Write information to the device on a given {@code BluetoothGattCharacteristic}. The content string and characteristic is 
     * only pushed into a ring buffer. All the transmission is based on the {@code onCharacteristicWrite} call back function, 
     * which is called directly in this function
     *
     * @param characteristic The characteristic to write to.
     */
    public void writeCharacteristicL(BluetoothGattCharacteristic characteristicL) {
        if (mBluetoothAdapterL == null || mBluetoothGattL == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        
    	//The character size of TI CC2540 is limited to 17 bytes, otherwise characteristic can not be sent properly,
    	//so String should be cut to comply this restriction. And something should be done here:
        String writeCharacteristicStringL;
        try {
        	writeCharacteristicStringL = new String(characteristicL.getValue(),"ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            // this should never happen because "US-ASCII" is hard-coded.
            throw new IllegalStateException(e);
        }
        System.out.println("allwriteCharacteristicString:"+writeCharacteristicStringL);
        
        //As the communication is asynchronous content string and characteristic should be pushed into an ring buffer for further transmission
    	mCharacteristicRingBufferL.push(new BluetoothGattCharacteristicHelperL(characteristicL,writeCharacteristicStringL) );
    	System.out.println("mCharacteristicRingBufferlength:"+mCharacteristicRingBufferL.size());


    	//The progress of onCharacteristicWrite and writeCharacteristic is almost the same. So callback function is called directly here
    	//for details see the onCharacteristicWrite function
    	mGattCallbackL.onCharacteristicWrite(mBluetoothGattL, characteristicL, WRITE_NEW_CHARACTERISTICL);

    }

    public void writeCharacteristicR(BluetoothGattCharacteristic characteristicR) {
        if (mBluetoothAdapterR == null || mBluetoothGattR == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        //The character size of TI CC2540 is limited to 17 bytes, otherwise characteristic can not be sent properly,
        //so String should be cut to comply this restriction. And something should be done here:
        String writeCharacteristicStringR;
        try {
            writeCharacteristicStringR = new String(characteristicR.getValue(),"ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            // this should never happen because "US-ASCII" is hard-coded.
            throw new IllegalStateException(e);
        }
        System.out.println("allwriteCharacteristicString:"+writeCharacteristicStringR);

        //As the communication is asynchronous content string and characteristic should be pushed into an ring buffer for further transmission
        mCharacteristicRingBufferR.push(new BluetoothGattCharacteristicHelperR(characteristicR,writeCharacteristicStringR) );
        System.out.println("mCharacteristicRingBufferlength:"+mCharacteristicRingBufferR.size());


        //The progress of onCharacteristicWrite and writeCharacteristic is almost the same. So callback function is called directly here
        //for details see the onCharacteristicWrite function
        mGattCallbackR.onCharacteristicWrite(mBluetoothGattR, characteristicR, WRITE_NEW_CHARACTERISTICR);

    }

    public void writeCharacteristicV(BluetoothGattCharacteristic characteristicV) {
        if (mBluetoothAdapterV == null || mBluetoothGattV == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        //The character size of TI CC2540 is limited to 17 bytes, otherwise characteristic can not be sent properly,
        //so String should be cut to comply this restriction. And something should be done here:
        String writeCharacteristicStringV;
        try {
            writeCharacteristicStringV = new String(characteristicV.getValue(),"ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            // this should never happen because "US-ASCII" is hard-coded.
            throw new IllegalStateException(e);
        }
        System.out.println("allwriteCharacteristicString:"+writeCharacteristicStringV);

        //As the communication is asynchronous content string and characteristic should be pushed into an ring buffer for further transmission
        mCharacteristicRingBufferV.push(new BluetoothGattCharacteristicHelperV(characteristicV,writeCharacteristicStringV) );
        System.out.println("mCharacteristicRingBufferlength:"+mCharacteristicRingBufferV.size());


        //The progress of onCharacteristicWrite and writeCharacteristic is almost the same. So callback function is called directly here
        //for details see the onCharacteristicWrite function
        mGattCallbackV.onCharacteristicWrite(mBluetoothGattV, characteristicV, WRITE_NEW_CHARACTERISTICV);

    }


    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotificationL(BluetoothGattCharacteristic characteristicL,
                                              boolean enabledL) {
        if (mBluetoothAdapterL == null || mBluetoothGattL == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGattL.setCharacteristicNotification(characteristicL, enabledL);

        //BluetoothGattDescriptor descriptor = characteristic.getDescriptor(characteristic.getUuid());
        //descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        //mBluetoothGatt.writeDescriptor(descriptor);
    	
        // This is specific to Heart Rate Measurement.
//        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
//            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
//                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            mBluetoothGatt.writeDescriptor(descriptor);
//        }
    }

    public void setCharacteristicNotificationR(BluetoothGattCharacteristic characteristicR,
                                              boolean enabledR) {
        if (mBluetoothAdapterR == null || mBluetoothGattR == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGattR.setCharacteristicNotification(characteristicR, enabledR);

        //BluetoothGattDescriptor descriptor = characteristic.getDescriptor(characteristic.getUuid());
        //descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        //mBluetoothGatt.writeDescriptor(descriptor);

        // This is specific to Heart Rate Measurement.
//        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
//            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
//                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            mBluetoothGatt.writeDescriptor(descriptor);
//        }
    }

    public void setCharacteristicNotificationV(BluetoothGattCharacteristic characteristicV,
                                               boolean enabledV) {
        if (mBluetoothAdapterV == null || mBluetoothGattV == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGattV.setCharacteristicNotification(characteristicV, enabledV);

        //BluetoothGattDescriptor descriptor = characteristic.getDescriptor(characteristic.getUuid());
        //descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        //mBluetoothGatt.writeDescriptor(descriptor);

        // This is specific to Heart Rate Measurement.
//        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
//            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
//                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            mBluetoothGatt.writeDescriptor(descriptor);
//        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServicesL() {
        if (mBluetoothGattL == null) return null;

        return mBluetoothGattL.getServices();
    }

    public List<BluetoothGattService> getSupportedGattServicesR() {
        if (mBluetoothGattR == null) return null;

        return mBluetoothGattR.getServices();
    }

    public List<BluetoothGattService> getSupportedGattServicesV() {
        if (mBluetoothGattV == null) return null;

        return mBluetoothGattV.getServices();
    }
    
    
}

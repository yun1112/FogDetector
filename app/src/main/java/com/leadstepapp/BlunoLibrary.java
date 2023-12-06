package com.leadstepapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public abstract class BlunoLibrary extends AppCompatActivity {

	private Context mainContext = this;

	//��Ҫ������Ȩ��
	private String[] mStrPermission = {
			Manifest.permission.ACCESS_FINE_LOCATION
	};

	private List<String> mPerList = new ArrayList<>();
	private List<String> mPerNoList = new ArrayList<>();

//	private List<String>  mPerListR   = new ArrayList<>();
//	private List<String>  mPerNoListR = new ArrayList<>();

	BLEDeviceAdapter bleDeviceAdapterL = null;
	BLEDeviceAdapter bleDeviceAdapterR = null;
	BLEDeviceAdapter bleDeviceAdapterV = null;
	String DEVICE_NAMEL[] = {"LaserL"};
	String DEVICE_NAMER[] = {"LaserR"};
	String DEVICE_NAMEV[] = {"Vibrator"};

	private OnPermissionsResult permissionsResultL;
	private OnPermissionsResult permissionsResultR;
	private OnPermissionsResult permissionsResultV;
	private int requestCodeL;
	private int requestCodeR;

//	public BlunoLibrary(Context theContext) {
//		
//		mainContext=theContext;
//	}

	public abstract void onConectionStateChangeL(connectionStateEnumL theconnectionStateEnumL);

	public abstract void onConectionStateChangeR(connectionStateEnumR theconnectionStateEnumR);

	public abstract void onConectionStateChangeV(connectionStateEnumV theconnectionStateEnumV);

	public abstract String onSerialReceivedL(String theString);

	public abstract String onSerialReceivedR(String theString);

	public abstract String onSerialReceivedV(String theString);

	public void serialSendL(String theString) {
		if (mConnectionStateL == connectionStateEnumL.isConnected) {
			mSCharacteristicL.setValue(theString);
			Log.d(TAG, "serialSendL: " + theString);
			mBluetoothLeServiceL.writeCharacteristicL(mSCharacteristicL);
		}
	}

	public void serialSendR(String theString) {
		if (mConnectionStateR == connectionStateEnumR.isConnected) {
			mSCharacteristicR.setValue(theString);
			mBluetoothLeServiceR.writeCharacteristicR(mSCharacteristicR);
		}
	}

	public void serialSendV(String theString) {
		if (mConnectionStateV == connectionStateEnumV.isConnected) {
			mSCharacteristicV.setValue(theString);
			mBluetoothLeServiceV.writeCharacteristicV(mSCharacteristicV);
		}
	}

	private int mBaudrateL = 115200;    //set the default baud rate to 115200
	private String mPasswordL = "AT+PASSWOR=DFRobot\r\n";

	private int mBaudrateR = 115200;    //set the default baud rate to 115200
	private String mPasswordR = "AT+PASSWOR=DFRobot\r\n";

	private int mBaudrateV = 115200;    //set the default baud rate to 115200
	private String mPasswordV = "AT+PASSWOR=DFRobot\r\n";


	private String mBaudrateBufferL = "AT+CURRUART=" + mBaudrateL + "\r\n";
	private String mBaudrateBufferR = "AT+CURRUART=" + mBaudrateR + "\r\n";
	private String mBaudrateBufferV = "AT+CURRUART=" + mBaudrateV + "\r\n";

//	byte[] mBaudrateBuffer={0x32,0x00,(byte) (mBaudrate & 0xFF),(byte) ((mBaudrate>>8) & 0xFF),(byte) ((mBaudrate>>16) & 0xFF),0x00};;


	public void serialBeginL(int baudL) {
		mBaudrateL = baudL;
		mBaudrateBufferL = "AT+CURRUART=" + mBaudrateL + "\r\n";
	}

	public void serialBeginR(int baudR) {
		mBaudrateR = baudR;
		mBaudrateBufferR = "AT+CURRUART=" + mBaudrateR + "\r\n";
	}

	public void serialBeginV(int baudV) {
		mBaudrateV = baudV;
		mBaudrateBufferV = "AT+CURRUART=" + mBaudrateV + "\r\n";
	}


	static class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
	}

	private static BluetoothGattCharacteristic mSCharacteristicL, mModelNumberCharacteristicL, mSerialPortCharacteristicL, mCommandCharacteristicL;
	private static BluetoothGattCharacteristic mSCharacteristicR, mModelNumberCharacteristicR, mSerialPortCharacteristicR, mCommandCharacteristicR;
	private static BluetoothGattCharacteristic mSCharacteristicV, mModelNumberCharacteristicV, mSerialPortCharacteristicV, mCommandCharacteristicV;
	BluetoothLeService mBluetoothLeServiceL;
	BluetoothLeService mBluetoothLeServiceR;
	BluetoothLeService mBluetoothLeServiceV;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristicsL = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristicsR = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristicsV = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	//	private LeDeviceListAdapter mLeDeviceListAdapterL=null;
//	private LeDeviceListAdapter mLeDeviceListAdapterR=null;
	private BluetoothAdapter mBluetoothAdapterL;
	private BluetoothAdapter mBluetoothAdapterR;
	private BluetoothAdapter mBluetoothAdapterV;
	private boolean mScanningL = false;
	private boolean mScanningR = false;
	private boolean mScanningV = false;
	AlertDialog mScanDeviceDialogL;
	AlertDialog mScanDeviceDialogR;
	AlertDialog mScanDeviceDialogV;
	private String mDeviceNameL, mDeviceNameR, mDeviceNameV;
	private String mDeviceAddressL, mDeviceAddressR, mDeviceAddressV;

	public enum connectionStateEnumL {isNull, isScanning, isToScan, isConnecting, isConnected, isDisconnecting}

	;

	public enum connectionStateEnumR {isNull, isScanning, isToScan, isConnecting, isConnected, isDisconnecting}

	;

	public enum connectionStateEnumV {isNull, isScanning, isToScan, isConnecting, isConnected, isDisconnecting}

	;
	public connectionStateEnumL mConnectionStateL = connectionStateEnumL.isNull;
	public connectionStateEnumR mConnectionStateR = connectionStateEnumR.isNull;
	public connectionStateEnumV mConnectionStateV = connectionStateEnumV.isNull;
	private static final int REQUEST_ENABLE_BTL = 1;
	private static final int REQUEST_ENABLE_BTR = 1;
	private static final int REQUEST_ENABLE_BTV = 1;

	private Handler mHandlerL = new Handler();
	private Handler mHandlerR = new Handler();
	private Handler mHandlerV = new Handler();

	public boolean mConnectedL = false;
	public boolean mConnectedR = false;
	public boolean mConnectedV = false;

	private final static String TAG = BlunoLibrary.class.getSimpleName();
	private String uid;
	private BluetoothDevice bleDevice;
	private ArrayAdapter<String> deviceName;
	private ArrayAdapter<String> deviceID;
	private Set<BluetoothDevice> pairedDevices;
	private String choseID;

	private Runnable mConnectingOverTimeRunnableL = new Runnable() {
		@Override
		public void run() {
			if (mConnectionStateL == connectionStateEnumL.isConnecting)
				mConnectionStateL = connectionStateEnumL.isToScan;
			onConectionStateChangeL(mConnectionStateL);
			mBluetoothLeServiceL.closeL();
		}
	};

	private Runnable mConnectingOverTimeRunnableR = new Runnable() {
		@Override
		public void run() {
			if (mConnectionStateR == connectionStateEnumR.isConnecting)
				mConnectionStateR = connectionStateEnumR.isToScan;
			onConectionStateChangeR(mConnectionStateR);
			mBluetoothLeServiceR.closeR();
		}
	};

	private Runnable mConnectingOverTimeRunnableV = new Runnable() {
		@Override
		public void run() {
			if (mConnectionStateV == connectionStateEnumV.isConnecting)
				mConnectionStateV = connectionStateEnumV.isToScan;
			onConectionStateChangeV(mConnectionStateV);
			mBluetoothLeServiceV.closeV();
		}
	};

	private Runnable mDisonnectingOverTimeRunnableL = new Runnable() {

		@Override
		public void run() {
			if (mConnectionStateL == connectionStateEnumL.isDisconnecting)
				mConnectionStateL = connectionStateEnumL.isToScan;
			onConectionStateChangeL(mConnectionStateL);
			mBluetoothLeServiceL.closeL();
		}
	};

	private Runnable mDisonnectingOverTimeRunnableR = new Runnable() {

		@Override
		public void run() {
			if (mConnectionStateR == connectionStateEnumR.isDisconnecting)
				mConnectionStateR = connectionStateEnumR.isToScan;
			onConectionStateChangeR(mConnectionStateR);
			mBluetoothLeServiceR.closeR();
		}
	};

	private Runnable mDisonnectingOverTimeRunnableV = new Runnable() {

		@Override
		public void run() {
			if (mConnectionStateV == connectionStateEnumV.isDisconnecting)
				mConnectionStateV = connectionStateEnumV.isToScan;
			onConectionStateChangeV(mConnectionStateV);
			mBluetoothLeServiceV.closeV();
		}
	};

	public static final String SerialPortUUIDL = "0000dfb1-0000-1000-8000-00805f9b34fb";
	public static final String SerialPortUUIDR = "0000dfb1-0000-1000-8000-00805f9b34fb";
	public static final String SerialPortUUIDV = "0000dfb1-0000-1000-8000-00805f9b34fb";
	public static final String CommandUUIDL = "0000dfb2-0000-1000-8000-00805f9b34fb";
	public static final String CommandUUIDR = "0000dfb2-0000-1000-8000-00805f9b34fb";
	public static final String CommandUUIDV = "0000dfb2-0000-1000-8000-00805f9b34fb";
	public static final String ModelNumberStringUUIDL = "00002a24-0000-1000-8000-00805f9b34fb";
	public static final String ModelNumberStringUUIDR = "00002a24-0000-1000-8000-00805f9b34fb";
	public static final String ModelNumberStringUUIDV = "00002a24-0000-1000-8000-00805f9b34fb";

	public void onCreateProcess() {
		if (!initiate()) {
			Toast.makeText(mainContext, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			((Activity) mainContext).finish();
		}

		deviceName = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1);
		deviceID = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1);

		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnectionL, Context.BIND_AUTO_CREATE);
		bindService(gattServiceIntent, mServiceConnectionR, Context.BIND_AUTO_CREATE);
		bindService(gattServiceIntent, mServiceConnectionV, Context.BIND_AUTO_CREATE);

		// Initializes list view adapter.
		bleDeviceAdapterL = new BLEDeviceAdapter(mainContext);
		bleDeviceAdapterR = new BLEDeviceAdapter(mainContext);
		bleDeviceAdapterV = new BLEDeviceAdapter(mainContext);
//		mLeDeviceListAdapterL = new LeDeviceListAdapter();
//		mLeDeviceListAdapterR = new LeDeviceListAdapter();
		// Initializes and show the scan Device Dialog
		mScanDeviceDialogL = new AlertDialog.Builder(mainContext)
				.setTitle("Laser L Scan...").setAdapter(bleDeviceAdapterL, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						final BluetoothDevice deviceL = bleDeviceAdapterL.getDevice(which);
						if (deviceL == null)
							return;
						scanLeDeviceL(false);

						if (ActivityCompat.checkSelfPermission(mainContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
							// TODO: Consider calling
							//    ActivityCompat#requestPermissions
							// here to request the missing permissions, and then overriding
							//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
							//                                          int[] grantResults)
							// to handle the case where the user grants the permission. See the documentation
							// for ActivityCompat#requestPermissions for more details.
//							return;
						}
						if (deviceL.getName() == null || deviceL.getAddress() == null) {
							mConnectionStateL = connectionStateEnumL.isToScan;
							onConectionStateChangeL(mConnectionStateL);
						} else {

							System.out.println("onListItemClick " + deviceL.getName().toString());

							System.out.println("Device Name:" + deviceL.getName() + "   " + "Device Name:" + deviceL.getAddress());

							mDeviceNameL = deviceL.getName().toString();
							mDeviceAddressL = deviceL.getAddress().toString();

							if (mBluetoothLeServiceL.connectL(mDeviceAddressL)) {
								Log.d(TAG, "Connect request success");
								mConnectionStateL = connectionStateEnumL.isConnecting;
								onConectionStateChangeL(mConnectionStateL);
								mHandlerL.postDelayed(mConnectingOverTimeRunnableL, 10000);
							} else {
								Log.d(TAG, "Connect request fail");
								mConnectionStateL = connectionStateEnumL.isToScan;
								onConectionStateChangeL(mConnectionStateL);
							}
						}
					}
				})
				.setOnCancelListener(new DialogInterface.OnCancelListener() {

					@Override
					public void onCancel(DialogInterface arg0) {
						System.out.println("mBluetoothAdapter.stopLeScan");

						mConnectionStateL = connectionStateEnumL.isToScan;
						onConectionStateChangeL(mConnectionStateL);

						mScanDeviceDialogL.dismiss();

						scanLeDeviceL(false);
					}
				}).create();


		mScanDeviceDialogR = new AlertDialog.Builder(mainContext)
				.setTitle("Laser R  Scan...").setAdapter(bleDeviceAdapterR, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						final BluetoothDevice deviceR = bleDeviceAdapterR.getDevice(which);
						if (deviceR == null)
							return;
						scanLeDeviceR(false);

						if (ActivityCompat.checkSelfPermission(mainContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
							// TODO: Consider calling
							//    ActivityCompat#requestPermissions
							// here to request the missing permissions, and then overriding
							//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
							//                                          int[] grantResults)
							// to handle the case where the user grants the permission. See the documentation
							// for ActivityCompat#requestPermissions for more details.
//							return;
						}
						if (deviceR.getName() == null || deviceR.getAddress() == null) {
							mConnectionStateR = connectionStateEnumR.isToScan;
							onConectionStateChangeR(mConnectionStateR);
						} else {

							System.out.println("onListItemClick " + deviceR.getName().toString());

							System.out.println("Device Name:" + deviceR.getName() + "   " + "Device Name:" + deviceR.getAddress());

							mDeviceNameR = deviceR.getName().toString();
							mDeviceAddressR = deviceR.getAddress().toString();

							if (mBluetoothLeServiceR.connectR(mDeviceAddressR)) {
								Log.d(TAG, "Connect request success");
								mConnectionStateR = connectionStateEnumR.isConnecting;
								onConectionStateChangeR(mConnectionStateR);
								mHandlerR.postDelayed(mConnectingOverTimeRunnableR, 10000);
							} else {
								Log.d(TAG, "Connect request fail");
								mConnectionStateR = connectionStateEnumR.isToScan;
								onConectionStateChangeR(mConnectionStateR);
							}
						}
					}
				})
				.setOnCancelListener(new DialogInterface.OnCancelListener() {

					@Override
					public void onCancel(DialogInterface arg0) {
						System.out.println("mBluetoothAdapter.stopLeScan");

						mConnectionStateR = connectionStateEnumR.isToScan;
						onConectionStateChangeR(mConnectionStateR);

						mScanDeviceDialogR.dismiss();

						scanLeDeviceR(false);
					}
				}).create();

		mScanDeviceDialogV = new AlertDialog.Builder(mainContext)
				.setTitle("Vibrator Scan...").setAdapter(bleDeviceAdapterV, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						final BluetoothDevice deviceV = bleDeviceAdapterV.getDevice(which);
						if (deviceV == null)
							return;
						scanLeDeviceV(false);

						if (ActivityCompat.checkSelfPermission(mainContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
							// TODO: Consider calling
							//    ActivityCompat#requestPermissions
							// here to request the missing permissions, and then overriding
							//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
							//                                          int[] grantResults)
							// to handle the case where the user grants the permission. See the documentation
							// for ActivityCompat#requestPermissions for more details.
//							return;
						}
						if (deviceV.getName() == null || deviceV.getAddress() == null) {
							mConnectionStateV = connectionStateEnumV.isToScan;
							onConectionStateChangeV(mConnectionStateV);
						} else {

							System.out.println("onListItemClick " + deviceV.getName().toString());

							System.out.println("Device Name:" + deviceV.getName() + "   " + "Device Name:" + deviceV.getAddress());

							mDeviceNameV = deviceV.getName().toString();
							mDeviceAddressV = deviceV.getAddress().toString();

							if (mBluetoothLeServiceV.connectV(mDeviceAddressV)) {
								Log.d(TAG, "Connect request success");
								mConnectionStateV = connectionStateEnumV.isConnecting;
								onConectionStateChangeV(mConnectionStateV);
								mHandlerV.postDelayed(mConnectingOverTimeRunnableV, 10000);
							} else {
								Log.d(TAG, "Connect request fail");
								mConnectionStateV = connectionStateEnumV.isToScan;
								onConectionStateChangeV(mConnectionStateV);
							}
						}
					}
				})
				.setOnCancelListener(new DialogInterface.OnCancelListener() {

					@Override
					public void onCancel(DialogInterface arg0) {
						System.out.println("mBluetoothAdapter.stopLeScan");

						mConnectionStateV = connectionStateEnumV.isToScan;
						onConectionStateChangeV(mConnectionStateV);

						mScanDeviceDialogV.dismiss();

						scanLeDeviceV(false);
					}
				}).create();
	}


	public void onResumeProcess() {
		System.out.println("BlUNOActivity onResume");
		// Ensures Bluetooth is enabled on the device. If Bluetooth is not
		// currently enabled,
		// fire an intent to display a dialog asking the user to grant
		// permission to enable it.
		if (!mBluetoothAdapterL.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
				// TODO: Consider calling
				//    ActivityCompat#requestPermissions
				// here to request the missing permissions, and then overriding
				//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
				//                                          int[] grantResults)
				// to handle the case where the user grants the permission. See the documentation
				// for ActivityCompat#requestPermissions for more details.
//					return;
			}
			((Activity) mainContext).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BTL);
			showBTDList(mBluetoothAdapterL);
		}

		if (!mBluetoothAdapterR.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			((Activity) mainContext).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BTR);
			showBTDList(mBluetoothAdapterR);
		}

//		if (!mBluetoothAdapterV.isEnabled()) {
//			if (!mBluetoothAdapterV.isEnabled()) {
//				Intent enableBtIntent = new Intent(
//						BluetoothAdapter.ACTION_REQUEST_ENABLE);
//				((Activity) mainContext).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BTV);
//			}
//		}

		mainContext.registerReceiver(mGattUpdateReceiverL, makeGattUpdateIntentFilterL());
		mainContext.registerReceiver(mGattUpdateReceiverR, makeGattUpdateIntentFilterR());
//		mainContext.registerReceiver(mGattUpdateReceiverV, makeGattUpdateIntentFilterV());

	}

	public BluetoothAdapter getBluetoothAdapterL() {
		return mBluetoothAdapterL;
	}

	public BluetoothAdapter getBluetoothAdapterR() {
		return mBluetoothAdapterR;
	}
	public void showBTDList(BluetoothAdapter mBluetoothAdapter) {
		if (ActivityCompat.checkSelfPermission(mainContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			pairedDevices = mBluetoothAdapter.getBondedDevices();

			final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					mainContext,
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
				mainContext);
		alertBuilder.setTitle("Choose to Bluetooth Device");

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
                        Toast.makeText(mainContext, "選擇了:" + choseID, Toast.LENGTH_SHORT).show();
//						deviceName.clear();
//
						try {
							connectBT(choseID);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				});
		alertBuilder.show();
	}

	private void connectBT(String choseID) throws IOException {
//		UUID uuid = UUID.fromString(_UUID); //藍芽模組UUID好像都是這樣

		if (pairedDevices != null) {
			for (BluetoothDevice device : pairedDevices) {
				bleDevice = device;
				System.out.println("device:"+device);

				if (device.getAddress().equals(choseID))
					break;
			}
		}

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
//            return;
		}

//		bluesoccket = bleDevice.createRfcommSocketToServiceRecord(uuid); //使用被選擇的設備UUID建立連線
//
//		if (bleDevice != null) { //DeviceID != null // 如果有找到設備
//			try {
//				mBluetoothAdapter.cancelDiscovery();
//				bluesoccket.connect();
//
//				if(!bluesoccket.isConnected()) return;
//				mmOuputStream = bluesoccket.getOutputStream();
//				mmInputStream = bluesoccket.getInputStream();
//				isChecked = true;
//
//				beginListenForData();
//
//				textDevice.setText(bleDevice.getName());
//				connectBtn.setText("DISCONNECT");
//				connectBtn.setBackgroundResource(R.drawable.edit_round_black);
//				Toast.makeText(myActivity, "Success to connect", Toast.LENGTH_SHORT).show();
//			} catch (SocketException e) {
//				Toast.makeText(myActivity, "Failed to connect", Toast.LENGTH_SHORT).show();
//				bluesoccket.close();
//			} catch (IOException e) {
//				Toast.makeText(myActivity, "Failed to connect", Toast.LENGTH_SHORT).show();
//				bluesoccket.close();
//			}
//		}

	}

	private void disconnectBTD() throws IOException {
//		bluesoccket.close();
//		imgPM.setImageResource(R.drawable.bluetooth);
//		connectBtn.setBackgroundResource(R.drawable.edit_round_white);
//		textDevice.setText("Device");
//		textStatus.setText("Disconnected");
//		textResult.setText("Connect to Bluetooth");
//		connectBtn.setText("CONNECT");
//		connectBtn.setTextColor(Color.parseColor("#FFFFFF"));
//		bgLayout.setBackground(myActivity.getResources().getDrawable(R.drawable.gradient_disconnect));
	}

	public void onPauseProcess() {
		System.out.println("BLUNOActivity onPause");
		scanLeDeviceL(false);
		scanLeDeviceR(false);
		scanLeDeviceV(false);
		mainContext.unregisterReceiver(mGattUpdateReceiverL);
		mainContext.unregisterReceiver(mGattUpdateReceiverR);
		mainContext.unregisterReceiver(mGattUpdateReceiverV);
		bleDeviceAdapterL.clear();
		bleDeviceAdapterR.clear();
		bleDeviceAdapterV.clear();
		mConnectionStateL = connectionStateEnumL.isToScan;
		onConectionStateChangeL(mConnectionStateL);
		mConnectionStateR = connectionStateEnumR.isToScan;
		onConectionStateChangeR(mConnectionStateR);
		mConnectionStateV = connectionStateEnumV.isToScan;
		onConectionStateChangeV(mConnectionStateV);
		mScanDeviceDialogL.dismiss();
		mScanDeviceDialogR.dismiss();
		mScanDeviceDialogV.dismiss();
		if (mBluetoothLeServiceL != null) {
			mBluetoothLeServiceL.disconnectL();
			mHandlerL.postDelayed(mDisonnectingOverTimeRunnableL, 10000);

//			mBluetoothLeService.close();
		}
		if (mBluetoothLeServiceR != null) {
			mBluetoothLeServiceR.disconnectR();
			mHandlerR.postDelayed(mDisonnectingOverTimeRunnableR, 10000);

//			mBluetoothLeService.close();
		}
		if (mBluetoothLeServiceV != null) {
			mBluetoothLeServiceV.disconnectV();
			mHandlerV.postDelayed(mDisonnectingOverTimeRunnableV, 10000);

//			mBluetoothLeService.close();
		}
		mSCharacteristicL = null;
		mSCharacteristicR = null;
		mSCharacteristicV = null;

	}


	public void onStopProcess() {
		System.out.println("MiUnoActivity onStop");
		if (mBluetoothLeServiceL != null) {
//			mBluetoothLeService.disconnect();
//            mHandler.postDelayed(mDisonnectingOverTimeRunnable, 10000);
			mHandlerL.removeCallbacks(mDisonnectingOverTimeRunnableL);
			mBluetoothLeServiceL.closeL();
		}
		mSCharacteristicL = null;

		if (mBluetoothLeServiceR != null) {
//			mBluetoothLeService.disconnect();
//            mHandler.postDelayed(mDisonnectingOverTimeRunnable, 10000);
			mHandlerR.removeCallbacks(mDisonnectingOverTimeRunnableR);
			mBluetoothLeServiceR.closeR();
		}
		mSCharacteristicR = null;

		if (mBluetoothLeServiceV != null) {
//			mBluetoothLeService.disconnect();
//            mHandler.postDelayed(mDisonnectingOverTimeRunnable, 10000);
			mHandlerV.removeCallbacks(mDisonnectingOverTimeRunnableV);
			mBluetoothLeServiceV.closeV();
		}
		mSCharacteristicV = null;
	}

	public void onDestroyProcess() {
		mainContext.unbindService(mServiceConnectionL);
		mainContext.unbindService(mServiceConnectionR);
		mainContext.unbindService(mServiceConnectionV);
		mBluetoothLeServiceL = null;
		mBluetoothLeServiceR = null;
		mBluetoothLeServiceV = null;
	}

	public void onActivityResultProcess(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		if (requestCode == REQUEST_ENABLE_BTL
				&& resultCode == Activity.RESULT_CANCELED) {
			((Activity) mainContext).finish();
			return;
		}

		if (requestCode == REQUEST_ENABLE_BTR
				&& resultCode == Activity.RESULT_CANCELED) {
			((Activity) mainContext).finish();
			return;
		}

		if (requestCode == REQUEST_ENABLE_BTV
				&& resultCode == Activity.RESULT_CANCELED) {
			((Activity) mainContext).finish();
			return;
		}
	}

	boolean initiate() {
		// Use this check to determine whether BLE is supported on the device.
		// Then you can
		// selectively disable BLE-related features.
		if (!mainContext.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			return false;
		}

		// Initializes a Bluetooth adapter. For API level 18 and above, get a
		// reference to
		// BluetoothAdapter through BluetoothManager.
		final BluetoothManager bluetoothManagerL = (BluetoothManager) mainContext.getSystemService(Context.BLUETOOTH_SERVICE);
		final BluetoothManager bluetoothManagerR = (BluetoothManager) mainContext.getSystemService(Context.BLUETOOTH_SERVICE);
		final BluetoothManager bluetoothManagerV = (BluetoothManager) mainContext.getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapterL = bluetoothManagerL.getAdapter();
		mBluetoothAdapterR = bluetoothManagerR.getAdapter();
		mBluetoothAdapterV = bluetoothManagerV.getAdapter();

		// Checks if Bluetooth is supported on the device.
		if (mBluetoothAdapterL == null) {
			return false;
		}

		if (mBluetoothAdapterR == null) {
			return false;
		}

		if (mBluetoothAdapterV == null) {
			return false;
		}
		return true;
	}

	// Handles various events fired by the Service.
	// ACTION_GATT_CONNECTED: connected to a GATT server.
	// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	// ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
	//                        or notification operations.
	private final BroadcastReceiver mGattUpdateReceiverL = new BroadcastReceiver() {
		@SuppressLint("DefaultLocale")
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			System.out.println("mGattUpdateReceiver->onReceive->action=" + action);
			if (BluetoothLeService.ACTION_GATT_CONNECTEDL.equals(action)) {
				mConnectedL = true;

				mHandlerL.removeCallbacks(mConnectingOverTimeRunnableL);

			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTEDL.equals(action)) {
				mConnectedL = false;

				mConnectionStateL = connectionStateEnumL.isToScan;
				onConectionStateChangeL(mConnectionStateL);
				mHandlerL.removeCallbacks(mDisonnectingOverTimeRunnableL);

				mBluetoothLeServiceL.closeL();
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVEREDL.equals(action)) {
				// Show all the supported services and characteristics on the user interface.
				for (BluetoothGattService gattServiceL : mBluetoothLeServiceL.getSupportedGattServicesL()) {
					System.out.println("ACTION_GATT_SERVICES_DISCOVERED  " +
							gattServiceL.getUuid().toString());
				}
				getGattServicesL(mBluetoothLeServiceL.getSupportedGattServicesL());
			} else if (BluetoothLeService.LaserL.equals(action)) {
				if (mSCharacteristicL == mModelNumberCharacteristicL) {
					if (intent.getStringExtra(BluetoothLeService.EXTRA_DATAL).toUpperCase().startsWith("DF BLUNO")) {
						mBluetoothLeServiceL.setCharacteristicNotificationL(mSCharacteristicL, false);
						mSCharacteristicL = mCommandCharacteristicL;
						mSCharacteristicL.setValue(mPasswordL);
						mBluetoothLeServiceL.writeCharacteristicL(mSCharacteristicL);
						mSCharacteristicL.setValue(mBaudrateBufferL);
						mBluetoothLeServiceL.writeCharacteristicL(mSCharacteristicL);
						mSCharacteristicL = mSerialPortCharacteristicL;
						mBluetoothLeServiceL.setCharacteristicNotificationL(mSCharacteristicL, true);
						mConnectionStateL = connectionStateEnumL.isConnected;
						onConectionStateChangeL(mConnectionStateL);
					} else {
						Toast.makeText(mainContext, "Please select DFRobot devices", Toast.LENGTH_SHORT).show();
						mConnectionStateL = connectionStateEnumL.isToScan;
						onConectionStateChangeL(mConnectionStateL);
					}
				} else if (mSCharacteristicL == mSerialPortCharacteristicL) {
					onSerialReceivedL(intent.getStringExtra(BluetoothLeService.EXTRA_DATAL));
				}


				System.out.println("displayData " + intent.getStringExtra(BluetoothLeService.EXTRA_DATAL));

//            	mPlainProtocol.mReceivedframe.append(intent.getStringExtra(BluetoothLeService.EXTRA_DATA)) ;
//            	System.out.print("mPlainProtocol.mReceivedframe:");
//            	System.out.println(mPlainProtocol.mReceivedframe.toString());


			}
		}
	};

	private final BroadcastReceiver mGattUpdateReceiverR = new BroadcastReceiver() {
		@SuppressLint("DefaultLocale")
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			System.out.println("mGattUpdateReceiver->onReceive->action=" + action);
			if (BluetoothLeService.ACTION_GATT_CONNECTEDR.equals(action)) {
				mConnectedR = true;

				mHandlerR.removeCallbacks(mConnectingOverTimeRunnableR);

			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTEDR.equals(action)) {
				mConnectedR = false;

				mConnectionStateR = connectionStateEnumR.isToScan;
				onConectionStateChangeR(mConnectionStateR);
				mHandlerR.removeCallbacks(mDisonnectingOverTimeRunnableR);

				mBluetoothLeServiceR.closeR();
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVEREDR.equals(action)) {
				// Show all the supported services and characteristics on the user interface.
				for (BluetoothGattService gattServiceR : mBluetoothLeServiceR.getSupportedGattServicesR()) {
					System.out.println("ACTION_GATT_SERVICES_DISCOVERED  " +
							gattServiceR.getUuid().toString());
				}
				getGattServicesR(mBluetoothLeServiceR.getSupportedGattServicesR());
			} else if (BluetoothLeService.LaserR.equals(action)) {
				if (mSCharacteristicR == mModelNumberCharacteristicR) {
					if (intent.getStringExtra(BluetoothLeService.EXTRA_DATAR).toUpperCase().startsWith("DF BLUNO")) {
						mBluetoothLeServiceR.setCharacteristicNotificationR(mSCharacteristicR, false);
						mSCharacteristicR = mCommandCharacteristicR;
						mSCharacteristicR.setValue(mPasswordR);
						mBluetoothLeServiceR.writeCharacteristicR(mSCharacteristicR);
						mSCharacteristicR.setValue(mBaudrateBufferR);
						mBluetoothLeServiceR.writeCharacteristicR(mSCharacteristicR);
						mSCharacteristicR = mSerialPortCharacteristicR;
						mBluetoothLeServiceR.setCharacteristicNotificationR(mSCharacteristicR, true);
						mConnectionStateR = connectionStateEnumR.isConnected;
						onConectionStateChangeR(mConnectionStateR);
					} else {
						Toast.makeText(mainContext, "Please select DFRobot devices", Toast.LENGTH_SHORT).show();
						mConnectionStateR = connectionStateEnumR.isToScan;
						onConectionStateChangeR(mConnectionStateR);
					}
				} else if (mSCharacteristicR == mSerialPortCharacteristicR) {
					onSerialReceivedR(intent.getStringExtra(BluetoothLeService.EXTRA_DATAR));
				}


				System.out.println("displayData " + intent.getStringExtra(BluetoothLeService.EXTRA_DATAR));

//            	mPlainProtocol.mReceivedframe.append(intent.getStringExtra(BluetoothLeService.EXTRA_DATA)) ;
//            	System.out.print("mPlainProtocol.mReceivedframe:");
//            	System.out.println(mPlainProtocol.mReceivedframe.toString());


			}
		}
	};

	private final BroadcastReceiver mGattUpdateReceiverV = new BroadcastReceiver() {
		@SuppressLint("DefaultLocale")
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			System.out.println("mGattUpdateReceiver->onReceive->action=" + action);
			if (BluetoothLeService.ACTION_GATT_CONNECTEDV.equals(action)) {
				mConnectedV = true;

				mHandlerV.removeCallbacks(mConnectingOverTimeRunnableV);

			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTEDV.equals(action)) {
				mConnectedV = false;

				mConnectionStateV = connectionStateEnumV.isToScan;
				onConectionStateChangeV(mConnectionStateV);
				mHandlerV.removeCallbacks(mDisonnectingOverTimeRunnableV);

				mBluetoothLeServiceV.closeV();
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVEREDV.equals(action)) {
				// Show all the supported services and characteristics on the user interface.
				for (BluetoothGattService gattServiceV : mBluetoothLeServiceV.getSupportedGattServicesV()) {
					System.out.println("ACTION_GATT_SERVICES_DISCOVERED  " +
							gattServiceV.getUuid().toString());
				}
				getGattServicesV(mBluetoothLeServiceV.getSupportedGattServicesV());
			} else if (BluetoothLeService.Vibrator.equals(action)) {
				if (mSCharacteristicV == mModelNumberCharacteristicV) {
					if (intent.getStringExtra(BluetoothLeService.EXTRA_DATAV).toUpperCase().startsWith("DF BLUNO")) {
						mBluetoothLeServiceV.setCharacteristicNotificationV(mSCharacteristicV, false);
						mSCharacteristicV = mCommandCharacteristicV;
						mSCharacteristicV.setValue(mPasswordV);
						mBluetoothLeServiceV.writeCharacteristicV(mSCharacteristicV);
						mSCharacteristicV.setValue(mBaudrateBufferV);
						mBluetoothLeServiceV.writeCharacteristicV(mSCharacteristicV);
						mSCharacteristicV = mSerialPortCharacteristicV;
						mBluetoothLeServiceV.setCharacteristicNotificationV(mSCharacteristicV, true);
						mConnectionStateV = connectionStateEnumV.isConnected;
						onConectionStateChangeV(mConnectionStateV);
					} else {
						Toast.makeText(mainContext, "Please select DFRobot devices", Toast.LENGTH_SHORT).show();
						mConnectionStateV = connectionStateEnumV.isToScan;
						onConectionStateChangeV(mConnectionStateV);
					}
				} else if (mSCharacteristicV == mSerialPortCharacteristicV) {
					onSerialReceivedV(intent.getStringExtra(BluetoothLeService.EXTRA_DATAV));
				}


				System.out.println("displayData " + intent.getStringExtra(BluetoothLeService.EXTRA_DATAV));

//            	mPlainProtocol.mReceivedframe.append(intent.getStringExtra(BluetoothLeService.EXTRA_DATA)) ;
//            	System.out.print("mPlainProtocol.mReceivedframe:");
//            	System.out.println(mPlainProtocol.mReceivedframe.toString());


			}
		}
	};

	void buttonScanOnClickProcessL() {
		switch (mConnectionStateL) {
			case isNull:
				mConnectionStateL = connectionStateEnumL.isScanning;
				onConectionStateChangeL(mConnectionStateL);
				scanLeDeviceL(true);
				mScanDeviceDialogL.show();
				break;
			case isToScan:
				mConnectionStateL = connectionStateEnumL.isScanning;
				onConectionStateChangeL(mConnectionStateL);
				scanLeDeviceL(true);
				mScanDeviceDialogL.show();
				break;
			case isScanning:

				break;

			case isConnecting:

				break;
			case isConnected:
				mBluetoothLeServiceL.disconnectL();
				mHandlerL.postDelayed(mDisonnectingOverTimeRunnableL, 10000);

//			mBluetoothLeService.close();
				mConnectionStateL = connectionStateEnumL.isDisconnecting;
				onConectionStateChangeL(mConnectionStateL);
				break;
			case isDisconnecting:

				break;

			default:
				break;
		}


	}

	void buttonScanOnClickProcessR() {
		switch (mConnectionStateR) {
			case isNull:
				mConnectionStateR = connectionStateEnumR.isScanning;
				onConectionStateChangeR(mConnectionStateR);
				scanLeDeviceR(true);
				mScanDeviceDialogR.show();
				break;
			case isToScan:
				mConnectionStateR = connectionStateEnumR.isScanning;
				onConectionStateChangeR(mConnectionStateR);
				scanLeDeviceR(true);
				mScanDeviceDialogR.show();
				break;
			case isScanning:

				break;

			case isConnecting:

				break;
			case isConnected:
				mBluetoothLeServiceR.disconnectR();
				mHandlerR.postDelayed(mDisonnectingOverTimeRunnableR, 10000);

//			mBluetoothLeService.close();
				mConnectionStateR = connectionStateEnumR.isDisconnecting;
				onConectionStateChangeR(mConnectionStateR);
				break;
			case isDisconnecting:

				break;

			default:
				break;
		}


	}

	void buttonScanOnClickProcessV() {
		switch (mConnectionStateV) {
			case isNull:
				mConnectionStateV = connectionStateEnumV.isScanning;
				onConectionStateChangeV(mConnectionStateV);
				scanLeDeviceV(true);
				mScanDeviceDialogV.show();
				break;
			case isToScan:
				mConnectionStateV = connectionStateEnumV.isScanning;
				onConectionStateChangeV(mConnectionStateV);
				scanLeDeviceV(true);
				mScanDeviceDialogV.show();
				break;
			case isScanning:

				break;

			case isConnecting:

				break;
			case isConnected:
				mBluetoothLeServiceV.disconnectV();
				mHandlerV.postDelayed(mDisonnectingOverTimeRunnableV, 10000);

//			mBluetoothLeService.close();
				mConnectionStateV = connectionStateEnumV.isDisconnecting;
				onConectionStateChangeV(mConnectionStateV);
				break;
			case isDisconnecting:

				break;

			default:
				break;
		}


	}

	void scanLeDeviceL(final boolean enable) {
		if (enable) {
			// Stops scanning after a pre-defined scan period.

			System.out.println("mBluetoothAdapter.startLeScan");

			if (bleDeviceAdapterL != null) {
				bleDeviceAdapterL.clear();
				bleDeviceAdapterL.notifyDataSetChanged();
			}

			if (!mScanningL) {
				mScanningL = true;
				if (Build.VERSION.SDK_INT < 21){
					if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
						// TODO: Consider calling
						//    ActivityCompat#requestPermissions
						// here to request the missing permissions, and then overriding
						//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
						//                                          int[] grantResults)
						// to handle the case where the user grants the permission. See the documentation
						// for ActivityCompat#requestPermissions for more details.
//						return;
					}
					mBluetoothAdapterL.startLeScan(mLeScanCallbackL);
				}
				else {
					mBluetoothAdapterL.getBluetoothLeScanner().startScan(scanCallbackL);
				}
			}
		} else {
			if (mScanningL) {
				mScanningL = false;
				mBluetoothAdapterL.stopLeScan(mLeScanCallbackL);
			}
		}
	}

	void scanLeDeviceR(final boolean enable) {
		if (enable) {
			// Stops scanning after a pre-defined scan period.

			System.out.println("mBluetoothAdapter.startLeScan");

			if (bleDeviceAdapterR != null) {
				bleDeviceAdapterR.clear();
				bleDeviceAdapterR.notifyDataSetChanged();
			}

			if (!mScanningR) {
				mScanningR = true;
				if (Build.VERSION.SDK_INT < 21) {
					if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
						// TODO: Consider calling
						//    ActivityCompat#requestPermissions
						// here to request the missing permissions, and then overriding
						//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
						//                                          int[] grantResults)
						// to handle the case where the user grants the permission. See the documentation
						// for ActivityCompat#requestPermissions for more details.
//						return;
					}
					mBluetoothAdapterR.startLeScan(mLeScanCallbackR);
				}
				else {
					mBluetoothAdapterR.getBluetoothLeScanner().startScan(scanCallbackR);
				}
			}
		} else {
			if (mScanningR) {
				mScanningR = false;
				if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
					// TODO: Consider calling
					//    ActivityCompat#requestPermissions
					// here to request the missing permissions, and then overriding
					//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
					//                                          int[] grantResults)
					// to handle the case where the user grants the permission. See the documentation
					// for ActivityCompat#requestPermissions for more details.
//					return;
				}
				mBluetoothAdapterR.stopLeScan(mLeScanCallbackR);
			}
		}
	}

	void scanLeDeviceV(final boolean enable) {
		if (enable) {
			// Stops scanning after a pre-defined scan period.

			System.out.println("mBluetoothAdapter.startLeScan");

			if (bleDeviceAdapterV != null) {
				bleDeviceAdapterV.clear();
				bleDeviceAdapterV.notifyDataSetChanged();
			}

			if (!mScanningV) {
				mScanningV = true;
				if (Build.VERSION.SDK_INT < 21) {
					if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
						// TODO: Consider calling
						//    ActivityCompat#requestPermissions
						// here to request the missing permissions, and then overriding
						//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
						//                                          int[] grantResults)
						// to handle the case where the user grants the permission. See the documentation
						// for ActivityCompat#requestPermissions for more details.
//						return;
					}
					mBluetoothAdapterV.startLeScan(mLeScanCallbackV);
				}
				else {
					mBluetoothAdapterV.getBluetoothLeScanner().startScan(scanCallbackV);
				}
			}
		} else {
			if (mScanningV) {
				mScanningV = false;
				mBluetoothAdapterV.stopLeScan(mLeScanCallbackV);
			}
		}
	}

	// Code to manage Service lifecycle.
	ServiceConnection mServiceConnectionL = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			System.out.println("mServiceConnection onServiceConnected");
			mBluetoothLeServiceL = ((BluetoothLeService.LocalBinder) service).getService();
			if (!mBluetoothLeServiceL.initializeL()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				((Activity) mainContext).finish();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			System.out.println("mServiceConnection onServiceDisconnected");
			mBluetoothLeServiceL = null;
		}
	};

	ServiceConnection mServiceConnectionR = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			System.out.println("mServiceConnection onServiceConnected");
			mBluetoothLeServiceR = ((BluetoothLeService.LocalBinder) service).getService();
			if (!mBluetoothLeServiceR.initializeR()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				((Activity) mainContext).finish();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			System.out.println("mServiceConnection onServiceDisconnected");
			mBluetoothLeServiceR = null;
		}
	};

	ServiceConnection mServiceConnectionV = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			System.out.println("mServiceConnection onServiceConnected");
			mBluetoothLeServiceV = ((BluetoothLeService.LocalBinder) service).getService();
			if (!mBluetoothLeServiceV.initializeV()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				((Activity) mainContext).finish();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			System.out.println("mServiceConnection onServiceDisconnected");
			mBluetoothLeServiceV = null;
		}
	};

	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallbackL = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
							 byte[] scanRecord) {
			((Activity) mainContext).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					System.out.println("mLeScanCallback onLeScan run ");
					bleDeviceAdapterL.addDevice(device);
					bleDeviceAdapterL.notifyDataSetChanged();
				}
			});
		}
	};

	private BluetoothAdapter.LeScanCallback mLeScanCallbackR = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
							 byte[] scanRecord) {
			((Activity) mainContext).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					System.out.println("mLeScanCallback onLeScan run ");
					bleDeviceAdapterR.addDevice(device);
					bleDeviceAdapterR.notifyDataSetChanged();
				}
			});
		}
	};

	private BluetoothAdapter.LeScanCallback mLeScanCallbackV = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
							 byte[] scanRecord) {
			((Activity) mainContext).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					System.out.println("mLeScanCallback onLeScan run ");
					bleDeviceAdapterV.addDevice(device);
					bleDeviceAdapterV.notifyDataSetChanged();
				}
			});
		}
	};

	private void getGattServicesL(List<BluetoothGattService> gattServicesL) {
		if (gattServicesL == null) return;
		String uuidL = null;
		mModelNumberCharacteristicL = null;
		mSerialPortCharacteristicL = null;
		mCommandCharacteristicL = null;
		mGattCharacteristicsL = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

		// Loops through available GATT Services.
		for (BluetoothGattService gattServiceL : gattServicesL) {
			uuidL = gattServiceL.getUuid().toString();
			System.out.println("displayGattServices + uuid=" + uuidL);

			List<BluetoothGattCharacteristic> gattCharacteristicsL =
					gattServiceL.getCharacteristics();
			ArrayList<BluetoothGattCharacteristic> charasL =
					new ArrayList<BluetoothGattCharacteristic>();

			// Loops through available Characteristics.
			for (BluetoothGattCharacteristic gattCharacteristicL : gattCharacteristicsL) {
				charasL.add(gattCharacteristicL);
				uuidL = gattCharacteristicL.getUuid().toString();
				if (uuidL.equals(ModelNumberStringUUIDL)) {
					mModelNumberCharacteristicL = gattCharacteristicL;
					System.out.println("mModelNumberCharacteristic  " + mModelNumberCharacteristicL.getUuid().toString());
				} else if (uuidL.equals(SerialPortUUIDL)) {
					mSerialPortCharacteristicL = gattCharacteristicL;
					System.out.println("mSerialPortCharacteristic  " + mSerialPortCharacteristicL.getUuid().toString());
//                    updateConnectionState(R.string.comm_establish);
				} else if (uuidL.equals(CommandUUIDL)) {
					mCommandCharacteristicL = gattCharacteristicL;
					System.out.println("mSerialPortCharacteristic  " + mSerialPortCharacteristicL.getUuid().toString());
//                    updateConnectionState(R.string.comm_establish);
				}
			}
			mGattCharacteristicsL.add(charasL);
		}

		if (mModelNumberCharacteristicL == null || mSerialPortCharacteristicL == null || mCommandCharacteristicL == null) {
			Toast.makeText(mainContext, "Please select DFRobot devices", Toast.LENGTH_SHORT).show();
			mConnectionStateL = connectionStateEnumL.isToScan;
			onConectionStateChangeL(mConnectionStateL);
		} else {
			mSCharacteristicL = mModelNumberCharacteristicL;
			mBluetoothLeServiceL.setCharacteristicNotificationL(mSCharacteristicL, true);
			mBluetoothLeServiceL.readCharacteristicL(mSCharacteristicL);
		}

	}

	private void getGattServicesR(List<BluetoothGattService> gattServicesR) {
		if (gattServicesR == null) return;
		String uuidR = null;
		mModelNumberCharacteristicR = null;
		mSerialPortCharacteristicR = null;
		mCommandCharacteristicR = null;
		mGattCharacteristicsR = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

		// Loops through available GATT Services.
		for (BluetoothGattService gattServiceR : gattServicesR) {
			uuidR = gattServiceR.getUuid().toString();
			System.out.println("displayGattServices + uuid=" + uuidR);

			List<BluetoothGattCharacteristic> gattCharacteristicsR =
					gattServiceR.getCharacteristics();
			ArrayList<BluetoothGattCharacteristic> charasR =
					new ArrayList<BluetoothGattCharacteristic>();

			// Loops through available Characteristics.
			for (BluetoothGattCharacteristic gattCharacteristicR : gattCharacteristicsR) {
				charasR.add(gattCharacteristicR);
				uuidR = gattCharacteristicR.getUuid().toString();
				if (uuidR.equals(ModelNumberStringUUIDR)) {
					mModelNumberCharacteristicR = gattCharacteristicR;
					System.out.println("mModelNumberCharacteristic  " + mModelNumberCharacteristicR.getUuid().toString());
				} else if (uuidR.equals(SerialPortUUIDR)) {
					mSerialPortCharacteristicR = gattCharacteristicR;
					System.out.println("mSerialPortCharacteristic  " + mSerialPortCharacteristicR.getUuid().toString());
//                    updateConnectionState(R.string.comm_establish);
				} else if (uuidR.equals(CommandUUIDR)) {
					mCommandCharacteristicR = gattCharacteristicR;
					System.out.println("mSerialPortCharacteristic  " + mSerialPortCharacteristicR.getUuid().toString());
//                    updateConnectionState(R.string.comm_establish);
				}
			}
			mGattCharacteristicsR.add(charasR);
		}

		if (mModelNumberCharacteristicR == null || mSerialPortCharacteristicR == null || mCommandCharacteristicR == null) {
			Toast.makeText(mainContext, "Please select DFRobot devices", Toast.LENGTH_SHORT).show();
			mConnectionStateR = connectionStateEnumR.isToScan;
			onConectionStateChangeR(mConnectionStateR);
		} else {
			mSCharacteristicR = mModelNumberCharacteristicR;
			mBluetoothLeServiceR.setCharacteristicNotificationR(mSCharacteristicR, true);
			mBluetoothLeServiceR.readCharacteristicR(mSCharacteristicR);
		}

	}

	private void getGattServicesV(List<BluetoothGattService> gattServicesV) {
		if (gattServicesV == null) return;
		String uuidV = null;
		mModelNumberCharacteristicV = null;
		mSerialPortCharacteristicV = null;
		mCommandCharacteristicV = null;
		mGattCharacteristicsV = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

		// Loops through available GATT Services.
		for (BluetoothGattService gattServiceV : gattServicesV) {
			uuidV = gattServiceV.getUuid().toString();
			System.out.println("displayGattServices + uuid=" + uuidV);

			List<BluetoothGattCharacteristic> gattCharacteristicsV =
					gattServiceV.getCharacteristics();
			ArrayList<BluetoothGattCharacteristic> charasV =
					new ArrayList<BluetoothGattCharacteristic>();

			// Loops through available Characteristics.
			for (BluetoothGattCharacteristic gattCharacteristicV : gattCharacteristicsV) {
				charasV.add(gattCharacteristicV);
				uuidV = gattCharacteristicV.getUuid().toString();
				if (uuidV.equals(ModelNumberStringUUIDV)) {
					mModelNumberCharacteristicV = gattCharacteristicV;
					System.out.println("mModelNumberCharacteristic  " + mModelNumberCharacteristicV.getUuid().toString());
				} else if (uuidV.equals(SerialPortUUIDV)) {
					mSerialPortCharacteristicV = gattCharacteristicV;
					System.out.println("mSerialPortCharacteristic  " + mSerialPortCharacteristicV.getUuid().toString());
//                    updateConnectionState(R.string.comm_establish);
				} else if (uuidV.equals(CommandUUIDV)) {
					mCommandCharacteristicV = gattCharacteristicV;
					System.out.println("mSerialPortCharacteristic  " + mSerialPortCharacteristicV.getUuid().toString());
//                    updateConnectionState(R.string.comm_establish);
				}
			}
			mGattCharacteristicsV.add(charasV);
		}

		if (mModelNumberCharacteristicV == null || mSerialPortCharacteristicV == null || mCommandCharacteristicV == null) {
			Toast.makeText(mainContext, "Please select DFRobot devices", Toast.LENGTH_SHORT).show();
			mConnectionStateV = connectionStateEnumV.isToScan;
			onConectionStateChangeV(mConnectionStateV);
		} else {
			mSCharacteristicV = mModelNumberCharacteristicV;
			mBluetoothLeServiceV.setCharacteristicNotificationV(mSCharacteristicV, true);
			mBluetoothLeServiceV.readCharacteristicV(mSCharacteristicV);
		}

	}

	private static IntentFilter makeGattUpdateIntentFilterL() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTEDL);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTEDL);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVEREDL);
		intentFilter.addAction(BluetoothLeService.LaserL);
		return intentFilter;
	}

	private static IntentFilter makeGattUpdateIntentFilterR() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTEDR);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTEDR);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVEREDR);
		intentFilter.addAction(BluetoothLeService.LaserR);
		return intentFilter;
	}

	private static IntentFilter makeGattUpdateIntentFilterV() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTEDV);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTEDV);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVEREDV);
		intentFilter.addAction(BluetoothLeService.Vibrator);
		return intentFilter;
	}

	private class LeDeviceListAdapter extends BaseAdapter {
		private ArrayList<BluetoothDevice> mLeDevices;
		private LayoutInflater mInflator;

		public LeDeviceListAdapter() {
			super();
			mLeDevices = new ArrayList<BluetoothDevice>();
			mInflator = ((Activity) mainContext).getLayoutInflater();
		}

		public void addDevice(BluetoothDevice device) {
			if (!mLeDevices.contains(device)) {
				mLeDevices.add(device);
			}
		}

		public BluetoothDevice getDevice(int position) {
			return mLeDevices.get(position);
		}

		public void clear() {
			mLeDevices.clear();
		}

		@Override
		public int getCount() {
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int i) {
			return mLeDevices.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			ViewHolder viewHolder;
			// General ListView optimization code.
			if (view == null) {
				view = mInflator.inflate(R.layout.listitem_device, null);
				viewHolder = new ViewHolder();
				viewHolder.deviceAddress = (TextView) view
						.findViewById(R.id.device_address);
				viewHolder.deviceName = (TextView) view
						.findViewById(R.id.device_name);
				System.out.println("mInflator.inflate  getView");
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			BluetoothDevice device = mLeDevices.get(i);
			if (ActivityCompat.checkSelfPermission(mainContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
				// TODO: Consider calling
				//    ActivityCompat#requestPermissions
				// here to request the missing permissions, and then overriding
				//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
				//                                          int[] grantResults)
				// to handle the case where the user grants the permission. See the documentation
				// for ActivityCompat#requestPermissions for more details.
				View TODO = null;
				return TODO;
			}
			final String deviceName = device.getName();
			if (deviceName != null && deviceName.length() > 0)
				viewHolder.deviceName.setText(deviceName);
			else
				viewHolder.deviceName.setText(R.string.unknown_device);
			viewHolder.deviceAddress.setText(device.getAddress());

			return view;
		}
	}

	/**
	 *
	 * @param requestCode
	 * @param permissionsResult
	 */
	public void request(int requestCode, OnPermissionsResult permissionsResult) {
		if (!checkPermissionsAll()) {
			requestPermissionAll(requestCode, permissionsResult);
		}
	}

	/**
	 * �ж����󵥸�Ȩ��
	 * @param permissions
	 * @return
	 */
	protected boolean checkPermissions(String permissions) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			int check = checkSelfPermission(permissions);
			return check == PackageManager.PERMISSION_GRANTED;
		}
		return false;
	}

	/**
	 * �ж�����Ȩ����
	 * @return
	 */
	protected boolean checkPermissionsAll() {
		mPerList.clear();
		for (int i = 0; i < mStrPermission.length; i++) {
			boolean check = checkPermissions(mStrPermission[i]);
			if (!check) {
				mPerList.add(mStrPermission[i]);
			}
		}
		return mPerList.size() > 0 ? false : true;
	}

	/**
	 * ���󵥸�Ȩ��
	 * @param mPermissions
	 * @param requestCode
	 */
	protected void requestPermission(String[] mPermissions, int requestCode) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			requestPermissions(mPermissions, requestCode);
		}
	}

	/**
	 *����Ȩ��
	 * @param requestCode
	 */
	protected void requestPermissionAll(int requestCode, OnPermissionsResult permissionsResult) {
		this.permissionsResultL = permissionsResult;
		this.permissionsResultR = permissionsResult;
		this.permissionsResultV = permissionsResult;
		requestPermission((String[]) mPerList.toArray(new String[mPerList.size()]), requestCode);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == requestCode) {
			if (grantResults.length > 0) {
				for (int i = 0; i < grantResults.length; i++) {
					if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
						System.out.println(permissions[i]);
						//�����ʧ��
						mPerNoList.add(permissions[i]);
					}
				}
				if (permissionsResultL != null) {
					if (mPerNoList.size() == 0) {
						permissionsResultL.OnSuccess();
					} else {
						permissionsResultL.OnFail(mPerNoList);
					}
				}
				if (permissionsResultR != null) {
					if (mPerNoList.size() == 0) {
						permissionsResultR.OnSuccess();
					} else {
						permissionsResultR.OnFail(mPerNoList);
					}
				}
				if (permissionsResultV != null) {
					if (mPerNoList.size() == 0) {
						permissionsResultV.OnSuccess();
					} else {
						permissionsResultV.OnFail(mPerNoList);
					}
				}
			}
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	public interface OnPermissionsResult {
		void OnSuccess();

		void OnFail(List<String> noPermissions);
	}

	private ScanCallback scanCallbackL = new ScanCallback() {
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			byte[] scanData = result.getScanRecord().getBytes();
			//把byte数组转成16进制字符串，方便查看
			Log.e("TAG", "onScanResult :" + toHexStringL(scanData));
			Log.e("TAG", "onScanResult :" + result.getScanRecord().toString());
			final BluetoothDevice device = result.getDevice();
			((Activity) mainContext).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					System.out.println("mLeScanCallback onLeScan run ");
					int opt = 0;
					if (device != null)
						for (String str : DEVICE_NAMEL) {
							if (ActivityCompat.checkSelfPermission(mainContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
								// TODO: Consider calling
								//    ActivityCompat#requestPermissions
								// here to request the missing permissions, and then overriding
								//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
								//                                          int[] grantResults)
								// to handle the case where the user grants the permission. See the documentation
								// for ActivityCompat#requestPermissions for more details.
								return;
							}
							if (str.equals(device.getName())) {
								opt = 1;
							}
						}
					if (opt == 1)
						bleDeviceAdapterL.addDevice(device);
					bleDeviceAdapterL.notifyDataSetChanged();
//                    mLeDeviceListAdapter.addDevice(device);
//                    mLeDeviceListAdapter.notifyDataSetChanged();
				}
			});
		}

		@Override
		public void onBatchScanResults(List<ScanResult> results) {
			super.onBatchScanResults(results);
		}

		@Override
		public void onScanFailed(int errorCode) {
			super.onScanFailed(errorCode);
		}
	};

	public static String toHexStringL(byte[] bytes) {
		StringBuilder hexString = new StringBuilder();

		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}

		return hexString.toString();
	}

	private ScanCallback scanCallbackR = new ScanCallback() {
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			byte[] scanData = result.getScanRecord().getBytes();
			//把byte数组转成16进制字符串，方便查看
			Log.e("TAG", "onScanResult :" + toHexStringR(scanData));
			Log.e("TAG", "onScanResult :" + result.getScanRecord().toString());
			final BluetoothDevice device = result.getDevice();
			((Activity) mainContext).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					System.out.println("mLeScanCallback onLeScan run ");
					int opt = 0;
					if (device != null)
						for (String str : DEVICE_NAMER) {
							if (ActivityCompat.checkSelfPermission(mainContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
								// TODO: Consider calling
								//    ActivityCompat#requestPermissions
								// here to request the missing permissions, and then overriding
								//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
								//                                          int[] grantResults)
								// to handle the case where the user grants the permission. See the documentation
								// for ActivityCompat#requestPermissions for more details.
								return;
							}
							if (str.equals(device.getName())) {
								opt = 1;
							}
						}
					if (opt == 1)
						bleDeviceAdapterR.addDevice(device);
					bleDeviceAdapterR.notifyDataSetChanged();
//                    mLeDeviceListAdapter.addDevice(device);
//                    mLeDeviceListAdapter.notifyDataSetChanged();
				}
			});
		}

		@Override
		public void onBatchScanResults(List<ScanResult> results) {
			super.onBatchScanResults(results);
		}

		@Override
		public void onScanFailed(int errorCode) {
			super.onScanFailed(errorCode);
		}
	};

	public static String toHexStringR(byte[] bytes) {
		StringBuilder hexString = new StringBuilder();

		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}

		return hexString.toString();
	}

	private ScanCallback scanCallbackV = new ScanCallback() {
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			byte[] scanData = result.getScanRecord().getBytes();
			//把byte数组转成16进制字符串，方便查看
			Log.e("TAG", "onScanResult :" + toHexStringV(scanData));
			Log.e("TAG", "onScanResult :" + result.getScanRecord().toString());
			final BluetoothDevice device = result.getDevice();
			((Activity) mainContext).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					System.out.println("mLeScanCallback onLeScan run ");
					int opt = 0;
					if (device != null)
						for (String str : DEVICE_NAMEV) {
							if (ActivityCompat.checkSelfPermission(mainContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
								// TODO: Consider calling
								//    ActivityCompat#requestPermissions
								// here to request the missing permissions, and then overriding
								//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
								//                                          int[] grantResults)
								// to handle the case where the user grants the permission. See the documentation
								// for ActivityCompat#requestPermissions for more details.
								return;
							}
							if (str.equals(device.getName())) {
								opt = 1;
							}
						}
					if(opt == 1)
						bleDeviceAdapterV.addDevice(device);
					bleDeviceAdapterV.notifyDataSetChanged();
//                    mLeDeviceListAdapter.addDevice(device);
//                    mLeDeviceListAdapter.notifyDataSetChanged();
				}
			});
		}
		@Override
		public void onBatchScanResults(List<ScanResult> results) {
			super.onBatchScanResults(results);
		}

		@Override
		public void onScanFailed(int errorCode) {
			super.onScanFailed(errorCode);
		}
	};

	public static String toHexStringV(byte[] bytes) {
		StringBuilder hexString = new StringBuilder();

		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}

		return hexString.toString();
	}
}

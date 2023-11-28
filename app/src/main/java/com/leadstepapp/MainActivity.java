package com.leadstepapp;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends BlunoLibrary {
	private Button buttonScanL, buttonScanR, buttonScanV;
	private Button buttonStartL, buttonStartR;
	private Button buttonStopL, buttonStopR;
	private Button buttonStart, buttonStop;
	private TextView serialReceivedTextL, serialReceivedTextR;

	private SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
	SimpleDateFormat sdf3 = new SimpleDateFormat("HH:mm:ss.SSS");

	String LL = "", LR = "", dataTimeDelay="";
	Timer timer = new Timer(true);
	int sequenceCount = 0;
	int writeCount = 0;

	private Timer carousalTimer;
	private Timer carousalTimerL;
	private Timer carousalTimerR;
	private Timer carousalTimerV;
	String data = "";
	String timeDL = "", timeDR= "", timeDV= "";

	private int indexL = 0, indexR = 0, indexV = 0;


	List LList = new ArrayList();
	List RList = new ArrayList();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		request(1000, new OnPermissionsResult() {
			@Override
			public void OnSuccess() {
				Toast.makeText(MainActivity.this,"权限请求成功",Toast.LENGTH_SHORT).show();
			}

			@Override
			public void OnFail(List<String> noPermissions) {
				Toast.makeText(MainActivity.this,"权限请求失败",Toast.LENGTH_SHORT).show();
			}
		});

        onCreateProcess();														//onCreate Process by BlunoLibrary


        serialBeginL(115200);													//set the Uart Baudrate on BLE chip to 115200
		serialBeginR(115200);
		serialBeginV(115200);


        serialReceivedTextL = (TextView) findViewById(R.id.serialReveicedTextL);	//initial the EditText of the received data
		serialReceivedTextR = (TextView) findViewById(R.id.serialReveicedTextR);	//initial the EditText of the received data

		buttonScanR = (Button) findViewById(R.id.btnScanR);		//initial the button for sending the data
		buttonScanR.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				buttonScanOnClickProcessR();

//				serialSend(serialSendText.getText().toString());				//send the data to the BLUNO
			}
		});

        buttonScanL = (Button) findViewById(R.id.btnScanL);					//initial the button for scanning the BLE device
        buttonScanL.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				buttonScanOnClickProcessL();										//Alert Dialog for selecting the BLE device
			}
		});

		buttonScanV = (Button) findViewById(R.id.btnScanVib);					//initial the button for scanning the BLE device
		buttonScanV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				buttonScanOnClickProcessV();										//Alert Dialog for selecting the BLE device
			}
		});

		buttonStartL = (Button)findViewById(R.id.btnStartL);
		buttonStartL.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {

				serialSendL("a");
				Toast.makeText(MainActivity.this, "start L", Toast.LENGTH_SHORT).show();
			}
		});

		buttonStopL = (Button)findViewById(R.id.btnStopL);
		buttonStopL.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				serialSendL("s");
				serialReceivedTextL.setText("");
				Toast.makeText(MainActivity.this, "L stop", Toast.LENGTH_SHORT).show();

			}
		});

		buttonStartR = (Button)findViewById(R.id.btnStartR);
		buttonStartR.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				serialSendR("a");
				Toast.makeText(MainActivity.this, "start R", Toast.LENGTH_SHORT).show();
			}
		});

		buttonStopR = (Button)findViewById(R.id.btnStopR);
		buttonStopR.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				serialSendR("s");
				serialReceivedTextL.setText("");
				serialReceivedTextR.setText("");
				Toast.makeText(MainActivity.this, "R stop", Toast.LENGTH_SHORT).show();

			}
		});

		buttonStart = (Button)findViewById(R.id.btnStart);
		buttonStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				serialSendL("1");
				serialSendR("1");
				serialSendV("1");
				startTimerDelayL();
				startTimerDelayR();
				startTimerDelayV();
//				syncSaveFile(LL,LR, dataTimeDelay);
				Toast.makeText(MainActivity.this, "Start", Toast.LENGTH_SHORT).show();

			}
		});

		buttonStop = (Button)findViewById(R.id.btnStop);
		buttonStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				serialSendL("0");
				serialSendR("0");
				serialSendV("0");
				stopTimerDelayL();
				stopTimerDelayR();
				stopTimerDelayV();

//				serialReceivedTextL.setText("");
//				serialReceivedTextR.setText("");
				timer.cancel();
				Toast.makeText(MainActivity.this, "Stop", Toast.LENGTH_SHORT).show();

			}
		});
	}

	protected void onResume(){
		super.onResume();
		System.out.println("BlUNOActivity onResume");
		onResumeProcess();														//onResume Process by BlunoLibrary
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		onActivityResultProcess(requestCode, resultCode, data);					//onActivityResult Process by BlunoLibrary
		super.onActivityResult(requestCode, resultCode, data);
	}
	
    @Override
    protected void onPause() {
        super.onPause();
        onPauseProcess();														//onPause Process by BlunoLibrary
    }
	
	protected void onStop() {
		super.onStop();
		onStopProcess();														//onStop Process by BlunoLibrary
	}
    
	@Override
    protected void onDestroy() {
        super.onDestroy();	
        onDestroyProcess();														//onDestroy Process by BlunoLibrary
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
	public String onSerialReceivedL(String theString) {							//Once connection data received, this function will be called
		// TODO Auto-generated method stub
//		Date date = new Date();

		if (theString.length() > 11 ){
//		if (theString.length() > 9 ){
			LL = theString;
//			Log.d("TAG", "onSerialReceivedR: " + LL +" "+ LL.length());
		}

//		int myListLs = 1000;
//		Log.d("TAG", "onSerialReceivedR: " + LL);

//		List<String> myListL = new ArrayList<String>(Arrays.asList(LL.split(" ")));
//		Log.d("TAG", "onSerialReceivedR: " + myListL.size());
//		sequenceCount = 0;
//		for (int i=0; i < myListL.size(); i++) {
//			sequenceCount++;
//			Log.d("TAG", "onSerialReceivedR: " + sequenceCount);
////			serialReceivedTextL.append("SequenceL : " + sequenceCount);	//append the LData into the EditText
//		}


//		if (!LL.equals("") && !checkR.equals("")){
//		}

		return theString;
	}

	@Override
	public String onSerialReceivedR(String theString) {							//Once connection data received, this function will be called
		// TODO Auto-generated method stub
		if (theString.length() > 11 ){
//		if (theString.length() > 9 ){
			LR = theString;
//			Log.d("TAG", "onSerialReceivedR: " + LR +" "+ LR.length());
		}

//		int myListRs = 1000;

//		List<String> myListR = new ArrayList<String>(Arrays.asList(LR.split(" ")));
//		Log.d("TAG", "onSerialReceivedR: " + LR.length());
////		sequenceCount = 0;
//		for (int i=0; i < myListR.size(); i++) {
//			sequenceCount++;
//			serialReceivedTextR.append("SequenceR : " + sequenceCount+ " | " + "DataR : " +myListR + "\n");	//append the LData into the EditText
//		}

		return theString;
	}

	@Override
	public String onSerialReceivedV(String theString) {							//Once connection data received, this function will be called
		// TODO Auto-generated method stub

		return theString;
	}

	public void startTimerDelayL(){
		synchronized (this) {
			final long DELAY = 100; // 100ms delay
			final AtomicInteger index = new AtomicInteger(0);
			final long startTimeL = System.currentTimeMillis();
			carousalTimerL = new Timer(); // At this line a new Thread will be created
			carousalTimerL.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							long timeL = startTimeL + (index.incrementAndGet() * DELAY);
							timeDL = String.valueOf(sdf3.format(timeL));
							String checkL = onSerialReceivedL(LL);
							if (!checkL.equals("")) {
								String LaserDataL = "TimeL : " + timeDL + " LL : " + checkL;
								LList.add(LaserDataL);

								serialReceivedTextL.append("SeqL : " + LList.size() + " | " + LaserDataL);    //append the LData into the EditText
								((ScrollView) serialReceivedTextL.getParent()).fullScroll(View.FOCUS_DOWN);

								indexL++;
								if (indexL>=10) {
									indexL=0;
								}
								syncSaveFileL(LaserDataL);
							}
						}
					});

				}
			}, 100, 100); // delay
		}
	}

	public void startTimerDelayR(){
		synchronized (this) {
			final long DELAY = 100; // 100ms delay
			final AtomicInteger index = new AtomicInteger(0);
			final long startTimeR = System.currentTimeMillis();
			carousalTimerR = new Timer(); // At this line a new Thread will be created
			carousalTimerR.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					//DO YOUR THINGS

					runOnUiThread(new Runnable() {

						@Override
						public void run() {

							//		Date date = new Date();
							long timeR = startTimeR + (index.incrementAndGet() * DELAY);
							timeDR = String.valueOf(sdf3.format(timeR));

							String checkR = onSerialReceivedR(LR);

							if (!checkR.equals("")) {

								String LaserDataR = "TimeR : " + timeDR + " LR : " + checkR;
								RList.add(LaserDataR);
								Log.d(TAG, "ancokR: " + "SeqR: " + RList.size() + LaserDataR);
								serialReceivedTextR.append("SeqR : " + RList.size() + " | " + LaserDataR);    //append the RData into the EditText
								((ScrollView) serialReceivedTextR.getParent()).fullScroll(View.FOCUS_DOWN);

								indexR++;
								if (indexR>=10) {
//									Log.d(TAG, "indexL: " + indexR);
//									carousalTimerR.cancel();
									indexR=0;
								}

								syncSaveFileR(LaserDataR);
							}

						}
					});

				}
			}, 100, 100); // delay

		}

	}

	public void startTimerDelayV(){
		synchronized (this) {
			final long DELAY = 100; // 100ms delay
			final AtomicInteger index = new AtomicInteger(0);
			final long startTimeV = System.currentTimeMillis();
			carousalTimerV = new Timer(); // At this line a new Thread will be created
			carousalTimerV.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					//DO YOUR THINGS

					runOnUiThread(new Runnable() {

						@Override
						public void run() {

							//		Date date = new Date();
//							long timeV = startTimeV + (index.incrementAndGet() * DELAY);
//							timeDV = String.valueOf(sdf3.format(timeV));
//
//							String checkR = onSerialReceivedR(LR);
//
//							if (!checkR.equals("")) {
//
//								String LaserDataR = "TimeR : " + timeDR + " LR : " + checkR;
//								RList.add(LaserDataR);
//								Log.d(TAG, "ancokR: " + "SeqR: " + RList.size() + LaserDataR);
//								serialReceivedTextR.append("SeqR : " + RList.size() + " | " + LaserDataR);    //append the RData into the EditText
//								((ScrollView) serialReceivedTextR.getParent()).fullScroll(View.FOCUS_DOWN);
//
//								indexR++;
//								if (indexR>=10) {
////									Log.d(TAG, "indexL: " + indexR);
////									carousalTimerR.cancel();
//									indexR=0;
//								}
//
//								syncSaveFileR(LaserDataR);
//							}

						}
					});

				}
			}, 100, 100); // delay

		}

	}


	public void startTimerDelay(){
		final long DELAY = 100; // 100ms delay
		final AtomicInteger index = new AtomicInteger(0);
		final long startTime = System.currentTimeMillis();
		carousalTimer = new Timer(); // At this line a new Thread will be created
		carousalTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				//DO YOUR THINGS

				runOnUiThread(new Runnable() {

					@Override
					public void run() {

						//		Date date = new Date();
						long time = startTime + (index.incrementAndGet() * DELAY);
						data = String.valueOf(sdf3.format(time));

						String checkL = onSerialReceivedL(LL);
						String checkR = onSerialReceivedR(LR);

						LList.add(checkL);
						RList.add(checkR);

//						getTimeDelay(data);
						syncSaveFile(checkL,checkR,data);

					}
				});

			}
		}, 100, 100); // delay
	}

	private void getTimeDelay(String data) {


//		syncSaveFile(checkL,checkR,data);
	}

	void stopTimerDelayL() {
		carousalTimerL.cancel();
	}

	void stopTimerDelayR() {
		carousalTimerR.cancel();
	}

	void stopTimerDelayV() {
		carousalTimerV.cancel();
	}

	void stopTimerDelay() {
		carousalTimer.cancel();
	}


	public void syncSaveFileL(String LaserDataL){

				Log.d("TAG", "ancok: " +  writeCount);
				File file = new File(MainActivity.this.getFilesDir(), "text");
				if (!file.exists()) {
					file.mkdir();
				}
				try {
					File gpxfile = new File(file, "LaserDataL.txt");
					FileWriter writer = new FileWriter(gpxfile, true);
//					writer.append(LaserDataL + System.lineSeparator());
					writer.append(LaserDataL);
					writer.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}

	}

	public void syncSaveFileR(String LaserDataR){

				Log.d("TAG", "ancok: " +  writeCount);
				File file = new File(MainActivity.this.getFilesDir(), "text");
				if (!file.exists()) {
					file.mkdir();
				}
				try {
					File gpxfile = new File(file, "LaserDataR.txt");
					FileWriter writer = new FileWriter(gpxfile, true);
//					writer.append(LaserDataR + System.lineSeparator());
					writer.append(LaserDataR);
					writer.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}

	}

	public void syncSaveFile(final String LL, final String LR , final String dataTimeDelay){

		if (!LL.equals("") && !LR.equals("") && !dataTimeDelay.equals("")){

			synchronized (this) {

//				StringBuilder strAppendL = new StringBuilder();
//				strAppendL.append("\n");
////
//				final String newLaserDataL = LL.replace(";", strAppendL);
////				serialReceivedTextL.append(newLaserDataL);
////				((ScrollView)serialReceivedTextL.getParent()).fullScroll(View.FOCUS_DOWN);
//
//				StringBuilder strAppendR = new StringBuilder();
//				strAppendR.append("\n");
////
//				final String newLaserDataR = LR.replace(";", strAppendR);
//				serialReceivedTextR.append(newLaserDataR);
//				((ScrollView)serialReceivedTextR.getParent()).fullScroll(View.FOCUS_DOWN);


//				List<String> myListL = new ArrayList<String>(Arrays.asList(LL.split(" ")));
//				List<String> myListR = new ArrayList<String>(Arrays.asList(LR.split(" ")));
//				Log.d("TAG", "onSerialReceivedR: " + myListR.size());

				String LaserData = "Time : " + dataTimeDelay +  " LL : " + LL + " LR : " + LR;

				StringBuilder strAppend = new StringBuilder();
				strAppend.append(" ");

//                                    }
				final String newLaserData = LaserData.replace("\n", strAppend);

//				Log.d(TAG, "onCreateL: " + LList.size());
//				Log.d(TAG, "onCreateR: " + LList.size());

//				serialReceivedTextL.append("DataL : " +LL + "\n");	//append the LData into the EditText
//				serialReceivedTextR.append("DataR : " +LR + "\n");	//append the RData into the EditText
				serialReceivedTextL.append("SeqL : " +LList.size()+" "+dataTimeDelay+ " | " + "DataL : " +LL + "\n");	//append the LData into the EditText
				serialReceivedTextR.append("SeqR : " +RList.size()+" "+dataTimeDelay+ " | " + "DataR : " +LR + "\n");	//append the RData into the EditText
//				//The Serial data from the BLUNO may be sub-packaged, so using a buffer to hold the String is a good choice.
				((ScrollView)serialReceivedTextL.getParent()).fullScroll(View.FOCUS_DOWN);
				((ScrollView)serialReceivedTextR.getParent()).fullScroll(View.FOCUS_DOWN);

//				Log.d("TAG", "cihuy: " +newLaserData.length());

//				writeCount++;
				Log.d("TAG", "ancok: " +  writeCount);
				File file = new File(MainActivity.this.getFilesDir(), "text");
				if (!file.exists()) {
					file.mkdir();
				}
				try {
					File gpxfile = new File(file, "LaserData.txt");
					FileWriter writer = new FileWriter(gpxfile, true);
					writer.append(newLaserData + System.lineSeparator());
					writer.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
	}


}
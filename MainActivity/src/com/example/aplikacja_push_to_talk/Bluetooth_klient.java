package com.example.aplikacja_push_to_talk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

public class Bluetooth_klient extends Thread {

	private BluetoothAdapter mmBluetoothAdapter;
	private BluetoothSocket mSocket;
	private final static String TAG = "Bluetooth_klient";
	private InputStream inputStream;
	private OutputStream outputStream;
	private Handler mmHandler;
	private boolean polaczone;

	public Bluetooth_klient(BluetoothDevice device, UUID myUUID,
			BluetoothAdapter mBluetoothAdapter, Handler mHandler) {
		// TODO Auto-generated constructor stub

		BluetoothSocket tmp = null;
		mmHandler = mHandler;
		polaczone = false;

		this.mmBluetoothAdapter = mBluetoothAdapter;

		try {
			tmp = device.createInsecureRfcommSocketToServiceRecord(myUUID);
		} catch (Exception e) {
			Log.d(TAG, "blad podczas proby polaczenia " + e);
			e.printStackTrace();
		}

		mSocket = tmp;

		try {
			inputStream = mSocket.getInputStream();
			outputStream = mSocket.getOutputStream();
		} catch (Exception e) {
			Log.d(TAG, "string wejsciowy blad " + e);
		}
		mBluetoothAdapter.cancelDiscovery();
		try {
			mSocket.connect();

		} catch (IOException ex) {
			try {
				Log.d(TAG, " blad " + ex);
				mSocket.close();
			} catch (IOException e) {
				Log.d(TAG, "podczas zamykania socketa");
				e.printStackTrace();
			}
		}
	}

	public void run() {
		Log.d(TAG, "run");
		try
		{
			
		inputStream = mSocket.getInputStream();
		outputStream = mSocket.getOutputStream();
		polaczone = true;
		byte[] buffer = new byte[100000];
		//byte[] buffer = new byte[1500];
		int bytes;

		mmHandler.obtainMessage(MainBluetoothActivity.MESSAGE_WRITE, -1, -1,
				buffer).sendToTarget();
		while (true) {
			try {
				bytes = inputStream.read(buffer);
				mmHandler.obtainMessage(MainBluetoothActivity.MESSAGE_READ,
						bytes, -1, buffer).sendToTarget();

			} catch (IOException ex) {
				Log.d(TAG, " problem z wczytaniem bufora wejsciowego" + ex);

				break;
			}
		}
		Log.d(TAG, "polaczone");
		mmHandler.obtainMessage(MainBluetoothActivity.CONNECTION_PROBLEM, -1,
				-1, buffer).sendToTarget();
		try {
			mSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, "zamkniecie " + e);
			e.printStackTrace();
		}
	}
		catch (Exception e)
		{
			Log.d(TAG, "problem z run klient " + e);
		}
		
	}

	public void write(byte[] buffer) {
		//Log.d(TAG, "wpisywanie");
		Log.d(TAG, "zapisywanie wiadomosci z MainBluetoothActivity " + buffer.length);
		try {
			outputStream.write(buffer);

		} catch (IOException e) {
			Log.d(TAG, "problem z wpisywaniem" + e);
		}
	}
	


	public void cancel() {
		try {
			mSocket.close();
		} catch (IOException e) {
			Log.d(TAG, "blad zamykania" + e);
			e.printStackTrace();
		}
	}
}

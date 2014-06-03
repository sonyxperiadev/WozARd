/*
* Copyright (c) 2012 Mikael Möller, Lund Institute of Technology
* Copyright (c) 2012 Per Sörbris, Lund Institute of Technology
* Copyright (c) 2012-2014 Sony Mobile Communications AB.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*/
package com.sonymobile.wozard.puppet;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.sonymobile.wozard.puppet.messages.Protocol.Base;
import com.sonymobile.wozard.puppet.messages.Protocol.Location;
import com.sonymobile.wozard.puppet.util.Util;
/**
 * The service that handles most of the data sent over the network
 */
public class NetworkService extends Service {
	private static final int EVT_INCOMING_MESSAGE = 20;
	private static final int EVT_DISCONNECTED = 30;
	private static final int EVT_CONNECTED = 10;
	private static final int EVT_PING = 60;
	private static final int EVT_PONG = 70;

	private boolean mConnected = false;
	private long mPingReceived = System.currentTimeMillis();
	private LocalBinder mBinder = new LocalBinder();
	private Socket mSocket;
	private NetworkCallback mNetworkCallback;
	private OutputStream mOstream;
	private Handler mHandler;
	private boolean run = true;

	private static final String TAG = "NetworkService";

	public class LocalBinder extends Binder {
		public NetworkService getService() {
			return NetworkService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private synchronized void setConnected(boolean connected) {
		mConnected = connected;
	}

	public synchronized boolean isConnected() {
		return mConnected;
	}

	private synchronized void setPong(long ping) {
		mPingReceived = ping;
	}

	private synchronized boolean isPong() {
		boolean ispong = (System.currentTimeMillis() - mPingReceived) < D.SERVICE_TCP_PING_TIMEOUT_MS;
		Log.d(TAG, "isPong " + ispong);
		return ispong;
	}
	/**
	 * Handles the tcp connection and parses the messages
	 */
	private class ConnectionThread implements Runnable {
		public void run() {
			mHandler.postDelayed(msgTimer, D.SERVICE_TCP_PING_TIMEOUT_MS);
			while (keepRunning()) {
				try {
					String ip = getIp();
					InetAddress serverAddr = InetAddress.getByName(ip);
					mSocket = new Socket(serverAddr, D.SERVICE_TCP_SOCKET);
					Log.d(TAG, "start Socket " + serverAddr);
					mOstream = mSocket.getOutputStream();
					mHandler.obtainMessage(EVT_PING).sendToTarget();
					do {
						Base msg = null;
						msg = Base.parseDelimitedFrom(mSocket.getInputStream());
						if (msg != null) {
							Log.d(TAG, "msg received " + msg);
							if (!isConnected()) {
								mHandler.obtainMessage(EVT_CONNECTED).sendToTarget();
							}
							if (msg.hasPing()) {
								mHandler.obtainMessage(EVT_PONG).sendToTarget();
								mHandler.obtainMessage(EVT_PING).sendToTarget();
							} else {
								mHandler.obtainMessage(EVT_INCOMING_MESSAGE, msg)
										.sendToTarget();
							}
						}
					} while (isConnected() && keepRunning());
					mSocket.close();
					mSocket = null;
				} catch (IOException e) {
					Log.d(TAG, e.toString());
					if (isConnected()) {
						Log.e(TAG, e.toString());
						mHandler.obtainMessage(EVT_DISCONNECTED).sendToTarget();
					}
				}
			}
		}
		/**
		 * used to check if a timeout has occurred
		 */
		private Runnable msgTimer = new Runnable() {
			@Override
			public void run() {
				if (!isPong()) {
					if (isConnected()) {
						Log.e(TAG, "Disconnect timer expired");
						setConnected(false);
						mHandler.obtainMessage(EVT_DISCONNECTED).sendToTarget();
					}
				}
				mHandler.postDelayed(msgTimer, D.SERVICE_TCP_PING_TIMEOUT_MS/3);
			}
		};
	}

	public static synchronized String getIp() {
		return D.SERVER_IP;
	}

	public static synchronized void setIp(String ip) {
		Util.log("changed the ip to " + ip, null);
		D.SERVER_IP = ip;
	}

	private synchronized boolean keepRunning() {
		return run;
	}

	/**
	 * A function that tells the thread handling the socket to quit.
	 */
	public synchronized void stopThread() {
		run = false;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		HandlerThread handlerThread = new HandlerThread("NetworkMessageThread");
		handlerThread.start();
		mHandler = new Handler(handlerThread.getLooper(),
				new ServerMessageCallback());

		new Thread(new ConnectionThread()).start();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mHandler.removeMessages(EVT_CONNECTED);
		mHandler.removeMessages(EVT_DISCONNECTED);
		mHandler.removeMessages(EVT_INCOMING_MESSAGE);
		mHandler.removeMessages(EVT_PING);
		mHandler.removeMessages(EVT_PONG);
		mHandler.getLooper().quit();
		setConnected(false);
		stopThread();
		try {
			if (mSocket != null)
				mSocket.close();
		} catch (Exception e) {

		}
	}

	/**
	 * handles callbacks
	 */
	private class ServerMessageCallback implements Handler.Callback {

		@Override
		public boolean handleMessage(Message msg) {
			if (mNetworkCallback != null)
				switch (msg.what) {
				case EVT_DISCONNECTED:
					setConnected(false);
					Log.d(TAG, "EVT_DISCONNECTED");
					mNetworkCallback.serverDisconnected();
					Util.log("Disconnected from the wizard device", null);
					break;
				case EVT_CONNECTED:
					Log.d(TAG, "EVT_CONNECTED");
					Util.log("Connected to the wizard device", null);
					setConnected(true);
					mNetworkCallback.serverConnected();
					break;
				case EVT_INCOMING_MESSAGE:
					Log.d(TAG, "EVT_INCOMING_MESSAGE");
					Base payload = (Base) msg.obj;
					if (payload == null)
						break;
					if (payload.hasData()){
						mNetworkCallback.dataReceived(payload.getData()
								.getName(), payload.getData().getData()
								.toByteArray());
						Util.log("Data received", null);
					}
					if (payload.hasCmd()){
						mNetworkCallback.commandReceived(payload.getCmd());
						Util.log("Command received: " + payload.getCmd(), null);
					}
					if (payload.hasScreen()){
						mNetworkCallback.screenReceived(payload.getScreen()
								.toByteArray());
						Util.log("Screenshot received", null);
					}
					if (payload.hasSpeech()){
						mNetworkCallback.speechReceived(payload.getSpeech());
						Util.log("Speech received: " + payload.getSpeech(), null);
					}
					break;
				case EVT_PING:
					Log.d(TAG, "EVT_PING");
					sendPing();
					break;
				case EVT_PONG:
					Log.d(TAG, "EVT_PONG");
					setPong(System.currentTimeMillis());
					break;
				}
			return false;
		}
	}

	/**
	 * interface for communication with the activity bound to the service
	 */
	public interface NetworkCallback {
		/**
		 * Called when a command has been received by the {@link NetworkService}
		 * @param cmd The command received
		 */
		public void commandReceived(String cmd);

		/**
		 * Called when a file has been received. This generally follows a request
		 * @param fileName The name of the file
		 * @param bytes	The data contained in the file
		 */
		public void dataReceived(String fileName, byte[] bytes);
		/**
		 * called when a screenshot has been received
		 * @param bytes The image data
		 */
		public void screenReceived(byte[] bytes);
		/**
		 * called when a string has been received that is meant to be read by the tts engine
		 * @param speech The string to read
		 */
		public void speechReceived(String speech);
		/**
		 * called when a connection has been established
		 */
		public void serverConnected();
		/**
		 * called when the connection has been severed
		 */
		public void serverDisconnected();
	}
	/**
	 * Registers a {@link NetworkCallback} with the service
	 * @param callback The {@link NetworkCallback} that will receive calls from the service
	 */
	public void setCallback(NetworkCallback callback) {
		mNetworkCallback = callback;
	}

	/**
	 * called when the puppet device is lacking a file
	 * @param fileName the path to the file missing
	 */
	public synchronized void sendRequest(String fileName) {
		Base.Builder builder = Base.newBuilder();
		builder.setReq(fileName);
		try {
			builder.build().writeDelimitedTo(mOstream);
			mOstream.flush();
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	/**
	 * used to send feedback to the wizard
	 * @param info the information to show the wizard
	 */
	public synchronized void sendInfo(String info){
		Base.Builder builder = Base.newBuilder();
		builder.setInfo(info);
		try{
			builder.build().writeDelimitedTo(mOstream);
			mOstream.flush();
		} catch (Exception e){
			Log.e(TAG, e.toString());
		}
		Util.log("Sending feedback to the wizard device: " + info, null);
	}

	/**
	 * used to send commandId to the wizard
	 * @param commandId the commandId
	 */
	public synchronized void sendCommandId(String commandId){
		Base.Builder builder = Base.newBuilder();
		builder.setInfo(commandId);
		try{
			builder.build().writeDelimitedTo(mOstream);
			mOstream.flush();
		} catch (Exception e){
			Log.e(TAG, e.toString());
		}
		Util.log("Sending commandId to the wizard device: " + commandId, null);
	}

	/**
	 * used to send the location of the puppet to the wizard
	 * @param longitude
	 * @param latitude
	 */
	public synchronized void sendLocation(double longitude, double latitude) {
		Location.Builder builder = Location.newBuilder();
		builder.setLongitude(longitude);
		builder.setLatitude(latitude);
		Base.Builder baseBuilder = Base.newBuilder();
		baseBuilder.setLoc(builder.build());
		try {
			baseBuilder.build().writeDelimitedTo(mOstream);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	/**
	 * used to send a screenshot to the wizard
	 * @param bytes the image as a byte array
	 */
	public synchronized void sendScreen(byte[] bytes) {
		Base.Builder builder = Base.newBuilder();
		builder.setScreen(ByteString.copyFrom(bytes));
		try {
			builder.build().writeDelimitedTo(mOstream);
			Util.log("Sending a screenshot to the wizard device", null);
			mOstream.flush();
		} catch (Exception e) {
			Log.d(TAG, "Failed to write to ostream", e);
		}
	}

	/**
	 * used to determine if the connection is still valid
	 */
	private synchronized void sendPing() {
		Log.d(TAG, "sendPing");
		Base.Builder build = Base.newBuilder();
		build.setPing(1);
		try {
			build.build().writeDelimitedTo(mOstream);
			mOstream.flush();
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	/**
	 * used to send a voice command
	 * @param cmd indicates that it is a voice command
	 * @param results a string containing the alternatives provided by the api
	 */
	public synchronized void sendVoiceCommand(String cmd, String results) {
		Base.Builder builder = Base.newBuilder();
		builder.setCmd(cmd);
		builder.setVoicecommand(results);
		Util.log("Sending voice command to puppet", null);
		try {
			builder.build().writeDelimitedTo(mOstream);
			mOstream.flush();
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}
}

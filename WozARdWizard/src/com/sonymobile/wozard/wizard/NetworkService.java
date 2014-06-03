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
package com.sonymobile.wozard.wizard;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.sonymobile.wozard.wizard.interfaces.PositionCallback;
import com.sonymobile.wozard.wizard.messages.Protocol.Base;
import com.sonymobile.wozard.wizard.messages.Protocol.Data;
import com.sonymobile.wozard.wizard.messages.Protocol.Location;
import com.sonymobile.wozard.wizard.utils.Util;

/**
 * A service to handle commands sent over the network connection between the
 * WoZWizard application and WoZPuppet application.
 */

public class NetworkService extends Service {

	public static final int EVT_CONNECTED = 10;
	public static final int EVT_DISCONNECTED = 30;
	public static final int EVT_INCOMING_REQUEST = 40;
	public static final int EVT_INCOMING_SCREEN = 45;
	public static final int EVT_INCOMING_COORDINATES = 60;
	public static final int EVT_INCOMING_CAM = 65;
	public static final int EVT_INCOMING_VOICECOMMAND = 70;
	public static final int EVT_INCOMING_INFO = 80;
	public static final int EVT_PING = 90;
	public static final int EVT_PONG = 100;

    public static int commandId = 0;

	private static final String TAG = "NetworkService";

	private ServerCallback mServerCallback;
	private LocalBinder mLocalBinder = new LocalBinder();
	private ServerSocket mServerSocket;
	private boolean mConnected = false;
	private long mPongReceived = System.currentTimeMillis();

	private Handler mHandler;
	private Socket mSocket;
	private boolean run = true;
	private PositionCallback mPositionCallback;
	private ServerThread mServerThread;

	public void setServerCallback(ServerCallback serverCallback) {
		mServerCallback = serverCallback;
	}

	public void setPositionCallback(PositionCallback positionCallback){
		mPositionCallback = positionCallback;
	}

	@Override
	public void onCreate(){
		super.onCreate();
		HandlerThread handlerThread = new HandlerThread("ServerHandler");
		handlerThread.start();
		Log.d(TAG, "onCreate ");
		mHandler = new Handler(handlerThread.getLooper(), new ServerMessageCallback());
		mServerThread = new ServerThread();
		run = true;
		mServerThread.start();
	}

	@Override
	public void onDestroy(){
		mHandler.removeMessages(EVT_CONNECTED);
		mHandler.removeMessages(EVT_DISCONNECTED);
		mHandler.removeMessages(EVT_INCOMING_REQUEST);
		mHandler.removeMessages(EVT_INCOMING_COORDINATES);
		mHandler.removeMessages(EVT_INCOMING_SCREEN);
		mHandler.removeMessages(EVT_INCOMING_CAM);
		mHandler.removeMessages(EVT_INCOMING_VOICECOMMAND);
		mHandler.removeMessages(EVT_INCOMING_INFO);
		mHandler.removeMessages(EVT_PING);
		mHandler.removeMessages(EVT_PONG);
		mHandler.getLooper().quit();
		setConnected(false);
		stopThread();
		try {
			if (mSocket != null) mSocket.close();
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
		try {
			if (mServerSocket != null) mServerSocket.close();
		} catch (IOException e) {
		}
	}

	public class LocalBinder extends Binder {
		public NetworkService getService() {
			return NetworkService.this;
		}
	}
	@Override
	public IBinder onBind(Intent intent) {
		return mLocalBinder;
	}

	/**
	 * Used internally for callbacks from the network thread.
	 */
	private class ServerMessageCallback implements Handler.Callback {

		@Override
		public boolean handleMessage(Message msg) {
			Log.d(TAG, "handleMessage " +msg);
			if(mServerCallback != null){
				ByteString tmp;
				switch(msg.what){
				case EVT_CONNECTED:
					Log.d(TAG, "handleMessage EVT_CONNECTED");
					setConnected(true);
					mServerCallback.serverConnected();
					Util.log("Connection established to the puppet device", null);
					break;
				case EVT_DISCONNECTED:
					setConnected(false);
					Log.d(TAG, "handleMessage EVT_DISCONNECTED");
					mServerCallback.serverDisconnected();
					Util.log("disconnected from puppet device", null);
					break;
				case EVT_INCOMING_SCREEN:
					Log.d(TAG, "handleMessage EVT_INCOMING_SCREEN");
					tmp = (ByteString)msg.obj;
					mServerCallback.screenReceived(tmp.toByteArray());
					break;
				case EVT_INCOMING_COORDINATES:
					Log.d(TAG, "handleMessage EVT_INCOMING_COORDINATES");
					Location loc = (Location)msg.obj;
					if(mPositionCallback != null)
						mPositionCallback.coordinatesRecieved(loc.getLongitude(), loc.getLatitude());
					break;
				case EVT_INCOMING_VOICECOMMAND:
					Log.d(TAG, "handleMessage EVT_INCOMING_VOICECOMMAND");
					mServerCallback.voiceCommandRecieved((String)msg.obj);
					break;
				case EVT_INCOMING_INFO:
					Log.d(TAG, "handleMessage EVT_INCOMING_INFO");
					mServerCallback.commandIdRecieved((String)msg.obj);
					break;
				case EVT_PING:
					Log.d(TAG, "handleMessage EVT_PING");
					sendPing();
					break;
				case EVT_PONG:
					Log.d(TAG, "handleMessage EVT_PONG");
					setPong(System.currentTimeMillis());
					break;
				}
			}
			return false;
		}
	}

	/**
	 * A function to handle requests from the puppet device.
	 * @param request
	 */
	private void handleRequest(String request) {
		if (!request.contains(D.PUPPET_PATH)) return;
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + D.WIZARD_PATH;
		path = path + request.substring(request.indexOf(D.PUPPET_PATH) + D.PUPPET_PATH.length());
		File req = new File(path);
		if (req.exists() && req.isFile()) {
			long len = req.length();
			if (len > Integer.MAX_VALUE) return;
			try {
				byte[] buf = new byte[(int)len];
				FileInputStream fstream = new FileInputStream(req);
				fstream.read(buf);
				sendImage(request, buf);
				fstream.close();
			} catch (IOException e) {
				Log.e(TAG, e.toString());
			}
		} else {
			Log.e(TAG, "file not found '" + path + "'");
		}
	}

	public synchronized void setConnected(boolean connected) {
		Log.d(TAG, "setConnected " + connected);
		mConnected = connected;
	}

	public synchronized void setPong(long received) {
		Log.d(TAG, "setPong in (" + (received - mPongReceived) + ") ms");
		mPongReceived = received;
	}

	public synchronized boolean isPong () {
		boolean ispong = (System.currentTimeMillis() - mPongReceived) < D.SERVICE_TCP_PING_TIMEOUT_MS;
		Log.d(TAG, "isPong " + ispong);
		return ispong;
	}
	/**
	 * Asks whether or not a connection has been established
	 * @return true if connected otherwise false
	 */
	public synchronized boolean isConnected() {
		return mConnected;
	}

	/**
	 * Handles the socket.
	 */
	private class ServerThread extends Thread {
		public void run() {
			mHandler.postDelayed(mPingTimerTask, D.SERVICE_TCP_PING_TIMEOUT_MS);
			while(keepRunning()){
				try{
					if (mServerSocket == null) {
						try {
							mServerSocket = new ServerSocket(D.SERVICE_TCP_SOCKET);
						} catch (IOException e) {
							return;
						}
					}
					Log.d(TAG, "accept IN");
					mSocket = mServerSocket.accept();
					// mSocket.setKeepAlive(true);
					mSocket.setReceiveBufferSize(10000);
					Log.d(TAG, "accept OUT");
					mHandler.obtainMessage(EVT_PING).sendToTarget();
					do {
						Base msg = null;
						msg = Base.parseDelimitedFrom(mSocket.getInputStream());
						if (msg != null){
							Log.d(TAG, "msg received " + msg);
							if (!isConnected()) {
								mHandler.obtainMessage(EVT_CONNECTED).sendToTarget();
							}
							if(msg.hasInfo())
								mHandler.obtainMessage(EVT_INCOMING_INFO, msg.getInfo()).sendToTarget();
							if(msg.hasReq())
								handleRequest(msg.getReq());
							if (msg.hasPing()) {
								mHandler.obtainMessage(EVT_PONG).sendToTarget();
							}
							if(msg.hasScreen()){
								mHandler.obtainMessage(EVT_INCOMING_SCREEN, msg.getScreen()).sendToTarget();
							}
							if(msg.hasCam()){
								mHandler.obtainMessage(EVT_INCOMING_CAM, msg.getCam()).sendToTarget();
							}
							if(msg.hasLoc()){
								mHandler.obtainMessage(EVT_INCOMING_COORDINATES, msg.getLoc()).sendToTarget();
							}
							if(msg.hasVoicecommand()){
								mHandler.obtainMessage(EVT_INCOMING_VOICECOMMAND, msg.getVoicecommand()).sendToTarget();
							}
						}
					} while (isConnected() && keepRunning());
					if (mSocket != null) try {
						mSocket.close();
					} catch (IOException e) {
					}
					mSocket = null;
					if (mServerSocket!=null) {
						try {
							mServerSocket.close();
						} catch (IOException e) {
						}
						mServerSocket = null;
					}
				} catch (IOException e) {
					Log.e(TAG, e.toString());
				} finally {
				}
			}
		}

		/**
		 * A timer to check the connection.
		 */
		private Runnable mPingTimerTask = new Runnable() {
			public void run() {
				Log.d(TAG, "Timer connected=" + isConnected() + " pong=" + isPong());
				if (!isPong()) {
					Log.d(TAG, "Disconnect timer expired");
					if (isConnected()) {
						setConnected(false);
						mHandler.obtainMessage(EVT_DISCONNECTED).sendToTarget();
					}
				}
				Log.d(TAG, "Ping timer expired");
				mHandler.obtainMessage(EVT_PING).sendToTarget();
				mHandler.postDelayed(mPingTimerTask, D.SERVICE_TCP_PING_TIMEOUT_MS/3);
			}
		};
	}

	/**
	 * Sends a ping to the puppet device.
	 */
	private synchronized void sendPing() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Base.Builder build = Base.newBuilder();
				Log.d(TAG, "sendPing");
				build.setPing(1);
				try {
					build.build().writeDelimitedTo(mSocket.getOutputStream());
					mSocket.getOutputStream().flush();
				} catch (Exception e) {
					Log.e(TAG, e.toString());
				}
			}
		});
	}

	private synchronized boolean keepRunning(){
		return run;
	}

	/**
	 * Kills the network thread.
	 */
	public synchronized void stopThread(){
		run = false;
	}

	/**
	 * sendImage is used to send an image after the controller has received a request for an image
	 * @param name filename of the image being sent
	 * @param img byte[] containing the image
	 */
	public synchronized void sendImage(final String name,final byte[] img){
		mHandler.post(new Runnable(){
			@Override
			public void run(){
				Data.Builder ib = Data.newBuilder();
				ib.setData(ByteString.copyFrom(img));
				ib.setName(name);
				Base.Builder build = Base.newBuilder();
				build.setData(ib.build());
				try{
					build.build().writeDelimitedTo(mSocket.getOutputStream());
					Util.log("Transfering image " + name + " to puppet device", null);
					mSocket.getOutputStream().flush();
				} catch (Exception e){
					Log.e(TAG, "Failed to write to ostream in sendImage",e);
				}
			}
		});
	}

	/**
	 * Sends a screenshot in a byte array.
	 * @param bytes a byte array containing the screenshot
	 */
	public void sendScreen(final byte[] bytes) {
		Log.d(TAG, "sendScreen " + bytes.length);

		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Base.Builder builder = Base.newBuilder();
				builder.setScreen(ByteString.copyFrom(bytes));
				try {
					if (!mSocket.isClosed()) {
						builder.build().writeDelimitedTo(mSocket.getOutputStream());
						mSocket.getOutputStream().flush();
					}
					Util.log("sending a screenshot to the puppet device", null);
				} catch (Exception e) {
					Log.e(TAG, "Failed to write to ostream in sendScreen");
					Util.log("encountered an error while sending a screenshot", null);
				}
			}
		});


	}

	/**
	 * Sends a command and a string to be read up by the Text-to-Speech engine.
	 * @param cmd the command to be sent
	 * @param sound the string to be read out
	 */
	public synchronized void sendSoundCommand(final String cmd, final String sound) {
		Log.d(TAG, "sendSoundCommand " + sound);
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Base.Builder build = Base.newBuilder();
				build.setCmd(cmd);
				if (sound != null) build.setSpeech(sound);
				try {
					if (!mSocket.isClosed()) {
						build.build().writeDelimitedTo(mSocket.getOutputStream());
						Util.log("sending command with speech, command: " + cmd + " sound: "
								+ sound, null);
						mSocket.getOutputStream().flush();
					}
				} catch (Exception e) {
					Log.e(TAG, "Failed to write to ostream in sendCommand", e);
					Util.log("exception caugt when trying to send a command with speech", null);
				}
			}
		});
	}

	/**
	 * Sends a command
	 * @param cmd to be sent
	 */
	public synchronized void sendCommand(final String cmd) {
		Log.d(TAG, "sendCommand " + cmd);

		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Base.Builder build = Base.newBuilder();
				build.setCmd(cmd);
				try {
					if ((mSocket != null) && !mSocket.isClosed()) {
						build.build().writeDelimitedTo(mSocket.getOutputStream());
						Util.log("Sending command: " + cmd, null);
						mSocket.getOutputStream().flush();
					}
				} catch (Exception e) {
					Log.e(TAG, "Failed to write to ostream in sendCommand", e);
					Util.log("Failed to send command: " + cmd, null);
				}
			}
		});
	}

	/**
	 * An interface for the callback from the NetworkService
	 * @author 23060470
	 *
	 */
	public interface ServerCallback{
		/**
		 * A function that is called when the server has established
		 * a connection to a player
		 */
		public void serverConnected();

		/**
		 * If the NetworkService gets disconnected from the player
		 * this function is called
		 */
		public void serverDisconnected();
		/**
		 * This function is called if a screenshot has been received
		 * @param the image in a byte vector
		 */
		public void screenReceived(byte[] bytes);

		/**
		 * This function is called when a voice command is received
		 * @param command contains the Voice-to-Text command results
		 */
		public void voiceCommandRecieved(String command);

		/**
		 * This function is called when a commandId is received
		 * @param command contains the commandId
		 */
		public void commandIdRecieved(String command);
	}
}

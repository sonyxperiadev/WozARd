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

import java.util.concurrent.CopyOnWriteArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import com.sonymobile.wozard.wizard.interfaces.PositionCallback;
import com.sonymobile.wozard.wizard.utils.CommandOverlay;
import com.sonymobile.wozard.wizard.utils.Util;

/**
 * A service used to handle automated tours.
 */
public class TourService extends Service implements PositionCallback, ServiceConnection{
	private static final String[] providerStrings = {"Out of Service","Temporary unavailable", "Available"};
	private LocalBinder mBinder = new LocalBinder();
	private NetworkService networkService;
	private boolean usingOwnGPS = true;
	private CopyOnWriteArrayList<CommandOverlay> commands;
	private ServiceHandler mHandler;
	private boolean tourMode;
	private TourServiceCallback callback;
	private static final int EVT_POSITION = 10;
	private static final int EVT_DISCONNECTED = -5;
	private static final int EVT_CONNECTED = 5;

	private final class ServiceHandler extends Handler{
		public ServiceHandler(Looper looper){
			super(looper);
		}

		@Override
		public void handleMessage(Message msg){

			switch(msg.what){
			case EVT_POSITION:
				updatePosition((Location)msg.obj);
				break;
			case EVT_CONNECTED:
				break;
			case EVT_DISCONNECTED:
				break;
			default:
				break;
			}

		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	@Override
	public void onCreate(){
		super.onCreate();

		HandlerThread thread = new HandlerThread("TourThread", Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		mHandler = new ServiceHandler(thread.getLooper());
		Intent intent = new Intent();
		intent.setClass(this, NetworkService.class);
		bindService(intent, this,Context.BIND_AUTO_CREATE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		Log.d("Tourservice", "onDestroy");
		disableGPS();
		unbindService(this);
	}

	public class LocalBinder extends Binder{
		public TourService getService() {
			return TourService.this;
		}
	}

	/**
	 * Updates the position of the puppet.
	 * @param loc the new location
	 */
	public void updatePosition(Location loc){
		if(tourMode)
			new CheckForHitTask().execute(loc);
		if(callback != null)
			callback.updatePosition(loc);

	}

	@Override
	public void coordinatesRecieved(double longitude, double latitude) {
		if(usingOwnGPS)
			return;
		Location loc = new Location("none");
		loc.setLongitude(longitude);
		loc.setLatitude(latitude);
		mHandler.obtainMessage(EVT_POSITION,loc).sendToTarget();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		networkService = ((NetworkService.LocalBinder) service).getService();
		networkService.setPositionCallback(this);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		networkService = null;
	}

	/**
	 * Starts the tour mode.
	 */
	public void startTourMode(){
		NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.icon;
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon,"Tour mode is active", when);
		Context context = getApplicationContext();
		CharSequence contentTitle = "Tourmode is active";
		CharSequence contentText = "Click to disable";
		Intent notificationIntent = new Intent(this, TourService.class);
		notificationIntent.putExtra("stop_tour", true);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		notificationManager.notify(1, notification);
		tourMode = true;
		Util.log("TourMode started", null);
	}

	/**
	 * Stops the tour mode.
	 */
	public void stopTourMode(){
		tourMode = false;
		Util.log("TourMode ended", null);
	}

	/**
	 * A function to set the source of location updates.
	 * @param self true to use the Wizard device's GPS, false to use the Puppet device's GPS
	 */
	public void setSelfAsSource(boolean self){
		usingOwnGPS = self;
		if(self){
			initGPS();
			Util.log("Wizard device set as source of location", null);
		}else{
			disableGPS();
			Util.log("Puppet device set as source of location", null);
		}
	}

	/**
	 * Starts the GPS on the Wizard device
	 */
	public void initGPS(){
		usingOwnGPS = true;
		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 1, tourListener);
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, tourListener);
	}

	/**
	 * Turns the GPS on Wizard device off
	 */
	public void disableGPS(){
		if(tourListener == null)
			return;
		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		lm.removeUpdates(tourListener);
	}

	private LocationListener tourListener = new LocationListener(){
		@Override
		public void onLocationChanged(Location location) {
			mHandler.obtainMessage(EVT_POSITION, location).sendToTarget();
		}

		@Override
		public void onProviderDisabled(String provider) {
			Toast.makeText(TourService.this, provider + " disabled", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onProviderEnabled(String provider) {
			Toast.makeText(TourService.this, provider + " enabled", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Toast.makeText(TourService.this, provider + " status changed to " + providerStrings[status], Toast.LENGTH_SHORT).show();
		}

	};

	/**
	 * An interface used to communicate with the TourService
	 */
	public interface TourServiceCallback{
		/**
		 * Called when a new position is available
		 * @param loc the new position
		 */
		public void updatePosition(Location loc);
	}


	/**
	 * Used to see if there are any hits
	 */
	private class CheckForHitTask extends AsyncTask<Location, Void, CommandOverlay>{

		@Override
		protected CommandOverlay doInBackground(Location... locations) {
			if(commands == null)
				return null;
			for(CommandOverlay cmd : commands){
				for(Location loc : locations){
					if(cmd.hit(loc)){
						Util.log("Hit registered for command: " + cmd.getCommand(),loc);
						return cmd;

					}
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(CommandOverlay result){
			if(result != null && networkService != null){
				networkService.sendCommand(result.toString());
			}
		}
	}

	/**
	 * Register a callback
	 * @param cb the callback
	 */
	public void setCallback(TourServiceCallback cb){
		callback = cb;
	}

	/**
	 * Sets the CommandOverlays to check against when looking for hits
	 * @param cmds A CopyOnWriteArrayList containing all the commandoverlays
	 */
	public void setCommands(CopyOnWriteArrayList<CommandOverlay> cmds){
		commands = cmds;
	}

	/**
	 * Check if the tour mode is active
	 * @return true if it is active, false otherwise
	 */
	public boolean tourModeOn(){
		return tourMode;
	}

}

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

import java.util.ArrayList;

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.MapActivity;
import com.sonymobile.wozard.wizard.NetworkService.ServerCallback;
import com.sonymobile.wozard.wizard.fragments.MenuFragment;
import com.sonymobile.wozard.wizard.fragments.NavigationFragment.NavigationCallback;
import com.sonymobile.wozard.wizard.fragments.TourFragment;
import com.sonymobile.wozard.wizard.interfaces.NotificationCallback;
import com.sonymobile.wozard.wizard.interfaces.ScreenSender;
import com.sonymobile.wozard.wizard.utils.CommandList;
import com.sonymobile.wozard.wizard.utils.Monitor;
import com.sonymobile.wozard.wizard.utils.Settings;
import com.sonymobile.wozard.wizard.utils.Util;

public class ControllerActivity extends MapActivity implements	ServiceConnection, ServerCallback, NavigationCallback,
    NotificationCallback, ScreenSender, OnItemClickListener {

	/** Called when the activity is first created. */
	public static final String COMMAND_ID = "COMMAND_ID";
	public static final String SHOW_IMAGE_CMD = "SHOW_IMAGE";
	public static final String SHOW_TRANSPARENT_IMAGE_CMD = "SHOW_TRANSPARENT_IMAGE";
	public static final String SHOW_SOUND_IMAGE_CMD = "SHOW_SOUND_IMAGE";
	public static final String SHOW_VIDEO_CMD = "SHOW_VIDEO";
	public static final String SHOW_MAP_CMD = "SHOW_MAP";
	public static final String SHOW_SMALL_NOTIFICATION_CMD = "SHOW_SMALL_NOTIFICATION";
	public static final String SHOW_SMALL_INFO_NOTIFICATION_CMD = "SHOW_SMALL_INFO_NOTIFICATION";
	public static final String SHOW_INFO_NOTIFICATION_CMD = "SHOW_INFO_NOTIFICATION";
	public static final String SHOW_BIG_NOTIFICATION_CMD = "SHOW_BIG_NOTIFICATION";
	public static final String SHOW_NEXT_SMS_CMD = "SHOW_NEXT_SMS";
	public static final String SHOW_PREVIOUS_SMS_CMD = "SHOW_PREVIOUS_SMS";
	public static final String PLAY_SOUND_CMD = "PLAY_SOUND";
	public static final String PLAY_VIBRATION_CMD = "PLAY_VIBRATION";
	public static final String SET_SOUND_CMD = "SET_SOUND";
	public static final String REQ_COORDINATES_CMD = "REQ_COORDINATES";
	public static final String GET_SCREEN_CMD = "GET_SCREEN";
	public static final String CAMERA_CMD = "START_CAMERA";
	public static final String SEND_CAMERA_OFF_CMD = "STOP_SEND_CAMERA";
	public static final String SEND_CAMERA_CMD = "SEND_CAMERA";
	public static final String AUTO_PICS_ON = "AUTO_ON";
	public static final String AUTO_PICS_OFF = "AUTO_OFF";
	public static final String TAKE_PICTURE_CMD = "TAKE_PICTURE";
	public static final String PUPPET_VOICECOMMAND_CMD = "PUPPET_VOICECOMMAND";
	public static final String REQ_VOICECOMMAND_CMD = "REQ_VOICECOMMAND";
	public static final String SEND_SOUND_CMD = "SEND_SOUND";
	public static final String SEND_SOUND_OFF_CMD = "STOP_SEND_SOUND";
	public static final String PRESS_EVENT_CMD = "PRESS_EVENT";
	public static final String SHOW_XY_COORDINATES_CMD = "SHOW_XY_COORDINATES";
	public static final String LOAD_CMD = "LOAD";
	public static final String VIDEO_SEE_THROUGH_DEVICE = "VIDEO_SEE_THROUGH_DEVICE";
	public static final String CLEAR_INDICATOR_CMD = "CLEAR_INDICATOR";
	public static final String[] COMMANDS = {SHOW_IMAGE_CMD,
			SHOW_SOUND_IMAGE_CMD, SHOW_VIDEO_CMD, SHOW_MAP_CMD,
			SHOW_SMALL_NOTIFICATION_CMD, SHOW_BIG_NOTIFICATION_CMD, SHOW_INFO_NOTIFICATION_CMD,
			PLAY_SOUND_CMD, PLAY_VIBRATION_CMD, SEND_CAMERA_CMD, CAMERA_CMD, TAKE_PICTURE_CMD,
			REQ_VOICECOMMAND_CMD };

	public static final int SWIPE_UP_INTENT = 0;
	public static final int SWIPE_LEFT_INTENT = 1;
	public static final int SWIPE_RIGHT_INTENT = 2;
	public static final int SWIPE_DOWN_INTENT = 3;
	public static final int PRESS_INTENT = 4;
	public static final int LONG_PRESS_INTENT = 5;
	public static int mCommandId = 0;
	public static int mNowShowingCommandId = 0;
	private static final String TAG = "ControllerActivity";
    private static CommandList mCommandList = CommandList.getInstance();
	private TextView mConnectedStatusTextView;
	private static ArrayList<ControllerCallback> listofControllerCallbacks = new ArrayList<ControllerActivity.ControllerCallback>();
	private static NetworkService networkService;
	private Monitor monitor;
	private Dialog dialog;
	private Dialog fileDialog;
	private ArrayAdapter<String> adapter;
	private ListView listview;
	private TextView infoText;
	private Button currentLocButton;
	private Button takePhotoButton;
	private Button showSMSButton;
	private Button cancelButton;
	private Button startCameraButton;

	/**
	 * Interface used for the callback if the connection is lost
	 * and when a new image is shown on the preview
	 */
	public interface ControllerCallback{
		public void onConnected(boolean isConnected);
		public void onNewPreviewImage(Bitmap previewImage);
		public void puppetVideoSeeThroughDevice(boolean videoSeeThroughDevice);
	}

	public static void sendCommandToPuppet(String command, String filePath, String speech, Bitmap bitmap) {
		if (networkService != null) {
			ControllerActivity.mCommandId++;
			mCommandList.setCommand(ControllerActivity.mCommandId, command,
					filePath, speech, bitmap);
			command = command.concat("$" + ControllerActivity.mCommandId);

			if (speech == null) {
				networkService.sendCommand(command);
			} else {
				networkService.sendSoundCommand(command, speech);
			}
		} else {
			Log.d(TAG, "Tried to send a command but networkservice was null");
		}
	}

	public static void sendScreenToPuppet(byte[] bytes) {
        if (networkService != null) {
            networkService.sendScreen(bytes);
        } else {
            Log.d(TAG, "Tried to send the screen but the networkservice was null");
        }
	}

	public static void stopConnection() {
		if (networkService != null) {
			networkService.stopThread();
		}
	}

	public static Bitmap getPreviewImage() {
		return mCommandList.getImageToShow(mNowShowingCommandId);
	}

	/**
	 * Callback function
	 * @param cb
	 */
	public static void registerCallback(ControllerCallback cb){
		listofControllerCallbacks.add(cb);
	}

	/**
	 * Unregister callback function
	 * @param cb
	 */
	public static void unRegisterCallback(ControllerCallback cb){
        listofControllerCallbacks.remove(cb);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
				== Configuration.SCREENLAYOUT_SIZE_XLARGE) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		setContentView(R.layout.fragment_layout);
		mConnectedStatusTextView = (TextView) findViewById(R.id.connection_text);
		mConnectedStatusTextView.setBackgroundColor(Color.RED);
		mConnectedStatusTextView.setText("Not connected to any player");

		Intent bindIntent = new Intent(this, NetworkService.class);
		bindService(bindIntent, this, Context.BIND_AUTO_CREATE);

		Util.actionBarSetup(this, true, R.string.action_bar_default_title);

		monitor = Monitor.getInstance();
		Thread smartWatchThread = new Thread() {

			@Override
			public void run() {
				FragmentManager man;
				SmartWatchReceiver frag;
				while (true) {
					int interaction = monitor.getCommand();
					man = getFragmentManager();
					frag = (SmartWatchReceiver) man
							.findFragmentById(R.id.details);
					switch (interaction) {

					case SWIPE_UP_INTENT:
						// handled on puppet if sms is showing - show next
						if (networkService != null) {
							networkService.sendCommand(SHOW_NEXT_SMS_CMD);

						} else {
							Log.d(TAG,
									"Tried to send a command but networkservice was null");
						}
						if (frag != null)
							frag.onUpSwipe();
						break;
					case SWIPE_LEFT_INTENT:
						if (((Boolean) Settings.getInstance().getValue(
								Settings.SMARTWATCHONRIGHTHAND_KEY))) {
							String cmd = SHOW_IMAGE_CMD + " "
									+ (String) Settings.DEFAULT_ROOT_NAV
									+ "00 blank.png";
							if (networkService != null)
								networkService.sendCommand(cmd);
							else {
								Log.d(TAG,
										"Tried to send a command but networkservice was null");
							}
						}
						break;
					case SWIPE_RIGHT_INTENT:
						// Allways show blank on puppet
						if (!((Boolean) Settings.getInstance().getValue(
								Settings.SMARTWATCHONRIGHTHAND_KEY))) {
							networkService.sendCommand(CLEAR_INDICATOR_CMD);
						}
						break;
					case SWIPE_DOWN_INTENT:
						// handled on puppet if sms is showing - show previous
						networkService.sendCommand(SHOW_PREVIOUS_SMS_CMD);
						if (frag != null)
							frag.onDownSwipe();
						break;
					case PRESS_INTENT:
						networkService.sendCommand(CLEAR_INDICATOR_CMD);

						if (frag != null) {
							frag.onPress();
						} else {
							networkService.sendCommand(PRESS_EVENT_CMD);
						}

						break;
					case LONG_PRESS_INTENT:
						networkService.sendCommand(REQ_VOICECOMMAND_CMD);
						break;
					}
				}
			}
		};
		smartWatchThread.start();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (networkService != null) {
			setConnected(networkService.isConnected());
		} else {
			serverDisconnected();
		}
		Util.updatePreviewImage(this);
	}

	/**
	 * Helper function to show toasts.
	 *
	 * @param msg
	 *            the string message to be displayed
	 */
	public void showToast(String msg) {
		Context context = getApplicationContext();
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	private void setConnected(boolean connected) {
		if (connected) {
			serverConnected();
		} else {
			serverDisconnected();
		}
	}

	/**
	 * Helper function to extract the command from the filename.
	 *
	 * @param fileName
	 *            the name of the file
	 * @return a string with the extracted command
	 */

	public static String getCommandFromName(String fileName) {
		String filenameArray[] = fileName.split("\\.");
		String extension = filenameArray[filenameArray.length - 1];
		if (extension.equalsIgnoreCase("jpeg")) {
			return SHOW_IMAGE_CMD;
		} else if (extension.equalsIgnoreCase("jpg")) {
			return SHOW_IMAGE_CMD;
		} else if (extension.equalsIgnoreCase("gif")) {
			return SHOW_IMAGE_CMD;
		} else if (extension.equalsIgnoreCase("png")) {
			return SHOW_IMAGE_CMD;
		} else if (extension.equalsIgnoreCase("3gp")) {
			return SHOW_VIDEO_CMD;
		} else if (extension.equalsIgnoreCase("mp4")) {
			return SHOW_VIDEO_CMD;
		} else if (extension.equalsIgnoreCase("mp3")) {
			return PLAY_SOUND_CMD;
		} else if (extension.equalsIgnoreCase("wav")) {
			return PLAY_SOUND_CMD;
		} else if (extension.equalsIgnoreCase("ogg")) {
			return PLAY_SOUND_CMD;
		} else if (extension.equalsIgnoreCase("vbr")) {
			return PLAY_VIBRATION_CMD;
		} else {
			// do nothing, because not supported
			return null;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		showToast("onDestroy -> networkService = null");
		unbindService(this);
		networkService = null;
	}

	@Override
	public void serverConnected() {
        for (int i = 0; i < listofControllerCallbacks.size(); i++) {
            listofControllerCallbacks.get(i).onConnected(true);
		}
		runOnUiThread(new Runnable() {
			public void run() {
				if (mConnectedStatusTextView != null) {
				    mConnectedStatusTextView.setBackgroundColor(Color.GREEN);
				    mConnectedStatusTextView.setText(getResources().getString(
                            R.string.connected_text));
				}
			}
		});
	}

	@Override
	public void serverDisconnected() {
        for (int i = 0; i < listofControllerCallbacks.size(); i++) {
            listofControllerCallbacks.get(i).onConnected(false);
        }
	    runOnUiThread(new Runnable() {
			public void run() {
				if (mConnectedStatusTextView != null) {
				    mConnectedStatusTextView.setBackgroundColor(Color.RED);
				    mConnectedStatusTextView.setText(getResources().getString(
					        R.string.not_connected_text));
				}
			}
		});
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		networkService = ((NetworkService.LocalBinder) service).getService();
		if (networkService != null) {
		    networkService.setServerCallback(this);
		    setConnected(networkService.isConnected());
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		networkService = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_portrait, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		Log.d(TAG, "onOptionsItemSelected " + item.getItemId());
		switch (item.getItemId()) {
		case R.id.menu_quit:
			Util.handleQuit(this);
			System.exit(0);
			return true;
		case R.id.clearButton:
		    Util.handleClearButton();
			return true;
		case R.id.menu_userview:
			Intent intent = new Intent();
			intent.setClass(this, UserViewActivity.class);
			startActivity(intent);
			return true;
		}
		return false;
	}

	public void screenReceived(final byte[] bs) {
		Util.screenReceived(bs, this);
	}

	@Override
	public void sendNavCommand(String cmd, String sound) {
		if (networkService != null)
			networkService.sendSoundCommand(cmd, sound);
		else
			Log.d(TAG, "Tried to send a command but networkservice was null");
	}

	@Override
	public void sendScreen(byte[] screen) {
		if (networkService != null)
			networkService.sendScreen(screen);
	}

	@Override
	public void sendNotificationToPuppet(String cmd) {
		if (networkService != null) {
			Log.d("mynot", "sending notifcation to puppet");
			sendCommand(cmd);
		} else {
			Log.d(TAG, "Tried to send a command but networkservice was null");
		}
	}

	@Override
	public void sendNotificationToPuppet(String cmd, String speech) {
		if (networkService != null) {
			networkService.sendSoundCommand(cmd, speech);
		} else {
			Log.d(TAG, "Tried to send a command but networkservice was null");
		}
	}

	@Override
	public void voiceCommandRecieved(String command) {
		Log.d("smartwatch", "the command: " + command);
		if (dialog == null) {
			dialog = new Dialog(ControllerActivity.this);
			dialog.setContentView(R.layout.voice_dialog);
			dialog.setTitle("Incomming voice command from Puppet device");
			dialog.setCancelable(true);
			listview = (ListView) dialog.findViewById(R.id.listView);
			infoText = (TextView) dialog.findViewById(R.id.infoText);
			currentLocButton = (Button) dialog
					.findViewById(R.id.currentLocationButton);
			startCameraButton = (Button) dialog
					.findViewById(R.id.startCameraButton);
			takePhotoButton = (Button) dialog
					.findViewById(R.id.takePhotoButton);
			showSMSButton = (Button) dialog.findViewById(R.id.showSMSButton);
			cancelButton = (Button) dialog.findViewById(R.id.cancelButton);
			View.OnClickListener listener = new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					switch (v.getId()) {
					case R.id.currentLocationButton:
						dialog.dismiss();
						sendCommand(LOAD_CMD);
						MenuFragment frag = (MenuFragment) getFragmentManager()
								.findFragmentById(R.id.side_menu);
						frag.showTourView();

						break;
					case R.id.startCameraButton:
						sendCommand(SEND_CAMERA_CMD);
						break;
					case R.id.takePhotoButton:
						sendCommand(TAKE_PICTURE_CMD);
						break;
					case R.id.showSMSButton:
						sendNotificationToPuppet(ControllerActivity.SHOW_BIG_NOTIFICATION_CMD
								+ " SMS#");
						break;
					case R.id.cancelButton:
						dialog.dismiss();
						break;
					}
				}
			};
			currentLocButton.setOnClickListener(listener);
			startCameraButton.setOnClickListener(listener);
			takePhotoButton.setOnClickListener(listener);
			showSMSButton.setOnClickListener(listener);
			cancelButton.setOnClickListener(listener);
		}
		ArrayList<String> commands = new ArrayList<String>();
		while (command.length() > 0) {
			if (command.contains(";") && commands.size() < 4) {
				int index = command.indexOf(";");
				String current = command.substring(0, index);
				command = command.substring(index + 1, command.length());
				commands.add(current);
			} else {
				Log.d("player", "command: " + command);
				if (command.contains("#")) {
					int index = command.indexOf("#");
					command = command.substring(index + 1, command.length());
					if (command.contains("camera")) {
						infoText.setText("Got match for camera, so the camera was started on puppet device");
					} else if (command.contains("sms")) {
						infoText.setText("Got match for show message, so the newest sms is shown on puppet device");
					}
				}
				command = "";
			}
		}

		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, commands);
		listview.setAdapter(adapter);
		dialog.show();
	}

	@Override
	public void commandIdRecieved(String command) {
        int nbr = command.indexOf("$");
		if (command.startsWith(COMMAND_ID)) {
			if (nbr > 0 && nbr < command.length()) {
				int commandId = Integer.parseInt(command.substring(nbr + 1).trim());
				if (mCommandList.checkIfMediaFile(commandId) ||
						mCommandList.checkIfClearCommand(commandId)) {
					mNowShowingCommandId = commandId;
                    Util.previewImage(this, mCommandList.getImageToShow(commandId));
                    for (int i = 0; i < listofControllerCallbacks.size(); i++) {
                        listofControllerCallbacks.get(i).onNewPreviewImage(mCommandList.
                                getImageToShow(commandId));
					}
				}
			}
		} else if (command.startsWith(VIDEO_SEE_THROUGH_DEVICE)) {
			if (nbr > 0 && nbr < command.length()) {
				boolean videoSeeThroughDevice = Boolean.parseBoolean(command.substring(nbr + 1).trim());
				for (int i = 0; i < listofControllerCallbacks.size(); i++) {
                    listofControllerCallbacks.get(i).
                            puppetVideoSeeThroughDevice(videoSeeThroughDevice);
				}
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View arg1, int arg2,
			long arg3) {
		String tmp = (String) parent.getAdapter().getItem(arg2);
		Settings.getInstance().putValue(Settings.TOUR_KEY,
				Settings.DEFAULT_ROOT_TOUR + tmp);
		Util.log("Changed tour file to " + tmp, null);
		Fragment fragment = getFragmentManager().findFragmentByTag(
				TourFragment.TAG);
		if (fragment != null) {
			((TourFragment) fragment).updateFile();
		}
		fileDialog.dismiss();
	}

	public static boolean isConnected() {
		boolean isConnected = false;
		if (networkService != null) {
			isConnected = networkService.isConnected();
		}
		return isConnected;
	}

	@Override
	public void sendCommand(String cmd) {
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Needed for Landscape mode
		return false;
	}
}

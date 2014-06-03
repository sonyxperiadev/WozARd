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

package com.sonymobile.wozard.wizard.fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.sonymobile.wozard.wizard.ControllerActivity;
import com.sonymobile.wozard.wizard.ControllerActivity.ControllerCallback;
import com.sonymobile.wozard.wizard.CreateNotificationActivity;
import com.sonymobile.wozard.wizard.OrganizeNotificationActivity;
import com.sonymobile.wozard.wizard.R;
import com.sonymobile.wozard.wizard.SmartWatchReceiver;
import com.sonymobile.wozard.wizard.utils.Notification;
import com.sonymobile.wozard.wizard.utils.NotificationListAdapter;
import com.sonymobile.wozard.wizard.utils.NotificationListAdapter.ListViewItemCallback;
import com.sonymobile.wozard.wizard.utils.NotificationParser;
import com.sonymobile.wozard.wizard.utils.Settings;
import com.sonymobile.wozard.wizard.utils.Util;

/**
 * Fragment class to handle the Notification view.
 */
public class NotificationFragment extends Fragment implements OnClickListener, SmartWatchReceiver,
		ListViewItemCallback, OnCheckedChangeListener, ControllerCallback {
	public static final String TAG = "NotificationFragment";
	private boolean mNotificationFileExists = true;
	private ListView list;
	private ImageView createView;
	private ImageView organizeView;
	private List<Notification> notificationList = new ArrayList<Notification>();
	NotificationListAdapter notificationListAdapter;
	private Switch switchEnableSound;
	private TextView mConnectedStatusView;
	private boolean mSoundEnabled = false;
	private NotificationSmartWatchCallback smartWatchCallback;
	ArrayList<Notification> notificationFromXml;

	public NotificationFragment() {
	    this.setRetainInstance(true);	
	}

	public void registerSmartWatchCallback(NotificationSmartWatchCallback swc) {
		smartWatchCallback = swc;
	}

	/**
	 * Used to communicate with the underlying activity
	 */
	public interface NotificationSmartWatchCallback {
		/**
		 * Function to send command
		 */
		public void onPress();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ControllerActivity.registerCallback(this);
		Util.actionBarSetup(getActivity(), false, R.string.notificationTitle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.notification_layout, container, false);

		fillListFromXML();

		notificationListAdapter = new NotificationListAdapter(getActivity(),
				R.layout.notification_item, notificationList);
		notificationListAdapter.registerCallback(this);

		list = (ListView)view.findViewById(R.id.notificationList);
		list.setAdapter(notificationListAdapter);

		switchEnableSound = (Switch)view.findViewById(R.id.switchEnableSound);

		mSoundEnabled = getSoundEnabledPreference();
		switchEnableSound.setActivated(mSoundEnabled);
		switchEnableSound.setOnCheckedChangeListener(this);

		if (mNotificationFileExists) {
			createView = (ImageView) view.findViewById(R.id.createNotifications);
			createView.setOnClickListener(this);

			organizeView = (ImageView) view.findViewById(R.id.organizeNotifications);
			organizeView.setOnClickListener(this);
		} else {
			Toast.makeText(getActivity(), R.string.notificationErrorMessage, Toast.LENGTH_LONG).show();
		}

		mConnectedStatusView = (TextView) view.findViewById(R.id.connection_text);
		if (mConnectedStatusView != null) {
		    mConnectedStatusView.setBackgroundColor(Color.RED);
		    mConnectedStatusView.setText("Not connected to any player");
		}

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		Util.updateConnectionStatusView(getActivity(), mConnectedStatusView,
				ControllerActivity.isConnected());
		Util.updatePreviewImage(getActivity());
		fillListFromXML();
		notificationListAdapter.notifyDataSetChanged();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		ControllerActivity.unRegisterCallback(this);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		switchEnableSound.setChecked(mSoundEnabled);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.createNotifications:
				if (getActivity().getRequestedOrientation() ==
				        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
				    Intent intent = new Intent();
				    intent.setClass(getActivity(), CreateNotificationActivity.class);
				    getActivity().startActivity(intent);
				} else {
					Intent intent = new Intent();
				    intent.setClass(getActivity(), CreateNotificationActivity.class);
				    getActivity().startActivity(intent);
				}
				break;
			case R.id.organizeNotifications:
				if (getActivity().getRequestedOrientation() ==
		                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
					Intent intent = new Intent();
					intent.setClass(getActivity(), OrganizeNotificationActivity.class);
					getActivity().startActivity(intent);
				} else {
					Intent intent = new Intent();
					intent.setClass(getActivity(), OrganizeNotificationActivity.class);
					getActivity().startActivity(intent);
				}
				break;
		}
	}

	/**
	 * Function to the send the full notification message to the puppet
	 */
	private void sendNotification(int position, boolean showMessage) {
		if (showMessage) {
			if (mSoundEnabled) {
				if (notificationList.get(position).getType().contentEquals("Info")) {
					String command = ControllerActivity.SHOW_INFO_NOTIFICATION_CMD
							+ " " + notificationList.get(position).getType() + "#" 
							+ notificationList.get(position).getIconFileName() + "¤"
					        + notificationList.get(position).getMessage();
					String filePath = notificationList.get(position).getIconFileName();
					String speech = notificationList.get(position).getMessage().toString();
					Bitmap bitmap = notificationList.get(position).getIcon();
					ControllerActivity.sendCommandToPuppet(command, filePath, speech, bitmap);
				} else {
					String command = ControllerActivity.SHOW_BIG_NOTIFICATION_CMD
							+ " " + notificationList.get(position).getType() + "#" 
					        + notificationList.get(position).getMessage();
					String filePath = notificationList.get(position).getIconFileName();
					String speech = notificationList.get(position).getMessage().toString();
					Bitmap bitmap = notificationList.get(position).getIcon();
					ControllerActivity.sendCommandToPuppet(command, filePath, speech, bitmap);
				}
			} else {
				if (notificationList.get(position).getType().contentEquals("Info")) {
					String command = ControllerActivity.SHOW_INFO_NOTIFICATION_CMD
							+ " " + notificationList.get(position).getType() + "#"
							+ notificationList.get(position).getIconFileName() + "¤"
					        + notificationList.get(position).getMessage();
					String filePath = notificationList.get(position).getIconFileName();
					Bitmap bitmap = notificationList.get(position).getIcon();
					ControllerActivity.sendCommandToPuppet(command, filePath, null, bitmap);
				} else {
					String command = ControllerActivity.SHOW_BIG_NOTIFICATION_CMD
							+ " " + notificationList.get(position).getType() + "#"
					        + notificationList.get(position).getMessage();
					String filePath = notificationList.get(position).getIconFileName();
					Bitmap bitmap = notificationList.get(position).getIcon();
					ControllerActivity.sendCommandToPuppet(command, filePath, null, bitmap);
				}
			}
		} else {
			if (notificationList.get(position).getType().contentEquals("Info")) {
				String command = ControllerActivity.SHOW_SMALL_INFO_NOTIFICATION_CMD
						+ " " + notificationList.get(position).getType() + "#"
						+ notificationList.get(position).getIconFileName();
				String filePath = notificationList.get(position).getIconFileName();
				Bitmap bitmap = notificationList.get(position).getIcon();
				ControllerActivity.sendCommandToPuppet(command, filePath, null, bitmap);
			} else {
				String command = ControllerActivity.SHOW_SMALL_NOTIFICATION_CMD
						+ " " + notificationList.get(position).getType();
				String filePath = notificationList.get(position).getIconFileName();
				Bitmap bitmap = notificationList.get(position).getIcon();
				ControllerActivity.sendCommandToPuppet(command, filePath, null, bitmap);
			}
		}
	}

	/**
	 * Helper function to populate the notification messages from values stored
	 * in xml files.
	 *
	 * @param fileName the xml file to be parsed
	 */
	private void fillListFromXML() {
		if (notificationList != null) {
			notificationList.clear();
		}
		String filename = getResources().getString(R.string.xmlFileForNotifications);
		notificationFromXml = NotificationParser
				.parseNotifications(Settings.DEFAULT_ROOT_NOTIFICATIONS + filename);
		if (notificationFromXml == null) {
			mNotificationFileExists = false;	
		}
		for (int i = 0; i < notificationFromXml.size(); i++) {
			int scaleWidth = 150;
			int scaleHeight = 150;
			Bitmap bitmap = null;
			String iconPath = notificationFromXml.get(i).getIconFileName();
			String messageType = notificationFromXml.get(i).getType();
			String message = notificationFromXml.get(i).getMessage();
			String imgFilePath = Settings.DEFAULT_ROOT_NOTIFICATIONS + iconPath;
			Bitmap bm = BitmapFactory.decodeFile(imgFilePath);

			if (bm == null) {
				bm = BitmapFactory.decodeResource(getResources(), R.drawable.image_not_found);
			}
			bitmap = Bitmap.createScaledBitmap(bm, scaleWidth, scaleHeight, true);

			Notification n = new Notification(iconPath, message, messageType, bitmap);
			notificationList.add(n);
		}
	}

	@Override
	public void onUpSwipe() {
	}

	@Override
	public void onLeftSwipe() {
	}

	@Override
	public void onDownSwipe() {
	}

	@Override
	public void onPress() {
		smartWatchCallback.onPress();
	}

	@Override
	public void onLongPress() {
	}

	@Override
	public void onIconClick(int position) {
		sendNotification(position, false);
	}

	@Override
	public void onMessageClick(int position) {
		sendNotification(position, true);
	}

	@Override
	public void onCheckedChanged(CompoundButton button, boolean enableStatus) {
		setSoundEnabledPreference(enableStatus);
		mSoundEnabled = enableStatus;
	}

	private void setSoundEnabledPreference(boolean enableSound) {
		SharedPreferences pref = getActivity().getSharedPreferences(getActivity().
				getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = pref.edit();
		prefsEditor.putBoolean(Settings.NOTIFICATION_SOUND_ENABLED, enableSound);
		prefsEditor.commit();
	}

	private boolean getSoundEnabledPreference() {
		SharedPreferences pref = getActivity().getSharedPreferences(getActivity().
				getPackageName(), Context.MODE_PRIVATE);

		if (pref.contains(Settings.NOTIFICATION_SOUND_ENABLED)) {
		    mSoundEnabled = pref.getBoolean(Settings.NOTIFICATION_SOUND_ENABLED, false); 
		} else {
			SharedPreferences.Editor prefsEditor = pref.edit();
			prefsEditor.putBoolean(Settings.NOTIFICATION_SOUND_ENABLED, false);
			prefsEditor.commit();
		}
		return mSoundEnabled;
	}

	@Override
	public void onConnected(boolean isConnected) {
		if (mConnectedStatusView != null) {
			Util.updateConnectionStatusView(getActivity(), mConnectedStatusView, isConnected);
		}
	}

	@Override
	public void onNewPreviewImage(Bitmap previewImage) {
		Util.previewImage(getActivity(), previewImage);
	}

	@Override
	public void puppetVideoSeeThroughDevice(boolean videoSeeThroughDevice) {
		// Not used
	}
}

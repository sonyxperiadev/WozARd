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

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.sonymobile.wozard.wizard.ControllerActivity;
import com.sonymobile.wozard.wizard.R;
import com.sonymobile.wozard.wizard.SmartWatchReceiver;
import com.sonymobile.wozard.wizard.SoundPlayUDPThread;
import com.sonymobile.wozard.wizard.ControllerActivity.ControllerCallback;
import com.sonymobile.wozard.wizard.utils.Settings;
import com.sonymobile.wozard.wizard.utils.Util;

/**
 * This fragment is used for the Settings.
 */
public class SettingsFragment extends Fragment implements OnClickListener,
		SmartWatchReceiver, OnItemSelectedListener, ControllerCallback {
	public static final String TAG = "SettingsFragment";
	private static boolean mTransferSound = false;
	private static boolean mTakeAutoPicturesEnabled = false;
	private static boolean mSmartWatchOnRightHand = false;
	private static String mContinueForward = "Continue forward";
	private static String mTurnLeft = "Turn left";
	private static String mTurnRight = "Turn right";
	private static String mTurnAround = "Turn around";
	private static String mStop = "Stop";
	private static String mAutoPictureIntervall = "15";
	private CheckBox smartwatchBox;

	private EditText customForward;
	private EditText customLeft;
	private EditText customStop;
	private EditText customRight;
	private EditText customTurnAround;
	private CheckBox autoBox;
	private EditText picInterval;
	private CheckBox transferSoundBox;
	private TextView mConnectedStatusView;
	private SoundPlayUDPThread soundThread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ControllerActivity.registerCallback(this);
		Util.actionBarSetup(getActivity(), false, R.string.settingsTitle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.settings_layout, null);

		getSharedPreferences();

		smartwatchBox = (CheckBox) v.findViewById(R.id.smartwatch_check);
		smartwatchBox.setOnClickListener(this);
		smartwatchBox.setChecked(mSmartWatchOnRightHand);

		customForward = (EditText) v.findViewById(R.id.customSoundForwardEdit);
		customLeft = (EditText) v.findViewById(R.id.customSoundLeftEdit);
		customStop = (EditText) v.findViewById(R.id.customSoundStopEdit);
		customRight = (EditText) v.findViewById(R.id.customSoundRightEdit);
		customTurnAround = (EditText) v
				.findViewById(R.id.customSoundTurnAroundEdit);

		customForward.setText(mContinueForward);
		customRight.setText(mTurnRight);
		customLeft.setText(mTurnLeft);
		customStop.setText(mStop);
		customTurnAround.setText(mTurnAround);

		transferSoundBox = (CheckBox) v.findViewById(R.id.transfer_sound_box);
		if (transferSoundBox != null) {
			transferSoundBox.setOnClickListener(this);
		    transferSoundBox.setChecked(mTransferSound);
		}

		autoBox = (CheckBox) v.findViewById(R.id.auto_box);
		if (autoBox != null) {
			autoBox.setOnClickListener(this);
		    autoBox.setChecked(mTakeAutoPicturesEnabled);
		}
		picInterval = (EditText) v.findViewById(R.id.autoIntervalNumber);
		picInterval.setText(mAutoPictureIntervall);

		mConnectedStatusView = (TextView) v.findViewById(R.id.connection_text);
		if (mConnectedStatusView != null) {
			mConnectedStatusView.setBackgroundColor(Color.RED);
		    mConnectedStatusView.setText("Not connected to any player");
		}
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mConnectedStatusView != null) {
			Util.updateConnectionStatusView(getActivity(), mConnectedStatusView,
				ControllerActivity.isConnected());
		}
		Util.updatePreviewImage(getActivity());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.auto_box:
			mTakeAutoPicturesEnabled = !mTakeAutoPicturesEnabled;
		    if (autoBox.isChecked()){
			    ControllerActivity.sendCommandToPuppet(ControllerActivity.AUTO_PICS_ON + " "
				        + picInterval.getText(), null, null, null);
				Util.log("Starting to take automatic pictures every " + picInterval.getText().toString() + " s", null);
			} else {
				Util.log("Automatic pictures turned off", null);
				ControllerActivity.sendCommandToPuppet(ControllerActivity.AUTO_PICS_OFF,
						null, null, null);
			}
			break;
		case R.id.transfer_sound_box:
			mTransferSound = !mTransferSound;
			if (transferSoundBox.isChecked()) {
			    ControllerActivity.sendCommandToPuppet(ControllerActivity.SEND_SOUND_CMD,
                        null, null, null);
				Util.log("Sound stream started", null);
				startSoundUDPThread();
			} else {
				ControllerActivity.sendCommandToPuppet(ControllerActivity.SEND_SOUND_OFF_CMD,
						null, null, null);
				Util.log("Sound stream stopped", null);
				stopSoundUDPThread();
			}
			break;
		case R.id.smartwatch_check:
			mSmartWatchOnRightHand = !mSmartWatchOnRightHand;
		default:
			break;
		}
		setSharedPreferences();
	}

	private void startSoundUDPThread() {
		if (soundThread == null) {
			soundThread = new SoundPlayUDPThread();
			soundThread.start();
		}
	}

	private void stopSoundUDPThread() {
		if (soundThread == null)
			return;
		soundThread.killThread();
		soundThread = null;
	}

	private void setSharedPreferences() {
		SharedPreferences pref = getActivity().getSharedPreferences(getActivity().
				getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = pref.edit();
		prefsEditor.putBoolean(Settings.TRANSFER_SOUND_ENABLED, mTransferSound);
		prefsEditor.putBoolean(Settings.TAKE_AUTO_PICTURES_ENABLED, mTakeAutoPicturesEnabled);
		prefsEditor.putBoolean(Settings.SMARTWATCH_ON_RIGHT_HAND, mSmartWatchOnRightHand);
		prefsEditor.putString(Settings.CONTINUE_FORWARD, mContinueForward);
		prefsEditor.putString(Settings.TURN_LEFT, mTurnLeft);
		prefsEditor.putString(Settings.TURN_RIGHT, mTurnRight);
		prefsEditor.putString(Settings.TURN_AROUND, mTurnAround);
		prefsEditor.putString(Settings.STOP, mStop);
		prefsEditor.putString(Settings.AUTO_PICTURE_INTERVALL, mAutoPictureIntervall);
		prefsEditor.commit();
	}

	private void getSharedPreferences() {
		SharedPreferences pref = getActivity().getSharedPreferences(getActivity().
				getPackageName(), Context.MODE_PRIVATE);

		if (pref.contains(Settings.TRANSFER_SOUND_ENABLED)) {
		    mTransferSound = pref.getBoolean(Settings.TRANSFER_SOUND_ENABLED, false);
		    mTakeAutoPicturesEnabled = pref.getBoolean(Settings.TAKE_AUTO_PICTURES_ENABLED, false);
		    mSmartWatchOnRightHand = pref.getBoolean(Settings.SMARTWATCH_ON_RIGHT_HAND, false);
		    mContinueForward = pref.getString(Settings.CONTINUE_FORWARD, mContinueForward);
		    mTurnLeft = pref.getString(Settings.TURN_LEFT, mTurnLeft);
		    mTurnRight = pref.getString(Settings.TURN_RIGHT, mTurnRight);
		    mTurnAround = pref.getString(Settings.TURN_AROUND, mTurnAround);
		    mStop = pref.getString(Settings.STOP, mStop);
		    mAutoPictureIntervall = pref.getString(Settings.AUTO_PICTURE_INTERVALL, mAutoPictureIntervall);
		} else {
			SharedPreferences.Editor prefsEditor = pref.edit();
			prefsEditor.putBoolean(Settings.TRANSFER_SOUND_ENABLED, false);
			prefsEditor.putBoolean(Settings.TAKE_AUTO_PICTURES_ENABLED, mTakeAutoPicturesEnabled);
			prefsEditor.putBoolean(Settings.SMARTWATCH_ON_RIGHT_HAND, mSmartWatchOnRightHand);
			prefsEditor.putString(Settings.CONTINUE_FORWARD, mContinueForward);
			prefsEditor.putString(Settings.TURN_LEFT, mTurnLeft);
			prefsEditor.putString(Settings.TURN_RIGHT, mTurnRight);
			prefsEditor.putString(Settings.TURN_AROUND, mTurnAround);
			prefsEditor.putString(Settings.STOP, mStop);
			prefsEditor.putString(Settings.AUTO_PICTURE_INTERVALL, mAutoPictureIntervall);
			prefsEditor.commit();
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
	}

	@Override
	public void onLongPress() {
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mContinueForward = customForward.getText().toString();
		mTurnLeft = customLeft.getText().toString();
		mTurnRight = customRight.getText().toString();
		mTurnAround = customTurnAround.getText().toString();
		mStop = customStop.getText().toString();
		mAutoPictureIntervall = picInterval.getText().toString();
		setSharedPreferences();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		ControllerActivity.unRegisterCallback(this);
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

	/*
	 * Returns the string for ContinueForward
	 */
	public static String getContinueForward() {
		return mContinueForward;
	}

	/*
	 * Returns the string for TurnLeft
	 */
	public static String getTurnLeft() {
		return mTurnLeft;
	}

	/*
	 * Returns the string for TurnRight
	 */
	public static String getTurnRight() {
		return mTurnRight;
	}

	/*
	 * Returns the string for TurnAround
	 */
	public static String getTurnAround() {
		return mTurnAround;
	}

	/*
	 * Returns the string for Stop
	 */
	public static String getStop() {
		return mStop;
	}

	@Override
	public void puppetVideoSeeThroughDevice(boolean videoSeeThroughDevice) {
		// Not used
	}
}

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.sonymobile.wozard.wizard.ControllerActivity;
import com.sonymobile.wozard.wizard.R;
import com.sonymobile.wozard.wizard.SmartWatchReceiver;
import com.sonymobile.wozard.wizard.ControllerActivity.ControllerCallback;
import com.sonymobile.wozard.wizard.interfaces.CommandSender;
import com.sonymobile.wozard.wizard.utils.Settings;
import com.sonymobile.wozard.wizard.utils.Util;

/**
 * Fragment class to handle the Navigation view.
 */
public class NavigationFragment extends Fragment implements OnClickListener, SmartWatchReceiver, 
        OnCheckedChangeListener, ControllerCallback {

	private ImageButton forward;
	private ImageButton left;
	private ImageButton right;
	private ImageButton stop;
	private ImageButton back;
	private Switch switchEnabled;
	private TextView mConnectedStatusView;
	private boolean mSoundEnabled = true;
	public static final String TAG = "NavigationFragment";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ControllerActivity.registerCallback(this);
		Util.actionBarSetup(getActivity(), false, R.string.navigationTitle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.navigation_layout, container, false);
		forward = (ImageButton) v.findViewById(R.id.imageButtonForward);
		left = (ImageButton) v.findViewById(R.id.imageButtonLeft);
		right = (ImageButton) v.findViewById(R.id.imageButtonRight);
		stop = (ImageButton) v.findViewById(R.id.imageButtonStop);
		back = (ImageButton) v.findViewById(R.id.imageButtonBack);
		switchEnabled = (Switch) v.findViewById(R.id.switchEnableSound);

		forward.setOnClickListener(this);
		left.setOnClickListener(this);
		right.setOnClickListener(this);
		stop.setOnClickListener(this);
		back.setOnClickListener(this);

		mSoundEnabled = getSoundEnabledPreference();
		switchEnabled.setSelected(mSoundEnabled);
		switchEnabled.setOnCheckedChangeListener(this);

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
	public void onDestroy() {
		super.onDestroy();
		ControllerActivity.unRegisterCallback(this);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		switchEnabled.setChecked(mSoundEnabled);
	}

	@Override
	public void onClick(View v) {
		String cmd = "";
		String filePath = "";
		String sound = null;
		boolean send = true;
		cmd  += ControllerActivity.SHOW_IMAGE_CMD + " " + Settings.DEFAULT_ROOT_NAV;
		filePath += Settings.DEFAULT_ROOT_NAV;

		switch (v.getId()) {
		case R.id.imageButtonForward:
			cmd += "01 arrow_forward.png";
			filePath += "01 arrow_forward.png";
			sound = SettingsFragment.getContinueForward();
			break;
		case R.id.imageButtonRight:
			cmd += "02 arrow_right.png";
			filePath += "02 arrow_right.png";
			sound = SettingsFragment.getTurnRight();
			break;
		case R.id.imageButtonLeft:
			cmd	+= "03 arrow_left.png";
			filePath += "03 arrow_left.png";
			sound = SettingsFragment.getTurnLeft();
			break;
		case R.id.imageButtonStop:
			cmd	+= "06 stop.png";
			filePath += "06 stop.png";
			sound = SettingsFragment.getStop();
			break;
		case R.id.imageButtonBack:
			cmd	+= "04 arrow_turn_around.png";
			filePath += "04 arrow_turn_around.png";
			sound = SettingsFragment.getTurnAround();
			break;
		}
		if (send){
			Log.d(TAG, "sound: " + sound);
			Bitmap bitmap = Util.createBitmapFromFile(getActivity(), filePath);
			if (mSoundEnabled) {
                ControllerActivity.sendCommandToPuppet(cmd, filePath, sound, bitmap);
		    } else {
                ControllerActivity.sendCommandToPuppet(cmd, filePath, null, bitmap);
			}
		}
	}

	public interface NavigationCallback extends CommandSender{
		public void sendNavCommand(String cmd, String speech);
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
	public void onCheckedChanged(CompoundButton button, boolean enableStatus) {
		setSoundEnabledPreference(enableStatus);
	}

	private void setSoundEnabledPreference(boolean enableSound) {
		SharedPreferences pref = getActivity().getSharedPreferences(getActivity().
				getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = pref.edit();
		prefsEditor.putBoolean(Settings.NAVIGATION_SOUND_ENABLED, enableSound);
		prefsEditor.commit();
		mSoundEnabled = enableSound;
	}

	private boolean getSoundEnabledPreference() {
		SharedPreferences pref = getActivity().getSharedPreferences(getActivity().
				getPackageName(), Context.MODE_PRIVATE);

		if (pref.contains(Settings.NAVIGATION_SOUND_ENABLED)) {
		    mSoundEnabled = pref.getBoolean(Settings.NAVIGATION_SOUND_ENABLED, false); 
		} else {
			SharedPreferences.Editor prefsEditor = pref.edit();
			prefsEditor.putBoolean(Settings.NAVIGATION_SOUND_ENABLED, false);
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

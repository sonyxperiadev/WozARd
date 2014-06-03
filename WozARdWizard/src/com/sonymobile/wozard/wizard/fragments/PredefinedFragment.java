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

import java.io.File;
import java.util.ArrayList;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sonymobile.wozard.wizard.ControllerActivity;
import com.sonymobile.wozard.wizard.R;
import com.sonymobile.wozard.wizard.SmartWatchReceiver;
import com.sonymobile.wozard.wizard.ControllerActivity.ControllerCallback;
import com.sonymobile.wozard.wizard.utils.Command;
import com.sonymobile.wozard.wizard.utils.CommandAdapter;
import com.sonymobile.wozard.wizard.utils.Settings;
import com.sonymobile.wozard.wizard.utils.Util;

/**
 * This fragment is used for the Predefined Sequences.
 */
public class PredefinedFragment extends Fragment implements
		OnItemClickListener, SmartWatchReceiver, OnItemSelectedListener, ControllerCallback {
	private ListView list;
	private Spinner predefSpinner;
	private TextView mConnectedStatusView;
	private ArrayAdapter<String> spinnerAdapter;
	public static final String TAG = "PredefinedFragment";
	private static Settings settings;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		settings = Settings.getInstance();
		ControllerActivity.registerCallback(this);
		Util.actionBarSetup(getActivity(), false, R.string.predefinedTitle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.predef_layout, container, false);
		int selected = 0;
		int file = 0;
		if (savedInstanceState != null){
			selected = savedInstanceState.getInt("selected");
			file = savedInstanceState.getInt("selectedFile");
		}
		predefSpinner = (Spinner) v.findViewById(R.id.predefSpinner);
		spinnerAdapter = new ArrayAdapter<String>(getActivity(),
				R.layout.spinner_layout,
				fillSpinner(Settings.DEFAULT_ROOT_PREDEF));
		list = (ListView) v.findViewById(R.id.command_list);
		if (!spinnerAdapter.isEmpty()) {
			spinnerAdapter.setDropDownViewResource(R.layout.spinner_layout);
			predefSpinner.setAdapter(spinnerAdapter);
			predefSpinner.setOnItemSelectedListener(this);
			predefSpinner.setSelection(file);
			if (predefSpinner.getSelectedItem() != null) {
				settings.putValue(Settings.PREDEF_KEY, Settings.DEFAULT_ROOT_PREDEF + predefSpinner.getSelectedItem().toString());
			}
			list.setOnItemClickListener(this);
			list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			list.setAdapter(new CommandAdapter(getActivity()));

			if (settings.getValue(Settings.PREDEF_KEY) == Settings.DEFAULT_ROOT_PREDEF) {
				settings.putValue(Settings.PREDEF_KEY, Settings.DEFAULT_ROOT_PREDEF
						+ predefSpinner.getSelectedItem().toString());
			} else {
				selected = (Integer)settings.getValue(Settings.PREDEF_KEY_POS);
				predefSpinner.setSelection((selected == -1) ? 0 : selected);
				list.setAdapter(new CommandAdapter(getActivity()));
			}
		}

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
	public void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		outState.putInt("selected", list.getSelectedItemPosition());
		outState.putInt("selectedFile", predefSpinner.getSelectedItemPosition());
	}

	/**
	 * Function to fill the spinner with available predefined xml files on the SD card
	 * @param path path to the res folder where the xml files are placed
	 * @return a array of available xml files
	 */
	private ArrayList<String> fillSpinner(String path) {
		ArrayList<String> files = new ArrayList<String>();
		File dir = new File(path);
		if (dir.listFiles() != null) {
			for (File f : dir.listFiles()) {
				if (f.isFile()) {
					files.add(f.getName());
				}
			}
		}
		return files;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Command tmp = (Command) parent.getAdapter().getItem(position);
		Bitmap bitmap = Util.createBitmapFromFile(getActivity(), tmp.getFilePath());
        ControllerActivity.sendCommandToPuppet(tmp.toString(), tmp.getFilePath(),
                null, bitmap);
	}

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		String selected = Settings.DEFAULT_ROOT_PREDEF + predefSpinner.getSelectedItem().toString();
		settings.putValue(Settings.PREDEF_KEY, selected);

		settings.putValue(Settings.PREDEF_KEY_POS, spinnerAdapter.getPosition((String) predefSpinner.getSelectedItem()));
		list.setAdapter(new CommandAdapter(getActivity()));
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
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	@Override
	public void onConnected(boolean isConnected) {
		if (mConnectedStatusView != null) {
			Util.updateConnectionStatusView(getActivity(), mConnectedStatusView,
				isConnected);
		}
	}

	@Override
	public void onNewPreviewImage(Bitmap previewImage) {
		Util.previewImage(getActivity(), previewImage);
		Toast.makeText(getActivity(), "onNewPreviewImage in Predefined", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void puppetVideoSeeThroughDevice(boolean videoSeeThroughDevice) {
		// Not used
	}
}

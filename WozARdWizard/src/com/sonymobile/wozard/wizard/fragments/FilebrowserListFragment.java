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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sonymobile.wozard.wizard.ControllerActivity;
import com.sonymobile.wozard.wizard.R;
import com.sonymobile.wozard.wizard.ControllerActivity.ControllerCallback;
import com.sonymobile.wozard.wizard.utils.Settings;
import com.sonymobile.wozard.wizard.utils.Util;
/**
 * This fragment is used for the Filebrowser in portrait modes.
 */
public class FilebrowserListFragment extends Fragment implements OnItemClickListener, 
        ControllerCallback {
	private List<String> directoryEntries = new ArrayList<String>();
	private ListView list;
	private File currentDirectory;
	private String root;
	private TextView mConnectedStatusView;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		currentDirectory = new File(Settings.DEFAULT_ROOT);
		root = currentDirectory.getAbsolutePath();

		ControllerActivity.registerCallback(this);
		Util.actionBarSetup(getActivity(), false, R.string.fileBrowserTitle);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.file_layout, null);
		list = (ListView)v.findViewById(R.id.file_list);

		if (list!=null) {
			list.setOnItemClickListener(this);
		}

		mConnectedStatusView = (TextView) v.findViewById(R.id.connection_text);
		mConnectedStatusView.setBackgroundColor(Color.RED);
		mConnectedStatusView.setText("Not connected to any player");

		browseTo(currentDirectory);
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		Util.updateConnectionStatusView(getActivity(), mConnectedStatusView, 
				ControllerActivity.isConnected());
		Util.updatePreviewImage(getActivity());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		ControllerActivity.unRegisterCallback(this);
	}

	/**
	 * Enters a folder or callbacks if it is a file
	 * @param target The folder to enter or file to be sent
	 */
	private void browseTo(File target){
		if (target.isDirectory()) {
			currentDirectory = target;
			fill(target.listFiles());
		} else {
			Util.log("onFileSelected(" + target.getAbsolutePath() + ");", null);
			String cmd = ControllerActivity.getCommandFromName(target.getAbsolutePath()) +
					" " + target.getAbsolutePath();
			Bitmap bitmap = Util.createBitmapFromFile(getActivity(), target.getAbsolutePath());
            ControllerActivity.sendCommandToPuppet(cmd, target.getAbsolutePath(), null, bitmap);
		}
	}

	/**
	 * Fills the filelist with files
	 * @param files - a list of files
	 */
	private void fill(File[] files){
		directoryEntries.clear();
		// add .. if we are not at the root folder
		if(currentDirectory.getParent() != null && !currentDirectory.getAbsolutePath().equals(root))
			directoryEntries.add("..");
		for(File file : files){
			directoryEntries.add(file.getName());
		}
		Collections.sort(directoryEntries, new Comparator<String>(){
			@Override
			public int compare(String lhs, String rhs) {
				return lhs.compareToIgnoreCase(rhs);
			}
		});
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_expandable_list_item_1, directoryEntries);
		if (list != null)
			list.setAdapter(adapter);
	}

	/**
	 * Function to see if we have reached to highest level in the file structure on the SD card that is allowed.
	 * @return true if we are allow to go higher in the structure, false otherwise
	 */
	public boolean upOneLevel(){
		if (!currentDirectory.getAbsolutePath().equals(root)){
			browseTo(currentDirectory.getParentFile());
			return true;
		}
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> adapterview, View arg1, int pos, long id) {

		int selectionRowId = (int) id;

		String selection = directoryEntries.get(selectionRowId);
		if (selection.equals(".."))
			upOneLevel();
		else {
			File clickedFile = null;
			clickedFile = new File(currentDirectory.getAbsolutePath() + "/" +
					this.directoryEntries.get(selectionRowId));
			if (clickedFile != null) {
				browseTo(clickedFile);
			}
		}
	}

	@Override
	public void onConnected(boolean isConnected) {
		Util.updateConnectionStatusView(getActivity(), mConnectedStatusView, 
				isConnected);
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

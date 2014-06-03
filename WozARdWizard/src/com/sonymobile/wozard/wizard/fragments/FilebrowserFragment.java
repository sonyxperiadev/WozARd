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

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.sonymobile.wozard.wizard.ControllerActivity;
import com.sonymobile.wozard.wizard.R;
import com.sonymobile.wozard.wizard.SmartWatchReceiver;
import com.sonymobile.wozard.wizard.utils.ImageAdapter;
import com.sonymobile.wozard.wizard.utils.Settings;
import com.sonymobile.wozard.wizard.utils.Util;

/**
 * This fragment is used for the Filebrowser.
 */
@SuppressLint("ValidFragment")
public class FilebrowserFragment extends Fragment implements SmartWatchReceiver {
	private List<String> directoryEntries = new ArrayList<String>();
	private GridView gridView;
	private File currentDirectory;
	private String rootDir;
	public static final String ROOT_DIR = "start_dir";
	public static final String TAG = "FileFragment";

	public FilebrowserFragment(){
		rootDir = Settings.DEFAULT_ROOT;
	}

	public FilebrowserFragment(String root){
		if(root == null)
			rootDir = Settings.DEFAULT_ROOT;
		else
			rootDir = root;
	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		currentDirectory = new File(Settings.DEFAULT_ROOT);
		rootDir = currentDirectory.getAbsolutePath();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.file_layout, container, false);
		gridView = (GridView)v.findViewById(R.id.file_grid);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Log.d("FileFragment", "getSelectedItem = " + position);
				String selectedFileString = directoryEntries.get(position);
				if(selectedFileString.equals("..")){
					upOneLevel();
				} else {
					File selectedFile = null;
					selectedFile = new File(currentDirectory.getAbsolutePath() + directoryEntries.get(position));
					if(selectedFile != null)
						browseTo(selectedFile);
				}
			}
		});
		browseTo(currentDirectory);
		return v;
	}

	/**
	 * Browse up one level
	 * @return true if it was able to go up false otherwise
	 */
	private boolean upOneLevel(){
		if (!currentDirectory.getAbsolutePath().equals(rootDir)){
			browseTo(currentDirectory.getParentFile());
			return true;
		}
		return false;
	}

	public void reset(){
		browseTo(new File(rootDir));
	}

	/**
	 * Enters a folder or callbacks if it is a file
	 * @param target The folder to enter or file to be sent
	 */
	private void browseTo(File target){
		if (target.isDirectory()) {
			this.currentDirectory = target;
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
	 * Populates the Filebrowser fragment with items
	 * @param files the files to populate the fragment
	 */
	private void fill(File[] files){
		if (files != null) {
			directoryEntries.clear();

			// Add the "." and the ".." == 'Up one level'
			if (this.currentDirectory.getParent() != null)
				this.directoryEntries.add("..");
			int currentPathStringLenght = this.currentDirectory.getAbsolutePath()
					.length();

			for (File file : files) {
				this.directoryEntries.add(file.getAbsolutePath().substring(
						currentPathStringLenght));
			}
			Collections.sort(directoryEntries, new Comparator<String>(){
				@Override
				public int compare(String lhs, String rhs) {
					return lhs.compareToIgnoreCase(rhs);
				}
			});
			gridView.setAdapter(new ImageAdapter(getActivity(), currentDirectory
					.getAbsolutePath(), directoryEntries));
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
}

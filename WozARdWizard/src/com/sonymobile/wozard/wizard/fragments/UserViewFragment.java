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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sonymobile.wozard.wizard.ControllerActivity;
import com.sonymobile.wozard.wizard.ControllerActivity.ControllerCallback;
import com.sonymobile.wozard.wizard.R;
import com.sonymobile.wozard.wizard.utils.Util;
/**
 * This fragment is used for the Puppet view.
 */
public class UserViewFragment extends Fragment implements ControllerCallback {
	public static final String TAG = "UserFragment";
	private ImageView mImageView;
	private TextView mConnectedStatusView;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		ControllerActivity.registerCallback(this);
		Util.actionBarSetup(getActivity(), false, R.string.puppetTitle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.userview_layout, container,false);
		//TODO show both camera and overlay!!
		//ControllerActivity.sendCommandToPuppet(ControllerActivity.GET_SCREEN_CMD,
			//	null, null, null);
		mImageView = (ImageView)v.findViewById(R.id.screenshot);

		mConnectedStatusView = (TextView) v.findViewById(R.id.connection_text);
		if (mConnectedStatusView != null) {
			mConnectedStatusView.setBackgroundColor(Color.RED);
		    mConnectedStatusView.setText("Not connected to any player");
		}

		if (mImageView != null) {
			mImageView.setImageBitmap(ControllerActivity.getPreviewImage());	
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

	/**
	 * Sets the image in the fragment
	 * @param img the image from the puppet to be set
	 */
	public void setScreen(Bitmap img){
		if (mImageView == null)
			return;
		mImageView.setImageBitmap(img);
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

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

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.sonymobile.wozard.wizard.CameraService;
import com.sonymobile.wozard.wizard.CameraService.CameraServiceCallback;
import com.sonymobile.wozard.wizard.ControllerActivity;
import com.sonymobile.wozard.wizard.ControllerActivity.ControllerCallback;
import com.sonymobile.wozard.wizard.R;
import com.sonymobile.wozard.wizard.SmartWatchReceiver;
import com.sonymobile.wozard.wizard.utils.Util;

/**
 * This fragment is used for the Camera.
 */
@TargetApi(14)
public class CameraFragment extends Fragment implements OnClickListener, SmartWatchReceiver,
        CameraServiceCallback, ServiceConnection, ControllerCallback, OnCheckedChangeListener {
	public static final String TAG = "CameraFragment";
	private ImageView mImageView;
	private Button captureButton;
	private Switch mSwitchEnableCamera;
	private View v;
	private CameraService cameraService;
	private TextView mConnectedStatusView;
	private boolean mPuppetVideoSeeThroughDevice = false;
	private boolean mEnablePuppetCamera = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = new Intent();
		intent.setClass(getActivity(), CameraService.class);
		getActivity().startService(intent);

		ControllerActivity.registerCallback(this);
		Util.actionBarSetup(getActivity(), false, R.string.cameraTitle);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mSwitchEnableCamera.setActivated(!mPuppetVideoSeeThroughDevice);
		mSwitchEnableCamera.setChecked(mEnablePuppetCamera);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		v = inflater.inflate(R.layout.camera_layout, container, false);
		final RelativeLayout layout = (RelativeLayout) v
				.findViewById(R.id.pressArea);
		mImageView = (ImageView) v.findViewById(R.id.camera_view);
		mImageView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (layout.getChildCount() > 1) {
					Log.d("player", "rootlayout removing child");
					layout.removeViewAt(1);
				}

                ImageView image = new ImageView(getActivity());
                image.setImageResource(R.drawable.product_found_indicator);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				        (int)(layout.getWidth() * 0.4), (int)(layout.getHeight() * 0.4));
                // times 0.9 for screen adjustment
				// Display display = getWindowManager().getDefaultDisplay();
				params.leftMargin = (int) ((event.getX() - params.width / 2));
				params.topMargin = (int) ((event.getY() - params.height / 2));
				layout.addView(image, params);
				xyValues(event.getX() / (float) v.getWidth(),
						event.getY() / (float) v.getHeight());
				Util.log("Displaying an indicator on the puppet device. x: " + event.getX() + " y: " + event.getY() , null);

				return false;
			}
		});
		Bitmap bm = Bitmap.createBitmap(300, 300, Config.ARGB_4444);
		mImageView.setImageBitmap(bm);

		captureButton = (Button) v.findViewById(R.id.camera_button);
		captureButton.setOnClickListener(this);

		ControllerActivity.sendCommandToPuppet(ControllerActivity.VIDEO_SEE_THROUGH_DEVICE,
				null, null, null);
		mSwitchEnableCamera = (Switch) v.findViewById(R.id.switchEnableCamera);

		mSwitchEnableCamera.setActivated(!mPuppetVideoSeeThroughDevice);
		mSwitchEnableCamera.setOnCheckedChangeListener(this);

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

		if (ControllerActivity.isConnected()) {
			serverConnected();
			ControllerActivity.sendCommandToPuppet(ControllerActivity.SEND_CAMERA_CMD, 
					null, null, null);
			Util.log("Camera stream started", null);
			mImageView.invalidate();
		} else {
            serverDisconnected();
		}

		Intent intent = new Intent();
		intent.setClass(getActivity(), CameraService.class);
		getActivity().bindService(intent, this, Context.BIND_AUTO_CREATE);

		Util.updatePreviewImage(getActivity());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		ControllerActivity.unRegisterCallback(this);
	}

	/**
	 * Sets the UI of connection status
	 */
	private void serverConnected() {
		getActivity().runOnUiThread(new Runnable() {
			public void run() {
				if (mConnectedStatusView != null) {
		            mConnectedStatusView.setBackgroundColor(Color.GREEN);
		            mConnectedStatusView.setText(R.string.connected_text);
				}
			}
		});
	}

	/**
	 * Sets the UI of connection status
	 */
	private void serverDisconnected() {
		getActivity().runOnUiThread(new Runnable() {
			public void run() {
                if (mConnectedStatusView != null) {
			        mConnectedStatusView.setBackgroundColor(Color.RED);
                    mConnectedStatusView.setText(R.string.not_connected_text);
                }
			}
	    });
	}

	private void xyValues(float x, float y) {
		String cmd = ControllerActivity.SHOW_XY_COORDINATES_CMD + " " + x + ";" + y;
		ControllerActivity.sendCommandToPuppet(cmd, null, null, null);
	}

	/**
	 * Sets the image from the camera feed in the fragment
	 */
	public void setScreen(final Bitmap img) {
		if (getActivity() != null) {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (mImageView == null) {
						Log.d("setScreen", "imageview null");
						return;
					}
					mImageView.setImageBitmap(img);
				}
			});	
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.camera_button:
			Util.log("Taking picture", null);
			takePicture();
			break;
		default:
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean enableCamera) {
        if (enableCamera) {
            ControllerActivity.sendCommandToPuppet(ControllerActivity.CAMERA_CMD,
					null, null, null);
            mEnablePuppetCamera = true;
			Util.log("Start the puppet camera", null);
		} else {
		    Util.log("Turning off the camera", null);
			ControllerActivity.sendCommandToPuppet(ControllerActivity.CLEAR_INDICATOR_CMD,
					null, null, null);
			mEnablePuppetCamera = false;
		}
	}

	/**
	 * Helper function to do a callback for taking a picture on the puppet device
	 */
	public void takePicture() {
        ControllerActivity.sendCommandToPuppet(ControllerActivity.TAKE_PICTURE_CMD,
                null, null, null);
	}

	@Override
	public void onUpSwipe() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onLeftSwipe() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDownSwipe() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onPress() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onLongPress() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		cameraService = ((CameraService.LocalBinder) service).getService();
		cameraService.registerCallback(this);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		cameraService = null;
	}

	@Override
	public void onConnected(boolean isConnected) {
		if (isConnected) {
			serverConnected();
		} else {
			serverDisconnected();
		}
	}

	@Override
	public void onNewPreviewImage(Bitmap previewImage) {
		Util.previewImage(getActivity(), previewImage);
	}

	@Override
	public void puppetVideoSeeThroughDevice(boolean videoSeeThroughDevice) {
		mPuppetVideoSeeThroughDevice = videoSeeThroughDevice;
	}
}

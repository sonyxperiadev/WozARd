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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.sonymobile.wozard.wizard.R;
import com.sonymobile.wozard.wizard.utils.ImageSpinnerArrayAdapter;
import com.sonymobile.wozard.wizard.utils.Notification;
import com.sonymobile.wozard.wizard.utils.NotificationParser;
import com.sonymobile.wozard.wizard.utils.Settings;

public class CreateNotificationFragment extends Fragment implements OnItemSelectedListener {
	public static final String TAG = "CreateNotificationFragment";
	private Spinner typeSpinner;
	private EditText messageTextField;
	private Button saveButton;
	private ImageSpinnerArrayAdapter typeAdapter;
	private String mNewMessage = "";
	private String mNewMessageType = "";
	private String mIconFileName = "";
	private Bitmap mBitmap = null;
	private List<Notification> notificationList = new ArrayList<Notification>();
	private ArrayList<Notification> notificationFromXml;
	CreateNotificationCallback mCallback;

	public void registerCallback(CreateNotificationCallback cb) {
		mCallback = cb;
	}

	/**
     * Used to communicate with the fragment
     */
    public interface CreateNotificationCallback {
    	/**
    	 * Function to notify that the icon was clicked.
    	 * @param cmd containing the command, type and message of the notification
    	 */
    	public void onSaveButtonClicked();
    }


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.create_notification, container, false);

		fillListFromXML();

		typeSpinner = (Spinner) view.findViewById(R.id.typeSpinner);
		typeAdapter = new ImageSpinnerArrayAdapter(getActivity(),
				R.layout.spinner_small_layout, notificationList);
		typeAdapter.setDropDownViewResource(R.layout.spinner_layout);
		typeSpinner.setAdapter(typeAdapter);
		typeSpinner.setOnItemSelectedListener(this);
		messageTextField = (EditText) view.findViewById(R.id.createMessageText);
		messageTextField.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
                mNewMessage = messageTextField.getText().toString();
			}
		});

		saveButton = (Button) view.findViewById(R.id.saveNotificationButton);
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			    // Save the new created notification
				if (mBitmap == null) {
					mBitmap = notificationList.get(0).getIcon();
				}
				Notification notification = new Notification(mIconFileName, mNewMessage, mNewMessageType, mBitmap);
				notificationList.add(notification);
	 			notificationFromXml.add(notification);
				saveNewItem();
				if (mCallback !=null) {
					mCallback.onSaveButtonClicked();
				}
			}
		});	
		return view;
	}

	/**
	 * Helper function to populate the notification messages from values stored in xml files.
	 * @param fileName the xml file to be parsed
	 */
	private void fillListFromXML() {
		int scaleWidth = 120;
		int scaleHeight = 120;
		String filename = getResources().getString(R.string.xmlFileForNotifications);
		notificationFromXml = NotificationParser
				.parseNotifications(Settings.DEFAULT_ROOT_NOTIFICATIONS + filename);

		for (int i = 0; i < notificationFromXml.size(); i++) {
			Bitmap bitmap = null;
			String iconPath = notificationFromXml.get(i).getIconFileName();
			String messageType = notificationFromXml.get(i).getType();
			String message = notificationFromXml.get(i).getMessage();
	        String imgFilePath = Settings.DEFAULT_ROOT_NOTIFICATIONS + iconPath;
	        Bitmap bm = BitmapFactory.decodeFile(imgFilePath);
 			if (bm != null) {
 				bitmap = Bitmap.createScaledBitmap(bm, scaleWidth, scaleHeight, true);
 			}
 			
 			if (newMessageType(messageType)) {
 			    Notification n = new Notification(iconPath, message, messageType, bitmap);
                notificationList.add(n);
 			}
		}
	}

	/**
	 * Makes sure that only one notification type is listed in the spinner.
	 * @param messageType
	 * @return true, if this notification type is not already listed.
	 */
	private boolean newMessageType(String messageType) {
		if (notificationList != null) {
			for (int i = 0; i < notificationList.size(); i++) {
				if (messageType.endsWith(notificationList.get(i).getType())) {
				    return false;	
				}
			}
		}
		return true;
	}

	/**
	 * Save the new created notification.
	 * @param notification
	 */
	private void saveNewItem() {
		String filename = getResources().getString(R.string.xmlFileForNotifications);
		NotificationParser.saveNotificationsToXml(filename, notificationFromXml);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {

		if (parent.getAdapter() == typeAdapter) {
			mNewMessageType = ((Notification) parent
					.getItemAtPosition(pos)).getType();
			mIconFileName = ((Notification) parent
					.getItemAtPosition(pos)).getIconFileName();
			mBitmap = ((Notification) parent
					.getItemAtPosition(pos)).getIcon();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}
}

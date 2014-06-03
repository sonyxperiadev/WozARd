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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.sonymobile.wozard.wizard.R;
import com.sonymobile.wozard.wizard.utils.Notification;
import com.sonymobile.wozard.wizard.utils.NotificationParser;
import com.sonymobile.wozard.wizard.utils.OrganizeNotificationListAdapter;
import com.sonymobile.wozard.wizard.utils.Settings;
import com.sonymobile.wozard.wizard.utils.OrganizeNotificationListAdapter.OrganizeDeleteCallback;

public class OrganizeNotificationFragment extends Fragment implements OrganizeDeleteCallback {
	public static final String TAG = "OrganizeNotificationFragment";
    private ListView list;
	private List<Notification> notificationList = new ArrayList<Notification>();
	ArrayList<Notification> notificationFromXml;
	OrganizeNotificationListAdapter organizeListAdapter;
	OrganizeNotificationCallback mCallback;
	
	public void registerCallback(OrganizeNotificationCallback cb) {
		mCallback = cb;
	}
	
	/**
     * Used to communicate with the fragment
     */
    public interface OrganizeNotificationCallback {
    	/**
    	 * Function to notify that the icon was clicked.
    	 * @param cmd containing the command, type and message of the notification
    	 */
    	public void onDeleteButtonClicked();
    }

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.organize_layout, container, false);

		fillListFromXML();

		organizeListAdapter = new OrganizeNotificationListAdapter(getActivity(), 
				R.layout.organize_item, notificationList);
		organizeListAdapter.registerCallback(this);
		
		list = (ListView) view.findViewById(R.id.organizeList);
		list.setAdapter(organizeListAdapter);
		return view;
	}

	/**
	 * Helper function to populate the notification messages from values stored in xml files.
	 * @param fileName the xml file to be parsed
	 */
	private void fillListFromXML() {
		String filename = getResources().getString(R.string.xmlFileForNotifications);
		notificationFromXml = NotificationParser
				.parseNotifications(Settings.DEFAULT_ROOT_NOTIFICATIONS + filename);
		for (int i = 0; i < notificationFromXml.size(); i++) {
			//TODO: move to resources
			int scaleWidth = 150;
			int scaleHeight = 150;
			Bitmap bitmap = null;
			String iconPath = notificationFromXml.get(i).getIconFileName();
			String messageType = notificationFromXml.get(i).getType();
			String message = notificationFromXml.get(i).getMessage();
	        String imgFilePath = Settings.DEFAULT_ROOT_NOTIFICATIONS + iconPath;
	        Bitmap bm = BitmapFactory.decodeFile(imgFilePath);
 			if (bm != null) {
 				bitmap = Bitmap.createScaledBitmap(bm, scaleWidth, scaleHeight, true);
 			}
 			Notification n = new Notification(iconPath, message, messageType, bitmap);
			notificationList.add(n);
		}
	}

	@Override
	public void onDeleteClick(int position) {
		notificationFromXml.remove(position);
		notificationList.remove(position);
		String filename = getResources().getString(R.string.xmlFileForNotifications);
		NotificationParser.saveNotificationsToXml(filename, notificationFromXml);
		organizeListAdapter.notifyDataSetChanged();
		list.invalidateViews();
	}
}

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
package com.sonymobile.wozard.wizard.utils;

import java.util.ArrayList;
import java.util.List;

import com.sonymobile.wozard.wizard.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;

public class NotificationListAdapter extends ArrayAdapter<Notification> {
	private ImageButton notificationIcon;
	private Button message;
    private List<Notification> notifications = new ArrayList<Notification>();
    ListViewItemCallback mCallback;

    public void registerCallback(ListViewItemCallback cb) {
		mCallback = cb;
	}

    /**
     * Used to communicate with the fragment
     */
    public interface ListViewItemCallback {
    	/**
    	 * Function to notify that the icon was clicked.
    	 * @param cmd containing the command, type and message of the notification
    	 */
    	public void onIconClick(int position);
    	
    	/**
    	 * Function to notify that the message was clicked.
    	 */
    	public void onMessageClick(int position);
    }

    public NotificationListAdapter(Context context, int textViewResourceId,
			List<Notification> objects) {
		super(context, textViewResourceId, objects);
		this.notifications = objects;
	}
	
    public int getCount() {
        return this.notifications.size();
    }
 
    public Notification getItem(int index) {
        return this.notifications.get(index);
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
    	View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.notification_item, parent, false);
        }
 
        // Get item
        Notification notification = getItem(position);
         
        // Get reference to ImageView 
        notificationIcon = (ImageButton) row.findViewById(R.id.notificationIcon);
        notificationIcon.setImageBitmap(notification.getIcon());
        notificationIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				notifyFragment(pos, true);
			}
		});
        
        // Get reference to message button
        message = (Button) row.findViewById(R.id.message);
        message.setText(notification.getMessage());
        message.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				message.getText();
		    	notifyFragment(pos, false);
			}
		});
        return row;
    }

    /**
     * Function to the send the full notification message to the puppet
     */
    private void notifyFragment(int position, boolean onIconClick) {
	    if (onIconClick && (mCallback != null)) {
	        mCallback.onIconClick(position);
	    } else {
		    mCallback.onMessageClick(position);
	    }
    }
}
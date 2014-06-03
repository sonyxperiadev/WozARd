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
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * An adaptor for images in spinners
 */
public class ImageSpinnerArrayAdapter extends ArrayAdapter<Notification> {

	private List<Notification> notifications = new ArrayList<Notification>();

	public ImageSpinnerArrayAdapter(Context context, int resourceId,
			List<Notification> notificationTypes) {
		super(context, resourceId, notificationTypes);
		this.notifications = notificationTypes;
	}

	public static class ViewHolder {
		public TextView type;
		public ImageView notificationIcon;
	}

	@Override
	public int getCount() {
		return this.notifications.size();
	}

	public Notification getItem(int index) {
		return this.notifications.get(index);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder;
		if (row == null) {
			holder = new ViewHolder();
			LayoutInflater inflater = (LayoutInflater)this.getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.spinner_small_layout, null);
			// Get reference to ImageView
			holder.notificationIcon = (ImageView)row.findViewById(R.id.notificationIcon);
			// Get reference to messageType text
			holder.type = (TextView)row.findViewById(R.id.message);
			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}
		// Get item
		Notification notification = getItem(position);
		if (holder.notificationIcon != null) {
			holder.notificationIcon.setImageBitmap(notification.getIcon());
		}
		if (holder.type != null) {
			holder.type.setText(notification.getType());
		}
		return row;
	}

	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getView(position, convertView, parent);
	}
}

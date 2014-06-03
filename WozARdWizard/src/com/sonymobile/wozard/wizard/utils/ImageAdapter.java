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

import com.sonymobile.wozard.wizard.R;
import com.sonymobile.wozard.wizard.fragments.FilebrowserFragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

/**
 * An adapter for the {@link FilebrowserFragment}. Creates and populate the filebrowser.
 */

public class ImageAdapter extends BaseAdapter {
	private Context context;
	private List<String> directoryEntries;
	private String path;

	public ImageAdapter(Context context, String path, List<String> directoryEntries) {
		this.context = context;
		this.directoryEntries = directoryEntries;
		this.path = path;
	}

	public static class ViewHolder {
		public TextView textView;
		public ImageView imageView;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View gridView = convertView;
		ViewHolder holder;
		if (gridView == null) {
			holder = new ViewHolder();
			gridView = inflater.inflate(R.layout.grid, null);
			holder.textView = (TextView)gridView.findViewById(R.id.grid_item_label);
			holder.imageView = (ImageView)gridView.findViewById(R.id.grid_item_image);
			gridView.setTag(holder);
		} else {
			holder = (ViewHolder) gridView.getTag();
		}
		if (new File(path + directoryEntries.get(position)).isDirectory()) {
			if (directoryEntries.get(position) == "..") {
				holder.imageView.setImageResource(R.drawable.up_icon);
				holder.textView.setText("Up One Level");
			} else {
				holder.imageView.setImageResource(R.drawable.ic_folder);
				holder.textView.setText(directoryEntries.get(position).substring(1));
			}
		} else if (new File(path + directoryEntries.get(position)).isFile()) {
			final ImageView imageView = holder.imageView;
			new AsyncTask<Void, Void, Drawable>() {
				@Override
				protected Drawable doInBackground(Void... params) {
					return Drawable.createFromPath(path + directoryEntries.get(position));
				}
				@Override
				protected void onPostExecute(Drawable result) {
					super.onPostExecute(result);
					imageView.setImageDrawable(result);
				}
			}.execute();
			holder.textView.setText(directoryEntries.get(position).substring(1));
			String fileType = directoryEntries.get(position);
			if (fileType.contains(".mp3") || fileType.contains(".ogg") || fileType.contains(".wav")) {
				holder.imageView.setImageResource(R.drawable.ic_audio);
			}
		} else {
			if (directoryEntries.get(position) == "..") {
				holder.imageView.setImageResource(R.drawable.up_icon);
				holder.textView.setText("Up One Level");
			} else {
				holder.imageView.setImageResource(R.drawable.ic_launcher);
			}
		}
		return gridView;
	}

	@Override
	public int getCount() {
		return directoryEntries.size();
	}

	public Object getItem(int arg0) {
		return directoryEntries.get(arg0);
	}

	public long getItemId(int arg0) {
		return -1;
	}
}

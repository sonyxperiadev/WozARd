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

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sonymobile.wozard.wizard.fragments.PredefinedFragment;

/**
 * An adapter used to display {@link Command}s in the {@link PredefinedFragment}
 */
public class CommandAdapter extends BaseAdapter {
	private ArrayList<Command> commands;

	private Context context;
	private Settings settings;
	private int highlight = -1;

	public static class ViewHolder {
		public TextView t1;
		public TextView t2;
	}

	/**
	 * The constructor
	 */
	public CommandAdapter(Context context) {
		this.context = context;
		//TODO instead of using settings this should use onSaveInstanceState in the PredefinedFragment
		settings = Settings.getInstance();
		if (settings.getValue(Settings.PREDEF_KEY) != null)
			commands = CommandParser.parseCommands((String) settings
					.getValue(Settings.PREDEF_KEY));
		highlight = (Integer) settings.getValue(Settings.PREDEF_HIGHLIGHT);
	}

	@Override
	public int getCount() {
		if (commands == null)
			return 0;
		return commands.size();
	}

	/**
	 * Used to set the currently highlighted selection
	 * @param selection The item to highlight
	 */
	public void setHighlighted(int selection){
		highlight = selection;
	}
	
	@Override
	public Object getItem(int position) {
		highlight = position;
		return commands.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (commands == null)
			if (settings.getValue(Settings.PREDEF_KEY) != null)
				commands = CommandParser.parseCommands((String) settings
						.getValue(Settings.PREDEF_KEY));
		View gridView = convertView;
		ViewHolder holder;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (gridView == null) {
			holder = new ViewHolder();
			gridView = inflater.inflate(android.R.layout.two_line_list_item, null);
			holder.t1 = (TextView) gridView.findViewById(android.R.id.text1);
			holder.t2 = (TextView) gridView.findViewById(android.R.id.text2);
			gridView.setTag(holder);
		} else {
			holder = (ViewHolder) gridView.getTag();
		}
		holder.t1.setText(commands.get(position).getDescription());
		holder.t2.setText(commands.get(position).toString());
		if (position == highlight){
			gridView.setBackgroundColor(Color.parseColor("#35aae8"));
			settings.putValue(Settings.PREDEF_HIGHLIGHT, highlight);
		}
		return gridView;
	}
}

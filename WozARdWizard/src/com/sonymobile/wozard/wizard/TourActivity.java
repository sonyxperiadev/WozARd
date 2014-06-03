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
package com.sonymobile.wozard.wizard;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.maps.MapActivity;
import com.sonymobile.wozard.wizard.fragments.TourFragment;
import com.sonymobile.wozard.wizard.utils.Settings;
import com.sonymobile.wozard.wizard.utils.Util;

/**
 * This activity is connected to the TourFragment class. This class will be active when using portrait mode.
 */
public class TourActivity extends MapActivity implements OnItemClickListener {
	private TourFragment fragment;
	private Dialog dialog;


	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		fragment = new TourFragment();
		getFragmentManager().beginTransaction().add(android.R.id.content,
				fragment, TourFragment.TAG).commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.tour_menu_portrait, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case R.id.menu_quit:
			Util.handleQuit(this);
			return true;

		case R.id.menu_userview:
			Intent intent = new Intent();
			intent.setClass(this, UserViewActivity.class);
			startActivity(intent);
			return true;

		case R.id.menu_select_file:
			dialog = new Dialog(TourActivity.this);
			dialog.setContentView(R.layout.select_file_dialog);
			dialog.setTitle("Select tour file");
			dialog.setCancelable(true);
			ListView tourFiles = (ListView) dialog.findViewById(R.id.tourListView);
			tourFiles.setOnItemClickListener(this);
			tourFiles.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, Util.getFiles(Settings.DEFAULT_ROOT_TOUR));
			tourFiles.setAdapter(adapter);
			dialog.show();
			break;			
		case android.R.id.home:
			finish();
			return true;
		case R.id.clearButton:
		    Util.handleClearButton();
			return true;
		}
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View arg1, int arg2, long arg3) {
		String tmp = (String)parent.getAdapter().getItem(arg2);
		Settings.getInstance().putValue(Settings.TOUR_KEY, Settings.DEFAULT_ROOT_TOUR + tmp);
		fragment.updateFile();
		Util.log("Changed tour file to " + tmp, null);
		dialog.dismiss();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}

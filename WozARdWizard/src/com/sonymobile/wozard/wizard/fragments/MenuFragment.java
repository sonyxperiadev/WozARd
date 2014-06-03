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
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sonymobile.wozard.wizard.CameraViewActivity;
import com.sonymobile.wozard.wizard.ControllerActivity;
import com.sonymobile.wozard.wizard.ControllerActivity.ControllerCallback;
import com.sonymobile.wozard.wizard.FileActivity;
import com.sonymobile.wozard.wizard.NavigationActivity;
import com.sonymobile.wozard.wizard.NotificationActivity;
import com.sonymobile.wozard.wizard.PredefinedActivity;
import com.sonymobile.wozard.wizard.R;
import com.sonymobile.wozard.wizard.SettingsActivity;
import com.sonymobile.wozard.wizard.SmartWatchReceiver;
import com.sonymobile.wozard.wizard.TourActivity;
import com.sonymobile.wozard.wizard.UserViewActivity;
import com.sonymobile.wozard.wizard.utils.Util;

/**
 * The fragment that is used to display the menu.
 */
public class MenuFragment extends ListFragment implements SmartWatchReceiver, 
        ControllerCallback {
	private boolean mDualPane;

	private static final int FILEBROWSER_VIEW = 0;
	private static final int NAVIGATION_VIEW = 1;
	private static final int NOTIFICATION_VIEW = 2;
	private static final int PREDEFINED_VIEW = 3;
	private static final int TOUR_VIEW = 4;
	private static final int CAMERA_VIEW = 5;
	private static final int USER_VIEW = 6;
	private static final int SETTINGS_VIEW = 7;

	private FilebrowserFragment fileFragment;
	private NavigationFragment navigationFragment;
	private NotificationFragment notFragment;
	private CameraFragment cameraView;
	private TourFragment tourFragment;
	private UserViewFragment puppetView;
	private PredefinedFragment preFragment;
	private SettingsFragment settingsFragment;

	private Fragment currentFragment;

	public static final String TAG = "MenuFragment";

	public interface MenuCallback {
		public void onItemSelected(String id);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Resources res = getResources();

		setListAdapter(new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_activated_1,
				android.R.id.text1, res.getStringArray(R.array.menu_items)));

		View details = getActivity().findViewById(R.id.details);
		mDualPane = details != null && details.getVisibility() == View.VISIBLE;
		if (mDualPane) {
			showFileFragment();
		}

		ControllerActivity.registerCallback(this);
		Util.actionBarSetup(getActivity(), false, R.string.action_bar_default_title);
	}

	@Override
	public void onResume() {
		super.onResume();
		TextView tv = (TextView) getView().findViewById(R.id.connection_text);
		Util.updateConnectionStatusView(getActivity(), tv,
				ControllerActivity.isConnected());
	}

	@Override
	public void onListItemClick(ListView l, View v, int pos, long id) {
		l.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		l.setItemChecked(pos, true);
		showDetails(pos);
	}

	/**
	 * Function to populate the fragment with the Navigation view
	 */
	private void showNavigationView() {
		mDualPane = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		if (mDualPane) {
			if (navigationFragment == null)
				navigationFragment = new NavigationFragment();

			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.details, navigationFragment);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();
			currentFragment = navigationFragment;

		} else {
			Intent intent = new Intent();
			intent.setClass(getActivity(), NavigationActivity.class);
			startActivityForResult(intent, 0);
		}
	}

	/**
	 * Function to populate the fragment with the Puppet view
	 */
	private void showPuppetView() {
		mDualPane = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		if (mDualPane) {
			if (puppetView == null)
				puppetView = new UserViewFragment();

			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.details, puppetView, UserViewFragment.TAG);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();
			currentFragment = puppetView;
		} else {
			Intent intent = new Intent();
			intent.setClass(getActivity(), UserViewActivity.class);
			startActivityForResult(intent,0);
		}
	}

	/**
	 * Function to populate the fragment with the Camera view
	 */
	private void showCameraFragment() {
		mDualPane = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		if (mDualPane) {
			if (cameraView == null)
				cameraView = new CameraFragment();

			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.details, cameraView, CameraFragment.TAG);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();
			currentFragment = cameraView;
		} else {
			Intent intent = new Intent();
			intent.setClass(getActivity(), CameraViewActivity.class);
			startActivityForResult(intent, 0);
		}
	}

	/**
	 * Function to determine the view which should populate the framgment
	 * @param pos value to highlight the pressed item in the menu
	 */
	private void showDetails(int pos) {
		getListView().setItemChecked(pos, true);
		switch (pos) {
		case FILEBROWSER_VIEW:
			showFileFragment();
			break;
		case CAMERA_VIEW:
			showCameraFragment();
			break;
		case USER_VIEW:
			showPuppetView();
			break;
		case NAVIGATION_VIEW:
			showNavigationView();
			break;
		case NOTIFICATION_VIEW:
			showNotificationView();
			break;
		case TOUR_VIEW:
			showTourView();
			break;
		case PREDEFINED_VIEW:
			showPredef();
			break;
		case SETTINGS_VIEW:
			showSettings();
			break;
		default:
			break;
		}
	}
	/**
	 * Function to populate the fragment with the Settings view
	 */
	private void showSettings() {
		if (mDualPane = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			if (settingsFragment == null)
				settingsFragment = new SettingsFragment();
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.details, settingsFragment);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();
		} else {
			Intent intent = new Intent();
			intent.setClass(getActivity(), SettingsActivity.class);
			startActivityForResult(intent, 0);
		}
	}
	/**
	 * Function to populate the fragment with the Predefined sequences view
	 */
	private void showPredef() {
		if (mDualPane = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

			if (preFragment == null) {
				preFragment = new PredefinedFragment();
			}
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.details, preFragment, PredefinedFragment.TAG);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();
		} else {
			Intent intent = new Intent();
			intent.setClass(getActivity(), PredefinedActivity.class);
			startActivityForResult(intent, 0);
		}
	}

	/**
	 * Function to populate the fragment with the Tour view
	 */
	public void showTourView() {
		if (mDualPane = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			if (tourFragment == null)
				tourFragment = new TourFragment();

			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.details, tourFragment, TourFragment.TAG);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();
			currentFragment = tourFragment;
		} else {
			Intent intent = new Intent();
			intent.setClass(getActivity(), TourActivity.class);
			startActivityForResult(intent, 0);
		}
	}

	/**
	 * Function to populate the fragment with the Notification view
	 */
	private void showNotificationView() {
		mDualPane = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		if (mDualPane) {
			if (notFragment == null) {
				notFragment = new NotificationFragment();
			}

			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.details, notFragment);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();
			currentFragment = notFragment;
		} else {
			Intent intent = new Intent();
			intent.setClass(getActivity(), NotificationActivity.class);
			startActivityForResult(intent, 0);
		}
	}

	/**
	 * Function to populate the fragment with the Filebrowser view
	 */
	private void showFileFragment() {
		mDualPane = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		if (mDualPane) {
			if (fileFragment == null)
				fileFragment = new FilebrowserFragment();
			if (currentFragment != null && fileFragment.equals(currentFragment)){
				fileFragment.reset();
				return;
			}

			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.details, fileFragment);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();
			currentFragment = fileFragment;

		} else {
			Intent intent = new Intent();
			intent.setClass(getActivity(), FileActivity.class);
			intent.putExtra(FilebrowserFragment.ROOT_DIR, "");
			startActivityForResult(intent, 0);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		ControllerActivity.unRegisterCallback(this);
	}

	/**
	 * Called to get the current fragment
	 * @return the current fragment
	 */
	public Fragment getCurrentFragment() {
		return currentFragment;
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
	public void onConnected(boolean isConnected) {	
        TextView tv = (TextView) getView().findViewById(R.id.connection_text);
        Util.updateConnectionStatusView(getActivity(), tv, isConnected);
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

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;

import com.sonymobile.wozard.wizard.D;
import com.sonymobile.wozard.wizard.R;
import com.sonymobile.wozard.wizard.fragments.FilebrowserFragment;
import com.sonymobile.wozard.wizard.fragments.FilebrowserListFragment;
import com.sonymobile.wozard.wizard.fragments.NavigationFragment;
import com.sonymobile.wozard.wizard.fragments.PredefinedFragment;
import com.sonymobile.wozard.wizard.fragments.SettingsFragment;

/**
 * The class handling settings.
 * This class has plenty of room for refactoring.
 * Most of the functionality in this class can be replaced by overriding onSaveInstanceState in the various fragments
 */
public class Settings {

	/**
	 * Notification
	 */
	public static final String NOTIFICATION_SOUND_ENABLED = "Notification-Sound";

	/**
	 * Navigation
	 */
	public static final String NAVIGATION_SOUND_ENABLED = "Navigation-Sound";

	/**
	 * Settings
	 */
	public static final String TRANSFER_SOUND_ENABLED = "Transfer-Sound";
	public static final String TAKE_AUTO_PICTURES_ENABLED = "Take-Auto-Pictures";
	public static final String SMARTWATCH_ON_RIGHT_HAND = "Smartwatch-on-right-hand";
	public static final String CONTINUE_FORWARD = "Continue-forward";
	public static final String TURN_LEFT = "Turn-left";
	public static final String TURN_RIGHT = "Turn-right";
	public static final String TURN_AROUND = "Turn-around";
	public static final String STOP = "Stop";
	public static final String AUTO_PICTURE_INTERVALL = "Auto-picture-intervall";

	/**
	 * the key for the tour file
	 */
	public static final String TOUR_KEY = "tour_file";
	/**
	 * the key for the predefined sequence file currently in use
	 */
	public static final String PREDEF_KEY = "predef_file";
	/**
	 * used to remember if the puppet is sending pictures
	 */
	public static final String SEND_KEY = "send_pics";
	/**
	 * used to remember if the puppet is taking automatic pictures
	 */

	public static final String AUTO_KEY = "auto_cam";
	/**
	 * used to distinguish between left and right hand use of the smartwatch
	 */
	public static final String SMARTWATCHONRIGHTHAND_KEY = "smartwatch_on_right";
	/**
	 * used to save the currently highlighted item in predefined sequence
	 * this should probably be replaced by overriding onSaveInstanceState in {@link PredefinedFragment}
	 */
	public static final String PREDEF_HIGHLIGHT = "predef_highlight";
	/**
	 * used to save the current position in predefined sequence
	 * this should probably be replaced by overriding onSaveInstanceState in {@link PredefinedFragment}
	 */
	public static final String PREDEF_KEY_POS = "predef_key_position";

	public static final int RESULT_CLOSE_ALL = 1;
	/**
	 * used to get the startingdirectory in the {@link FilebrowserFragment} and {@link FilebrowserListFragment}
	 */
	public static final String ROOT_KEY = "start_dir";


	/**
	 * Used to save the state of the usage of the gps
	 */
	public static final String GPS_KEY = "gps";

	/**
	 * used to save if transparent images are used in combination with a video see through device
	 */
	public static final String TRANSPARENT_NAV_KEY = "enableTransparentNavigation";
	/**
	 * save if sound is used with notifications
	 */
	public static final String SOUND_NOTIFICATION_KEY = "enableSoundNotification";
	/**
	 * used to remember the state of the camera feed
	 */
	public static final String TRANSFER_CAMERAFEED_KEY = "transferCameraFeed";

	/**
	 * Used to save if automatic pictures should be checked or not
	 */
	public static final String AUTO_PICS_KEY = "takeAutomaticPictures";

	public static final String TRANSFER_SOUND_KEY = "transferSoundFeed";

	public static final String CAMERA_TYPE_KEY = "cameraType";

	private static final String sdcard_root = Environment.getExternalStorageDirectory().getPath();

	public static final String DEFAULT_ROOT = sdcard_root + D.WIZARD_PATH +"filebrowser/";

	public static final String DEFAULT_ROOT_NAV = sdcard_root
			+ D.WIZARD_PATH + "filebrowser/Navigation/";

	public static final String DEFAULT_CAMERAPATH = sdcard_root
			+ D.WIZARD_PATH +"filebrowser/Camera";

	public static final String DEFAULT_ROOT_NOT = sdcard_root
			+ D.WIZARD_PATH +"notifications/";

	public static final String DEFAULT_ROOT_NOTIFICATIONS = sdcard_root
			+ D.WIZARD_PATH + "notifications/";

	public static final String DEFAULT_ROOT_NOT_ICONS = sdcard_root
			+ D.WIZARD_PATH + "notifications/icons/";

	public static final String DEFAULT_ROOT_PREDEF = sdcard_root
			+ D.WIZARD_PATH + "predefs/";

	public static final String DEFAULT_ROOT_TOUR = sdcard_root
			+ D.WIZARD_PATH + "tours/";

	public static final String DEFAULT_ROOT_SETTINGS = sdcard_root
			+ D.WIZARD_PATH + "settings/";

	private static final String custom_fileName = "customized_sound_directions.txt";

	private static ArrayList<String> default_dirs;

	private static Settings instance;
	private HashMap<String, Object> settings;

	/**
	 * Used by the {@link NavigationFragment} for the directory strings
	 * @param res The application resources
	 * @return An {@link ArrayList} containing the strings
	 */
	public static ArrayList<String> getDefaultDirs(Resources res) {
		if (default_dirs == null)
			default_dirs = new ArrayList<String>(Arrays.asList(res
					.getStringArray(R.array.directions)));
		return default_dirs;
	}

	private Settings() {
		settings = new HashMap<String, Object>();

		settings.put(TOUR_KEY, (DEFAULT_ROOT_TOUR + "tour.xml"));
		settings.put(PREDEF_KEY, DEFAULT_ROOT_PREDEF);
		settings.put(PREDEF_KEY_POS, -1);
		settings.put(GPS_KEY, true);
		settings.put(TRANSPARENT_NAV_KEY, false);
		settings.put(SOUND_NOTIFICATION_KEY, false);
		settings.put(TRANSFER_CAMERAFEED_KEY, true);
		settings.put(AUTO_PICS_KEY, false);
		settings.put(SMARTWATCHONRIGHTHAND_KEY, false);
		settings.put(PREDEF_HIGHLIGHT, -1);
		settings.put(TRANSFER_SOUND_KEY, false);
		settings.put(CAMERA_TYPE_KEY, false);
	}

	/**
	 * A function to get the instance of {@link Settings}
	 * @return the settings instance
	 */
	public static Settings getInstance() {
		if (instance == null)
			instance = new Settings();
		return instance;
	}

	/**
	 * Save a value in settings
	 * @param key The string key used to save the value
	 * @param value the value to save
	 */
	public void putValue(String key, Object value) {
		settings.put(key, value);
		//TODO save to file as well
	}

	/**
	 * Returns a value from settings
	 * @param key The key to the setting
	 * @return the setting. it is up to you to know what kind of item it is.
	 */
	public Object getValue(String key) {
		return settings.get(key);
	}

	/**
	 * used to save custom strings used in the {@link NavigationFragment}
	 * @param forward The string corresponding to forward
	 * @param right	The string corresponding to right
	 * @param left	The string corresponding to left
	 * @param stop The string corresponding to stop
	 * @param turnAround The string corresponding to turn around
	 */
	public static void setCustomDir(String forward, String right,
			String left, String stop, String turnAround) {
		try {
			File customFile = new File(DEFAULT_ROOT_SETTINGS
					+ custom_fileName);
			customFile.delete();
			File customFileNew = new File(DEFAULT_ROOT_SETTINGS
					+ custom_fileName);
			customFileNew.createNewFile();
			FileOutputStream os = new FileOutputStream(customFileNew, true);
			PrintStream ps = new PrintStream(os);
			ps.print("Forward:" + forward);
			ps.println();
			ps.print("Right:" + right);
			ps.println();
			ps.print("Left:" + left);
			ps.println();
			ps.print("Stop:" + stop);
			ps.println();
			ps.print("TurnAround:" + turnAround);
			ps.close();
			os.flush();
			os.close();

		} catch (Exception e) {
			Log.d(SettingsFragment.TAG, "couldn't write to file: "
					+ DEFAULT_ROOT_SETTINGS + custom_fileName);
		}
	}
}

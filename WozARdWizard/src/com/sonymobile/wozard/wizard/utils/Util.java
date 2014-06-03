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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sonymobile.wozard.wizard.CameraService;
import com.sonymobile.wozard.wizard.ControllerActivity;
import com.sonymobile.wozard.wizard.D;
import com.sonymobile.wozard.wizard.R;
import com.sonymobile.wozard.wizard.TourService;
import com.sonymobile.wozard.wizard.fragments.UserViewFragment;
/**
 * A class containing some static utility functions
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class Util {
	private static final String logfile = "log.txt";
	private static final String TAG = "Util";
	static SharedPreferences mPrefs = null;
	private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);

	/**
	 * A function that makes sure that checks if the given folder exists
	 * otherwise it tries to create it
	 * @param dir the folder to create if it doesn't exist.
	 * @return true if exists or if it has been created, false otherwise
	 */
	public static boolean preparePath(String dir){
		File f = new File(dir);
		if(f.exists() && f.isDirectory())
			return true;
		return f.mkdirs();
	}

	/**
	 * A function to write an event to a log file
	 * The function will add a time stamp to the entry and if location is not null the location of the event
	 * @param message a string describing the event
	 * @param location the Location where it happened
	 */
	public static void log(String message, Location location) {
		try {
			File tmp = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
					+ D.WIZARD_PATH + logfile);

			BufferedWriter writer = new BufferedWriter(new FileWriter(tmp, true));
			StringBuilder msg = new StringBuilder();
			msg.append(format.format(new Date(System.currentTimeMillis())));
			msg.append(" ; ");
			if (location != null) {
				msg.append("latitude: " + location.getLatitude());
				msg.append(", longitude: " + location.getLongitude());
				msg.append(" ; ");
			}
			msg.append(message);
			msg.append('\n');
			writer.write(msg.toString());
			writer.close();
		} catch (IOException e) {

		}
	}

	public static boolean hasICS() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
	}

	/**
	 * Function to get a list of files in a directory on the SD card
	 *
	 * @param path
	 *            the string path to the directory
	 * @return an array of String filenames
	 */
	public static ArrayList<String> getFiles(String path) {
		ArrayList<String> files = new ArrayList<String>();
		File dir = new File(path);
		if (dir.listFiles() != null) {
			for (File f : dir.listFiles()) {
				if (f.isFile()) {
					files.add(f.getName());
				}
			}
		}
		return files;
	}

	/**
	 * @brief Screen receiver base implementation
	 * @param bs byte Stream array with compressed image
	 * @param activity Target activity
	 * */
	public static void screenReceived(final byte[] bs, final Activity activity) {
		Log.d(TAG, "screenReceived " + bs.length);
		new AsyncTask<Void, Void, Bitmap> (){
			@Override
			protected Bitmap doInBackground(Void... params) {
				return BitmapFactory.decodeByteArray(bs, 0, bs.length);
			}
			@Override
			protected void onPostExecute(final Bitmap bitmap) {
				super.onPostExecute(bitmap);
				activity.runOnUiThread(new Runnable() {
					public void run() {
//           NOT USED NOW FIX THIS!! TRY TO SHOW CAMERA AND OVERLAY IMAGE OF PUPPET DEVICE
//						View actionView = activity.getActionBar().getCustomView();
//						if (actionView != null) {
//					        ImageView imageView = (ImageView)actionView.findViewById(
//							    R.id.actionbar_image_view);
//					        if (imageView != null) {
//					        	if (bitmap != null) {
//					        		imageView.setImageBitmap(bitmap);
//					        	} else {
//									imageView.setImageResource(R.drawable.blank);
//					        	}
//					        }
//						} else {
//							Log.w(TAG, "Action bar custom view not found");
//						}
//						Fragment f = activity.getFragmentManager().findFragmentByTag(
//								UserViewFragment.TAG);
//						if (f!= null) {
//							UserViewFragment uf = (UserViewFragment) f;
//							if (bitmap != null) {
//								uf.setScreen(bitmap);	
//							} else {
//								Bitmap bm = BitmapFactory.decodeResource(
//										activity.getResources(), R.drawable.blank);
//								uf.setScreen(bm);
//								Log.w(TAG, "PuppetFragment not found");
//                            }
//						}
					}
				});
			}
		}.execute();
	}

	/**
	 * @brief Setup action bar
	 * @param activity Target activity
	 * @param home True if home
	 * */
	public static void actionBarSetup(final Activity activity, final boolean home, final int titleText) {
		if (Util.hasICS()) {
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					activity.getActionBar().setHomeButtonEnabled(!home);
					activity.getActionBar().setDisplayHomeAsUpEnabled(!home);
					activity.getActionBar().setDisplayShowCustomEnabled(true);
					activity.getActionBar().setCustomView(R.layout.actionbar_view);
					View actionView = activity.getActionBar().getCustomView();
					ImageView imageView = (ImageView)actionView.findViewById(
							R.id.actionbar_image_view);
				    if (imageView != null) {
                        imageView.setImageResource(R.drawable.blank);
				    }
					TextView tv = (TextView) actionView.findViewById(R.id.titleText);
					tv.setText(titleText);
				}
			});
		}
	}

	/**
	 * @brief Show a preview image of what is shown on the user side
	 * @param activity Target activity
	 * @param path
	 * */
	public static void previewImage(final Activity activity, final Bitmap bitmap) {
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
			    public void run() {
				    View actionView = activity.getActionBar().getCustomView();
					if (actionView != null) {
					    ImageView imageView = (ImageView)actionView.findViewById(
                                R.id.actionbar_image_view);
	                    if (imageView != null) {
                            if (bitmap != null) {
                                imageView.setImageBitmap(bitmap);
                            } else {
                                imageView.setImageResource(R.drawable.blank);
                            }
                        }
                    } else {
                        Log.w(TAG, "Action bar custom view not found");
                    }
                    Fragment f = activity.getFragmentManager().findFragmentByTag(
                            UserViewFragment.TAG);
                    if (f!= null) {
                        UserViewFragment uf = (UserViewFragment) f;
                        if (bitmap != null) {
                            uf.setScreen(bitmap);
                        } else {
                            Bitmap bm = BitmapFactory.decodeResource(
                                    activity.getResources(), R.drawable.blank);
	                        uf.setScreen(bm);
                            Log.w(TAG, "PuppetFragment not found");
                        }
                    }
			    }
			});
		}
	}

	/**
	 * @brief Show a preview image of what is shown on the user side
	 * @param activity Target activity
	 * @param path
	 * */
	public static void updatePreviewImage(final Activity activity) {
		if (activity != null) {
			activity.runOnUiThread(new Runnable() {
			    public void run() {
                    Bitmap bitmap = ControllerActivity.getPreviewImage();
				    View actionView = activity.getActionBar().getCustomView();
					if (actionView != null) {
                        ImageView imageView = (ImageView)actionView.findViewById(
                            R.id.actionbar_image_view);
                        if (imageView != null) {
                            if (bitmap != null) {
                                imageView.setImageBitmap(bitmap);
                            } else {
                                imageView.setImageResource(R.drawable.blank);
                            }
                        }
                    } else {
                        Log.w(TAG, "Action bar custom view not found");
                    }
                    Fragment f = activity.getFragmentManager().findFragmentByTag(
                            UserViewFragment.TAG);
                    if (f!= null) {
                        UserViewFragment uf = (UserViewFragment) f;
                        if (bitmap != null) {
                            uf.setScreen(bitmap);
                        } else {
                            Bitmap bm = BitmapFactory.decodeResource(
                                activity.getResources(), R.drawable.blank);
                            uf.setScreen(bm);
                            Log.w(TAG, "PuppetFragment not found");
                        }
                    }
			    }
			});
		}
    }

	/**
	 * @brief Handle quit operations
	 * @param activity Target activity
	 * */
	public static void handleQuit(final Activity activity) {
		if (activity != null) {
			activity.setResult(Settings.RESULT_CLOSE_ALL);
			activity.finish();
			ControllerActivity.stopConnection();
			Intent stopIntent = new Intent(activity, TourService.class);
			activity.stopService(stopIntent);
			Intent stopCamera = new Intent(activity, CameraService.class);
			activity.stopService(stopCamera);
			activity.moveTaskToBack(true);
		}
	}

	public static void handleClearButton() {
		ControllerActivity.sendCommandToPuppet(ControllerActivity.CLEAR_INDICATOR_CMD,
				null, null, null);
	}

	public static void updateConnectionStatusView(final Activity activity, final TextView connectedStatusView, boolean isConnected) {
		if (activity != null && connectedStatusView != null) {
			if (isConnected) {
			    activity.runOnUiThread(new Runnable() {
				    public void run() {
					    if (connectedStatusView != null) {
			                connectedStatusView.setBackgroundColor(Color.GREEN);
			                connectedStatusView.setText(R.string.connected_text);
					    }
				    }
			    });
		    } else {
			    activity.runOnUiThread(new Runnable() {
				    public void run() {
					    if (connectedStatusView != null) {
			                connectedStatusView.setBackgroundColor(Color.RED);
			                connectedStatusView.setText(R.string.not_connected_text);
                        }
				    }
			    });
		    }
		}
	}

	/**
	 * Creates a bitmap from a file.
	 * @param activity
	 * @param filePath
	 * @return
	 */
	public static Bitmap createBitmapFromFile(final Activity activity, final String filePath) {
		int scaleWidth = 150;
		int scaleHeight = 150;
		Bitmap bm = BitmapFactory.decodeFile(filePath);

		if (bm == null) {
			bm = BitmapFactory.decodeResource(activity.getResources(), R.drawable.image_not_found);
		}
		return Bitmap.createScaledBitmap(bm, scaleWidth, scaleHeight, true);
	}
}

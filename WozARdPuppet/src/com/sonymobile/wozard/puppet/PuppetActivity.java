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
package com.sonymobile.wozard.puppet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.sonymobile.wozard.puppet.NetworkService.NetworkCallback;
import com.sonymobile.wozard.puppet.util.Util;

@TargetApi(9)
public class PuppetActivity extends Activity implements ServiceConnection,
		NetworkCallback, LocationListener, OnInitListener, PreviewCallback {
	private static final String INSERT_NEW_IP_NUMBER = "Insert new ip number";
	private static final String DISCONNECTED_FROM_NETWORK_SERVICE = "Disconnected from network service";
	private static final String CONNECTED_TO_NETWORK_SERVICE = "Connected to network service";
	private static final String SHOW_IMAGE_CMD = "SHOW_IMAGE";
	private static final String SHOW_MAP_CMD = "SHOW_MAP";
	private static final String COMMAND_ID = "COMMAND_ID";
	private static final String SHOW_SMALL_NOTIFICATION_CMD = "SHOW_SMALL_NOTIFICATION";
	private static final String SHOW_SMALL_INFO_NOTIFICATION_CMD = "SHOW_SMALL_INFO_NOTIFICATION";
	private static final String SHOW_INFO_NOTIFICATION_CMD = "SHOW_INFO_NOTIFICATION";
	private static final String SHOW_BIG_NOTIFICATION_CMD = "SHOW_BIG_NOTIFICATION";
	private static final String SHOW_NEXT_SMS_CMD = "SHOW_NEXT_SMS";
	private static final String SHOW_PREVIOUS_SMS_CMD = "SHOW_PREVIOUS_SMS";
	private static final String PLAY_SOUND_CMD = "PLAY_SOUND";
	private static final String PLAY_VIDEO_CMD = "PLAY_VIDEO";
	private static final String REQ_COORDINATES_CMD = "REQ_COORDINATES";
	private static final String PLAY_VIBRATION_CMD = "PLAY_VIBRATION";
	private static final String GET_SCREEN_CMD = "GET_SCREEN";
	private static final String CAMERA_CMD = "START_CAMERA";
	private static final String TAKE_PICTURE_CMD = "TAKE_PICTURE";
	private static final String AUTO_PICS_ON = "AUTO_ON";
	private static final String AUTO_PICS_OFF = "AUTO_OFF";
	private static final String SEND_CAMERA_CMD = "SEND_CAMERA";
	private static final String SEND_CAMERA_OFF_CMD = "STOP_SEND_CAMERA";
	private static final String SEND_SOUND_CMD = "SEND_SOUND";
	private static final String SEND_SOUND_OFF_CMD = "STOP_SEND_SOUND";
	private static final String PUPPET_VOICECOMMAND_CMD = "PUPPET_VOICECOMMAND";
	private static final String REQ_VOICECOMMAND_CMD = "REQ_VOICECOMMAND";
	private static final String PRESS_EVENT_CMD = "PRESS_EVENT";
	private static final String SHOW_XY_COORDINATES_CMD = "SHOW_XY_COORDINATES";
	private static final String CLEAR_INDICATOR_CMD = "CLEAR_INDICATOR";
	private static final String LOAD_CMD = "LOAD";
	private static final String VIDEO_SEE_THROUGH_DEVICE = "VIDEO_SEE_THROUGH_DEVICE";
	private static boolean saveNext = false;
	private AutoCompleteTextView autoCompleteTextView;
	private Dialog dialog;
	private Mailbox box;
	private long lastPic = System.currentTimeMillis();

	private NetworkService mService;

	private RelativeLayout rootLayout;

	private static final String TAG = "PlayerActivity";
	private static final String VIDEO_SEE_THROUGH_ENABLED = "Video-see-through-enabled";

	private UDPThread thread;
	private SoundRecordUDPThread soundThread;

	private Camera camera;
	private CameraView mPreview;
	private boolean takePictures = false;
	private boolean sendPictures = false;
	private boolean sendCoords = false;
	private MediaPlayer mAudioPlayer;
	private int picInterval;
	private long lastTransmit = 0;

	private LocationManager locationManager;

	private TextToSpeech tts;
	private boolean showingSMS;
	private ArrayList<String> smsList;
	private int smsIndex;
	private boolean mVideoSeeThroughEnabled = false;
	private ImageView myTransparentImageView;
	private ImageView myImageView;
	private ImageView mScreenshotView;
	private ImageView mInfoNotificationImage;
	private ImageView mBigNotIcon;
	private ImageView mSMS_Icon;
	private ImageView mProductFoundIndicator;
	private ImageView mSmallNot;
	private TextView mSmsMsg;
	private TextView mPreSmsMsg;
	private TextView mNextSmsMsg;
	private TextView mNotMsg;
	RadioButton mOpticalButton;
	RadioButton mVideoButton;
	private SpeechRecognizer sr;

	private Animation blinkAnimation;
	private Animation fromRightAnimation;
	private Animation fadeInTopAnimation;
	private Animation fadeInBottomAnimation;
	private Animation fromMidToBottomAnimation;
	private Animation fromMidToTopAnimation;
	private Animation fromTopAnimation;
	private Animation fromBottomAnimation;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// make sure that the app is run in fullscreen and that it doesn't shut
		// down the screen
		this.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN
						| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_FULLSCREEN
						| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.main);

		rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);
		mAudioPlayer = new MediaPlayer();

		boolean f = bindService(new Intent(this, NetworkService.class), this,
				Context.BIND_AUTO_CREATE);
		if (!f)
			Log.d("Puppet", "Failed to bind service");

		/*
		 * If the puppet device has gps service then we request updates from it
		 */
		if (hasGps()) {
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 1000L, 500.0f, this);
		}

		blinkAnimation = AnimationUtils.loadAnimation(this,
				R.anim.blink_animation);
		fromRightAnimation = AnimationUtils.loadAnimation(this,
				R.anim.fromright_animation);

		tts = new TextToSpeech(this, this);

		// fill the list of dummy sms
		fillSms();

		// the mailbox that will be used by the udp thread
		box = new Mailbox();

		sr = SpeechRecognizer.createSpeechRecognizer(this);
		sr.setRecognitionListener(new listener());
		picInterval = 15;
		startBackgroundCamera();

		// Video see through device or not
		mVideoSeeThroughEnabled = getVideoSeeThroughEnabled();
		if (mVideoSeeThroughEnabled) {
            shiftToPhotoView(CameraInfo.CAMERA_FACING_BACK);
        }
	}

	/**
	 * Used to fill the sms list with some dummy items
	 */
	private void fillSms() {
		smsList = new ArrayList<String>();
		String[] arr = getResources().getStringArray(R.array.dummySMS);
		for (int i = 0; i < arr.length; i++) {
			smsList.add(arr[i]);
		}
	}

	/**
	 * Used to remove whatever is showing on the screen
	 */
	private void emptyScreen() {
		if (mVideoSeeThroughEnabled) {
			// Remove only transparent views if the exist
			if (myTransparentImageView != null) {
				myTransparentImageView.setImageResource(android.R.color.transparent);
			}
			if (mInfoNotificationImage != null) {
				mInfoNotificationImage.setImageResource(android.R.color.transparent);
			}
			if (mBigNotIcon != null) {
				mBigNotIcon.setImageResource(android.R.color.transparent);
			}
			if (mSmsMsg != null) {
				mSmsMsg.setVisibility(View.INVISIBLE);
			}
			if (mPreSmsMsg != null) {
				mPreSmsMsg.setVisibility(View.INVISIBLE);
			}
			if (mNextSmsMsg != null) {
				mNextSmsMsg.setVisibility(View.INVISIBLE);
			}
			if (mNotMsg != null) {
                mNotMsg.setVisibility(View.INVISIBLE);
		    }
			if (mSMS_Icon != null) {
				mSMS_Icon.setImageResource(android.R.color.transparent);
			}
			if (mSmallNot != null) {
				mSmallNot.setVisibility(View.INVISIBLE);
			}
			if (mProductFoundIndicator != null) {
				mProductFoundIndicator.setImageResource(android.R.color.transparent);
			}
			if (myImageView != null) {
				myImageView.setImageResource(android.R.color.transparent);
			}
			if (mScreenshotView != null) {
				mScreenshotView.setImageResource(android.R.color.transparent);
			}
		} else {
		    rootLayout.removeAllViews();
		}
	}

	/**
	 * A function that starts the camera in the background without showing on
	 * the puppet screen
	 */
	private void startBackgroundCamera() {
		startCamera(CameraInfo.CAMERA_FACING_BACK);
		FrameLayout frame = (FrameLayout) findViewById(R.id.camera_pixel);
		frame.removeAllViews();

		frame.addView(mPreview);

		if (mService != null)
			mService.sendInfo("The puppet has started the camera in the background");
		if (sendPictures) {
			startUDPThread();
		}
	}

	/**
	 * stopBackgroundCamera stops the camera, if it is running in the pixel it
	 * is removed from there.
	 */
	private void stopBackgroundCamera() {
		stopCamera();
		FrameLayout frame = (FrameLayout) findViewById(R.id.camera_pixel);
		frame.removeAllViews();
		if (mService != null)
			mService.sendInfo("The background camera has been stoppen on the puppet");
	}

	/**
	 * used to start the thread responsible for sending the camera feed to the
	 * wizard device
	 */
	private void startUDPThread() {
		if (thread == null) {
			thread = new UDPThread(box, NetworkService.getIp());
			thread.start();
			if (mService != null)
				mService.sendInfo("A new UDPThread has been started on the puppet device");
		}
	}

	/**
	 * used to stop the thread responsible for sending the camera feed to the
	 * wizard device
	 */
	private void stopUDPThread() {
		if (thread == null)
			return;
		thread.killThread();
		thread = null;
		if (mService != null)
			mService.sendInfo("The UDPThread on the puppet has been stopped");
	}

	private void startSoundUDPThread() {
		if (soundThread == null) {
			soundThread = new SoundRecordUDPThread(NetworkService.getIp());
			soundThread.start();
			if (mService != null)
				mService.sendInfo("A new sound UDPThread has been started on the puppet device");
		}

	}

	private void stopSoundUDPThread() {
		if (soundThread == null)
			return;
		soundThread.killThread();
		soundThread = null;
		if (mService != null)
			mService.sendInfo("The UDPThread on the puppet has been stopped");
	}


	/**
	 * Used to show the camera on the puppet screen
	 */
	private void shiftToPhotoView(int camera) {
		emptyScreen();
		startCamera(CameraInfo.CAMERA_FACING_BACK);
		Log.d("player", "start camera");
		RelativeLayout v = (RelativeLayout) View.inflate(this,
				R.layout.photo_view, null);

		v.addView(mPreview);
		rootLayout.addView(v);
	}

	/**
	 * a function that displays an image sent in the form of a bytearray
	 *
	 * @param img
	 *            the bytearray containing the image
	 */
	private void displayScreenshot(byte[] img) {
		emptyScreen();

		View v = View.inflate(this, R.layout.map_view, null);
		rootLayout.addView(v, new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		Bitmap b = BitmapFactory.decodeByteArray(img, 0, img.length);
		mScreenshotView = (ImageView) findViewById(R.id.mapview);
		mScreenshotView.setImageBitmap(b);

	}

	/**
	 * this function is part of the networkcallback. call this with a string to
	 * be spoken out by the application
	 *
	 * @param speech
	 *            the string to be spoken
	 */
	public void speechReceived(String speech) {
		playSoundCommand(speech);
	}

	/**
	 * used when the camera is showing to paint an image over it
	 *
	 * @param fileName
	 *            the transparent image to display over the camera
	 */
	protected void shiftToTransparentImageView(String fileName) {
		View v = View.inflate(this, R.layout.image_view, null);
		rootLayout.addView(v, new RelativeLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		emptyScreen();

		Bitmap bitmap = BitmapFactory.decodeFile(fileName);

		if (bitmap == null && mService != null) {
			mService.sendRequest(fileName);
		}

		myTransparentImageView = (ImageView) findViewById(R.id.imageview);
		myTransparentImageView.setImageBitmap(bitmap);

	}

	/**
	 * used to display an image on the puppet device. If the file does not exist
	 * the puppet will request it from the wizard
	 *
	 * @param fileName
	 *            the name of the file to show
	 */
	private void shiftToImageView(String fileName) {
		emptyScreen();

		View v = View.inflate(this, R.layout.image_view, null);
		rootLayout.addView(v, new RelativeLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		Bitmap bitmap = BitmapFactory.decodeFile(fileName);

		if (bitmap == null && mService != null) {
			mService.sendRequest(fileName);
			File tmp = new File(fileName);
			Util.preparePath(tmp.getParent());
		}
		myImageView = (ImageView) findViewById(R.id.imageview);
		myImageView.setImageBitmap(bitmap);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopCamera();
		unbindService(this);
		mService = null;
		stopUDPThread();
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}

		mAudioPlayer.release();
	}

	/**
	 * used to initialize the camera and set the previewcallback
	 */
	private void startCamera(int cameraId) {
        CameraInfo cameraInfo = new CameraInfo();
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == cameraId) {
                cameraId = i;
                break;
            }
        }
        if (camera != null) {
            stopCamera();
            camera = null;
        }
	    if (camera == null)
			camera = getCamera(cameraId);
		if (camera == null)
			return;
		camera.setPreviewCallback(this);
		Parameters param = camera.getParameters();
		try {
			List<Integer> formats = param.getSupportedPreviewFormats();
			if (formats.contains(ImageFormat.JPEG))
				param.setPreviewFormat(ImageFormat.JPEG);
			else if (formats.contains(ImageFormat.NV21))
				param.setPreviewFormat(ImageFormat.NV21);
			camera.setParameters(param);
		} catch (NullPointerException e) {

		}
        mPreview = new CameraView(this, camera);
		if (mService != null)
			mService.sendInfo("The camera has started on the puppet device");
	}

	@Override
	public void onPause() {
		super.onPause();
		stopCamera();
	}

	/**
	 * used to stop and release the camera, it also unregisters the
	 * previewcallback
	 */
	private void stopCamera() {
		if (camera != null) {

			camera.setPreviewCallback(null);
			camera.release();
			camera = null;
			if (mService != null)
				mService.sendInfo("The camera has been released on the puppet device");
		}
	}

	/**
	 * a helper method to display a toast message. the duration is set to short
	 *
	 * @param msg
	 *            the string to display in the toast
	 */
	public void showToast(String msg) {
		Context context = getApplicationContext();
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	/**
	 * helper function to handle commands related to notifications (mainly sms)
	 *
	 * @param cmd
	 *            the command from the wizard
	 */
	private void handleSMS(String cmd) {
		String fileName = extractFileName(cmd);
		if (cmd.startsWith(SHOW_PREVIOUS_SMS_CMD)) {
			if (showingSMS) {
				if (smsIndex != 0) {
					smsIndex--;
					showSMS(smsList.get(smsIndex), false);
				}
			}
		} else if (cmd.startsWith(SHOW_NEXT_SMS_CMD)) {
			if (showingSMS) {
				if (smsIndex != smsList.size() - 1) {
					smsIndex++;
					showSMS(smsList.get(smsIndex), true);
				}
			}
		} else if (cmd.startsWith(SHOW_INFO_NOTIFICATION_CMD)
					&& fileName != null) {
				int index = fileName.indexOf("#");
				int index2 = fileName.indexOf("¤");
				String type = fileName.substring(0, index);
				String iconPath = "";
				if (index < fileName.length() - 1) {
					iconPath = fileName.substring(index + 1, index2);
				}
				String msg = "";
				if (index2 < fileName.length() - 1) {
					msg = fileName.substring(index2 + 1, fileName.length());
				}

			    if (type.startsWith("Info")) {
				    showInfoNotificaiton(type, msg, iconPath);
			    }
		} else if (cmd.startsWith(SHOW_BIG_NOTIFICATION_CMD)
				&& fileName != null) {
			int index = fileName.indexOf("#");
			String type = fileName.substring(0, index);
			String msg = "";
			boolean hasMsg = true;
			if (index < fileName.length() - 1) {
				msg = fileName.substring(index + 1, fileName.length());
			} else {
				hasMsg = false;
			}

			if (type.startsWith("SMS")) {
				if (!showingSMS) {
					if (hasMsg)
						smsList.add(msg);
					smsIndex = smsList.size() - 1;
					showingSMS = true;
					showSMS();
				}
			} else {
				showingSMS = false;
				showNotification(type, msg);
			}
		} else if (cmd.startsWith(PRESS_EVENT_CMD)) {
		    takePicture();
		}
	}

	@Override
	public void commandReceived(final String cmd) {
		runOnUiThread(new Runnable() {
			public void run() {
				String command = extractAndReSendCommandId(cmd);
				if (command.startsWith(SHOW_PREVIOUS_SMS_CMD)
						|| command.startsWith(SHOW_NEXT_SMS_CMD)
						|| command.startsWith(SHOW_BIG_NOTIFICATION_CMD)
						|| command.startsWith(SHOW_INFO_NOTIFICATION_CMD)
						|| command.startsWith(PRESS_EVENT_CMD)) {
					handleSMS(command);
					getScreen();
				} else if (command.startsWith(CLEAR_INDICATOR_CMD)) {
					emptyScreen();
				} else if (command.startsWith(GET_SCREEN_CMD)) {
					getScreen();
				} else {
					String fileName = extractFileName(command);
					showingSMS = false;
					if (command.startsWith(SHOW_IMAGE_CMD) && fileName != null) {
						if (mVideoSeeThroughEnabled) {
							shiftToTransparentImageView(fileName);
						} else {
							shiftToImageView(fileName);
						}
						getScreen();
						if (mService != null) {
							mService.sendInfo(command);
						}
					} else if (command.startsWith(PLAY_SOUND_CMD)
							&& fileName != null) {
						playSound(fileName);
					} else if (command.startsWith(PLAY_VIDEO_CMD)
							&& fileName != null) {
						shiftToVideoView(fileName);
					} else if (command.startsWith(SHOW_MAP_CMD) && fileName != null) {
						shiftToImageView(fileName);
					} else if (command.startsWith(SHOW_SMALL_INFO_NOTIFICATION_CMD)) {
						smallInfoNotification(command);
					} else if (command.startsWith(SHOW_SMALL_NOTIFICATION_CMD)) {
						showSmallNotification(command.substring(
								command.indexOf(" ") + 1, command.length()).trim());
						getScreen();
					} else if (command.startsWith(PLAY_VIBRATION_CMD)) {
						playVibration();
					} else if (command.startsWith(AUTO_PICS_OFF)) {
						takePictures = false;
						mService.sendInfo("The puppet has stopped taking pictures automatically");
					} else if (command.startsWith(AUTO_PICS_ON)) {
						picInterval = Integer.parseInt(command.substring(
								command.indexOf(" ") + 1, command.length()));
						mService.sendInfo("The puppet has started taking pictures automatically. Interval: "
								+ picInterval + " s.");
						takePictures = true;
						if (!mVideoSeeThroughEnabled)
							startBackgroundCamera();
					} else if (command.startsWith(SEND_CAMERA_CMD)) {
						sendPictures = true;
						if (!mVideoSeeThroughEnabled)
							startBackgroundCamera();
						startUDPThread();
					} else if (command.startsWith(SEND_CAMERA_OFF_CMD)) {
						if (!takePictures && !mVideoSeeThroughEnabled) {
							stopBackgroundCamera();
						}
					} else if (command.startsWith(SEND_SOUND_CMD)) {
						startSoundUDPThread();
					} else if (command.startsWith(SEND_SOUND_OFF_CMD)) {
						stopSoundUDPThread();
					} else if (command.startsWith(CAMERA_CMD)) {
						if (!mVideoSeeThroughEnabled) {
                            shiftToPhotoView(0);
						}
					} else if (command.startsWith(TAKE_PICTURE_CMD)) {
						takePicture();
					} else if (command.startsWith(REQ_COORDINATES_CMD)) {
						if (hasGps()) {
							sendCoords = true;
							startGps();
						} else {
							if (mService != null)
								mService.sendInfo("The puppet device has no GPS");
						}
					} else if (command.startsWith(REQ_VOICECOMMAND_CMD)) {
						startVoiceCommand();
					} else if (command.startsWith(SHOW_XY_COORDINATES_CMD)) {
						drawIndicator(command);
					} else if (command.startsWith(LOAD_CMD)) {
						shiftToLoadingView("Loading map...");
					} else if (command.startsWith(VIDEO_SEE_THROUGH_DEVICE)) {
						sendVideoSeeThroughDevice();
					}
				}
			}
		});
	}

	/**
	 * draws an indicator on top of the camera feed or a blank screen. The
	 * indicator coordinates are provided by the wizard.
	 *
	 * @param cmd
	 *            the command
	 */
	private void drawIndicator(String cmd) {
		int blank = cmd.indexOf(" ");
		int separator = cmd.indexOf(";");
		String xString = cmd.substring(blank + 1, separator);
		String yString = cmd.substring(separator + 1, cmd.length());
		float x = Float.parseFloat(xString);
		float y = Float.parseFloat(yString);
		if (sendPictures) {
			if (mVideoSeeThroughEnabled) {
				if (rootLayout.getChildCount() > 1) {
					rootLayout.removeViewAt(1);
				}
			} else {
				if (rootLayout.getChildCount() > 0) {
					rootLayout.removeViewAt(0);
				}
			}

			mProductFoundIndicator = new ImageView(PuppetActivity.this);
			mProductFoundIndicator.setImageResource(R.drawable.product_found_indicator);
			RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(
					400, 400);
			params1.leftMargin = (int) ((rootLayout.getWidth() * x - 200));
			params1.topMargin = (int) ((rootLayout.getHeight() * y - 200));
			Animation animation1 = AnimationUtils.loadAnimation(
					PuppetActivity.this, R.anim.productfound_animation);
			mProductFoundIndicator.setAnimation(animation1);
			rootLayout.addView(mProductFoundIndicator, params1);
		}

	}

	/**
	 * a function that handles the animations related to sms notifications
	 */
	protected void showSMS() {

		fromRightAnimation.reset();
		fadeInTopAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		fadeInTopAnimation.setStartOffset(600);
		fadeInBottomAnimation = AnimationUtils.loadAnimation(this,
				R.anim.fade_in);
		fadeInBottomAnimation.setStartOffset(600);

		emptyScreen();
		View v = View.inflate(this, R.layout.sms_notification, null);
		rootLayout.addView(v, new RelativeLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mSmsMsg = (TextView) findViewById(R.id.smsMsg);
				mSmsMsg.setText(smsList.get(smsIndex));
				mSmsMsg.setVisibility(View.VISIBLE);
				mPreSmsMsg = (TextView) findViewById(R.id.preSMSMsg);
				mPreSmsMsg.setVisibility(View.VISIBLE);
				mNextSmsMsg = (TextView) findViewById(R.id.nextSMSMsg);
				mNextSmsMsg.setVisibility(View.VISIBLE);
				mNextSmsMsg.setText("No newer message..");
				mSMS_Icon = (ImageView) findViewById(R.id.smsIcon);
				mSMS_Icon.setImageResource(R.drawable.ic_sms);

				LinearLayout smsLayout = (LinearLayout) findViewById(R.id.smsLayout);
				smsLayout.setVisibility(View.VISIBLE);
				smsLayout.startAnimation(fromRightAnimation);

				LinearLayout preLayout = (LinearLayout) findViewById(R.id.preSMSLayout);
				if (smsIndex != 0) {
					String[] lines = smsList.get(smsIndex - 1).split("\\n");
					mPreSmsMsg.setText(lines[0]);
				} else {
					mPreSmsMsg.setText("No older message..");
				}

				preLayout.setVisibility(View.VISIBLE);
				preLayout.setAnimation(fadeInTopAnimation);

				LinearLayout nextSmsLayout = (LinearLayout) findViewById(R.id.nextSMSLayout);
				nextSmsLayout.setVisibility(View.VISIBLE);
				nextSmsLayout.setAnimation(fadeInBottomAnimation);

			}
		});

	}

	/**
	 * a function that is used to scroll between sms messages.
	 *
	 * @param msg
	 *            the message to be displayed
	 * @param next
	 *            true if next message, false if previous message
	 */
	protected void showSMS(final String msg, final boolean next) {
		fromMidToBottomAnimation = AnimationUtils.loadAnimation(this,
				R.anim.mid_to_bottom_animation);
		fromMidToBottomAnimation.setStartOffset(20);
		fromMidToTopAnimation = AnimationUtils.loadAnimation(this,
				R.anim.mid_to_top_animation);
		fromTopAnimation = AnimationUtils.loadAnimation(this,
				R.anim.top_to_mid_animation);
		fromBottomAnimation = AnimationUtils.loadAnimation(this,
				R.anim.bottom_to_mid_animation);
		fadeInBottomAnimation = AnimationUtils.loadAnimation(this,
				R.anim.fade_in);
		fadeInTopAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		fadeInTopAnimation.setStartOffset(20);

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mSmsMsg = (TextView) findViewById(R.id.smsMsg);
				mSmsMsg.setText(smsList.get(smsIndex));
				mSmsMsg.setVisibility(View.VISIBLE);
				mPreSmsMsg = (TextView) findViewById(R.id.preSMSMsg);
				mPreSmsMsg.setVisibility(View.VISIBLE);
				mNextSmsMsg = (TextView) findViewById(R.id.nextSMSMsg);
				mNextSmsMsg.setVisibility(View.VISIBLE);
				mSMS_Icon = (ImageView) findViewById(R.id.smsIcon);
				mSMS_Icon.setImageResource(R.drawable.ic_sms);

				LinearLayout smsLayout = (LinearLayout) findViewById(R.id.smsLayout);
				smsLayout.setVisibility(View.VISIBLE);
				LinearLayout preLayout = (LinearLayout) findViewById(R.id.preSMSLayout);
				LinearLayout nextSmsLayout = (LinearLayout) findViewById(R.id.nextSMSLayout);

				if (next) {
					smsLayout.startAnimation(fromBottomAnimation);
					String[] lines = smsList.get(smsIndex - 1).split("\\n");
					mPreSmsMsg.setText(lines[0]);
					if (smsIndex != smsList.size() - 1) {
						lines = smsList.get(smsIndex + 1).split("\\n");
						mNextSmsMsg.setText(lines[0]);
					} else {
						mNextSmsMsg.setText("No newer message..");
					}
					preLayout.setVisibility(View.VISIBLE);
					preLayout.setAnimation(fromMidToTopAnimation);

					nextSmsLayout.setVisibility(View.VISIBLE);
					nextSmsLayout.setAnimation(fadeInBottomAnimation);

				} else {
					String[] lines = smsList.get(smsIndex + 1).split("\\n");
					mNextSmsMsg.setText(lines[0]);
					smsLayout.startAnimation(fromTopAnimation);
					if (smsIndex != 0) {
						lines = smsList.get(smsIndex - 1).split("\\n");
						mPreSmsMsg.setText(lines[0]);
					} else {
						mPreSmsMsg.setText("No older message..");
					}
					preLayout.setVisibility(View.VISIBLE);
					preLayout.setAnimation(fadeInTopAnimation);

					nextSmsLayout.setVisibility(View.VISIBLE);
					nextSmsLayout.setAnimation(fromMidToBottomAnimation);
				}
			}
		});
	}

	/**
	 * 
	 */
	protected void showInfoNotificaiton(final String notType, final String msg, final String iconPath) {
		fromRightAnimation = AnimationUtils.loadAnimation(this,
				R.anim.fromright_animation);

        emptyScreen();

		View v = View.inflate(this, R.layout.info_notification, null);
		rootLayout.addView(v, new RelativeLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mInfoNotificationImage = (ImageView) findViewById(R.id.infoNotificationImage);
				if (notType.equals("Info")) {
					Bitmap bm = getInfoNotificationBitmap(iconPath);
					mInfoNotificationImage.setImageBitmap(bm);
				}
				LinearLayout layout = (LinearLayout) findViewById(R.id.infoNotificationLayout);
				layout.setAnimation(fromRightAnimation);
			}
		});
	}

	protected Bitmap getInfoNotificationBitmap(final String iconPath) {
	    Bitmap bitmap = null;
	    final String sdcard_root = Environment.getExternalStorageDirectory().getPath();
	    final String DEFAULT_ROOT_NOTIFICATIONS = sdcard_root
				+ "/Content/puppet_res/" + "notifications/";
	    String imgFilePath = DEFAULT_ROOT_NOTIFICATIONS + iconPath;
	    bitmap = BitmapFactory.decodeFile(imgFilePath);
	    return bitmap;
	}

	/**
	 * Used to show all notifications except sms
	 *
	 * @param notType
	 *            the type of the notification
	 * @param msg
	 *            the message to display
	 */
	protected void showNotification(final String notType, final String msg) {
		fromRightAnimation = AnimationUtils.loadAnimation(this,
				R.anim.fromright_animation);

		emptyScreen();

		View v = View.inflate(this, R.layout.big_notification, null);
		rootLayout.addView(v, new RelativeLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mBigNotIcon = (ImageView) findViewById(R.id.notificationIcon);
				if (notType.equals("SMS")) {
					mBigNotIcon.setImageResource(R.drawable.ic_sms);
				} else if (notType.equals("Calendar")) {
					mBigNotIcon.setImageResource(R.drawable.ic_calendar);
				} else if (notType.equals("Mail")) {
					mBigNotIcon.setImageResource(R.drawable.ic_email);
				}

				mNotMsg = (TextView) findViewById(R.id.notificationMsg);
				mNotMsg.setText(msg);
			    mNotMsg.setVisibility(View.VISIBLE);

				LinearLayout notificationlayout = (LinearLayout) findViewById(R.id.notificationLayout);
				notificationlayout.setAnimation(fromRightAnimation);
			}
		});
	}

	/**
	 * a function that shows a loading screen on the puppet screen
	 *
	 * @param msg
	 *            the text to be displayed below the loading bar
	 */
	private void shiftToLoadingView(final String msg) {

		emptyScreen();
		View v = View.inflate(this, R.layout.loading_map_view, null);
		rootLayout.addView(v, new RelativeLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				TextView text = (TextView) findViewById(R.id.progressText);
				text.setText(msg);
			}
		});
	}

	/**
	 * displays a microphone and a message to indicate that the application is
	 * ready for voice input
	 *
	 * @param msg
	 *            the text to display below the microphone
	 */
	private void shiftReadyForVoiceView(final String msg) {
		emptyScreen();
		View v = View.inflate(this, R.layout.voice_command_layout, null);
		rootLayout.addView(v, new RelativeLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				TextView text = (TextView) findViewById(R.id.voiceText);
				text.setText(msg);
			}
		});
	}

	/**
	 * This function displays a small notification
	 *
	 * @param notType
	 *            the type of notification
	 */
	protected void showSmallNotification(final String notType) {
		emptyScreen();
		View v = View.inflate(this, R.layout.small_notification, null);
		rootLayout.addView(v, new RelativeLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mSmallNot = (ImageView) findViewById(R.id.smallNotificationIcon);
				if (notType.equals("SMS")) {
					mSmallNot.setImageResource(R.drawable.ic_sms_circle);
				} else if (notType.equals("Calendar")) {
					mSmallNot.setImageResource(R.drawable.ic_calendar_circle);
				} else if (notType.equals("Mail")) {
					mSmallNot.setImageResource(R.drawable.ic_email_circle);
				}
				blinkAnimation.reset();
				mSmallNot.setAnimation(blinkAnimation);
			}
		});
	}

	/**
	 * This function displays a small notification
	 *
	 * @param cmd
	 *
	 */
	protected void smallInfoNotification(String cmd) {
		String fileName = extractFileName(cmd);
		String type;
		String iconPath;

        if (cmd.startsWith(SHOW_SMALL_INFO_NOTIFICATION_CMD) && fileName != null) {
		    int index = fileName.indexOf("#");
			type = fileName.substring(0, index);
			iconPath = "";
			if (index < fileName.length() - 1) {
				iconPath = fileName.substring(index + 1, fileName.length());
			}
			showSmallInfoNotification(type, iconPath);
        }
	}

	/**
	 * This function displays a small notification
	 *
	 * @param cmd
	 *
	 */
	protected void showSmallInfoNotification(final String type, final String iconPath) {
		emptyScreen();

		View v = View.inflate(this, R.layout.small_notification, null);
		rootLayout.addView(v, new RelativeLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mSmallNot = (ImageView) findViewById(R.id.smallNotificationIcon);
				if (type.equals("Info")) {
					Bitmap bm = getInfoNotificationBitmap(iconPath);
					mSmallNot.setImageBitmap(bm);
				}
				blinkAnimation.reset();
				mSmallNot.setAnimation(blinkAnimation);
			}
		});
	}

	/**
	 * called by the networkservice to indicate that a image has been received
	 *
	 * @param bytes
	 *            a byte vector containing the image to show
	 */
	@Override
	public void screenReceived(final byte[] bytes) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				displayScreenshot(bytes);
			}
		});
	}

	/**
	 * used to show a video
	 *
	 * @param fileName
	 *            the path to the video file to play
	 */
	private void shiftToVideoView(String fileName) {
		emptyScreen();
		File f = new File(fileName);
		if (!f.exists()) {
			mService.sendRequest(fileName);
			return;
		}
		View v = View.inflate(this, R.layout.video_view, null);
		rootLayout.addView(v, new RelativeLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		VideoView myVideoView = (VideoView) findViewById(R.id.videoview);
		myVideoView.setVideoPath(fileName);
		myVideoView.start();
	}

	/**
	 * used to play a sound file
	 *
	 * @param fileName
	 *            the path to the sound file to play
	 */
	private void playSound(String fileName) {
		File f = new File(fileName);
		if (!f.exists()) {
			mService.sendRequest(fileName);
			return;
		}
		mAudioPlayer.reset();
		try {
			mAudioPlayer.setDataSource(fileName);
			mAudioPlayer.prepare();
			mAudioPlayer.start();
		} catch (Exception e) {
			// error
		}
	}

	/**
	 * uses the speech synthesizer to play sounds from strings
	 *
	 * @param speech
	 *            the string to speak
	 */
	protected void playSoundCommand(String speech) {
		try{
			tts.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
		} catch (Exception e){
			Util.log("The Puppet device couldn't use the TextToSpeech engine.", null);
		}
	}

	/**
	 * makes the puppet device vibrate for one second
	 */
	private void playVibration() {
		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(1000);
	}

	/**
	 * a helper function used to extract the extra field of a command
	 *
	 * @param cmd
	 *            the command string sent from the wizard
	 * @return the extra field e.g. filename
	 */
	private String extractFileName(String cmd) {
		int space = cmd.indexOf(" ");
		if (space > 0 && space < cmd.length()) {
			cmd = cmd.substring(space + 1).trim();
			String wizardPath = "/wizard_res/filebrowser/";
			if (cmd.contains(wizardPath)) {
				String temp = Environment.getExternalStorageDirectory()
						.getPath() + "/Content/puppet_res/filebrowser/";
				temp += cmd.substring(
						cmd.indexOf(wizardPath) + wizardPath.length(),
						cmd.length());
				cmd = temp;
			}
			return cmd;
		}
		return null;
	}

	/**
	 * A helper function used to extract the command Id.
	 *
	 * @param cmd
	 *            the command string sent from the wizard
	 */
	private String extractAndReSendCommandId(String cmd) {
		int space = cmd.indexOf("$");
		String extractedCommand;

		if (space > 0 && space < cmd.length()) {
			int commandId = Integer.parseInt(cmd.substring(space + 1).trim());
			extractedCommand = cmd.substring(0, space);
			mService.sendCommandId(COMMAND_ID + "$" + commandId);
			return extractedCommand;
		}
		return cmd;
	}

	/**
	 * A helper function used to send whether the device is a Video see-through.
	 */
	private void sendVideoSeeThroughDevice() {
		String videoDevice = "";
		if (mVideoSeeThroughEnabled) {
			videoDevice = "true";
		} else {
			videoDevice = "false";
		}
		mService.sendCommandId(VIDEO_SEE_THROUGH_DEVICE + "$" + videoDevice);
	}

	/**
	 * This function is called by the network service when a file has been
	 * received.
	 *
	 * @param fileName
	 *            the path to the new file
	 * @param bytes
	 *            the data contained in the new file
	 */
	public void dataReceived(final String fileName, byte[] bytes) {
		File file = new File(fileName);

		if (!file.exists()) {
			try {
				if (file.createNewFile()) {

					FileOutputStream fstream = new FileOutputStream(file);
					fstream.write(bytes);
					fstream.close();
					if (fileName.endsWith(".jpg") || fileName.endsWith(".png")) {
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								shiftToImageView(fileName);
							}
						});
					}

				}
			} catch (IOException e) {
			}
		}
	}

	/**
	 * called when a connection has been established to the wizard device
	 */
	@Override
	public void serverConnected() {
		runOnUiThread(new Runnable() {
			public void run() {
				//showToast("Connected to controller");
			}
		});

	}

	/**
	 * called when the connection to the wizard device has been disconnected
	 */
	@Override
	public void serverDisconnected() {
		runOnUiThread(new Runnable() {
			public void run() {
				//showToast(DISCONNECTED_FROM_CONTROLLER);
			}
		});
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		mService = ((NetworkService.LocalBinder) service).getService();
		mService.setCallback(this);
		showToast(CONNECTED_TO_NETWORK_SERVICE);
		if (mService.isConnected()) {
			this.serverConnected();
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		mService = null;
		showToast(DISCONNECTED_FROM_NETWORK_SERVICE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();

		inflater.inflate(R.menu.menu_layout, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case R.id.menu_quit:
			finish();
			mService.stopThread();
			break;
		case R.id.menu_ip:
			showDialog(1);
			break;
		case R.id.menu_setting:
			showDialog(2);
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 1:
			dialog = new Dialog(this);
			dialog.setContentView(R.layout.ip_dialog_layout);
			dialog.setTitle(INSERT_NEW_IP_NUMBER);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_dropdown_item_1line, D.IP_NUMS);
			autoCompleteTextView = (AutoCompleteTextView) dialog
					.findViewById(R.id.iptext);
			autoCompleteTextView.setAdapter(adapter);
			Button save = (Button) dialog.findViewById(R.id.save_button);
			Button cancel = (Button) dialog.findViewById(R.id.cancel_button);
			View.OnClickListener listener = new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					switch (v.getId()) {
					case R.id.save_button:

						if (mService != null)
							NetworkService.setIp(autoCompleteTextView.getText()
									.toString());

						dialog.dismiss();
						break;
					case R.id.cancel_button:
						dialog.dismiss();
						break;
					default:
						break;
					}

				}
			};
			save.setOnClickListener(listener);
			cancel.setOnClickListener(listener);
			return dialog;
		case 2:
		default:
			dialog = new Dialog(this);
			dialog.setContentView(R.layout.setting_dialog_layout);
			dialog.setTitle(R.string.setting_dialog_title);
			mOpticalButton = (RadioButton) dialog.findViewById(R.id.optical_button);
			mVideoButton = (RadioButton) dialog.findViewById(R.id.video_button);

			if (mVideoSeeThroughEnabled) {
				mOpticalButton.setChecked(false);
				mVideoButton.setChecked(true);
			} else {
				mOpticalButton.setChecked(true);
				mVideoButton.setChecked(false);
			}

			mOpticalButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					setVideoSeeThroughEnabled(false);
					mOpticalButton.setChecked(true);
					mVideoButton.setChecked(false);
					emptyScreen();
					dialog.dismiss();
				}
			});

			mVideoButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					shiftToPhotoView(CameraInfo.CAMERA_FACING_BACK);
					mOpticalButton.setChecked(false);
					mVideoButton.setChecked(true);
					setVideoSeeThroughEnabled(true);
					dialog.dismiss();
				}
			});
			return dialog;
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (locationManager != null)
			locationManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		if (sendCoords)
			mService.sendLocation(location.getLongitude(),
					location.getLatitude());
	}

	@Override
	public void onProviderDisabled(String provider) {
		if (provider.equals(LocationManager.GPS_PROVIDER)) {
			Log.d("onProviderDisabled", provider + " disabled");
			// TODO feedback
		}
	}

	@Override
	public void onProviderEnabled(String arg0) {
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
	}

	/**
	 * helper function to get the camera
	 *
	 * @return the camera
	 */
	private static Camera getCamera(int cameraId) {
		Camera c = null;
		try {
			c = Camera.open(cameraId);
		} catch (Exception e) {
			System.out.println("hej");
		}

		return c;
	}

	/**
	 * function to get a screenshot of the current screen
	 */
	private void getScreen() {
		View view = rootLayout.getRootView();
		view.setDrawingCacheEnabled(true);
		Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
		view.setDrawingCacheEnabled(false);

	    Canvas c = new Canvas(bitmap);
		rootLayout.draw(c);
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 1, byteStream);

		if (mService != null) {
			mService.sendScreen(byteStream.toByteArray());
		}
	}

	/**
	 * called to take a picture
	 */
	private void takePicture() {
		if (camera != null)
			saveNext = true;
		showToast("Picture saved");

	}

	/**
	 * a helper function to check if the device has gps
	 *
	 * @return true if the device has gps
	 */
	private boolean hasGps() {
		boolean tmp = getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_LOCATION_GPS);

		return tmp;
	}

	/**
	 * a function to request location updates. updates are requested from both
	 * the gps and the network.
	 */
	private void startGps() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				10000L, 50.0f, this);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 1000, 50, this);
	}

	/**
	 * initializes the voice recognition.
	 */
	protected void startVoiceCommand() {

		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
				"voice.recognition.test");

		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
		sr.startListening(intent);
	}

	/**
	 * an internal class to recognize voice commands
	 */
	class listener implements RecognitionListener {
		public void onReadyForSpeech(Bundle params) {
			shiftReadyForVoiceView("Speak now");
		}

		public void onBeginningOfSpeech() {
		}

		public void onRmsChanged(float rmsdB) {
		}

		public void onBufferReceived(byte[] buffer) {
		}

		public void onEndOfSpeech() {
			shiftToLoadingView("Loading...");
		}

		public void onError(int error) {
			shiftReadyForVoiceView("Speak again");
		}

		public void onResults(Bundle results) {
			ArrayList<String> matches = results
					.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

			String result = "";
			for (int i = 0; i < matches.size(); i++) {
				if (i != 0)
					result = result + ";" + matches.get(i);
				else
					result = matches.get(i);
			}

			if (result.contains("start camera") || result.contains("camera")) {
				mService.sendVoiceCommand(PUPPET_VOICECOMMAND_CMD, result
						+ "#camera");
				shiftToPhotoView(CameraInfo.CAMERA_FACING_BACK);
			} else if (result.contains("show message")
					|| result.contains("show messages")) {
				showingSMS = true;
				smsIndex = smsList.size() - 1;
				mService.sendVoiceCommand(PUPPET_VOICECOMMAND_CMD, result
						+ "#sms");
				showSMS();
			} else {
				mService.sendVoiceCommand(PUPPET_VOICECOMMAND_CMD, result);
			}
		}

		public void onPartialResults(Bundle partialResults) {
		}

		public void onEvent(int eventType, Bundle params) {
		}
	}

	/**
	 * initializes the tts
	 */
	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {

			int result = tts.setLanguage(Locale.US);

			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
			}

		} else {
			Log.d("player", "Initilization Failed!");
		}
	}

	/**
	 * called when a new preview frame is available
	 */
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {

		Log.d("image size", data.length + " bytes");
		if (sendPictures || takePictures || saveNext) {

			Camera.Parameters parameters = camera.getParameters();
			int format = parameters.getPreviewFormat();
			int w = parameters.getPreviewSize().width;
			int h = parameters.getPreviewSize().height;

			Package tmp = new Package(w, h, format, data);
			if (saveNext) {
				saveNext = false;
				tmp.regularSave = true;
				new AsyncWriter().execute(tmp);
			}
			if (sendPictures && System.currentTimeMillis() - lastTransmit > 100) {
				box.setImg(tmp);
				lastTransmit = System.currentTimeMillis();
			}
			if (takePictures) {
				if (System.currentTimeMillis() - lastPic > picInterval * 1000) {
					Log.d(TAG, "Writing file");
					Bitmap bmp = Bitmap.createBitmap(rootLayout.getWidth(), rootLayout.getHeight(), Bitmap.Config.ARGB_8888);
					Canvas c = new Canvas(bmp);
					rootLayout.draw(c);
					Package screen = new Package(bmp);
					mService.sendInfo("Photo taken");
					new AsyncWriter().execute(tmp, screen);
					lastPic = System.currentTimeMillis();
				}
			}
		}
	}

	/**
	 * a class used as a struct for the mailbox it is used to package
	 * information about a preview frame for the udpthread.
	 */
	public class Package {
		public int w;
		public int h;
		public int format;
		public Bitmap bmp;
		public byte[] data;
		public boolean regularSave = false;
		public boolean screenshot = false;
		public Package(int w, int h, int format, byte[] data) {
			this.w = w;
			this.h = h;
			this.format = format;
			this.data = data;
		}
		public Package(Bitmap bmp){
			this.bmp = bmp;
			screenshot = true;
		}
	}

	private void setVideoSeeThroughEnabled(boolean enableSound) {
		SharedPreferences pref = this.getSharedPreferences(this.
				getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = pref.edit();
		prefsEditor.putBoolean(VIDEO_SEE_THROUGH_ENABLED, enableSound);
		prefsEditor.commit();
		mVideoSeeThroughEnabled = enableSound;
		sendVideoSeeThroughDevice();
	}

	private boolean getVideoSeeThroughEnabled() {
		SharedPreferences pref = this.getSharedPreferences(this.
				getPackageName(), Context.MODE_PRIVATE);

		if (pref.contains(VIDEO_SEE_THROUGH_ENABLED)) {
		    mVideoSeeThroughEnabled = pref.getBoolean(VIDEO_SEE_THROUGH_ENABLED, false); 
		} else {
			SharedPreferences.Editor prefsEditor = pref.edit();
			prefsEditor.putBoolean(VIDEO_SEE_THROUGH_ENABLED, false);
			prefsEditor.commit();
		}
		return mVideoSeeThroughEnabled;
	}
}
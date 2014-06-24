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

package com.sonymobile.wozard.smartwatch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.sonyericsson.extras.liveware.aef.control.Control;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.control.ControlTouchEvent;

/**
 * The sample control for SmartWatch handles the control on the accessory. This
 * class exists in one instance for every supported host application that we
 * have registered to
 */
class ControlSmartWatch extends ControlExtension {

	private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.RGB_565;

	private static final int ANIMATION_X_POS = 46;

	private static final int ANIMATION_Y_POS = 46;

	private static final int ANIMATION_DELTA_MS = 500;

	private Handler mHandler;

	private WOZExtensionService wozService;

	public static final String SWIPE_UP_INTENT = "com.sonymobile.wozard.wizard.SWIPE_UP";
	public static final String SWIPE_LEFT_INTENT = "com.sonymobile.wozard.wizard.SWIPE_LEFT";
	public static final String SWIPE_RIGHT_INTENT = "com.sonymobile.wozard.wizard.SWIPE_RIGHT";
	public static final String SWIPE_DOWN_INTENT = "com.sonymobile.wozard.wizard.SWIPE_DOWN";
	public static final String PRESS_INTENT = "com.sonymobile.wozard.wizard.PRESS";
	public static final String LONG_PRESS_INTENT = "com.sonymobile.wozard.wizard.LONG_PRESS";

	public final int SWIPE_UP = 0;
	public final int SWIPE_DOWN = 1;
	public final int SWIPE_LEFT = 2;
	public final int SWIPE_RIGHT = 3;

	private boolean lastWasLongPress = false;

	private boolean mIsShowingAnimation = false;

	private boolean mIsVisible = false;

	private Animation mAnimation = null;

	private final int width;

	private final int height;

	/**
	 * Create sample control.
	 *
	 * @param hostAppPackageName
	 *            Package name of host application.
	 * @param context
	 *            The context.
	 * @param handler
	 *            The handler to use
	 */
	ControlSmartWatch(final String hostAppPackageName, final Context context,
			Handler handler) {
		super(context, hostAppPackageName);
		if (handler == null) {
			throw new IllegalArgumentException("handler == null");
		}
		wozService = (WOZExtensionService) context;
		mHandler = handler;
		width = getSupportedControlWidth(context);
		height = getSupportedControlHeight(context);
		setScreenState(Control.Intents.SCREEN_STATE_DIM);
	}

	/**
	 * Get supported control width.
	 *
	 * @param context
	 *            The context.
	 * @return the width.
	 */
	public static int getSupportedControlWidth(Context context) {
		return context.getResources().getDimensionPixelSize(
				R.dimen.smart_watch_control_width);
	}

	/**
	 * Get supported control height.
	 *
	 * @param context
	 *            The context.
	 * @return the height.
	 */
	public static int getSupportedControlHeight(Context context) {
		return context.getResources().getDimensionPixelSize(
				R.dimen.smart_watch_control_height);
	}

	@Override
	public void onDestroy() {

		Log.d(WOZExtensionService.LOG_TAG, "SampleControlSmartWatch onDestroy");
		// stopAnimation();
		mHandler = null;
	};

	@Override
	public void onStart() {
		// Nothing to do. Animation is handled in onResume.
	}

	@Override
	public void onStop() {
		// Nothing to do. Animation is handled in onPause.
	}

	@Override
	public void onResume() {
		mIsVisible = true;

		Log.d(WOZExtensionService.LOG_TAG,
				"Starting animation - SampleControlSmartWatch");

		// Animation not showing. Show animation.
		mIsShowingAnimation = true;
		mAnimation = new Animation();
		mAnimation.run();
	}

	@Override
	public void onPause() {
		Log.d(WOZExtensionService.LOG_TAG,
				"Stopping animation - SampleControlSmartWatch");
		mIsVisible = false;

		if (mIsShowingAnimation) {
			stopAnimation();
		}
	}

	/**
	 * Stop showing animation on control.
	 */
	public void stopAnimation() {
		// Stop animation on accessory
		if (mAnimation != null) {
			mAnimation.stop();
			mHandler.removeCallbacks(mAnimation);
			mAnimation = null;
		}
		mIsShowingAnimation = false;

		// If the control is visible then stop it
		if (mIsVisible) {
			stopRequest();
		}
	}

	@Override
	public void onTouch(final ControlTouchEvent event) {
		Log.d(WOZExtensionService.LOG_TAG, "onTouch() " + event.getAction());
		Intent broadcastIntent = new Intent();
		int action = event.getAction();
		switch (action) {
		case Control.Intents.TOUCH_ACTION_PRESS:
			lastWasLongPress = false;
			break;
		case Control.Intents.TOUCH_ACTION_RELEASE:
			if (lastWasLongPress) {
				broadcastIntent.setAction(LONG_PRESS_INTENT);
				Log.d("WOZbroadcast", "sending long press intent");
				wozService.sendBroadcast(broadcastIntent);
			} else {
				broadcastIntent.setAction(PRESS_INTENT);
				Log.d("WOZbroadcast", "sending press intent");
				wozService.sendBroadcast(broadcastIntent);
			}
			break;
		case Control.Intents.TOUCH_ACTION_LONGPRESS:
			lastWasLongPress = true;
			break;
		}

	}

	@Override
	public void onSwipe(int direction) {
		Intent broadcastIntent = new Intent();
		switch (direction) {
		case SWIPE_UP:
			broadcastIntent.setAction(SWIPE_UP_INTENT);
			break;
		case SWIPE_DOWN:
			broadcastIntent.setAction(SWIPE_DOWN_INTENT);
			break;
		case SWIPE_LEFT:
			broadcastIntent.setAction(SWIPE_LEFT_INTENT);
			break;
		case SWIPE_RIGHT:
			broadcastIntent.setAction(SWIPE_RIGHT_INTENT);
			break;
		}
		wozService.sendBroadcast(broadcastIntent);
	}

	/**
	 * The animation class shows an animation on the accessory. The animation
	 * runs until mHandler.removeCallbacks has been called.
	 */
	private class Animation implements Runnable {
		private int mIndex = 1;

		private final Bitmap mBackground;

		private boolean mIsStopped = false;

		/**
		 * Create animation.
		 */
		Animation() {
			mIndex = 1;

			// Extract the last part of the host application package name.
			String packageName = mHostAppPackageName
					.substring(mHostAppPackageName.lastIndexOf(".") + 1);

			// Create background bitmap for animation.
			mBackground = Bitmap.createBitmap(width, height, BITMAP_CONFIG);
			// Set default density to avoid scaling.
			mBackground.setDensity(DisplayMetrics.DENSITY_DEFAULT);

			LinearLayout root = new LinearLayout(mContext);
			root.setLayoutParams(new LayoutParams(width, height));

			LinearLayout sampleLayout = (LinearLayout) LinearLayout.inflate(
					mContext, R.layout.sample_control, root);
			// ((TextView) sampleLayout.findViewById(R.id.sample_control_text))
			// .setText(packageName);
			sampleLayout.measure(width, height);
			sampleLayout.layout(0, 0, sampleLayout.getMeasuredWidth(),
					sampleLayout.getMeasuredHeight());

			Canvas canvas = new Canvas(mBackground);
			sampleLayout.draw(canvas);

			showBitmap(mBackground);
		}

		/**
		 * Stop the animation.
		 */
		public void stop() {
			mIsStopped = true;
		}

		public void run() {
			int resourceId;
			switch (mIndex) {
			case 1:
				resourceId = R.drawable.generic_anim_1_icn;
				break;
			case 2:
				resourceId = R.drawable.generic_anim_2_icn;
				break;
			case 3:
				resourceId = R.drawable.generic_anim_3_icn;
				break;
			case 4:
				resourceId = R.drawable.generic_anim_2_icn;
				break;
			default:
				resourceId = R.drawable.generic_anim_1_icn;
				break;
			}
			mIndex++;
			if (mIndex > 4) {
				mIndex = 1;
			}

			if (!mIsStopped) {
				updateAnimation(resourceId);
			}
			if (mHandler != null && !mIsStopped) {
				mHandler.postDelayed(this, ANIMATION_DELTA_MS);
			}
		}

		/**
		 * Update the animation on the accessory. Only updates the part of the
		 * screen which contains the animation.
		 *
		 * @param resourceId
		 *            The new resource to show.
		 */
		private void updateAnimation(int resourceId) {
			Bitmap animation = BitmapFactory.decodeResource(
					mContext.getResources(), resourceId, mBitmapOptions);

			// Create a bitmap for the part of the screen that needs updating.
			Bitmap bitmap = Bitmap.createBitmap(animation.getWidth(),
					animation.getHeight(), BITMAP_CONFIG);
			bitmap.setDensity(DisplayMetrics.DENSITY_DEFAULT);
			Canvas canvas = new Canvas(bitmap);
			Paint paint = new Paint();
			Rect src = new Rect(ANIMATION_X_POS, ANIMATION_Y_POS,
					ANIMATION_X_POS + animation.getWidth(), ANIMATION_Y_POS
							+ animation.getHeight());
			Rect dst = new Rect(0, 0, animation.getWidth(),
					animation.getHeight());

			// Add first the background and then the animation.
			canvas.drawBitmap(mBackground, src, dst, paint);
			canvas.drawBitmap(animation, 0, 0, paint);

			showBitmap(bitmap, ANIMATION_X_POS, ANIMATION_Y_POS);
		}
	};

}

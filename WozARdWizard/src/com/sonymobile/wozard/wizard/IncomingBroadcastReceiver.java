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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sonymobile.wozard.wizard.utils.Monitor;

/**
 * A class that handles broadcasted SmartWatch interactions.
 */

public class IncomingBroadcastReceiver extends BroadcastReceiver  {

	/**
	 * When receiving a broadcast, it puts the latest interaction in a monitor
	 * for the active activity to pull.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Monitor mon = Monitor.getInstance();
		if(intent.getAction().equals("com.sonymobile.wozard.wizard.SWIPE_UP")){
			mon.setCommand(ControllerActivity.SWIPE_UP_INTENT);
		}else if(intent.getAction().equals("com.sonymobile.wozard.wizard.SWIPE_LEFT")){
			mon.setCommand(ControllerActivity.SWIPE_LEFT_INTENT);
		}else if(intent.getAction().equals("com.sonymobile.wozard.wizard.SWIPE_RIGHT")){
			mon.setCommand(ControllerActivity.SWIPE_RIGHT_INTENT);
		}else if(intent.getAction().equals("com.sonymobile.wozard.wizard.SWIPE_DOWN")){
			mon.setCommand(ControllerActivity.SWIPE_DOWN_INTENT);
		}else if(intent.getAction().equals("com.sonymobile.wozard.wizard.PRESS")){
			mon.setCommand(ControllerActivity.PRESS_INTENT);
		}else if(intent.getAction().equals("com.sonymobile.wozard.wizard.LONG_PRESS")){
			mon.setCommand(ControllerActivity.LONG_PRESS_INTENT);
		}
	}
}

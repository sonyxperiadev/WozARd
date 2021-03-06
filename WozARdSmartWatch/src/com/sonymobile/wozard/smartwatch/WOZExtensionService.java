/*
 Copyright (c) 2011, Sony Ericsson Mobile Communications AB

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.

 * Neither the name of the Sony Ericsson Mobile Communications AB nor the names
 of its contributors may be used to endorse or promote products derived from
 this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.sonymobile.wozard.smartwatch;

import com.sonyericsson.extras.liveware.extension.util.ExtensionService;
import com.sonyericsson.extras.liveware.extension.util.control.ControlExtension;
import com.sonyericsson.extras.liveware.extension.util.registration.DeviceInfo;
import com.sonyericsson.extras.liveware.extension.util.registration.DisplayInfo;
import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationAdapter;
import com.sonyericsson.extras.liveware.extension.util.registration.RegistrationInformation;

import android.os.Handler;
import android.util.Log;

/**
 * The Sample Extension Service handles registration and keeps track of all
 * controls on all accessories.
 */
public class WOZExtensionService extends ExtensionService {

    public static final String EXTENSION_KEY = "com.sonyericsson.extras.liveware.extension.samplecontrol.key";

    public static final String LOG_TAG = "SampleControlExtension";

    public WOZExtensionService() {
        super(EXTENSION_KEY);
    }

    /**
     * {@inheritDoc}
     *
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(WOZExtensionService.LOG_TAG, "SampleControlService: onCreate");
    }

    @Override
    protected RegistrationInformation getRegistrationInformation() {
        return new WOZRegistrationInformation(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sonyericsson.extras.liveware.aef.util.ExtensionService#
     * keepRunningWhenConnected()
     */
    @Override
    protected boolean keepRunningWhenConnected() {
        return false;
    }

    @Override
    public ControlExtension createControlExtension(String hostAppPackageName) {
    	Log.d(WOZExtensionService.LOG_TAG, "SampleControlService: createControlExtension");
        final int controlSWWidth = ControlSmartWatch.getSupportedControlWidth(this);
        final int controlSWHeight = ControlSmartWatch.getSupportedControlHeight(this);

        for (DeviceInfo device : RegistrationAdapter.getHostApplication(this, hostAppPackageName)
                .getDevices()) {
            for (DisplayInfo display : device.getDisplays()) {
                if (display.sizeEquals(controlSWWidth, controlSWHeight)) {
                    return new ControlSmartWatch(hostAppPackageName, this, new Handler());
                }
            }
        }
        throw new IllegalArgumentException("No control for: " + hostAppPackageName);
    }
}

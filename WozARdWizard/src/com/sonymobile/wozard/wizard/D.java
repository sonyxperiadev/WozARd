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

import android.media.AudioFormat;
import android.media.AudioRecord;

public class D {
	public static final int CAMERA_STREAM_SIZE = 65535;

	public static final int CAMERA_STREAM_UDP_SOCKET = 12335;

	public static final int SOUND_STREAM_UDP_SOCKET = 50005;

	public static final int SOUND_RATE = 8000;

	public static final int SOUND_CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_MONO;

	public static final int SOUND_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

	public static final int SOUND_STREAM_SIZE = AudioRecord.getMinBufferSize(D.SOUND_RATE,
			D.SOUND_CHANNEL_CONFIG, D.SOUND_AUDIO_FORMAT);

	public static final int SERVICE_TCP_SOCKET = 4455;

	public static final int SERVICE_TCP_PING_TIMEOUT_MS = 3000;

	//TODO:
	public static final String PUPPET_PATH = "/Content/puppet_res/";

	public static final String WIZARD_PATH = "/Content/wizard_res/";
}

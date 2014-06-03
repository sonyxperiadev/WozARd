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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

public class SoundRecordUDPThread extends Thread {
	private static final String TAG = "SoundUDPThread";

	private InetAddress mAdr;

	private DatagramSocket mSocket;

	private AudioRecord mRecorder;

	private boolean keepOn = true;

	private static final int SOUND_UDP_PORT = 50005;

	private static final int SOUND_RATE = 8000;

	private static final int SOUND_CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_MONO;

	private static final int SOUND_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

	private static int mMinBufferSize = AudioRecord.getMinBufferSize(SOUND_RATE,
			SOUND_CHANNEL_CONFIG, SOUND_AUDIO_FORMAT);

	public SoundRecordUDPThread(String ip) {
		try {
			mAdr = InetAddress.getByName(ip);
		} catch (IOException e) {
			mAdr = null;
		}
	}

	private synchronized boolean keepOn() {
		return keepOn;
	}

	public void run() {
		try {
			DatagramSocket socket = new DatagramSocket();
			Log.d(TAG, "Socket Created");
			byte[] buffer = new byte[mMinBufferSize];
			Log.d(TAG, "Buffer created of size " + mMinBufferSize);
			DatagramPacket packet;
			final InetAddress destination = mAdr;
			Log.d(TAG, "Address retrieved");
			mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, SOUND_RATE,
					SOUND_CHANNEL_CONFIG, SOUND_AUDIO_FORMAT, mMinBufferSize);
			Log.d(TAG, "Recorder initialized");
			mRecorder.startRecording();
			while (keepOn()) {
				if (mAdr == null) break;
				mMinBufferSize = mRecorder.read(buffer, 0, buffer.length);
				packet = new DatagramPacket(buffer, buffer.length, destination, SOUND_UDP_PORT);
				Log.d(TAG, "Send sound " + buffer.length);
				socket.send(packet);
			}
		} catch (UnknownHostException e) {
			Log.e(TAG, e.toString());
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		} finally {
			if (mSocket != null) mSocket.close();
			mRecorder.release();
		}
	}

	/**
	 * call this to kill the thread
	 */
	public synchronized void killThread() {
		keepOn = false;
	}
}

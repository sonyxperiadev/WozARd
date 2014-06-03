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

import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/*
 * @brief Worker thread receives raw audio stream and forwards to sound speaker
 * */
public class SoundPlayUDPThread extends Thread {
	private static final String TAG = "SoundUDPThread";

	private DatagramSocket mSocket;

	private AudioTrack mSpeaker;

	private boolean keepOn = true;

	private synchronized boolean keepOn() {
		return keepOn;
	}

	/**
	 * call this to kill the thread
	 */
	public synchronized void killThread() {
		keepOn = false;
	}

	@Override
	public void run() {
		try {
			DatagramSocket socket = new DatagramSocket(D.SOUND_STREAM_UDP_SOCKET);
			Log.d(TAG, "Socket Created");
			byte[] buffer = new byte[D.SOUND_STREAM_SIZE];
			mSpeaker = new AudioTrack(AudioManager.STREAM_MUSIC, D.SOUND_RATE, D.SOUND_CHANNEL_CONFIG,
					D.SOUND_AUDIO_FORMAT, D.SOUND_STREAM_SIZE, AudioTrack.MODE_STREAM);
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			mSpeaker.play();
			while (keepOn()) {
				try {
					socket.receive(packet);
					Log.d(TAG, "Packet Received");
					buffer = packet.getData();
					Log.d(TAG, "Packet data read into buffer");
					if (mSpeaker.write(buffer, 0, packet.getLength()) > 0) {
						mSpeaker.flush();
					}
					packet.setLength(D.SOUND_STREAM_SIZE);
				} catch (IOException e) {
					Log.e(TAG, e.toString());
				}
			}
			socket.close();
		} catch (SocketException e) {
			Log.e(TAG, e.toString());
			if (mSocket != null) {
				mSocket.close();
			}
		} finally {
			if (mSocket != null) {
				mSocket.close();
			}
			if (mSpeaker != null) {
				mSpeaker.release();
			}
			mSpeaker = null;
			mSocket = null;
		}
	}
}

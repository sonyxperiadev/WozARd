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


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;


/**
 * A service used for the camera stream
 * Contains a thread that handles the UDP socket
 * @author 23060470
 *
 */
public class CameraService extends Service{
	private final LocalBinder mBinder = new LocalBinder();
	private CameraServiceCallback callback;
	private UDPThread thread;

	@Override
	public void onCreate(){
		super.onCreate();

		thread = new UDPThread();
		thread.start();
	}


	public class LocalBinder extends Binder{
		public CameraService getService(){
			return CameraService.this;
		}
	}
	/**
	 * Registers the callback to be used by the service
	 * @param cb The callback, has to implement the interface CameraServiceCallback
	 */
	public void registerCallback(CameraServiceCallback cb){
		callback = cb;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	/**
	 * The thread responsible for the datagramsocket
	 * currently uses port 12335.
	 * @author 23060470
	 *
	 */
	private class UDPThread extends Thread{
		private DatagramSocket socket;
		private DatagramPacket pack;

		private boolean go = true;
		@Override
		public void run() {
			try{
				byte[] buf = new byte[D.CAMERA_STREAM_SIZE];
				pack = new DatagramPacket(buf, D.CAMERA_STREAM_SIZE);
				socket = new DatagramSocket(D.CAMERA_STREAM_UDP_SOCKET);
				while (keepOn()){
					socket.receive(pack);
					final Bitmap bmp =
							BitmapFactory.decodeByteArray(
									pack.getData(), 0, pack.getLength());
					if (callback != null) {
						callback.setScreen(bmp);
					}
					pack.setLength(D.CAMERA_STREAM_SIZE);
				}
			} catch (IOException e) {
				if(socket != null)
					socket.close();
				socket = null;
			} finally {
				if(socket!= null)
					socket.close();
				socket = null;
			}
		}

		public synchronized void killThread(){
			go = false;
		}

		public synchronized boolean keepOn(){
			return go;
		}
	}

	/**
	 * An interface used to communicate with the camera service
	 * @author 23060470
	 *
	 */
	public interface CameraServiceCallback{
		public void setScreen(final Bitmap bmp);
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		thread.killThread();
	}
}

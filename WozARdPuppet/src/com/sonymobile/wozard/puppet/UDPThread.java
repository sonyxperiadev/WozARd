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
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.Log;

import com.sonymobile.wozard.puppet.PuppetActivity.Package;
import com.sonymobile.wozard.puppet.util.Util;

/**
 * A class used to send the camera feed. It sends the images over udp
 */
public class UDPThread extends Thread{
	private DatagramSocket socket;
	private boolean keepOn = true;
	private Mailbox mailbox;
	private DatagramPacket pack;
	private InetAddress adr;

	public UDPThread(Mailbox box, String ip){
		try{
			adr = InetAddress.getByName(ip);
		}catch (IOException e){
			adr = null;
		}
		mailbox = box;
	}
	private synchronized boolean keepOn(){
		return keepOn;
	}
	/**
	 * the run loop
	 * it tries to fetch an image from the mailbox. when it gets one it compresses the image to jpg and sends it
	 * in a udp package
	 */
	public void run(){
		try{
			socket = new DatagramSocket();
			while(keepOn()){
				if(adr == null)
					break;
				byte[] bytes = null;
				Package pkg = mailbox.getImg();
				ByteArrayOutputStream ostream = new ByteArrayOutputStream();

				if(pkg.format == ImageFormat.NV21){
					YuvImage img = new YuvImage(pkg.data, pkg.format, pkg.w, pkg.h, null);
					Rect rect = new Rect(0,0,pkg.w,pkg.h);
					img.compressToJpeg(rect, 30, ostream);
					bytes = ostream.toByteArray();
				} else if (pkg.format == ImageFormat.JPEG){
					Log.d("size of jpg", pkg.data.length + " bytes");
				} else {
					int[] argb8888 = new int[pkg.w * pkg.h];
					Util.decodeYUV420SP(argb8888, pkg.data, pkg.w, pkg.h);
					Bitmap img = Bitmap.createBitmap(argb8888, pkg.w, pkg.h, Config.ARGB_8888);

					img.compress(Bitmap.CompressFormat.JPEG, 30, ostream);
					bytes = ostream.toByteArray();

				}



				//				if(pkg.format == ImageFormat.NV21 || pkg.format == ImageFormat.NV16){

				Log.d("after compression", bytes.length + " bytes");
				pack = new DatagramPacket(bytes, bytes.length, adr, D.CAMERA_STREAM_UDP_SOCKET);
				socket.send(pack);
				//				}
			}
		}catch (IOException e){
			if(socket != null)
				socket.close();
		} catch(Exception e){
			if(socket != null)
				socket.close();
		}
		finally {
			if(socket != null)
				socket.close();
		}
	}
	/**
	 * call this to kill the thread
	 */
	public synchronized void killThread(){
		keepOn = false;
	}
}

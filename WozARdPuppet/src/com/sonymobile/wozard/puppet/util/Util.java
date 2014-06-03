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
package com.sonymobile.wozard.puppet.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.location.Location;
import android.os.Environment;
/**
 * A class containing some static utility functions
 *
 */
public class Util {
	private static final String logfile = "log.txt";
	private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
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
	 * A function that writes a logmessage to the SD card
	 * @param message The message to write
	 * @param location If not null this position is added to the log entry
	 */
	public static void log(String message, Location location) {
		try{
		File tmp = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/puppet_res/"  + logfile);

		BufferedWriter writer = new BufferedWriter(new FileWriter(tmp, true));
		StringBuilder msg = new StringBuilder();
		msg.append(format.format(new Date(System.currentTimeMillis())));
		msg.append(" ; ");
		if(location != null){
			msg.append("latitude: " + location.getLatitude());
			msg.append(", longitude: " + location.getLongitude());
			msg.append(" ; ");
		}
		msg.append(message);
		msg.append('\n');
		writer.write(msg.toString());
		writer.close();
		}catch (IOException e){
			
		}
	}
	/**
	 * A function used to decode an image if the camera preview is using nv16
	 * @param rgb this is where the new image will be put
	 * @param yuv420sp the old image
	 * @param width the width of the image
	 * @param height the height of the image
	 */
	static public void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
	    final int frameSize = width * height;

	    for (int j = 0, yp = 0; j < height; j++) {
	        int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
	        for (int i = 0; i < width; i++, yp++) {
	            int y = (0xff & ((int) yuv420sp[yp])) - 16;
	            if (y < 0) y = 0;
	            if ((i & 1) == 0) {
	                v = (0xff & yuv420sp[uvp++]) - 128;
	                u = (0xff & yuv420sp[uvp++]) - 128;
	            }
	            int y1192 = 1192 * y;
	            int r = (y1192 + 1634 * v);
	            int g = (y1192 - 833 * v - 400 * u);
	            int b = (y1192 + 2066 * u);

	            if (r < 0) r = 0; else if (r > 262143) r = 262143;
	            if (g < 0) g = 0; else if (g > 262143) g = 262143;
	            if (b < 0) b = 0; else if (b > 262143) b = 262143;

	            rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
	        }
	    }
	}
}

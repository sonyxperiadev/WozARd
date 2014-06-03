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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.sonymobile.wozard.puppet.PuppetActivity.Package;
import com.sonymobile.wozard.puppet.util.Util;
/**
 * an asynchronous task class used to save images
 */
public class AsyncWriter extends AsyncTask<Package, Void, Void>{

	@Override
	protected Void doInBackground(Package... params) {
		if (params.length < 1)
			return null;
		Date d = new Date(System.currentTimeMillis());
		SimpleDateFormat df = new SimpleDateFormat("HHmmss", Locale.GERMAN);
		String dir = Environment.getExternalStorageDirectory().getPath() + D.capFolder;
		for(Package p : params){
			if(!p.regularSave)
				dir+="auto/";
			if(p.screenshot){
				dir+="screen-" + df.format(d) + ".png";
				try{
					File f = new File(dir);
					FileOutputStream ostream = new FileOutputStream(f);
					p.bmp.compress(CompressFormat.PNG, 1, ostream);
					ostream.flush();
					ostream.close();
				}catch (IOException e){

				}
			} else {
				dir += "img-" + df.format(d) + ".jpg";
				File f = new File(dir);
				try{
					FileOutputStream ostream = new FileOutputStream(f);

					if(params[0].format != ImageFormat.NV21){
						int[] argb8888 = new int[params[0].w * params[0].h];
						Util.decodeYUV420SP(argb8888, params[0].data, params[0].w, params[0].h);
						Bitmap img = Bitmap.createBitmap(argb8888, params[0].w, params[0].h, Config.ARGB_8888);
						img.compress(Bitmap.CompressFormat.JPEG, 30, ostream);
						ostream.close();

					}else{
						YuvImage img = new YuvImage(params[0].data, params[0].format, params[0].w, params[0].h, null);
						Rect rect = new Rect(0,0,params[0].w,params[0].h);


						img.compressToJpeg(rect, 100, ostream);
						ostream.close();

					}
				}catch (IOException e) {
					Log.d("AsyncWriter", "Something went wrong when writing to " + f.getAbsolutePath(), e);
				}

			}
		}
		return null;
	}
}
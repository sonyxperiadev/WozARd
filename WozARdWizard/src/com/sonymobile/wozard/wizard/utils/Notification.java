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
package com.sonymobile.wozard.wizard.utils;

import android.graphics.Bitmap;


/**
 * A class used to represent a notification
 */
public class Notification {

	private String iconFileName;
	private String message;
	private String messageType;
	private Bitmap icon;

	/**
	 * Creates an instance of {@link Notification}
	 * @param title A string containing what to display in the title field
	 * @param message The message of the notification
	 */
	public Notification(String iconName, String message, String messageType, Bitmap icon){
		this.iconFileName = iconName;
		this.message = message;
		this.messageType = messageType;
		this.icon = icon;
	}
	
	/**
	 * A function to get the icon file name of the notification
	 * @return The iconFileName
	 */
	public String getIconFileName(){
		return iconFileName;
	}

	/**
	 * A function to get the message of the notification
	 * @return the message
	 */
	public String getMessage(){
		return message;
	}

	/**
	 * A function to get the type of message of the notification
	 * @return the type
	 */
	public String getType(){
		return messageType;
	}

	/**
	 * A function to get the icon of the notification
	 * @return the icon
	 */
	public Bitmap getIcon() {
		return icon;
	}
}

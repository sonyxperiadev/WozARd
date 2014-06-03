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

import com.sonymobile.wozard.wizard.fragments.PredefinedFragment;

/**
 * A class used to represent commands in the {@link PredefinedFragment}
 */
public class Command { 
	private String descriptor;
	private String commandType;
	private String extras = "";
	
	public Command(String descriptor, String command){
		this.descriptor = descriptor;
		commandType = command.toUpperCase();
	}
	
	public void setExtra(String extra){
		if(extra == null)
			return;
		extras = extra;
	}
	
	/**
	 * Returns the description of the command
	 * @return The description
	 */
	public String getDescription(){
		return descriptor;
	}

	/**
	 * Returns the description of the command
	 * @return The description
	 */
	public String getFilePath(){
		return extras;
	}

	/**
	 * Returns a string representation of the command. The string is formatted in such a way that it can be 
	 * used to send to the puppet application
	 */
	@Override
	public String toString(){
		return (commandType + " " + extras).trim();
	}

}

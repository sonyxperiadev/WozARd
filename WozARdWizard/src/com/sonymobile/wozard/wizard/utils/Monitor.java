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

/**
 * A monitor used for smartwatch interaction
 */
public class Monitor {
	private boolean changed = false;
	private int cmd;
	private static Monitor instance;
	private Monitor(){}
	/**
	 * A function to get the instance of the monitor
	 * @return The monitor
	 */
	public static Monitor getInstance(){
		if(instance == null)
			instance = new Monitor();
		return instance;
	}

	/**
	 * Sets the command in the monitor and notifies threads waiting
	 * @param cmd The command to set
	 */
	public synchronized void setCommand(int cmd){
		this.cmd = cmd;
		changed = true;
		notifyAll();
	}

	/**
	 * Get a command from the monitor. 
	 * This function blocks until a command has been received
	 * @return The command
	 */
	public synchronized int getCommand(){
		while(!changed){
			try{
			wait();
			}catch (InterruptedException e) {
				//ignore
			}
		}
		changed = false;
		return cmd;
	}

}

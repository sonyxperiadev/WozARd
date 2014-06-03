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

import java.util.ArrayList;
import java.util.List;

import com.sonymobile.wozard.wizard.ControllerActivity;

import android.graphics.Bitmap;

public class CommandList {

	private static CommandList mCommandList;
	private List<CommandObject> commands = new ArrayList<CommandList.CommandObject>();

	public static CommandList getInstance() {
		if (mCommandList == null) {
			mCommandList = new CommandList();
		}
		return mCommandList;
	}

	public void setCommand(int commandId, String command, String filePath, String speech,
			Bitmap image) {
		String tmpCommand = "";
		String tmpFilePath = "";
		String tmpSpeech = "";
		
		if (command != null) {
			tmpCommand = command;
		}
		if (filePath != null) {
			tmpFilePath = filePath;
		}
		if (speech != null) {
			tmpSpeech = speech;
		}
        CommandObject co = new CommandObject(commandId, tmpCommand, tmpFilePath, tmpSpeech, image);
        mCommandList.commands.add(co);
	}

	/** 
	 * Helper function to check if the command that was sent was a media file
	 * @param filePath
	 * @return
	 */
	public boolean checkIfMediaFile (int commandId) {
		boolean mediaFile = false;
		String filePath = "";
		if (commandId > 0) {
			// if speech is sent it is considered to be media file
			if (mCommandList.commands.get(commandId - 1).speech != null && 
					mCommandList.commands.get(commandId - 1).speech != "") {
				mediaFile = true;
			}
			filePath = mCommandList.commands.get(commandId - 1).filePath;
		    if (filePath.endsWith(".jpg") || 
			    filePath.endsWith(".png") ||
			    filePath.endsWith(".vbr") ||
			    filePath.endsWith(".mp3") ||
			    filePath.endsWith(".mp4") ||
			    filePath.endsWith(".gif") ||
			    filePath.endsWith(".3gp") ||
			    filePath.endsWith(".wav") ||
			    filePath.endsWith(".ogg")) {	
			    mediaFile = true;
		   }
		}
		return mediaFile;
	}

	/** 
	 * Helper function to check if the command that was sent was the clear command
	 * @param filePath
	 * @return
	 */
	public boolean checkIfClearCommand (int commandId) {
		boolean clearCommand = false;
		if (commandId > 0) {
			if (mCommandList.commands.get(commandId - 1).command.
					equals(ControllerActivity.CLEAR_INDICATOR_CMD))
				clearCommand = true;
			}
		return clearCommand;
	}

	public Bitmap getImageToShow(int commandId) {
		//TODO comment this why -1
		if (commandId > 0) {
			return mCommandList.commands.get(commandId - 1).getImage();
		}
		return null;
	}

	public class CommandObject {
		private String command;
		private String filePath;
		private String speech;
		private int commandId;
		private Bitmap theImage;
		private boolean success = false;

		/**
		 * Creates an instance of
		 */
		public CommandObject(int commandId, String command, String filePath, String speech, Bitmap image) {
			this.commandId = commandId;
			this.command = command;
			this.filePath = filePath;
			this.speech = speech;
			this.theImage = image;
		}

		/**
		 * A function to set the command.
		 */
		public void setCommand(String command){
			this.command = command;
		}

		/**
		 * A function to set the file path.
		 */
		public void setFilePath(String filePath){
			this.filePath = filePath;
		}

		/**
		 * A function to set the file path.
		 */
		public void setSpeech(String speech){
			this.speech = speech;
		}

		/**
		 * A function to set the commandId.
		 */
		public void setCommandId(int commandId){
			this.commandId = commandId;
		}

		/**
		 * A function to set the image that will be shown on the user side.
		 */
		public void setTheImage(Bitmap image){
			this.theImage = image;
		}

		/**
		 * A function to set if the command was successfully sent.
		 */
		public void setSuccess(boolean success){
			this.success = success;
		}

		/**
		 * A function to get the command
		 * @return the command
		 */
		public String getCommand(){
			return command;
		}

		/**
		 * A function to get the file path of the sent command
		 * @return The iconFileName
		 */
		public String getfilePath(){
			return filePath;
		}

		/**
		 * A function to get the speech string of the sent command
		 * @return The speech
		 */
		public String getSpeech(){
			return speech;
		}

		/**
		 * A function to get the commandId
		 * @return the command id
		 */
		public int getCommandId(){
			return commandId;
		}

		/**
		 * A function to get the icon of the notification
		 * @return the icon
		 */
		public Bitmap getImage() {
			return theImage;
		}

		/**
		 * A function to know if the command was successfully sent.
		 * @return true if successfully sent.
		 */
	    public boolean getSuccess() {
	    	return success;
	    }
	}
}

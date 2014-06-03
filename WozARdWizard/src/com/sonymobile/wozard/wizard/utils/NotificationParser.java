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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.util.Log;
import android.util.Xml;


/**
 * This class contains functionality for parsing xml files containing notifications
 *
 */
public class NotificationParser {

	private static final String ns = null;

	/**
	 * This function parses an xml file containing notifications
	 *
	 * @param filename the file containing the commands
	 * @return an {@link Arraylist} containing the commands
	 */
	public static ArrayList<Notification> parseNotifications(String filename){
		ArrayList<Notification> notifications = new ArrayList<Notification>();
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(new File(filename)));
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(reader);
			parser.nextTag();

			readNotifications(parser, notifications);

		} catch (IOException e) {
			Log.e("CommandParser.parseCommands()", "IOException caught", e);
		} catch (XmlPullParserException e){
			Log.e("CommandParser.parseCommands()", "XMLPullParserException caught", e);
		} finally {
			if(reader != null)
				try{
					reader.close();
				} catch (Exception e){
					//Ignore
				}
		}
		return notifications;
	}

	/**
	 *
	 * @param parser
	 * @param notifications
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private static void readNotifications(XmlPullParser parser,
			ArrayList<Notification> notifications) throws XmlPullParserException, IOException {

		parser.require(XmlPullParser.START_TAG, ns, "notifications");
		while (parser.next() != XmlPullParser.END_TAG){
			if (parser.getEventType() != XmlPullParser.START_TAG)
				continue;
				String name = parser.getName();
				if (name.equals("item")) {
				    notifications.add(readItem(parser));
				} else {
                    skip(parser);
				}
		}
		parser.require(XmlPullParser.END_TAG, ns, "notifications");
	}

	/**
	 * 
	 * @param parser
	 * @return
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private static Notification readItem(XmlPullParser parser) throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "item");
		String iconFileName = "";
		String messageType = "";
		String message = "";

		while(parser.next() != XmlPullParser.END_TAG){
			if (parser.getEventType() != XmlPullParser.START_TAG)
				continue;
				String name = parser.getName();

				if (name.equals("icon-filename")) {
					iconFileName =  readIconPath(parser);
				} else if (name.equals("type")) {
					messageType = readMessageType(parser);
				} else if (name.equals("message")) {
					message = readMessage(parser);
				} else {
					skip(parser);
				}
		}
		parser.require(XmlPullParser.END_TAG, ns, "item");
		return new Notification(iconFileName, message, messageType, null);
	}

	private static String readIconPath(XmlPullParser parser)throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "icon-filename");
		String iconPath = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "icon-filename");
		return iconPath;
	}

	private static String readMessageType(XmlPullParser parser) throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG, ns, "type");
		String messageType = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "type");
		return messageType;
	}

	private static String readMessage(XmlPullParser parser) throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG, ns, "message");
		String message = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "message");
		return message;
	}

	private static String readText(XmlPullParser parser) throws XmlPullParserException, IOException{
		String text = "";
		if(parser.next() == XmlPullParser.TEXT){
			text = parser.getText();
			parser.nextTag();
		}
		return text;
	}

	private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException{
		if (parser.getEventType() != XmlPullParser.START_TAG) {
			throw new IllegalStateException();
		}
		int depth = 1;
		while (depth != 0) {
			switch (parser.next()) {
			case XmlPullParser.END_TAG:
				depth--;
				break;
			case XmlPullParser.START_TAG:
				depth++;
				break;
			}
		}
	}

	/**
	 * A function that writes the notifications to an xml file
	 * @param filename the file to save the notifications in
	 * @param A CopyOnWriteArrayList containing the tour points
	 */
	public static void saveNotificationsToXml(String filename,
			ArrayList<Notification> notifications) {

		File tmp = new File(Settings.DEFAULT_ROOT_NOTIFICATIONS + filename);
		Util.preparePath(tmp.getParent());
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(tmp,
					false));
			XmlSerializer serializer = Xml.newSerializer();
			serializer.setOutput(writer);
			serializer.startDocument(null, Boolean.valueOf(true));
			serializer.startTag(null, "notifications");
			for(Notification notification : notifications){
				serializer.startTag(null,"item");
				serializer.startTag(null, "icon-filename");
				serializer.text(notification.getIconFileName());
				serializer.endTag(null, "icon-filename");
				serializer.startTag(null, "type");
				serializer.text(notification.getType());
				serializer.endTag(null, "type");
				serializer.startTag(null, "message");
				serializer.text(notification.getMessage());
				serializer.endTag(null, "message");
				serializer.endTag(null, "item");
			}
			serializer.endTag(null, "notifications");
			serializer.endDocument();
			serializer.flush();
			writer.close();
		} catch (IOException e) {
			Log.e("CommandParser",
					"encountered an error in save notifications", e);
		}
	}
}

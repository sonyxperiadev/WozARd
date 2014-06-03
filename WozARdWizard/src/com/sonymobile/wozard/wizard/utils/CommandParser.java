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
import java.util.concurrent.CopyOnWriteArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.util.Log;
import android.util.Xml;

import com.google.android.maps.GeoPoint;
import com.sonymobile.wozard.wizard.fragments.PredefinedFragment;


/**
 * This class contains parsers for both {@link Command}s and {@link CommandOverlay}s
 */
public class CommandParser {
	private static final String ns = null;

	/**
	 * This function parses an xml file containing {@link Command}s for {@link PredefinedFragment}
	 *
	 * @param filename the file containing the commands
	 * @return an {@link ArrayList} containing the commands
	 */
	public static ArrayList<Command> parseCommands(String filename){
		ArrayList<Command> commands = new ArrayList<Command>();
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(new File(filename)));
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(reader);
			parser.nextTag();

			readSequence(parser, commands);

		} catch (IOException e) {
			Log.e("CommandParser.parseCommands()", "IOException caught", e);
		} catch (XmlPullParserException e){
			Log.e("CommandParser.parseCommands()", "XMLPullParserException caught", e);
		}
		finally {
			if(reader != null)
				try{
					reader.close();
				} catch (Exception e){
					//Ignore
				}
		}


		return commands;
	}

	private static void readSequence(XmlPullParser parser, ArrayList<Command> commands) throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG, ns, "sequence");
		while(parser.next() != XmlPullParser.END_TAG){
			if(parser.getEventType() != XmlPullParser.START_TAG)
				continue;
			String name = parser.getName();
			if(name.equals("item"))
				commands.add(readItem(parser));
			else
				skip(parser);
		}
		parser.require(XmlPullParser.END_TAG, ns, "sequence");
	}

	private static Command readItem(XmlPullParser parser) throws XmlPullParserException, IOException{
		String descriptor = "";
		String command = "";
		String extra = null;
		Command cmd = null;
		parser.require(XmlPullParser.START_TAG, ns, "item");
		while(parser.next() != XmlPullParser.END_TAG){
			if(parser.getEventType() != XmlPullParser.START_TAG)
				continue;
			String name = parser.getName();
			if(name.equals("command"))
				command = readCommand(parser);
			else if(name.equals("extra"))
				extra = readExtra(parser);
			else if(name.equals("description"))
				descriptor = readDescription(parser);
			else
				skip(parser);
		}
		cmd = new Command(descriptor, command);
		if(extra != null)
			cmd.setExtra(extra);
		return cmd;
	}


	/**
	 * A function that writes the tour to an xml file
	 * @param filename the file to save the points in
	 * @param A CopyOnWriteArrayList containing the tour points
	 */
	public static void printCommand(String filename,
			CopyOnWriteArrayList<CommandOverlay> commands) {

		File tmp = new File(filename);
		Util.preparePath(tmp.getParent());
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(tmp,
					false));
			XmlSerializer serializer = Xml.newSerializer();

			serializer.setOutput(writer);
			serializer.startDocument(null, Boolean.valueOf(true));
			serializer.startTag(null, "tour");
			for(CommandOverlay cmd : commands){
				serializer.startTag(null,"point");
				serializer.attribute(null, "longitude", Integer.toString(cmd.getPoint().getLongitudeE6()));
				serializer.attribute(null, "latitude", Integer.toString(cmd.getPoint().getLatitudeE6()));
				serializer.attribute(null, "threshold", Float.toString(cmd.getThreshold()));
				serializer.startTag(null, "command");
				serializer.text(cmd.getCommand());
				serializer.endTag(null, "command");
				if(cmd.getExtra().length() > 0){
					serializer.startTag(null, "extra");
					serializer.text(cmd.getExtra());
					serializer.endTag(null, "extra");
				}
				serializer.endTag(null, "point");
				serializer.flush();
			}
			serializer.endTag(null, "tour");
			serializer.endDocument();
			serializer.flush();
			writer.close();
		} catch (IOException e) {
			Log.e("CommandParser",
					"encountered an error in printCommandOverlays", e);
		}
	}

	/**
	 * A function that parses a file containing {@link CommandOverlay}s in xml. For a full description of the
	 * xml format check the developer guidelines
	 * @param filename the name of the file containing the overlays
	 * @return a {@link CopyOnWriteArrayList} containing the {@link CommandOverlay}s
	 */
	public static CopyOnWriteArrayList<CommandOverlay> parseTour(String filename){
		CopyOnWriteArrayList<CommandOverlay> cmds = new CopyOnWriteArrayList<CommandOverlay>();
		BufferedReader reader = null;
		try{
			File file = new File(filename);
			if(!file.exists()){
				Log.e("CommandParser.parserTour()", "File " + filename + " not found");
				return cmds;
			}
			reader = new BufferedReader(new FileReader(file));
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(reader);
			parser.nextTag();
			cmds = readTour(parser, cmds);
		} catch (IOException e) {
			Log.e("CommandParser.parseTour()", "IOException caught", e);
		} catch (XmlPullParserException e){
			Log.e("CommandParser.parseTour()", "XMLPullParserException caught", e);
		} finally {
			if(reader != null)
				try{
					reader.close();
				} catch (Exception e){

				}
		}
		return cmds;
	}

	private static CopyOnWriteArrayList<CommandOverlay> readTour(XmlPullParser parser,
			CopyOnWriteArrayList<CommandOverlay> cmds)
					throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG, ns, "tour");
		while(parser.next() != XmlPullParser.END_TAG){
			if(parser.getEventType() != XmlPullParser.START_TAG)
				continue;
			String name = parser.getName();
			if(name.equals("point"))
				cmds.add(readPoint(parser));
			else
				skip(parser);
		}
		parser.require(XmlPullParser.END_TAG, ns, "tour");
		return cmds;
	}

	private static CommandOverlay readPoint(XmlPullParser parser) throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG, ns, "point");
		String command = "";
		String extra = "";
		CommandOverlay cmd = null;
		int longitude = Integer.parseInt(parser.getAttributeValue(ns, "longitude"));
		int latitude = Integer.parseInt(parser.getAttributeValue(ns, "latitude"));
		float threshold = Float.parseFloat(parser.getAttributeValue(ns, "threshold"));

		while(parser.next() != XmlPullParser.END_TAG){
			if(parser.getEventType() != XmlPullParser.START_TAG)
				continue;
			String name = parser.getName();

			if(name.equals("command"))
				command = readCommand(parser);
			else if(name.equals("extra"))
				extra = readExtra(parser);
			else
				skip(parser);
		}
		cmd = new CommandOverlay(new GeoPoint(latitude, longitude), "title", "snippet", command);
		if(extra != null)
			cmd.setExtra(extra);
		cmd.setThreshold(threshold);
		parser.require(XmlPullParser.END_TAG, ns, "point");
		return cmd;
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

	private static String readCommand(XmlPullParser parser) throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG, ns, "command");
		String cmd = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "command");
		return cmd;
	}

	private static String readExtra(XmlPullParser parser) throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG, ns, "extra");
		String extra = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "extra");
		return extra;
	}

	private static String readDescription(XmlPullParser parser) throws XmlPullParserException, IOException{
		parser.require(XmlPullParser.START_TAG, ns, "description");
		String description = readText(parser);
		parser.require(XmlPullParser.END_TAG, ns, "description");
		return description;
	}

	private static String readText(XmlPullParser parser) throws XmlPullParserException, IOException{
		String text = "";
		if(parser.next() == XmlPullParser.TEXT){
			text = parser.getText();
			parser.nextTag();
		}
		return text;
	}
}

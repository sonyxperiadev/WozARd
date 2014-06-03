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

import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;
import com.sonymobile.wozard.wizard.TourService;
import com.sonymobile.wozard.wizard.fragments.TourFragment;
/**
 * A class to represent commands on the map in the {@link TourFragment}
 * This class is also used by the {@link TourService} 
 */
public class CommandOverlay extends OverlayItem{
	private static int counter = 0;
	private String command;
	private String extra = "";
	private float threshold = 20;
	private final int id = counter++;
	public CommandOverlay(GeoPoint point, String title, String snippet, String command) {
		super(point, title, snippet);
		this.command = command;
	}

	public CommandOverlay(int latitude, int longitude, String title, String snippet, String command){
		super(new GeoPoint(latitude,longitude), title, snippet);
		this.command = command;
	}

	/**
	 * Sets the extra field of the {@link CommandOverlay}
	 * @param xtra the extra information to add
	 */
	public void setExtra(String xtra){
		extra = xtra;
	}

	public String getExtra(){
		if (extra == null)
			return "";
		return extra;
	}

	/**
	 * Sets the threshold at which this command is triggered when in tour mode. 
	 * @param threshold a float with the distance in meters
	 */
	public void setThreshold(float threshold) {
		this.threshold = threshold; 			
	}

	public String getCommand(){
		return command;
	}

	public void setCommand(String cmd){
		command = cmd;
	}

	/**
	 * Compares the {@link CommandOverlay}s position with the provided {@link Location} to determine if it is 
	 * withing the threshold
	 * @param loc The location to compare with
	 * @return true if a hit is registered false otherwise
	 */
	public boolean hit(Location loc){
		double lat = mPoint.getLatitudeE6()/1E6d;
		double lon = mPoint.getLongitudeE6()/1E6d;
		Location tmp = new Location("none");
		tmp.setLatitude(lat);
		tmp.setLongitude(lon);
		
		return tmp.distanceTo(loc)<threshold;
	}

	public float getThreshold(){
		return threshold;
	}

	public int getId(){
		return id;
	}

	@Override
	public boolean equals(Object obj){
		if(obj instanceof CommandOverlay)
			return ((CommandOverlay)obj).getId() == id;
		return false;
	}

	/**
	 * Returns a string of the command
	 * The string is formatted in such a way that it can be sent to the puppet device
	 */
	@Override
	public String toString(){
		return (command + " " + extra).trim();
	}

	/**
	 * A function used before we started to use xml
	 * @return A comma separated string with all the fields
	 */
	public String toFileString(){
		//TODO This might not be needed any more
		return command + ", " + extra + ", " + mPoint.getLatitudeE6() + ", " + mPoint.getLongitudeE6();
	}
}

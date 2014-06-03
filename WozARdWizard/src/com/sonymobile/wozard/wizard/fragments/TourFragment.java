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
package com.sonymobile.wozard.wizard.fragments;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.sonymobile.wozard.wizard.ControllerActivity;
import com.sonymobile.wozard.wizard.R;
import com.sonymobile.wozard.wizard.TourService;
import com.sonymobile.wozard.wizard.ControllerActivity.ControllerCallback;
import com.sonymobile.wozard.wizard.TourService.TourServiceCallback;
import com.sonymobile.wozard.wizard.utils.CommandOverlay;
import com.sonymobile.wozard.wizard.utils.CommandParser;
import com.sonymobile.wozard.wizard.utils.ImageHelper;
import com.sonymobile.wozard.wizard.utils.Settings;
import com.sonymobile.wozard.wizard.utils.Util;

/**
 * This fragment is used for the Tours.
 */
public class TourFragment extends Fragment implements OnClickListener,
        ServiceConnection, TourServiceCallback, ControllerCallback {
	private View view;
	private MapView mapView;
	private MapController mapController;
	private EventOverlay events;
	private TextView longitudeText;
	private TextView latitudeText;
	private TextView fileText;
	private EditText thresholdEditor;
	private EditText commandEditor;
	private EditText extraEditor;
	private TextView mConnectedStatusView;
	private CommandOverlay currentItem = null;
	private PositionOverlay position;
	public static final String TAG = "TourFragment";
	private GeoPoint currentLocation;
	private TourService tourService;
	private Handler handler;
	private String extra;
	private String command;
	private FrameLayout expansionFrame;
	private ArrayAdapter<String> adapter;
	private static final String[] cmds = {"Clear screen","Show image", "Play Sound", "Play video"};

	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private String pictureFileName;
	private TextView pathText;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		currentLocation = new GeoPoint(55718253, 13225539);
		Intent bindintent = new Intent(getActivity(), TourService.class);
		getActivity().startService(bindintent);
		getActivity().bindService(bindintent, this, Context.BIND_AUTO_CREATE);
		adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_smaller_layout, cmds);
		adapter.setDropDownViewResource(R.layout.tour_spinner_layout);
		handler = new Handler();
		Util.preparePath(Settings.DEFAULT_CAMERAPATH);
		ControllerActivity.registerCallback(this);
		Util.actionBarSetup(getActivity(), false, R.string.tourTitle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		if(view == null){
			view = inflater.inflate(R.layout.tour_layout, container, false);
			mapView = (MapView) view.findViewById(R.id.myGMap);

			events = new EventOverlay(getActivity().getResources().getDrawable(R.drawable.positionmarker));
			Settings settings = Settings.getInstance();
			mapView.getOverlays().add(new UnselectOverlay());
			if(settings.getValue(Settings.TOUR_KEY) != null)
				events.readFile((String)settings.getValue(Settings.TOUR_KEY));
			mapView.getOverlays().add(events);
			position = new PositionOverlay(55718253, 13225539);
			mapView.getOverlays().add(position);
			ControllerActivity.sendCommandToPuppet(ControllerActivity.REQ_COORDINATES_CMD,
					null,  null,  null);
			mapController = mapView.getController();
			longitudeText = (TextView)view.findViewById(R.id.longitude_text);
			latitudeText = (TextView)view.findViewById(R.id.latitude_text);
			fileText = (TextView)view.findViewById(R.id.file_text);
			fileText.setText(getFileName((String)settings.getValue(Settings.TOUR_KEY)));
			expansionFrame = (FrameLayout)view.findViewById(R.id.tour_frame);
			view.findViewById(R.id.gps_box).setOnClickListener(this);
			view.findViewById(R.id.start_tour_button).setOnClickListener(this);
			CheckBox tmp = ((CheckBox)view.findViewById(R.id.gps_box));
			if(tmp != null && Settings.getInstance().getValue(Settings.GPS_KEY) != null){
				tmp.setChecked((Boolean)Settings.getInstance().getValue(Settings.GPS_KEY));
			}
			Location tmploc = new Location("none");
			tmploc.setLatitude(55.718253);
			tmploc.setLongitude(13.225539);
			updatePosition(tmploc);
			mapController.animateTo(currentLocation);
			mapController.setZoom(17);

			mConnectedStatusView = (TextView) view.findViewById(R.id.connection_text);
			if (mConnectedStatusView != null) {
			    mConnectedStatusView.setBackgroundColor(Color.RED);
			    mConnectedStatusView.setText("Not connected to any player");
			}
		}
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mConnectedStatusView != null) {
			Util.updateConnectionStatusView(getActivity(), mConnectedStatusView,
				ControllerActivity.isConnected());
		}
		Util.updatePreviewImage(getActivity());
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
        ControllerActivity.unRegisterCallback(this);
        getActivity().unbindService(this);
        tourService = null;
	}

	@Override
	public void onClick(View v){
		switch(v.getId()){
		case R.id.save_button:
			if(currentItem != null){
				try{
					String cmd = commandEditor.getText().toString();
					String extra = extraEditor.getText().toString();
					float threshold = Float.parseFloat(thresholdEditor.getText().toString());
					currentItem.setCommand(cmd);
					currentItem.setExtra(extra);
					currentItem.setThreshold(threshold);
				}catch(NumberFormatException e){
					Toast.makeText(getActivity(), "The format of the threshold appears to be wrong", Toast.LENGTH_SHORT).show();
				}catch(NullPointerException e){
					Toast.makeText(getActivity(), "Something appears to have gone wrong", Toast.LENGTH_SHORT).show();
				}
				String fileName;
				if((fileName = (String)Settings.getInstance().getValue(Settings.TOUR_KEY)) != null)
					events.printToFile(fileName);
				mapView.invalidate();
			}
			unselect();
			break;
		case R.id.delete_button:
			if(currentItem != null){
				events.remove(currentItem);
				currentItem = null;
				String fileName;
				if((fileName = (String)Settings.getInstance().getValue(Settings.TOUR_KEY)) != null)
					events.printToFile(fileName);
				mapView.invalidate();
				unselect();
			}
			break;
		case R.id.toggle_button:
			events.toogle();
			mapView.invalidate();
			break;
		case R.id.send_button:
			sendMap();
			break;

		case R.id.new_marker_button:
			CommandOverlay cmd = new CommandOverlay(currentLocation, "walkpoint", null, command);
			if(command.equals(ControllerActivity.PLAY_SOUND_CMD) || command.equals(ControllerActivity.SHOW_VIDEO_CMD))
				cmd.setExtra(((EditText)getView().findViewById(R.id.path_edit)).getText().toString());
			else
				cmd.setExtra(extra);
			try{
				cmd.setThreshold(Float.parseFloat(thresholdEditor.getText().toString()));
			}catch(NumberFormatException e){
				cmd.setThreshold(20f);
				Toast.makeText(getActivity(), "Caught a numberformatexception while parsing the threshold\n"
						+ "Threshold set to 20 m", Toast.LENGTH_SHORT).show();
			}
			events.addOverlay(cmd);
			mapView.invalidate();
			if(Settings.getInstance().getValue(Settings.TOUR_KEY) != null)
				events.printToFile((String)Settings.getInstance().getValue(Settings.TOUR_KEY));
			unselect();
			break;

		case R.id.gps_box:
			if( ((CheckBox)view.findViewById(R.id.gps_box)).isChecked()){
				Settings.getInstance().putValue(Settings.GPS_KEY, Boolean.valueOf(true));
				initGPS();
			}else{
				Settings.getInstance().putValue(Settings.GPS_KEY, Boolean.valueOf(false));
				disableGPS();
			}
			break;
		case R.id.start_tour_button:
			if(tourService != null){
				if(tourService.tourModeOn()){
					expandCreationMode();
					tourService.stopTourMode();
					((Button)(getView().findViewById(R.id.start_tour_button))).setText(getResources().getString(R.string.start_tour_button));
				} else {
					expandTourMode();
					tourService.startTourMode();
					((Button)(getView().findViewById(R.id.start_tour_button))).setText(getResources().getString(R.string.disable_tour_button));

				}
			}
			break;

		case R.id.takePictureButtonTour:
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, getImageUri());
			startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			break;
		default:

		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				pathText.setText(pictureFileName);
				extra = pictureFileName;
			} else if (resultCode == Activity.RESULT_CANCELED) {
				// User cancelled the image capture
			} else {
				// Image capture failed, advise user
			}
		}
	}

	/**
	 * Creates the file where the camera application will save the picture
	 * @return the location to save a photo
	 */
	private Uri getImageUri() {
		Date d = new Date(System.currentTimeMillis());
		SimpleDateFormat df = new SimpleDateFormat("MM-dd-HHmmss", Locale.GERMAN);
		pictureFileName = Settings.DEFAULT_CAMERAPATH + "/pic"
				+ df.format(d)
				+ ".jpg";
		File file = new File(pictureFileName);
		Uri imgUri = Uri.fromFile(file);

		return imgUri;
	}

	/**
	 * used to unselect an overlay
	 */
	protected void unselect(){
		if(tourService != null){
			if(tourService.tourModeOn())
				expandTourMode();
			else
				expandCreationMode();
		}
		currentItem = null;
	}

	/**
	 * Used when the fragment is in a "create marker"-state
	 */
	private void expandCreationMode(){
		expansionFrame.removeAllViews();
		LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.creation_layout, null);
		thresholdEditor = (EditText)v.findViewById(R.id.threshold_edit);
		thresholdEditor.setText("20");

		v.findViewById(R.id.new_marker_button).setOnClickListener(this);

		((Spinner)v.findViewById(R.id.command_spinner)).setAdapter(adapter);
		((Spinner)v.findViewById(R.id.command_spinner)).setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
					int position, long id) {
				View v = TourFragment.this.getView();
				LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				((FrameLayout)v.findViewById(R.id.command_expander)).removeAllViews();
				FrameLayout expander = (FrameLayout)v.findViewById(R.id.command_expander);

				switch((int)id){
				case 0:
					command = ControllerActivity.CLEAR_INDICATOR_CMD;
					break;
				case 1:
					command = ControllerActivity.SHOW_IMAGE_CMD;
					RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.take_picture_layout, null);
					pathText = (TextView) layout.findViewById(R.id.picturePath);
					layout.findViewById(R.id.takePictureButtonTour).setOnClickListener(TourFragment.this);
					expander.addView(layout);

					break;
				case 2:
					command = ControllerActivity.PLAY_SOUND_CMD;
					LinearLayout ll = (LinearLayout)inflater.inflate(R.layout.sound_command_layout, null);
					((TextView)ll.findViewById(R.id.type_label)).setText(getResources().getString(R.string.sound_label));
					expander.addView(ll);
					//sound
					break;
				case 3:
					command = ControllerActivity.SHOW_VIDEO_CMD;
					ll = (LinearLayout)inflater.inflate(R.layout.sound_command_layout, null);
					((TextView)ll.findViewById(R.id.type_label)).setText(getResources().getString(R.string.video_label));
					expander.addView(ll);
					//video
					break;
				case 4:
					//notification
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		expansionFrame.addView(v);
	}

	/**
	 * This function expands the view used to edit tour points.
	 * it is expanded as soon as you press an overlay on the map.
	 */
	private void expandEditMode(CommandOverlay item){
		currentItem = item;
		expansionFrame.removeAllViews();
		LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.edit_layout, null);
		v.findViewById(R.id.save_button).setOnClickListener(this);
		v.findViewById(R.id.delete_button).setOnClickListener(this);
		thresholdEditor = (EditText)v.findViewById(R.id.threshold_edit);
		commandEditor = (EditText)v.findViewById(R.id.command_edit);
		extraEditor = (EditText)v.findViewById(R.id.extra_edit);
		extraEditor.setText(item.getExtra());
		commandEditor.setText(item.getCommand());
		thresholdEditor.setText(Float.toString(item.getThreshold()));
		expansionFrame.addView(v);
	}

	/**
	 * This function expands the view used when the tour is active and no overlay is selected.
	 */
	private void expandTourMode(){
		expansionFrame.removeAllViews();
		LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.tour_mode_layout, null);
		v.findViewById(R.id.toggle_button).setOnClickListener(this);
		v.findViewById(R.id.send_button).setOnClickListener(this);
		expansionFrame.addView(v);
		thresholdEditor = null;
		extraEditor = null;
		commandEditor = null;
	}

	/**
	 * Called when the wizard is supposed to use its own gps instead of the puppets
	 */
	private void initGPS(){
		if(tourService != null)
			tourService.initGPS();
	}

	/**
	 * Called to unregister the tourservice from location updates.
	 */
	private void disableGPS(){
		if(tourService != null)
			tourService.disableGPS();
	}

	/**
	 * Sends an image of the map to the puppet device.
	 */
	private void sendMap(){
		mapView.setWillNotCacheDrawing(false);
		mapView.destroyDrawingCache();
		mapView.buildDrawingCache();
		Bitmap map = Bitmap.createBitmap(mapView.getDrawingCache());
		map = ImageHelper.getRoundedCornerBitmap(map, 60);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		map.compress(CompressFormat.PNG, 0, os);
		ControllerActivity.sendScreenToPuppet(os.toByteArray());
		ControllerActivity.sendCommandToPuppet(ControllerActivity.GET_SCREEN_CMD,
				null, null, null);
	}

	/**
	 * Call this function to set the point on the map. It also changes the values of the textviews.
	 * @param loc the new Location
	 */
	public void updatePosition(final Location loc){
		handler.post(new Runnable(){
			public void run(){
				currentLocation = new GeoPoint((int)(loc.getLatitude()*1E6), (int)(loc.getLongitude()*1E6));
				mapView.getController().animateTo(currentLocation);
				longitudeText.setText(Double.toString(loc.getLongitude()));
				latitudeText.setText(Double.toString(loc.getLatitude()));
				position.updatePosition(currentLocation);

				mapView.invalidate();
			}
		});
	}

	/**
	 * The itemized overlay subclass that holds the commands on the map
	 */
	private class EventOverlay extends ItemizedOverlay<CommandOverlay>{
		protected CopyOnWriteArrayList<CommandOverlay> overlays;
		private boolean hidden = false;
		//		private Context context;
		public EventOverlay(Drawable img) {
			super(boundCenterBottom(img));
		}
		
		/**
		 * Used to read in the {@link CommandOverlay}s from a file
		 * @param filename the path to the file to read
		 */
		public void readFile(String filename){
			overlays = CommandParser.parseTour(filename);
			if(tourService != null)
				tourService.setCommands(overlays);
			populate();
		}

		@Override
		protected CommandOverlay createItem(int i) {
			if(i >= overlays.size() || hidden)
				return null;
			return overlays.get(i);
		}


		public void addOverlay(CommandOverlay item){
			if(overlays == null)
				overlays = new CopyOnWriteArrayList<CommandOverlay>();
			overlays.add(item);
			populate();
		}

		@Override
		public int size() {
			if(hidden)
				return 0;
			return overlays.size();
		}

		@Override
		public boolean onTap(int index){
			if(index >= overlays.size())
				return false;
			CommandOverlay item = overlays.get(index);
			expandEditMode(item);
			return true;
		}

		public boolean remove(CommandOverlay item){
			boolean res = overlays.remove(item);
			setLastFocusedIndex(-1);
			populate();
			return res;
		}
		/**
		 * A function that is used to hide the {@link CommandOverlay}s on the mapview
		 */
		public void toogle(){
			hidden = !hidden;
			setLastFocusedIndex(-1);
			populate();
		}
		
		/**
		 * When this is called the {@link CommandOverlay}s is saved in the file specified by the filename
		 * @param filename path to the file to save in
		 */
		public void printToFile(String filename){
			CommandParser.printCommand(filename, overlays);
		}
	}

	/**
	 * Used to keep track of the puppet devices position
	 */
	private class PositionOverlay extends Overlay{
		private GeoPoint position;
		public PositionOverlay(int latitude, int longitude){
			position = new GeoPoint(latitude, longitude);
		}

		public void updatePosition(GeoPoint newPos){
			position = newPos;
		}

		@Override
		public void draw(Canvas canvas, MapView view, boolean shadow){
			super.draw(canvas, view, shadow);

			Paint paint = new Paint();
			Point myScreenCoords = new Point();
			mapView.getProjection().toPixels(position, myScreenCoords);

			paint.setStrokeWidth(10);
			paint.setARGB(0xff, 0x00, 0x00, 0xFF);
			paint.setStyle(Paint.Style.FILL);
			canvas.drawPoint(myScreenCoords.x, myScreenCoords.y, paint);
		}
	}

	/**
	 * An overlay used to register taps on the map to unselect items currently selected
	 */
	private class UnselectOverlay extends Overlay{
		public boolean onTap(GeoPoint gp,MapView mv){
			unselect();
			return true;
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		tourService = ((TourService.LocalBinder)service).getService();
		tourService.setCallback(this);
		Button b = 	(Button)getView().findViewById(R.id.start_tour_button);
		if(tourService.tourModeOn()){
			expandTourMode();
			b.setText(getResources().getString(R.string.disable_tour_button));
		} else {
			expandCreationMode();
			b.setText(getResources().getString(R.string.start_tour_button));
		}
		tourService.setCommands(events.overlays);
		if(Settings.getInstance().getValue(Settings.GPS_KEY) != null){
			tourService.setSelfAsSource((Boolean)Settings.getInstance().getValue(Settings.GPS_KEY));
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		tourService = null;
	}

	/**
	 * Called when the user selects another file to read from in the menu
	 */
	public void updateFile(){
		String fileName = (String)Settings.getInstance().getValue(Settings.TOUR_KEY);
		events.readFile(fileName);
		fileText.setText(getFileName(fileName));
	}

	/**
	 * Helper function to extract the filename from the path.
	 * @param path to the file
	 * @return the filename
	 */
	private String getFileName(String path){
		return "File:" + path.substring(Settings.DEFAULT_ROOT_TOUR.length(), path.length());
	}

	@Override
	public void onConnected(boolean isConnected) {
		if (mConnectedStatusView != null) {
			Util.updateConnectionStatusView(getActivity(), mConnectedStatusView, isConnected);
		}
	}

	@Override
	public void onNewPreviewImage(Bitmap previewImage) {
		Util.previewImage(getActivity(), previewImage);
	}

	@Override
	public void puppetVideoSeeThroughDevice(boolean videoSeeThroughDevice) {
		// Not used
	}
}

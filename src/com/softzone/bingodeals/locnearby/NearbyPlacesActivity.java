package com.softzone.bingodeals.locnearby;

import android.os.Bundle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.app.Dialog;
import android.content.Intent;

import android.os.AsyncTask;

import android.support.v4.app.FragmentActivity;
import android.util.Log;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.softzone.loctracker.GPSTracker;
import com.softzone.parser.PlaceJSONParser;
import com.softzone.bingodeals.R;
import com.softzone.bingodeals.R.drawable;
import com.softzone.bingodeals.livemsg.LiveSitesActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * 
 * show every near by site in the map and
 * start NearSitesActivity
 */

public class NearbyPlacesActivity extends FragmentActivity {

	// google map
	GoogleMap map;

	// spinner for palce types
	Spinner mSprPlaceType;

	// string array for keep place types
	String[] mPlaceType = null;
	String[] mPlaceTypeName = null;

	// to keep particular type
	public static String type;

	public static List<HashMap<String, String>> places;

	double mLatitude = 0;
	double mLongitude = 0;

	GPSTracker gps;

	Button btnFind;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.nearby_sites);

		gps = new GPSTracker(NearbyPlacesActivity.this);

		if (gps.canGetLocation()) {
			mLatitude = gps.getLatitude();
			mLongitude = gps.getLongitude();

		} else {
			// can't get location
			// GPS or Network is not enabled
			gps.showSettingsAlert();
		}

		// Array of place types
		mPlaceType = getResources().getStringArray(R.array.place_type);

		// Array of place type names
		mPlaceTypeName = getResources().getStringArray(R.array.place_type_name);

		// each category has image
		int images[] = { R.drawable.cafe, R.drawable.food, R.drawable.movie,
				R.drawable.shopping };

		// Each row in the list stores category and image
		List<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

		// set the map
		for (int i = 0; i < 4; i++) {
			HashMap<String, String> hm = new HashMap<String, String>();
			hm.put("type", mPlaceTypeName[i]);
			hm.put("image", Integer.toString(images[i]));
			list.add(hm);
		}

		// Keys used in Hashmap
		String[] keys = { "type", "image" };

		// Ids of views in listview_layout
		int[] ids = { R.id.name, R.id.list_image };

		// Instantiating an adapter to store each items
		SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), list,
				R.layout.list_category_map, keys, ids);

		// Getting reference to the Spinner
		mSprPlaceType = (Spinner) findViewById(R.id.spr_place_type);

		// set the adapter to spinner
		mSprPlaceType.setAdapter(adapter);

		/*
		 * btn initiating
		 */

		// Getting reference to Find Button
		btnFind = (Button) findViewById(R.id.btn_find);

		// Getting Google Play availability status
		int status = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getBaseContext());

		if (status != ConnectionResult.SUCCESS) { // Google Play Services are
													// not available

			int requestCode = 10;
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this,
					requestCode);
			dialog.show();

		} else { // Google Play Services are available

			// Getting reference to the SupportMapFragment
			SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map);

			// Getting Google Map
			map = fragment.getMap();

			LatLng latLng = new LatLng(mLatitude, mLongitude);
			// focus the camera on my location
			map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
			map.animateCamera(CameraUpdateFactory.zoomTo(12));

			// Enabling MyLocation in Google Map
			map.setMyLocationEnabled(true);

			// add the marker on my location
			MarkerOptions options_1 = new MarkerOptions();
			options_1.position(latLng);
			options_1.icon(BitmapDescriptorFactory
					.fromResource(drawable.current_location));
			map.addMarker(options_1);

			// Setting click event lister for the find button
			btnFind.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					places = null;

					int selectedPosition = mSprPlaceType
							.getSelectedItemPosition();
					type = mPlaceType[selectedPosition];

					// append data to send http request
					StringBuilder sb = new StringBuilder(
							"https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
					sb.append("location=" + mLatitude + "," + mLongitude);
					System.out
							.println("*mLatitude + mLongitude in nearbyplacesActivity   "
									+ mLatitude + "," + mLongitude);
					sb.append("&radius=5000");
					sb.append("&types=" + type);
					System.out.println("*type in nearbyplacesActivity   "
							+ type);
					sb.append("&sensor=true");
					//append google api key
					sb.append("&key=AIzaSyBw4g9sfCRP4kViAE9TNBWpmAXkOzjPn-k");

					// Creating a new non-ui thread task to download Google
					// place json data
					PlacesTask placesTask = new PlacesTask();

					// Invokes the "doInBackground()" method of the class
					// PlaceTask
					placesTask.execute(sb.toString());

					// Launching Near sites Activity
					Intent i = new Intent(getApplicationContext(),
							NearSitesActivity.class);
					startActivity(i);

				}
			});

		}

	}

	/** A method to download json data from url */
	private String downloadUrl(String strUrl) throws IOException {
		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(strUrl);

			// Creating an http connection to communicate with url
			urlConnection = (HttpURLConnection) url.openConnection();

			// Connecting to url
			urlConnection.connect();

			// Reading data from url
			iStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					iStream));

			StringBuffer sb = new StringBuffer();

			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			data = sb.toString();

			br.close();

		} catch (Exception e) {
			Log.d("Exception while downloading url", e.toString());
		} finally {
			iStream.close();
			urlConnection.disconnect();
		}

		return data;
	}

	/** A class, to download Google Places */
	private class PlacesTask extends AsyncTask<String, Integer, String> {

		String data = null;

		// Invoked by execute() method of this object
		@Override
		protected String doInBackground(String... url) {
			try {
				data = downloadUrl(url[0]);
			} catch (Exception e) {
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		// Executed after the complete execution of doInBackground() method
		@Override
		protected void onPostExecute(String result) {
			ParserTask parserTask = new ParserTask();

			// Start parsing the Google places in JSON format
			// Invokes the "doInBackground()" method of the class ParseTask
			parserTask.execute(result);
		}

	}

	/** A class to parse the Google Places in JSON format */
	private class ParserTask extends
			AsyncTask<String, Integer, List<HashMap<String, String>>> {

		JSONObject jObject;

		// Invoked by execute() method of this object
		@Override
		protected List<HashMap<String, String>> doInBackground(
				String... jsonData) {

			places = null;
			PlaceJSONParser placeJsonParser = new PlaceJSONParser();

			try {
				jObject = new JSONObject(jsonData[0]);

				/** Getting the parsed data as a List construct */
				places = placeJsonParser.parse(jObject);

			} catch (Exception e) {
				Log.d("Exception", e.toString());
			}

			return places;
		}

		// Executed after the complete execution of doInBackground() method
		@Override
		protected void onPostExecute(List<HashMap<String, String>> list) {

			try {

				// Clears all the existing markers
				map.clear();

				Marker marker;

				for (int i = 0; i < list.size(); i++) {

					// Getting a place from the places list
					HashMap<String, String> hmPlace = list.get(i);

					// Getting latitude of the place
					double lat = Double.parseDouble(hmPlace.get("lat"));

					// Getting longitude of the place
					double lng = Double.parseDouble(hmPlace.get("lng"));

					// Getting name
					String name = hmPlace.get("place_name");

					// Getting vicinity
					String vicinity = hmPlace.get("vicinity");

					LatLng latLng = new LatLng(lat, lng);

					// Setting the position for the marker
					// markerOptions.position(latLng);

					String place_id = hmPlace.get("place_id");

					// getting res ID
					String siteName = name;
					String sName = siteName.replaceAll("\\s+", "");
					String sn = sName.toLowerCase();
					System.out.println("iName is: " + sn);
					String sDrawableName = sn;

					if (getResources().getIdentifier(sDrawableName, "drawable",
							getPackageName()) == 0) {
						marker = map
								.addMarker(new MarkerOptions()

										.position(latLng)
										.title(name)
										.snippet(vicinity)
										.icon(BitmapDescriptorFactory
												.defaultMarker(BitmapDescriptorFactory.HUE_RED))

								);
					}

					else {

						int resID = getResources().getIdentifier(sDrawableName,
								"drawable", getPackageName());
						marker = map.addMarker(new MarkerOptions()

								.position(latLng)
								.title(name)
								.snippet(vicinity)
								.icon(BitmapDescriptorFactory
										.fromResource(resID))

						);
					}

				}

			} catch (NullPointerException e) {
				System.out.println("No Places Nearby !!!");
			}

		}

	}

}

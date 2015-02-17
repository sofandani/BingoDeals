package com.softzone.bingodeals.locnearby;

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

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import com.softzone.loctracker.GPSTracker;
import com.softzone.parser.DirectionsJSONParser;
import com.softzone.bingodeals.R;
import com.softzone.bingodeals.R.drawable;
import com.softzone.bingodeals.dbactivity.SiteDetailActivity;

/**
 * 
 * show the route to the given location from my location
 */

public class RouteMapActivity extends FragmentActivity {

	// my latitude and longitude
	double mLatitude = 0;
	double mLongitude = 0;

	// given place latitude and longitude
	double nLatitude = 0;
	double nLongitude = 0;

	String nName, nVicinity;

	// to get location (gps or network)
	GPSTracker gps;

	// google map
	GoogleMap map;

	// Lat and Long for my loc and places loc
	LatLng currentLocation;
	LatLng placeLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.route_map);

		gps = new GPSTracker(RouteMapActivity.this);

		if (gps.canGetLocation()) {
			mLatitude = gps.getLatitude();
			mLongitude = gps.getLongitude();

			nLatitude = SiteDetailActivity.nLatitude;
			nLongitude = SiteDetailActivity.nLongitude;
			nName = SiteDetailActivity.nName;
			nVicinity = SiteDetailActivity.nVicinity;

		} else {
			// can't get location
			// GPS or Network is not enabled
			gps.showSettingsAlert();
		}

		// Initializing locations
		currentLocation = new LatLng(mLatitude, mLongitude);
		placeLocation = new LatLng(nLatitude, nLongitude);

		// Getting reference to SupportMapFragment of the activity_main
		SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);

		// Getting Map for the SupportMapFragment
		map = fm.getMap();

		if (map != null) {

			LatLng latLng = new LatLng(mLatitude, mLongitude);
			map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
			map.animateCamera(CameraUpdateFactory.zoomTo(12));

			// Enable MyLocation Button in the Map
			map.setMyLocationEnabled(true);

			LatLng origin = currentLocation;
			LatLng dest = placeLocation;

			// marker of the my location
			MarkerOptions options_1 = new MarkerOptions();
			options_1.position(currentLocation);
			options_1.icon(BitmapDescriptorFactory
					.fromResource(drawable.current_location));
			map.addMarker(options_1);

			Marker marker;

			// getting res ID
			String siteName = nName;
			String sName = siteName.replaceAll("\\s+", "");
			String sn = sName.toLowerCase();
			System.out.println("iName is: " + sn);
			String sDrawableName = sn;

			// other locations
			if (getResources().getIdentifier(sDrawableName, "drawable",
					getPackageName()) == 0) {
				marker = map
						.addMarker(new MarkerOptions()

								.position(placeLocation)
								.title(nName)
								.snippet(nVicinity)
								.icon(BitmapDescriptorFactory
										.defaultMarker(BitmapDescriptorFactory.HUE_RED))

						);
			}

			else {

				int resID = getResources().getIdentifier(sDrawableName,
						"drawable", getPackageName());
				marker = map.addMarker(new MarkerOptions()

				.position(placeLocation).title(nName).snippet(nVicinity)
						.icon(BitmapDescriptorFactory.fromResource(resID))

				);
			}

			// Getting URL to the Google Directions API
			String url = getDirectionsUrl(origin, dest);

			DownloadTask downloadTask = new DownloadTask();

			// Start downloading json data from Google Directions API
			downloadTask.execute(url);

		}
	}

	private String getDirectionsUrl(LatLng origin, LatLng dest) {

		// Origin of route
		String str_origin = "origin=" + origin.latitude + ","
				+ origin.longitude;

		// Destination of route
		String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

		// Sensor enabled
		String sensor = "sensor=false";

		// Building the parameters to the web service
		String parameters = str_origin + "&" + str_dest + "&" + sensor;

		// Output format
		String output = "json";

		// Building the url to the web service
		String url = "https://maps.googleapis.com/maps/api/directions/"
				+ output + "?" + parameters;

		return url;
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

	// Fetches data from url passed
	private class DownloadTask extends AsyncTask<String, Void, String> {

		// Downloading data in non-ui thread
		@Override
		protected String doInBackground(String... url) {

			// For storing data from web service
			String data = "";

			try {
				// Fetching the data from web service
				data = downloadUrl(url[0]);
			} catch (Exception e) {
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		// Executes in UI thread, after the execution of
		// doInBackground()
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			ParserTask parserTask = new ParserTask();

			// Invokes the thread for parsing the JSON data
			parserTask.execute(result);
		}
	}

	/** A class to parse the Google Places in JSON format */
	private class ParserTask extends
			AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

		// Parsing the data in non-ui thread
		@Override
		protected List<List<HashMap<String, String>>> doInBackground(
				String... jsonData) {

			JSONObject jObject;
			List<List<HashMap<String, String>>> routes = null;

			try {
				jObject = new JSONObject(jsonData[0]);
				DirectionsJSONParser parser = new DirectionsJSONParser();

				// Starts parsing data
				routes = parser.parse(jObject);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return routes;
		}

		// Executes in UI thread, after the parsing process
		@Override
		protected void onPostExecute(List<List<HashMap<String, String>>> result) {
			ArrayList<LatLng> points = null;
			PolylineOptions lineOptions = null;
			MarkerOptions markerOptions = new MarkerOptions();

			// Traversing through all the routes
			for (int i = 0; i < result.size(); i++) {
				points = new ArrayList<LatLng>();
				lineOptions = new PolylineOptions();

				// Fetching i-th route
				List<HashMap<String, String>> path = result.get(i);

				// Fetching all the points in i-th route
				for (int j = 0; j < path.size(); j++) {
					HashMap<String, String> point = path.get(j);

					double lat = Double.parseDouble(point.get("lat"));
					double lng = Double.parseDouble(point.get("lng"));
					LatLng position = new LatLng(lat, lng);

					points.add(position);
				}

				// Adding all the points in the route to LineOptions
				lineOptions.addAll(points);
				lineOptions.width(5);
				lineOptions.color(Color.BLUE);
			}

			// Drawing polyline in the Google Map for the i-th route
			map.addPolyline(lineOptions);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
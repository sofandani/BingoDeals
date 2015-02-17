package com.softzone.bingodeals.locnearby;

import com.softzone.bingodeals.R;
//import com.softzone.bingodeals.dbactivity.NewSiteActivity;
import com.softzone.bingodeals.dbactivity.SiteDetailActivity;

import com.softzone.parser.JSONParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * 
 * show the list of sites which is in nearby and database
 */

public class NearSitesActivity extends ListActivity {

	// Progress Dialog
	private ProgressDialog pDialog;

	// Creating JSON Parser object
	JSONParser jParser = new JSONParser();

	ArrayList<HashMap<String, String>> sitesList;

	// url to get all sites list
	private static String url_all_sites = "http://www.sweetandnicecakes.com/android_connect/get_all_sites.php";

	// JSON Node names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_SITES = "sites";
	private static final String TAG_LID = "lid";
	private static final String TAG_NAME = "name";
	private static final String TAG_GID = "gid";
	private static final String TAG_VICINITY = "vicinity";
	private static final String TAG_IMAGE = "image";

	// sites JSONArray
	JSONArray sites = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.common_list);

		// Hashmap for ListView
		sitesList = new ArrayList<HashMap<String, String>>();

		// Loading sites in Background Thread
		new LoadAllSites().execute();

		// Get listview
		ListView lv = getListView();

		// on seleting single site
		// launching Edit site Screen
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// getting values from selected ListItem
				String lid = ((TextView) view.findViewById(R.id.lid)).getText()
						.toString();

				// Starting new intent
				Intent in = new Intent(getApplicationContext(),
						SiteDetailActivity.class);
				// sending lid to next activity
				in.putExtra(TAG_LID, lid);

				// starting new activity and expecting some response back
				startActivityForResult(in, 100);
			}
		});

	}

	/**
	 * Background Async Task to Load all sites by making HTTP Request
	 * */
	class LoadAllSites extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(NearSitesActivity.this);
			pDialog.setMessage("Loading sites. Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting All sites from url
		 * */
		protected String doInBackground(String... args) {
			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			// getting JSON string from URL
			JSONObject json = jParser.makeHttpRequest(url_all_sites, "GET",
					params);

			// Check your log cat for JSON reponse
			Log.d("All Sites: ", json.toString());

			try {
				// Checking for SUCCESS TAG
				int success = json.getInt(TAG_SUCCESS);

				if (success == 1) {
					// sites found
					// Getting Array of sites
					sites = json.getJSONArray(TAG_SITES);

					// looping through All sites
					for (int i = 0; i < sites.length(); i++) {
						JSONObject c = sites.getJSONObject(i);

						// Storing each json item in variable
						String id = c.getString(TAG_LID);
						String name = c.getString(TAG_NAME);

						String gid = c.getString(TAG_GID);

						String vicinity = c.getString(TAG_VICINITY);

						// image - uri of the image
						String image = makeImageUri(name);

						// creating new HashMap
						HashMap<String, String> map = new HashMap<String, String>();

						// adding each child node to HashMap key => value
						map.put(TAG_LID, id);
						map.put(TAG_NAME, name);

						map.put(TAG_GID, gid);

						map.put(TAG_VICINITY, vicinity);

						map.put(TAG_IMAGE, image);

						// adding HashList to ArrayList
						sitesList.add(map);
					}

				} else {
					/*
					 * if no sites in database
					 */
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all sites
			pDialog.dismiss();
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {

					try {

						// sleep for 3 seconds while downloading data
						Thread.sleep(3000);

						// get the sitesList
						// if sites were not in NearbyPlacesActivity.places
						// remove the site

						for (int i = 0; i < sitesList.size(); i++) {
							int x = 0;
							for (int j = 0; j < NearbyPlacesActivity.places
									.size(); j++) {

								if (sitesList
										.get(i)
										.get("gid")
										.equals(NearbyPlacesActivity.places
												.get(j).get("place_id"))) {

									x++;

								}
							}
							if (x == 0) {
								sitesList.remove(i);
								i--;
							}
						}

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					/**
					 * Updating parsed JSON data into ListView
					 * */
					ListAdapter adapter = new SimpleAdapter(
							NearSitesActivity.this, sitesList,
							R.layout.list_site_near, new String[] { TAG_LID,
									TAG_NAME, TAG_VICINITY, TAG_IMAGE },
							new int[] { R.id.lid, R.id.name, R.id.vicinity,
									R.id.list_image });
					// updating listview
					setListAdapter(adapter);

				}
			});

		}

	}

	// make image uri to string
	private String makeImageUri(String name) {
		String image = name;
		// remove all white spaces
		String in = image.replaceAll("\\s+", "");
		// turn to lower case
		String iname = in.toLowerCase();
		System.out.println("iName is: " + iname);
		String mDrawableName = iname;
		// get the resId of the image
		int resID = getResources().getIdentifier(mDrawableName, "drawable",
				getPackageName());

		// resID is notfound show default image
		if (resID == 0) {
			resID = getResources().getIdentifier("default_place", "drawable",
					getPackageName());
		}

		// make the uri
		Uri imageURI = Uri.parse("android.resource://" + getPackageName() + "/"
				+ resID);
		image = imageURI.toString();
		return image;
	}

}

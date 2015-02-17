package com.softzone.bingodeals.livemsg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.softzone.parser.JSONParser;
import com.softzone.sqlitedb.DatabaseHandler;
import com.softzone.sqlitedb.LiveMessage;
import com.softzone.bingodeals.R;
//import com.softzone.bingodeals.dbactivity.NewSiteActivity;
import com.softzone.bingodeals.dbactivity.SiteDetailActivity;
import com.softzone.bingodeals.locnearby.NearSitesActivity;
import com.softzone.bingodeals.locnearby.NearbyPlacesActivity;
import com.softzone.bingodeals.locnearby.RouteMapActivity;

/*
 * get the info from live message
 * and show it
 */

public class LiveSitesActivity extends Activity {

	// Progress Dialog
	private ProgressDialog pDialog;

	// Creating JSON Parser object
	JSONParser jParser = new JSONParser();

	public static ArrayList<HashMap<String, String>> sitesList;

	// url to get all sites list
	private static String url_all_sites = "http://www.sweetandnicecakes.com/android_connect/get_all_sites.php";

	// JSON Node names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_SITES = "sites";
	private static final String TAG_LID = "lid";
	private static final String TAG_NAME = "name";
	private static final String TAG_GID = "gid";

	private static final String TAG_LATITUDE = "latitude";
	private static final String TAG_LONGITUDE = "longitude";
	private static final String TAG_TELE = "tele";
	private static final String TAG_WEBSITE = "website";

	private static final String TAG_VICINITY = "vicinity";

	// sites JSONArray
	JSONArray sites = null;

	String msgName;
	String msgDetail;

	TextView companyName, tele, website, msgDeal;
	Button btnRoute, btnDelete;
	ImageView imageView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.livedeal_activity);

		// getting site details from intent
		Intent i = getIntent();

		// getting site id (lid) from intent
		msgName = i.getStringExtra("name");

		msgDetail = i.getStringExtra("msgDetail");

		companyName = (TextView) findViewById(R.id.textName);
		msgDeal = (TextView) findViewById(R.id.textDeal);

		// making imageUri
		String imageName = msgName;
		// image - uri of the image
		Uri imageURI = makeImageUri(imageName);

		// for image
		imageView = (ImageView) findViewById(R.id.imageView);
		imageView.setImageURI(imageURI);

		companyName.setText(msgName);
		msgDeal.setText(msgDetail);

		// Hashmap for ListView
		sitesList = new ArrayList<HashMap<String, String>>();

		// Loading sites in Background Thread
		new LoadAllSites().execute();

		btnRoute = (Button) findViewById(R.id.btnRoute);
		btnDelete = (Button) findViewById(R.id.btnDelete);

		// Setting click event lister for the find button
		btnRoute.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent i = new Intent(getApplicationContext(),
						LiveRoutesActivity.class);
				// Closing all previous activities
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);

			}
		});

		btnDelete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// deleting msg from database
				DatabaseHandler db = new DatabaseHandler(
						getApplicationContext());

				List<LiveMessage> messages = db.getAllMessages();

				// delete the msg if name and the detail are equals
				for (LiveMessage cn : messages) {

					String dbMsg = cn.getMsg();
					String dbMsgName = cn.getName();

					if (dbMsg.equalsIgnoreCase(msgDetail)
							&& dbMsgName.equals(msgName)) {
						int msgId = cn.getId();
						db.deleteMessageByID(msgId);
					} else {
						continue;

					}

				}

				Intent i = new Intent(getApplicationContext(),
						LiveMsgActivity.class);
				// Closing all previous activities
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);

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
			pDialog = new ProgressDialog(LiveSitesActivity.this);
			pDialog.setMessage("Loading Msg. Please wait...");
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
			final JSONObject json = jParser.makeHttpRequest(url_all_sites,
					"GET", params);

			// Check your log cat for JSON reponse
			Log.d("All Sites: ", json.toString());

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub

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

								String name = c.getString(TAG_NAME);

								if (!(name.equalsIgnoreCase(msgName)))
									continue;

								// Storing each json item in variable
								String id = c.getString(TAG_LID);
								String gid = c.getString(TAG_GID);
								String vicinity = c.getString(TAG_VICINITY);
								String latitude = c.getString(TAG_LATITUDE);
								String longitude = c.getString(TAG_LONGITUDE);
								String tele = c.getString(TAG_TELE);
								String website = c.getString(TAG_WEBSITE);

								// creating new HashMap
								HashMap<String, String> map = new HashMap<String, String>();

								// adding each child node to HashMap key =>
								// value
								map.put(TAG_LID, id);
								map.put(TAG_NAME, name);
								map.put(TAG_GID, gid);
								map.put(TAG_VICINITY, vicinity);
								map.put(TAG_LATITUDE, latitude);
								map.put(TAG_LONGITUDE, longitude);
								map.put(TAG_TELE, tele);
								map.put(TAG_WEBSITE, website);

								// adding HashList to ArrayList
								sitesList.add(map);
							}

							tele = (TextView) findViewById(R.id.textTelephone);
							website = (TextView) findViewById(R.id.textWebsite);

							// get the telephone, website
							tele.setText(sitesList.get(0).get("tele"));
							website.setText(sitesList.get(0).get("website"));

							// set call on clicking tel. number
							tele.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub

									Intent callIntent = new Intent(
											Intent.ACTION_CALL);
									callIntent.setData(Uri.parse("tel:+"
											+ tele.getText().toString().trim()));
									startActivity(callIntent);

								}

							});

						} else {
							/*
							 * if sites were not found (not in database)
							 */

						}
					} catch (JSONException e) {
						e.printStackTrace();
					}

				}
			});

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

					/*
					 * thread for UI if wanted
					 */

				}
			});

		}

	}

	// returns uri of the image
	private Uri makeImageUri(String name) {
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
		// image = imageURI.toString();
		return imageURI;
	}

}

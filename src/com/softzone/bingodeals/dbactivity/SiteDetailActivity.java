package com.softzone.bingodeals.dbactivity;

import com.softzone.bingodeals.R;

import com.softzone.alert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.softzone.parser.JSONParser;
import com.softzone.bingodeals.locnearby.NearSitesActivity;
import com.softzone.bingodeals.locnearby.NearbyPlacesActivity;
import com.softzone.bingodeals.locnearby.RouteMapActivity;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

/*
 * show the deals of each site
 * island wide sites
 */
public class SiteDetailActivity extends Activity {

	TextView txtName, txtOffer_1, txtOffer_2, txtOffer_3, website, tele;
	// for image
	ImageView imageView;
	// for site id
	String lid;

	Button btnRoute;

	// Progress Dialog
	private ProgressDialog pDialog;

	// JSON parser class
	JSONParser jsonParser = new JSONParser();

	// single site url
	private static final String url_site_detials = "http://www.sweetandnicecakes.com/android_connect/get_site_details.php";

	// JSON Node names
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_SITE = "site";
	private static final String TAG_LID = "lid";
	private static final String TAG_NAME = "name";
	private static final String TAG_GID = "gid";
	private static final String TAG_VICINITY = "vicinity";
	private static final String TAG_OFFER_1 = "offer_1";
	private static final String TAG_OFFER_2 = "offer_2";
	private static final String TAG_OFFER_3 = "offer_3";
	private static final String TAG_WEBSITE = "website";
	private static final String TAG_TELEPHONE = "tele";
	private static final String TAG_LATITUDE = "latitude";
	private static final String TAG_LONGITUDE = "longitude";

	// get latitude, longitude, name and vicinity if needed
	public static double nLatitude = 0.0;
	public static double nLongitude = 0.0;
	public static String nName;
	public static String nVicinity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.site_details);

		// Getting reference to Find Button
		btnRoute = (Button) findViewById(R.id.btn_route);

		// getting site details from intent
		Intent i = getIntent();

		// getting site id (lid) from intent
		lid = i.getStringExtra(TAG_LID);

		// Getting complete site details in background thread
		new GetSiteDetails().execute();

		// Setting click event lister for the find button
		btnRoute.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// Launching Near sites Activity
				Intent i = new Intent(getApplicationContext(),
						RouteMapActivity.class);
				startActivity(i);

			}
		});

	}

	/**
	 * Background Async Task to Get complete site details
	 * */
	class GetSiteDetails extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(SiteDetailActivity.this);
			pDialog.setMessage("Loading place details. Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		/**
		 * Getting site details in background thread
		 * */
		protected String doInBackground(String... strings) {

			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("lid", lid));

			// getting site details by making HTTP request
			// Note that site details url will use GET request
			final JSONObject json = jsonParser.makeHttpRequest(
					url_site_detials, "GET", params);

			// check your log for json response
			Log.d("Single site Details", json.toString());

			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {

					try {

						// json success tag
						int success = json.getInt(TAG_SUCCESS);
						if (success == 1) {
							// successfully received site details
							JSONArray siteObj = json.getJSONArray(TAG_SITE); // JSON
																				// Array

							// get first site object from JSON Array
							JSONObject site = siteObj.getJSONObject(0);

							// site with this lid found
							// Edit Text
							txtName = (TextView) findViewById(R.id.inputName);
							// txtVicinity = (TextView)
							// findViewById(R.id.inputVicinity);
							txtOffer_1 = (TextView) findViewById(R.id.inputOffer_1);
							txtOffer_2 = (TextView) findViewById(R.id.inputOffer_2);
							txtOffer_3 = (TextView) findViewById(R.id.inputOffer_3);
							website = (TextView) findViewById(R.id.textWebsite);
							tele = (TextView) findViewById(R.id.textTelephone);
							imageView = (ImageView) findViewById(R.id.imageView);

							if (site.getString(TAG_OFFER_1).equalsIgnoreCase(
									"null")) {
								txtOffer_1.setVisibility(View.GONE);
							}
							if (site.getString(TAG_OFFER_2).equalsIgnoreCase(
									"null")) {
								txtOffer_2.setVisibility(View.GONE);
							}
							if (site.getString(TAG_OFFER_3).equalsIgnoreCase(
									"null")) {
								txtOffer_3.setVisibility(View.GONE);
							}

							String siteName = site.getString(TAG_NAME);

							txtName.setText(siteName);

							// image - uri of the image
							Uri imageURI = makeImageUri(siteName);
							imageView.setImageURI(imageURI);

							txtOffer_1.setText(site.getString(TAG_OFFER_1));
							txtOffer_2.setText(site.getString(TAG_OFFER_2));
							txtOffer_3.setText(site.getString(TAG_OFFER_3));
							website.setText(site.getString(TAG_WEBSITE));
							tele.setText(site.getString(TAG_TELEPHONE));

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

							// generate notifications for deals
							if (!site.getString(TAG_OFFER_1).equalsIgnoreCase(
									"null")) {

								try {
									generateNotification(
											getApplicationContext(),
											"New Offer: "
													+ site.getString(TAG_OFFER_1));
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							if (!site.getString(TAG_OFFER_2).equalsIgnoreCase(
									"null")) {

								try {
									generateNotification(
											getApplicationContext(),
											"New Offer: "
													+ site.getString(TAG_OFFER_2));
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							if (!site.getString(TAG_OFFER_3).equalsIgnoreCase(
									"null")) {
								generateNotification(
										getApplicationContext(),
										"New Offer: "
												+ site.getString(TAG_OFFER_3));
							}

							// for send to another intent
							nName = site.getString(TAG_NAME);
							nVicinity = site.getString(TAG_VICINITY);
							nLatitude = site.getDouble(TAG_LATITUDE);
							nLongitude = site.getDouble(TAG_LONGITUDE);

						} else {
							// site with lid not found
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
			// dismiss the dialog once got all details
			pDialog.dismiss();
		}
	}

	/**
	 * Issues a notification to inform the user that server has sent a message.
	 */
	private static void generateNotification(Context context, String message) {
		int icon = R.drawable.ic_launcher;
		long when = System.currentTimeMillis();
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(icon, message, when);

		String title = context.getString(R.string.app_name);

		/************************/
		Intent notificationIntent = new Intent(context,
				com.softzone.bingodeals.MainActivity.class);
		// set intent so it does not start a new activity
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent intent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(context, title, message, intent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		// Play default notification sound
		notification.defaults |= Notification.DEFAULT_SOUND;

		// Vibrate if vibrate is enabled
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notificationManager.notify(0, notification);

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

package com.softzone.bingodeals.livemsg;

import static com.softzone.bingodeals.CommonUtilities.EXTRA_MESSAGE;
import static com.softzone.bingodeals.CommonUtilities.SENDER_ID;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.softzone.alert.AlertDialogManager;
import com.softzone.alert.WakeLocker;
import com.softzone.bingodeals.ConnectionDetector;
import com.softzone.bingodeals.ServerUtilities;
import com.google.android.gcm.GCMRegistrar;

import com.softzone.bingodeals.R;

import com.softzone.sqlitedb.DatabaseHandler;
import com.softzone.sqlitedb.LiveMessage;

/**
 * 
 * show live messages on list get the message from GCM
 */

public class LiveMsgActivity extends ListActivity {

	// Progress Dialog
	private ProgressDialog pDialog;

	// label to display gcm messages
	TextView lblMessage;

	// Asyntask
	AsyncTask<Void, Void, Void> mRegisterTask;

	// Alert dialog manager
	AlertDialogManager alert = new AlertDialogManager();

	// Connection detector
	ConnectionDetector cd;

	public static String email;

	private int msgId;
	private String msgName, msgGid, msgVicinity, msgMsg, msgImage;

	// list of messages in DB
	ArrayList<HashMap<String, String>> messagesList;

	ImageView iv;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.common_list);

		cd = new ConnectionDetector(getApplicationContext());

		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			alert.showAlertDialog(LiveMsgActivity.this,
					"Internet Connection Error",
					"Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}

		// Getting name, email from intent
		Intent i = getIntent();

		email = i.getStringExtra("email");

		// Make sure the device has the proper dependencies.
		GCMRegistrar.checkDevice(this);

		// Make sure the manifest was properly set - comment out this line
		// while developing the app, then uncomment it when it's ready.
		GCMRegistrar.checkManifest(this);

		registerReceiver(mHandleMessageReceiver, new IntentFilter());

		// Get GCM registration id
		final String regId = GCMRegistrar.getRegistrationId(this);

		// Check if regid already presents
		if (regId.equals("")) {
			// Registration is not present, register now with GCM
			GCMRegistrar.register(this, SENDER_ID);
		} else {
			// Device is already registered on GCM
			if (GCMRegistrar.isRegisteredOnServer(this)) {
				// Skips registration.
				Toast.makeText(getApplicationContext(),
						"Already registered with BingoDeals", Toast.LENGTH_LONG)
						.show();
			} else {
				// Try to register again, but not in the UI thread.
				// It's also necessary to cancel the thread onDestroy(),
				// hence the use of AsyncTask instead of a raw thread.
				final Context context = this;
				mRegisterTask = new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						// Register on our server
						// On server creates a new user
						ServerUtilities.register(context, email, regId);
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						mRegisterTask = null;
					}

				};
				mRegisterTask.execute(null, null, null);
			}
		}

		/*
		 * listing view
		 */
		// Hashmap for ListView
		messagesList = new ArrayList<HashMap<String, String>>();

		// Loading sites in Background Thread
		new LoadAllSites().execute();

		// listview
		ListView lv = getListView();

		// select a single site and launch edit screen
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id2) {
				// getting values from selected ListItem
				String lid = ((TextView) view.findViewById(R.id.id)).getText()
						.toString();

				String liveMsg = ((TextView) view.findViewById(R.id.name))
						.getText().toString();

				String msgDetail = ((TextView) view.findViewById(R.id.msg))
						.getText().toString();

				// Starting new intent (EditSiteActivity)
				Intent in = new Intent(getApplicationContext(),
						LiveSitesActivity.class);
				// sending lid to next activity
				in.putExtra("name", liveMsg);

				in.putExtra("msgDetail", msgDetail);

				// starting new activity and expecting some response back
				startActivityForResult(in, 100);
			}
		});

		// SQLite database for live messages
		DatabaseHandler db = new DatabaseHandler(this);

		List<LiveMessage> messages = db.getAllMessages();

		for (LiveMessage cn : messages) {
			String log = "Id: " + cn.getId() + " ,Name: " + cn.getName()
					+ " ,Vicinity: " + cn.getVicinity() + " ,GID: "
					+ cn.getGid() + " ,msg: " + cn.getMsg();

			msgId = cn.getId();
			msgGid = cn.getGid();
			msgName = cn.getName();
			msgVicinity = cn.getVicinity();
			msgMsg = cn.getMsg();

			// testing purposes
			Log.d("***Name: ", log);

		}

	}

	/**
	 * Receiving push messages
	 * */
	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);

			// Waking up mobile if it is sleeping
			WakeLocker.acquire(getApplicationContext());

			// Showing received message
			lblMessage.append(newMessage + "\n");
			Toast.makeText(getApplicationContext(),
					"New Message: " + newMessage, Toast.LENGTH_LONG).show();

			// Releasing wake lock
			WakeLocker.release();
		}
	};

	@Override
	protected void onDestroy() {
		if (mRegisterTask != null) {
			mRegisterTask.cancel(true);
		}
		try {
			unregisterReceiver(mHandleMessageReceiver);
			GCMRegistrar.onDestroy(this);
		} catch (Exception e) {
			Log.e("UnRegister Receiver Error", "> " + e.getMessage());
		}
		super.onDestroy();
	}

	/**
	 * Background Async Task to Load all sites by making HTTP Request database
	 * connection
	 * */
	class LoadAllSites extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(LiveMsgActivity.this);
			pDialog.setMessage("Loading Messages. Please wait...");
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

			// getting message data from sqlite database
			DatabaseHandler db = new DatabaseHandler(getApplicationContext());

			List<LiveMessage> messages = db.getAllMessages();

			for (LiveMessage cn : messages) {
				String log = "Id: " + cn.getId() + " ,Name: " + cn.getName()
						+ " ,Vicinity: " + cn.getVicinity() + " ,GID: "
						+ cn.getGid() + " ,msg: " + cn.getMsg();

				msgId = cn.getId();
				msgGid = cn.getGid();
				msgName = cn.getName();
				msgVicinity = cn.getVicinity();
				msgMsg = cn.getMsg();

				String imageName = msgName;

				// image - uri of the image
				String msgImage = makeImageUri(imageName);

				// creating new HashMap
				HashMap<String, String> map = new HashMap<String, String>();

				map.put("msgId", Integer.toString(msgId));
				map.put("msgGid", msgGid);
				map.put("msgName", msgName);
				map.put("msgVicinity", msgVicinity);
				map.put("msgMsg", msgMsg);

				map.put("msgImage", msgImage);

				// adding HashList to ArrayList
				messagesList.add(map);

				// Writing Contacts to log
				Log.d("*****Name: ", log);
				System.out.println("****  " + log);
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
					/**
					 * Updating parsed sqlite data into ListView
					 * */
					ListAdapter adapter = new SimpleAdapter(
							LiveMsgActivity.this, messagesList,
							R.layout.list_live_msg, new String[] { "msgId",
									"msgGid", "msgName", "msgVicinity",
									"msgMsg", "msgImage" }, new int[] {
									R.id.id, R.id.gid, R.id.name,
									R.id.vicinity, R.id.msg, R.id.list_image });
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

package com.softzone.bingodeals;

import com.softzone.bingodeals.R;
import com.softzone.bingodeals.dbactivity.SiteCategoryActivity;
import com.softzone.bingodeals.locnearby.NearbyPlacesActivity;
import com.google.android.gcm.GCMRegistrar;
import com.softzone.alert.AlertDialogManager;
import com.softzone.bingodeals.ConnectionDetector;
import com.softzone.bingodeals.livemsg.LiveMsgActivity;
import com.softzone.bingodeals.livemsg.RegisterActivity;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	// for check the Internet connection
	ConnectionDetector cd;

	// alert dialog manager
	AlertDialogManager alert = new AlertDialogManager();

	// layout buttons
	Button btnViewSites, btnNearbySites, btnLiveDeals;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		cd = new ConnectionDetector(getApplicationContext());

		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present

			alert.showAlertDialog(MainActivity.this,
					"Internet Connection Error",
					"Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}

		if (!GCMRegistrar.isRegistered(getApplicationContext())) {
			// if user is not registered register activity
			Intent i = new Intent(getApplicationContext(),
					RegisterActivity.class);
			startActivity(i);

		}

		/*
		 * 
		 * layout assigning
		 */

		btnLiveDeals = (Button) findViewById(R.id.btnLiveDeals);
		btnNearbySites = (Button) findViewById(R.id.btnNearbySites);
		btnViewSites = (Button) findViewById(R.id.btnViewSites);

		btnLiveDeals.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {

				if (GCMRegistrar.isRegistered(getApplicationContext())) {

					System.out
							.println("btnliveDeals clicks: livemessage activity class should start");

					// Skips registration.
					Toast.makeText(getApplicationContext(),
							"Already registered with GCM", Toast.LENGTH_LONG)
							.show();
					// start LiveMsgActivity
					Intent i = new Intent(getApplicationContext(),
							LiveMsgActivity.class);
					startActivity(i);
				} else {

					// start register activity
					Intent i = new Intent(getApplicationContext(),
							RegisterActivity.class);
					startActivity(i);

				}

			}
		});

		// near view sites click event
		btnNearbySites.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				// start all near sites Activity
				Intent i = new Intent(getApplicationContext(),
						NearbyPlacesActivity.class);
				startActivity(i);

			}
		});

		// view sites click event
		btnViewSites.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {

				System.out
						.println("btnViewSites clicks: sitecategory activity class should start");

				// Launching All sites Activity
				Intent i = new Intent(getApplicationContext(),
						SiteCategoryActivity.class);
				startActivity(i);

			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

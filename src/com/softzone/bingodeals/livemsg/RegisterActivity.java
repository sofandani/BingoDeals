package com.softzone.bingodeals.livemsg;

import static com.softzone.bingodeals.CommonUtilities.SENDER_ID;
import static com.softzone.bingodeals.CommonUtilities.SERVER_URL;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.softzone.alert.AlertDialogManager;
import com.softzone.bingodeals.ConnectionDetector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.softzone.bingodeals.R;

/*
 * for register to get livemessges
 * gcm registration
 * 
 */
public class RegisterActivity extends Activity {
	// alert dialog manager
	AlertDialogManager alert = new AlertDialogManager();

	// Internet detector
	ConnectionDetector cd;

	// UI elements
	EditText txtEmail;

	// Register button
	Button btnRegister;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_activity);

		cd = new ConnectionDetector(getApplicationContext());

		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			alert.showAlertDialog(RegisterActivity.this,
					"Internet Connection Error",
					"Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}

		// Check if GCM configuration is set
		if (SERVER_URL == null || SENDER_ID == null || SERVER_URL.length() == 0
				|| SENDER_ID.length() == 0) {
			// GCM sernder id / server url is missing
			alert.showAlertDialog(RegisterActivity.this,
					"Configuration Error!",
					"Please set your Server URL and GCM Sender ID", false);
			// stop executing code by return
			return;
		}

		txtEmail = (EditText) findViewById(R.id.txtEmail);
		btnRegister = (Button) findViewById(R.id.btnRegister);

		/*
		 * Click event on Register button
		 */
		btnRegister.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// Read EditText dat

				String email = txtEmail.getText().toString();

				System.out.println(" testing validation of email  "
						+ isValidEmailAddress(email));

				// Check if user filled the form
				if (email.trim().length() > 0 && isValidEmailAddress(email)) {
					// Launch Main Activity
					Intent i = new Intent(getApplicationContext(),
							LiveMsgActivity.class);

					// Registering user on our server
					// Sending registraiton details to LiveMsgActivity

					i.putExtra("email", email);
					startActivity(i);
					finish();
				} else {
					// user doen't filled that data
					// ask him to fill the form
					alert.showAlertDialog(RegisterActivity.this,
							"Registration Error!", "Please enter your details",
							false);
				}
			}
		});
	}

	// to validate the email address
	private boolean isValidEmailAddress(String email) {
		String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
		Pattern p = Pattern.compile(ePattern);
		Matcher m = p.matcher(email);
		return m.matches();
	}

}

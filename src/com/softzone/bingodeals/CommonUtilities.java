package com.softzone.bingodeals;

import android.content.Context;
import android.content.Intent;

/**
 * gcm configuration and server reg url
 * 
 * */

public final class CommonUtilities {

	// server registration url
	public static final String SERVER_URL = "http://sweetandnicecakes.com/gcm_server_php/register.php";

	// Google project id for register
	public static final String SENDER_ID = "1079645095286";

	// Tag used on log messages
	public static final String TAG = "GCM config: ";

	public static final String EXTRA_MESSAGE = "message";

	/**
	 * Notifies UI to display a message.
	 * 
	 * @param context
	 *            - application's context
	 * @param message
	 *            - message to be displayed
	 */
	static void displayMessage(Context context, String message) {
		Intent intent = new Intent();
		intent.putExtra(EXTRA_MESSAGE, message);
		context.sendBroadcast(intent);
	}
}

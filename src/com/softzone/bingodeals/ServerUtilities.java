package com.softzone.bingodeals;

import static com.softzone.bingodeals.CommonUtilities.SERVER_URL;
import static com.softzone.bingodeals.CommonUtilities.TAG;
import static com.softzone.bingodeals.CommonUtilities.displayMessage;

import com.softzone.bingodeals.R;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import android.content.Context;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;

public final class ServerUtilities {

	// back off time
	private static final int BACKOFF_MILLI_SECONDS = 2000;
	// to create back off algo
	private static final Random random = new Random();
	// try 5 attempts to connect to the server
	private static final int MAX_ATTEMPTS = 5;

	/**
	 * Register this account/device pair within the server.
	 * 
	 */
	public static void register(final Context context, String email,
			final String regId) {
		Log.i(TAG, "registering device (regId = " + regId + ")");
		String serverUrl = SERVER_URL;

		// map for keep parameters
		Map<String, String> params = new HashMap<String, String>();
		params.put("regId", regId);
		params.put("email", email);

		long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);

		// after gcm returns the registration id need to register on our server
		// try 5 times (server might be down)
		for (int i = 1; i <= MAX_ATTEMPTS; i++) {

			Log.d(TAG, "Attempt #" + i + " to register");

			try {
				displayMessage(context, context.getString(
						R.string.server_registering, i, MAX_ATTEMPTS));
				post(serverUrl, params);
				GCMRegistrar.setRegisteredOnServer(context, true);
				String message = context.getString(R.string.server_registered);
				CommonUtilities.displayMessage(context, message);
				return;
			} catch (IOException e) {

				Log.e(TAG, "Failed to register on attempt " + i + ":" + e);

				// retry if error occurs
				if (i == MAX_ATTEMPTS) {
					break;
				}
				try {
					Log.d(TAG, "Sleeping for " + backoff + " ms before retry");
					Thread.sleep(backoff);
				} catch (InterruptedException e1) {
					// Activity finished before we complete - exit.
					Log.d(TAG, "Thread interrupted: abort remaining retries!");
					Thread.currentThread().interrupt();
					return;
				}
				// increase backoff exponentially
				backoff *= 2;
			}
		}
		String message = context.getString(R.string.server_register_error,
				MAX_ATTEMPTS);
		CommonUtilities.displayMessage(context, message);
	}

	/**
	 * Unregister this account/device pair within the server.
	 */
	static void unregister(final Context context, final String regId) {
		Log.i(TAG, "unregistering device (regId = " + regId + ")");
		String serverUrl = SERVER_URL + "/unregister";
		Map<String, String> params = new HashMap<String, String>();
		params.put("regId", regId);
		try {
			post(serverUrl, params);
			GCMRegistrar.setRegisteredOnServer(context, false);
			String message = context.getString(R.string.server_unregistered);
			CommonUtilities.displayMessage(context, message);
		} catch (IOException e) {

			// when get the not registerd error message server should unregister
			// the device
			String message = context.getString(
					R.string.server_unregister_error, e.getMessage());
			CommonUtilities.displayMessage(context, message);
		}
	}

	/**
	 * Issue a POST request to the server.
	 * 
	 * @param endpoint
	 *            POST address.
	 * @param params
	 *            request parameters.
	 * 
	 * @throws IOException
	 *             propagated from POST.
	 */
	private static void post(String endpoint, Map<String, String> params)
			throws IOException {

		URL url;
		try {
			url = new URL(endpoint);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("invalid url: " + endpoint);
		}
		StringBuilder bodyBuilder = new StringBuilder();
		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
		// constructs the POST body using the parameters
		while (iterator.hasNext()) {
			Entry<String, String> param = iterator.next();
			bodyBuilder.append(param.getKey()).append('=')
					.append(param.getValue());
			if (iterator.hasNext()) {
				bodyBuilder.append('&');
			}
		}
		String body = bodyBuilder.toString();
		Log.v(TAG, "Posting '" + body + "' to " + url);
		byte[] bytes = body.getBytes();
		HttpURLConnection conn = null;
		try {
			Log.e("URL", "> " + url);
			conn = (HttpURLConnection) url.openConnection();
			// indicate flag that url valid output
			conn.setDoOutput(true);
			// set flag that connection doesnt allow to use caches
			conn.setUseCaches(false);
			conn.setFixedLengthStreamingMode(bytes.length);
			// set the post request command which wil send to http server
			conn.setRequestMethod("POST");
			// set the value of the specified required header field
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded;charset=UTF-8");
			// post the request
			OutputStream out = conn.getOutputStream();
			out.write(bytes);
			out.close();
			// handle the response
			int status = conn.getResponseCode();
			if (status != 200) {
				throw new IOException("Post failed with error code " + status);
			}
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
}

package com.softzone.bingodeals;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * detect Internet connection status
 * */

public class ConnectionDetector {

	private Context _context;

	/**
	 * Method to detect Internet connections
	 * 
	 * @param context
	 *            - context
	 * 
	 * */

	public ConnectionDetector(Context context) {
		this._context = context;
	}

	// Checking for all possible internet providers
	public boolean isConnectingToInternet() {
		ConnectivityManager connectivity = (ConnectivityManager) _context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null)
				for (int i = 0; i < info.length; i++)
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}

		}
		return false;
	}
}

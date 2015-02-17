package com.softzone.alert;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.softzone.bingodeals.R;

public class AlertDialogManager {

	static final int BUTTON_POSITIVE = -1;

	/**
	 * Method to display simple Alert Dialog
	 * 
	 * @param context
	 *            - context
	 * @param title
	 *            - alert dialog title
	 * @param message
	 *            - alert message
	 * @param status
	 *            - success/failure (used to set icon)
	 * */
	public void showAlertDialog(Context context, String title, String message,
			Boolean status) {
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();

		// Setting Dialog Title
		alertDialog.setTitle(title);

		// Setting Dialog Message
		alertDialog.setMessage(message);

		if (status != null) {
			// Setting alert dialog icon
			alertDialog
					.setIcon((status) ? R.drawable.success : R.drawable.fail);
		}

		// Setting OK Button
		alertDialog.setButton(BUTTON_POSITIVE, "OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				});

		// Showing Alert Message
		alertDialog.show();
	}

}

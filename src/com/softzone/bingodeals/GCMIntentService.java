package com.softzone.bingodeals;

import static com.softzone.bingodeals.CommonUtilities.SENDER_ID;
import static com.softzone.bingodeals.CommonUtilities.displayMessage;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.softzone.bingodeals.R;
import com.softzone.bingodeals.livemsg.LiveMsgActivity;
import com.softzone.bingodeals.ServerUtilities;
import com.softzone.sqlitedb.DatabaseHandler;
import com.softzone.sqlitedb.LiveMessage;

import com.google.android.gcm.GCMBaseIntentService;

/**
 * for GCM base intent services
 * */
public class GCMIntentService extends GCMBaseIntentService {

	private static final String TAG = "GCMIntentService";

	public GCMIntentService() {
		super(SENDER_ID);
	}

	/**
	 * called on device successfully registered with gcm
	 * */
	@Override
	protected void onRegistered(Context context, String registrationId) {
		Log.i(TAG, "Device registered: regId = " + registrationId);
		displayMessage(context, "Your device registred");
		Log.d("EMAIL", LiveMsgActivity.email);
		// register the device
		ServerUtilities
				.register(context, LiveMsgActivity.email, registrationId);
	}

	/**
	 * called on device un registred
	 * */
	@Override
	protected void onUnregistered(Context context, String registrationId) {
		Log.i(TAG, "Device unregistered");
		displayMessage(context, getString(R.string.gcm_unregistered));
		// unregister the device
		ServerUtilities.unregister(context, registrationId);
	}

	/**
	 * called on Receiving a new message
	 * */
	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.i(TAG, "Received message");

		String message = intent.getExtras().getString("price");

		displayMessage(context, message);

		System.out.println("recieving message is : " + message
				+ "not null condition: ");

		if (message != null) {

			DatabaseHandler db = new DatabaseHandler(context);
			String[] splittedMessage = message.split(":");
			LiveMessage msg = new LiveMessage();
			msg.setGid(splittedMessage[0]);
			msg.setName(splittedMessage[1]);
			msg.setVicinity(splittedMessage[2]);
			msg.setMsg(splittedMessage[3]);

			db.addMsg(msg);

			db.close();

		}

		// notify all users
		generateNotification(context, message);
	}

	/**
	 * called on receiving a deleted message
	 * */
	@Override
	protected void onDeletedMessages(Context context, int total) {
		Log.i(TAG, "Received deleted messages notification");
		String message = getString(R.string.gcm_deleted, total);
		displayMessage(context, message);
		// notify all users
		generateNotification(context, message);
	}

	/**
	 * called on Error
	 * */
	@Override
	public void onError(Context context, String errorId) {
		Log.i(TAG, "Received error: " + errorId);
		displayMessage(context, getString(R.string.gcm_error, errorId));
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		// log message
		Log.i(TAG, "Received recoverable error: " + errorId);
		displayMessage(context,
				getString(R.string.gcm_recoverable_error, errorId));
		return super.onRecoverableError(context, errorId);
	}

	/**
	 * notify all users saying server has sent a message
	 */
	private static void generateNotification(Context context, String message) {
		int icon = R.drawable.ic_launcher;
		long when = System.currentTimeMillis();
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(icon, message, when);

		String title = context.getString(R.string.app_name);

		Intent notificationIntent = new Intent(context, LiveMsgActivity.class);
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

}

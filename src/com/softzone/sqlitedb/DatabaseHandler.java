package com.softzone.sqlitedb;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*
 * to store LiveMessages it needs a database
 * Android os has built in database SQLite
 * 
 */
public class DatabaseHandler extends SQLiteOpenHelper {

	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "liveMsgManager";

	// messages table name
	private static final String TABLE_MESSAGES = "messages";

	// messages Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_GID = "gid";
	private static final String KEY_NAME = "name";
	private static final String KEY_VICINITY = "vicinity";
	private static final String KEY_MESSAGE = "msg";

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Table
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_MESSAGES_TABLE = "CREATE TABLE " + TABLE_MESSAGES + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_GID + " TEXT,"
				+ KEY_NAME + " TEXT," + KEY_VICINITY + " TEXT," + KEY_MESSAGE
				+ " TEXT" + ")";
		db.execSQL(CREATE_MESSAGES_TABLE);
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);

		// Create tables again
		onCreate(db);
	}

	/**
	 * All CRUD(Create, Read, Update, Delete) Operations
	 */

	// Adding new message
	public void addMsg(LiveMessage message) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_GID, message.getGid()); // message GID
		values.put(KEY_NAME, message.getName()); // message Name
		values.put(KEY_VICINITY, message.getVicinity()); // message vicinity
		values.put(KEY_MESSAGE, message.getMsg()); // deal of the message

		db.insert(TABLE_MESSAGES, null, values);// Inserting Row
		db.close(); // Closing database connection
	}

	// Getting single message
	LiveMessage getMessage(int id) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_MESSAGES, new String[] { KEY_ID,
				KEY_GID, KEY_NAME, KEY_VICINITY, KEY_MESSAGE }, KEY_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (cursor != null)
			cursor.moveToFirst();

		LiveMessage liveMessage = new LiveMessage(Integer.parseInt(cursor
				.getString(0)), cursor.getString(1), cursor.getString(2),
				cursor.getString(3), cursor.getString(4));
		// return message
		return liveMessage;
	}

	// Getting All messages
	public List<LiveMessage> getAllMessages() {
		List<LiveMessage> messageList = new ArrayList<LiveMessage>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_MESSAGES;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				LiveMessage message = new LiveMessage();
				message.setId(Integer.parseInt(cursor.getString(0)));
				message.setGid(cursor.getString(1));
				message.setName(cursor.getString(2));
				message.setVicinity(cursor.getString(3));
				message.setMsg(cursor.getString(4));
				// Adding message to list
				messageList.add(message);
			} while (cursor.moveToNext());
		}

		// return message list
		return messageList;
	}

	// Updating single message
	public int updateMessage(LiveMessage msg) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_GID, msg.getGid());
		values.put(KEY_NAME, msg.getName());
		values.put(KEY_VICINITY, msg.getVicinity());
		values.put(KEY_MESSAGE, msg.getMsg());

		// updating row
		return db.update(TABLE_MESSAGES, values, KEY_ID + " = ?",
				new String[] { String.valueOf(msg.getId()) });
	}

	// Deleting single message
	public void deleteMessage(LiveMessage msg) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_MESSAGES, KEY_ID + " = ?",
				new String[] { String.valueOf(msg.getId()) });
		db.close();
	}

	// Deleting single message
	public void deleteMessageByID(int id) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_MESSAGES, KEY_ID + " = ?",
				new String[] { String.valueOf(id) });
		db.close();
	}

	// Getting messages count
	public int getmessagesCount() {
		String countQuery = "SELECT  * FROM " + TABLE_MESSAGES;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		cursor.close();

		// return count
		return cursor.getCount();
	}

}

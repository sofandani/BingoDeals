package com.softzone.sqlitedb;

/*
 * class for crating
 * Live Deal
 */

public class LiveMessage {

	// msg attributes
	int id;
	String gid, name, vicinity, msg;
	double latitude, longitude;

	// Empty constructor
	public LiveMessage() {

	}

	// constructor
	public LiveMessage(int id, String gid, double latitude, double longitude,
			String name, String vicinity, String msg) {
		this.id = id;
		this.gid = gid;
		this.latitude = latitude;
		this.longitude = longitude;
		this.name = name;
		this.vicinity = vicinity;
		this.msg = msg;
	}

	// constructor
	public LiveMessage(int id, String gid, String name, String vicinity,
			String msg) {
		this.id = id;
		this.gid = gid;
		this.name = name;
		this.vicinity = vicinity;
		this.msg = msg;
	}

	// constructor
	public LiveMessage(String gid, double latitude, double longitude,
			String name, String vicinity, String msg) {
		this.gid = gid;
		this.latitude = latitude;
		this.longitude = longitude;
		this.name = name;
		this.vicinity = vicinity;
		this.msg = msg;
	}

	// constructor
	public LiveMessage(String gid, String name, String vicinity, String msg) {
		this.gid = gid;
		this.name = name;
		this.vicinity = vicinity;
		this.msg = msg;
	}

	/*
	 * 
	 * getters and setters for attributes
	 */
	public int getId() {
		return id;
	}

	public String getGid() {
		return gid;
	}

	public String getName() {
		return name;
	}

	public String getVicinity() {
		return vicinity;
	}

	public String getMsg() {
		return msg;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setGid(String gid) {
		this.gid = gid;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setVicinity(String vicinity) {
		this.vicinity = vicinity;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

}

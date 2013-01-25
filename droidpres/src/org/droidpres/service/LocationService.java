package org.droidpres.service;

import java.util.Calendar;

import org.droidpres.activity.SetupRootActivity;
import org.droidpres.db.DB;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;
import android.util.Log;

public class LocationService extends Service implements Runnable {
	private static final String TAG	= "LocationService";
	private LocationDeterminer mLocationDeterminer;
	private WakeLock mWakeLock;
	private volatile boolean isStart = false;

	@Override
	public void onCreate() {
		super.onCreate();
        mLocationDeterminer = new LocationDeterminer(this);
	}
	
	@Override
	public void onDestroy() {
		Log.v(TAG, "onDestroy");

		mWakeLock.release();
		mLocationDeterminer.stop(true);
		isStart = false;
		super.onDestroy();
	}
	
	@Override
	public void onStart(Intent pIntent, int startId) {
		super.onStart(pIntent, startId);
		Log.w(TAG, "onStart service");
		
		if (isStart) {
			Log.w(TAG, "Service is already started");
			return;
		}
		
		int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		if (hour < 7 || hour > 20) {
			Log.w(TAG, "Not work time");
			return;		
		}
		
		int timeout = SetupRootActivity.getGPSTimeout(this);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		mWakeLock.setReferenceCounted(false);
		mWakeLock.acquire(timeout * 60 * 1000);
			
		mLocationDeterminer.start(SetupRootActivity.getGPSAccuracy(this), timeout * 60 * 1000, this);
		isStart = true;
	}

	@Override
	public IBinder onBind(Intent pIntent) {
		return null;
	}
	
	@Override
	public void run() {
		if (mLocationDeterminer.hasLocation()) {
			try {
				Log.w(TAG, "Received location OK");
				SQLiteDatabase db = DB.get().getWritableDatabase();
				Location location = mLocationDeterminer.getLocation();
				
				String provider = location.getProvider();
				
				ContentValues val = new ContentValues();
				val.put("provider",	TextUtils.isEmpty(provider) ? "Unknown" : provider);
				val.put("lat",	(int) (location.getLatitude() * 1E6));
				val.put("lon",	(int) (location.getLongitude() * 1E6));
				val.put("accuracy",	(int) location.getAccuracy());
				db.insertOrThrow(DB.TABLE_LOCATION, null, val);
				db.close();
			} catch (SQLException e) {
				Log.e(TAG, "ERROR Store location", e);
			}
		} else {
			Log.w(TAG, "Timeout, location not received.");
		}
		stopSelf();
	}
	
}

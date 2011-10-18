package org.droidpres.utils;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;

public class PositionManager {
	private final LocationManager mLocationManager;
	private final LocationHandler mLocationHandler;
	private final LocationListener locationListener = new LocationListener() {

		public synchronized void onLocationChanged(Location location) {
			if (location == null || !isStart) return;
			mLocationHandler.onLocation(location);
		}

		public void onProviderDisabled(String provider) { }
		public void onProviderEnabled(String provider) { }
		public void onStatusChanged(String provider, int status, Bundle extras) { }
		
	};
	
	private boolean isStart = false;
	private int mAccuracy;
	
	public PositionManager(Context context, LocationHandler locationHandler, int accuracy){
		mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		mLocationHandler = locationHandler;
		mAccuracy = accuracy;
	}
	
	public void setAccuracy(int accuracy) {
		mAccuracy = accuracy;
		if (isStart) {
			pause();
			resume();
		}
	}
	
	public void resume() {
		if (isStart) return;
		
		Criteria criteria = new Criteria();
		if (mAccuracy <= 20) {
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			criteria.setPowerRequirement(Criteria.POWER_HIGH);
		} else
		if (mAccuracy <= 50) {
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
			criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
		} else {
			criteria.setAccuracy(Criteria.ACCURACY_COARSE);
			criteria.setPowerRequirement(Criteria.POWER_LOW);
		}
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		String provider = mLocationManager.getBestProvider(criteria, true);
		if (TextUtils.isEmpty(provider)) {
			provider = LocationManager.GPS_PROVIDER;
		}
		
		mLocationManager.requestLocationUpdates(provider, 30 * 1000, 10, locationListener);
		isStart = true;
	}
	
	public void pause(){
		if (isStart) mLocationManager.removeUpdates(locationListener);
		isStart = false;
	}
	
	public boolean isStart() {
		return isStart;
	}
	
	public interface LocationHandler {
		public void onLocation(Location location);
	}
}
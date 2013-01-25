/*******************************************************************************
 * Copyright (c) 2012 Eugene Vorobkalo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Eugene Vorobkalo - initial API and implementation
 ******************************************************************************/

package org.droidpres.service;

import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class LocationDeterminer implements LocationListener {
	private static final String TAG	= "LocationDeterminer";

	private static final int MSG_TIMEOUT			= 0xf1;
	private static final int MSG_INIT_TIMEOUT		= 0xf2;
	private static final long INIT_TIMEOUT		= 60 * 1000;
	
	private Location mLocation = null;
	private Runnable mOnCompletedCallBack;
	private float mAccuracy;
	private long mStartTimeMillis;
	private final LocationManager mLocationManager;
	private final Formatter mFormatter = new Formatter(Locale.US);
	private final TimeOutHandler mHandler; 

	
	public LocationDeterminer(Context pContext) {
		mLocationManager = (LocationManager) pContext.getSystemService(Context.LOCATION_SERVICE);
		mHandler = new TimeOutHandler(this);
	}
	
	@Override
	public void onProviderDisabled(String pProvider) {}

	@Override
	public void onProviderEnabled(String pProvider) {}
	
	@Override
	public void onStatusChanged(String pProvider, int pStatus, Bundle pExtras) {}

	@Override
	public void onLocationChanged(Location pLocation) {
		if (mLocation == null || mLocation.getAccuracy() > pLocation.getAccuracy()) {
			mLocation = pLocation;
		}

		if (mLocation.getAccuracy() <= mAccuracy && (System.currentTimeMillis() - mStartTimeMillis) > INIT_TIMEOUT) {
			stop(false);
		}
	}
	
	public synchronized void start(float pAccuracy, long pTimeOut, Runnable onCompleted) {
		Log.w(TAG, "Start Accuracy="+pAccuracy + " TimeOut="+pTimeOut);
		mStartTimeMillis = System.currentTimeMillis();
		mOnCompletedCallBack = onCompleted;
		mAccuracy = pAccuracy;
		mLocation = null;
		
		if (!mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) &&
			!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			if (mOnCompletedCallBack != null) mOnCompletedCallBack.run();
			Log.w(TAG,"No available location provider");
			return;
		}
		
		mHandler.sendEmptyMessageDelayed(MSG_TIMEOUT, pTimeOut);
		mHandler.sendEmptyMessageDelayed(MSG_INIT_TIMEOUT, INIT_TIMEOUT);
		
		try {
			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		} catch (Exception e) {
			Log.e(TAG, "ERROR Request location updates for NETWORK_PROVIDER: " + e);
		} 
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	}
	
	public synchronized boolean hasLocation() {
		return mLocation != null;
	}

	public synchronized Location getLocation() {
		return mLocation;
	}
	
	public void stop(boolean pExternal) {
		mLocationManager.removeUpdates(this);
		mHandler.removeMessages(MSG_TIMEOUT);
		mHandler.removeMessages(MSG_INIT_TIMEOUT);
		if (!pExternal && mOnCompletedCallBack != null) mOnCompletedCallBack.run();
	}
	
	public String getNmeaLocation() {
		double[] fractPartIntPart = modf(Math.abs(mLocation.getLatitude()));
		final int latDegrees = (int) fractPartIntPart[0];
		final double latMinutes = 60 * fractPartIntPart[1];
		
		fractPartIntPart = modf(Math.abs(mLocation.getLongitude()));
		final int longDegrees = (int) fractPartIntPart[0];
	    final double longMinutes = 60 * fractPartIntPart[1];
	    
	    final char latDirection = (mLocation.getLatitude() > 0) ? 'N' : 'S';
	    final char longDirection = (mLocation.getLongitude() > 0) ? 'E' : 'W';
	    return mFormatter.format("%02d%07.4f,%c,%03d%07.4f,%c", latDegrees, latMinutes,
	    		latDirection, longDegrees, longMinutes, longDirection).toString();
	}
	
	private static double[] modf(final double fullDouble) {
        final int intVal = (int)fullDouble;
        return new double[] {intVal, fullDouble - intVal};
	}
	
	private static class TimeOutHandler extends Handler {
		private final WeakReference<LocationDeterminer> mParent;
		
		public TimeOutHandler(LocationDeterminer pParent) {
			mParent = new WeakReference<LocationDeterminer>(pParent);
		}
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
			LocationDeterminer ld = mParent.get();
			if (ld == null) return;
			
			switch (msg.what) {
			case MSG_TIMEOUT:
				ld.stop(false);
				return;
			case MSG_INIT_TIMEOUT:
				if (ld.mLocation != null && ld.mLocation.getAccuracy() < ld.mAccuracy) {
					ld.stop(false);
				}
			}
		}
	}
}

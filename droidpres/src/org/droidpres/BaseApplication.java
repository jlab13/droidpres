package org.droidpres;

import java.util.Calendar;

import org.droidpres.activity.SetupRootActivity;
import org.droidpres.service.LocationService;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class BaseApplication extends Application {
	public static String VERSION, FULL_VERSION;
	public static int VERSION_CODE;
	private static BaseApplication sInstance;
	
	@Override
	public void onCreate() {
		super.onCreate();
		sInstance = this;
		
		try {
			PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
			VERSION = pi.versionName;
			VERSION_CODE = pi.versionCode;
			FULL_VERSION = VERSION + '.' + VERSION_CODE;  
		} catch (NameNotFoundException e) { }
		
		schedule(this);
	}
	
	public static BaseApplication getInstance() {
		return sInstance;
	}

	public static void schedule(Context pContext) {
		Intent intent = new Intent(pContext, LocationService.class);
		if (SetupRootActivity.getIsGPSLocation(pContext)) {
			scheduleLocation(pContext);
		} else {
	    	AlarmManager am = (AlarmManager) pContext.getSystemService(Context.ALARM_SERVICE);
			am.cancel(PendingIntent.getService(pContext, 0, new Intent(pContext, LocationService.class), 0));
			pContext.stopService(intent);
			Log.w("GPS", "Disable location");
		}
	}
	
	public static void scheduleLocation(Context pContext) {
    	int schedule = SetupRootActivity.getGPSchedule(pContext);    	
    	
    	final Calendar cl = Calendar.getInstance();
		cl.set(Calendar.SECOND, 0);
		int min = cl.get(Calendar.MINUTE);
		cl.add(Calendar.MINUTE, -(min % schedule));
		cl.add(Calendar.MINUTE, schedule);
		
		Log.w("GPS", "Schedule location on " + cl.getTime().toLocaleString());
		
		long interval = schedule * 60 * 1000;
		PendingIntent pi = PendingIntent.getService(pContext, 0, new Intent(pContext, LocationService.class), 0);
    	AlarmManager am = (AlarmManager) pContext.getSystemService(Context.ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, cl.getTimeInMillis(), interval, pi);
	}	
	
}

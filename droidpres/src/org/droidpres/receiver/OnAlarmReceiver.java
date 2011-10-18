package org.droidpres.receiver;

import java.util.Calendar;

import org.droidpres.activity.SetupRootActivity;
import org.droidpres.service.LocationService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OnAlarmReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		final int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		if (hour < 8 || hour > 19 || !SetupRootActivity.getIsGPSLocation(context)) return;
		
		context.startService(new Intent(context, LocationService.class));
	}
}

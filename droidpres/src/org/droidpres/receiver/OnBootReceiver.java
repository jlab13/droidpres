package org.droidpres.receiver;

import org.droidpres.activity.SetupRootActivity;
import org.droidpres.service.LocationService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OnBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (SetupRootActivity.getIsGPSLocation(context)) {
			context.startService(new Intent(context, LocationService.class));
		}
	}
	
}

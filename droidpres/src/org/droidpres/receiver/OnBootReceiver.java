package org.droidpres.receiver;

import org.droidpres.BaseApplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OnBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		BaseApplication.schedule(context);
	}
	
}

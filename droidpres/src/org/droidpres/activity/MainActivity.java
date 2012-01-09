/*******************************************************************************
 * Copyright (c) 2010 Eugene Vorobkalo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Eugene Vorobkalo - initial API and implementation
 ******************************************************************************/
package org.droidpres.activity;

import java.sql.Date;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;

import org.droidpres.R;
import org.droidpres.db.DBDroidPres;
import org.droidpres.service.LocationService;
import org.droidpres.utils.Const;
import org.droidpres.utils.Utils;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends ListActivity {
	private static final int MSG_SETUP = 1;
	private static final int MENU_SETUP = Menu.FIRST;

	private final static int MAINLIST_NEWDOCUMENT		= 0;
	private final static int MAINLIST_TRANSFER		= 1;
	private final static int MAINLIST_REPORTS			= 2;
	private final static int MAINLIST_PHONE			= 3;
	
	public static int versionCode;
	private static int mAccuracy;
	public static String versionName;
	public static String appName;

	private boolean mAgentID = false;
	private boolean mCustomTitleFlag;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCustomTitleFlag = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main_list);
        
        onLocationServece(false);
        
        PackageInfo pi;
		try {
			pi = getPackageManager().getPackageInfo("org.droidpres", 0);
			versionCode = pi.versionCode;
			versionName = pi.versionName;
		} catch (NameNotFoundException e) { }
		appName = getString(R.string.app_name);
        
        setListAdapter(ArrayAdapter.createFromResource(this, R.array.itemMain,
        		android.R.layout.simple_list_item_1));
		
		Calendar cl = Calendar.getInstance();
		cl.add(Calendar.DAY_OF_MONTH, -30);
		Date sqlDate = new Date(cl.getTime().getTime());
		
		SQLiteDatabase db = (new DBDroidPres(this)).Open();
		db.execSQL("delete from document where docstate = ? and docdate < ?",
				new Object[] {Const.DOCSTATE_SEND, sqlDate.toString()});
		db.close();
    }
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		switch (position) {
		case MAINLIST_NEWDOCUMENT:
			startActivity(new Intent(this, ClientListActivity.class));
			break;

		case MAINLIST_TRANSFER:
			startActivity(new Intent(this, TransferActivity.class));
			break;

		case MAINLIST_REPORTS:
			Utils.ToastMsg(this, "В разработке");
			break;

		case MAINLIST_PHONE:
			startActivity(new Intent(Intent.ACTION_DIAL));
			break;
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		DecimalFormat nf = SetupRootActivity.getQtyFormat(this, getString(R.string.lb_qty));
		DecimalFormat cf = SetupRootActivity.getCurrencyFormat(this);
		mAgentID = SetupRootActivity.getAgentID(this).length() > 0;
		if (!mAgentID) Utils.ToastMsg(this, R.string.err_NoSetAgentID);
		SQLiteDatabase db = (new DBDroidPres(this)).Open();
		Cursor cur = db.rawQuery("select distinct count(_id), sum(mainsumm) from document\n"+
				"where docstate = 2 and docdate = current_date", null);
		cur.moveToFirst();
		((TextView)findViewById(R.id.tvMainInfoStr1)).setText(getString(R.string.lb_send_doc_count,
				nf.format(cur.getInt(0))));
		((TextView)findViewById(R.id.tvMainInfoStr2)).setText(getString(R.string.lb_send_doc_sum,
				cf.format(cur.getFloat(1))));
		cur.close();
		db.close();
	}

	@Override
	protected void onStop(){
		unregisterReceiver(mBatInfoReceiver);
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_SETUP, Menu.NONE, R.string.lb_setup)
			.setIcon(android.R.drawable.ic_menu_preferences);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case (MENU_SETUP):
			mAccuracy = SetupRootActivity.getGPSAccuracy(getBaseContext());
			startActivityForResult(new Intent(MainActivity.this, SetupActivity.class), MSG_SETUP);
			return true;
		}
		return false;		
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == MSG_SETUP) {
			onLocationServece(mAccuracy != SetupRootActivity.getGPSAccuracy(getBaseContext()));
		}
	}

	private void customTitleBar(String left, String right) {
		if (right.length() > 20) right = right.substring(0, 20);
		if (mCustomTitleFlag) {
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar);
			((TextView) findViewById(R.id.tvTitleLeft)).setText(left);
			((TextView) findViewById(R.id.tvTitleRight)).setText(right);
		}
	}
	
	private void checkGPS() {
		if (!SetupRootActivity.getNoStartGPS(this)) return;

		LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (! manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			new AlertDialog.Builder(this)
				.setTitle(R.string.setup_nostartwithoutgps)
				.setMessage(R.string.msg_NoOnGPS)
				.setCancelable(false)
		        .setPositiveButton(R.string.lb_on_gps, new DialogInterface.OnClickListener() {
		        	public void onClick(DialogInterface dialog, int which) {
		        		startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
		        				MSG_SETUP); 
		        	}
		        })
		        .setNegativeButton(R.string.lb_setup, new DialogInterface.OnClickListener() {
		        	public void onClick(DialogInterface dialog, int which) {
		        		startActivityForResult(new Intent(MainActivity.this, SetupActivity.class),
		        				MSG_SETUP); 
		        	}
		        })
				.show();
		}
	}
	
	private void onLocationServece(boolean change_accuracy) {
		boolean is_start = false;
		
		if (change_accuracy) {
			stopService(new Intent(this, LocationService.class));
			is_start = false;
		} else {
			ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	        List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
	        for (ActivityManager.RunningServiceInfo service : services) {
	            if (".service.LocationService".equals(service.service.getShortClassName())) {
	            	is_start = true;
	            	break;
	            }
	        }
		}

		if (SetupRootActivity.getIsGPSLocation(this)) {
			checkGPS();
			
			if (!is_start) {
				Log.i("LOCATION", "Start intent location service.");
				startService(new Intent(this, LocationService.class));
			}
		} else {
			if (is_start) stopService(new Intent(this, LocationService.class));
		}
	}
    
	private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
		@Override
	    public void onReceive(Context arg0, Intent intent) {
			int level = intent.getIntExtra("level", 0);
			customTitleBar(appName + " v" + versionName + "." + versionCode, "Заряд: " +
					String.valueOf(level) + "%");
	    }
	};
    
}
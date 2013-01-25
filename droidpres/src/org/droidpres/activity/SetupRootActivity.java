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

import java.text.DateFormat;
import java.text.DecimalFormat;

import org.droidpres.R;
import org.droidpres.db.DB;
import org.droidpres.utils.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class SetupRootActivity extends PreferenceActivity{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setup_root);
	}
	
	//TODO: Временное решение 
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		super.onPreferenceTreeClick(preferenceScreen, preference);			

		if (preference.getKey().equals("clean_ref")) {
			SQLiteDatabase db = DB.get().getWritableDatabase();
			db.execSQL("DELETE FROM " + DB.TABLE_TYPEDOC);
			db.execSQL("DELETE FROM " + DB.TABLE_CLIENT);
			db.execSQL("DELETE FROM " + DB.TABLE_PRODUCT);
			db.execSQL("DELETE FROM " + DB.TABLE_CLIENT_GROUP);
			db.execSQL("DELETE FROM " + DB.TABLE_PRODUCT_GROUP);
			db.execSQL("DELETE FROM " + DB.TABLE_LOCATION);
			db.close();
			Utils.ToastMsg(this, "Справочники очищены.");
			return true;
		} else
		if (preference.getKey().equals("clean_doc")) {
			SQLiteDatabase db = DB.get().getWritableDatabase();
			db.execSQL("DELETE FROM " + DB.TABLE_DOCUMENT);
			db.close();
			Utils.ToastMsg(this, "Документы удаленны.");
			return true;
		} else
		if (preference.getKey().equals("show_location")) {
			startActivity(new Intent(this, TableActivity.class));
			return true;
		}
		return false;
	}

	public static void setSeting(Context context, String key, String val) {
		SharedPreferences.Editor settings_editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        settings_editor.putString(key, val);
        settings_editor.commit();
	}
	
	public static DecimalFormat getQtyFormat(Context context, String suffix) {
		return new DecimalFormat("#,##0.## '"+suffix+"'");
	}

	public static DecimalFormat getCurrencyFormat(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return new DecimalFormat("#,##0.## '"+sharedPreferences.getString(context.getString(R.string.CURRENCY), "")+"'");
	}

	public static DateFormat getDateFormat(Context context) {
		//return new SimpleDateFormat("dd.MM.yy");
		return android.text.format.DateFormat.getDateFormat(context);
	}
	
	public static String getAdminPasswd(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences.getString(context.getString(R.string.ADMIN_PASSWORD), "");
	}
	
	public static String getAgentID(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences.getString(context.getString(R.string.AGENT_ID), "0");
	}

	public static String getWiFiServer(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences.getString(context.getString(R.string.TRANSFER_WIFI_SERVER), "");
	}

	public static String getMobileServer(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences.getString(context.getString(R.string.TRANSFER_MOBILE_SERVER), "");
	}
	
	public static String getApkFileName() {
		return Environment.getExternalStorageDirectory().getAbsolutePath() + "/DroidPres.apk";
	}

	public static String getHttpLogin(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences.getString(context.getString(R.string.HTTP_LOGIN), "login");
	}

	public static String getHttpPasswd(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences.getString(context.getString(R.string.HTTP_PASSWD), "password");
	}

	public static boolean getNoStartGPS(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences.getBoolean(context.getString(R.string.NOSTARTWITHOUTGPS), true);
	}

	public static boolean getIsGPSLocation(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences.getBoolean(context.getString(R.string.GPSLOCATION), true);
	}
	
	public static int getGPSchedule(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(sharedPreferences.getString(context.getString(R.string.GPSSCHEDULE), "15"));
	}
	
	public static int getGPSAccuracy(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(sharedPreferences.getString(context.getString(R.string.GPSACCURACY), "100"));
	}
	
	public static int getGPSTimeout(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(sharedPreferences.getString(context.getString(R.string.GPSTIMEOUT), "3"));
	}
	
}

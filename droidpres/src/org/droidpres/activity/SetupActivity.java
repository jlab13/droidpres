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

import org.droidpres.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.EditText;

public class SetupActivity extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.setup);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		super.onPreferenceTreeClick(preferenceScreen, preference);
		if (preference.getKey().equals(getString(R.string.SETUP_ROOT))) {
			startSetupRoot();
			return true;
		}
		return false;
	}

	
	private void startSetupRoot() {
		final EditText input = new EditText(this);
		input.setSingleLine();
		final String passwd = SetupRootActivity.getAdminPasswd(this);

		if (passwd.length() <= 0)
			startActivity(new Intent(this, SetupRootActivity.class));
		else {
			new AlertDialog.Builder(this).
			setTitle("Код авторизации").
			setView(input).
			setCancelable(true).
			setPositiveButton(android.R.string.ok, new OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					if (input.getEditableText().toString().equalsIgnoreCase(passwd))
						startActivity(new Intent(SetupActivity.this, SetupRootActivity.class));
					}
				
			}).setNegativeButton(android.R.string.cancel, null).show();
		}
	}
	
	
	public static boolean getVibration(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPreferences.getBoolean(context.getString(R.string.VIBRATION_QTY), true);
	}

}

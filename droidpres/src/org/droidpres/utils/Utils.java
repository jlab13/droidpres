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
package org.droidpres.utils;

import android.content.Context;
import android.widget.Toast;

public class Utils {
	public static void ToastMsg(Context context, CharSequence msg) {
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	public static void ToastMsg(Context context, int res) {
		Toast.makeText(context, res, Toast.LENGTH_SHORT).show();
	}

	public static String getConstName(Class<?> clazz, String con) {
		return clazz.getName() + "." + con;
	}

//	public static String getSerialNo(Context context){
//  SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
//  String result = settings.getString("device_id", "empty");
//  if (result.equals("empty")){
//      Process p;
//      try {
//          p = Runtime.getRuntime().exec("getprop ro.serialno");
//          BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
//          String line;
//          result = "";
//          while ((line = input.readLine()) != null){
//              result += line;
//          }
//          input.close();
//      } catch (IOException e) {}
//      
//  }
//  
//  if (result.equals("")){
//  	result = Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
//  	if (result == null) result = "emulator";
//  }
//
//  setSeting(context, "device_id", result);
//  return result;
//}	

}

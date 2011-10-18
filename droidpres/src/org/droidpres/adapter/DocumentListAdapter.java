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
package org.droidpres.adapter;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.droidpres.R;
import org.droidpres.activity.SetupRootActivity;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class DocumentListAdapter extends SimpleCursorAdapter implements ViewBinder {
	public static final DateFormat FORMAT_DATE_ISO_8601 = new SimpleDateFormat("yyyy-MM-dd");
	private DecimalFormat cf;
	private DateFormat df;
	private String[] sDocStateInfo;

	public DocumentListAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
		super(context, layout, c, from, to);
		cf = SetupRootActivity.getCurrencyFormat(context);
		df = SetupRootActivity.getDateFormat(context);
		sDocStateInfo = context.getResources().getStringArray(R.array.DocState);
		setViewBinder(this);
	}

	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		// Очередность columnIndex такая как указано в запросе (создание курсора)
		switch (view.getId()) {
		case android.R.id.text1: // docdate
			String date;
			try {
				date = df.format(FORMAT_DATE_ISO_8601.parse(cursor.getString(columnIndex)));
			} catch (ParseException e) {
				date = cursor.getString(columnIndex);
			}
			((TextView) view).setText(date + "  №" + cursor.getString(0));
			return true;
		case android.R.id.text2: // mainsumm 
			((TextView) view).setText(cf.format(cursor.getDouble(columnIndex)) + " (" +
					sDocStateInfo[cursor.getInt(6)] + ")");
			return true;
		default:
			return false;
		}
	}
}
package org.droidpres.adapter;

import org.droidpres.db.DBDroidPres;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class TableAdapter extends SimpleCursorAdapter implements ViewBinder {

	public static TableAdapter getInstance(Context context) {
		final SQLiteDatabase db = (new DBDroidPres(context)).getWritableDatabase();
		final Cursor cursor =  db.query(DBDroidPres.TABLE_LOCATION,
				new String[] {"date_location as _id, lat, lon, accuracy, provider"},
				null, null, null, null, null);
		return new TableAdapter(context, cursor); 
	}
	
	public TableAdapter(Context context, Cursor c) {
		super(context, android.R.layout.simple_list_item_2,
				c, 
				new String[] {BaseColumns._ID, "provider"},
				new int[] {android.R.id.text1, android.R.id.text2});
		setViewBinder(this);
	}


	public boolean setViewValue(View view, Cursor c, int columnIndex) {
		final TextView tv = (TextView) view;
		if (columnIndex > 0) {
			tv.setText(Float.toString((float) (c.getInt(1) / 1e6)) + " / " +
					Float.toString((float) (c.getInt(2) / 1e6)) +
					" : " + c.getString(3) + " (" +c.getString(4) + ")");
			return true;
		}
		return false;
	}
}

package org.droidpres.activity;

import org.droidpres.db.DB;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

public class TableActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getListView().setAdapter(TableAdapter.getInstance(this));
		startManagingCursor(((SimpleCursorAdapter) getListView().getAdapter()).getCursor());
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Cursor c = ((SimpleCursorAdapter) getListView().getAdapter()).getCursor();
		double lat = c.getInt(1) / 1E6;
		double lon = c.getInt(2) / 1E6;
		
		Intent intent = new Intent(Intent.ACTION_VIEW, 
				Uri.parse(String.format("geo:<%s>,<%s>?z=17", Double.toString(lat), Double.toString(lon))));
		startActivity(intent);
	}
	
	public static class TableAdapter extends SimpleCursorAdapter implements ViewBinder {

		public static TableAdapter getInstance(Context context) {
			SQLiteDatabase db = DB.get().getWritableDatabase();
			Cursor cursor =  db.query(DB.TABLE_LOCATION,
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
}

package org.droidpres.activity;

import org.droidpres.adapter.TableAdapter;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

public class TableActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getListView().setAdapter(TableAdapter.getInstance(this));
		startManagingCursor(((SimpleCursorAdapter) getListView().getAdapter()).getCursor());
	}

	@Override
	protected void onDestroy() {
		stopManagingCursor(((SimpleCursorAdapter) getListView().getAdapter()).getCursor());
		super.onDestroy();
	}
	
	

}

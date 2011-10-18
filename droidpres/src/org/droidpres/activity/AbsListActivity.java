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

import java.util.List;

import org.droidpres.db.DBDroidPres;
import org.droidpres.db.QueryHelper;
import org.droidpres.utils.MenuItemInfo;

import android.app.ListActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.CursorAdapter;

public abstract class AbsListActivity extends ListActivity {
	protected SQLiteDatabase mDataBase;
	protected CursorAdapter mAdapter;
	protected QueryHelper mQueryHelper;
	
	private final int contentId;

	protected AbsListActivity(int contentId, String table_name, String[] fields, String order_by) {
		this.contentId = contentId;
		mQueryHelper = new QueryHelper(table_name, fields, order_by); 
	}

	protected AbsListActivity(int contentId, String table_name, String order_by) {
		this.contentId = contentId;
		mQueryHelper = new QueryHelper(table_name, null, order_by); 
	}
	
	protected AbsListActivity(int contentId, String table_name) {
		this.contentId = contentId;
		mQueryHelper = new QueryHelper(table_name, null); 
	}

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		setContentView(contentId);
		
		mDataBase = (new DBDroidPres(this)).getWritableDatabase();
		
		Cursor cursor = createCursor();
		if (cursor != null) {
			startManagingCursor(cursor);
		}
		
		mAdapter = createAdapter(cursor);
		setListAdapter(mAdapter);				
		
		registerForContextMenu(getListView());
	}
	
	protected abstract Cursor createCursor();

	protected abstract CursorAdapter createAdapter(Cursor cursor);

	@Override
	protected void onDestroy() {
		getCursor().close();
		mDataBase.close();
		super.onDestroy();
	}
	
	public Cursor getCursor() {
		if (mAdapter != null) return mAdapter.getCursor();
		else return null;
	}

	public SQLiteDatabase getDb() {
		return mDataBase;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterView.AdapterContextMenuInfo mi = (AdapterView.AdapterContextMenuInfo)menuInfo;
		String headerTitle = getContextMenuTitle(mi.position, mi.id);
		if (headerTitle != null) menu.setHeaderTitle(headerTitle);
		List<MenuItemInfo> menus = createContextMenus(mi.position, mi.id);
		if (menus == null) return; 
		int i = 0;
		for (MenuItemInfo m : menus) {
			if (m.enabled) menu.add(0, m.menuId, i++, m.titleId);				
		}
	}	
	
	protected String getContextMenuTitle(int position, long id) {
		return "";
	}

	protected List<MenuItemInfo> createContextMenus(int position, long id) {
		return null;
	}
	
	protected void requeryCursor() {
		Cursor cursor = getCursor();
		if (cursor != null) {
			cursor.requery();
		}
	}	
}

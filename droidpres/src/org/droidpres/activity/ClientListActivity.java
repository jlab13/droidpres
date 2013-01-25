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

import java.util.LinkedList;
import java.util.List;

import org.droidpres.R;
import org.droidpres.adapter.ClientListAdapter;
import org.droidpres.db.DB;
import org.droidpres.db.QueryHelper;
import org.droidpres.dialog.ClientInfoDialog;
import org.droidpres.dialog.DocHead;
import org.droidpres.utils.Const;
import org.droidpres.utils.MenuItemInfo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.ToggleButton;

public class ClientListActivity extends AbsListActivity implements FilterQueryProvider, OnKeyListener, OnClickListener {
	private static final int MENU_GROUP			= Menu.FIRST;
	private static final int MENU_SEARCH		= Menu.FIRST + 1;
	private static final int MENU_NEW_DOC		= Menu.FIRST + 2;
	private static final int MENU_LIST_DOC		= Menu.FIRST + 3;
	private static final int MENU_INFO			= Menu.FIRST + 4;

	private static final int DLG_CLIENT_GROUP	= 1;
	private static final int DLG_DOCHEAD		= 2;
	
	private Bundle mDocParams = new Bundle();
	
	public ClientListActivity() {
		super(R.layout.client_list, DB.TABLE_CLIENT,
				new String[] {"_id", "name", "address", "(select count(*) from document\n" +
					"where docstate <> 2 and client_id = "+DB.TABLE_CLIENT+"._id) doc_count"},
				"name");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getListView().setOnKeyListener(this);
		mAdapter.setFilterQueryProvider(this);
		findViewById(R.id.tbClDoc).setOnClickListener(this);
	}

	@Override
	protected CursorAdapter createAdapter(Cursor cursor) {
		return new ClientListAdapter(this,
				R.layout.client_list_item,
				cursor,
                new String[] {"name", "address", "doc_count"}, 
                new int[] {R.id.tvClient, R.id.tvAddres, R.id.imgClientDoc});
	}

	@Override
	protected Cursor createCursor() {
		return mQueryHelper.createCurcor(mDataBase);
	}

	/**
	 * Перехват аппаратных клавиш
	 */
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_SEARCH && event.getAction() == KeyEvent.ACTION_DOWN) {
			InputMethodManager inputMgr = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMgr.toggleSoftInput(0, 0);
			return true;
		}
		return false;
	}
	
	/** 
	 * Фильтр по горячим клавишам
	 */
	public Cursor runQuery(CharSequence constraint) {
		String selection = constraint.toString().trim().toUpperCase();
		if (selection.length() > 1) {
			selection = "name_case like ('%" + selection + "%')";
			mQueryHelper.appendFilter("LIKE_NAME", QueryHelper.FILTER_AND, selection);
		} else 
			mQueryHelper.removeFilter("LIKE_NAME");
		return createCursor();
	}
	
	/** 
	 * Основное меню
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_GROUP, Menu.NONE, R.string.lb_client_group).
		setIcon(android.R.drawable.ic_menu_directions);
		menu.add(0, MENU_SEARCH, Menu.NONE, R.string.lb_search).
		setIcon(android.R.drawable.ic_menu_search);
		return true;
	}

	/** 
	 * Выполнение основного меню
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch  (item.getItemId()) {
		case (MENU_GROUP):
			showDialog(DLG_CLIENT_GROUP);
			return true;
		case (MENU_SEARCH):
			InputMethodManager inputMgr = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMgr.toggleSoftInput(0, 0);
			return true;
		}
		return false;		
	}
	
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.tbClDoc:	// Отфильтровать по наличию документов у клиента
			if (((ToggleButton) v).isChecked()) 
				mQueryHelper.appendFilter("DOC", QueryHelper.FILTER_AND,
						"_id in (select distinct client_id from document where docstate <> 2)");
			else
				mQueryHelper.removeFilter("DOC");
			mAdapter.changeCursor(createCursor());
			break;
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
		AdapterView.AdapterContextMenuInfo mi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
			case MENU_NEW_DOC:  
				NewDoc(mi.id, mi.position);
				return true;
			case MENU_LIST_DOC: 
				ListDoc(mi.id, mi.position);
				return true;
			case MENU_INFO: 
				ClientInfoDialog dlg = new ClientInfoDialog(this);
				dlg.show(mi.id);
				return true;
		}
		return false;
	}

	/**
	 * Создаем диалоги
	 */
	@Override
	protected Dialog onCreateDialog(int dialogID) {
		super.onCreateDialog(dialogID);
		switch (dialogID) {
		case DLG_DOCHEAD:
			return new DocHead(this, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog,	int whichButton) {
					if (whichButton == DialogInterface.BUTTON_POSITIVE) {
						Intent document = new Intent(ClientListActivity.this, ProductListActivity.class);
						
						mDocParams.putInt(Const.EXTRA_DOC_PRESVEN, ((DocHead) dialog).getPresVenType());
						mDocParams.putInt(Const.EXTRA_DOC_TYPE, ((DocHead) dialog).getDocTypeID());
						mDocParams.putString(Const.EXTRA_DOC_DESC, ((DocHead) dialog).getDocDesc());
						document.putExtras(mDocParams);
						
						startActivity(document);
					}
				}
				
			});
		case DLG_CLIENT_GROUP: // Диалог категория клиентов 
			Cursor cursor = mDataBase.rawQuery("select _id, name from client_group\n" +
					"where _id in (select distinct clientgroup_id from client) order by name", null);

			final CharSequence[] names = new CharSequence[cursor.getCount() + 1];  
			final int[] ids = new int[cursor.getCount() + 1];  
			names[0] = getString(R.string.lb_all_outles);
			ids[0] = 0;
			
			int i = 1;
			if (cursor.moveToFirst())
				do {
					ids[i] = cursor.getInt(0);
					names[i++] = cursor.getString(1);
				} while (cursor.moveToNext());
			cursor.close();
			
			Dialog dlg = new AlertDialog.Builder(this).
			setTitle(R.string.lb_client_group).
			setItems(names, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					if (item > 0) 
						mQueryHelper.appendFilter("GR", QueryHelper.FILTER_AND, "clientgroup_id = %d", ids[item]);
					else 
						mQueryHelper.removeFilter("GR");
					mAdapter.changeCursor(createCursor());
				}
			}).create();
			return dlg;
		default:
			return null;
		}
	}

	/**
	 * При выборе торговой точки из основного списка 
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		NewDoc(id, position);
	}

	/**
	 * Контекстное меню 
	 */
	@Override
	protected List<MenuItemInfo> createContextMenus(int position, long id) {
		List<MenuItemInfo> menus = new LinkedList<MenuItemInfo>();
		menus.add(new MenuItemInfo(MENU_NEW_DOC, R.string.lb_new_doc));
		menus.add(new MenuItemInfo(MENU_LIST_DOC, R.string.lb_documents));
		menus.add(new MenuItemInfo(MENU_INFO, R.string.lb_info));
		return menus;
	}

	/**
	 * Заголовок контекстного меню 
	 */
	@Override
	protected String getContextMenuTitle(int position, long id) {
		return QueryHelper.fieldByNameString(getCursor(), "name"); 
	}
	
	private void NewDoc(long id, int position) {
		Cursor cur = getCursor();
		cur.moveToPosition(position);
		mDocParams.putLong(Const.EXTRA_CLIENT_ID, id);
		mDocParams.putString(Const.EXTRA_CLIENT_NAME, QueryHelper.fieldByNameString(cur, "name"));
		showDialog(DLG_DOCHEAD);
	}
	
	private void ListDoc(long id, int position) {
		Cursor cur = getCursor();
		cur.moveToPosition(position);
		Intent document = new Intent(this, DocumentListActivity.class);
		mDocParams.putLong(Const.EXTRA_CLIENT_ID, id);
		mDocParams.putString(Const.EXTRA_CLIENT_NAME, QueryHelper.fieldByNameString(cur, "name"));
		document.putExtras(mDocParams);
		startActivity(document);
	}
}
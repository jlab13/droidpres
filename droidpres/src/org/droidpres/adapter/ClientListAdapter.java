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

import org.droidpres.R;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.AlphabetIndexer;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class ClientListAdapter extends SimpleCursorAdapter implements SectionIndexer, ViewBinder {
	private SectionIndexer sIndexer;

	public ClientListAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to) {
		super(context, layout, cursor, from, to);
		sIndexer = CreateIndexer(cursor);
		setViewBinder(this);
	}

	@Override
	public void changeCursor(Cursor cursor) {
		super.changeCursor(cursor);
		//indexer = CreateIndexer(cursor);
		((AlphabetIndexer) sIndexer).setCursor(cursor);
	}

	public int getPositionForSection(int section) {
		return sIndexer.getPositionForSection(section);
	}

	public int getSectionForPosition(int position) {
		return sIndexer.getSectionForPosition(position);
	}

	public Object[] getSections() {
		return sIndexer.getSections();
	}

	private SectionIndexer CreateIndexer(Cursor cursor) {
		return new AlphabetIndexer(cursor, cursor.getColumnIndex("name"),
				"АБВГДЕЁЄЖЗИIЇЙКЛМНОПРСТУФХЦЧШЩЫЬЭЮЯ");
	}
	
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		if (view.getId() == R.id.imgClientDoc) {
			if (cursor.getInt(columnIndex) > 0) { 
				((ImageView) view).setImageResource(R.drawable.document);
			} else {
				((ImageView) view).setImageBitmap(null);
			}
			return true;
		} else return false;
	}

}

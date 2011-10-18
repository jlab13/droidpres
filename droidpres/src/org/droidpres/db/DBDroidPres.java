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
package org.droidpres.db;

import org.droidpres.R;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBDroidPres extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "DroidPres.sqlite";
	private static final int DATABASE_VERSION = 4;
	private Context mContext;
	
	public static final String TABLE_CLIENT 		=	"client";
	public static final String TABLE_CLIENT_GROUP	=	"client_group";
	public static final String TABLE_PRODUCT		=	"product";
	public static final String TABLE_PRODUCT_GROUP	=	"product_group";
	public static final String TABLE_TYPEDOC 		=	"typedoc";
	public static final String TABLE_DOCUMENT 		=	"document";
	public static final String TABLE_DOCUMENT_DET	=	"document_det";
	public static final String TABLE_LOCATION 		=	"location";

	public DBDroidPres(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String[] ddl = mContext.getResources().getStringArray(R.array.ddl_of_database);
		for (String ddl_txt: ddl) db.execSQL(ddl_txt);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int old_version, int new_version) {
		Log.d("onUpgradeDB",String.format("New version: %d, Old version: %d", new_version, old_version));
		if (old_version != new_version) {
			if (old_version < 2) {
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_TYPEDOC);
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLIENT_GROUP);
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLIENT);
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCT_GROUP);
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCT);
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOCUMENT_DET);
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOCUMENT);
			}
			onCreate(db);
		}
	}

	public SQLiteDatabase Open() {
		return getWritableDatabase();
	}
}
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class QueryHelper {
	public static final String KEY_ID = "_id";

	public static final int JOIN_INNER 			= 0;
	public static final int JOIN_LEFT_OUTER 	= 1;
	public static final int FILTER_AND 			= 0;
	public static final int FILTER_OR		 	= 1;

	private String table_name;
	private String order_by;
	private String[] fileds = null;
	private String[] fields_on_table = null;
	private Map<String, FilterItem> map_filter;
	private Map<String, JoinItem> map_join;

	private class JoinItem {
		String join_field;
		int type_join;
		String fields[];
		
		JoinItem(String join_field, int type_join, String[] fields) {
			this.join_field = join_field;
			this.type_join = type_join;
			this.fields = fields;
		}
	}
	
	private class FilterItem {
		int type_filter;
		String value;
		
		FilterItem(int type_filter, String value) {
			this.type_filter = type_filter;
			this.value = value;
		}
	}

	public QueryHelper(String table_name, String[] fields) {
		this.table_name = table_name;
		this.fileds = fields;
	}

	public QueryHelper(String table_name, String[] fields, String order_by) {
		this.table_name = table_name;
		this.fileds = fields;
		this.order_by = order_by;
	}
	
	public Cursor createCurcor(SQLiteDatabase db) {
		return db.query(getTables(), getFields(), getFilter(),  null,  null,  null,  order_by);
	}

	public static Cursor createCurcor(String table_name, SQLiteDatabase db, String ...fields) {
		return db.query(table_name, fields, null,  null,  null,  null, null);
	}
	
	public static Cursor createCurcorOneRecord(String table_name, SQLiteDatabase db, long id, String ...fields) {
		Cursor cursor = db.query(table_name, fields, table_name + '.' + KEY_ID + "=" + id,  null,  null,  null,  null);
		cursor.moveToFirst();
		return cursor;
	}
	
	public static String[] getFieldsOnTable(SQLiteDatabase db, CharSequence table_name) {
		Cursor cur = db.rawQuery("PRAGMA table_info(" + table_name + ")", null);
		cur.moveToFirst();

		String[] fields_on_table = new String[cur.getCount()];
		int i = 0;
		do {
			fields_on_table[i++] = cur.getString(1);
		} while (cur.moveToNext());
		cur.close();
		return fields_on_table; 
	}

	
	private String getTables() {
		if (map_join == null || map_join.isEmpty())	return table_name;
		
		StringBuffer sb = new StringBuffer();
		for (String table_join: map_join.keySet()) { 
			String join_str = "";
			switch (map_join.get(table_join).type_join) {
			case JOIN_INNER:
				join_str = " INNER JOIN ";
				break;
			case JOIN_LEFT_OUTER:
				join_str = " LEFT OUTER JOIN ";
				break;
			}
			
			sb.append(table_name + join_str + table_join + " ON (" +
					table_name + "." + map_join.get(table_join).join_field +  " = " +table_join + "._id) ");
		}
		return sb.toString();
	}

	private String[] getFields() {
		if (fileds == null) return null;
		if (map_join == null || map_join.isEmpty()) return fileds;

		ArrayList<String> _rsult = new ArrayList<String>();
		for (String str: fileds)
			if (str.charAt(0) != '(')
				_rsult.add(tff(str));
		for (JoinItem val: map_join.values())
			_rsult.addAll(Arrays.asList(val.fields));
		return _rsult.toArray(new String[_rsult.size()]); 
	}
	
	public String getFilter() {
		if (map_filter == null || map_filter.isEmpty()) return null; 
		
		StringBuffer sb = new StringBuffer();
		boolean isFirst = true;
		for(FilterItem item: map_filter.values()) {
			if (isFirst)
				sb.append(item.value);
			else {
				switch (item.type_filter) {
				case FILTER_AND:
					sb.append(" AND " + item.value);
					break;
				case FILTER_OR:
					sb.append(" OR " + item.value);
					break;
				}
			}
			isFirst = false;
		}
		return sb.toString();
	}

	public String tff(String field) {
		return table_name + '.' + field; 
	}

	public void appendFilter(String name, int type_filter, String value){
		if (map_filter == null)
			map_filter = new HashMap<String, FilterItem>();
			
		FilterItem item = new FilterItem(type_filter, value);
		map_filter.put(name, item);
	}

	public void appendFilter(String name, int type_filter, String value, Object... args){
		appendFilter(name, type_filter, String.format(value, args));
	}

	public void removeFilter(String name){
		if (map_filter != null)
			map_filter.remove(name);
	}
	
	public void appendJoin(String table, String join_field, int type_join, String[] fields) {
		if (map_join == null)
			map_join = new HashMap<String, JoinItem>();
		
		if (fields != null)
			for (int i = 0; i < fields.length; i++) 
				fields[i] = table + '.' + fields[i]; 
		
		JoinItem rec = new JoinItem(join_field, type_join, fields);
		map_join.put(table, rec);
	}
	
	public void removeJoin(String table) {
		if (map_join == null) return;
		map_join.remove(table);
	}
	
	public void setOrderBy(String order_by) {
		this.order_by = order_by;
	}
	
	public void cleanOrderBy() {
		this.order_by = null;
	}

	public  String[] getFieldsOnTable(SQLiteDatabase db) {
		if (fields_on_table == null) { 
			Cursor cur = db.rawQuery("PRAGMA table_info(" + table_name + ")", null);
			cur.moveToFirst();

			fields_on_table = new String[cur.getCount()];
			int i = 0;
			do {
				fields_on_table[i++] = cur.getString(1);
			} while (cur.moveToNext());
			cur.close();
		}
		return fields_on_table;
	}
	
	public static String fieldByNameString(Cursor cursor, String field_name) {
		return cursor.getString(cursor.getColumnIndex(field_name));
	}

	public static int fieldByNameInt(Cursor cursor, String field_name) {
		return cursor.getInt(cursor.getColumnIndex(field_name));
	}

	public static long fieldByNameLong(Cursor cursor, String field_name) {
		return cursor.getLong(cursor.getColumnIndex(field_name));
	}

	public static float fieldByNameFloat(Cursor cursor, String field_name) {
		return cursor.getFloat(cursor.getColumnIndex(field_name));
	}
}

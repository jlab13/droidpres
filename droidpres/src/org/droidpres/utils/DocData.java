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

import java.util.HashMap;
import java.util.Map;

import org.droidpres.db.DB;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

public class DocData implements Parcelable {
	private static final int QTY = 0;
	private static final int PRICE = 1;

	private onDocDataChange DataChange;
	private Map<Long, float[]> map = new HashMap<Long, float[]>();
	private float summ;
	private boolean bUbdateFlag;

	public DocData() {
		this.DataChange = null;
		summ = 0;
	}
	
	public DocData(onDocDataChange DataChange) {
		this.DataChange = DataChange;
		summ = 0;
	}

	public DocData(Parcel in) {
		this.DataChange = null;
		summ = 0;
		readFromParcel(in);
	}
	
	public void setOnDocDataChange(onDocDataChange DataChange) {
		this.DataChange = DataChange;
	}
	
	public void put(Long id, float qty, float price) {
		summ -= getRecSumm(id);
		map.put(id, new float[] {qty, price});
		summ += price * qty;
		if (DataChange != null && !bUbdateFlag) DataChange.onDataChange(summ);
	}

	public float[] get(Long id) {
		return map.get(id);
	}

	public float getPrice(Long id) {
		float[] val = map.get(id);
		if (val != null) return val[PRICE];
		else return 0;
	}

	public float getQty(Long id) {
		float[] val = map.get(id);
		if (val != null) return val[QTY];
		else return 0;
	}
	
	public float getQtySumm() {
		float result = 0;
		for (float[] val: map.values())
			result += val[QTY]; 
		return result;
	}

	public float getRecSumm(Long id) {
		float[] val = map.get(id);
		if (val != null) return (val[PRICE] * val[QTY]);
		else return 0;
	}

	public float getSumm() {
		return summ;
	}

	public int getRecCount() {
		return map.size();
	}
	
	public void remove(Long id) {
		summ -= getRecSumm(id);
		map.remove(id);
		if (DataChange != null && !bUbdateFlag) DataChange.onDataChange(summ);
	}
	
	public void clear() {
		map.clear();
		summ = 0;
		if (DataChange != null && !bUbdateFlag) DataChange.onDataChange(summ);
	}

	public void StartUpdate() {
		bUbdateFlag = true;
	}

	public void StopUpdate() {
		bUbdateFlag = false;
		if (DataChange != null) DataChange.onDataChange(summ);
	}
	
	public void Load(long id, SQLiteDatabase db) {
		Cursor cursor = db.query(DB.TABLE_DOCUMENT_DET,
				new String[] {"product_id", "qty", "price"},
				"document_id = " + id, null, null, null, null);

		if (cursor.moveToFirst()) {
			StartUpdate();
			do {
				put(cursor.getLong(0), cursor.getFloat(1), cursor.getFloat(2));
			} while (cursor.moveToNext());
			StopUpdate();
		}
		cursor.close();
	}
	
	public void Save(long document_id, SQLiteDatabase db) {
		ContentValues _val = new ContentValues();
		for (long goodsID: map.keySet()) {
			_val.put("document_id", document_id);
			_val.put("product_id", goodsID);
			_val.put("qty", getQty(goodsID));
			_val.put("price", getPrice(goodsID));
			db.insert(DB.TABLE_DOCUMENT_DET, null, _val);
			_val.clear();
		}
	}
	
	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(map.size());
		for (long _goodsID: map.keySet()) {
			dest.writeLong(_goodsID);
			dest.writeFloatArray(map.get(_goodsID));
		}
	}

	public void readFromParcel(Parcel in) {
		summ = 0;
		int count = in.readInt();
		for (int i = 0; i < count; i++) {
			final long _goodsID = in.readLong(); 
			float[] _val = new float[2]; 
			in.readFloatArray(_val);
			map.put(_goodsID, _val);
			summ += _val[0] * _val[1];  
        }
	}
	
	
	public class MyCreator implements Parcelable.Creator<DocData> {
	      public DocData createFromParcel(Parcel source) {
	            return new DocData(source);
	      }
	      public DocData[] newArray(int size) {
	            return new DocData[size];
	      }
	}	
}

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

import java.text.DecimalFormat;

import org.droidpres.R;
import org.droidpres.activity.SetupRootActivity;
import org.droidpres.db.QueryHelper;
import org.droidpres.utils.DocData;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class ProductListAdapter extends SimpleCursorAdapter implements ViewBinder {
//	private Context mContext;
//	private SectionIndexer mIndexer;
	public DecimalFormat nf;
	public DecimalFormat cf;
	private long mId;
	private float mQty;
	private float mAvailable, mPrice, mCasesize;
	public boolean mCaseShowFlag;
	public DocData mDocData;

	public ProductListAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to) {
		super(context, layout, cursor, from, to);
//		mContext = context; 
//		mIndexer = CreateIndexer(cursor);
		setViewBinder(this);

		nf = SetupRootActivity.getQtyFormat(context, context.getString(R.string.lb_qty));
		cf = SetupRootActivity.getCurrencyFormat(context);
	}

	
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		mCasesize = QueryHelper.fieldByNameFloat(cursor, "casesize");  
		
		// Очередность columnIndex такая как указано в запросе (создание курсора)
		switch (columnIndex) {
		case 0: // _id По этому полю обрабатываем количество в документе 
			mId = cursor.getLong(columnIndex);
			mQty = mDocData.getQty(mId);
			if (mQty > 0) {
				if (mCaseShowFlag) ((TextView) view).setText(nf.format(mQty / mCasesize));
				else ((TextView) view).setText(nf.format(mQty));
			} else ((TextView) view).setText("");
			return true;
		case 2: // available Остаток на складе 
			mAvailable = cursor.getFloat(columnIndex);
			
			if (mAvailable > 0) {  
				if (mCaseShowFlag) ((TextView) view).setText(nf.format(mAvailable / mCasesize));
				else ((TextView) view).setText(nf.format(mAvailable));
			} else ((TextView) view).setText("");
			return true;
		case 3: // price Цена товара 
			mPrice = cursor.getFloat(columnIndex);
			if (mPrice > 0.0) { 
				if (mCaseShowFlag) ((TextView) view).setText(cf.format(mPrice * mCasesize));
				else ((TextView) view).setText(cf.format(mPrice));
			} else ((TextView) view).setText("");
			return true;
			
		default:
			return false;
		}
	}

//	@Override
//	public void changeCursor(Cursor cursor) {
//		super.changeCursor(cursor);
//		//mIndexer = CreateIndexer(cursor);
//		((ProductIndexer) indexer).setCursor(cursor);
//	}
	
//	public int getPositionForSection(int section) {
//	return mIndexer(section);
//}
//
//public int getSectionForPosition(int position) {
//	return mIndexer(position);
//}
//
//public Object[] getSections() {
//	return mIndexer();
//}
//
//private SectionIndexer CreateIndexer(Cursor cursor) {
//	return new ProductIndexer(mContext, cursor); 
//}
	
	
//	class ProductIndexer extends DataSetObserver implements SectionIndexer {
//		private Cursor mDataCursor;
//		private String[] mGroupName;
//		private int[] mGroupId;
//		final SparseIntArray mAlphaMap;
//		
//		public ProductIndexer(Context context, Cursor cursor) {
//			mDataCursor = cursor;
//			
//			SQLiteDatabase db = (new DBDroidPres(context)).getWritableDatabase();
//			Cursor cur = db.rawQuery("select distinct product_group.* from product\n" +
//					"inner join product_group on (product_group._id = product.productgroup_id)\n" + 
//					"order by sortorder", null);
//			if (cur.moveToFirst()) { 
//				mGroupName = new String[cur.getCount()];
//				mGroupId = new int[cur.getCount()];
//				int row = 0;
//				do {
//					mGroupId[row] = cur.getInt(0);
//					mGroupName[row] = cur.getString(1);
//					row++;
//				} while (cur.moveToNext());
//			}
//			cur.close();
//			db.close();
//			
//			if (cursor != null) 
//				cursor.registerDataSetObserver(this);
//
//			if (mGroupId != null) 
//				mAlphaMap = new SparseIntArray(mGroupId.length);
//			else
//				mAlphaMap = new SparseIntArray();
//		}
//		
//		public int getSectionForPosition(int position) {
//			int savedCursorPos = mDataCursor.getPosition();
//			mDataCursor.moveToPosition(position);
//			int _gr_id = QueryHelper.FieldByNameInt(mDataCursor, "productgroup_id"); 
//			mDataCursor.moveToPosition(savedCursorPos);
//			for (int i = 0; i < mGroupId.length; i++) {
//				if (_gr_id == mGroupId[i]) return i;
//			}
//			return 0;
//		}
//		
//		public Object[] getSections() {
//			return mGroupName;
//		}
//		
//		public int getPositionForSection(int sectionIndex) {
//			int pos = 0;
//			if (sectionIndex <= 0) return pos;
//			
//			if (sectionIndex >= mGroupId.length) sectionIndex = mGroupId.length - 1;
//
//			if (Integer.MIN_VALUE != (pos = mAlphaMap.get(sectionIndex,	Integer.MIN_VALUE))) return pos;
//			
//			int savedCursorPos = mDataCursor.getPosition();
//			int value = mGroupId[sectionIndex];
//			int columnindex = mDataCursor.getColumnIndex("productgroup_id"); 
//			if (mDataCursor.moveToFirst())
//				do {
//					if (mDataCursor.getInt(columnindex) == value) {
//						pos = mDataCursor.getPosition();
//						mAlphaMap.put(sectionIndex, pos);
//						break;
//					}
//				} while (mDataCursor.moveToNext());
//
//			mDataCursor.moveToPosition(savedCursorPos);
//			return pos;
//		}
//
//	    public void setCursor(Cursor cursor) {
//	        if (mDataCursor != null) {
//	            mDataCursor.unregisterDataSetObserver(this);
//	        }
//	        mDataCursor = cursor;
//	        if (cursor != null) {
//	            mDataCursor.registerDataSetObserver(this);
//	        }
//	        mAlphaMap.clear();
//	    }
//	}
}

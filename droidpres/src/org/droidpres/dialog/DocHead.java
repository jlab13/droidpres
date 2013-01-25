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
package org.droidpres.dialog;

import java.util.ArrayList;

import org.droidpres.R;
import org.droidpres.db.DB;
import org.droidpres.db.SpinnerDB;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

/**
 * Диалог для заполнения шапки документа
 * @author Eugene Vorobkalo
 *
 */
public class DocHead extends AlertDialog {
	public static final int DOC_PRES = 0;
	public static final int DOC_VEN = 1;

	private View view;
	private ArrayList<SpinnerDB> items;
	private Spinner spTypeDoc;
	

	public DocHead(Context context, android.content.DialogInterface.OnClickListener listener) {
		super(context);
		setTitle(R.string.lb_title_doc_params);
		
		items = new ArrayList<SpinnerDB>(); 
		
		ArrayAdapter<SpinnerDB> adapter = new ArrayAdapter<SpinnerDB>(context, 
                android.R.layout.simple_spinner_item, 
                items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		
		SQLiteDatabase db = DB.get().getReadableDatabase();
		Cursor cursor = db.query(DB.TABLE_TYPEDOC, 
        		new String[] {"_id","name"}, null, null, null, null, null);
		
		if (cursor.moveToFirst())
			do {
				items.add(new SpinnerDB(cursor.getInt(0), cursor.getString(1)));
			} while (cursor.moveToNext());
		
		cursor.close();
		db.close();

		LayoutInflater inflater = LayoutInflater.from(context);
		view = inflater.inflate(R.layout.dlg_document_head, null);
		
		((RadioGroup) view.findViewById(R.id.rgPresVen)).check(R.id.rbPres);
		spTypeDoc = (Spinner) view.findViewById(R.id.spTypeDoc); 
		spTypeDoc.setAdapter(adapter);
		spTypeDoc.getSelectedItemPosition();
		
		setView(view);
		//setIcon(android.R.drawable.ic_dialog_map);
		setButton(DialogInterface.BUTTON_POSITIVE, context.getText(android.R.string.ok), listener);
		setButton(DialogInterface.BUTTON_NEGATIVE, context.getText(android.R.string.cancel), listener);
	}
	
	public int getPresVenType() {
		if ( ((RadioButton) view.findViewById(R.id.rbPres)).isChecked() )
			return DOC_PRES;
		else 
			return DOC_VEN;
	}
	
	public void setPresVenType(int val) {
		((RadioButton) view.findViewById(R.id.rbPres)).setChecked(val == DOC_PRES);
	}

	public int getDocTypeID() {
		return items.get(spTypeDoc.getSelectedItemPosition()).id ;
	}
	
	public void setDocTypeID(int val) {
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).id == val ) {
				spTypeDoc.setSelection(i);
				break;
			}
		}
	}

	public String getDocDesc() {
		return ((EditText) view.findViewById(R.id.eDescription)).getText().toString();
	}

	public void setDocDesc(String val) {
		((EditText) view.findViewById(R.id.eDescription)).setText(val);
	}
}

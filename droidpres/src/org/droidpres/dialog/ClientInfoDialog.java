package org.droidpres.dialog;

import java.text.DecimalFormat;

import org.droidpres.R;
import org.droidpres.activity.AbsListActivity;
import org.droidpres.activity.SetupRootActivity;
import org.droidpres.db.DB;
import org.droidpres.db.QueryHelper;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ClientInfoDialog {
	private final AbsListActivity parentActivity;
	private final LayoutInflater layoutInflater;
	private final DecimalFormat cf;
	private boolean divider = true;
	
	public ClientInfoDialog(AbsListActivity parentActivity) {
		this.parentActivity = parentActivity;
		layoutInflater = LayoutInflater.from(parentActivity);
		cf = SetupRootActivity.getCurrencyFormat(parentActivity);
	}
	
	public void show(long clientId) {
		View v = layoutInflater.inflate(R.layout.client_info, null);
		LinearLayout layout = (LinearLayout) v.findViewById(R.id.list);
		
		Cursor _cur = DB.get().getReadableDatabase().query(DB.TABLE_CLIENT, null, 
				BaseColumns._ID + clientId, null, null, null, null);
		if (!_cur.moveToFirst()) {
			_cur.close();
			return;
		}
		
		String str;
		Float ft;

		ft = QueryHelper.fieldByNameFloat(_cur, "debtsumm1");
		if (ft != null && ft > 0.0 ) {
			str = cf.format(ft) + ' ' +  QueryHelper.fieldByNameString(_cur, "debtdays1");
			char c = str.charAt(str.length()-1);
			if (c == '1')
				str = str + ' ' + parentActivity.getString(R.string.lb_dey);
			else if (c >= '1' && c < '5')
				str = str + ' ' + parentActivity.getString(R.string.lb_dey1);
			else
				str = str + ' ' + parentActivity.getString(R.string.lb_deys);
			add(layout, R.string.lb_cl_debtsumm1, str);
		}
		
		ft = QueryHelper.fieldByNameFloat(_cur, "debtsumm2");
		if (ft != null && ft > 0.0 ) {
			str = cf.format(ft) + ' ' +  QueryHelper.fieldByNameString(_cur, "debtdays2");
			char c = str.charAt(str.length()-1);
			if (c == '1')
				str = str + ' ' + parentActivity.getString(R.string.lb_dey);
			else if (c >= '1' && c < '5')
				str = str + ' ' + parentActivity.getString(R.string.lb_dey1);
			else
				str = str + ' ' + parentActivity.getString(R.string.lb_deys);
			add(layout, R.string.lb_cl_debtsumm2, str);
		}

		str = QueryHelper.fieldByNameString(_cur, "address");
		if (str != null && str.length() > 0 ) add(layout, R.string.lb_cl_address, str);
		
		str = QueryHelper.fieldByNameString(_cur, "phone");
		if (str != null && str.length() > 0 ) add(layout, R.string.lb_cl_phone, str);

		str = QueryHelper.fieldByNameString(_cur, "fname");
		Log.i("ClientInfoDialog.fname", str);
		if (str != null && str.length() > 0 ) add(layout, R.string.lb_cl_fname, str);

		str = QueryHelper.fieldByNameString(_cur, "addresslaw");
		if (str != null && str.length() > 0 ) add(layout, R.string.lb_cl_addresslaw, str);

		divider = false;
		str = QueryHelper.fieldByNameString(_cur, "_id");
		if (str != null && str.length() > 0 ) add(layout, R.string.lb_cl_id, str);

		str = QueryHelper.fieldByNameString(_cur, "name");
		_cur.close();

		final AlertDialog dlg = new AlertDialog.Builder(parentActivity).
		setTitle(str).
		setView(v).
		create();
		
		dlg.setCanceledOnTouchOutside(true);
		dlg.setButton(DialogInterface.BUTTON_POSITIVE, parentActivity.getText(android.R.string.ok), new android.content.DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		dlg.show();
	}

	private void add(LinearLayout layout, int labelId, String data) {
		View v = layoutInflater.inflate(R.layout.select_entry_simple, layout, false);
		TextView labelView = (TextView)v.findViewById(R.id.label);
		labelView.setText(labelId);		
		
		TextView dataView = (TextView)v.findViewById(R.id.data);
		dataView.setText(data);
		
		layout.addView(v);

		if (divider) {
			View divider = layoutInflater.inflate(R.layout.edit_divider, layout, false);
			layout.addView(divider);
			v.setTag(divider);
		}
	}
}

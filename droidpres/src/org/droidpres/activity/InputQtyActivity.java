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

import org.droidpres.R;
import org.droidpres.utils.Const;
import org.droidpres.utils.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class InputQtyActivity extends Activity implements OnClickListener, OnTouchListener {

	private Button mBtQtyPoint;
	private EditText mEdQty;
	private Vibrator mVibrator;
	private TextView mTvMessage;
	private Bundle mParams;
	private StringBuffer mQtyBuffer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dlg_qty_input);
		
		if (savedInstanceState != null) { 
			mParams = savedInstanceState.getBundle(Utils.getConstName(getClass(), "mParams"));
		} else {
			mParams = getIntent().getExtras();
		}

		mQtyBuffer = new StringBuffer();

		mBtQtyPoint = (Button) findViewById(R.id.btQtyPoint); 
		mEdQty = (EditText) findViewById(R.id.eQty);
		mTvMessage = (TextView) findViewById(R.id.tvQtyGoodsInfo);
				
		if (SetupActivity.getVibration(this)) {
			mVibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
			findViewById(R.id.btQty0).setOnTouchListener(this);
			findViewById(R.id.btQty1).setOnTouchListener(this);
			findViewById(R.id.btQty2).setOnTouchListener(this);
			findViewById(R.id.btQty3).setOnTouchListener(this);
			findViewById(R.id.btQty4).setOnTouchListener(this);
			findViewById(R.id.btQty5).setOnTouchListener(this);
			findViewById(R.id.btQty6).setOnTouchListener(this);
			findViewById(R.id.btQty7).setOnTouchListener(this);
			findViewById(R.id.btQty8).setOnTouchListener(this);
			findViewById(R.id.btQty9).setOnTouchListener(this);
			findViewById(R.id.btQtyC).setOnTouchListener(this);
			findViewById(R.id.btQtyPoint).setOnTouchListener(this);
			findViewById(R.id.btBackspace).setOnTouchListener(this);
		}

		findViewById(R.id.btQty0).setOnClickListener(this);
		findViewById(R.id.btQty1).setOnClickListener(this);
		findViewById(R.id.btQty2).setOnClickListener(this);
		findViewById(R.id.btQty3).setOnClickListener(this);
		findViewById(R.id.btQty4).setOnClickListener(this);
		findViewById(R.id.btQty5).setOnClickListener(this);
		findViewById(R.id.btQty6).setOnClickListener(this);
		findViewById(R.id.btQty7).setOnClickListener(this);
		findViewById(R.id.btQty8).setOnClickListener(this);
		findViewById(R.id.btQty9).setOnClickListener(this);
		findViewById(R.id.btQtyC).setOnClickListener(this);
		findViewById(R.id.btQtyPoint).setOnClickListener(this);
		findViewById(R.id.btBackspace).setOnClickListener(this);

		Button b = (Button)findViewById(R.id.bOK);
		b.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				close();
			}		
		});
		b = (Button)findViewById(R.id.bCancel);
		b.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				setResult(RESULT_CANCELED);
				finish();
			}		
		});
		
		
		if (mParams != null) {
			mTvMessage.setText(mParams.getString(Const.EXTRA_PRODUCT_NAME));
			setQty(mParams.getFloat(Const.EXTRA_QTY));
		}

	}
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btQty0:
			if (mQtyBuffer.length() > 0) mQtyBuffer.append("0");
			break;
		case R.id.btQty1:
			mQtyBuffer.append("1");
			break;
		case R.id.btQty2:
			mQtyBuffer.append("2");
			break;
		case R.id.btQty3:
			mQtyBuffer.append("3");
			break;
		case R.id.btQty4:
			mQtyBuffer.append("4");
			break;
		case R.id.btQty5:
			mQtyBuffer.append("5");
			break;
		case R.id.btQty6:
			mQtyBuffer.append("6");
			break;
		case R.id.btQty7:
			mQtyBuffer.append("7");
			break;
		case R.id.btQty8:
			mQtyBuffer.append("8");
			break;
		case R.id.btQty9:
			mQtyBuffer.append("9");
			break;
		case R.id.btQtyC:
			cleanQty();
			break;
		case R.id.btQtyPoint:
			if (mQtyBuffer.length() > 0) mQtyBuffer.append(".");
			else mQtyBuffer.append("0.");
			mBtQtyPoint.setEnabled(false);
			break;
		case R.id.btBackspace:
			int l = mQtyBuffer.length();
			if (l > 1) {
				if (mQtyBuffer.charAt(l -1) == '.') mBtQtyPoint.setEnabled(true);
				mQtyBuffer.deleteCharAt(l -1);
			} else {
				cleanQty();
			}
			break;
		}
		Buf2Screen();
	}

	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) mVibrator.vibrate(30);
		return false;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mParams.putFloat(Const.EXTRA_QTY, getQty());
		outState.putBundle(Utils.getConstName(getClass(), "mParams"), mParams);
	}

	/**
	 * Вывести буфер на экран
	 */
	private void Buf2Screen() {
		 if (mQtyBuffer.length() > 0) {
			 mEdQty.setText(mQtyBuffer.toString());
		 }
	}
	
	/**
	 * Получить введенное количество
	 * @return возвращает введенное количество
	 */
	private float getQty() {
		if (mQtyBuffer.length() > 0) { 
			Float qty = (float) Math.round(Float.parseFloat(mQtyBuffer.toString()) * 10000) / 10000;
			if (qty < 0.0) qty = (float) 0.0; 
			return qty;
		} else {
			return 0;
		}
	}
	
	/**
	 * Установить количество
	 * @param val требуемое количество
	 */
	private void setQty(float val) {
		if (val <= 0.0) {
			cleanQty();
			return;
		}
		if (mQtyBuffer.length() > 0) mQtyBuffer.delete(0, mQtyBuffer.length());
		mQtyBuffer.append(String.valueOf(val).replaceAll(".0$", ""));
		Buf2Screen();
		
		if (val - (int) val > 0) mBtQtyPoint.setEnabled(false);
		else mBtQtyPoint.setEnabled(true);
	}

	/**
	 * сбросить количество на 0
	 */
	private void cleanQty() {
		if (mQtyBuffer.length() > 0) mQtyBuffer.delete(0,mQtyBuffer.length());
		mEdQty.setText("");
		mBtQtyPoint.setEnabled(true);
	}
	
	private void close() {
		mParams.putFloat(Const.EXTRA_QTY, getQty());
		Intent data = new Intent();		
		data.putExtras(mParams);
		setResult(RESULT_OK, data);
		finish();
	}
}

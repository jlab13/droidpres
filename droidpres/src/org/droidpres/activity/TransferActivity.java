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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.conn.HttpHostConnectException;
import org.droidpres.R;
import org.droidpres.db.DBDroidPres;
import org.droidpres.db.QueryHelper;
import org.droidpres.utils.Const;
import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;
import org.xmlrpc.android.XMLRPCFault;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class TransferActivity extends Activity implements OnClickListener{
	private static final int SERVER_MOBILE 		= 0; 
	private static final int SERVER_WIFI 		= 1; 

	private static DecimalFormat cf;

	private SQLiteDatabase sDataBase;
	private Integer sAgentId;
	private boolean sWiFiFlag, sWiFiConnectFlag, sImportFlag, sStartTransferFlag;
	private boolean sNewVersion = false;
	private String sURL = "";

	private TextView sTvTrLog;
	private Button sBtExport, sBtImport;
	private Spinner sSpNetType;
	private XMLRPCClient sClientXMLRPC;
	private WiFiStateReceiver sWifiStateReceiver;
	private WifiManager sWiFiManager;
	private IntentFilter sFilter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.transfer);

        cf = SetupRootActivity.getCurrencyFormat(this);
		
        sBtExport = (Button) findViewById(R.id.btExport);
		sBtImport = (Button) findViewById(R.id.btImport);
        sTvTrLog = (TextView) findViewById(R.id.tvTransferLog);
        sSpNetType = (Spinner) findViewById(R.id.spNetType);

        sBtImport.setOnClickListener(this);
        sBtExport.setOnClickListener(this);
        
        sDataBase = (new DBDroidPres(this)).Open();
        
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
        		R.array.itemNetType, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sSpNetType.setAdapter(adapter);
        
        sAgentId = Integer.parseInt(SetupRootActivity.getAgentID(this));
	}
	
	public void onClick(View v) {
		sTvTrLog.setText("");

		switch (sSpNetType.getSelectedItemPosition()) {
		case SERVER_MOBILE:
			sURL = SetupRootActivity.getMobileServer(this);
			if (sURL.trim().length() == 0) {
				log(getString(R.string.err_ServerPort), "No setup url transfer server.");
				return;
			}
			setGuiEnabled(false);

			if (sURL.toLowerCase().indexOf("http://") < 0)
				sURL = "http://" + sURL + Const.RPC_PATH; 
				
			if (v.getId() == R.id.btImport)
				new ImportFromRCD().execute(sURL);
			else
				new ExportToRCD().execute(sURL);
			break;

		case SERVER_WIFI:
			sURL = SetupRootActivity.getWiFiServer(this);
			if (sURL.trim().length() == 0) {
				log(getString(R.string.err_ServerPort), "No setup url transfer server.");
				return;
			}
			setGuiEnabled(false);

			if (sURL.toLowerCase().indexOf("http://") < 0)
				sURL = "http://" + sURL + Const.RPC_PATH; 

			sImportFlag = v.getId() == R.id.btImport;

			sWiFiFlag = true;
			swichWiFi();
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		setBtExportEnable();
	}

	@Override
	protected void onDestroy() {
		sDataBase.close();
		super.onDestroy();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN &&
				sStartTransferFlag)	return true;
		else return super.onKeyDown(keyCode, event);
	}

	private void setBtExportEnable() {
		Cursor cur = sDataBase.rawQuery("select count(*) + (select count(*) from " + 
				DBDroidPres.TABLE_LOCATION +") from " +
				DBDroidPres.TABLE_DOCUMENT + " where docstate = " +
				Const.DOCSTATE_PREPARE_SEND, null);
		if (cur.moveToFirst()) {
			boolean flag = cur.getInt(0) > 0;
			sBtExport.setEnabled(flag);
		} else {
			sBtExport.setEnabled(false);
		}
		cur.close();
	}

	private void setGuiEnabled(boolean flag) {
		if (!flag) sBtExport.setEnabled(false);
		else setBtExportEnable();
		sBtImport.setEnabled(flag);
	    sSpNetType.setEnabled(flag);
	}

	private void log(String msg, String error) {
		if (error == null) {
			sTvTrLog.append(msg+"\n");
		} else {
			sTvTrLog.append("\nERROR: "+error+"\n\n");
			new AlertDialog.Builder(this)
			.setMessage(msg)
			.setTitle(android.R.string.dialog_alert_title)
			.setPositiveButton(android.R.string.ok, null)
			.setCancelable(true)
			.show();
		}
	}

	private class ExportToRCD extends AsyncTask<String, String, Void> {
		@Override
		protected Void doInBackground(String... params) {
			try {
				publishProgress(getString(R.string.msg_StartExport), null);
				
				int send_doc_coun = 0;
				Cursor cur_docdet = null;
				Cursor cur_doc = sDataBase.rawQuery("select d.*, c.name, t.paytype1or2 " +
						"from document d inner join typedoc t on (t._id = d.typedoc_id) " +
						"inner join client c on (c._id = d.client_id) " +
						"where docstate = " + Const.DOCSTATE_PREPARE_SEND , null);
				
				sClientXMLRPC = new XMLRPCClient(params[0], SetupRootActivity.getHttpLogin(
						TransferActivity.this),	SetupRootActivity.getHttpPasswd(TransferActivity.this));

				QueryHelper qh_docdet = new QueryHelper(DBDroidPres.TABLE_DOCUMENT_DET, null); 
				//DataSet dsDocDet = new DataSet(sDataBase, DBDroidPres.TABLE_DOCUMENT_DET);
				Integer central_id = 0;

				final Map<String, Object> dict = new HashMap<String, Object>();
				final List<Object> items = new ArrayList<Object>();
				if (cur_doc.moveToFirst()) {
					do {
						final long _id = QueryHelper.fieldByNameLong(cur_doc, QueryHelper.KEY_ID);
						dict.clear();
						dict.put("_id", _id);
						dict.put("agent_id", sAgentId);
						dict.put("presventype",  QueryHelper.fieldByNameInt(cur_doc, "presventype"));
						dict.put("client_id", QueryHelper.fieldByNameInt(cur_doc, "client_id"));
						dict.put("docdate", QueryHelper.fieldByNameString(cur_doc, "docdate"));
						dict.put("doctime", QueryHelper.fieldByNameString(cur_doc, "doctime"));
						final String desc = QueryHelper.fieldByNameString(cur_doc, "description");
						if (desc.length() > 0) dict.put("description", desc);
						dict.put("paytype", QueryHelper.fieldByNameInt(cur_doc, "paytype"));
						dict.put("paytype1or2", QueryHelper.fieldByNameInt(cur_doc, "paytype1or2"));
						dict.put("paymentdate", QueryHelper.fieldByNameString(cur_doc, "paymentdate"));
						dict.put("typedoc_id", QueryHelper.fieldByNameInt(cur_doc, "typedoc_id"));
						dict.put("itemcount", QueryHelper.fieldByNameFloat(cur_doc, "itemcount"));
						dict.put("mainsumm", QueryHelper.fieldByNameFloat(cur_doc, "mainsumm"));
						
						items.clear();
						qh_docdet.appendFilter("DOCUMENT_ID", QueryHelper.FILTER_AND, "document_id = %d", _id);
						if (cur_docdet != null) cur_docdet.close();
						cur_docdet = qh_docdet.createCurcor(sDataBase);
						if (cur_docdet.moveToFirst()) {
							do {
								Map<String, Object> mapDocDet = new HashMap<String, Object>();
								mapDocDet.put("product_id", QueryHelper.fieldByNameInt(cur_docdet, "product_id"));
								mapDocDet.put("qty", QueryHelper.fieldByNameFloat(cur_docdet, "qty"));
								mapDocDet.put("price", QueryHelper.fieldByNameFloat(cur_docdet, "price"));
								items.add(mapDocDet);
							} while (cur_docdet.moveToNext());

							Object[] XMLRPCDocParams = {dict, items};
							central_id = (Integer) sClientXMLRPC.callEx("SetDoc", XMLRPCDocParams);
							publishProgress(QueryHelper.fieldByNameString(cur_doc, "name") + " " +
									QueryHelper.fieldByNameString(cur_doc, "docdate") + " " +
									cf.format(QueryHelper.fieldByNameFloat(cur_doc, "mainsumm")) , null);
							
							if (central_id != null && central_id > 0) {
								ContentValues values = new ContentValues();
								values.put("docstate", 2);
								values.put("central_id", central_id);
								sDataBase.update(DBDroidPres.TABLE_DOCUMENT, values,
										QueryHelper.KEY_ID + " = " + _id, null);
								send_doc_coun++;
							}
						}
					} while (cur_doc.moveToNext());
					cur_docdet.close();
					cur_doc.close();
					dict.clear();
					items.clear();
					
					final Cursor cur = sDataBase.query(DBDroidPres.TABLE_LOCATION, null, null, null,
							null, null, null);
					if (cur.moveToFirst()) do {
						Map<String, Object> row = new HashMap<String, Object>();
						row.put("date_location", cur.getString(0));
						row.put("provider", cur.getString(1));
						row.put("lat", cur.getInt(2));
						row.put("lon", cur.getInt(3));
						row.put("accuracy", cur.getInt(4));
						items.add(row);
					} while (cur.moveToNext());
					cur.close();
					Object result = sClientXMLRPC.call("SetLocation", sAgentId, items);
					items.clear();
					if ((Boolean)result) {
						sDataBase.delete(DBDroidPres.TABLE_LOCATION, null, null);
					}
					
				}
				publishProgress(getString(R.string.msg_StopExport) , null);
				publishProgress(getString(R.string.msg_SendDocCount, send_doc_coun) , null);
				
			} catch (final XMLRPCFault e) {
				Log.e("Transfer", "XMLRPCFault", e);
				publishProgress(getString(R.string.err_XMLRPCFault), e.getMessage());
			} catch (final XMLRPCException e) {
				Throwable couse = e.getCause();
				if (couse instanceof HttpHostConnectException) {
					Log.e("Transfer", "HttpHostConnectException", e);
					publishProgress(getString(R.string.err_HttpHostConnectException), e.getMessage());
				} else if (couse instanceof SocketTimeoutException) {
					Log.e("Transfer", "SocketTimeoutException", e);
					publishProgress(getString(R.string.err_SocketTimeoutException), e.getMessage());
				} else {
					Log.e("Transfer", "XMLRPCException", e);
					publishProgress(getString(R.string.err_XMLRPCException), e.getMessage());
				}
			} catch (Exception e) {
				Log.e("Transfer", "UnknownException", e);
				publishProgress(getString(R.string.err_UnknownException), e.toString());
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(String... progress) {
			log(progress[0], progress[1]);
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setProgressBarIndeterminateVisibility(true);
			sStartTransferFlag = true;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			setProgressBarIndeterminateVisibility(false);
			sStartTransferFlag = false;
			if (sSpNetType.getSelectedItemPosition() == SERVER_WIFI) {
				sWiFiFlag = false;
				swichWiFi();
			}
			setGuiEnabled(true);
	     }
	}
	
	private class ImportFromRCD extends AsyncTask<String, String, Void> {
		@Override
		protected Void doInBackground(String... params) {
			try {
				publishProgress(getString(R.string.msg_StartImport, params[0]), null);
				sClientXMLRPC = new XMLRPCClient(params[0], SetupRootActivity.getHttpLogin(
						TransferActivity.this), SetupRootActivity.getHttpPasswd(TransferActivity.this));
				publishProgress(getString(R.string.msg_GetRefClientGroup), null);
				getReferenceFromRCD(sDataBase, DBDroidPres.TABLE_CLIENT_GROUP, "GetRefClientGroup");
				publishProgress(getString(R.string.msg_GetRefClient), null);
				getReferenceFromRCD(sDataBase, DBDroidPres.TABLE_CLIENT, "GetRefClient");
				publishProgress(getString(R.string.msg_GetRefProductGroup), null);
				getReferenceFromRCD(sDataBase, DBDroidPres.TABLE_PRODUCT_GROUP, "GetRefProductGroup");
				publishProgress(getString(R.string.msg_GetRefProduct), null);
				getReferenceFromRCD(sDataBase, DBDroidPres.TABLE_PRODUCT, "GetRefProduct");
				publishProgress(getString(R.string.msg_GetRefTypeDoc), null);
				getReferenceFromRCD(sDataBase, DBDroidPres.TABLE_TYPEDOC, "GetRefTypeDoc");
				publishProgress(getString(R.string.msg_StopImport), null);
				sNewVersion = getUppdateApp();

			} catch (final XMLRPCFault e) {
				Log.e("ImportFromRCD", "XMLRPCFault", e);
				publishProgress(getString(R.string.err_XMLRPCFault), e.toString());
			} catch (final IOException e) {
				Log.e("ImportFromRCD", "IOException", e);
				publishProgress(getString(R.string.err_IOException), e.toString());
			} catch (final NameNotFoundException e) {
				Log.e("ImportFromRCD", "NameNotFoundException", e);
				publishProgress(getString(R.string.err_NameNotFoundException), e.toString());
			} catch (final XMLRPCException e) {
				Throwable couse = e.getCause();
				if (couse instanceof HttpHostConnectException) {
					Log.e("ImportFromRCD", "HttpHostConnectException", e);
					publishProgress(getString(R.string.err_HttpHostConnectException), e.toString());
				} else if (couse instanceof SocketTimeoutException) {
					Log.e("ImportFromRCD", "SocketTimeoutException", e);
					publishProgress(getString(R.string.err_SocketTimeoutException), e.toString());
				} else {
					Log.e("ImportFromRCD", "XMLRPCException", e);
					publishProgress(getString(R.string.err_XMLRPCException), e.toString());
				}
			} catch (Exception e) {
				Log.e("ImportFromRCD", "UnknownException", e);
				publishProgress(getString(R.string.err_UnknownException), e.toString());
			}

			return null;
		}
		
		@Override
		protected void onProgressUpdate(String... progress) {
			log(progress[0], progress[1]);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setProgressBarIndeterminateVisibility(true);
			sStartTransferFlag = true;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			setProgressBarIndeterminateVisibility(false);
			sStartTransferFlag = false;
			if (sSpNetType.getSelectedItemPosition() == SERVER_WIFI) {
				sWiFiFlag = false;
				swichWiFi();
			}
			setGuiEnabled(true);
			if (sNewVersion) updateApp();
		}

		@SuppressWarnings("unchecked")
		private void getReferenceFromRCD(SQLiteDatabase db, String table_name,
				String method_name) throws Exception {
			final Object[] params = {sAgentId};
			final Object result = sClientXMLRPC.callEx(method_name, params);
			final Object[] arr = (Object[]) result;
			
			publishProgress(getString(R.string.msg_ReturNRecords, arr.length), null);

			Map<String, Object> map;
				
			String[] fields = QueryHelper.getFieldsOnTable(db, table_name);
			Arrays.sort(fields);

			db.execSQL("delete from " + table_name);
			InsertHelper ih = new InsertHelper(db, table_name);
			//ContentValues values = new ContentValues();

			int processedCount = 0;
			for (int row = 0; row < arr.length; row++) {
				map = (Map<String, Object>) arr[row];
				ih.prepareForInsert();
				for (String key: map.keySet()) {  
					final String _key = key.toLowerCase();
					
					if (Arrays.binarySearch(fields, _key) >= 0) {   
						//values.put(_key, map.get(key).toString());
						ih.bind(ih.getColumnIndex(_key) , map.get(key).toString());
					}
					
					//TODO: Временно
					if (_key.equals("name") && (Arrays.binarySearch(fields, "name_case") >= 0)) {
						//values.put("name_case", map.get(key).toString().toUpperCase());
						ih.bind(ih.getColumnIndex("name_case") , map.get(key).toString().toUpperCase());
					}
				}
				ih.execute();
				//db.insert(table_name, null, values);
				//values.clear();
				processedCount++;
			}
			ih.close();
			publishProgress(getString(R.string.msg_UpdateNRecords, processedCount), null);
		}
		
		
		private boolean getUppdateApp() throws XMLRPCException, IOException {
			byte[] data = (byte[]) sClientXMLRPC.callEx("GetUpdateApp", 
					new Object[] {MainActivity.versionCode});
			if (data.length > 1) {
				File apkFile = new File(SetupRootActivity.getApkFileName());
				apkFile.createNewFile();
				FileOutputStream fOut = new FileOutputStream(apkFile);
				fOut.write(data);
				fOut.flush();
				fOut.close();
				return true;
			} else
				return false;
		}
	}
	
	public void swichWiFi() {
		sWiFiConnectFlag = false;
		
		IntentFilter filter = sFilter;
		WiFiStateReceiver receiver = sWifiStateReceiver;
		if (receiver == null) {
			receiver = new WiFiStateReceiver();
			sWifiStateReceiver = receiver;
			filter = new IntentFilter();
			filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
			filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
			sFilter = filter;
		}
		this.registerReceiver(receiver, filter);
		getWiFiManager().setWifiEnabled(sWiFiFlag);
		
		if (sWiFiFlag) {
			new Timer().schedule(new TimerTask() {
				
				@Override
				public void run() {
					if (!sWiFiConnectFlag) {
						sWiFiFlag = false;
						getWiFiManager().setWifiEnabled(sWiFiFlag);
					}
				}
			}, 90000);
		}
	}	
	
	class WiFiStateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null)	return;

			if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
				switch (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)) {
				case WifiManager.WIFI_STATE_DISABLING:
					log(getString(R.string.msg_WiFiState_DISABLING), null);
					break;

				case WifiManager.WIFI_STATE_DISABLED:
					log(getString(R.string.msg_WiFiState_DISABLED), null);
					break;

				case WifiManager.WIFI_STATE_ENABLING:
					log(getString(R.string.msg_WiFiState_ENABLING), null);
					break;

				case WifiManager.WIFI_STATE_ENABLED:
					log(getString(R.string.msg_WiFiState_ENABLED), null);
					break;

				default:
					log(getString(R.string.msg_WiFiState_UNKNOWN), null);
					break;
				}
			} else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
				NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				switch (info.getDetailedState()) {
					case CONNECTING:
						log(getString(R.string.msg_NetworkState_CONNECTING), null);
						break;
					case OBTAINING_IPADDR:
						log(getString(R.string.msg_NetworkState_OBTAINING_IPADDR), null);
						break;
					case DISCONNECTING:
						log(getString(R.string.msg_NetworkState_DISCONNECTING), null);
						break;
					case DISCONNECTED:
						log(getString(R.string.msg_NetworkState_DISCONNECTED), null);
						break;
					case FAILED:
						log(getString(R.string.msg_NetworkState_FAILED), null);
						break;
					case CONNECTED:
						sWiFiConnectFlag = true;
						log(getString(R.string.msg_NetworkState_CONNECTED, 
								getWiFiManager().getConnectionInfo().getSSID()), null);

						if (sWiFiFlag && !sStartTransferFlag) { 
							if (sImportFlag)
								new ImportFromRCD().execute(sURL);
							else
								new ExportToRCD().execute(sURL);
						}
						break;
				}
			}
		}
	}
	
	private WifiManager getWiFiManager() {
		if (sWiFiManager == null) {
			sWiFiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		}
		return sWiFiManager;
	}
	
	private void updateApp() {
		new AlertDialog.Builder(this)
		.setMessage(R.string.msg_UpdateApk)
		.setTitle(android.R.string.dialog_alert_title)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				File apkFile = new File(SetupRootActivity.getApkFileName());
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
				startActivity(intent);
			}
		})
		.setCancelable(false).show();
	}
}

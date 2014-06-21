package com.congvc.ntpclock;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

/**
 * Class common for activity
 * 
 * @author CongVo
 *
 */
public class BaseActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	/**
	 * Get unique id of device android
	 * 
	 * @return
	 */
	public String getDeviceUniqueId() {
		
		String result = "";
		
		TelephonyManager mngr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE); 
		result = mngr.getDeviceId();
		
		if(null == result || result.equals("")){
			WifiManager wm = (WifiManager)getSystemService(Context.WIFI_SERVICE);
			result = wm.getConnectionInfo().getMacAddress();
		}
		
		if(null == result || result.equals("")){
			result = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
		}
		
		return result;
	}
	
	/**
	 * Create dialog show message report
	 * 
	 * @param title
	 * @param message
	 * @return
	 */
	public Dialog createDialogMessage(String title, String message) {
		return new AlertDialog.Builder(this)
		.setTitle(title)
		.setMessage(message)
		.setPositiveButton("OK",
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int whichButton) {
					
				}
			})
		.create();
	}
	
	/**
	 * Check network connect
	 * 
	 * @param context
	 * @return true if network is connected
	 */
	public boolean isNetworkAvailable() {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] infoset = mConnectivityManager.getAllNetworkInfo();
		for (NetworkInfo info : infoset) {
			if (info.isAvailable() && info.isConnected()){
				return true;
			}
		}
		return false;
	}

}

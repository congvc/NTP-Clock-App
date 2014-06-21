package com.congvc.ntpclock;

import java.net.InetAddress;
import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.congvc.ntpclock.view.MyAnalogClock;

/**
 * Class screen analog clock.
 * 
 * @author CongVo
 * @since 21/06/2014
 *
 */
public class MainActivity extends BaseActivity implements OnClickListener {
	
	//NTP server list: http://tf.nist.gov/tf-cgi/servers.cgi
	private static final String TIME_SERVER = "0.ubuntu.pool.ntp.org";//time-a.nist.gov
	private static final String FORMAT_COUNTDOWNTIMER = "%02d:%02d";
	private static final long TIME_COUNTDOWN = 1000*60*10;
	
	private CountDownTimer mCountDownTimer;
	
	/**** Controls view  *****/
	private TextView tvCountDownTimer;
	private Button btnSync;
	private MyAnalogClock analogClock;
	
	/**** Available date and time ******/
	private int mHours = 0;
	private int mMinutes = 0;
	private int mSeconds = 0;
	private String mDateNTP = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		/* Init controls from xml layout file */
		tvCountDownTimer = (TextView) findViewById(R.id.tvCountDownTimer);
		btnSync = (Button) findViewById(R.id.btnSyncManual);
		analogClock = (MyAnalogClock) findViewById(R.id.analogClock);
		
		// Init event click button Sync 
		btnSync.setOnClickListener(this);
		
		// Call task to process get network time from NTP server
		new GetNTPTask().execute();

	}
	
	/**
	 * Class process sync network time from NTP server
	 * 
	 */
	private class GetNTPTask extends AsyncTask<Object, Object, Object> {
		
		private Date dateNTP = null;
		
		private ProgressDialog dialog;
		
		@Override
		protected void onPreExecute() {
			
  	  		runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					dialog = new ProgressDialog(MainActivity.this);
		  	  	  	dialog.setIndeterminate(true);
		  	  	  	dialog.setCancelable(false);
			  	  	dialog.setMessage(getString(R.string.sync_progress));
		  	  	  	dialog.show();
				}
			});
  	  	  	
		}

		@Override
		protected Object doInBackground(Object... params) {
			try {
			
				NTPUDPClient timeClient = new NTPUDPClient();
				timeClient.setDefaultTimeout(10000);
//				timeClient.setSoTimeout(10000);
			    InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
			    TimeInfo timeInfo = timeClient.getTime(inetAddress);
			    
			    //long returnTime = timeInfo.getReturnTime();   //local device time
			    long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();   //server time
	
			    Date time = new Date(returnTime);
			    
			    dateNTP = time;
			
			} catch (Exception e) {
				e.printStackTrace();
				dateNTP = null;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			
			super.onPostExecute(result);
			
			try {
		  	    if (dialog.isShowing())
		  	    	dialog.dismiss();
		  	   } catch (Exception e) {
		  	   }
			
			// Get server time successful
			if(null != dateNTP){
				
				mDateNTP = DateFormat.format("dd-MM-yyyy",dateNTP).toString();
			    
				Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
				calendar.setTime(dateNTP);   // assigns calendar to given date 
				mHours = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
//				calendar.get(Calendar.HOUR);        // gets hour in 12h format
				mMinutes = calendar.get(Calendar.MINUTE);
				mSeconds = calendar.get(Calendar.SECOND);
				
				// Reset count down timer
			    resetCountDownTimer();
			}
			// Get server time fail: show message dialog error
			// Reset count down timer when dialog dismiss
			else {
				 Dialog dialog = createDialogMessage(getString(R.string.error), 
						 							getString(R.string.msg_sync_ntp_error));
				 dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					
					@Override
					public void onDismiss(DialogInterface dialog) {
						// Reset count down timer
						resetCountDownTimer();
					}
				});
				 dialog.show();
			}
			
			// Update date and time into analog clock
			analogClock.setTime(mHours, mMinutes, mSeconds, mDateNTP);
		}
		
	}
	
	/**
	 * Method process reset count down timer
	 */
	private void resetCountDownTimer(){
		if(null != mCountDownTimer){
			mCountDownTimer.cancel();
		}
		
		// Init new count down timer 
		mCountDownTimer = new CountDownTimer(TIME_COUNTDOWN, 1000) {
			
			@Override
			public void onTick(long millisUntilFinished) {
				long minuteTimer = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) 
						- TimeUnit.HOURS.toMinutes(
								TimeUnit.MILLISECONDS.toHours(millisUntilFinished));
				long secondTimer = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) 
						- TimeUnit.MINUTES.toSeconds(
								TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished));
				// Update time to textview 
				tvCountDownTimer.setText(""+String.format(FORMAT_COUNTDOWNTIMER,
														minuteTimer,
														secondTimer));
			}
			
			@Override
			public void onFinish() {
				tvCountDownTimer.setText(getString(R.string.time_def_countdown));
				// Call task to process get network time from NTP server
				new GetNTPTask().execute();
			}
		}.start();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnSyncManual:
			tvCountDownTimer.setText(getString(R.string.time_def_countdown));
			if(null != mCountDownTimer){
				mCountDownTimer.cancel();
			}
			// Call task to process get network time from NTP server
			new GetNTPTask().execute();
			break;

		default:
			break;
		}
	}


}

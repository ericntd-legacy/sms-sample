package com.example.test;

import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class AboutActivity extends Activity {

	// debugging
	private final static String TAG = "Test/AboutActivity";

	private SmsReceiver mSmsReceiver;
	private SmsReceiver sentReportReceiver;
	private SmsReceiver deliveredReportReceiver;

	// actions/ intent filters
	private final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	private final String SMS_SENT = "SMS_SENT";
	private final String SMS_DELIVERED = "SMS_DELIVERED";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.w(TAG, "onCreate");
		setContentView(R.layout.activity_about);

		mSmsReceiver = new SmsReceiver();
		sentReportReceiver = new SmsReceiver();
		deliveredReportReceiver = new SmsReceiver();
		
		/*
		 * defautl the value of "receivedsms" to false
		 */
		SharedPreferences prefs = getSharedPreferences("prefs",Context.MODE_PRIVATE);
		SharedPreferences.Editor prefseditor = prefs.edit();
		prefseditor.putBoolean("receivedsms", false);
		prefseditor.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.about, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.ActionMain:
			Intent i = new Intent(this, MainActivity.class);
			startActivity(i);
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.w(TAG, "onresume");
		IntentFilter iff = new IntentFilter();
		iff.addAction("android.provider.Telephony.SMS_RECEIVED");
		// Put whatever message you want to receive as the action
		this.registerReceiver(this.mSmsReceiver, iff);

		// to receive delivery report of sms
		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(
				SMS_SENT), 0);

		PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
				new Intent(SMS_DELIVERED), 0);

		IntentFilter if1 = new IntentFilter();
		if1.addAction(SMS_SENT);
		this.registerReceiver(this.sentReportReceiver, if1);

		IntentFilter if2 = new IntentFilter();
		if2.addAction(SMS_DELIVERED);
		this.registerReceiver(this.deliveredReportReceiver, if2);

		// Send an sms
		SmsManager sm = SmsManager.getDefault();
		//sm.sendTextMessage("93628809", null,"a message from Test/AboutActivity", sentPI, deliveredPI);

	}

	@Override
	public void onPause() {
		super.onPause();
		Log.w(TAG, "onPause");
		if (this.mSmsReceiver!=null) this.unregisterReceiver(this.mSmsReceiver);
		if (this.sentReportReceiver!=null) this.unregisterReceiver(this.sentReportReceiver);
		if (this.deliveredReportReceiver!=null) this.unregisterReceiver(this.deliveredReportReceiver);
	}
	
	public static void receivedBroadcast(Intent i, Context context) {
		// SmsReceiver will try to trigger this
		//
		Log.w(TAG, "received SMS - AboutActivity");
		// at this point, sharedpreferences 'receivedsms' should already be
		// updated thanks to the smsreceiver
		SharedPreferences prefs = context.getSharedPreferences("prefs",
				Context.MODE_PRIVATE);
		Log.w(TAG, "received sms? " + prefs.getBoolean("receivedsms", false)); // expecting
																				// true
	}

}

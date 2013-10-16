package com.example.test;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {

	private final static String TAG = "Test/SmsReceiver";

	// actions/ intent filters
	private final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	private final String SMS_SENT = "SMS_SENT";
	private final String SMS_DELIVERED = "SMS_DELIVERED";

	@Override
	public void onReceive(Context context, Intent intent) {
		// Toast.makeText(context.getApplicationContext(), "got an sms",
		// Toast.LENGTH_LONG).show();
		Log.w(TAG, "what is the result code for sms reception "
				+ getResultCode() + " action is " + intent.getAction()+" "+intent.getDataString());
		intent.putExtra("IntentSource", "this comes from the sms receiver");

		// is this restarting the activity or somehow access the live one
		// magically?
		// MainActivity.receivedBroadcast(intent, context); // without calling
		// this the activity UI won't see the updated SharedPreferences
		String action = intent.getAction();
		if (action.equals(SMS_RECEIVED)) {
			/*
			 * updating a sharedpreferences boolean value, hopefully the
			 * UI activity can see the updated value after that
			 */
			
			SharedPreferences prefs = context.getSharedPreferences("prefs",Context.MODE_PRIVATE);
			SharedPreferences.Editor prefseditor = prefs.edit();
			prefseditor.putBoolean("receivedsms", true);
			prefseditor.commit();
			
			Map<String, String> msg = retrieveMessages(intent);

			Log.w(TAG, "we received " + msg.size()
					+ " messages in total; 1st message is ");
			Toast.makeText(context, "received sms", Toast.LENGTH_LONG).show();
			
			// call the UI Activity
			int activitySwitch = 1;
			if (activitySwitch==1) {
				MainActivity.receivedBroadcast(intent, context); // without calling
				// this the activity UI won't see the updated SharedPreferences
			} else if (activitySwitch==2)  {
				AboutActivity.receivedBroadcast(intent, context); // without calling
				// this the activity UI won't see the updated SharedPreferences
			}
		} else if (action.equals(SMS_SENT)) {
			Log.w(TAG, "received sent sms report");
			handleSentSms(context);
		} else if (action.equals(SMS_DELIVERED)) {
			Log.w(TAG, "received delivered sms report");
			handleDeliveredSms(context);
		}
	}

	private void handleSentSms(Context context) {
		switch (getResultCode()) {
		case Activity.RESULT_OK:
			Log.w(TAG, "SMS sent");
			Toast.makeText(context, "SMS sent", Toast.LENGTH_SHORT).show();

			// TextView debug = (TextView) findViewById(R.id.DebugMessages);
			// debug.append("SMS sent");
			break;
		case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
			Toast.makeText(context, "Generic failure", Toast.LENGTH_SHORT)
					.show();
			Log.w(TAG, "Generic failure");
			break;
		case SmsManager.RESULT_ERROR_NO_SERVICE:
			Toast.makeText(context, "No service", Toast.LENGTH_SHORT).show();
			Log.w(TAG, "No service");
			break;
		case SmsManager.RESULT_ERROR_NULL_PDU:
			Toast.makeText(context, "Null PDU", Toast.LENGTH_SHORT).show();
			Log.w(TAG, "Null PDU");
			break;
		case SmsManager.RESULT_ERROR_RADIO_OFF:
			Toast.makeText(context, "Radio off", Toast.LENGTH_SHORT).show();
			Log.w(TAG, "Radio off");
			break;
		}
	}

	private void handleDeliveredSms(Context context) {
		switch (getResultCode()) {
		case Activity.RESULT_OK:
			Log.w(TAG, "SMS delivered");
			Toast.makeText(context, "SMS delivered", Toast.LENGTH_SHORT).show();

			// TextView debug = (TextView) findViewById(R.id.DebugMessages);
			// debug.append("SMS sent");
			break;
		case Activity.RESULT_CANCELED:
			Toast.makeText(context, "SMS not delivered", Toast.LENGTH_SHORT)
					.show();
			Log.w(TAG, "SMS not delivered");
			break;
		}
	}

	private Map<String, String> retrieveMessages(Intent intent) {
		Map<String, String> msg = null;
		SmsMessage[] msgs = null;
		Bundle bundle = intent.getExtras();

		if (bundle != null && bundle.containsKey("pdus")) {
			Object[] pdus = (Object[]) bundle.get("pdus");

			if (pdus != null) {
				int nbrOfpdus = pdus.length;
				msg = new HashMap<String, String>(nbrOfpdus);
				msgs = new SmsMessage[nbrOfpdus];

				// There can be multiple SMS from multiple senders, there
				// can be
				// a maximum of nbrOfpdus different senders
				// However, send long SMS of same sender in one message
				for (int i = 0; i < nbrOfpdus; i++) {
					msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);

					String originatinAddress = msgs[i].getOriginatingAddress();

					// Check if index with number exists
					if (!msg.containsKey(originatinAddress)) {
						// Index with number doesn't exist
						// Save string into associative array with sender
						// number
						// as index
						msg.put(msgs[i].getOriginatingAddress(),
								msgs[i].getMessageBody());

					} else {
						// Number has been there, add content but consider
						// that
						// msg.get(originatinAddress) already contains
						// sms:sndrNbr:previousparts of SMS,
						// so just add the part of the current PDU
						String previousparts = msg.get(originatinAddress);
						String msgString = previousparts
								+ msgs[i].getMessageBody();
						msg.put(originatinAddress, msgString);
					}
				}
			}
		}

		return msg;
	}

}

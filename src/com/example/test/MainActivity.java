package com.example.test;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	private final static String TAG = "Test/MainActivity";

	boolean mIsBound = false;
	Messenger mService = null;

	private SmsReceiver mSmsReceiver;
	private SmsReceiver sentReportReceiver;
	private SmsReceiver deliveredReportReceiver;

	// actions/ intent filters
	private final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	private final String SMS_SENT = "SMS_SENT";
	private final String SMS_DELIVERED = "SMS_DELIVERED";

	private boolean isMyServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (MessengerService.class.getName().equals(
					service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.w(TAG, "onCreate");
		setContentView(R.layout.activity_main);

		Button bind = (Button) findViewById(R.id.button1);
		bind.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// doBindService();
			}
		});

		Button unbind = (Button) findViewById(R.id.button2);
		unbind.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doUnbindService();
			}
		});

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
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.ActionAbout:
			Intent i = new Intent(this, AboutActivity.class);
			startActivity(i);
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		
		
		Log.w(TAG, "onresume");
		IntentFilter iff = new IntentFilter();
		iff.addAction(SMS_RECEIVED);
		// Put whatever message you want to receive as the action
		this.registerReceiver(this.mSmsReceiver, iff);

		// to receive delivery report of sms
		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SMS_SENT), 0);

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
		//sm.sendTextMessage("93628809", null,"a message from Test/MainActivity", sentPI, deliveredPI);

	}

	@Override
	public void onPause() {
		super.onPause();
		Log.w(TAG, "onPause");
		this.unregisterReceiver(this.mSmsReceiver);
		this.unregisterReceiver(this.sentReportReceiver);
		this.unregisterReceiver(this.deliveredReportReceiver);
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.w(TAG, "onStart");
	}

	class TempHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MessengerService.MSG_SAY_GOODBYE:
				Toast.makeText(MainActivity.this,
						"Received from service: " + msg.arg1, 1000).show();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(new TempHandler());

	/**
	 * Class for interacting with the main interface of the service.
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. We are communicating with our
			// service through an IDL interface, so get a client-side
			// representation of that from the raw service object.
			mService = new Messenger(service);
			// mCallbackText.setText("Attached.");

			// We want to monitor the service for as long as we are
			// connected to it.
			try {
				Message msg = Message.obtain(null,
						MessengerService.MSG_SAY_HELLO);
				msg.replyTo = mMessenger;
				mService.send(msg);

				// Give it some value as an example.
				// msg = Message.obtain(null,
				// MessengerService.MSG_E, this.hashCode(), 0);
				// mService.send(msg);
			} catch (RemoteException e) {
				// In this case the service has crashed before we could even
				// do anything with it; we can count on soon being
				// disconnected (and then reconnected if it can be restarted)
				// so there is no need to do anything here.
			}

			// As part of the sample, tell the user what happened.
			Toast.makeText(MainActivity.this, "remote_service_connected",
					Toast.LENGTH_SHORT).show();
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mService = null;
			// mCallbackText.setText("Disconnected.");

			// As part of the sample, tell the" user what happened.
			Toast.makeText(MainActivity.this, "remote_service_disconnected",
					Toast.LENGTH_SHORT).show();
		}
	};

	void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because there is no reason to be able to let other
		// applications replace our component.
		bindService(new Intent(MainActivity.this, MessengerService.class),
				mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
		Toast.makeText(MainActivity.this, "Binding", 1000).show();
	}

	void doUnbindService() {
		if (mIsBound) {
			// If we have received the service, and hence registered with
			// it, then now is the time to unregister.
			if (mService != null) {
				try {
					Message msg = Message.obtain(null,
							MessengerService.MSG_SAY_GOODBYE);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {
					// There is nothing special we need to do if the service
					// has crashed.
				}
			}

			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
			Toast.makeText(MainActivity.this,
					"UnBinding" + isMyServiceRunning(), 1000).show();

		}
	}

	/*
	 * private void doSendEcho() { if(isMyServiceRunning()) // if service is
	 * running { if (mMessengerService != null) { Message msg =
	 * Message.obtain(null, MessengerService.MSG_ECHO, 12345, 0); msg.replyTo =
	 * mMessenger; try { mMessengerService.send(msg); } catch (RemoteException
	 * e) { // TODO Auto-generated catch block e.printStackTrace(); } } } }
	 */

	@Override
	protected void onStop() {
		super.onStop();
		// Unbind from the service
		// unbindService(mConnection);
		Log.i(TAG, "" + isMyServiceRunning());
		Log.i(TAG, "Service Stopped");
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
	}

	public static void receivedBroadcast(Intent i, Context context) {
		// SmsReceiver will try to trigger this
		//
		Log.w(TAG, "received SMS - MainActivity");
		// at this point, sharedpreferences 'receivedsms' should already be
		// updated thanks to the smsreceiver
		SharedPreferences prefs = context.getSharedPreferences("prefs",
				Context.MODE_PRIVATE);
		Log.w(TAG, "received sms? " + prefs.getBoolean("receivedsms", false)); // expecting
																				// true
	}

	/*
	 * private BroadcastReceiver mSmsReceiver = new BroadcastReceiver() {
	 * 
	 * @Override public void onReceive(Context context, Intent intent) {
	 * Toast.makeText(getApplicationContext(), "got an sms",
	 * Toast.LENGTH_LONG).show(); intent.putExtra("IntentSource",
	 * "this comes from the sms receiver");
	 * 
	 * // updating a sharedpreferences boolean value, hopefully the // activity
	 * can see the updated value after that SharedPreferences prefs =
	 * getSharedPreferences("prefs", Context.MODE_PRIVATE);
	 * SharedPreferences.Editor prefseditor = prefs.edit();
	 * prefseditor.putBoolean("receivedsms", true); prefseditor.commit();
	 * 
	 * // is this restarting the activity or somehow access the live one //
	 * magically? MainActivity.receivedBroadcast(intent, context);
	 * 
	 * Map<String, String> msg = retrieveMessages(intent);
	 * 
	 * Log.w(TAG, "we received " + msg.size() + " messages in total");
	 * 
	 * }
	 * 
	 * private Map<String, String> retrieveMessages(Intent intent) { Map<String,
	 * String> msg = null; SmsMessage[] msgs = null; Bundle bundle =
	 * intent.getExtras();
	 * 
	 * if (bundle != null && bundle.containsKey("pdus")) { Object[] pdus =
	 * (Object[]) bundle.get("pdus");
	 * 
	 * if (pdus != null) { int nbrOfpdus = pdus.length; msg = new
	 * HashMap<String, String>(nbrOfpdus); msgs = new SmsMessage[nbrOfpdus];
	 * 
	 * // There can be multiple SMS from multiple senders, there // can be // a
	 * maximum of nbrOfpdus different senders // However, send long SMS of same
	 * sender in one message for (int i = 0; i < nbrOfpdus; i++) { msgs[i] =
	 * SmsMessage.createFromPdu((byte[]) pdus[i]);
	 * 
	 * String originatinAddress = msgs[i] .getOriginatingAddress();
	 * 
	 * // Check if index with number exists if
	 * (!msg.containsKey(originatinAddress)) { // Index with number doesn't
	 * exist // Save string into associative array with sender // number // as
	 * index msg.put(msgs[i].getOriginatingAddress(), msgs[i].getMessageBody());
	 * 
	 * } else { // Number has been there, add content but consider // that //
	 * msg.get(originatinAddress) already contains // sms:sndrNbr:previousparts
	 * of SMS, // so just add the part of the current PDU String previousparts =
	 * msg.get(originatinAddress); String msgString = previousparts +
	 * msgs[i].getMessageBody(); msg.put(originatinAddress, msgString); } } } }
	 * 
	 * return msg; } };
	 */
}

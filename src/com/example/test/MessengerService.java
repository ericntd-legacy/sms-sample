package com.example.test;

import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

public class MessengerService extends Service {
	private final String TAG = "MessengerService";

	public static final int MSG_SAY_HELLO = 1;
	public static final int MSG_SAY_GOODBYE = 2;

	ArrayList<Messenger> mClients = new ArrayList<Messenger>();

	private final Messenger mMessenger = new Messenger(new TempHandler());

	@Override
	public void onCreate() {
		super.onCreate();
		Log.w(TAG, "onCreate");
	}
	private class TempHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SAY_HELLO:
				mClients.add(msg.replyTo);
				Toast.makeText(getApplicationContext(), "Hi, there.",
						Toast.LENGTH_SHORT).show();
				break;

			case MSG_SAY_GOODBYE:
				mClients.add(msg.replyTo);

				break;

			default:
				super.handleMessage(msg);
			}
		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		Toast.makeText(getApplicationContext(), "Service bound",
				Toast.LENGTH_SHORT).show();
		return mMessenger.getBinder();
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "Service Destroyed...");
		super.onDestroy();
	}
}

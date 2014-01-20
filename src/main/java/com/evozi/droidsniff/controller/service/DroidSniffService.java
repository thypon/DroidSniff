package com.evozi.droidsniff.controller.service;

import com.evozi.droidsniff.model.Command;
import com.evozi.droidsniff.model.Constants;
import com.evozi.droidsniff.model.Executor;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.util.Log;
import com.evozi.droidsniff.model.Setup;
import com.evozi.droidsniff.model.auth.AuthChecker;


public class DroidSniffService extends IntentService {

	private volatile Thread myThread;
	private static volatile WifiManager.WifiLock wifiLock;
	private static volatile PowerManager.WakeLock wakeLock;

	public DroidSniffService() {
		super("ListenService");
	}

	@Override
	public void onHandleIntent(Intent intent) {
		final String command = Setup.get().getBinaryPath("droidsniff");
		Executor.get().execSUCommand("chmod 777 " + command);
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		
		wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "wifiLock");
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wakeLock");
		wifiLock.acquire();
		wakeLock.acquire();

		try {
			myThread = new Command(command, AuthChecker.get());
			myThread.setDaemon(true);
			myThread.start();
			myThread.join();
		} catch (InterruptedException e) {
			Log.i(Constants.APPLICATION_TAG, "DroidSniff was interrupted", e);
		} finally {
			if (myThread != null)
				myThread = null;
			if (wifiLock != null && wifiLock.isHeld()) {
				wifiLock.release();
			}
			if (wakeLock != null && wakeLock.isHeld()) {
				wakeLock.release();
			}
			stopForeground(true);
		}
	}

	@Override
	public void onDestroy() {
		//at the suggestion of the internet
		if (myThread != null) {
			Thread tmpThread = myThread;
			myThread = null;
			tmpThread.interrupt();
		}
		Executor.get().execSUCommand(Constants.CLEANUP_COMMAND_DROIDSNIFF);
	}
}
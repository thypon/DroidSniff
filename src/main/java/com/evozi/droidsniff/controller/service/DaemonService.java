package com.evozi.droidsniff.controller.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import com.evozi.droidsniff.model.*;
import lombok.extern.apachecommons.CommonsLog;

import java.util.List;

@CommonsLog
public abstract class DaemonService extends IntentService {
    private Intent intent;

    public DaemonService() {
        super("DaemonService");
    }

    protected abstract List<String> preCommands(Intent intent);
    protected Processor getProcessor() { return null; }
    protected abstract String command(Intent intent);
    protected abstract List<String> postCommands(Intent intent);

    private volatile 		Thread myThread;
    private static volatile WifiManager.WifiLock wifiLock;
    private static volatile PowerManager.WakeLock wakeLock;

    @Override
    public void onHandleIntent(Intent intent) {
        this.intent = intent;
        for (String command : preCommands(intent)) {
            Executor.get().execSUCommand(command);
        }

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "wifiLock");
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "wakeLock");
        wifiLock.acquire();
        wakeLock.acquire();
        try {
            myThread = new Command(command(intent), getProcessor());
            myThread.setDaemon(true);
            myThread.start();
            myThread.join();
        } catch (InterruptedException e) {
            log.info("DaemonService was interrupted", e);
        } finally {
            if (myThread != null)
                myThread = null;
            if (wifiLock.isHeld()) {
                wifiLock.release();
            }
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
            stopForeground(true);
        }
    }

    @Override
    public void onDestroy() {
        for (String command : postCommands(intent)) {
            Executor.get().execSUCommand(command);
        }

        if(myThread != null) {
            myThread.interrupt();
            myThread = null;
        }
        if (wifiLock.isHeld()) {
            wifiLock.release();
        }
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
        stopForeground(true);
    }
}

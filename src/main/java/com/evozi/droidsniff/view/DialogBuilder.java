package com.evozi.droidsniff.view;

import com.evozi.droidsniff.model.BlackList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import com.evozi.droidsniff.R;

public class DialogBuilder {

	private static Activity context = null;

	public static void installBusyBox(Activity context) {
		DialogBuilder.context = context;
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(R.string.installbusybox).setCancelable(false)
				.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent goToMarket = null;
						goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=stericson.busybox"));
						DialogBuilder.context.startActivity(goToMarket);
						dialog.cancel();
					}
				}).setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public static void clearBlacklist(Activity context) {
		DialogBuilder.context = context;
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(R.string.clear_blacklist).setCancelable(false)
				.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
                        BlackList.get().clear();
					}
				}).setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public static void showUnrooted(Activity context) {
		DialogBuilder.context = context;
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(R.string.unrooted).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}


	public static void showDisclaimer(final Activity context) {

	AlertDialog.Builder builder = new AlertDialog.Builder(context);
	
	builder.setMessage(R.string.license);
	builder.setPositiveButton("Agree", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		}).setNegativeButton("Disagree", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				context.finish();
			}
		});

	builder.setCancelable(false);
	builder.show();
	}
}

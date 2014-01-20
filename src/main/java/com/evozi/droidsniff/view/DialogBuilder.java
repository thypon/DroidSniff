package com.evozi.droidsniff.view;

import android.app.Application;
import android.content.Context;
import com.evozi.droidsniff.model.BlackList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import com.evozi.droidsniff.R;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DialogBuilder {
    private static DialogBuilder instance;

    public static DialogBuilder get() {
        if (instance == null) {
            instance = new DialogBuilder();
        }

        return instance;
    }

	public void installBusyBox(final Activity activity) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage(R.string.installbusybox).setCancelable(false)
				.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Intent goToMarket = null;
						goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=stericson.busybox"));
						activity.startActivity(goToMarket);
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

	public void clearBlacklist(final Activity activity) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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

	public void showUnrooted(final Activity activity) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage(R.string.unrooted).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}


	public void showDisclaimer(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    
        builder.setMessage(R.string.license);
        builder.setPositiveButton("Agree", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            }).setNegativeButton("Disagree", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    activity.finish();
                }
            });
    
        builder.setCancelable(false);
        builder.show();
	}
}

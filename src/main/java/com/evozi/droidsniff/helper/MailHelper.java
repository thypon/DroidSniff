package com.evozi.droidsniff.helper;

import com.evozi.droidsniff.auth.Auth;
import com.evozi.droidsniff.objects.Session;

import android.content.Context;
import android.content.Intent;

public class MailHelper {
	
	public static void sendAuthByMail(Context c, Auth a) {
		StringBuilder sb = new StringBuilder();
		for (Session session : a.getSessions()) {
			sb.append("[Cookie: \n");
			sb.append("domain: ").append(session.getCookie().getDomain()).append("\n");
			sb.append("path: ").append(session.getCookie().getPath()).append("\n");
			sb.append(session.getCookie().getName());
			sb.append("=");
			sb.append(session.getCookie().getValue());
			sb.append(";]\n");
		}
				
	    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
	    emailIntent .setType("plain/text");
	    emailIntent .putExtra(android.content.Intent.EXTRA_SUBJECT, "DroidSniff Cookie export");
	    emailIntent .putExtra(android.content.Intent.EXTRA_TEXT, sb.toString());
	    c.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
	}

	public static void sendStringByMail(Context c, String string) {
	    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
	    emailIntent .setType("plain/text");
	    emailIntent .putExtra(android.content.Intent.EXTRA_SUBJECT, "DROIDSNIFF DEBUG INFORMATION");
	    emailIntent .putExtra(android.content.Intent.EXTRA_TEXT, string);
	    c.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
	}

}
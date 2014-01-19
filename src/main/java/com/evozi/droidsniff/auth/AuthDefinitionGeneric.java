package com.evozi.droidsniff.auth;

import java.util.ArrayList;

import org.apache.http.impl.cookie.BasicClientCookie;

import com.evozi.droidsniff.helper.Constants;
import com.evozi.droidsniff.objects.Session;

import android.util.Log;

public class AuthDefinitionGeneric extends AuthDefinition {

	public AuthDefinitionGeneric() {
		super(null, null, null, null, null, null, null);
	}

	@Override
	public Auth getAuthFromCookieString(String cookieListString) {
		return getAuthFromCookieStringGeneric(cookieListString);
	}

	public Auth getAuthFromCookieStringGeneric(String cookieListString) {
		String[] lst = cookieListString.split("\\|\\|\\|");

		if (lst.length < 3) {
			Log.d(Constants.APPLICATION_TAG, "String not recognized: " + cookieListString);
			return null;
		}
		String host = lst[1].replaceAll("Host=", "");
		host = host.replaceAll(" ", "");
		if (host == null || host.replaceAll(" ", "").equals("")) {
			Log.d(Constants.APPLICATION_TAG, "Host is empty or null: " + cookieListString);
			return null;
		}

		cookieListString = lst[0];
		String theurl = "";

		if (!host.startsWith("http://")) {
			theurl = "http://" + host;
		} else {
			theurl = host;
		}

		ArrayList<Session> sessions = new ArrayList<Session>();
		String[] cookies = cookieListString.split(";");
		for (String cookieString : cookies) {
			String[] values = cookieString.split("=");
			if (cookieString.endsWith("=")) {
				values[values.length - 1] = values[values.length - 1] + "=";
			}
			values[0] = values[0].replaceAll("Cookie:", "");
			values[0] = values[0].replaceAll(" ", "");
			String val = "";
			for (int i = 1; i < values.length; i++) {
				if (i > 1)
					val += "=";
				val += values[i];
			}
			BasicClientCookie cookie = new BasicClientCookie(values[0], val);
			cookie.setDomain(host.replaceAll("www.", ""));
			cookie.setPath("/");
			cookie.setVersion(0);

			sessions.add(new Session(cookie, theurl));
		}
		return new Auth(sessions, theurl, null, null, lst[2], "generic");
	}

}

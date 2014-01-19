/*
 * AuthDefinition.java defnies one Authentication, read from auth.xml resource
 * Copyright (C) 2011 Andreas Koch <koch.trier@gmail.com>
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

package com.evozi.droidsniff.model.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;
import com.evozi.droidsniff.common.Constants;
import com.evozi.droidsniff.model.Session;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;

interface IAuthFactory {
    Auth getAuth(String cookiesString);
}

@AllArgsConstructor
final class AuthFactory implements IAuthFactory {
	@NonNull List<String> cookieNames;
	String url;
    String mobileUrl;
    @NonNull String domain;
	@NonNull String name;
    String idUrl;
    String regexp;

    private String getIdUrl() {
        return this.idUrl != null ? this.idUrl : this.url;
    }

	public Auth getAuth(String cookiesString) {
		String[] lst = cookiesString.split("\\|\\|\\|");
		if (lst.length < 3)
			return null;
		cookiesString = lst[0];

		final List<Session> sessions = new ArrayList<Session>();
		String[] cookies = cookiesString.split(";");
		for (String cookieString : cookies) {
			String[] values = cookieString.split("=");
			if (cookieString.endsWith("=")) {
				values[values.length - 1] = values[values.length - 1] + "=";
			}
			values[0] = values[0].replaceAll("Cookie:", "");
			values[0] = values[0].replaceAll(" ", "");
			if (cookieNames.contains(values[0])) {
				String val = "";
				for (int i = 1; i < values.length; i++) {
					if (i > 1)
						val += "=";
					val += values[i];
				}
				BasicClientCookie cookie = new BasicClientCookie(values[0], val);
				cookie.setDomain(domain);
				cookie.setPath("/");
				cookie.setVersion(0);
				sessions.add(new Session(cookie, url));
			}
		}
		if (!sessions.isEmpty() && sessions.size() == cookieNames.size()) {
			return new Auth(sessions, url, mobileUrl, getIdFromWebservice(sessions), lst[2], this.name);
		}
		return null;
	}

	private String getIdFromWebservice(List<Session> sessions) {
		try {
			Pattern pattern = Pattern.compile(regexp);

			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet http = new HttpGet(this.getIdUrl());
			StringBuilder cookies = new StringBuilder();
			for (Session session : sessions) {
				cookies.append(session.getCookie().getName());
				cookies.append("=");
				cookies.append(session.getCookie().getValue());
				cookies.append("; ");
			}
			http.addHeader("Cookie", cookies.toString());
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String response = httpClient.execute(http, responseHandler);

			Matcher matcher = pattern.matcher(response);
			boolean matchFound = matcher.find();
			if (matchFound) {
				String s = matcher.group(2);
				return s;
			}
		} catch (Exception e) {
			return "";
		}
		return "";
	}
	
}

final class GenericAuthFactory implements IAuthFactory {

    public Auth getAuth(String cookiesString) {
        String[] lst = cookiesString.split("\\|\\|\\|");

        if (lst.length < 3) {
            Log.d(Constants.APPLICATION_TAG, "String not recognized: " + cookiesString);
            return null;
        }
        String host = lst[1].replaceAll("Host=", "");
        host = host.replaceAll(" ", "");
        if (host == null || host.replaceAll(" ", "").equals("")) {
            Log.d(Constants.APPLICATION_TAG, "Host is empty or null: " + cookiesString);
            return null;
        }

        cookiesString = lst[0];
        String theurl = "";

        if (!host.startsWith("http://")) {
            theurl = "http://" + host;
        } else {
            theurl = host;
        }

        ArrayList<Session> sessions = new ArrayList<Session>();
        String[] cookies = cookiesString.split(";");
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

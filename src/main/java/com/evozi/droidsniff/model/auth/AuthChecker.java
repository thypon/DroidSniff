package com.evozi.droidsniff.model.auth;

import android.app.Application;
import android.content.Context;
import android.content.res.XmlResourceParser;
import com.evozi.droidsniff.R;
import com.evozi.droidsniff.controller.activity.ListenActivity;
import com.evozi.droidsniff.model.BlackList;
import com.evozi.droidsniff.model.Processor;
import com.evozi.droidsniff.model.event.AuthEvent;
import com.evozi.droidsniff.model.event.AuthEventType;
import de.greenrobot.event.EventBus;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
public final class AuthChecker implements Processor {
    private final HashMap<String, IAuthFactory> authDefList = new HashMap<String, IAuthFactory>();
    private final IAuthFactory generic = new GenericAuthFactory();

    private static Context ctx;
    private static AuthChecker instance;

    public static void init(Application app) {
        ctx = app;
    }

    @SneakyThrows({IOException.class, XmlPullParserException.class})
    public static AuthChecker get() {
        if (instance == null) {
            instance = new AuthChecker();
            instance.readConfig(ctx);
        }

        return instance;
    }

    private void readConfig(Context context) throws IOException, XmlPullParserException {
        XmlResourceParser xpp = context.getResources().getXml(R.xml.auth);

        xpp.next();
        int eventType = xpp.getEventType();

        String mobileurl 	= null;
        String name 		= null;
        String url 			= null;
        String domain 		= null;
        String idurl 		= null;
        String regexp 		= null;
        ArrayList<String> cookieNames = new ArrayList<String>();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("auth")) {
                name = null;
                url = null;
                mobileurl = null;
                domain = null;
                idurl = null;
                regexp = null;
                cookieNames = new ArrayList<String>();
            }
            while (!(eventType == XmlPullParser.END_TAG && xpp.getName().equals("auth")) && eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("name")) {
                        xpp.next();
                        name = xpp.getText();
                    } else if (xpp.getName().equals("url")) {
                        xpp.next();
                        url = xpp.getText();
                    } else if (xpp.getName().equals("domain")) {
                        xpp.next();
                        domain = xpp.getText();
                    } else if (xpp.getName().equals("cookieName")) {
                        xpp.next();
                        cookieNames.add(xpp.getText());
                    } else if (xpp.getName().equals("mobileUrl")) {
                        xpp.next();
                        mobileurl = xpp.getText();
                    } else if (xpp.getName().equals("idUrl")) {
                        xpp.next();
                        idurl = xpp.getText();
                    } else if (xpp.getName().equals("regexp")) {
                        xpp.next();
                        regexp = xpp.getText();
                    }
                }
                eventType = xpp.next();
            }
            if (name!= null && url != null && domain != null && cookieNames != null && !cookieNames.isEmpty()) {
                authDefList.put(name, new AuthFactory(cookieNames, url, mobileurl, domain, name, idurl, regexp));
            }
            eventType = xpp.next();
        }
    }

    public List<Auth> match(String line) {
        List<Auth> lst = new ArrayList<Auth>();
        lst.clear();
        for (String key : authDefList.keySet()) {
            IAuthFactory ad = authDefList.get(key);
            Auth a = ad.getAuth(line);
            if (a != null) {
                if (BlackList.get().contains(a.getName())) {
                    continue;
                }
                lst.add(a);
            }
        }
        if (ListenActivity.generic && lst.isEmpty()) {
            Auth a = generic.getAuth(line);
            if (a != null && a.getName() != null) {
                if (!BlackList.get().contains(a.getName())) {
                    lst.add(a);
                }
            }
        }
        return lst;
    }

    public void process (String line) {
        List<Auth> lstAuth = match(line);
        if (lstAuth != null && !lstAuth.isEmpty()) {
            for (Auth a : lstAuth) {
                EventBus.getDefault().post(AuthEvent.of(a, AuthEventType.NEW));
            }
        }
    }
}

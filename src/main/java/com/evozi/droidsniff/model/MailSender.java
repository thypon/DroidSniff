package com.evozi.droidsniff.model;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import com.evozi.droidsniff.model.auth.Auth;
import lombok.NonNull;

public final class MailSender {
    private static Context ctx;

    public static void init(@NonNull Application app) {
        ctx = app;
    }

    private static MailSender instance;

    public static MailSender get() {
        if (instance == null) {
            instance = new MailSender();
        }

        return instance;
    }

    public void sendAuthByMail(Auth a) {
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
        ctx.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    public void sendStringByMail(String string) {
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent .setType("plain/text");
        emailIntent .putExtra(android.content.Intent.EXTRA_SUBJECT, "DROIDSNIFF DEBUG INFORMATION");
        emailIntent .putExtra(android.content.Intent.EXTRA_TEXT, string);
        ctx.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }
}

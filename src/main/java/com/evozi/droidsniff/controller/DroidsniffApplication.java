package com.evozi.droidsniff.controller;

import android.app.Application;
import com.evozi.droidsniff.model.BlackList;
import com.evozi.droidsniff.model.DB;
import com.evozi.droidsniff.model.MailSender;
import com.evozi.droidsniff.model.auth.AuthChecker;

public class DroidsniffApplication extends Application {
    @Override
    public void onCreate() {
        BlackList.init(this);
        AuthChecker.init(this);
        DB.init(this);
        MailSender.init(this);
    }
}

package com.evozi.droidsniff.controller;

import android.app.Application;
import com.evozi.droidsniff.model.*;
import com.evozi.droidsniff.model.auth.AuthChecker;
import com.evozi.droidsniff.model.auth.AuthManager;
import com.evozi.droidsniff.view.DialogBuilder;

public final class DroidsniffApplication extends Application {
    @Override
    public void onCreate() {
        BlackList.init(this);
        AuthChecker.init(this);
        DB.init(this);
        MailSender.init(this);
        Setup.init(this);
        AuthManager.init(this);
    }
}

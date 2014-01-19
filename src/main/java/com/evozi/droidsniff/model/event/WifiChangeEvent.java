package com.evozi.droidsniff.model.event;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WifiChangeEvent {
    private static WifiChangeEvent instance;

    public static WifiChangeEvent get() {
        if (instance == null) {
            instance = new WifiChangeEvent();
        }

        return instance;
    }
}

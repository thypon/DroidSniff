package com.evozi.droidsniff.model;

import android.app.Application;
import android.content.Context;
import lombok.*;

import java.util.Set;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public final class BlackList {
    @Delegate(types = SimpleSet.class)
    @NonNull
    private Set<String> delegate;

    private static Context ctx;

    public static void init(@NonNull Application app) {
        ctx = app;
    }

    private static BlackList instance;

    public static BlackList get() {
        if (instance == null) {
            instance = new BlackList(DB.get().getBlacklist());
        }

        return instance;
    }

    public void add(String s) {
        delegate.add(s);
        DB.get().addBlacklistEntry(s);
    }

    public void clear() {
        delegate.clear();
        DB.get().clearBlacklist();
    }
}

interface SimpleSet<T1> {
    boolean contains(T1 key);
}
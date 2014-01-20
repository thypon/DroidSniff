package com.evozi.droidsniff.model.auth;

import android.app.Application;
import android.content.Context;
import de.greenrobot.event.EventBus;
import lombok.Cleanup;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.apachecommons.CommonsLog;

import java.io.*;

@CommonsLog
public final class AuthManager {
    private static Context ctx;

    public static void init(@NonNull Application app) {
        ctx = app;
    }

    private static AuthManager instance;

    public static AuthManager get() {
        if (instance == null) {
            instance = new AuthManager();
        }

        return instance;
    }


    public void save(Auth a) {
        File dir = new File(ctx.getFilesDir() + File.separator + "saved");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        File f = new File(ctx.getFilesDir() + File.separator + "saved" + File.separator + "droidsniff" + a.getId());
        try {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(a);
            out.close();
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();
            bos.writeTo(new FileOutputStream(f.getAbsolutePath()));
            a.setSaved(true);
        } catch (IOException ioe) {
            AuthManager.log.error("serializeObject error", ioe);
        }
    }

    public void delete(Auth a) {
        if (a == null) {
            return;
        }
        File f = new File(ctx.getFilesDir() + File.separator + "saved" + File.separator + "droidsniff" + a.getId());
        if (f.exists()) {
            for (int i = 0; i < 5; i++) {
                if (f.delete()) break; // In case deletion fails, retry 5 times...
            }

        }
        a.setSaved(false);
    }

    @SneakyThrows({IOException.class, ClassNotFoundException.class})
    public void read() {
        File f = new File(ctx.getFilesDir() + File.separator + "saved");
        if (!f.exists() || !f.isDirectory()) {
            AuthManager.log.error(ctx.getFilesDir() + File.separator + "saved" + " does not exist or is no folder!");
            return;
        }

        for (File objFile : f.listFiles()) {
            @Cleanup ObjectInputStream in = new ObjectInputStream(new FileInputStream(objFile));
            Auth auth = (Auth) in.readObject();
            auth.setSaved(true);
            EventBus.getDefault().post(auth);
        }
    }
}

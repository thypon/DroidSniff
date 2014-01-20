/*
 * SetupHelper.java does the initial copy of the binaries Copyright (C) 2011
 * Andreas Koch <koch.trier@gmail.com>
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

package com.evozi.droidsniff.model;

import java.io.*;

import android.app.Application;
import android.os.Build;

import android.content.Context;
import lombok.*;
import lombok.extern.apachecommons.CommonsLog;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@CommonsLog
public final class Setup {
    private static Context ctx;

    public static void init(@NonNull Application app) {
        ctx = app;
    }

    private static Setup instance;

    public static Setup get() {
        if (instance == null) {
            instance = new Setup();
        }

        return instance;
    }

	public void prepareBinaries() {
		Setup.log.debug("CHECKPREREQUISITES");

        try {
            extractBin("droidsniff");
        } catch (IOException e) {
            Setup.log.error(e);
            deleteBin("droidsniff");
        }

        try {
            extractBin("arpspoof");
        } catch (IOException e) {
            Setup.log.error(e);
            deleteBin("arpspoof");
        }
	}

    private void deleteBin(String name) {
        File file = new File(getBinaryPath(name));
        if (file.exists()) file.delete();
    }

    private void extractBin(String name) throws IOException {
        File file = new File(getBinaryPath(name));
        if (file.exists()) return;

        InputStream in = ctx.getResources().openRawResource(getExecutableResourceId(name));

        FileOutputStream out = ctx.openFileOutput(name, Context.MODE_PRIVATE);
        byte[] buffer = new byte[64];
        while (in.read(buffer) > -1) {
            out.write(buffer);
        }
        out.flush();
        out.close();

        Executor.get().execSUCommand("chmod 777 " + ctx.getFilesDir().toString() + File.separator + name);
    }

    @SneakyThrows({IOException.class, InterruptedException.class})
	public boolean checkCommands() {
        Process process = Runtime.getRuntime().exec("busybox");
        Thread.sleep(50);
        @Cleanup InputStreamReader osRes = new InputStreamReader(process.getInputStream());
        BufferedReader reader = new BufferedReader(osRes);

        String line = reader.readLine();
        while (line != null) {
            if (line.contains("killall")) {
                return true;
            }
            line = reader.readLine();
        }
        process.waitFor();

        return false;
	}

    @SneakyThrows({IOException.class, InterruptedException.class})
    public boolean checkSu() {
        Process process = Runtime.getRuntime().exec("ls /system/bin");
        @Cleanup DataOutputStream os = new DataOutputStream(process.getOutputStream());
        @Cleanup InputStreamReader osRes = new InputStreamReader(process.getInputStream());
        BufferedReader reader = new BufferedReader(osRes);

        os.writeBytes("exit \n");
        os.flush();

        String line = reader.readLine();
        while (line != null) {
            if (line.contains("su")) {
                return true;
            }
            line = reader.readLine();
        }
        process.waitFor();

        return false;
	}

    private int getExecutableResourceId(String executable) {
        return ctx.getResources()
                .getIdentifier(
                        "raw/" + executable + "_" + getBestArchitecture().replace('-', '_'),
                        null, ctx.getPackageName());
    }

    private String getBestArchitecture() {
        if (Constants.ARCH.contains(Build.CPU_ABI)) {
            return Build.CPU_ABI;
        } else if (Constants.ARCH.contains(Build.CPU_ABI2)) {
            return Build.CPU_ABI2;
        } else {
            throw new IllegalStateException("No CPU ABI Available");
        }
    }

    public String getBinaryPath(String name) {
        return ctx.getFilesDir().getAbsolutePath() + File.separator + name;
    }
}

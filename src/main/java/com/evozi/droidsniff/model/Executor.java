/*
 * System.java executed superuser commands Copyright (C) 2011 Andreas Koch
 * <koch.trier@gmail.com>
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

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public final class Executor {
    private static Executor instance;

    public static Executor get() {
        if (instance == null) {
            instance = new Executor();
        }

        return instance;
    }

    Process process = null;
	
	public boolean execSUCommand(String command) {
		try {
			if (process == null || process.getOutputStream() == null) {
				process = new ProcessBuilder().command("su").start();
			}

			process.getOutputStream().write((command + "\n").getBytes("ASCII"));
			process.getOutputStream().flush();				
			Thread.sleep(100);
			return true;
		} catch (Exception e) {
			Executor.log.error("Error executing: " + command, e);
			return false;
		}
	}
	
	public void execNewSUCommand(String command) {
		try {
			Process process = new ProcessBuilder().command("su").start();
			process.getOutputStream().write((command + "\n").getBytes("ASCII"));
			process.getOutputStream().flush();				
			Thread.sleep(100);
		} catch (Exception e) {
			Executor.log.error("Error executing: " + command, e);
		}
	}
}

/*
 * The arpspoof package containts Software, initially developed by Robboe
 * Clemons, It has been used, changed and published in DroidSheep by Andreas
 * Koch according the GNU GPLv3
 */

/*
 * Command.java uses java's exec to execute commands as root Copyright
 * (C) 2011 Robbie Clemons <robclemons@gmail.com>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import lombok.*;
import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
@AllArgsConstructor
public final class Command extends Thread {
	@NonNull private final String command;
    private final Processor processor;

    public Command(String command) {
        this(command, null);
    }

    @Getter(lazy = true, value = AccessLevel.PRIVATE, onMethod=@_(@SneakyThrows(IOException.class)))
    private final Process process = Runtime.getRuntime().exec("su");
	@Getter(lazy = true, value = AccessLevel.PRIVATE) private final BufferedReader reader =
            new BufferedReader(new InputStreamReader(getProcess().getInputStream()));
	@Getter(lazy = true, value = AccessLevel.PRIVATE) private final BufferedReader errorReader =
            new BufferedReader(new InputStreamReader(getProcess().getErrorStream()));
	@Getter(lazy = true, value = AccessLevel.PRIVATE) private final DataOutputStream os =
            new DataOutputStream(getProcess().getOutputStream());

	public void run() {

        @RequiredArgsConstructor
		class StreamGobbler extends Thread {
			/*
			 * "gobblers" seem to be the recommended way to ensure the streams
			 * don't cause issues
			 */

			@NonNull private final BufferedReader buffReader;

			public void run() {
				try {
					while (true) {
						String line = "";
						if (buffReader.ready()) {
							line = buffReader.readLine();
							if (line == null)
								continue;
						} else {
							try {
								Thread.sleep(200);
							} catch (InterruptedException e) {
							}
							continue;
						}

						if (processor != null) {
                            processor.process(line);
						}
					}
				} catch (IOException e) {
					Command.log.warn("StreamGobbler couldn't read stream or stream closed");
				} finally {
					try {
						buffReader.close();
					} catch (IOException e) {
						// swallow error
					}
				}
			}
		}

		try {
			getOs().writeBytes(command + '\n');
			getOs().flush();
			StreamGobbler errorGobler = new StreamGobbler(getErrorReader());
			StreamGobbler stdOutGobbler = new StreamGobbler(getReader());
			errorGobler.setDaemon(true);
			stdOutGobbler.setDaemon(true);
			errorGobler.start();
			stdOutGobbler.start();
			getOs().writeBytes("exit\n");
			getOs().flush();
			// The following catastrophe of code seems to be the best way to
			// ensure this thread can be interrupted
			while (!Thread.currentThread().isInterrupted()) {
				try {
					getProcess().exitValue();
					Thread.currentThread().interrupt();
				} catch (IllegalThreadStateException e) {
					// the process hasn't terminated yet so sleep some, then
					// check again
					Thread.sleep(250);// .25 seconds seems reasonable
				}
			}
		} catch (IOException e) {
			Command.log.error("error running commands", e);
		} catch (InterruptedException e) {
			try {
				/**
				 * os.close();//key to killing executable and process
				 * reader.close(); errorReader.close();
				 **/
				if (getOs() != null) {
					getOs().close();// key to killing executable and process
				}
				if (getReader() != null) {
					getReader().close();
				}
				if (getErrorReader() != null) {
					getErrorReader().close();
				}
			} catch (IOException ex) {
				// swallow error
			} finally {
				if (getProcess() != null) {
					getProcess().destroy();
				}
			}
		} finally {
			if (getProcess() != null) {
				getProcess().destroy();
			}
		}
	}

}
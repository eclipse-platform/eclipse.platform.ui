/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.standalone;

import java.io.*;
import java.util.List;

/**
 * Eclipse launcher.  Spawns eclipse executable
 * or org.eclipse.core.launcher.Main.
 */
public class Eclipse extends Thread {
	private static final int RESTART = 23;
	File dir;
	String[] cmdarray;
	/**
	 * Constructor
	 */
	public Eclipse() {
		super();
		this.setName("Eclipse");
		this.dir = Options.getEclipseHome();
	}
	private void prepareCommand() {
		if (Options.useExe()) {
			prepareEclipseCommand();
		} else {
			prepareJavaCommand();
		}
	}
	private void prepareEclipseCommand() {
		List vmArgs = Options.getVmArgs();
		List eclipseArgs = Options.getEclipseArgs();
		cmdarray = new String[3 + vmArgs.size() + 1 + eclipseArgs.size()];
		cmdarray[0] =
			new File(Options.getEclipseHome(), "eclipse").getAbsolutePath();
		cmdarray[1] = "-vm";
		cmdarray[2] = Options.getVm();
		for (int i = 0; i < eclipseArgs.size(); i++) {
			cmdarray[3 + i] = (String) eclipseArgs.get(i);
		}
		cmdarray[3 + eclipseArgs.size()] = "-vmargs";
		for (int i = 0; i < vmArgs.size(); i++) {
			cmdarray[4 + eclipseArgs.size() + i] = (String) vmArgs.get(i);
		}
	}
	private void prepareJavaCommand() {
		List vmArgs = Options.getVmArgs();
		List eclipseArgs = Options.getEclipseArgs();
		cmdarray = new String[1 + vmArgs.size() + 3 + eclipseArgs.size()];
		cmdarray[0] = Options.getVm();
		for (int i = 0; i < vmArgs.size(); i++) {
			cmdarray[1 + i] = (String) vmArgs.get(i);
		}
		cmdarray[1 + vmArgs.size()] = "-cp";
		cmdarray[2 + vmArgs.size()] = "startup.jar";
		cmdarray[3 + vmArgs.size()] = "org.eclipse.core.launcher.Main";
		for (int i = 0; i < eclipseArgs.size(); i++) {
			cmdarray[4 + vmArgs.size() + i] = (String) eclipseArgs.get(i);
		}
	}
	/**
	 * Launches Eclipse process and waits for it.
	 */
	public void run() {
		prepareCommand();
		try {
			Process pr;
			do {
				pr = Runtime.getRuntime().exec(cmdarray, (String[]) null, dir);
				(new StreamConsumer(pr.getInputStream())).start();
				(new StreamConsumer(pr.getErrorStream())).start();
				pr.waitFor();
			} while (pr.exitValue() == RESTART);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Reads a stream
	 */
	public class StreamConsumer extends Thread {
		BufferedReader bReader;
		public StreamConsumer(InputStream inputStream) {
			super();
			this.setName("Eclipse out/err consumer");
			bReader = new BufferedReader(new InputStreamReader(inputStream));
		}
		public void run() {
			try {
				String line;
				while (null != (line = bReader.readLine())) {
					System.out.println(line);
				}
				bReader.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.standalone;

import java.io.*;
import java.util.List;

/**
 * Launches Eclipse executable.
 */
public class Eclipse extends Thread {
	File dir;
	String[] cmdarray;
	/**
	 * Constructor
	 * @param eclipseHome Eclipse instllation directory
	 * @param eclipseOptions List of String options to be passed to Eclipse
	 */
	public Eclipse(File eclipseHome, List eclipseOptions) {
		super();
		this.dir = eclipseHome;
		cmdarray = new String[eclipseOptions.size() + 1];
		cmdarray[0] = new File(eclipseHome, "eclipse").getAbsolutePath();
		for (int i = 0; i < eclipseOptions.size(); i++) {
			cmdarray[i + 1] = (String) eclipseOptions.get(i);
		}
	}
	/**
	 * Launches Eclipse process and waits for it.
	 */
	public void run() {
		try {
			Process pr =
				Runtime.getRuntime().exec(cmdarray, (String[]) null, dir);
			(new StreamConsumer(pr.getInputStream())).start();
			(new StreamConsumer(pr.getErrorStream())).start();
			pr.waitFor();
			//return pr.exitValue();
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

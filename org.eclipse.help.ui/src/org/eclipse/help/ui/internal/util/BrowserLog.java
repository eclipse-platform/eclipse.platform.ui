/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.util;

import java.io.*;
import java.text.*;
import java.util.Date;

import org.eclipse.help.internal.HelpPlugin;

/**
 * Log for messages output by external browser processes.
 */
public class BrowserLog {
	private static BrowserLog instance;
	private String logFileName;
	private boolean newSession;
	DateFormat formatter = new SimpleDateFormat("MMM dd, yyyy kk:mm:ss.SS");
	/**
	 * Constructor
	 */
	private BrowserLog() {
		try {
			newSession = true;
			logFileName =
				HelpPlugin
					.getDefault()
					.getStateLocation()
					.append("browser.log")
					.toOSString();
		} catch (Exception e) {
			// can get here if platform is shutting down
		}
	}
	/**
	 * Obtains singleton
	 */
	private static BrowserLog getInstance() {
		if (instance == null) {
			instance = new BrowserLog();
		}
		return instance;
	}
	/**
	 * Appends a line to the browser.log
	 */
	public static synchronized void log(String message) {
		getInstance().append(message);
	}
	private void append(String message) {
		if (logFileName == null) {
			return;
		}
		Writer outWriter = null;
		try {
			outWriter =
				new BufferedWriter(
					new OutputStreamWriter(
						new FileOutputStream(logFileName, true),
						"UTF-8"));
			if (newSession) {
				newSession = false;
				outWriter.write(
					"\n" + formatter.format(new Date()) + " NEW SESSION\n");
			}
			outWriter.write(
				formatter.format(new Date()) + " " + message + "\n");
			outWriter.flush();
			outWriter.close();
		} catch (Exception e) {
			if (outWriter != null) {
				try {
					outWriter.close();
				} catch (IOException ioe) {
				}
			}
		}
	}
}

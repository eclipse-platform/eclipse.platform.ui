package org.eclipse.core.internal.runtime;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;
import java.io.*;
import java.util.Date;

class PlatformLogListener implements ILogListener {
	private PrintWriter log = null;
	private boolean usingLogFile = false;
PlatformLogListener() {
	usingLogFile = true;
	// remove old log file
	InternalPlatform.getMetaArea().getLogLocation().toFile().delete();
}
/**
 * It should only be used to pass System.out .
 */
PlatformLogListener(OutputStream out) {
	log = new PrintWriter(out);
}
private void closeLogFile() {
	try {
		log.flush();
		log.close();
	} finally {
		log = null;
	}
}
private void indent(int count) {
	for (int i = 0; i < count; i++)
		log.print("\t");
}
private void logging(IStatus status, int nesting) {
	indent(nesting);
	log.print(status.getSeverity());
	log.print(" ");
	log.print(status.getPlugin());
	log.print(" ");
	log.print(status.getCode());
	log.print(" ");
	log.println(status.getMessage());
	Throwable throwable = status.getException();
	if (throwable != null) {
		throwable.printStackTrace(log);
		if (throwable instanceof CoreException) {
			CoreException ex = (CoreException) throwable;
			IStatus s = ex.getStatus();
			if (s != null)
				logging(s, nesting + 1);
		}
	}
	if (status.isMultiStatus()) {
		indent(nesting + 1);
		log.print(nesting + 1);
		log.println("=============<children>=============");
		IStatus[] children = status.getChildren();
		for (int i = 0; i < children.length; i++)
			logging(children[i], nesting + 1);
		indent(nesting + 1);
		log.print(nesting + 1);
		log.println("=============</children>=============");
	}
	log.flush();
}
public synchronized void logging(IStatus status, String plugin) {
	// thread safety: (Concurrency003)
	if (usingLogFile)
		openLogFile();
	if (log == null)
		return;
	try {
		log.println("Log: " + new Date());
		logging(status, 0);
	} finally {
		if (usingLogFile)
			closeLogFile();
	}
}
private void openLogFile() {
	try {
		log = new PrintWriter(new FileOutputStream(InternalPlatform.getMetaArea().getLogLocation().toOSString(), true));
	} catch (IOException e) {
		// there was a problem opening the log file so log to the console
		log = new PrintWriter(System.out);
	}
}
/**
 * @see ILogListener
 */
public synchronized void shutdown() {
	if (log == null)
		return;
	PrintWriter old = log;
	log = null;
	old.flush();
	old.close();
}
}

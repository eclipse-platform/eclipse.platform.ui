package org.eclipse.core.internal.runtime;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
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
	if (status.getException() != null)
		status.getException().printStackTrace(log);
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
		log = null;
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

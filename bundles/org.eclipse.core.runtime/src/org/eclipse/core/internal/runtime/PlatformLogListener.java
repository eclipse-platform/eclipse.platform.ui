package org.eclipse.core.internal.runtime;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.core.runtime.*;
import java.io.*;

class PlatformLogListener implements ILogListener {
	PrintWriter log = null;
PlatformLogListener() {
	try {
		log = new PrintWriter(new FileOutputStream(InternalPlatform.getMetaArea().getLogLocation().toFile()));
	} catch (IOException e) {
		log = null;
	}
}
PlatformLogListener(OutputStream out) {
	log = new PrintWriter(out);
}
private void indent(int count) {
	for (int i = 0; i < count; i++)
		log.print("\t");
}
public synchronized void logging(IStatus status) {
	// thread safety: (Concurrency003)
	if (log == null)
		return;
	log.println("Log: " + new java.util.Date());
	logging(status, 0);
}
protected void logging(IStatus status, int nesting) {
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
	if (log == null)
		return;
	log.println("Log: " + new java.util.Date());
	logging(status, 0);
}
/**
 * @see ILogListener
 */
public void shutdown() {
	if (log == null)
		return;
	PrintWriter old = log;
	log = null;
	old.flush();
	old.close();
}
}

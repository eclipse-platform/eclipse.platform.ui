package org.eclipse.core.internal.runtime;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.*;
import java.util.*;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
/**
 * A log writer that writes log entries in XML format.  
 * See PlatformLogReader for reading logs back into memory.
 */
public class PlatformLogWriter implements ILogListener {
	protected File logFile = null;
	protected PrintWriter log = null;
	protected int tabDepth;

	protected static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	protected static final String ATTRIBUTE_DATE = "date";
	protected static final String ATTRIBUTE_SEVERITY = "severity";
	protected static final String ATTRIBUTE_PLUGIN_ID = "plugin-id";
	protected static final String ATTRIBUTE_CODE = "code";
	protected static final String ATTRIBUTE_MESSAGE = "message";
	protected static final String ATTRIBUTE_TRACE = "trace";
	
	protected static final String ELEMENT_LOG = "log";
	protected static final String ELEMENT_LOG_ENTRY = "log-entry";
	protected static final String ELEMENT_STATUS = "status";
	protected static final String ELEMENT_EXCEPTION = "exception";

public PlatformLogWriter(File file) {
	this.logFile = file;
	// remove old log file
	logFile.delete();
}
/**
 * This constructor should only be used to pass System.out .
 */
public PlatformLogWriter(OutputStream out) {
	log = new PrintWriter(out);
}
protected static void appendEscapedChar(StringBuffer buffer, char c) {
	String replacement = getReplacement(c);
	if (replacement != null) {
		buffer.append('&');
		buffer.append(replacement);
		buffer.append(';');
	} else {
		buffer.append(c);
	}
}
protected static String getEscaped(String s) {
	StringBuffer result = new StringBuffer(s.length() + 10);
	for (int i = 0; i < s.length(); ++i)
		appendEscapedChar(result, s.charAt(i));
	return result.toString();
}
protected static String getReplacement(char c) {
	// Encode special XML characters into the equivalent character references.
	// These five are defined by default for all XML documents.
	switch (c) {
		case '<' :
			return "lt";
		case '>' :
			return "gt";
		case '"' :
			return "quot";
		case '\'' :
			return "apos";
		case '&' :
			return "amp";
	}
	return null;
}
protected void closeLogFile() {
	try {
		log.flush();
		log.close();
	} finally {
		log = null;
	}
}
/**
 * Returns a string representation of the given severity.
 */
protected String encodeSeverity(int severity) {
	switch (severity) {
		case IStatus.ERROR :
			return "ERROR";
		case IStatus.INFO :
			return "INFO";
		case IStatus.OK:
			return "OK";
		case IStatus.WARNING :
			return "WARNING";
	}
	//unknown severity, just print the integer
	return Integer.toString(severity);
}
protected String encodeStackTrace(Throwable t) {
	StringWriter sWriter = new StringWriter();
	PrintWriter pWriter = new PrintWriter(sWriter);
	pWriter.println();
	t.printStackTrace(pWriter);
	pWriter.flush();
	return sWriter.toString();
}
protected void endTag(String name) {
	tabDepth--;
	printTag('/' + name, null);
}
public synchronized void logging(IStatus status, String plugin) {
	// thread safety: (Concurrency003)
	if (logFile != null)
		openLogFile();
	if (log == null)
		return;
	try {
		writeLogEntry(status);
	} catch (Exception e) {
		e.printStackTrace();
	}finally {
		if (logFile != null)
			closeLogFile();
	}
}
protected void openLogFile() {
	try {
		File file = InternalPlatform.getMetaArea().getLogLocation().toFile();
		boolean newLog = !file.exists();
		log = new PrintWriter(new BufferedWriter(new FileWriter(file.getAbsolutePath(), true)));
		if (newLog) {
			log.println(XML_VERSION);
			startTag(ELEMENT_LOG, null);
		}
	} catch (IOException e) {
		// there was a problem opening the log file so log to the console
		log = new PrintWriter(System.out);
	}
}
protected void printTabulation() {
	for (int i = 0; i < tabDepth; i++)
		log.print("  ");
}

protected void printTag(String name, HashMap parameters) {
	printTabulation();
	log.print('<');
	log.print(name);
	tabDepth++;
	if (parameters != null)
		for (Enumeration enum = Collections.enumeration(parameters.keySet()); enum.hasMoreElements();) {
			//new line for each attribute if there's more than one
			if (parameters.size() > 1) {
				log.println();
				printTabulation();
			}
			log.print(" ");
			String key = (String) enum.nextElement();
			log.print(key);
			log.print("=\"");
			log.print(getEscaped(String.valueOf(parameters.get(key))));
			log.print("\"");
		}
	tabDepth--;
	log.println(">");
}
/**
 * @see ILogListener
 */
public synchronized void shutdown() {
	if (logFile != null) {
		try {
			openLogFile();
			endTag(ELEMENT_LOG);
		} finally {
			closeLogFile();
			logFile = null;
		}
	} else {
		if (log != null) {
			PrintWriter old = log;
			log = null;
			old.flush();
			old.close();
		}
	}
}
protected void startTag(String name, HashMap parameters) {
	printTag(name, parameters);
	tabDepth++;
}
protected void write(Throwable throwable) {
	if (throwable == null)
		return;
	HashMap attributes = new HashMap();
	attributes.put(ATTRIBUTE_MESSAGE, throwable.getMessage());
	attributes.put(ATTRIBUTE_TRACE, encodeStackTrace(throwable));
	startTag(ELEMENT_EXCEPTION, attributes);
	endTag(ELEMENT_EXCEPTION);
}
protected void write(IStatus status) {
	HashMap attributes = new HashMap();
	attributes.put(ATTRIBUTE_SEVERITY, encodeSeverity(status.getSeverity()));
	attributes.put(ATTRIBUTE_PLUGIN_ID, status.getPlugin());
	attributes.put(ATTRIBUTE_CODE, Integer.toString(status.getCode()));
	attributes.put(ATTRIBUTE_MESSAGE, status.getMessage());
	startTag(ELEMENT_STATUS, attributes); {
		write(status.getException());
		if (status.isMultiStatus()) {
			IStatus[] children = status.getChildren();
			for (int i = 0; i < children.length; i++) {
				write(children[i]);
			}
		}
	}
	endTag(ELEMENT_STATUS);
}
protected void writeLogEntry(IStatus status) {
	tabDepth = 0;
	HashMap attributes = new HashMap();
	attributes.put(ATTRIBUTE_DATE, new Date());
	startTag(ELEMENT_LOG_ENTRY, attributes);
	write(status);
	endTag(ELEMENT_LOG_ENTRY);
}
}


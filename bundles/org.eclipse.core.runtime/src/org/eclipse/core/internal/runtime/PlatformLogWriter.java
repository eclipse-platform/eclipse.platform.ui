package org.eclipse.core.internal.runtime;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.*;
import java.util.*;

import org.apache.xml.serialize.XMLSerializer;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

class PlatformLogWriter implements ILogListener {
	private PrintWriter log = null;
	private boolean usingLogFile = false;
	private int tab;

	private static final String XML_VERSION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

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


	
PlatformLogWriter() {
	usingLogFile = true;
	// remove old log file
	InternalPlatform.getMetaArea().getLogLocation().toFile().delete();
}
/**
 * It should only be used to pass System.out .
 */
PlatformLogWriter(OutputStream out) {
	log = new PrintWriter(out);
}
private static void appendEscapedChar(StringBuffer buffer, char c) {
	String replacement = getReplacement(c);
	if (replacement != null) {
		buffer.append('&');
		buffer.append(replacement);
		buffer.append(';');
	} else {
		buffer.append(c);
	}
}
public static String getEscaped(String s) {
	StringBuffer result = new StringBuffer(s.length() + 10);
	for (int i = 0; i < s.length(); ++i)
		appendEscapedChar(result, s.charAt(i));
	return result.toString();
}
private static String getReplacement(char c) {
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
private void closeLogFile() {
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
public void endTag(String name) {
	tab--;
	printTag('/' + name, null);
}
public synchronized void logging(IStatus status, String plugin) {
	// thread safety: (Concurrency003)
	if (usingLogFile)
		openLogFile();
	if (log == null)
		return;
	try {
		writeLogEntry(status);
	} catch (Exception e) {
		e.printStackTrace();
	}finally {
		if (usingLogFile)
			closeLogFile();
	}
}
private void openLogFile() {
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
public void printTabulation() {
	for (int i = 0; i < tab; i++)
		log.print("  ");
}

public void printTag(String name, HashMap parameters) {
	printTabulation();
	log.print('<');
	log.print(name);
	tab++;
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
	tab--;
	log.println(">");
}

/**
 * @see ILogListener
 */
public synchronized void shutdown() {
	if (usingLogFile) {
		try {
			openLogFile();
			endTag(ELEMENT_LOG);
		} finally {
			closeLogFile();
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
public void startTag(String name, HashMap parameters) {
	printTag(name, parameters);
	tab++;
}
protected void write(Throwable throwable) throws SAXException {
	if (throwable == null)
		return;
	HashMap attributes = new HashMap();
	attributes.put(ATTRIBUTE_MESSAGE, throwable.getMessage());
	attributes.put(ATTRIBUTE_TRACE, encodeStackTrace(throwable));
	startTag(ELEMENT_EXCEPTION, attributes);
	endTag(ELEMENT_EXCEPTION);
}
protected void write(IStatus status) throws SAXException {
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
protected void writeLogEntry(IStatus status) throws SAXException {
	tab = 0;
	HashMap attributes = new HashMap();
	attributes.put(ATTRIBUTE_DATE, new Date());
	startTag(ELEMENT_LOG_ENTRY, attributes);
	tab = 1;
	write(status);
	endTag(ELEMENT_LOG_ENTRY);
	tab = 0;
}
}


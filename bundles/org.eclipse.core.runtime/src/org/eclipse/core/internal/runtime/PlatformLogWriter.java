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
	protected Writer log = null;
	protected int tabDepth;
	
	protected static final String LINE_SEPARATOR;
	protected static final String TAB_STRING = "  ";
	
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

	static {
		String s = System.getProperty("line.separator");
		LINE_SEPARATOR = s == null ? "\n" : s;
	}

public PlatformLogWriter(File file) {
	this.logFile = file;
	// remove old log file
	logFile.delete();
}
/**
 * This constructor should only be used to pass System.out .
 */
public PlatformLogWriter(OutputStream out) {
	log = new OutputStreamWriter(out);
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
protected void closeLogFile() throws IOException {
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
protected void endTag(String name) throws IOException {
	tabDepth--;
	printTag('/' + name, null);
}
/**
 * @see ILogListener#logging.
 */
public synchronized void logging(IStatus status, String plugin) {
	// thread safety: (Concurrency003)
	if (logFile != null)
		openLogFile();
	if (log == null)
		log = new OutputStreamWriter(System.err);
	try {
		try {
			writeLogEntry(status);
		} finally {
			if (logFile != null)
				closeLogFile();
			else 
				log.flush();
		}			
	} catch (Exception e) {
		System.err.println("An exception occurred while writing to the platform log:");
		System.err.println(e.getClass().getName() + ": " + e.getMessage());
		System.err.println("Logging to the console instead.");
		//we failed to write, so dump log entry to console instead
		try {
			log = new OutputStreamWriter(System.err);
			writeLogEntry(status);
			log.flush();
		} catch (Exception e2) {
			System.err.println("An exception occurred while logging to the console:");
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
	} finally {
			log = null;
	}
}
protected void openLogFile() {
	try {
		boolean newLog = !logFile.exists();
		log =new BufferedWriter(new FileWriter(logFile.getAbsolutePath(), true));
		if (newLog) {
			println(XML_VERSION);
			startTag(ELEMENT_LOG, null);
		}
	} catch (IOException e) {
		// there was a problem opening the log file so log to the console
		log = new OutputStreamWriter(System.err);
	}
}
/**
 * Writes the given string to the log, followed by the line terminator string.
 */
protected void println(String s) throws IOException {
	log.write(s);
	log.write(LINE_SEPARATOR);
}
protected void printTabulation() throws IOException {
	for (int i = 0; i < tabDepth; i++)
		log.write(TAB_STRING);
}

protected void printTag(String name, HashMap parameters) throws IOException {
	printTabulation();
	log.write('<');
	log.write(name);
	tabDepth++;
	if (parameters != null)
		for (Enumeration enum = Collections.enumeration(parameters.keySet()); enum.hasMoreElements();) {
			//new line for each attribute if there's more than one
			if (parameters.size() > 1) {
				log.write(LINE_SEPARATOR);
				printTabulation();
			}
			log.write(" ");
			String key = (String) enum.nextElement();
			log.write(key);
			log.write("=\"");
			log.write(getEscaped(String.valueOf(parameters.get(key))));
			log.write("\"");
		}
	tabDepth--;
	println(">");
}
/**
 * Shuts down the platform log.
 */
public synchronized void shutdown() {
	try {
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
				Writer old = log;
				log = null;
				old.flush();
				old.close();
			}
		}
	} catch (Exception e) {
		//we've shutdown the log, so not much else we can do!
		e.printStackTrace();
	}
}
protected void startTag(String name, HashMap parameters) throws IOException {
	printTag(name, parameters);
	tabDepth++;
}
protected void write(Throwable throwable) throws IOException {
	if (throwable == null)
		return;
	HashMap attributes = new HashMap();
	attributes.put(ATTRIBUTE_MESSAGE, throwable.getMessage());
	attributes.put(ATTRIBUTE_TRACE, encodeStackTrace(throwable));
	startTag(ELEMENT_EXCEPTION, attributes);
	endTag(ELEMENT_EXCEPTION);
}
protected void write(IStatus status) throws IOException {
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
protected void writeLogEntry(IStatus status) throws IOException {
	tabDepth = 0;
	HashMap attributes = new HashMap();
	attributes.put(ATTRIBUTE_DATE, new Date());
	startTag(ELEMENT_LOG_ENTRY, attributes);
	write(status);
	endTag(ELEMENT_LOG_ENTRY);
}
}


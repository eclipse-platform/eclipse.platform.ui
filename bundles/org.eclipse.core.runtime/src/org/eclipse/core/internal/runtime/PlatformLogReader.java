package org.eclipse.core.internal.runtime;

import java.io.*;
import java.util.*;

import org.apache.xerces.parsers.SAXParser;
import org.eclipse.core.internal.boot.DelegatingURLClassLoader;
import org.eclipse.core.internal.boot.PlatformClassLoader;
import org.eclipse.core.runtime.*;
import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Reads a structured log from disk and reconstructs status and exception objects.
 * General strategy: log entries that are malformed in any way are skipped, and an extra
 * status is returned mentioned that there were problems.
 */
public class PlatformLogReader extends DefaultHandler {
	private static final String NULL_STRING = "" + null;
	private ArrayList result = null;
	private Stack objectStack = null;

/**
 * Returns a severity given its string representation.  
 * Converse of PlatformLogReader#encodeSeverity.
 */
protected int decodeSeverity(String severity) {
	if (severity == null)
		return -1;
	if (severity.equals("ERROR"))
		return IStatus.ERROR;
	if (severity.equals("INFO"))
		return IStatus.INFO;
	if (severity.equals("WARNING"))
		return IStatus.WARNING;
	if (severity.equals("OK"))
		return IStatus.OK;
	try {
		return Integer.parseInt(severity);
	} catch (NumberFormatException e) {
		return -1;
	}
}
public void endElement(String uri, String elementName, String qName) {
	if (elementName.equals(PlatformLogWriter.ELEMENT_LOG_ENTRY)) {
		readLogEntry();
	} else if (elementName.equals(PlatformLogWriter.ELEMENT_STATUS)) {
		readStatus();
	} else if (elementName.equals(PlatformLogWriter.ELEMENT_EXCEPTION)) {
		readException();
	}
}
/**
 * @see org.xml.sax.ErrorHandler#error.
 */
public void error(SAXParseException ex) {
	log(ex);
}
/**
 * @see org.xml.sax.ErrorHandler#fatalError
 */
public void fatalError(SAXParseException ex) throws SAXException {
	log(ex);
	throw ex;
}
/**
 * Given a stack trace without carriage returns, returns a pretty-printed stack.
 */
protected String formatStack(String stack) {
	StringWriter sWriter = new StringWriter();
	PrintWriter writer = new PrintWriter(sWriter);
	StringTokenizer tokenizer = new StringTokenizer(stack);
	//first entry has no indentation
	if (tokenizer.hasMoreTokens())
		writer.print(tokenizer.nextToken());
	while (tokenizer.hasMoreTokens()) {
		String next = tokenizer.nextToken();
		if (next != null && next.length() > 0) {
			if (next.equals("at")) {
				writer.println();
				writer.print('\t');
				writer.print(next);
			} else {
				writer.print(' ');
				writer.print(next);
			}
		}
	}
	writer.flush();
	writer.close();
	return sWriter.toString();
}
protected String getString(Attributes attributes, String attributeName) {
	return attributes.getValue(attributeName);
}
protected void log(Exception ex) {
	String msg = Policy.bind("meta.exceptionParsingLog", ex.getMessage());
	result.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, Platform.PARSE_PROBLEM, msg, ex));
}
protected void readException() {
	Attributes attributes = (Attributes)objectStack.pop();
	String message = getString(attributes, PlatformLogWriter.ATTRIBUTE_MESSAGE);
	if (NULL_STRING.equals(message)) {
		message = null;
	}
	String stack = getString(attributes, PlatformLogWriter.ATTRIBUTE_TRACE);
	objectStack.push(new FakeException(message, formatStack(stack)));
}
protected void readLogEntry() {
	while (!objectStack.isEmpty()) {
		Object o = objectStack.pop();
		if (o instanceof IStatus) {
			result.add(o);
		}
	}
}
/**
 * Reads the given log file and returns the contained status objects. 
 * If the log file could not be read, a status object indicating this fact
 * is returned.
 */
public IStatus[] readLogFile(String path) {
	result = new ArrayList();
	objectStack = new Stack();
	//XXX workaround.  See Bug 5801.
	DelegatingURLClassLoader xmlClassLoader = (DelegatingURLClassLoader)Platform.getPluginRegistry().getPluginDescriptor("org.apache.xerces").getPluginClassLoader();
	PlatformClassLoader.getDefault().setImports(new DelegatingURLClassLoader[] { xmlClassLoader });
	try {
		Reader reader = new BufferedReader(new FileReader(path));
		SAXParser parser = new SAXParser();
		parser.setContentHandler(this);
		parser.setErrorHandler(this);
		parser.parse(new InputSource(reader));
	} catch (IllegalStateException e) {
		log(e);
	} catch (IOException e) {
		log(e);
	}catch (SAXException e) {
		log(e);
	}finally {
		PlatformClassLoader.getDefault().setImports(null);
	}
	return (IStatus[]) result.toArray(new IStatus[result.size()]);
}
protected void readStatus() {
	//status children are either child statii or an exception
	Attributes attributes = null;
	Throwable exception = null;
	ArrayList children = new ArrayList();
	while (!objectStack.isEmpty()) {
		Object o = objectStack.pop();
		if (o instanceof IStatus) {
			//stacking reversed order, so reverse order on pop
			children.add(0, o);
		} else if (o instanceof Throwable) {
			exception = (Throwable)o;
		} else {
			attributes = (Attributes)o;
			break;
		}
	}
	if (attributes == null) 
		throw new IllegalStateException("Status missing attributes");//$NON-NLS$
	int severity = decodeSeverity(getString(attributes, PlatformLogWriter.ATTRIBUTE_SEVERITY));
	String pluginID = getString(attributes, PlatformLogWriter.ATTRIBUTE_PLUGIN_ID);
	String s = getString(attributes, PlatformLogWriter.ATTRIBUTE_CODE);
	int code = s == null ? -1 : Integer.parseInt(s);
	String message = getString(attributes, PlatformLogWriter.ATTRIBUTE_MESSAGE);
	if (severity == -1 || pluginID == null || code == -1 || message == null)
		throw new IllegalStateException();

	if (children.size() > 0) {
		IStatus[] childStatii = (IStatus[]) children.toArray(new IStatus[children.size()]);
		objectStack.push(new MultiStatus(pluginID, code, childStatii, message, exception));
	} else {
		objectStack.push(new Status(severity, pluginID, code, message, exception));
	}
}
public void startElement(String uri, String elementName, String qName, Attributes attributes) {
	objectStack.push(new AttributesImpl(attributes));
}
/**
 * @see org.xml.sax.ErrorHandler#warning.
 */
public void warning(SAXParseException ex) {
	log(ex);
}	
/**
 * A reconsituted exception that only contains a stack trace and a message.
 */
class FakeException extends Throwable {
	private String message;
	private String stackTrace;
	FakeException(String msg, String stack) {
		this.message = msg;
		this.stackTrace = stack;
	}
	public String getMessage() {
		return message;
	}
	public void printStackTrace() {
		printStackTrace(System.out);
	}
	public void printStackTrace(PrintWriter writer) {
		writer.println(stackTrace);
	}
	public void printStackTrace(PrintStream stream) {
		stream.println(stackTrace);
	}		
}
}


package org.eclipse.core.internal.runtime;

import java.io.*;
import java.util.ArrayList;

import java.util.StringTokenizer;
import javax.xml.parsers.*;
import org.eclipse.core.runtime.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * Reads a structured log from disk and reconstructs status and exception objects.
 * General strategy: log entries that are malformed in any way are skipped, and an extra
 * status is returned mentioned that there were problems.
 */
class PlatformLogReader {
	/**
	 * Temporary main class for testing...
	 */
	public static void main(String[] args) {
		if (args.length != 1)
			return;
		String filename = args[0];
		IStatus[] statii = new PlatformLogReader().readLogFile(filename);
		System.out.println(statii.length + " status objects read from log");
	}
	/**
	 * Reads the given log file and returns the contained status objects. 
	 * If the log file could not be read, a status object indicating this fact
	 * is returned.
	 */
public IStatus[] readLogFile(String path) {
	Exception err = null;
	try {
		DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = parser.parse(new File(path));
		return (IStatus[])read(document.getFirstChild());
	} catch (IOException e) {
		err = e;
	} catch (SAXException e) {
		err = e;
	} catch (ParserConfigurationException e) {
		err = e;
	}
	return new IStatus[] {new Status(IStatus.WARNING, Platform.PI_RUNTIME, 1, "Unable to parse error log", err)};
}
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
protected String getString(Node target, String attributeName) {
	NamedNodeMap map = target.getAttributes();
	Node item = map.getNamedItem(attributeName);
	return item == null ? null : item.getNodeValue();
}
protected IStatus[] readLog(Node node) {
	NodeList children = node.getChildNodes();
	int childCount = children.getLength();
	boolean parseProblems = false;
	ArrayList statii = new ArrayList(childCount);
	for (int i = 0; i < childCount; i++) {
		try {
			Object status = read(children.item(i));
			if (status != null)
				statii.add(status);
		} catch (RuntimeException e) {
			parseProblems = true;
		}
	}
	if (parseProblems) {
		statii.add(new Status(IStatus.WARNING, Platform.PI_RUNTIME, 1, "Some log file entries could not be read", null));
	}
	return (IStatus[]) statii.toArray(new IStatus[statii.size()]);
}
protected Object read(Node node) {
	if (node == null)
		return null;
	switch (node.getNodeType()) {
		case Node.ELEMENT_NODE :
			String name = node.getNodeName();
			if (name.equals(PlatformLogWriter.ELEMENT_LOG)) {
				return readLog(node);
			} else if (name.equals(PlatformLogWriter.ELEMENT_LOG_ENTRY)) {
				return readLogEntry(node);
			} else if (name.equals(PlatformLogWriter.ELEMENT_STATUS)) {
				return readStatus(node);
			} else if (name.equals(PlatformLogWriter.ELEMENT_EXCEPTION)) {
				return readException(node);
			}
			break;
		//ignore text nodes for now, we don't have any in the log format
		case Node.TEXT_NODE:
	}
	return null;
}
protected IStatus readLogEntry(Node node) {
	NodeList children = node.getChildNodes();
	int len = children.getLength();
	for (int i = 0; i < len; i++) {
		Object o = read(children.item(i));
		if (o instanceof IStatus) {
			return (IStatus)o;
		}
	}
	return null;
}
protected IStatus readStatus(Node node) {
	int severity = decodeSeverity(getString(node, PlatformLogWriter.ATTRIBUTE_SEVERITY));
	String pluginID = getString(node, PlatformLogWriter.ATTRIBUTE_PLUGIN_ID);
	String s = getString(node, PlatformLogWriter.ATTRIBUTE_CODE);
	int code = s == null ? -1 : Integer.parseInt(s);
	String message = getString(node, PlatformLogWriter.ATTRIBUTE_MESSAGE);
	if (severity == -1 || pluginID == null || code == -1 || message == null)
		throw new IllegalStateException();
	//status children are either child statii or an exception
	Throwable exception = null;
	ArrayList children = new ArrayList();
	NodeList childNodes = node.getChildNodes();
	int childCount = childNodes.getLength();
	for (int i = 0; i < childCount; i++) {
		Object o = read(childNodes.item(i));
		if (o instanceof IStatus) {
			children.add(o);
		} else if (o instanceof Throwable) {
			exception = (Throwable)o;
			exception.printStackTrace();
		}
	}
	if (children.size() > 0) {
		IStatus[] childStatii = (IStatus[]) children.toArray(new IStatus[children.size()]);
		return new MultiStatus(pluginID, code, childStatii, message, exception);
	} else {
		return new Status(severity, pluginID, code, message, exception);
	}

}

protected Throwable readException(Node node) {
	String message = getString(node, PlatformLogWriter.ATTRIBUTE_MESSAGE);
	String stack = getString(node, PlatformLogWriter.ATTRIBUTE_TRACE);
	return new FakeException(message, formatStack(stack));
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


package org.eclipse.team.tests.ccvs.ui.logformatter;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import junit.framework.Assert;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public abstract class LogEntry {
	LogEntryContainer parent;
	String name;
	
	/**
	 * Creates a new log entry with the specified parent.
	 * @param parent the parent container
	 * @param name the name of the entry
	 */
	public LogEntry(LogEntryContainer parent, String name) {
		this.parent = parent;
		this.name = name != null ? name : "unknown";
		if (parent != null) parent.addEntry(this);
	}
	
	/**
	 * Accepts a visitor.
	 * @param visitor the visitor
	 */
	public abstract void accept(ILogEntryVisitor visitor);
	
	/**
	 * Returns the name of this entry.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the parent container of this entry, or null if none.
	 */
	public LogEntryContainer getParent() {
		return parent;
	}
	
	/**
	 * Reads an array of log entries from a file.
	 * @return the log entries
	 */
	public static RootEntry readLog(File file) throws IOException, SAXException {
		XMLReader reader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
		LogContentHandler contentHandler = new LogContentHandler();
		reader.setContentHandler(contentHandler);
		reader.parse(new InputSource(new FileInputStream(file)));
		return contentHandler.getLogEntries();
	}
	
	private static class LogContentHandler extends DefaultHandler implements ContentHandler {
		private RootEntry root = null;
		private LogEntry current = null;
		
		public RootEntry getLogEntries() {
			return root;
		}
		public void startElement(String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
			if ("log".equals(localName)) {
				Assert.assertNull(current);
				current = root = new RootEntry(null, attributes);
			} else if ("case".equals(localName)) {
				Assert.assertNotNull(current);
				Assert.assertTrue(current instanceof RootEntry);
				current = new CaseEntry((LogEntryContainer) current, attributes);
			} else if ("group".equals(localName)) {
				Assert.assertNotNull(current);
				Assert.assertTrue(current instanceof CaseEntry || current instanceof GroupEntry);
				current = new GroupEntry((LogEntryContainer) current, attributes);
			} else if ("task".equals(localName)) {
				Assert.assertNotNull(current);
				Assert.assertTrue(current instanceof CaseEntry || current instanceof GroupEntry);
				current = new TaskEntry((LogEntryContainer) current, attributes);
			} else if ("result".equals(localName)) {
				Assert.assertNotNull(current);
				Assert.assertTrue(current instanceof TaskEntry);
				((TaskEntry) current).addResult(new Result(attributes));
			} else if ("abort".equals(localName)) {
				// currently we ignore failure entries
				// XXX need a good way to represent failures
			} else if ("trace".equals(localName)) {
				// currently we ignore stack frames associated with failure entries
			} else if ("status".equals(localName)) {
				// currently we ignore status associated with failure entries
			} else {
				throw new SAXException("Unrecognized element: " + localName);
			}
		}
		public void endElement(String uri, String localName, String qName)
			throws SAXException {
			Assert.assertNotNull(current);
			if ("result".equals(localName) || "abort".equals(localName) ||
				"trace".equals(localName) || "status".equals(localName)) {
				// nothing to do
			} else {
				current = current.getParent();
			}
		}
	}
}

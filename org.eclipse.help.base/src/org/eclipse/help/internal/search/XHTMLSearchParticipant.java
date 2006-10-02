/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Stack;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.xhtml.DynamicXHTMLProcessor;
import org.eclipse.help.search.ISearchIndex;
import org.eclipse.help.search.LuceneSearchParticipant;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The search participant responsible for indexing XHTML documents.
 */
public class XHTMLSearchParticipant extends LuceneSearchParticipant {
	
	private Stack stack = new Stack();
	private SAXParser parser;
	private boolean hasFilters;

	private static class ParsedXMLContent {
		private StringBuffer buffer = new StringBuffer();
		private StringBuffer summary = new StringBuffer();
		private String title;
		private String locale;
		private static int SUMMARY_LENGTH = 200;

		public ParsedXMLContent(String locale) {
			this.locale = locale;
		}

		public String getLocale() {
			return locale;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public void addToSummary(String text) {
			if (summary.length() >= SUMMARY_LENGTH)
				return;
			if (summary.length() > 0)
				summary.append(" "); //$NON-NLS-1$
			summary.append(text);
			if (summary.length() > SUMMARY_LENGTH)
				summary.delete(SUMMARY_LENGTH, summary.length());
		}

		public void addText(String text) {
			if (buffer.length() > 0)
				buffer.append(" "); //$NON-NLS-1$
			buffer.append(text);
		}

		public Reader newContentReader() {
			return new StringReader(buffer.toString());
		}

		public String getSummary() {
			// if the summary starts with the title, trim that part off.
			String summaryStr = summary.toString();
			if (title != null && summaryStr.length() >= title.length()) {
				String header = summaryStr.substring(0, title.length());
				if (header.equalsIgnoreCase(title)) {
					return summaryStr.substring(title.length()).trim();
				}
			}
			return summaryStr;
		}

		public String getTitle() {
			return title;
		}
	}

	private class XMLHandler extends DefaultHandler {

		public ParsedXMLContent data;

		public XMLHandler(ParsedXMLContent data) {
			this.data = data;
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			stack.push(qName);
			if (attributes.getValue("filter") != null || qName.equalsIgnoreCase("filter")) { //$NON-NLS-1$ //$NON-NLS-2$
				hasFilters = true;
			}
		}

		public void endElement(String uri, String localName, String qName) throws SAXException {
			String top = (String) stack.peek();
			if (top != null && top.equals(qName))
				stack.pop();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
		 */
		public void characters(char[] characters, int start, int length) throws SAXException {
			if (length == 0)
				return;
			StringBuffer buff = new StringBuffer();
			for (int i = 0; i < length; i++) {
				buff.append(characters[start + i]);
			}
			String text = buff.toString().trim();
			if (text.length() > 0)
				handleText(text, data);
		}
		
		/**
		 * Note: throws clause does not declare IOException due to a bug in
		 * sun jdk: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6327149
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#resolveEntity(java.lang.String, java.lang.String)
		 */
		public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
			return new InputSource(new StringReader("")); //$NON-NLS-1$
		}
	}

	/*
	 * @see LuceneSearchParticipant#addDocument(String, String, URL, String, Document)
	 */
	public IStatus addDocument(ISearchIndex index, String pluginId, String name, URL url, String id,
			Document doc) {
		InputStream stream = null;
		try {
			if (parser == null) {
				parser = SAXParserFactory.newInstance().newSAXParser();
			}
			stack.clear();
			ParsedXMLContent parsed = new ParsedXMLContent(index.getLocale());
			XMLHandler handler = new XMLHandler(parsed);
			stream = DynamicXHTMLProcessor.process(name, url.openStream(), index.getLocale(), false);
			parser.parse(stream, handler);
			doc.add(Field.Text("contents", parsed.newContentReader())); //$NON-NLS-1$
			doc.add(Field.Text("exact_contents", parsed.newContentReader())); //$NON-NLS-1$
			String title = parsed.getTitle();
			if (title != null) {
				addTitle(title, doc);
			}
			String summary = parsed.getSummary();
			if (summary != null) {
				doc.add(Field.UnIndexed("summary", summary)); //$NON-NLS-1$
			}
			if (hasFilters) {
				doc.add(Field.UnIndexed("filters", "true")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return Status.OK_STATUS;
		} catch (Exception e) {
			return new Status(IStatus.ERROR, HelpBasePlugin.PLUGIN_ID, IStatus.ERROR,
					"Exception occurred while adding document " + name //$NON-NLS-1$
							+ " to index.", //$NON-NLS-1$
					e);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
				stream = null;
			}
		}
	}

	protected void handleText(String text, ParsedXMLContent data) {
		String stackPath = getElementStackPath();
		IPath path = new Path(stackPath);
		if (path.segment(1).equalsIgnoreCase("body")) { //$NON-NLS-1$
			data.addText(text);
			data.addToSummary(text);
		} else if (path.segment(1).equalsIgnoreCase("head")) { //$NON-NLS-1$
			data.setTitle(text);
		}
	}
	
	/**
	 * Returns the name of the element that is currently at the top of the element stack.
	 * 
	 * @return
	 */

	protected String getTopElement() {
		return (String) stack.peek();
	}

	/**
	 * Returns the full path of the current element in the stack separated by the '/' character.
	 * 
	 * @return the path to the current element in the stack.
	 */
	protected String getElementStackPath() {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < stack.size(); i++) {
			if (i > 0)
				buf.append("/"); //$NON-NLS-1$
			buf.append((String) stack.get(i));
		}
		return buf.toString();
	}
}
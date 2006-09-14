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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
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
import org.eclipse.help.internal.xhtml.BundleUtil;
import org.eclipse.help.internal.xhtml.DynamicXHTMLProcessor;
import org.eclipse.help.internal.xhtml.XHTMLSupport;
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
	
    private static String XHTML1_TRANSITIONAL = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"; //$NON-NLS-1$
    private static String XHTML1_STRICT = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"; //$NON-NLS-1$
    private static String XHTML1_FRAMESET = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd"; //$NON-NLS-1$

	private Stack stack = new Stack();
	private SAXParser parser;
	private Set filters;

    /*
     * Load XHTML dtds from help base plugin location.
     */
    protected static Hashtable dtdMap = new Hashtable();

    static {
        String dtdBaseLocation = "dtds/xhtml1-20020801/"; //$NON-NLS-1$

        String dtdLocation = dtdBaseLocation + "xhtml1-transitional.dtd"; //$NON-NLS-1$
        URL dtdURL_T = BundleUtil.getResourceAsURL(dtdLocation,
            HelpBasePlugin.PLUGIN_ID);
        dtdMap.put(XHTML1_TRANSITIONAL, dtdURL_T);

        dtdLocation = dtdBaseLocation + "xhtml1-strict.dtd"; //$NON-NLS-1$
        URL dtdURL_S = BundleUtil.getResourceAsURL(dtdLocation,
        		HelpBasePlugin.PLUGIN_ID);
        dtdMap.put(XHTML1_STRICT, dtdURL_S);

        dtdLocation = dtdBaseLocation + "xhtml1-frameset.dtd"; //$NON-NLS-1$
        URL dtdURL_F = BundleUtil.getResourceAsURL(dtdLocation,
        		HelpBasePlugin.PLUGIN_ID);
        dtdMap.put(XHTML1_FRAMESET, dtdURL_F);
    }
    
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
			
			/*
			 * Keep track of all the filters this document. e.g.,
			 * "os=macosx", "ws=carbon", ...
			 */
			String filterAttribute = attributes.getValue("filter"); //$NON-NLS-1$
			if (filterAttribute != null) {
				filters.add(filterAttribute);
			}
			if (qName.equalsIgnoreCase("filter")) { //$NON-NLS-1$
				String name = attributes.getValue("name"); //$NON-NLS-1$
				String value = attributes.getValue("value"); //$NON-NLS-1$
				if (name != null && value != null) {
					filters.add(name + '=' + value);
				}
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
			if (systemId.equals(XHTML1_TRANSITIONAL)
                    || systemId.equals(XHTML1_STRICT)
                    || systemId.equals(XHTML1_FRAMESET)) {
				try {
	                URL dtdURL = (URL) dtdMap.get(systemId);
	                InputSource in = new InputSource(dtdURL.openStream());
	                in.setSystemId(dtdURL.toExternalForm());
	                return in;
				}
				catch (IOException e) {
					throw new SAXException(e);
				}
            }
            return null;
		}
	}

	/*
	 * @see LuceneSearchParticipant#addDocument(String, String, URL, String, Document)
	 */
	public IStatus addDocument(ISearchIndex index, String pluginId, String name, URL url, String id,
			Document doc) {
		filters = new HashSet();
		InputStream stream = null;
		try {
			if (parser == null)
				parser = SAXParserFactory.newInstance().newSAXParser();
			stack.clear();
			ParsedXMLContent parsed = new ParsedXMLContent(index.getLocale());
			XMLHandler handler = new XMLHandler(parsed);
			stream = DynamicXHTMLProcessor.process(name, url.openStream(), index.getLocale(), index instanceof SearchIndexCache);
			parser.parse(stream, handler);
			doc.add(Field.Text("contents", parsed.newContentReader())); //$NON-NLS-1$
			doc.add(Field.Text("exact_contents", parsed //$NON-NLS-1$
					.newContentReader()));
			String title = parsed.getTitle();
			if (title != null)
				addTitle(title, doc);
			String summary = parsed.getSummary();
			if (summary != null)
				doc.add(Field.UnIndexed("summary", summary)); //$NON-NLS-1$
			// store the filters this document is sensitive to
			if (doc.getField("filters") == null && filters.size() > 0) { //$NON-NLS-1$
				filters = generalizeFilters(filters);
				doc.add(Field.UnIndexed("filters", serializeFilters(filters))); //$NON-NLS-1$
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
	
	/**
	 * Given the set of all filters in a document, generalize the filters to
	 * denote which filters this document is sensitive to. This strips off
	 * all the environment-specific information. For single value filters like
	 * os, simply keep the name of the filter. For multi value filters like plugin,
	 * keep each name and value pair.
	 * 
	 * e.g.,
	 * before: "os=linux,ws!=gtk,plugin!=org.eclipse.help,product=org.eclipse.sdk"
	 * after:  "os,ws,plugin=org.eclipse.help,product"
	 * 
	 * @param filters the filters contained in the document
	 * @return the filters this document is sensitive to in general
	 */
	private Set generalizeFilters(Set filters) {
		Set processed = new HashSet();
		Iterator iter = filters.iterator();
		while (iter.hasNext()) {
			String filter = (String)iter.next();
			int index = filter.indexOf('=');
			if (index > 0) {
				String[] tokens = filter.split("!?="); //$NON-NLS-1$
				String name = tokens[0]; 
				String value = tokens[1];
				// strip any leading NOT symbols ('!')
				if (value != null && value.length() > 0 && value.charAt(0) == '!') {
					value = value.substring(1);
				}
				if (XHTMLSupport.getFilterProcessor().isMultiValue(name)) {
					processed.add(name + '=' + value);
				}
				else {
					processed.add(name);
				}
			}
		}
		return processed;
	}
	
	/**
	 * Converts the given set of filters to string form. e.g.,
	 * "os,arch,plugin=org.eclipse.help"
	 * 
	 * @param set the set of filters to serialize
	 * @return the serialized string
	 */
	private String serializeFilters(Set set) {
		StringBuffer buf = new StringBuffer();
		Iterator iter = set.iterator();
		boolean firstIter = true;
		while (iter.hasNext()) {
			if (!firstIter) {
				buf.append(',');
			}
			firstIter = false;
			buf.append(iter.next());
		}
		return buf.toString();
	}
}
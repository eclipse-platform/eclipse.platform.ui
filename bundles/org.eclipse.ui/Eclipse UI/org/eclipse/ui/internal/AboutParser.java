package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.xml.sax.*;
import org.apache.xerces.parsers.*;
import org.xml.sax.helpers.*;
import java.io.*;
import java.util.*;
/**
 * A parser for the the welcome page
 */
public class AboutParser extends DefaultHandler {
	private static final String TAG_ABOUT = "about"; //$NON-NLS-1$	
	private static final String TAG_BOLD = "b"; //$NON-NLS-1$	
	private static final String TAG_LINK = "a"; //$NON-NLS-1$	
	private static final String ATT_HREF = "href"; //$NON-NLS-1$
	
	private SAXParser parser;

	private String title;
	private AboutItem item;

	private class AboutContentHandler implements ContentHandler {
		protected ContentHandler parent;
		public void setParent(ContentHandler p) {
			parent = p;
		}
		public void characters(char[] ch, int start, int length) throws SAXException {
		}
		public void endDocument() throws SAXException {
		}
		public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
		}
		public void endPrefixMapping(String prefix) throws SAXException {
		}
		public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		}
		public void processingInstruction(String target, String data) throws SAXException {
		}
		public void setDocumentLocator(Locator locator) {
		}
		public void skippedEntity(String name) throws SAXException {
		}
		public void startDocument() throws org.xml.sax.SAXException {
		}
		public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		}
		public void startPrefixMapping(String prefix, String uri) throws SAXException {
		}
	}

	private class ItemHandler extends AboutContentHandler {
		private ArrayList boldRanges = new ArrayList();
		private ArrayList linkRanges = new ArrayList();
		private ArrayList hrefs  = new ArrayList();
		private StringBuffer text = new StringBuffer();
		private int offset = 0;
		private int textStart;

		private class BoldHandler extends AboutContentHandler {
			public void characters(char[] ch, int start, int length) throws SAXException {
				ItemHandler.this.characters(ch, start, length);
			}
			public void endElement (String namespaceURI, String localName, String qName) throws SAXException {
				if (localName.equals(TAG_BOLD)) {
					boldRanges.add(new int[] {textStart, offset - textStart});
					parser.setContentHandler(parent);
				}
			}
		}
		private class LinkHandler extends AboutContentHandler {
			public LinkHandler(String href) {
				hrefs.add(href);
			}
			public void characters(char[] ch, int start, int length) throws SAXException {
				ItemHandler.this.characters(ch, start, length);
			}
			public void endElement (String namespaceURI, String localName, String qName) throws SAXException {
				if (localName.equals(TAG_LINK)) {
					linkRanges.add(new int[] {textStart, offset - textStart});
					parser.setContentHandler(parent);
				}
			}
		}	
		
		protected AboutItem constructAboutItem() {
			return new AboutItem(
				text.toString(), 
				(int[][])boldRanges.toArray(new int[boldRanges.size()][2]),
				(int[][])linkRanges.toArray(new int[linkRanges.size()][2]),
				(String[])hrefs.toArray(new String[hrefs.size()]));
		}
		public void characters(char[] ch, int start, int length) throws SAXException {
			for (int i = 0; i < length; i++) {
				text.append(ch[start + i]);
			}
			offset += length;
		}

		public void startElement (String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
			textStart = offset;
			if (localName.equals(TAG_BOLD)) {
				BoldHandler h = new BoldHandler();
				h.setParent(ItemHandler.this);
				parser.setContentHandler(h);
			} else if(localName.equals(TAG_LINK)) {
				LinkHandler h = new LinkHandler(atts.getValue(ATT_HREF));
				h.setParent(ItemHandler.this);
				parser.setContentHandler(h);
			}
		}
		public void endElement (String namespaceURI, String localName, String qName) throws SAXException {
			if (localName.equals(TAG_ABOUT)) {
				item = constructAboutItem();
				parser.setContentHandler(parent);
			}
		}
	}	
/**
 * Creates a new welcome parser.
 */
public AboutParser() {
	super();
	parser = new SAXParser();
	parser.setContentHandler(this);
	parser.setDTDHandler(this);
	parser.setEntityResolver(this);
	parser.setErrorHandler(this);
}
/**
 * Returns the about item.
 */
public AboutItem getAboutItem() {
	return item;
}
/**
 * Returns the title
 */
public String getTitle() {
	return title;
}
/**
 * Parse the contents of the about text
 */
public void parse(String s) {
	try {
		parser.parse(new InputSource(new StringReader(s)));
	} catch (SAXException e) {
		reportError(e);
	} catch (IOException e) {
		reportError(e);
	}
}
/**
 * Handles the start element
 */
public void startElement (String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
	if (localName.equals(TAG_ABOUT)) {
		AboutContentHandler h = new ItemHandler();
		h.setParent(this);
		parser.setContentHandler(h);
	}
}
/**
 * Report an error
 */
private void reportError(Exception e) {
	if (!WorkbenchPlugin.DEBUG) {
		// only report ini problems if the -debug command line argument is used
		return;
	}
	IStatus status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 1, WorkbenchMessages.getString("Exception in AboutParser.parse"), e);  //$NON-NLS-1$	
	WorkbenchPlugin.log("An error occured parsing an about.ini file", status);  //$NON-NLS-1$	
}
}

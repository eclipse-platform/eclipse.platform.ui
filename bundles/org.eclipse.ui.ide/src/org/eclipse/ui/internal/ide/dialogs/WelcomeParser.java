/*******************************************************************************
. * Copyright (c) 2000, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.ide.dialogs;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A parser for the the welcome page
 */
public class WelcomeParser extends DefaultHandler {
	private static final String TAG_WELCOME_PAGE = "welcomePage"; //$NON-NLS-1$

	private static final String TAG_INTRO = "intro"; //$NON-NLS-1$

	private static final String TAG_ITEM = "item"; //$NON-NLS-1$

	private static final String TAG_BOLD = "b"; //$NON-NLS-1$

	private static final String TAG_ACTION = "action"; //$NON-NLS-1$

	private static final String TAG_PARAGRAPH = "p"; //$NON-NLS-1$

	private static final String TAG_TOPIC = "topic"; //$NON-NLS-1$

	private static final String ATT_TITLE = "title"; //$NON-NLS-1$

	private static final String ATT_FORMAT = "format"; //$NON-NLS-1$

	private static final String ATT_PLUGIN_ID = "pluginId"; //$NON-NLS-1$

	private static final String ATT_CLASS = "class"; //$NON-NLS-1$

	private static final String ATT_ID = "id"; //$NON-NLS-1$

	private static final String ATT_HREF = "href"; //$NON-NLS-1$

	private static final String FORMAT_WRAP = "wrap"; //$NON-NLS-1$

	private static final char DELIMITER = '\n'; // sax parser replaces crlf with lf

	private SAXParser parser;

	private String title;

	private WelcomeItem introItem;

	private ArrayList<WelcomeItem> items = new ArrayList<>();

	private String format;

	private static class WelcomeContentHandler implements ContentHandler {
		protected ContentHandler parent;

		public void setParent(ContentHandler p) {
			parent = p;
		}

		@Override
		public void characters(char[] ch, int start, int length) {
		}

		@Override
		public void endDocument() {
		}

		@Override
		public void endElement(String namespaceURI, String localName,
				String qName) throws SAXException {
		}

		@Override
		public void endPrefixMapping(String prefix) {
		}

		@Override
		public void ignorableWhitespace(char[] ch, int start, int length) {
		}

		@Override
		public void processingInstruction(String target, String data) {
		}

		@Override
		public void setDocumentLocator(Locator locator) {
		}

		@Override
		public void skippedEntity(String name) {
		}

		@Override
		public void startDocument() {
		}

		@Override
		public void startElement(String namespaceURI, String localName,
				String qName, Attributes atts) throws SAXException {
		}

		@Override
		public void startPrefixMapping(String prefix, String uri) {
		}
	}

	private class WelcomePageHandler extends WelcomeContentHandler {
		public WelcomePageHandler(String newTitle) {
			title = newTitle;
		}

		@Override
		public void startElement(String namespaceURI, String localName,
				String qName, Attributes atts) throws SAXException {
			if (localName.equals(TAG_INTRO)) {
				ItemHandler h = new IntroItemHandler();
				h.setParent(WelcomePageHandler.this);
				parser.getXMLReader().setContentHandler(h);
			} else if (localName.equals(TAG_ITEM)) {
				ItemHandler h = new ItemHandler();
				h.setParent(WelcomePageHandler.this);
				parser.getXMLReader().setContentHandler(h);
			}
		}
	}

	private class ItemHandler extends WelcomeContentHandler {
		private ArrayList<int[]> boldRanges = new ArrayList<>();

		protected ArrayList<int[]> wrapRanges = new ArrayList<>();

		private ArrayList<int[]> actionRanges = new ArrayList<>();

		private ArrayList<String> pluginIds = new ArrayList<>();

		private ArrayList<String> classes = new ArrayList<>();

		private ArrayList<int[]> helpRanges = new ArrayList<>();

		private ArrayList<String> helpIds = new ArrayList<>();

		private ArrayList<String> helpHrefs = new ArrayList<>();

		private StringBuilder text = new StringBuilder();

		protected int offset = 0;

		protected int textStart;

		protected int wrapStart;

		private class BoldHandler extends WelcomeContentHandler {
			@Override
			public void characters(char[] ch, int start, int length) {
				ItemHandler.this.characters(ch, start, length);
			}

			@Override
			public void endElement(String namespaceURI, String localName,
					String qName) throws SAXException {
				if (localName.equals(TAG_BOLD)) {
					boldRanges.add(new int[] { textStart, offset - textStart });
					parser.getXMLReader().setContentHandler(parent);
				}
			}
		}

		private class ActionHandler extends WelcomeContentHandler {
			public ActionHandler(String pluginId, String className) {
				pluginIds.add(pluginId);
				classes.add(className);
			}

			@Override
			public void characters(char[] ch, int start, int length) {
				ItemHandler.this.characters(ch, start, length);
			}

			@Override
			public void endElement(String namespaceURI, String localName,
					String qName) throws SAXException {
				if (localName.equals(TAG_ACTION)) {
					actionRanges
							.add(new int[] { textStart, offset - textStart });
					parser.getXMLReader().setContentHandler(parent);
				}
			}
		}

		private class TopicHandler extends WelcomeContentHandler {
			public TopicHandler(String helpId, String href) {
				helpIds.add(helpId);
				helpHrefs.add(href);
			}

			@Override
			public void characters(char[] ch, int start, int length) {
				ItemHandler.this.characters(ch, start, length);
			}

			@Override
			public void endElement(String namespaceURI, String localName,
					String qName) throws SAXException {
				if (localName.equals(TAG_TOPIC)) {
					helpRanges.add(new int[] { textStart, offset - textStart });
					parser.getXMLReader().setContentHandler(parent);
				}
			}
		}

		protected WelcomeItem constructWelcomeItem() {
			if (isFormatWrapped()) {
				// replace all line delimiters with a space
				for (int[] range : wrapRanges) {
					int start = range[0];
					int length = range[1];
					for (int j = start; j < start + length; j++) {
						char ch = text.charAt(j);
						if (ch == DELIMITER) {
							text.replace(j, j + 1, " "); //$NON-NLS-1$
						}
					}
				}
			}
			return new WelcomeItem(
					text.toString(),
					boldRanges.toArray(new int[boldRanges.size()][2]),
					actionRanges
							.toArray(new int[actionRanges.size()][2]),
					pluginIds.toArray(new String[pluginIds.size()]),
					classes.toArray(new String[classes.size()]),
					helpRanges.toArray(new int[helpRanges.size()][2]),
					helpIds.toArray(new String[helpIds.size()]),
					helpHrefs.toArray(new String[helpHrefs.size()]));
		}

		@Override
		public void characters(char[] ch, int start, int length) {
			for (int i = 0; i < length; i++) {
				text.append(ch[start + i]);
			}
			offset += length;
		}

		@Override
		public void startElement(String namespaceURI, String localName,
				String qName, Attributes atts) throws SAXException {
			textStart = offset;
			switch (localName) {
			case TAG_BOLD:
				{
					BoldHandler h = new BoldHandler();
					h.setParent(ItemHandler.this);
					parser.getXMLReader().setContentHandler(h);
					break;
				}
			case TAG_ACTION:
				{
					ActionHandler h = new ActionHandler(atts
							.getValue(ATT_PLUGIN_ID), atts.getValue(ATT_CLASS));
					h.setParent(ItemHandler.this);
					parser.getXMLReader().setContentHandler(h);
					break;
				}
			case TAG_PARAGRAPH:
				wrapStart = textStart;
				break;
			case TAG_TOPIC:
				{
					TopicHandler h = new TopicHandler(atts.getValue(ATT_ID), atts
							.getValue(ATT_HREF));
					h.setParent(ItemHandler.this);
					parser.getXMLReader().setContentHandler(h);
					break;
				}
			default:
				break;
			}
		}

		@Override
		public void endElement(String namespaceURI, String localName,
				String qName) throws SAXException {
			if (localName.equals(TAG_ITEM)) {
				items.add(constructWelcomeItem());
				parser.getXMLReader().setContentHandler(parent);
			} else if (localName.equals(TAG_PARAGRAPH)) {
				wrapRanges.add(new int[] { wrapStart, offset - wrapStart });
			}
		}
	}

	private class IntroItemHandler extends ItemHandler {
		@Override
		public void endElement(String namespaceURI, String localName,
				String qName) throws SAXException {
			if (localName.equals(TAG_INTRO)) {
				introItem = constructWelcomeItem();
				parser.getXMLReader().setContentHandler(parent);
			} else if (localName.equals(TAG_PARAGRAPH)) {
				wrapRanges.add(new int[] { wrapStart, offset - wrapStart });
			}
		}
	}

	/**
	 * Creates a new welcome parser.
	 */
	public WelcomeParser() throws ParserConfigurationException, SAXException,
			FactoryConfigurationError {
		super();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setFeature("http://xml.org/sax/features/namespaces", true); //$NON-NLS-1$
		parser = factory.newSAXParser();

		parser.getXMLReader().setContentHandler(this);
		parser.getXMLReader().setDTDHandler(this);
		parser.getXMLReader().setEntityResolver(this);
		parser.getXMLReader().setErrorHandler(this);
	}

	/**
	 * Returns the intro item.
	 */
	public WelcomeItem getIntroItem() {
		return introItem;
	}

	/**
	 * Returns the items.
	 */
	public WelcomeItem[] getItems() {
		return items.toArray(new WelcomeItem[items.size()]);
	}

	/**
	 * Returns the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Returns whether or not the welcome editor input should be wrapped.
	 */
	public boolean isFormatWrapped() {
		return FORMAT_WRAP.equals(format);
	}

	/**
	 * Parse the contents of the input stream
	 */
	public void parse(InputStream is) {
		try {
			parser.parse(new InputSource(is), this);
		} catch (SAXException | IOException e) {
			IStatus status = new Status(IStatus.ERROR,
					IDEWorkbenchPlugin.IDE_WORKBENCH, 1, IDEWorkbenchMessages.WelcomeParser_parseException, e);
			IDEWorkbenchPlugin.log(IDEWorkbenchMessages.WelcomeParser_parseError, status);
		}
	}

	/**
	 * Handles the start element
	 */
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		if (localName.equals(TAG_WELCOME_PAGE)) {
			WelcomeContentHandler h = new WelcomePageHandler(atts
					.getValue(ATT_TITLE));
			format = atts.getValue(ATT_FORMAT);
			h.setParent(this);
			parser.getXMLReader().setContentHandler(h);
		}
	}
}

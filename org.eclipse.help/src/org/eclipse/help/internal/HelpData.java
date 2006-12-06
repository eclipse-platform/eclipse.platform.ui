/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * Interface to the help data file contents defined in plugin_customization.ini under
 * key org.eclipse.help/HELP_DATA.
 */
public class HelpData {

	private static final String ELEMENT_TOC_ORDER = "tocOrder"; //$NON-NLS-1$
	private static final String ELEMENT_HIDDEN = "hidden"; //$NON-NLS-1$
	private static final String ELEMENT_TOC = "toc"; //$NON-NLS-1$
	private static final String ELEMENT_CATEGORY = "category"; //$NON-NLS-1$
	private static final String ELEMENT_INDEX = "index"; //$NON-NLS-1$
	private static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$

	private static HelpData productHelpData;
	
	private URL url;
	private List tocOrder;
	private Set hiddenTocs;
	private Set hiddenIndexes;

	/*
	 * Get the active product's help data, or null if the product doesn't have
	 * help data or there is no active product.
	 */
	public static synchronized HelpData getProductHelpData() {
		if (productHelpData == null) {
			String helpDataFile = HelpPlugin.getDefault().getPluginPreferences().getString(HelpPlugin.HELP_DATA_KEY);
			if (helpDataFile.length() != 0) {
				IProduct product = Platform.getProduct();
				if (product != null) {
					URL url = product.getDefiningBundle().getEntry(helpDataFile);
					productHelpData = new HelpData(url);
				}
			}
		}
		return productHelpData;
	}

	/*
	 * Constructs help data from the XML at the given URL.
	 */
	public HelpData(URL url) {
		this.url = url;
	}
	
	/*
	 * Returns a list of strings which are the IDs of the tocs/categories listed
	 * in tocOrder.
	 */
	public synchronized List getTocOrder() {
		if (tocOrder == null) {
			loadHelpData();
		}
		return tocOrder;
	}

	/*
	 * Returns a set of strings which are the IDs of the tocs/categories listed
	 * in the hidden section.
	 */
	public synchronized Set getHiddenTocs() {
		if (hiddenTocs == null) {
			loadHelpData();
		}
		return hiddenTocs;
	}

	/*
	 * Returns a set of strings which are the IDs of the indexes listed
	 * in the hidden section.
	 */
	public synchronized Set getHiddenIndexes() {
		if (hiddenIndexes == null) {
			loadHelpData();
		}
		return hiddenIndexes;
	}

	/*
	 * Allow unit tests to override for providing test data.
	 */
	public InputStream getHelpDataFile(String filePath) throws IOException {
		return Platform.getProduct().getDefiningBundle().getEntry(filePath).openStream();
	}

	/*
	 * Loads and parses the file and populates the data structures.
	 */
	private void loadHelpData() {
		tocOrder = new ArrayList();
		hiddenTocs = new HashSet();
		hiddenIndexes = new HashSet();
		if (url != null) {
			try {
				SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
				InputStream in = url.openStream();
				parser.parse(in, new Handler());
			}
			catch (Throwable t) {
				String msg = "Error loading help data file \"" + url + "\""; //$NON-NLS-1$ //$NON-NLS-2$
				HelpPlugin.logError(msg, t);
			}
		}
	}

	/*
	 * SAX Handler for parsing the help data file.
	 */
	private class Handler extends DefaultHandler {
		
		private boolean inTocOrder;
		private boolean inHidden;
		
		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			if (ELEMENT_TOC_ORDER.equals(name)) {
				inTocOrder = true;
			}
			else if (ELEMENT_HIDDEN.equals(name)) {
				inHidden = true;
			}
			else if (ELEMENT_TOC.equals(name) || ELEMENT_CATEGORY.equals(name)) {
				String id = attributes.getValue(ATTRIBUTE_ID);
				if (id != null) {
					if (inTocOrder) {
						tocOrder.add(id);
					}
					else if (inHidden) {
						hiddenTocs.add(id);
					}
				}
			}
			else if (ELEMENT_INDEX.equals(name) && inHidden) {
				String id = attributes.getValue(ATTRIBUTE_ID);
				if (id != null) {
					hiddenIndexes.add(id);
				}
			}
		}
		
		/* (non-Javadoc)
		 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
		 */
		public void endElement(String uri, String localName, String name) throws SAXException {
			if (ELEMENT_TOC_ORDER.equals(name)) {
				inTocOrder = false;
			}
			else if (ELEMENT_HIDDEN.equals(name)) {
				inHidden = false;
			}
		}
		
		/*
		 * Note: throws clause does not declare IOException due to a bug in
		 * sun jdk: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6327149
		 * 
		 * @see org.xml.sax.helpers.DefaultHandler#resolveEntity(java.lang.String, java.lang.String)
		 */
		public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
			return new InputSource(new StringReader("")); //$NON-NLS-1$
		}
	}
}

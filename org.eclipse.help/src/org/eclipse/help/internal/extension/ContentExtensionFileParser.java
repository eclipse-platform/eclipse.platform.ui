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
package org.eclipse.help.internal.extension;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.help.IContentExtension;
import org.eclipse.help.internal.HelpPlugin;
import org.osgi.framework.Bundle;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/*
 * Parses content extension XML files into extension model elements.
 */
public class ContentExtensionFileParser extends DefaultHandler {

	private static final String ELEMENT_TOPIC_EXTENSION = "topicExtension"; //$NON-NLS-1$
	private static final String ELEMENT_TOPIC_REPLACE = "topicReplace"; //$NON-NLS-1$
	private static final String ATTRIBUTE_CONTENT = "content"; //$NON-NLS-1$
	private static final String ATTRIBUTE_PATH = "path"; //$NON-NLS-1$
	
	private SAXParser parser;
	private String bundleId;
	private String path;
	private List extensions;

	/*
	 * Parses the file at the specified path in the bundle and returns
	 * model elements describing the extensions contained within.
	 */
	public IContentExtension[] parse(Bundle bundle, String path) throws IOException, SAXException, ParserConfigurationException {
		this.path = path;
		if (extensions == null) {
			extensions = new ArrayList();
		}
		else {
			extensions.clear();
		}
		URL url = bundle.getEntry(path);
		if (url != null) {
			bundleId = bundle.getSymbolicName();
			InputStream in = url.openStream();
			try {
				if (parser == null) {
					parser = SAXParserFactory.newInstance().newSAXParser();
				}
				parser.parse(in, this);
			}
			finally {
				try {
					in.close();
				}
				catch (IOException e) {}
			}
		}
		return (IContentExtension[])extensions.toArray(new IContentExtension[extensions.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		int type = -1;
		if (ELEMENT_TOPIC_EXTENSION.equals(qName)) {
			type = IContentExtension.CONTRIBUTION;
		}
		else if (ELEMENT_TOPIC_REPLACE.equals(qName)) {
			type = IContentExtension.REPLACE;
		}		
		if (type != -1) {
			String content = attributes.getValue(ATTRIBUTE_CONTENT);
			if (content != null) {
				content = '/' + bundleId +  normalizePath(content);
				String path = attributes.getValue(ATTRIBUTE_PATH);
				if (path != null) {
					extensions.add(new ContentExtension(content, normalizePath(path), type));
				}
				else {
					logError(ATTRIBUTE_PATH, qName);
				}
			}
			else {
				logError(ATTRIBUTE_CONTENT, qName);
			}
		}
	}
	
	/*
	 * Normalizes the given path by adding a leading slash if one doesn't
	 * exist, and converting the final slash into a '#' if it is thought to
	 * separate the end of the document with the element (legacy form).
	 */
	private String normalizePath(String path) {
		int bundleStart, bundleEnd;
		int pathStart, pathEnd;
		int elementStart, elementEnd;
		
		bundleStart = path.charAt(0) == '/' ? 1 : 0;
		bundleEnd = path.indexOf('/', bundleStart);
		
		pathStart = bundleEnd + 1;
		pathEnd = path.indexOf('#', pathStart);
		if (pathEnd == -1) {
			int lastSlash = path.lastIndexOf('/');
			if (lastSlash > 0) {
				int secondLastSlash = path.lastIndexOf('/', lastSlash - 1);
				if (secondLastSlash != -1) {
					String secondLastToken = path.substring(secondLastSlash, lastSlash);
					boolean hasDot = (secondLastToken.indexOf('.') != -1);
					if (hasDot) {
						pathEnd = lastSlash;
					}
				}
			}
			if (pathEnd == -1) {
				pathEnd = path.length();
			}
		}
		
		elementStart = Math.min(pathEnd + 1, path.length());
		elementEnd = path.length();
		
		if (bundleEnd > bundleStart && pathStart > bundleEnd && pathEnd > pathStart && elementStart >= pathEnd && elementEnd >= elementStart) {
			String bundleId = path.substring(bundleStart, bundleEnd);
			String relativePath = path.substring(pathStart, pathEnd);
			String elementId = path.substring(elementStart, elementEnd);
			path = '/' + bundleId + '/' + relativePath;
			if (elementId.length() > 0) {
				path += '#' + elementId;
			}
		}
		return path;
	}
	
	/*
	 * Logs an error about a missing attribute.
	 */
	private void logError(String attribute, String element) {
		String msg = "Required attribute " + attribute + " missing from element " + element + " in /" + bundleId + "/" + path; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		HelpPlugin.logError(msg, null);
	}
}

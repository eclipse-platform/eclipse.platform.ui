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
package org.eclipse.help.internal.xhtml;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.runtime.content.IContentDescriber;
import org.eclipse.help.internal.dynamic.DOMProcessorHandler;
import org.eclipse.help.internal.dynamic.ExtensionHandler;
import org.eclipse.help.internal.dynamic.FilterHandler;
import org.eclipse.help.internal.dynamic.IncludeHandler;
import org.eclipse.help.internal.dynamic.XMLProcessor;
import org.xml.sax.SAXException;

/*
 * Performs any needed XHTML processing on the given input stream. If the input
 * is not XHTML, simply forwards the stream.
 */
public class DynamicXHTMLProcessor {

	private static IContentDescriber xhtmlDescriber;
	private static XMLProcessor xmlProcessor;
	private static XMLProcessor xmlProcessorNoFilter;

	/*
	 * Performs any needed processing. Does nothing if not XHTML.
	 */
	public static InputStream process(String href, InputStream in, String locale, boolean filter) throws IOException, SAXException, ParserConfigurationException, TransformerConfigurationException, TransformerException {
		BufferedInputStream buf = new BufferedInputStream(in, XHTMLContentDescriber.BUFFER_SIZE);
		buf.mark(XHTMLContentDescriber.BUFFER_SIZE);
		boolean isXHTML = isXHTML(buf);
		buf.reset();
		if (isXHTML) {
			if (filter) {
				if (xmlProcessor == null) {
					xmlProcessor = new XMLProcessor(new DOMProcessorHandler[] {
							new IncludeHandler(locale),
							new ExtensionHandler(locale),
							new XHTMLCharsetHandler(),
							new FilterHandler()
					});
				}
				return xmlProcessor.process(buf, href);
			}
			if (xmlProcessorNoFilter == null) {
				xmlProcessorNoFilter = new XMLProcessor(new DOMProcessorHandler[] {
						new IncludeHandler(locale),
						new ExtensionHandler(locale),
						new XHTMLCharsetHandler()
				});
			}
			return xmlProcessorNoFilter.process(buf, href);
		}
		return buf;
	}

	/*
	 * Returns whether or not the given input stream is XHTML content.
	 */
	private static synchronized boolean isXHTML(InputStream in) {
		if (xhtmlDescriber == null) {
			xhtmlDescriber = new XHTMLContentDescriber();
		}
		if (in != null) {
			try {
				return (xhtmlDescriber.describe(in, null) == IContentDescriber.VALID);
			}
			catch (IOException e) {
			}
		}
		return false;
	}
}

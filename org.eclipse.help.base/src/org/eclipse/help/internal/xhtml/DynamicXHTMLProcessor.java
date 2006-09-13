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

import org.eclipse.core.runtime.content.IContentDescriber;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.w3c.dom.Document;

/*
 * Performs any needed XHTML processing on the given input stream. If the input
 * is not XHTML, simply forwards the stream.
 */
public class DynamicXHTMLProcessor {

	private static IContentDescriber xhtmlDescriber;

	/*
	 * Performs any needed processing. Does nothing if not XHTML.
	 */
	public static InputStream process(String pluginID, String file, InputStream in, String locale, boolean filter) {
		BufferedInputStream buf = new BufferedInputStream(in, XHTMLContentDescriber.BUFFER_SIZE);
		buf.mark(XHTMLContentDescriber.BUFFER_SIZE);
		boolean isXHTML = isXHTML(buf);
		try {
			buf.reset();
			if (isXHTML) {
				return processXHTML(pluginID, file, buf, locale, filter);
			}
		}
		catch (IOException e) {
			String msg = ""; //$NON-NLS-1$
			HelpBasePlugin.logError(msg, e);
		}
		return buf;
	}
	
	/*
	 * Processes the given XHTML input stream.
	 */
	private static InputStream processXHTML(String pluginID, String file, InputStream in, String locale, boolean filter) {
		UAContentParser parser = new UAContentParser(in);
		Document dom = parser.getDocument();
		String href = '/' + pluginID + '/' + file;
		XHTMLSupport support = new XHTMLSupport(dom, href);
		dom = support.processDOM(filter);
		try {
			in.close();
		} catch (IOException e) {
		}
		return UATransformManager.getAsInputStream(dom);
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

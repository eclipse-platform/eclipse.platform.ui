/***************************************************************************************************
 * Copyright (c) 2006 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/

package org.eclipse.help.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentDescriber;
import org.eclipse.help.IHelpContentProducer;
import org.eclipse.help.internal.util.ResourceLocator;
import org.eclipse.help.internal.xhtml.UAContentParser;
import org.eclipse.help.internal.xhtml.UATransformManager;
import org.eclipse.help.internal.xhtml.XHTMLContentDescriber;
import org.eclipse.help.internal.xhtml.XHTMLSupport;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;

/**
 * The entry for all the dynamic format support. Depending on the file extension, this content
 * producer delegates to content producers that can handle one format.
 */

public class DynamicContentProducer implements IHelpContentProducer {

	private IContentDescriber xhtmlDescriber;

	public InputStream getInputStream(String pluginID, String href, Locale locale) {
		if (isXHTML(pluginID, href, locale)) {
			String file = href;
			int qloc = href.indexOf('?');
			if (qloc != -1) {
				file = href.substring(0, qloc);
			}

			/*
			 * Filtering can be turned off when, for example,
			 * indexing documents.
			 */
			boolean filter = true;
			if (qloc != -1 && qloc < href.length() - 1) {
				String query = href.substring(qloc + 1);
				filter = (query.indexOf("filter=false") == -1); //$NON-NLS-1$
			}
			return openXHTMLFromPlugin(pluginID, file, locale.toString(), filter);
		}
		return null;
	}

	/**
	 * Returns whether or not the given href is pointing to an XHTML
	 * document (it can be within a .html file).
	 * 
	 * @param pluginID the id of the plugin containing the document
	 * @param href the href to the document
	 * @param locale the document's locale
	 * @return whether or not the document is XHTML
	 */
	private boolean isXHTML(String pluginID, String href, Locale locale) {
		String file = href;
		int qloc = href.indexOf('?');
		if (qloc != -1) {
			file = href.substring(0, qloc);
		}
		if (xhtmlDescriber == null) {
			xhtmlDescriber = new XHTMLContentDescriber();
		}
		// first open it to peek inside to see whether it's really XHTML
		InputStream in = null;
		try {
			in = openXHTMLFromPluginRaw(pluginID, file, locale.toString());
			return (xhtmlDescriber.describe(in, null) == IContentDescriber.VALID);
		}
		catch (Exception e) {
			HelpPlugin.logError("An error occured in DynamicContentProducer while trying to determine the content type", e); //$NON-NLS-1$
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (IOException e) {
					// nothing we can do
				}
			}
		}
		return false;
	}
	
	/**
	 * Opens an input stream to an xhtml file contained in a plugin or in a doc.zip
	 * in the plugin. This includes includes OS, WS and NL lookup.
	 * 
	 * @param pluginDesc
	 *            the plugin description of the plugin that contains the file you are trying to find
	 * @param file
	 *            the relative path of the file to find
	 * @param locale
	 *            the locale used as an override or <code>null</code> to use the default locale
	 * @param filter
	 *            whether or not the content should be filtered
	 * 
	 * @return an InputStream to the file or <code>null</code> if the file wasn't found
	 */
	private static InputStream openXHTMLFromPlugin(String pluginID, String file, String locale, boolean filter) {
		InputStream inputStream = openXHTMLFromPluginRaw(pluginID, file, locale);
		if (inputStream != null) {
			UAContentParser parser = new UAContentParser(inputStream);
			Document dom = parser.getDocument();
			XHTMLSupport support = new XHTMLSupport(pluginID, file, dom, locale);
			dom = support.processDOM(filter);
			try {
				inputStream.close();
			} catch (IOException e) {
			}
			return UATransformManager.getAsInputStream(dom);
		}
		return null;
	}

	/**
	 * Same as openXHTMLFromPlugin() but does not do any processing of the document
	 * (it is opened raw).
	 */
	private static InputStream openXHTMLFromPluginRaw(String pluginID, String file, String locale) {
		Bundle bundle = Platform.getBundle(pluginID);
		if (bundle != null) {
			// look in the doc.zip and in the plugin to find the xhtml file.
			InputStream inputStream = ResourceLocator.openFromZip(bundle, "doc.zip", //$NON-NLS-1$
						file, locale);
			if (inputStream == null) {
				inputStream = ResourceLocator.openFromPlugin(bundle, file, locale);
			}
			return inputStream;
		}
		return null;
	}
}

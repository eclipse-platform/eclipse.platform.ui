/***************************************************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/

package org.eclipse.help.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.IHelpContentProducer;
import org.eclipse.help.internal.util.ResourceLocator;
import org.eclipse.help.internal.xhtml.UAContentParser;
import org.eclipse.help.internal.xhtml.UATransformManager;
import org.eclipse.help.internal.xhtml.XHTMLSupport;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;

/**
 * The entry for all the dynamic format support. Depending on the file extension, this content
 * producer delegates to content producers that can handle one format.
 */

public class DynamicContentProducer implements IHelpContentProducer {

	public InputStream getInputStream(String pluginID, String href, Locale locale) {
		int qloc = href.indexOf('?');
		if (qloc != -1)
			href = href.substring(0, qloc);
		int loc = href.lastIndexOf('.');
		if (loc != -1) {
			String extension = href.substring(loc + 1).toLowerCase();
			if ("xhtml".equals(extension))
				return openXHTMLFromPlugin(pluginID, href, locale.toString());
			// place support for other formats here
		}
		return null;
	}

	/**
	 * Opens an input stream to an xhtml file contained in a plugin. This includes includes OS, WS
	 * and NL lookup.
	 * 
	 * @param pluginDesc
	 *            the plugin description of the plugin that contains the file you are trying to find
	 * @param file
	 *            the relative path of the file to find
	 * @param locale
	 *            the locale used as an override or <code>null</code> to use the default locale
	 * 
	 * @return an InputStream to the file or <code>null</code> if the file wasn't found
	 */
	private InputStream openXHTMLFromPlugin(String pluginID, String file, String locale) {
		InputStream inputStream = openStreamFromPlugin(pluginID, file, locale);
		if (inputStream != null) {
			UAContentParser parser = new UAContentParser(inputStream);
			Document dom = parser.getDocument();
			try {
				inputStream.close();
			} catch (IOException e) {
			}
			dom = XHTMLSupport.processDOM(dom, locale);
			return UATransformManager.getAsInputStream(dom);
		}
		return null;
	}

	/**
	 * Opens the stream from a file relative to the plug-in and the provided locale.
	 * 
	 * @param pluginID
	 *            the unique plug-in ID
	 * @param file
	 *            the relative file name
	 * @param locale
	 *            the locale
	 * @return the input stream or <code>null</code> if the operation failed for some reason. The
	 *         caller is responsible for closing the stream.
	 */

	public static InputStream openStreamFromPlugin(String pluginID, String file, String locale) {
		ArrayList pathPrefix = ResourceLocator.getPathPrefix(locale);

		Bundle pluginDesc = Platform.getBundle(pluginID);

		URL flatFileURL = ResourceLocator.find(pluginDesc, new Path(file), pathPrefix);
		if (flatFileURL != null) {
			try {
				return flatFileURL.openStream();
			} catch (IOException e) {
				return null;
			}
		}
		return null;
	}
}

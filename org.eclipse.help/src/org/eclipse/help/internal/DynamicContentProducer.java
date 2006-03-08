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
		String file = href;
		int qloc = href.indexOf('?');
		if (qloc != -1)
			file = href.substring(0, qloc);
		int loc = file.lastIndexOf('.');
		if (loc != -1) {
			String extension = file.substring(loc + 1).toLowerCase();
			if ("xhtml".equals(extension)) {//$NON-NLS-1$
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
			// place support for other formats here
		}
		return null;
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
	private InputStream openXHTMLFromPlugin(String pluginID, String file, String locale, boolean filter) {
		Bundle bundle = Platform.getBundle(pluginID);
		if (bundle != null) {
			// look in the doc.zip and in the plugin to find the xhtml file.
			InputStream inputStream = ResourceLocator.openFromZip(bundle, "doc.zip", //$NON-NLS-1$
						file, locale);
			if (inputStream == null) {
				inputStream = ResourceLocator.openFromPlugin(bundle, file, locale);
			}
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
		}
		return null;
	}
}

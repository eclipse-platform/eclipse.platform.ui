/***************************************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/

package org.eclipse.help.internal.xhtml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.IHelpContentProducer;
import org.eclipse.help.internal.util.ResourceLocator;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;


public class XHTMLContentProducer implements IHelpContentProducer {

	public InputStream getInputStream(String pluginID, String href, Locale locale) {
		if (!href.endsWith("xhtml"))
			return null;
		return openXHTMLFromPlugin(pluginID, href, locale.toString());
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
		ArrayList pathPrefix = ResourceLocator.getPathPrefix(locale);

		Bundle pluginDesc = Platform.getBundle(pluginID);

		URL flatFileURL = ResourceLocator.find(pluginDesc, new Path(file), pathPrefix);
		if (flatFileURL != null)
			try {
				InputStream inputStream = flatFileURL.openStream();
				UAContentParser parser = new UAContentParser(inputStream);
				Document dom = parser.getDocument();
				dom = XHTMLSupport.processDOM(dom, locale);
				return UATransformManager.getAsInputStream(dom);
			} catch (IOException e) {
				return null;
			}
		return null;
	}


}

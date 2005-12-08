/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.content;

import javax.xml.parsers.SAXParserFactory;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * This class contains a set of OSGi-related helper methods for the Content plugin.
 * The closeServices() method should be called before the plugin is stopped. 
 * 
 * @since org.eclipse.core.contenttype 3.2
 */
public class ContentOSGiUtils {
	private ServiceTracker parserTracker = null;
	private ServiceTracker debugTracker = null;

	private static final ContentOSGiUtils singleton = new ContentOSGiUtils();

	public static ContentOSGiUtils getDefault() {
		return singleton;
	}

	/**
	 * Private constructor to block instance creation.
	 */
	private ContentOSGiUtils() {
		super();
		initServices();
	}

	private void initServices() {
		BundleContext context = Activator.getContext();
		if (context == null) {
			ContentMessages.message("ContentOSGiUtils called before plugin started"); //$NON-NLS-1$
			return;
		}

		parserTracker = new ServiceTracker(context, "javax.xml.parsers.SAXParserFactory", null); //$NON-NLS-1$
		parserTracker.open();

		debugTracker = new ServiceTracker(context, DebugOptions.class.getName(), null);
		debugTracker.open();
	}

	void closeServices() {
		if (parserTracker != null) {
			parserTracker.close();
			parserTracker = null;
		}
		if (debugTracker != null) {
			debugTracker.close();
			debugTracker = null;
		}
	}

	public SAXParserFactory getFactory() {
		if (parserTracker == null) {
			ContentMessages.message("SAX tracker is not set"); //$NON-NLS-1$
			return null;
		}
		SAXParserFactory theFactory = (SAXParserFactory) parserTracker.getService();
		if (theFactory != null)
			theFactory.setNamespaceAware(true);
		return theFactory;
	}

	public boolean getBooleanDebugOption(String option, boolean defaultValue) {
		if (debugTracker == null) {
			ContentMessages.message("Debug tracker is not set"); //$NON-NLS-1$
			return defaultValue;
		}
		DebugOptions options = (DebugOptions) debugTracker.getService();
		if (options != null) {
			String value = options.getOption(option);
			if (value != null)
				return value.equalsIgnoreCase("true"); //$NON-NLS-1$
		}
		return defaultValue;
	}

}

/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.runtime;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import org.eclipse.osgi.service.localization.BundleLocalization;
import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.*;

public class ResourceTranslator {
	private static final String KEY_PREFIX = "%"; //$NON-NLS-1$
	private static final String KEY_DOUBLE_PREFIX = "%%"; //$NON-NLS-1$	
	private static ServiceReference localizationServiceReference;
	private static BundleLocalization localizationService;

	public static String getResourceString(Bundle bundle, String value) {
		return getResourceString(bundle, value, null);
	}

	public static String getResourceString(Bundle bundle, String value, ResourceBundle resourceBundle) {
		String s = value.trim();
		if (!s.startsWith(KEY_PREFIX, 0))
			return s;
		if (s.startsWith(KEY_DOUBLE_PREFIX, 0))
			return s.substring(1);

		int ix = s.indexOf(' ');
		String key = ix == -1 ? s : s.substring(0, ix);
		String dflt = ix == -1 ? s : s.substring(ix + 1);

		if (resourceBundle == null && bundle != null) {
			try {
				resourceBundle = getResourceBundle(bundle);
			} catch (MissingResourceException e) {
				// just return the default (dflt)
			}
		}

		if (resourceBundle == null)
			return dflt;

		try {
			return resourceBundle.getString(key.substring(1));
		} catch (MissingResourceException e) {
			//this will avoid requiring a bundle access on the next lookup
			return dflt;
		}
	}

	public static void start() {
		BundleContext context = InternalPlatform.getDefault().getBundleContext();
		localizationServiceReference = InternalPlatform.getDefault().getBundleContext().getServiceReference(BundleLocalization.class.getName());
		if (localizationServiceReference == null)
			return;
		localizationService = (BundleLocalization) context.getService(localizationServiceReference);
	}

	public static void stop() {
		if (localizationServiceReference == null)
			return;
		localizationService = null;
		InternalPlatform.getDefault().getBundleContext().ungetService(localizationServiceReference);
		localizationServiceReference = null;
	}

	public static ResourceBundle getResourceBundle(Bundle bundle) throws MissingResourceException {
		if (hasRuntime21(bundle))
			return ResourceBundle.getBundle("plugin", Locale.getDefault(), createTempClassloader(bundle)); //$NON-NLS-1$
		return localizationService.getLocalization(bundle, null);
	}

	private static boolean hasRuntime21(Bundle b) {
		try {
			ManifestElement[] prereqs = ManifestElement.parseHeader(Constants.REQUIRE_BUNDLE, (String) b.getHeaders("").get(Constants.REQUIRE_BUNDLE)); //$NON-NLS-1$
			if (prereqs == null)
				return false;
			for (int i = 0; i < prereqs.length; i++) {
				if ("2.1".equals(prereqs[i].getAttribute(Constants.BUNDLE_VERSION_ATTRIBUTE)) && "org.eclipse.core.runtime".equals(prereqs[i].getValue())) { //$NON-NLS-1$//$NON-NLS-2$
					return true;
				}
			}
		} catch (BundleException e) {
			return false;
		}
		return false;
	}

	private static ClassLoader createTempClassloader(Bundle b) {
		ArrayList classpath = new ArrayList();
		addClasspathEntries(b, classpath);
		addBundleRoot(b, classpath);
		addDevEntries(b, classpath);
		addFragments(b, classpath);
		URL[] urls = new URL[classpath.size()];
		return new URLClassLoader((URL[]) classpath.toArray(urls));
	}

	private static void addFragments(Bundle host, ArrayList classpath) {
		Bundle[] fragments = InternalPlatform.getDefault().getFragments(host);
		if (fragments == null)
			return;

		for (int i = 0; i < fragments.length; i++) {
			addClasspathEntries(fragments[i], classpath);
			addDevEntries(fragments[i], classpath);
		}
	}

	private static void addClasspathEntries(Bundle b, ArrayList classpath) {
		ManifestElement[] classpathElements;
		try {
			classpathElements = ManifestElement.parseHeader(Constants.BUNDLE_CLASSPATH, (String) b.getHeaders("").get(Constants.BUNDLE_CLASSPATH)); //$NON-NLS-1$
			if (classpathElements == null)
				return;
			for (int i = 0; i < classpathElements.length; i++) {
				URL classpathEntry = b.getEntry(classpathElements[i].getValue());
				if (classpathEntry != null)
					classpath.add(classpathEntry);
			}
		} catch (BundleException e) {
			//ignore
		}
	}

	private static void addBundleRoot(Bundle b, ArrayList classpath) {
		classpath.add(b.getEntry("/")); //$NON-NLS-1$
	}

	private static void addDevEntries(Bundle b, ArrayList classpath) {
		if (!DevClassPathHelper.inDevelopmentMode())
			return;

		String[] binaryPaths = DevClassPathHelper.getDevClassPath(b.getSymbolicName());
		for (int i = 0; i < binaryPaths.length; i++) {
			URL classpathEntry = b.getEntry(binaryPaths[i]);
			if (classpathEntry != null)
				classpath.add(classpathEntry);
		}
	}
}
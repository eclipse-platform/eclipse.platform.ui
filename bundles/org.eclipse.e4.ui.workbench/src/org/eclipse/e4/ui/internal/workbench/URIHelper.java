/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.spi.RegistryContributor;
import org.osgi.framework.Bundle;
import org.osgi.service.log.LogService;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * Collection of URI-related utilities
 */
public class URIHelper {

	/**
	 * The schema identifier used for Eclipse platform references
	 */
	final private static String PLATFORM_SCHEMA = "platform:/"; //$NON-NLS-1$

	/**
	 * The schema identifier used for EMF platform references
	 */
	final private static String PLATFORM_SCHEMA_EMF = "platform"; //$NON-NLS-1$

	/**
	 * The segment used to specify location in a plugin
	 */
	final private static String PLUGIN_SEGMENT = "plugin/"; //$NON-NLS-1$

	/**
	 * The segment used to specify location in a fragment
	 */
	final private static String FRAGMENT_SEGMENT = "fragment/"; //$NON-NLS-1$

	static public String constructPlatformURI(Bundle bundle) {
		PackageAdmin packageAdmin = Activator.getDefault().getBundleAdmin();
		if (packageAdmin == null)
			return null;
		StringBuffer tmp = new StringBuffer();
		tmp.append(PLATFORM_SCHEMA);
		if ((packageAdmin.getBundleType(bundle) & PackageAdmin.BUNDLE_TYPE_FRAGMENT) > 0)
			tmp.append(FRAGMENT_SEGMENT);
		else
			tmp.append(PLUGIN_SEGMENT);
		tmp.append(bundle.getSymbolicName());
		return tmp.toString();
	}

	static public String constructPlatformURI(IContributor contributor) {
		// registry contributors are singletons
		String bundleName;
		if (contributor instanceof RegistryContributor)
			bundleName = ((RegistryContributor) contributor).getActualName();
		else
			// should not happen for the standard registry, but try to make a best guess
			bundleName = contributor.getName();
		Bundle bundle = Activator.getDefault().getBundleForName(bundleName);
		return constructPlatformURI(bundle);
	}

	static public Bundle getBundle(String contributorURI) {
		URI uri;
		try {
			uri = new URI(contributorURI);
		} catch (URISyntaxException e) {
			Activator.log(LogService.LOG_ERROR, "Invalid contributor URI: " + contributorURI); //$NON-NLS-1$
			return null;
		}
		if (!PLATFORM_SCHEMA.equals(uri.getScheme()))
			return null; // not implemented
		return Activator.getDefault().getBundleForName(uri.getPath());
	}

	static public String EMFtoPlatform(org.eclipse.emf.common.util.URI uri) {
		if (!PLATFORM_SCHEMA_EMF.equals(uri.scheme()))
			return null;
		// remove all segments but first two - only need bundle/fragment name
		int segments = uri.segmentCount();
		// segments: { "plugin", "org.eclipse.platform", "myDir", "model.e4xmi" }
		if (segments > 2)
			uri = uri.trimSegments(segments - 2);
		return uri.toString();
	}

}

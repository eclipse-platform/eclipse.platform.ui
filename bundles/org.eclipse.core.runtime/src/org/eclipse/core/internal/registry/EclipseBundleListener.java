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
package org.eclipse.core.internal.registry;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.core.internal.runtime.*;
import org.eclipse.core.runtime.*;
import org.osgi.framework.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A listener for bundle events.  When a bundles come and go we look to see 
 * if there are any extensions or extension points and update the registry accordingly.
 * Using a Synchronous listener here is important. If the
 * bundle activator code tries to access the registry to get its extension
 * points, we need to ensure that they are in the registry before the
 * bundle start is called. By listening sync we are able to ensure that
 * happens.
 */
public class EclipseBundleListener implements SynchronousBundleListener {
	private static final String PLUGIN_MANIFEST = "plugin.xml"; //$NON-NLS-1$
	private static final String FRAGMENT_MANIFEST = "fragment.xml"; //$NON-NLS-1$	

	private ExtensionRegistry registry;
	private ExtensionsParser parser;

	public EclipseBundleListener(ExtensionRegistry registry) {
		this.registry = registry;
		this.parser = new ExtensionsParser();
	}

	public void bundleChanged(BundleEvent event) {
		/* Only should listen for RESOLVED and UNRESOLVED events.  
		 * 
		 * When a bundle is updated the Framework will publish an UNRESOLVED and 
		 * then a RESOLVED event which should cause the bundle to be removed 
		 * and then added back into the registry.  
		 * 
		 * When a bundle is uninstalled the Framework should publish an UNRESOLVED 
		 * event and then an UNINSTALLED event so the bundle will have been removed 
		 * by the UNRESOLVED event before the UNINSTALLED event is published.
		 * 
		 * When a bundle is refreshed from PackageAdmin an UNRESOLVED event will be
		 * published which will remove the bundle from the registry.  If the bundle
		 * can be RESOLVED after a refresh then a RESOLVED event will be published 
		 * which will add the bundle back.  This is required because the classloader
		 * will have been refreshed for the bundle so all extensions and extension
		 * points for the bundle must be refreshed.
		 */
		Bundle bundle = event.getBundle();
		switch (event.getType()) {
			case BundleEvent.RESOLVED :
				addBundle(bundle);
				break;
			case BundleEvent.UNRESOLVED :
				removeBundle(bundle);
				break;
		}
	}

	public void processBundles(Bundle[] bundles) {
		for (int i = 0; i < bundles.length; i++) {
			if (isBundleResolved(bundles[i]))
				addBundle(bundles[i]);
			else
				removeBundle(bundles[i]);
		}
	}

	private boolean isBundleResolved(Bundle bundle) {
		return (bundle.getState() & (Bundle.RESOLVED | Bundle.ACTIVE | Bundle.STARTING | Bundle.STOPPING)) != 0;
	}

	private void removeBundle(Bundle bundle) {
		registry.remove(bundle.getSymbolicName(), bundle.getBundleId());
	}

	private void addBundle(Bundle bundle) {
		// if the given bundle already exists in the registry then return.
		// note that this does not work for update cases.
		if (registry.getNamespace(bundle.getSymbolicName()) != null)
			return;
		Namespace bundleModel = getBundleModel(bundle);
		if (bundleModel == null)
			return;
		// Do not synchronize on registry here because the registry handles
		// the synchronization for us in registry.add
		registry.add(bundleModel);
	}

	/**
	 * Tries to create a bundle model from a plugin/fragment manifest in the
	 * bundle.
	 */
	private Namespace getBundleModel(Bundle bundle) {
		// bail out if system bundle
		if (bundle.getBundleId() == 0)
			return null;
		// bail out if the bundle does not have a symbolic name
		if (bundle.getSymbolicName() == null)
			return null;
		InputStream is = null;
		String manifestType = null;
		boolean isFragment = InternalPlatform.getDefault().isFragment(bundle);
		String manifestName = isFragment ? FRAGMENT_MANIFEST : PLUGIN_MANIFEST;
		try {
			URL url = bundle.getEntry(manifestName);
			if (url != null) {
				is = url.openStream();
				manifestType = isFragment ? ExtensionsParser.FRAGMENT : ExtensionsParser.PLUGIN;
			}
		} catch (IOException ex) {
			is = null;
		}
		if (is == null)
			return null;
		try {
			String message = Policy.bind("parse.problems", bundle.getLocation()); //$NON-NLS-1$
			MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, ExtensionsParser.PARSE_PROBLEM, message, null); //$NON-NLS-1$
			ResourceBundle b = null;
			try {
				b = ResourceTranslator.getResourceBundle(bundle);
			} catch (MissingResourceException e) {
				//Ignore the exception
			}
			Namespace bundleModel = parser.parseManifest(problems, new InputSource(is), manifestType, manifestName, b);
			bundleModel.setUniqueIdentifier(bundle.getSymbolicName());
			bundleModel.setBundle(bundle);
			if (isFragment) {
				Bundle[] hosts = InternalPlatform.getDefault().getHosts(bundle);
				if (hosts != null && hosts.length > 0)
					bundleModel.setHostIdentifier(hosts[0].getSymbolicName());
			}
			if (problems.getSeverity() != IStatus.OK)
				InternalPlatform.getDefault().log(problems);
			return bundleModel;
		} catch (ParserConfigurationException e) {
			logParsingError(bundle, e);
			return null;
		} catch (SAXException e) {
			logParsingError(bundle, e);
			return null;
		} catch (IOException e) {
			logParsingError(bundle, e);
			return null;
		} finally {
			try {
				is.close();
			} catch (IOException ioe) {
				// nothing to do
			}
		}
	}

	private void logParsingError(Bundle bundle, Exception e) {
		String message = Policy.bind("parse.failedParsingManifest", bundle.getLocation()); //$NON-NLS-1$
		InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, 0, message, e));
	}

}
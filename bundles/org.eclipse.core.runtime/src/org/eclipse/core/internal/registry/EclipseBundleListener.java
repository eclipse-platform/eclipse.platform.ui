/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.registry;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.eclipse.core.internal.runtime.*;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.ManifestElement;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;
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
	private ServiceTracker xmlTracker;

	public EclipseBundleListener(ExtensionRegistry registry) {
		this.registry = registry;
		xmlTracker = new ServiceTracker(InternalPlatform.getDefault().getBundleContext(), SAXParserFactory.class.getName(), null);
		xmlTracker.open();
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
		registry.remove(bundle.getBundleId());
	}

	private void addBundle(Bundle bundle) {
		// if the given bundle already exists in the registry then return.
		// note that this does not work for update cases.
		if (registry.hasNamespace(bundle.getBundleId()))
			return;
		Contribution bundleModel = getBundleModel(bundle);
		if (bundleModel == null)
			return;
		// bug 70941
		// need to ensure we can find resource bundles from fragments 
		if (Platform.PI_RUNTIME.equals(bundleModel.getNamespace()))
			Messages.reloadMessages();
		// Do not synchronize on registry here because the registry handles
		// the synchronization for us in registry.add		
		registry.add(bundleModel);
	}

	private boolean isSingleton(Bundle bundle) {
		Dictionary allHeaders = bundle.getHeaders(""); //$NON-NLS-1$
		String symbolicNameHeader = (String) allHeaders.get(Constants.BUNDLE_SYMBOLICNAME); //$NON-NLS-1$
		try {
			if (symbolicNameHeader != null) {
				ManifestElement[] symbolicNameElements = ManifestElement.parseHeader(Constants.BUNDLE_SYMBOLICNAME, symbolicNameHeader);
				if (symbolicNameElements.length > 0) {
					String singleton = symbolicNameElements[0].getDirective(Constants.SINGLETON_DIRECTIVE);
					if (singleton == null)
						singleton = symbolicNameElements[0].getAttribute(Constants.SINGLETON_DIRECTIVE);

					if (!"true".equalsIgnoreCase(singleton)) { //$NON-NLS-1$
						int status = IStatus.INFO;
						String manifestVersion = (String) allHeaders.get(org.osgi.framework.Constants.BUNDLE_MANIFESTVERSION);
						if (manifestVersion == null) {//the header was not defined for previous versions of the bundle
							//3.0 bundles without a singleton attributes are still being accepted
							if (InternalPlatform.getDefault().getBundle(symbolicNameElements[0].getValue()) == bundle) {
								return true;
							}
							status = IStatus.ERROR;
						}
						if (InternalPlatform.DEBUG_REGISTRY || status == IStatus.ERROR) {
							String message = NLS.bind(Messages.parse_nonSingleton, bundle.getLocation());
							InternalPlatform.getDefault().log(new Status(status, Platform.PI_RUNTIME, 0, message, null));
						}
						return false;
					}
				}
			}
		} catch (BundleException e1) {
			//This can't happen because the fwk would have rejected the bundle
		}
		return true;
	}

	/**
	 * Tries to create a bundle model from a plugin/fragment manifest in the bundle.
	 */
	private Contribution getBundleModel(Bundle bundle) {
		// bail out if system bundle
		if (bundle.getBundleId() == 0)
			return null;
		// bail out if the bundle does not have a symbolic name
		if (bundle.getSymbolicName() == null)
			return null;

		//If the bundle is not a singleton, then it is not added
		if (!isSingleton(bundle))
			return null;

		boolean isFragment = InternalPlatform.getDefault().isFragment(bundle);

		//If the bundle is a fragment being added to a non singleton host, then it is not added
		if (isFragment) {
			Bundle[] hosts = InternalPlatform.getDefault().getHosts(bundle);
			if (hosts != null && isSingleton(hosts[0]) == false)
				return null;
		}

		InputStream is = null;
		String manifestType = null;
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
			String message = NLS.bind(Messages.parse_problems, bundle.getLocation());
			MultiStatus problems = new MultiStatus(Platform.PI_RUNTIME, ExtensionsParser.PARSE_PROBLEM, message, null); //$NON-NLS-1$
			ResourceBundle b = null;
			try {
				b = ResourceTranslator.getResourceBundle(bundle);
			} catch (MissingResourceException e) {
				//Ignore the exception
			}
			ExtensionsParser parser = new ExtensionsParser(problems);
			Contribution bundleModel = new Contribution(bundle);
			parser.parseManifest(xmlTracker, new InputSource(is), manifestType, manifestName, registry.getObjectManager(), bundleModel, b);
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
		String message = NLS.bind(Messages.parse_failedParsingManifest, bundle.getLocation());
		InternalPlatform.getDefault().log(new Status(IStatus.ERROR, Platform.PI_RUNTIME, 0, message, e));
	}
}

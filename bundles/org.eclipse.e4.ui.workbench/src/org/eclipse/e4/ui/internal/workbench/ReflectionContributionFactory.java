/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.contributions.IContributionFactorySpi;
import org.eclipse.emf.common.util.URI;
import org.osgi.framework.Bundle;
import org.osgi.service.log.LogService;

/**
 * Create the contribution factory.
 */
public class ReflectionContributionFactory implements IContributionFactory {

	private IExtensionRegistry registry;
	private Map<String, Object> languages;

	/**
	 * Create a reflection factory.
	 *
	 * @param registry
	 *            to read languages.
	 */
	public ReflectionContributionFactory(IExtensionRegistry registry) {
		this.registry = registry;
		processLanguages();
	}

	@Override
	public Object create(String uriString, IEclipseContext context, IEclipseContext staticContext) {
		return doCreate(uriString, context, staticContext);
	}

	@Override
	public Object create(String uriString, IEclipseContext context) {
		return doCreate(uriString, context, null);
	}

	private Object doCreate(String uriString, IEclipseContext context, IEclipseContext staticContext) {
		if (uriString == null) {
			return null;
		}
		// translate old-style platform:/plugin/ class specifiers into new-style bundleclass:// URIs
		if (uriString.startsWith("platform:/plugin/")) { //$NON-NLS-1$
			Activator.log(LogService.LOG_ERROR,
					"platform-style URIs deprecated for referencing types: " + uriString); //$NON-NLS-1$
			uriString = uriString.replace("platform:/plugin/", "bundleclass://"); //$NON-NLS-1$ //$NON-NLS-2$
			Activator.log(LogService.LOG_ERROR, "URI rewritten as: " + uriString); //$NON-NLS-1$
		}
		URI uri = URI.createURI(uriString);
		Bundle bundle = getBundle(uri);
		Object contribution;
		if (bundle != null) {
			contribution = createFromBundle(bundle, context, staticContext, uri);
		} else {
			contribution = null;
			Activator.log(LogService.LOG_ERROR, "Unable to retrieve the bundle from the URI: " //$NON-NLS-1$
					+ uriString);
		}
		return contribution;
	}

	protected Object createFromBundle(Bundle bundle, IEclipseContext context,
			IEclipseContext staticContext, URI uri) {
		Object contribution;
		if (uri.segmentCount() > 1) {
			String prefix = uri.segment(0);
			IContributionFactorySpi factory = (IContributionFactorySpi) languages.get(prefix);
			if (factory == null) {
				String message = "Unsupported contribution factory type '" + prefix + "'"; //$NON-NLS-1$ //$NON-NLS-2$
				Activator.log(LogService.LOG_ERROR, message);
				return null;
			}
			StringBuffer resource = new StringBuffer(uri.segment(1));
			for (int i = 2; i < uri.segmentCount(); i++) {
				resource.append('/');
				resource.append(uri.segment(i));
			}
			contribution = factory.create(bundle, resource.toString(), context);
		} else {
			String clazz = uri.segment(0);
			try {
				Class<?> targetClass = bundle.loadClass(clazz);
				if (staticContext == null)
					contribution = ContextInjectionFactory.make(targetClass, context);
				else
					contribution = ContextInjectionFactory
							.make(targetClass, context, staticContext);

				if (contribution == null) {
					String message = "Unable to load class '" + clazz + "' from bundle '" //$NON-NLS-1$ //$NON-NLS-2$
							+ bundle.getBundleId() + "'"; //$NON-NLS-1$
					Activator.log(LogService.LOG_ERROR, message, new Exception());
				}
			} catch (ClassNotFoundException e) {
				contribution = null;
				String message = "Unable to load class '" + clazz + "' from bundle '" //$NON-NLS-1$ //$NON-NLS-2$
						+ bundle.getBundleId() + "'"; //$NON-NLS-1$
				Activator.log(LogService.LOG_ERROR, message, e);
			} catch (InjectionException e) {
				contribution = null;
				String message = "Unable to create class '" + clazz + "' from bundle '" //$NON-NLS-1$ //$NON-NLS-2$
						+ bundle.getBundleId() + "'"; //$NON-NLS-1$
				Activator.log(LogService.LOG_ERROR, message, e);
			}
		}
		return contribution;
	}

	protected void processLanguages() {
		languages = new HashMap<String, Object>();
		String extId = "org.eclipse.e4.languages"; //$NON-NLS-1$
		IConfigurationElement[] languageElements = registry.getConfigurationElementsFor(extId);
		for (int i = 0; i < languageElements.length; i++) {
			IConfigurationElement languageElement = languageElements[i];
			try {
				languages.put(languageElement.getAttribute("name"), //$NON-NLS-1$
						languageElement.createExecutableExtension("contributionFactory")); //$NON-NLS-1$
			} catch (InvalidRegistryObjectException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected Bundle getBundle(URI platformURI) {
		if (platformURI.authority() == null) {
			Activator.log(LogService.LOG_ERROR, "Failed to get bundle for: " + platformURI); //$NON-NLS-1$
			return null;
		}
		return Activator.getDefault().getBundleForName(platformURI.authority());
	}

	@Override
	public Bundle getBundle(String uriString) {
		URI uri = URI.createURI(uriString);
		return getBundle(uri);
	}

}

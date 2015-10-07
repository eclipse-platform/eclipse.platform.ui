/*******************************************************************************
 * Copyright (c) 2014 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <scholzsimon@vogella.com> - Bug 445663
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 445663
 *******************************************************************************/
package org.eclipse.ui.internal.ide.application.addons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.internal.workbench.URIHelper;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.UILifeCycle;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.namespace.HostNamespace;
import org.osgi.framework.namespace.IdentityNamespace;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.framework.wiring.FrameworkWiring;
import org.osgi.resource.Namespace;
import org.osgi.resource.Requirement;
import org.osgi.resource.Resource;
import org.osgi.service.event.Event;

/**
 * The model-addon searches for model contributions in the runtime application
 * and removes elements for which the classes cannot be accessed anymore.
 * Currently it only covered part descriptors but it is planned to extend this
 * addon to also remove other broken model contributions
 */
public class ModelCleanupAddon {

	private static String COMPATIBILITY_EDITOR_URI = "bundleclass://org.eclipse.ui.workbench/org.eclipse.ui.internal.e4.compatibility.CompatibilityEditor"; //$NON-NLS-1$
	private static String COMPATIBILITY_VIEW_URI = "bundleclass://org.eclipse.ui.workbench/org.eclipse.ui.internal.e4.compatibility.CompatibilityView"; //$NON-NLS-1$

	@Inject
	@Optional
	private MApplication application;

	@Inject
	@Optional
	private Logger logger;

	/**
	 * This addon listens to the {@link UILifeCycle#APP_STARTUP_COMPLETE} event.
	 *
	 * @param event
	 *            {@link Event}
	 */
	@Inject
	@Optional
	public void applicationStartUp(@EventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) Event event) {
		List<MPartDescriptor> descriptors = application.getDescriptors();
		Bundle bundle = FrameworkUtil.getBundle(getClass());
		for (Iterator<MPartDescriptor> iterator = descriptors.iterator(); iterator.hasNext();) {
			MPartDescriptor partDescriptor = iterator.next();
			boolean validPartDescriptor = isValidPartDescriptor(bundle, partDescriptor);
			if (!validPartDescriptor) {
				logger.warn("Removing part descriptor with the '" + partDescriptor.getElementId() //$NON-NLS-1$
						+ "' id and the '" + partDescriptor.getLocalizedLabel() //$NON-NLS-1$
						+ "' description. Points to the invalid '" + partDescriptor.getContributionURI() + "' class."); //$NON-NLS-1$ //$NON-NLS-2$
				iterator.remove();
			}
		}
	}

	/**
	 * @param bundle
	 * @param iterator
	 */
	private boolean isValidPartDescriptor(Bundle bundle, MPartDescriptor partDescriptor) {
		String contributionURI = partDescriptor.getContributionURI();
		if (!(COMPATIBILITY_EDITOR_URI.equals(contributionURI)) && !(COMPATIBILITY_VIEW_URI.equals(contributionURI))) {
			if (!URIHelper.isBundleClassUri(contributionURI))
				return false;
		}
		String[] bundleClass = contributionURI.substring(14).split("/"); //$NON-NLS-1$
		String bundleSymbolicName = bundleClass[0];
		String className = bundleClass[1];

		Collection<BundleWiring> wirings = findWirings(bundleSymbolicName, bundle.getBundleContext());
		if (!isPartDescriptorClassAvailable(wirings, className)) {
			// remove PartDescriptor, if there is not wiring available
			// or if the class cannot be found
			return false;
		}

		return true;
	}

	private boolean isPartDescriptorClassAvailable(Collection<BundleWiring> wirings, String className) {
		if (wirings.isEmpty()) {
			return false;
		}

		for (BundleWiring bundleWiring : wirings) {
			Class<?> partsClass = findClass(className, bundleWiring);
			if (null == partsClass) {
				// class for PartDescriptor cannot be found
				return false;
			}
		}

		return true;
	}

	private Collection<BundleWiring> findWirings(final String bundleSymbolicName, BundleContext bundleContext) {
		Requirement req = new Requirement() {
			@Override
			public Resource getResource() {
				// no resource
				return null;
			}

			@Override
			public String getNamespace() {
				return IdentityNamespace.IDENTITY_NAMESPACE;
			}

			@Override
			public Map<String, String> getDirectives() {
				return Collections.singletonMap(Namespace.REQUIREMENT_FILTER_DIRECTIVE,
						"(" + IdentityNamespace.IDENTITY_NAMESPACE + "=" + bundleSymbolicName + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}

			@Override
			public Map<String, Object> getAttributes() {
				return Collections.emptyMap();
			}
		};
		Collection<BundleCapability> identities = bundleContext.getBundle(Constants.SYSTEM_BUNDLE_LOCATION)
				.adapt(FrameworkWiring.class).findProviders(req);
		Collection<BundleWiring> result = new ArrayList<BundleWiring>(1); // normally
																			// only
																			// one
		for (BundleCapability identity : identities) {
			BundleRevision revision = identity.getRevision();
			BundleWiring wiring = revision.getWiring();
			if (wiring != null) {
				if ((revision.getTypes() & BundleRevision.TYPE_FRAGMENT) != 0) {
					// fragment case; need to get the host wiring
					wiring = wiring.getRequiredWires(HostNamespace.HOST_NAMESPACE).get(0).getProviderWiring();
				}
				result.add(wiring);
			}
		}
		return result;
	}

	private Class<?> findClass(String className, BundleWiring wiring) {
		if (wiring == null) {
			return null;
		}
		if ((wiring.getRevision().getTypes() & BundleRevision.TYPE_FRAGMENT) != 0) {
			// fragment case; need to get the host wiring
			wiring = wiring.getRequiredWires(HostNamespace.HOST_NAMESPACE).get(0).getProviderWiring();
		}
		try {
			return wiring.getClassLoader().loadClass(className);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
}

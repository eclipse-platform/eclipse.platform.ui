/*******************************************************************************
 * Copyright (c) 2014, 2019 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simon Scholz <scholzsimon@vogella.com> - Bug 445663
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 445663
 *     Rolf Theunissen <rolf.theunissen@gmail.com> - Bug 527689
 *******************************************************************************/
package org.eclipse.ui.internal.ide.application.addons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.internal.workbench.URIHelper;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.UILifeCycle;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityEditor;
import org.eclipse.ui.internal.registry.ViewRegistry;
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

import jakarta.inject.Inject;

/**
 * The model-addon searches for model contributions in the runtime application
 * and removes elements for which the classes cannot be accessed anymore.
 * Currently it only covered part descriptors but it is planned to extend this
 * addon to also remove other broken model contributions
 */
@SuppressWarnings("restriction")
public class ModelCleanupAddon {

	/**
	 * See URIHelper#BUNDLECLASS_SCHEMA constant.
	 */
	private static final int BUNDLECLASS_SCHEMA_LENGTH = 14;
	private static final String COMPATIBILITY_EDITOR_URI = "bundleclass://org.eclipse.ui.workbench/org.eclipse.ui.internal.e4.compatibility.CompatibilityEditor"; //$NON-NLS-1$
	private static final String COMPATIBILITY_VIEW_URI = "bundleclass://org.eclipse.ui.workbench/org.eclipse.ui.internal.e4.compatibility.CompatibilityView"; //$NON-NLS-1$

	@Inject
	@Optional
	private MApplication application;

	@Inject
	@Optional
	private EModelService modelService;

	@Inject
	@Optional
	private Logger logger;

	private BundleContext bundleContext;

	/**
	 * This addon listens to the {@link UILifeCycle#APP_STARTUP_COMPLETE} event.
	 *
	 * @param event  {@link Event}
	 * @param app    {@link MApplication}
	 * @param uiSync {@link UISynchronize}
	 */
	@Inject
	@Optional
	public void applicationStartUp(@EventTopic(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE) Event event, MApplication app,
			UISynchronize uiSync) {

		Bundle bundle = FrameworkUtil.getBundle(getClass());
		bundleContext = bundle.getBundleContext();

		cleanUnavailablePartDescriptors(app, uiSync);
		cleanUnavailableHandlers(app, uiSync);

		cleanHiddenCompatibilityEditors();
	}

	private void cleanUnavailablePartDescriptors(MApplication app, UISynchronize uiSync) {
		// make copies of the lists for thread safety
		List<MPartDescriptor> descriptors = new ArrayList<>(app.getDescriptors());

		ExecutorService executor = Executors.newFixedThreadPool(1);

		CompletableFuture.supplyAsync(() -> getObsoletePartDescriptors(descriptors), executor)
				.thenAccept(d -> uiSync.asyncExec(() -> iteratorRemove(app.getDescriptors(), d)));
	}

	private void cleanUnavailableHandlers(MApplication app, UISynchronize uiSync) {
		// make copies of the lists for thread safety
		List<MHandler> handlers = new ArrayList<>(app.getHandlers());

		ExecutorService executor = Executors.newFixedThreadPool(1);

		CompletableFuture.supplyAsync(() -> getObsoleteHandlers(handlers), executor)
				.thenAccept(d -> uiSync.asyncExec(() -> iteratorRemove(app.getHandlers(), d)));
	}

	private List<MPartDescriptor> getObsoletePartDescriptors(List<MPartDescriptor> partDescriptors) {
		for (Iterator<MPartDescriptor> iterator = partDescriptors.iterator(); iterator.hasNext();) {
			MPartDescriptor appElement = iterator.next();
			boolean validAppElement = isValidPartDescriptor(appElement);
			if (validAppElement) {
				iterator.remove();
			} else {
				logMissingClassWarning(appElement);
			}
		}

		return partDescriptors;
	}

	// This method only removes invalid top-level handlers, in the future this might
	// be extended to remove
	// scope handlers, like a handler defined for a part
	private List<MHandler> getObsoleteHandlers(List<MHandler> handlers) {
		for (Iterator<MHandler> iterator = handlers.iterator(); iterator.hasNext();) {
			MHandler appElement = iterator.next();
			boolean validAppElement = isValidHandler(appElement);
			if (validAppElement) {
				iterator.remove();
			} else {
				logMissingClassWarning(appElement);
			}
		}

		return handlers;
	}

	private void iteratorRemove(List<?> list, List<?> elementsToBeRemoved) {
		if (elementsToBeRemoved.isEmpty()) {
			return;
		}

		for (Iterator<?> iterator = list.iterator(); iterator.hasNext();) {
			Object object = iterator.next();
			if (elementsToBeRemoved.contains(object)) {
				iterator.remove();
			}

		}
	}

	private void logMissingClassWarning(MApplicationElement appElement) {
		StringBuilder sb = new StringBuilder();
		sb.append("Removing "); //$NON-NLS-1$
		sb.append(appElement.getClass().getSimpleName());
		sb.append(" with the \""); //$NON-NLS-1$
		sb.append(appElement.getElementId());
		sb.append("\" id"); //$NON-NLS-1$
		if (appElement instanceof MUILabel) {
			sb.append(" and the \""); //$NON-NLS-1$
			sb.append(((MUILabel) appElement).getLocalizedLabel());
			sb.append("\" label"); //$NON-NLS-1$
		}
		sb.append("."); //$NON-NLS-1$
		sb.append("It points to the non available \""); //$NON-NLS-1$
		sb.append(getContributionUri(appElement));
		sb.append("\" class. Bundle might have been uninstalled"); //$NON-NLS-1$

		logger.warn(sb.toString());
	}

	private String getContributionUri(MApplicationElement appElement) {
		if (appElement instanceof MPartDescriptor) {
			return ((MPartDescriptor) appElement).getContributionURI();
		} else if (appElement instanceof MAddon) {
			return ((MAddon) appElement).getContributionURI();
		} else if (appElement instanceof MHandler) {
			return ((MHandler) appElement).getContributionURI();
		}
		return null;
	}
	/**
	 * Compatibility editors were not always removed when hidden, see Bug 527689.
	 * Clean up any Compatibility editor that is not to be rendered.
	 */
	private void cleanHiddenCompatibilityEditors() {
		List<MPart> compatEditors = modelService.findElements(application, CompatibilityEditor.MODEL_ELEMENT_ID,
				MPart.class);
		for (MPart editor : compatEditors) {
			if (!editor.isToBeRendered()) {
				editor.getParent().getChildren().remove(editor);
			}
		}
	}

	private boolean isValidPartDescriptor(MPartDescriptor partDescriptor) {
		String contributionURI = partDescriptor.getContributionURI();
		if (!URIHelper.isBundleClassUri(contributionURI)) {
			return false;
		}

		String originalCompatibilityViewClass = partDescriptor.getPersistedState()
				.get(ViewRegistry.ORIGINAL_COMPATIBILITY_VIEW_CLASS);
		// if the originalCompatibilityViewClass is not null, the given
		// MPartDescriptor is based on a ViewPart (not e4view)
		// See createDescriptor method of the ViewRegistry
		if (COMPATIBILITY_VIEW_URI.equals(contributionURI) && originalCompatibilityViewClass != null) {
			String originalCompatibilityViewBundle = partDescriptor.getPersistedState()
					.get(ViewRegistry.ORIGINAL_COMPATIBILITY_VIEW_BUNDLE);
			return checkPartDescriptorByBundleSymbolicNameAndClass(bundleContext, originalCompatibilityViewBundle,
					originalCompatibilityViewClass);
		} else if (!COMPATIBILITY_EDITOR_URI.equals(contributionURI)) {
			// check for e4views and usual MPartDescriptors
			String[] bundleClass = contributionURI.substring(BUNDLECLASS_SCHEMA_LENGTH).split("/"); //$NON-NLS-1$
			String bundleSymbolicName = bundleClass[0];
			String className = bundleClass[1];
			return checkPartDescriptorByBundleSymbolicNameAndClass(bundleContext, bundleSymbolicName, className);
		}

		return true;
	}

	private boolean isValidHandler(MHandler handler) {
		String contributionURI = handler.getContributionURI();
		if (!URIHelper.isBundleClassUri(contributionURI)) {
			return false;
		}

			// check for e4views and usual MPartDescriptors
			String[] bundleClass = contributionURI.substring(BUNDLECLASS_SCHEMA_LENGTH).split("/"); //$NON-NLS-1$
			String bundleSymbolicName = bundleClass[0];
			String className = bundleClass[1];
			return checkPartDescriptorByBundleSymbolicNameAndClass(bundleContext, bundleSymbolicName, className);

	}


	private boolean checkPartDescriptorByBundleSymbolicNameAndClass(BundleContext bundleContext,
			String bundleSymbolicName,
			String className) {
		Collection<BundleWiring> wirings = findWirings(bundleSymbolicName, bundleContext);
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

		String classPackageName;
		String classResourceName;
		int indexLastDot = className.lastIndexOf('.');
		if (indexLastDot < 0) {
			classPackageName = "/"; //$NON-NLS-1$
			classResourceName = className;
		} else {
			classPackageName = '/' + className.substring(0, indexLastDot).replace('.', '/');
			classResourceName = className.substring(indexLastDot + 1) + ".class"; //$NON-NLS-1$
		}
		for (BundleWiring bundleWiring : wirings) {
			if (!checkClassResource(classPackageName, classResourceName, bundleWiring)) {
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
		Collection<BundleWiring> result = new ArrayList<>(1); // normally
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

	private boolean checkClassResource(String classPackageName, String classFileName, BundleWiring wiring) {
		if (wiring == null) {
			return false;
		}
		if ((wiring.getRevision().getTypes() & BundleRevision.TYPE_FRAGMENT) != 0) {
			// fragment case; need to get the host wiring
			wiring = wiring.getRequiredWires(HostNamespace.HOST_NAMESPACE).get(0).getProviderWiring();
		}
		Collection<String> classResourcePaths = wiring.listResources(classPackageName, classFileName, 0);
		return classResourcePaths != null && !classResourcePaths.isEmpty();
	}
}

/*******************************************************************************
 * Copyright (c) 2009, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tristan Hume - <trishume@gmail.com> -
 *     		Fix for Bug 2369 [Workbench] Would like to be able to save workspace without exiting
 *     		Implemented workbench auto-save to correctly restore state in case of crash.
 *     Terry Parker <tparker@google.com> - Bug 416673
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 393171
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.e4.ui.workbench.IModelResourceHandler;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.framework.Bundle;

/**
 * This class is responsible to load and save the model
 */
public class ResourceHandler implements IModelResourceHandler {

	private ResourceSet resourceSet;
	private Resource resource;

	@Inject
	private Logger logger;

	@Inject
	private IEclipseContext context;

	@Inject
	@Named(E4Workbench.INITIAL_WORKBENCH_MODEL_URI)
	private URI applicationDefinitionInstance;

	@Inject
	@Optional
	@Named(E4Workbench.INSTANCE_LOCATION)
	private Location instanceLocation;

	@Inject
	@Optional
	@Named(IWorkbench.PERSIST_STATE)
	private boolean saveAndRestore;

	@Inject
	@Optional
	@Named(IWorkbench.CLEAR_PERSISTED_STATE)
	private boolean clearPersistedState;

	/**
	 * Constructor.
	 */
	@Inject
	public ResourceHandler(@Named(IWorkbench.PERSIST_STATE) boolean saveAndRestore,
			@Named(IWorkbench.CLEAR_PERSISTED_STATE) boolean clearPersistedState) {
		this.saveAndRestore = saveAndRestore;
		this.clearPersistedState = clearPersistedState;
	}

	@PostConstruct
	void init() {
		resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new E4XMIResourceFactory());
		resourceSet.getPackageRegistry().put(ApplicationPackageImpl.eNS_URI, ApplicationPackageImpl.eINSTANCE);
		resourceSet.getPackageRegistry().put(CommandsPackageImpl.eNS_URI, CommandsPackageImpl.eINSTANCE);
		resourceSet.getPackageRegistry().put(UiPackageImpl.eNS_URI, UiPackageImpl.eINSTANCE);
		resourceSet.getPackageRegistry().put(MenuPackageImpl.eNS_URI, MenuPackageImpl.eINSTANCE);
		resourceSet.getPackageRegistry().put(BasicPackageImpl.eNS_URI, BasicPackageImpl.eINSTANCE);
		resourceSet.getPackageRegistry().put(AdvancedPackageImpl.eNS_URI, AdvancedPackageImpl.eINSTANCE);
		resourceSet.getPackageRegistry().put(
				org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eNS_URI,
				org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eINSTANCE);

	}

	/**
	 * @return {@code true} if the current application model has top-level windows.
	 */
	public boolean hasTopLevelWindows() {
		return hasTopLevelWindows(resource);
	}

	/**
	 * @return {@code true} if the specified application model has top-level windows.
	 */
	private boolean hasTopLevelWindows(Resource applicationResource) {
		if (applicationResource == null || applicationResource.getContents() == null) {
			// If the application resource doesn't exist or has no contents, then it has no
			// top-level windows (and we are in an error state).
			return false;
		}
		MApplication application = (MApplication) applicationResource.getContents().get(0);
		return !application.getChildren().isEmpty();
	}

	@Override
	public Resource loadMostRecentModel() {
		File workbenchData = null;
		URI restoreLocation = null;

		if (saveAndRestore) {
			workbenchData = getWorkbenchSaveLocation();
			restoreLocation = URI.createFileURI(workbenchData.getAbsolutePath());
		}

		if (clearPersistedState && workbenchData != null && workbenchData.exists()) {
			workbenchData.delete();
		}

		// last stored time-stamp
		long restoreLastModified = restoreLocation == null ? 0L : new File(
				restoreLocation.toFileString()).lastModified();

		// See bug 380663, bug 381219
		// long lastApplicationModification = getLastApplicationModification();
		// boolean restore = restoreLastModified > lastApplicationModification;
		boolean restore = restoreLastModified > 0;
		boolean initialModel;

		resource = null;
		if (restore && saveAndRestore) {
			resource = loadResource(restoreLocation);
			// If the saved model does not have any top-level windows, Eclipse will exit
			// immediately, so throw out the persisted state and reinitialize with the defaults.
			if (!hasTopLevelWindows(resource)) {
				if (logger != null) {
					logger.error(new Exception(), // log a stack trace to help debug the corruption
							"The persisted application model has no top-level window. Reinitializing with the default application model."); //$NON-NLS-1$
				}
				resource = null;
			}
		}
		if (resource == null) {
			Resource applicationResource = loadResource(applicationDefinitionInstance);
			MApplication theApp = (MApplication) applicationResource.getContents().get(0);
			resource = createResourceWithApp(theApp);
			context.set(E4Workbench.NO_SAVED_MODEL_FOUND, Boolean.TRUE);
			initialModel = true;
		} else {
			initialModel = false;
		}

		// Add model items described in the model extension point
		// This has to be done before commands are put into the context
		MApplication appElement = (MApplication) resource.getContents().get(0);

		this.context.set(MApplication.class, appElement);

		ModelAssembler mac = context.get(ModelAssembler.class);
		if (mac != null) {
			ContextInjectionFactory.invoke(mac, PostConstruct.class, context);
			mac.processModel(initialModel);
		}

		if (!hasTopLevelWindows(resource) && logger != null) {
			logger.error(new Exception(), // log a stack trace to help debug the
											// corruption
					"Loading the application model results in no top-level window." //$NON-NLS-1$
							+ "Continuing execution, but the missing window may cause other initialization failures."); //$NON-NLS-1$
		}

		CommandLineOptionModelProcessor processor = ContextInjectionFactory.make(CommandLineOptionModelProcessor.class, context);
		processor.process();

		return resource;
	}

	@Override
	public void save() throws IOException {
		if (saveAndRestore) {
			Map<String, Object> options = new HashMap<>();
			options.put(E4XMIResource.OPTION_FILTER_PERSIST_STATE, Boolean.TRUE);
			resource.save(options);
		}
	}

	/**
	 * Creates a resource with an app Model, used for saving copies of the main app model.
	 *
	 * @param theApp
	 *            the application model to add to the resource
	 * @return a resource with a proper save path with the model as contents
	 */
	@Override
	public Resource createResourceWithApp(MApplication theApp) {
		Resource res = createResource();
		res.getContents().add((EObject) theApp);
		return res;
	}

	private Resource createResource() {
		if (saveAndRestore) {
			URI saveLocation = URI.createFileURI(getWorkbenchSaveLocation().getAbsolutePath());
			return resourceSet.createResource(saveLocation);
		}
		return resourceSet.createResource(URI.createURI("workbench.xmi")); //$NON-NLS-1$
	}

	private File getWorkbenchSaveLocation() {
		return new File(getBaseLocation(), "workbench.xmi"); //$NON-NLS-1$
	}

	private File getBaseLocation() {
		File baseLocation;
		try {
			baseLocation = new File(URIUtil.toURI(instanceLocation.getURL()));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		baseLocation = new File(baseLocation, ".metadata"); //$NON-NLS-1$
		baseLocation = new File(baseLocation, ".plugins"); //$NON-NLS-1$
		return new File(baseLocation, "org.eclipse.e4.workbench"); //$NON-NLS-1$
	}

	// Ensures that even models with error are loaded!
	private Resource loadResource(URI uri) {
		Resource resource;
		try {
			resource = getResource(uri);
		} catch (Exception e) {
			// TODO We could use diagnostics for better analyzing the error
			logger.error(e, "Unable to load resource " + uri); //$NON-NLS-1$
			return null;
		}

		// TODO once we switch from deltas, we only need this once on the default model?
		String contributorURI = URIHelper.EMFtoPlatform(uri);
		if (contributorURI != null) {
			TreeIterator<EObject> it = EcoreUtil.getAllContents(resource.getContents());
			while (it.hasNext()) {
				EObject o = it.next();
				if (o instanceof MApplicationElement) {
					((MApplicationElement) o).setContributorURI(contributorURI);
				}
			}
		}
		return resource;
	}

	private Resource getResource(URI uri) throws Exception {
		Resource resource;
		if (saveAndRestore) {
			resource = resourceSet.getResource(uri, true);
		} else {
			// Workaround for java.lang.IllegalStateException: No instance data can be specified
			// thrown by org.eclipse.core.internal.runtime.DataArea.assertLocationInitialized
			// The DataArea.assertLocationInitialized is called by ResourceSetImpl.getResource(URI,
			// boolean)
			resource = resourceSet.createResource(uri);
			try (InputStream openStream = new URL(uri.toString()).openStream()) {
				resource.load(openStream, resourceSet.getLoadOptions());
			}
		}

		return resource;
	}

	protected long getLastApplicationModification() {
		long appLastModified = 0L;
		ResourceSetImpl resourceSetImpl = new ResourceSetImpl();

		Map<String, ?> attributes = resourceSetImpl.getURIConverter().getAttributes(
				applicationDefinitionInstance,
				Collections.singletonMap(URIConverter.OPTION_REQUESTED_ATTRIBUTES,
						Collections.singleton(URIConverter.ATTRIBUTE_TIME_STAMP)));

		Object timestamp = attributes.get(URIConverter.ATTRIBUTE_TIME_STAMP);
		if (timestamp instanceof Long) {
			appLastModified = ((Long) timestamp).longValue();
		} else if (applicationDefinitionInstance.isPlatformPlugin()) {
			try {
				java.net.URL url = new java.net.URL(applicationDefinitionInstance.toString());
				// can't just use 'url.openConnection()' as it usually returns a
				// PlatformURLPluginConnection which doesn't expose the
				// last-modification time. So we try to resolve the file through
				// the bundle to obtain a BundleURLConnection instead.
				@SuppressWarnings("restriction")
				Object[] obj = org.eclipse.core.internal.runtime.PlatformURLPluginConnection.parse(url.getFile().trim(),
						url);
				Bundle b = (Bundle) obj[0];
				// first try to resolve as an bundle file entry, then as a resource using
				// the bundle's classpath
				java.net.URL resolved = b.getEntry((String) obj[1]);
				if (resolved == null) {
					resolved = b.getResource((String) obj[1]);
				}
				if (resolved != null) {
					URLConnection openConnection = resolved.openConnection();
					appLastModified = openConnection.getLastModified();
				}
			} catch (Exception e) {
				// ignore
			}
		}

		return appLastModified;
	}
}

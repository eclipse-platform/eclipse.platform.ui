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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.parsers.DocumentBuilderFactory;
import org.eclipse.core.internal.runtime.PlatformURLPluginConnection;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
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
import org.eclipse.e4.ui.workbench.modeling.IModelReconcilingService;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;

/**
 * This class is responsible to load and save the model
 */
public class ResourceHandler implements IModelResourceHandler {

	private ResourceSetImpl resourceSetImpl;
	private Resource resource;

	@Inject
	private Logger logger;

	@Inject
	private IEclipseContext context;

	@Inject
	@Named(E4Workbench.INITIAL_WORKBENCH_MODEL_URI)
	private URI applicationDefinitionInstance;

	@Inject
	@Named(E4Workbench.INSTANCE_LOCATION)
	private Location instanceLocation;

	/**
	 * Dictates whether the model should be stored using EMF or with the merging algorithm.
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=295524
	 * 
	 */
	final private boolean deltaRestore;
	final private boolean saveAndRestore;
	final private boolean clearPersistedState;

	/**
	 * Constructor.
	 * 
	 * @param saveAndRestore
	 * @param clearPersistedState
	 * @param deltaRestore
	 */
	@Inject
	public ResourceHandler(@Named(E4Workbench.PERSIST_STATE) boolean saveAndRestore,
			@Named(E4Workbench.CLEAR_PERSISTED_STATE) boolean clearPersistedState,
			@Named(E4Workbench.DELTA_RESTORE) boolean deltaRestore) {
		this.saveAndRestore = saveAndRestore;
		this.clearPersistedState = clearPersistedState;
		this.deltaRestore = deltaRestore;
	}

	@PostConstruct
	void init() {
		resourceSetImpl = new ResourceSetImpl();
		resourceSetImpl.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new E4XMIResourceFactory());

		resourceSetImpl.getPackageRegistry().put(ApplicationPackageImpl.eNS_URI,
				ApplicationPackageImpl.eINSTANCE);
		resourceSetImpl.getPackageRegistry().put(CommandsPackageImpl.eNS_URI,
				CommandsPackageImpl.eINSTANCE);
		resourceSetImpl.getPackageRegistry().put(UiPackageImpl.eNS_URI, UiPackageImpl.eINSTANCE);
		resourceSetImpl.getPackageRegistry()
				.put(MenuPackageImpl.eNS_URI, MenuPackageImpl.eINSTANCE);
		resourceSetImpl.getPackageRegistry().put(BasicPackageImpl.eNS_URI,
				BasicPackageImpl.eINSTANCE);
		resourceSetImpl.getPackageRegistry().put(AdvancedPackageImpl.eNS_URI,
				AdvancedPackageImpl.eINSTANCE);
		resourceSetImpl
				.getPackageRegistry()
				.put(org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eNS_URI,
						org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eINSTANCE);

	}

	public Resource loadMostRecentModel() {
		File baseLocation;
		try {
			baseLocation = new File(URIUtil.toURI(instanceLocation.getURL()));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		baseLocation = new File(baseLocation, ".metadata"); //$NON-NLS-1$
		baseLocation = new File(baseLocation, ".plugins"); //$NON-NLS-1$
		baseLocation = new File(baseLocation, "org.eclipse.e4.workbench"); //$NON-NLS-1$

		// This is temporary code to migrate existing delta files into full models
		if (deltaRestore && saveAndRestore && !clearPersistedState) {
			File deltaFile = new File(baseLocation, "deltas.xml"); //$NON-NLS-1$
			if (deltaFile.exists()) {
				MApplication appElement = null;
				try {
					// create new resource in case code below fails somewhere
					File workbenchData = new File(baseLocation, "workbench.xmi"); //$NON-NLS-1$			
					URI restoreLocationNew = URI.createFileURI(workbenchData.getAbsolutePath());
					resource = resourceSetImpl.createResource(restoreLocationNew);

					Resource oldResource = loadResource(applicationDefinitionInstance);
					appElement = (MApplication) oldResource.getContents().get(0);

					context.set(MApplication.class, appElement);
					ModelAssembler contribProcessor = ContextInjectionFactory.make(
							ModelAssembler.class, context);
					contribProcessor.processModel();

					File deltaOldFile = new File(baseLocation, "deltas_42M7migration.xml"); //$NON-NLS-1$
					deltaFile.renameTo(deltaOldFile);
					URI restoreLocation = URI.createFileURI(deltaOldFile.getAbsolutePath());

					File file = new File(restoreLocation.toFileString());

					if (file.exists()) {
						Document document = DocumentBuilderFactory.newInstance()
								.newDocumentBuilder().parse(file);
						IModelReconcilingService modelReconcilingService = new ModelReconcilingService();
						ModelReconciler modelReconciler = modelReconcilingService
								.createModelReconciler();
						document.normalizeDocument();
						Collection<ModelDelta> deltas = modelReconciler.constructDeltas(oldResource
								.getContents().get(0), document);
						modelReconcilingService.applyDeltas(deltas);
					}
				} catch (Exception e) {
					if (logger != null) {
						logger.error(e);
					}
				}
				if (appElement != null)
					resource.getContents().add((EObject) appElement);
				return resource;
			}
		}

		File workbenchData = new File(baseLocation, "workbench.xmi"); //$NON-NLS-1$			

		if (clearPersistedState && workbenchData.exists())
			workbenchData.delete();

		URI restoreLocation = null;
		if (saveAndRestore)
			restoreLocation = URI.createFileURI(workbenchData.getAbsolutePath());

		// last stored time-stamp
		long restoreLastModified = restoreLocation == null ? 0L : new File(
				restoreLocation.toFileString()).lastModified();

		// See bug 380663, bug 381219
		// long lastApplicationModification = getLastApplicationModification();
		// boolean restore = restoreLastModified > lastApplicationModification;
		boolean restore = restoreLastModified > 0;

		resource = null;
		if (restore && saveAndRestore) {
			resource = loadResource(restoreLocation);
		}
		if (resource == null) {
			Resource applicationResource = loadResource(applicationDefinitionInstance);
			MApplication theApp = (MApplication) applicationResource.getContents().get(0);
			if (restoreLocation == null)
				restoreLocation = URI.createFileURI(workbenchData.getAbsolutePath());
			resource = resourceSetImpl.createResource(restoreLocation);
			resource.getContents().add((EObject) theApp);
			context.set(E4Workbench.NO_SAVED_MODEL_FOUND, Boolean.TRUE);
		}

		// Add model items described in the model extension point
		// This has to be done before commands are put into the context
		MApplication appElement = (MApplication) resource.getContents().get(0);

		this.context.set(MApplication.class, appElement);
		ModelAssembler contribProcessor = ContextInjectionFactory.make(ModelAssembler.class,
				context);
		contribProcessor.processModel();

		return resource;
	}

	public void save() throws IOException {
		if (saveAndRestore)
			resource.save(null);
	}

	// Ensures that even models with error are loaded!
	private Resource loadResource(URI uri) {
		Resource resource;
		try {
			resource = resourceSetImpl.getResource(uri, true);
		} catch (Exception e) {
			// TODO We could use diagnostics for better analyzing the error
			logger.error(e, "Unable to load resource " + uri.toString()); //$NON-NLS-1$
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
				Object[] obj = PlatformURLPluginConnection.parse(url.getFile().trim(), url);
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

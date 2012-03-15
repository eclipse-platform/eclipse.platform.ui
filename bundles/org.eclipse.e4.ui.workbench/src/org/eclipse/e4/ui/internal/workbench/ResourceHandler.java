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
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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

	private File workbenchData;

	private ResourceSetImpl resourceSetImpl;
	private URI restoreLocation;
	private Resource resource;
	private ModelReconciler reconciler;

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

	private boolean saveAndRestore;

	private boolean clearPersistedState;

	/**
	 * Dictates whether the model should be stored using EMF or with the merging algorithm.
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=295524
	 * 
	 */
	private boolean deltaRestore = true;

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

		// this.registry = registry;
		try {
			workbenchData = new File(URIUtil.toURI(instanceLocation.getURL()));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		workbenchData = new File(workbenchData, ".metadata"); //$NON-NLS-1$
		workbenchData = new File(workbenchData, ".plugins"); //$NON-NLS-1$
		workbenchData = new File(workbenchData, "org.eclipse.e4.workbench"); //$NON-NLS-1$

		if (deltaRestore) {
			workbenchData = new File(workbenchData, "deltas.xml"); //$NON-NLS-1$	
		} else {
			workbenchData = new File(workbenchData, "workbench.xmi"); //$NON-NLS-1$			
		}

		if (workbenchData != null && clearPersistedState) {
			if (workbenchData.exists()) {
				workbenchData.delete();
			}
		}

		if (workbenchData != null && saveAndRestore) {
			restoreLocation = URI.createFileURI(workbenchData.getAbsolutePath());
		}
	}

	public long getLastStoreDatetime() {
		long restoreLastModified = restoreLocation == null ? 0L : new File(
				restoreLocation.toFileString()).lastModified();
		return restoreLastModified;
	}

	public Resource loadRestoredModel() {
		Activator.trace(Policy.DEBUG_WORKBENCH, "Restoring workbench: " + restoreLocation, null); //$NON-NLS-1$
		resource = loadResource(restoreLocation);
		return resource;
	}

	public Resource loadBaseModel() {
		Activator.trace(Policy.DEBUG_WORKBENCH,
				"Initializing workbench: " + applicationDefinitionInstance, null); //$NON-NLS-1$
		if (deltaRestore) {
			resource = loadResource(applicationDefinitionInstance);
		} else {
			MApplication theApp = loadDefaultModel(applicationDefinitionInstance);
			resource = resourceSetImpl.createResource(restoreLocation);
			resource.getContents().add((EObject) theApp);
		}
		return resource;
	}

	private MApplication loadDefaultModel(URI defaultModelPath) {
		Resource resource = loadResource(defaultModelPath);
		MApplication app = (MApplication) resource.getContents().get(0);
		return app;
	}

	// Ensures that even models with error are loaded!
	private Resource loadResource(URI uri) {
		Resource resource;
		try {
			resource = resourceSetImpl.getResource(uri, true);
		} catch (Exception e) {
			// TODO We could use diagnostics for better analyzing the error
			logger.error(e);
			resource = resourceSetImpl.getResource(uri, false);
		}

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

	public void save() throws IOException {
		if (saveAndRestore) {
			if (deltaRestore && reconciler != null) {
				try {
					Document document = (Document) reconciler.serialize();

					// Use a Transformer for output
					TransformerFactory tFactory = TransformerFactory.newInstance();
					Transformer transformer = tFactory.newTransformer();

					DOMSource source = new DOMSource(document);
					File f = new File(restoreLocation.toFileString());
					f.getParentFile().mkdirs();
					StreamResult result = new StreamResult(f);
					transformer.transform(source, result);
				} catch (Exception e) {
					if (logger != null) {
						logger.error(e);
					}
				}
			} else {
				resource.save(null);
			}
		}
	}

	public Resource loadMostRecentModel() {
		if (deltaRestore) {
			try {
				Resource resource = loadBaseModel();
				MApplication appElement = (MApplication) resource.getContents().get(0);
				// Add model items described in the model extension point
				// This has to be done before commands are put into the context
				// ModelExtensionProcessor extProcessor = new ModelExtensionProcessor(appElement);
				// extProcessor.addModelExtensions();

				this.context.set(MApplication.class, appElement);
				ModelAssembler contribProcessor = ContextInjectionFactory.make(
						ModelAssembler.class, context);
				contribProcessor.processModel();

				if (restoreLocation != null) {
					File file = new File(restoreLocation.toFileString());
					reconciler = new XMLModelReconciler();
					reconciler.recordChanges(appElement);

					if (file.exists()) {
						Document document = DocumentBuilderFactory.newInstance()
								.newDocumentBuilder().parse(file);
						IModelReconcilingService modelReconcilingService = new ModelReconcilingService();
						ModelReconciler modelReconciler = modelReconcilingService
								.createModelReconciler();
						document.normalizeDocument();
						Collection<ModelDelta> deltas = modelReconciler.constructDeltas(resource
								.getContents().get(0), document);
						modelReconcilingService.applyDeltas(deltas);
					}
				}
			} catch (Exception e) {
				if (logger != null) {
					logger.error(e);
				}
			}
			return resource;
		}

		long restoreLastModified = getLastStoreDatetime();
		long lastApplicationModification = getLastApplicationModification();

		boolean restore = restoreLastModified > lastApplicationModification;

		Resource resource;
		if (restore && saveAndRestore) {
			resource = loadRestoredModel();
		} else {
			resource = loadBaseModel();
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

	/**
	 * @param applicationDefinitionInstance2
	 * @return
	 */
	public long getLastApplicationModification() {
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
				Object[] obj = PlatformURLPluginConnection.parse(url.getFile().trim(), url);
				Bundle b = (Bundle) obj[0];
				URLConnection openConnection = b.getResource((String) obj[1]).openConnection();
				appLastModified = openConnection.getLastModified();
			} catch (Exception e) {
				// ignore
			}
		}

		return appLastModified;
	}
}

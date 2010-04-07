/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.workbench.ui.internal;

import org.eclipse.e4.core.services.log.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.eclipse.core.internal.runtime.PlatformURLPluginConnection;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.workbench.modeling.IModelReconcilingService;
import org.eclipse.e4.workbench.modeling.ModelDelta;
import org.eclipse.e4.workbench.modeling.ModelReconciler;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;

/**
 * This class is responsible to load and save the model
 */
public class ResourceHandler {

	/**
	 * Dictates whether the model should be stored using EMF or with the merging algorithm.
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=295524
	 */
	private static final boolean RESTORE_VIA_DELTAS = true;

	private File workbenchData;
	private URI applicationDefinitionInstance;
	private ResourceSetImpl resourceSetImpl;
	private URI restoreLocation;
	private Resource resource;
	private ModelReconciler reconciler;
	private Logger logger;

	public ResourceHandler(Location instanceLocation, URI applicationDefinitionInstance,
			boolean saveAndRestore, Logger logger) {
		this.applicationDefinitionInstance = applicationDefinitionInstance;
		this.logger = logger;
		resourceSetImpl = new ResourceSetImpl();
		resourceSetImpl.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				Resource.Factory.Registry.DEFAULT_EXTENSION, new E4XMIResourceFactory());
		resourceSetImpl.getPackageRegistry().put(MApplicationPackage.eNS_URI,
				MApplicationPackage.eINSTANCE);

		// this.registry = registry;
		try {
			workbenchData = new File(URIUtil.toURI(instanceLocation.getURL()));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		workbenchData = new File(workbenchData, ".metadata"); //$NON-NLS-1$
		workbenchData = new File(workbenchData, ".plugins"); //$NON-NLS-1$
		workbenchData = new File(workbenchData, "org.eclipse.e4.workbench"); //$NON-NLS-1$

		if (RESTORE_VIA_DELTAS) {
			workbenchData = new File(workbenchData, "deltas.xml"); //$NON-NLS-1$	
		} else {
			workbenchData = new File(workbenchData, "workbench.xmi"); //$NON-NLS-1$			
		}

		if (workbenchData != null && saveAndRestore) {
			restoreLocation = URI.createFileURI(workbenchData.getAbsolutePath());
		}
	}

	public long getLastStoreDatetime() {
		long restoreLastModified = restoreLocation == null ? 0L : new File(restoreLocation
				.toFileString()).lastModified();
		return restoreLastModified;
	}

	public Resource loadRestoredModel() {
		Activator.trace(Policy.DEBUG_WORKBENCH, "Restoring workbench: " + restoreLocation, null); //$NON-NLS-1$
		resource = resourceSetImpl.getResource(restoreLocation, true);
		return resource;
	}

	public Resource loadBaseModel() {
		Activator.trace(Policy.DEBUG_WORKBENCH,
				"Initializing workbench: " + applicationDefinitionInstance, null); //$NON-NLS-1$
		if (RESTORE_VIA_DELTAS) {
			resource = new ResourceSetImpl().getResource(applicationDefinitionInstance, true);
		} else {
			resource = new E4XMIResource();
			MApplication theApp = loadDefaultModel(applicationDefinitionInstance);
			resource.getContents().add((EObject) theApp);
			resource.setURI(restoreLocation);
		}
		return resource;
	}

	private MApplication loadDefaultModel(URI defaultModelPath) {
		Resource resource = new ResourceSetImpl().getResource(defaultModelPath, true);
		MApplication app = (MApplication) resource.getContents().get(0);
		return app;
	}

	public void save() throws IOException {
		if (RESTORE_VIA_DELTAS) {
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

	public Resource loadMostRecentModel() {
		if (RESTORE_VIA_DELTAS) {
			try {
				Resource resource = loadBaseModel();
				MApplication appElement = (MApplication) resource.getContents().get(0);
				// Add model items described in the model extension point
				// This has to be done before commands are put into the context
				ModelExtensionProcessor extProcessor = new ModelExtensionProcessor(appElement);
				extProcessor.addModelExtensions();

				File file = new File(restoreLocation.toFileString());
				reconciler = new XMLModelReconciler();
				reconciler.recordChanges(appElement);

				if (file.exists()) {
					Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
							.parse(file);
					IModelReconcilingService modelReconcilingService = new ModelReconcilingService();
					ModelReconciler modelReconciler = modelReconcilingService
							.createModelReconciler();
					document.normalizeDocument();
					Collection<ModelDelta> deltas = modelReconciler.constructDeltas(resource
							.getContents().get(0), document);
					modelReconcilingService.applyDeltas(deltas);
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
		if (restore) {
			resource = loadRestoredModel();
		} else {
			resource = loadBaseModel();
		}

		// Add model items described in the model extension point
		// This has to be done before commands are put into the context
		MApplication appElement = (MApplication) resource.getContents().get(0);
		ModelExtensionProcessor extProcessor = new ModelExtensionProcessor(appElement);
		extProcessor.addModelExtensions();

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
				Collections.singletonMap(URIConverter.OPTION_REQUESTED_ATTRIBUTES, Collections
						.singleton(URIConverter.ATTRIBUTE_TIME_STAMP)));

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

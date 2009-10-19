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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.osgi.service.datalocation.Location;

/**
 * This class is responsible to load and save the model
 */
public class ResourceHandler {
	private File workbenchData;
	private URI applicationDefinitionInstance;
	private ResourceSetImpl resourceSetImpl;
	private URI restoreLocation;
	private Resource resource;

	public ResourceHandler(Location instanceLocation, URI applicationDefinitionInstance,
			boolean saveAndRestore) {
		this.applicationDefinitionInstance = applicationDefinitionInstance;
		resourceSetImpl = new ResourceSetImpl();
		resourceSetImpl.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
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
		workbenchData = new File(workbenchData, "workbench.xmi"); //$NON-NLS-1$

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
		resource = new XMIResourceImpl();
		MApplication workbench = loadDefaultModel(applicationDefinitionInstance);
		resource.getContents().add((EObject) workbench);
		resource.setURI(restoreLocation);
		return resource;
	}

	private MApplication loadDefaultModel(URI defaultModelPath) {
		Resource resource = new ResourceSetImpl().getResource(defaultModelPath, true);
		MApplication app = (MApplication) resource.getContents().get(0);
		return app;
	}

	public void save() throws IOException {
		resource.save(null);
	}
}

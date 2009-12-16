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

package org.eclipse.e4.ui.tests.reconciler;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.workbench.ui.internal.E4XMIResourceFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Factory;

public abstract class ModelResourceTest extends TestCase {

	private File temporaryFile;

	private URI temporaryURI;

	private Factory factory;

	private Resource resource;

	@Override
	protected void setUp() throws Exception {
		temporaryFile = new File(System.getProperty("java.io.tmpdir"),
				getClass().getSimpleName() + "_" + getName() + ".e4xmi");
		temporaryFile.delete();
		temporaryURI = URI.createFileURI(temporaryFile.getAbsolutePath());
		factory = createFactory();
		assertNotNull(factory);
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		factory = null;
		temporaryFile.delete();
		temporaryFile = null;
		temporaryURI = null;
	}

	protected Factory createFactory() {
		return new E4XMIResourceFactory();
	}

	protected void saveModel() {
		try {
			resource.save(null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected MApplication createApplication() {
		resource = factory.createResource(temporaryURI);
		if (temporaryFile.exists()) {
			try {
				resource.load(null);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			return (MApplication) resource.getContents().get(0);
		}

		MApplication application = MApplicationFactory.eINSTANCE
				.createApplication();
		resource.getContents().add((EObject) application);
		return application;
	}

	protected MWindow createWindow(MApplication application) {
		MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		application.getChildren().add(window);
		return window;
	}

}

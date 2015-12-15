/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.reconciler;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.E4XMIResourceFactory;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Factory;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.junit.After;
import org.junit.Before;

public abstract class ModelResourceTest {

	private File temporaryFile;

	private URI temporaryURI;

	private Factory factory;

	private Resource resource;

	private XMLResource xmlResource;

	protected EModelService ems;

	@Before
	public void setUp() throws Exception {
		temporaryFile = new File(System.getProperty("java.io.tmpdir"),
				getClass().getSimpleName() + "_" + this.getClass().getSimpleName() + ".e4xmi");
		temporaryFile.delete();
		temporaryURI = URI.createFileURI(temporaryFile.getAbsolutePath());
		factory = createFactory();
		assertNotNull(factory);

		IEclipseContext defaultContext = E4Application.createDefaultContext();
		ems = defaultContext.get(EModelService.class);
	}

	@After
	protected void tearDown() throws Exception {
		factory = null;
		temporaryFile.delete();
		temporaryFile = null;
		temporaryURI = null;
	}

	protected String getId(MApplicationElement applicationElement) {
		return getId((EObject) applicationElement);
	}

	protected String getId(EObject object) {
		return xmlResource.getID(object);
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
		if (resource instanceof XMLResource) {
			xmlResource = (XMLResource) resource;
		}

		if (temporaryFile.exists()) {
			try {
				resource.load(null);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			return (MApplication) resource.getContents().get(0);
		}

		MApplication application = ems.createModelElement(MApplication.class);
		resource.getContents().add((EObject) application);
		return application;
	}

	protected MWindow createWindow(MApplication application) {
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		return window;
	}

	protected MTrimmedWindow createTrimmedWindow(MApplication application) {
		MTrimmedWindow window = ems.createModelElement(MTrimmedWindow.class);
		application.getChildren().add(window);
		return window;
	}

}

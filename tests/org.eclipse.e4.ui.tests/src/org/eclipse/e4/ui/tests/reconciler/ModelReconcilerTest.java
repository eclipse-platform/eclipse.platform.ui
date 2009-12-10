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
import java.io.StringWriter;
import java.util.Collection;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.workbench.modeling.IModelReconcilingService;
import org.eclipse.e4.workbench.modeling.ModelDelta;
import org.eclipse.e4.workbench.modeling.ModelReconciler;
import org.eclipse.e4.workbench.ui.internal.E4XMIResourceFactory;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.Resource.Factory;
import org.w3c.dom.Node;

public abstract class ModelReconcilerTest extends TestCase {

	protected IModelReconcilingService service;

	private File temporaryFile;

	private URI temporaryURI;

	private Factory factory;

	private Resource resource;

	@Override
	protected void setUp() throws Exception {
		service = getModelReconcilingService();
		temporaryFile = new File(System.getProperty("java.io.tmpdir"),
				getClass().getSimpleName() + ".e4x");
		temporaryFile.delete();
		temporaryURI = URI.createFileURI(temporaryFile.getAbsolutePath());
		factory = new E4XMIResourceFactory();
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		factory = null;
		temporaryFile.delete();
		temporaryFile = null;
		temporaryURI = null;
		service = null;
	}

	protected abstract IModelReconcilingService getModelReconcilingService();

	protected ModelReconciler createModelReconciler() {
		return service.createModelReconciler();
	}

	protected Collection<ModelDelta> constructDeltas(Object object,
			Object serializedState) {
		return constructDeltas(createModelReconciler(), object, serializedState);
	}

	protected Collection<ModelDelta> constructDeltas(
			ModelReconciler reconciler, Object object, Object serializedState) {
		return reconciler.constructDeltas(object, serializedState);
	}

	protected void applyAll(Collection<ModelDelta> deltas) {
		applyAll(deltas, new String[0]);
	}

	protected void applyAll(Collection<ModelDelta> deltas, String[] filters) {
		IStatus status = service.applyDeltas(deltas, filters);
		assertNotNull(status);
		assertEquals(IStatus.OK, status.getCode());
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

	protected static void print(Object serializedState) {
		print((Node) serializedState);
	}

	protected static void print(Node serializedState) {
		try {
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer trans = transfac.newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");

			// create string from xml tree
			StringWriter sw = new StringWriter();
			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(serializedState);
			trans.transform(source, result);
			String xmlString = sw.toString();

			// print xml
			System.out.println(xmlString);
			System.out.println();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

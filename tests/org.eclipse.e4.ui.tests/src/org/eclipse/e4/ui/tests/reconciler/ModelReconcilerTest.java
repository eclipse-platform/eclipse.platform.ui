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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.StringWriter;
import java.util.Collection;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.ui.workbench.modeling.IModelReconcilingService;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;
import org.junit.After;
import org.junit.Before;
import org.w3c.dom.Node;

public abstract class ModelReconcilerTest extends ModelResourceTest {

	protected IModelReconcilingService service;

	@Before
	@Override
	public void setUp() throws Exception {
		service = getModelReconcilingService();
		super.setUp();
	}

	@After
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
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

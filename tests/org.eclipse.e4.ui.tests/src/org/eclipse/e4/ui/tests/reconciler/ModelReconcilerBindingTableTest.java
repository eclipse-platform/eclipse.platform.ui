/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.reconciler;

import java.util.Collection;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MBindingTable;
import org.eclipse.e4.workbench.modeling.ModelDelta;
import org.eclipse.e4.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerBindingTableTest extends
		ModelReconcilerTest {

	private void testBindingTable_BindingContextId(String before, String after) {
		MApplication application = createApplication();

		MBindingTable bindingTable = MApplicationFactory.eINSTANCE
				.createBindingTable();
		bindingTable.setBindingContextId(before);
		application.getBindingTables().add(bindingTable);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		bindingTable.setBindingContextId(after);

		Object state = reconciler.serialize();

		application = createApplication();
		bindingTable = application.getBindingTables().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(before, bindingTable.getBindingContextId());

		applyAll(deltas);

		assertEquals(after, bindingTable.getBindingContextId());
	}

	public void testBindingTable_BindingContextId_NullNull() {
		testBindingTable_BindingContextId(null, null);
	}

	public void testBindingTable_BindingContextId_NullEmpty() {
		testBindingTable_BindingContextId(null, "");
	}

	public void testBindingTable_BindingContextId_NullString() {
		testBindingTable_BindingContextId(null, "name");
	}

	public void testBindingTable_BindingContextId_EmptyNull() {
		testBindingTable_BindingContextId("", null);
	}

	public void testBindingTable_BindingContextId_EmptyEmpty() {
		testBindingTable_BindingContextId("", "");
	}

	public void testBindingTable_BindingContextId_EmptyString() {
		testBindingTable_BindingContextId("", "name");
	}

	public void testBindingTable_BindingContextId_StringNull() {
		testBindingTable_BindingContextId("name", null);
	}

	public void testBindingTable_BindingContextId_StringEmpty() {
		testBindingTable_BindingContextId("name", "");
	}

	public void testBindingTable_BindingContextId_StringStringUnchanged() {
		testBindingTable_BindingContextId("name", "name");
	}

	public void testBindingTable_BindingContextId_StringStringChanged() {
		testBindingTable_BindingContextId("name", "name2");
	}

}

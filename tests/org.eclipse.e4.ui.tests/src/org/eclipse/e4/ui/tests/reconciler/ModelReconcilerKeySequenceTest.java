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

import java.util.Collection;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;
import org.junit.Test;

public abstract class ModelReconcilerKeySequenceTest extends
		ModelReconcilerTest {

	private void testKeySequence_KeySequence(String before, String after) {
		MApplication application = createApplication();

		MBindingTable bindingTable = ems.createModelElement(MBindingTable.class);
		application.getBindingTables().add(bindingTable);

		MKeyBinding keyBinding = ems.createModelElement(MKeyBinding.class);
		keyBinding.setKeySequence(before);
		bindingTable.getBindings().add(keyBinding);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		keyBinding.setKeySequence(after);

		Object state = reconciler.serialize();

		application = createApplication();
		bindingTable = application.getBindingTables().get(0);
		keyBinding = bindingTable.getBindings().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(before, keyBinding.getKeySequence());

		applyAll(deltas);

		assertEquals(after, keyBinding.getKeySequence());
	}

	@Test
	public void testKeySequence_KeySequence_NullNull() {
		testKeySequence_KeySequence(null, null);
	}

	@Test
	public void testKeySequence_KeySequence_NullEmpty() {
		testKeySequence_KeySequence(null, "");
	}

	@Test
	public void testKeySequence_KeySequence_NullString() {
		testKeySequence_KeySequence(null, "Ctrl+S");
	}

	@Test
	public void testKeySequence_KeySequence_EmptyNull() {
		testKeySequence_KeySequence("", null);
	}

	@Test
	public void testKeySequence_KeySequence_EmptyEmpty() {
		testKeySequence_KeySequence("", "");
	}

	@Test
	public void testKeySequence_KeySequence_EmptyString() {
		testKeySequence_KeySequence("", "Ctrl+S");
	}

	@Test
	public void testKeySequence_KeySequence_StringNull() {
		testKeySequence_KeySequence("Ctrl+S", null);
	}

	@Test
	public void testKeySequence_KeySequence_StringEmpty() {
		testKeySequence_KeySequence("Ctrl+S", "");
	}

	@Test
	public void testKeySequence_KeySequence_StringString() {
		testKeySequence_KeySequence("Ctrl+S", "Ctrl+D");
	}

}

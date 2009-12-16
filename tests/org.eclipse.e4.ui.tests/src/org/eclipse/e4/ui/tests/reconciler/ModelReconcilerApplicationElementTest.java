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

import java.util.Collection;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.workbench.modeling.ModelDelta;
import org.eclipse.e4.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerApplicationElementTest extends
		ModelReconcilerTest {

	private void testApplicationElement_Style(String before, String after) {
		MApplication application = createApplication();
		application.setStyle(before);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		application.setStyle(after);

		Object state = reconciler.serialize();

		application = createApplication();

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(before, application.getStyle());

		applyAll(deltas);

		assertEquals(after, application.getStyle());
	}

	public void testApplicationElement_Style_NullNull() {
		testApplicationElement_Style(null, null);
	}

	public void testApplicationElement_Style_NullEmpty() {
		testApplicationElement_Style(null, "");
	}

	public void testApplicationElement_Style_NullString() {
		testApplicationElement_Style(null, "Ctrl+S");
	}

	public void testApplicationElement_Style_EmptyNull() {
		testApplicationElement_Style("", null);
	}

	public void testApplicationElement_Style_EmptyEmpty() {
		testApplicationElement_Style("", "");
	}

	public void testApplicationElement_Style_EmptyString() {
		testApplicationElement_Style("", "Ctrl+S");
	}

	public void testApplicationElement_Style_StringNull() {
		testApplicationElement_Style("Ctrl+S", null);
	}

	public void testApplicationElement_Style_StringEmpty() {
		testApplicationElement_Style("Ctrl+S", "");
	}

	public void testApplicationElement_Style_StringStringUnchanged() {
		testApplicationElement_Style("Ctrl+S", "Ctrl+S");
	}

	public void testApplicationElement_Style_StringStringChanged() {
		testApplicationElement_Style("Ctrl+S", "Ctrl+D");
	}
}

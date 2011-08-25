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
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerPartDescriptorTest extends
		ModelReconcilerTest {

	private void testPartDescriptor_AllowMultiple(boolean before, boolean after) {
		MApplication application = createApplication();
		MPartDescriptor descriptor = BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		application.getDescriptors().add(descriptor);

		descriptor.setAllowMultiple(before);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		descriptor.setAllowMultiple(after);

		Object state = reconciler.serialize();

		application = createApplication();
		descriptor = application.getDescriptors().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(before, descriptor.isAllowMultiple());

		applyAll(deltas);

		assertEquals(after, descriptor.isAllowMultiple());
	}

	public void testPartDescriptor_AllowMultiple_TrueTrue() {
		testPartDescriptor_AllowMultiple(true, true);
	}

	public void testPartDescriptor_AllowMultiple_TrueFalse() {
		testPartDescriptor_AllowMultiple(true, false);
	}

	public void testPartDescriptor_AllowMultiple_FalseTrue() {
		testPartDescriptor_AllowMultiple(false, true);
	}

	public void testPartDescriptor_AllowMultiple_FalseFalse() {
		testPartDescriptor_AllowMultiple(false, false);
	}

	private void testUIItem_Tooltip(String before, String after) {
		MApplication application = createApplication();
		MPartDescriptor descriptor = BasicFactoryImpl.eINSTANCE
				.createPartDescriptor();
		application.getDescriptors().add(descriptor);

		descriptor.setCategory(before);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		descriptor.setCategory(after);

		Object state = reconciler.serialize();

		application = createApplication();
		descriptor = application.getDescriptors().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(before, descriptor.getCategory());

		applyAll(deltas);

		assertEquals(after, descriptor.getCategory());
	}

	public void testUIItem_Tooltip_NullNull() {
		testUIItem_Tooltip(null, null);
	}

	public void testUIItem_Tooltip_NullEmpty() {
		testUIItem_Tooltip(null, "");
	}

	public void testUIItem_Tooltip_NullString() {
		testUIItem_Tooltip(null, "toolTip");
	}

	public void testUIItem_Tooltip_EmptyNull() {
		testUIItem_Tooltip("", null);
	}

	public void testUIItem_Tooltip_EmptyEmpty() {
		testUIItem_Tooltip("", "");
	}

	public void testUIItem_Tooltip_EmptyString() {
		testUIItem_Tooltip("", "toolTip");
	}

	public void testUIItem_Tooltip_StringNull() {
		testUIItem_Tooltip("toolTip", null);
	}

	public void testUIItem_Tooltip_StringEmpty() {
		testUIItem_Tooltip("toolTip", "");
	}

	public void testUIItem_Tooltip_StringStringUnchanged() {
		testUIItem_Tooltip("toolTip", "toolTip");
	}

	public void testUIItem_Tooltip_StringStringChanged() {
		testUIItem_Tooltip("toolTip", "toolTip2");
	}
}

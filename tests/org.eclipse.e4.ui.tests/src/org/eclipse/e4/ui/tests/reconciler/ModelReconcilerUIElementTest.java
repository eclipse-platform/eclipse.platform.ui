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
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.workbench.modeling.ModelDelta;
import org.eclipse.e4.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerUIElementTest extends ModelReconcilerTest {

	private void testUIElement_Visible(boolean before, boolean after) {
		String applicationId = createId();
		String windowId = createId();
		String partId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		part.setToBeRendered(before);
		part.setId(partId);
		window.getChildren().add(part);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		part.setToBeRendered(after);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		part = MApplicationFactory.eINSTANCE.createPart();
		part.setToBeRendered(before);
		part.setId(partId);
		window.getChildren().add(part);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(before, part.isToBeRendered());

		applyAll(deltas);

		assertEquals(after, part.isToBeRendered());
	}

	public void testUIElement_Visible_TrueTrue() {
		testUIElement_Visible(true, true);
	}

	public void testUIElement_Visible_TrueFalse() {
		testUIElement_Visible(true, false);
	}

	public void testUIElement_Visible_FalseTrue() {
		testUIElement_Visible(false, true);
	}

	public void testUIElement_Visible_FalseFalse() {
		testUIElement_Visible(false, false);
	}

	public void testUIElement_Visible_Unmodified() {
		boolean defaultValue = ((Boolean) MApplicationPackage.eINSTANCE
				.getUIElement_ToBeRendered().getDefaultValue()).booleanValue();

		String applicationId = createId();
		String windowId = createId();
		String partId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		part.setToBeRendered(!defaultValue);
		part.setName("name");
		part.setId(partId);
		window.getChildren().add(part);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		part.setName("newName");

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		part = MApplicationFactory.eINSTANCE.createPart();
		part.setToBeRendered(!defaultValue);
		part.setName("name");
		part.setId(partId);
		window.getChildren().add(part);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(!defaultValue, part.isToBeRendered());
		assertEquals("name", part.getName());

		applyAll(deltas);

		assertEquals(!defaultValue, part.isToBeRendered());
		assertEquals("newName", part.getName());
	}

	private void testUIElement_Widget(Object before, Object after) {
		String applicationId = createId();
		String windowId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);
		window.setWidget(before);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.setWidget(after);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);
		window.setWidget(before);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(before, window.getWidget());

		applyAll(deltas);

		// no change, 'factory' is a transient attribute
		assertEquals(before, window.getWidget());
	}

	public void testUIElement_Widget_NullNull() {
		testUIElement_Widget(null, null);
	}

	public void testUIElement_Widget_NullObject() {
		testUIElement_Widget(null, new Object());
	}

	public void testUIElement_Widget_ObjectNull() {
		testUIElement_Widget(new Object(), null);
	}

	public void testUIElement_Widget_ObjectObject() {
		testUIElement_Widget(new Object(), new Object());
	}

	private void testUIElement_Factory(Object before, Object after) {
		String applicationId = createId();
		String windowId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);
		window.setRenderer(before);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.setRenderer(after);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);
		window.setRenderer(before);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(before, window.getRenderer());

		applyAll(deltas);

		// no change, 'factory' is a transient attribute
		assertEquals(before, window.getRenderer());
	}

	public void testUIElement_Factory_NullNull() {
		testUIElement_Factory(null, null);
	}

	public void testUIElement_Factory_NullObject() {
		testUIElement_Factory(null, new Object());
	}

	public void testUIElement_Factory_ObjectNull() {
		testUIElement_Factory(new Object(), null);
	}

	public void testUIElement_Factory_ObjectObject() {
		testUIElement_Factory(new Object(), new Object());
	}
}

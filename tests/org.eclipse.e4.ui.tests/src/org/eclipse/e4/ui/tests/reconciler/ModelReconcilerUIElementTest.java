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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.impl.UiFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;
import org.junit.Test;

public abstract class ModelReconcilerUIElementTest extends ModelReconcilerTest {

	private void testUIElement_ToBeRendered(boolean before, boolean after) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part = ems.createModelElement(MPart.class);
		part.setToBeRendered(before);
		window.getChildren().add(part);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		part.setToBeRendered(after);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		part = (MPart) window.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(before, part.isToBeRendered());

		applyAll(deltas);

		assertEquals(after, part.isToBeRendered());
	}

	@Test
	public void testUIElement_ToBeRendered_TrueTrue() {
		testUIElement_ToBeRendered(true, true);
	}

	@Test
	public void testUIElement_ToBeRendered_TrueFalse() {
		testUIElement_ToBeRendered(true, false);
	}

	@Test
	public void testUIElement_ToBeRendered_FalseTrue() {
		testUIElement_ToBeRendered(false, true);
	}

	@Test
	public void testUIElement_ToBeRendered_FalseFalse() {
		testUIElement_ToBeRendered(false, false);
	}

	private void testUIElement_Visible(boolean before, boolean after) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part = ems.createModelElement(MPart.class);
		part.setVisible(before);
		window.getChildren().add(part);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		part.setVisible(after);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		part = (MPart) window.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(before, part.isVisible());

		applyAll(deltas);

		assertEquals(after, part.isVisible());
	}

	@Test
	public void testUIElement_Visible_TrueTrue() {
		testUIElement_Visible(true, true);
	}

	@Test
	public void testUIElement_Visible_TrueFalse() {
		testUIElement_Visible(true, false);
	}

	@Test
	public void testUIElement_Visible_FalseTrue() {
		testUIElement_Visible(false, true);
	}

	@Test
	public void testUIElement_Visible_FalseFalse() {
		testUIElement_Visible(false, false);
	}

	@Test
	public void testUIElement_Visible_Unmodified() {
		boolean defaultValue = ((Boolean) UiPackageImpl.eINSTANCE
				.getUIElement_ToBeRendered().getDefaultValue()).booleanValue();

		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part = ems.createModelElement(MPart.class);
		part.setToBeRendered(!defaultValue);
		part.setLabel("name");
		window.getChildren().add(part);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		part.setLabel("newName");

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		part = (MPart) window.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(!defaultValue, part.isToBeRendered());
		assertEquals("name", part.getLabel());

		applyAll(deltas);

		assertEquals(!defaultValue, part.isToBeRendered());
		assertEquals("newName", part.getLabel());
	}

	private void testUIElement_Widget(Object before, Object after) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setWidget(before);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.setWidget(after);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		before = window.getWidget();

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(before, window.getWidget());

		applyAll(deltas);

		// no change, 'factory' is a transient attribute
		assertEquals(before, window.getWidget());
	}

	@Test
	public void testUIElement_Widget_NullNull() {
		testUIElement_Widget(null, null);
	}

	@Test
	public void testUIElement_Widget_NullObject() {
		testUIElement_Widget(null, new Object());
	}

	@Test
	public void testUIElement_Widget_ObjectNull() {
		testUIElement_Widget(new Object(), null);
	}

	@Test
	public void testUIElement_Widget_ObjectObject() {
		testUIElement_Widget(new Object(), new Object());
	}

	@Test
	public void testUIElement_ContainerData_Unmodified() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setLabel("name");
		window.setTooltip("toolTip");
		window.setContainerData("baseData");

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.setContainerData("changedData");

		Object serializedState = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application,
				serializedState);

		assertEquals("toolTip", window.getTooltip());
		assertEquals("name", window.getLabel());
		assertEquals("baseData", window.getContainerData());

		applyAll(deltas);

		assertEquals("toolTip", window.getTooltip());
		assertEquals("name", window.getLabel());
		assertEquals("changedData", window.getContainerData());
	}

	private void testUIElement_ContainerDataUnchanged(String containerData) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setContainerData(containerData);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		saveModel();

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(containerData, window.getContainerData());

		applyAll(deltas);

		assertEquals(containerData, window.getContainerData());
	}

	@Test
	public void testUIElement_ContainerDataUnchanged_Null() {
		testUIElement_ContainerDataUnchanged(null);
	}

	@Test
	public void testUIElement_ContainerDataUnchanged_Empty() {
		testUIElement_ContainerDataUnchanged("");
	}

	@Test
	public void testUIElement_ContainerDataUnchanged_String() {
		testUIElement_ContainerDataUnchanged("newData");
	}

	private void testUIElement_ContainerData(String before, String after) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setContainerData(before);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.setContainerData(after);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(before, window.getContainerData());

		applyAll(deltas);

		assertEquals(after, window.getContainerData());
	}

	@Test
	public void testUIElement_ContainerData_NullNull() {
		testUIElement_ContainerData(null, null);
	}

	@Test
	public void testUIElement_ContainerData_NullEmpty() {
		testUIElement_ContainerData(null, "");
	}

	@Test
	public void testUIElement_ContainerData_NullString() {
		testUIElement_ContainerData(null, "newData");
	}

	@Test
	public void testUIElement_ContainerData_EmptyNull() {
		testUIElement_ContainerData("", null);
	}

	@Test
	public void testUIElement_ContainerData_EmptyEmpty() {
		testUIElement_ContainerData("", "");
	}

	@Test
	public void testUIElement_ContainerData_EmptyString() {
		testUIElement_ContainerData("", "newData");
	}

	@Test
	public void testUIElement_ContainerData_StringNull() {
		testUIElement_ContainerData("newData", null);
	}

	@Test
	public void testUIElement_ContainerData_StringEmpty() {
		testUIElement_ContainerData("newData", "");
	}

	@Test
	public void testUIElement_ContainerData_StringStringUnchanged() {
		testUIElement_ContainerData("newData", "newData");
	}

	@Test
	public void testUIElement_ContainerData_StringStringChanged() {
		testUIElement_ContainerData("newData", "newData2");
	}

	private void testUIElement_Renderer(Object before, Object after) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setRenderer(before);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.setRenderer(after);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		before = window.getRenderer();

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(before, window.getRenderer());

		applyAll(deltas);

		// no change, 'renderer' is a transient attribute
		assertEquals(before, window.getRenderer());
	}

	@Test
	public void testUIElement_Renderer_NullNull() {
		testUIElement_Renderer(null, null);
	}

	@Test
	public void testUIElement_Renderer_NullObject() {
		testUIElement_Renderer(null, new Object());
	}

	@Test
	public void testUIElement_Renderer_ObjectNull() {
		testUIElement_Renderer(new Object(), null);
	}

	@Test
	public void testUIElement_Renderer_ObjectObject() {
		testUIElement_Renderer(new Object(), new Object());
	}

	@Test
	public void testUIElement_VisibleWhen() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MCoreExpression expression = UiFactoryImpl.eINSTANCE
				.createCoreExpression();
		expression.setCoreExpressionId("id");
		window.setVisibleWhen(expression);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(null, window.getVisibleWhen());

		applyAll(deltas);

		assertTrue(window.getVisibleWhen() instanceof MCoreExpression);
		expression = (MCoreExpression) window.getVisibleWhen();
		assertEquals("id", expression.getCoreExpressionId());
	}

	@Test
	public void testUIElement_CurSharedRef_Set() {
		MApplication application = createApplication();
		MWindow window = ems.createModelElement(MWindow.class);
		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		MPerspective perspective = ems.createModelElement(MPerspective.class);
		MPart part = ems.createModelElement(MPart.class);
		MPlaceholder placeholder = ems.createModelElement(MPlaceholder.class);

		application.getChildren().add(window);
		application.setSelectedElement(window);

		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);

		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		perspective.getChildren().add(placeholder);
		perspective.setSelectedElement(placeholder);

		window.getSharedElements().add(part);

		placeholder.setRef(part);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		part.setCurSharedRef(placeholder);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		perspectiveStack = (MPerspectiveStack) window.getChildren().get(0);
		perspective = perspectiveStack.getChildren().get(0);
		placeholder = (MPlaceholder) perspective.getChildren().get(0);
		part = (MPart) window.getSharedElements().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));
		assertEquals(1, window.getChildren().size());
		assertEquals(perspectiveStack, window.getChildren().get(0));
		assertEquals(1, perspectiveStack.getChildren().size());
		assertEquals(perspective, perspectiveStack.getChildren().get(0));
		assertEquals(1, perspective.getChildren().size());
		assertEquals(placeholder, perspective.getChildren().get(0));
		assertEquals(1, window.getSharedElements().size());
		assertEquals(part, window.getSharedElements().get(0));
		assertEquals(part, placeholder.getRef());
		assertNull(part.getCurSharedRef());

		applyAll(deltas);

		assertEquals(1, application.getChildren().size());
		assertEquals(window, application.getChildren().get(0));
		assertEquals(1, window.getChildren().size());
		assertEquals(perspectiveStack, window.getChildren().get(0));
		assertEquals(1, perspectiveStack.getChildren().size());
		assertEquals(perspective, perspectiveStack.getChildren().get(0));
		assertEquals(1, perspective.getChildren().size());
		assertEquals(placeholder, perspective.getChildren().get(0));
		assertEquals(1, window.getSharedElements().size());
		assertEquals(part, window.getSharedElements().get(0));
		assertEquals(part, placeholder.getRef());
		assertNull(part.getCurSharedRef());
	}
}

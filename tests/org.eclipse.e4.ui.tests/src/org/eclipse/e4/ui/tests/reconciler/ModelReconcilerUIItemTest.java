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
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;
import org.junit.Test;

public abstract class ModelReconcilerUIItemTest extends ModelReconcilerTest {

	@Test
	public void testUIItem_IconURI_Unmodified() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setLabel("name");
		window.setIconURI("iconURI");

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.setLabel("name2");

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals("iconURI", window.getIconURI());
		assertEquals("name", window.getLabel());

		applyAll(deltas);

		assertEquals("iconURI", window.getIconURI());
		assertEquals("name2", window.getLabel());
	}

	private void testUIItem_IconURIUnchanged(String iconURI) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setIconURI(iconURI);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(iconURI, window.getIconURI());

		applyAll(deltas);

		assertEquals(iconURI, window.getIconURI());
	}

	@Test
	public void testUIItem_IconURIUnchanged_Null() {
		testUIItem_IconURIUnchanged(null);
	}

	@Test
	public void testUIItem_IconURIUnchanged_Empty() {
		testUIItem_IconURIUnchanged("");
	}

	@Test
	public void testUIItem_IconURIUnchanged_String() {
		testUIItem_IconURIUnchanged("iconURI");
	}

	private void testUIItem_IconURI(String before, String after) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setIconURI(before);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.setIconURI(after);

		Object serializedState = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application,
				serializedState);

		assertEquals(before, window.getIconURI());

		applyAll(deltas);

		assertEquals(after, window.getIconURI());
	}

	@Test
	public void testUIItem_IconURI_NullNull() {
		testUIItem_IconURI(null, null);
	}

	@Test
	public void testUIItem_IconURI_NullEmpty() {
		testUIItem_IconURI(null, "");
	}

	@Test
	public void testUIItem_IconURI_NullString() {
		testUIItem_IconURI(null, "iconURI");
	}

	@Test
	public void testUIItem_IconURI_EmptyNull() {
		testUIItem_IconURI("", null);
	}

	@Test
	public void testUIItem_IconURI_EmptyEmpty() {
		testUIItem_IconURI("", "");
	}

	@Test
	public void testUIItem_IconURI_EmptyString() {
		testUIItem_IconURI("", "iconURI");
	}

	@Test
	public void testUIItem_IconURI_StringNull() {
		testUIItem_IconURI("iconURI", null);
	}

	@Test
	public void testUIItem_IconURI_StringEmpty() {
		testUIItem_IconURI("iconURI", "");
	}

	@Test
	public void testUIItem_IconURI_StringStringUnchanged() {
		testUIItem_IconURI("iconURI", "iconURI");
	}

	@Test
	public void testUIItem_IconURI_StringStringChanged() {
		testUIItem_IconURI("iconURI", "iconURI2");
	}

	@Test
	public void testUIItem_Tooltip_Unmodified() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setLabel("name");
		window.setTooltip("toolTip");

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.setLabel("name2");

		Object serializedState = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application,
				serializedState);

		assertEquals("toolTip", window.getTooltip());
		assertEquals("name", window.getLabel());

		applyAll(deltas);

		assertEquals("toolTip", window.getTooltip());
		assertEquals("name2", window.getLabel());
	}

	private void testUIItem_TooltipUnchanged(String toolTip) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setTooltip(toolTip);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		saveModel();

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(toolTip, window.getTooltip());

		applyAll(deltas);

		assertEquals(toolTip, window.getTooltip());
	}

	@Test
	public void testUIItem_TooltipUnchanged_Null() {
		testUIItem_TooltipUnchanged(null);
	}

	@Test
	public void testUIItem_TooltipUnchanged_Empty() {
		testUIItem_TooltipUnchanged("");
	}

	@Test
	public void testUIItem_TooltipUnchanged_String() {
		testUIItem_TooltipUnchanged("toolTip");
	}

	private void testUIItem_Tooltip(String before, String after) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setTooltip(before);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.setTooltip(after);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(before, window.getTooltip());

		applyAll(deltas);

		assertEquals(after, window.getTooltip());
	}

	@Test
	public void testUIItem_Tooltip_NullNull() {
		testUIItem_Tooltip(null, null);
	}

	@Test
	public void testUIItem_Tooltip_NullEmpty() {
		testUIItem_Tooltip(null, "");
	}

	@Test
	public void testUIItem_Tooltip_NullString() {
		testUIItem_Tooltip(null, "toolTip");
	}

	@Test
	public void testUIItem_Tooltip_EmptyNull() {
		testUIItem_Tooltip("", null);
	}

	@Test
	public void testUIItem_Tooltip_EmptyEmpty() {
		testUIItem_Tooltip("", "");
	}

	@Test
	public void testUIItem_Tooltip_EmptyString() {
		testUIItem_Tooltip("", "toolTip");
	}

	@Test
	public void testUIItem_Tooltip_StringNull() {
		testUIItem_Tooltip("toolTip", null);
	}

	@Test
	public void testUIItem_Tooltip_StringEmpty() {
		testUIItem_Tooltip("toolTip", "");
	}

	@Test
	public void testUIItem_Tooltip_StringStringUnchanged() {
		testUIItem_Tooltip("toolTip", "toolTip");
	}

	@Test
	public void testUIItem_Tooltip_StringStringChanged() {
		testUIItem_Tooltip("toolTip", "toolTip2");
	}

	@Test
	public void testUIItem_Label_Unmodified() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setLabel("name");
		window.setTooltip("toolTip");

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.setTooltip("toolTip2");

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals("toolTip", window.getTooltip());
		assertEquals("name", window.getLabel());

		applyAll(deltas);

		assertEquals("toolTip2", window.getTooltip());
		assertEquals("name", window.getLabel());
	}

	private void testUIItem_LabelUnchanged(String name) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setLabel(name);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(name, window.getLabel());

		applyAll(deltas);

		assertEquals(name, window.getLabel());
	}

	@Test
	public void testUIItem_LabelUnchanged_Null() {
		testUIItem_LabelUnchanged(null);
	}

	@Test
	public void testUIItem_LabelUnchanged_Empty() {
		testUIItem_LabelUnchanged("");
	}

	@Test
	public void testUIItem_LabelUnchanged_String() {
		testUIItem_LabelUnchanged("name");
	}

	private void testUIItem_Label(String before, String after) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);
		window.setLabel(before);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.setLabel(after);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(before, window.getLabel());

		applyAll(deltas);

		assertEquals(after, window.getLabel());
	}

	@Test
	public void testUIItem_Label_NullNull() {
		testUIItem_Label(null, null);
	}

	@Test
	public void testUIItem_Label_NullEmpty() {
		testUIItem_Label(null, "");
	}

	@Test
	public void testUIItem_Label_NullString() {
		testUIItem_Label(null, "name");
	}

	@Test
	public void testUIItem_Label_EmptyNull() {
		testUIItem_Label("", null);
	}

	@Test
	public void testUIItem_Label_EmptyEmpty() {
		testUIItem_Label("", "");
	}

	@Test
	public void testUIItem_Label_EmptyString() {
		testUIItem_Label("", "name");
	}

	@Test
	public void testUIItem_Label_StringNull() {
		testUIItem_Label("name", null);
	}

	@Test
	public void testUIItem_Label_StringEmpty() {
		testUIItem_Label("name", "");
	}

	@Test
	public void testUIItem_Label_StringStringUnchanged() {
		testUIItem_Label("name", "name");
	}

	@Test
	public void testUIItem_Label_StringStringChanged() {
		testUIItem_Label("name", "name2");
	}
}

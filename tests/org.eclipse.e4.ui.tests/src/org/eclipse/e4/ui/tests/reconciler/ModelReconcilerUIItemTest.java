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
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.workbench.modeling.ModelDelta;
import org.eclipse.e4.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerUIItemTest extends ModelReconcilerTest {

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

	public void testUIItem_IconURIUnchanged_Null() {
		testUIItem_IconURIUnchanged(null);
	}

	public void testUIItem_IconURIUnchanged_Empty() {
		testUIItem_IconURIUnchanged("");
	}

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

	public void testUIItem_IconURI_NullNull() {
		testUIItem_IconURI(null, null);
	}

	public void testUIItem_IconURI_NullEmpty() {
		testUIItem_IconURI(null, "");
	}

	public void testUIItem_IconURI_NullString() {
		testUIItem_IconURI(null, "iconURI");
	}

	public void testUIItem_IconURI_EmptyNull() {
		testUIItem_IconURI("", null);
	}

	public void testUIItem_IconURI_EmptyEmpty() {
		testUIItem_IconURI("", "");
	}

	public void testUIItem_IconURI_EmptyString() {
		testUIItem_IconURI("", "iconURI");
	}

	public void testUIItem_IconURI_StringNull() {
		testUIItem_IconURI("iconURI", null);
	}

	public void testUIItem_IconURI_StringEmpty() {
		testUIItem_IconURI("iconURI", "");
	}

	public void testUIItem_IconURI_StringStringUnchanged() {
		testUIItem_IconURI("iconURI", "iconURI");
	}

	public void testUIItem_IconURI_StringStringChanged() {
		testUIItem_IconURI("iconURI", "iconURI2");
	}

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

	public void testUIItem_TooltipUnchanged_Null() {
		testUIItem_TooltipUnchanged(null);
	}

	public void testUIItem_TooltipUnchanged_Empty() {
		testUIItem_TooltipUnchanged("");
	}

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

	public void testUIItem_Name_Unmodified() {
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

	private void testUIItem_NameUnchanged(String name) {
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

	public void testUIItem_NameUnchanged_Null() {
		testUIItem_NameUnchanged(null);
	}

	public void testUIItem_NameUnchanged_Empty() {
		testUIItem_NameUnchanged("");
	}

	public void testUIItem_NameUnchanged_String() {
		testUIItem_NameUnchanged("name");
	}

	private void testUIItem_Name(String before, String after) {
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

	public void testUIItem_Name_NullNull() {
		testUIItem_Name(null, null);
	}

	public void testUIItem_Name_NullEmpty() {
		testUIItem_Name(null, "");
	}

	public void testUIItem_Name_NullString() {
		testUIItem_Name(null, "name");
	}

	public void testUIItem_Name_EmptyNull() {
		testUIItem_Name("", null);
	}

	public void testUIItem_Name_EmptyEmpty() {
		testUIItem_Name("", "");
	}

	public void testUIItem_Name_EmptyString() {
		testUIItem_Name("", "name");
	}

	public void testUIItem_Name_StringNull() {
		testUIItem_Name("name", null);
	}

	public void testUIItem_Name_StringEmpty() {
		testUIItem_Name("name", "");
	}

	public void testUIItem_Name_StringStringUnchanged() {
		testUIItem_Name("name", "name");
	}

	public void testUIItem_Name_StringStringChanged() {
		testUIItem_Name("name", "name2");
	}
}

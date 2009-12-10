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
import org.eclipse.e4.ui.model.application.MMenu;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MToolBar;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.workbench.modeling.ModelDelta;
import org.eclipse.e4.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerPartTest extends ModelReconcilerTest {

	public void testPart_Menus_Add() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		window.getChildren().add(part);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MMenu menu = MApplicationFactory.eINSTANCE.createMenu();
		part.getMenus().add(menu);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		part = (MPart) window.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(0, part.getMenus().size());

		applyAll(deltas);

		assertEquals(1, part.getMenus().size());
	}

	public void testPart_Menus_Remove() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		window.getChildren().add(part);

		MMenu menu = MApplicationFactory.eINSTANCE.createMenu();
		part.getMenus().add(menu);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		part.getMenus().remove(menu);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		part = (MPart) window.getChildren().get(0);
		menu = part.getMenus().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, part.getMenus().size());
		assertEquals(menu, part.getMenus().get(0));

		applyAll(deltas);

		assertEquals(0, part.getMenus().size());
	}

	private void testPart_Menu_ToBeRendered(boolean before, boolean after) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		window.getChildren().add(part);

		MMenu menu = MApplicationFactory.eINSTANCE.createMenu();
		menu.setToBeRendered(before);
		part.getMenus().add(menu);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		menu.setToBeRendered(after);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		part = (MPart) window.getChildren().get(0);
		menu = part.getMenus().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(before, menu.isToBeRendered());

		applyAll(deltas);

		assertEquals(after, menu.isToBeRendered());
	}

	public void testPart_Menu_ToBeRendered_TrueTrue() {
		testPart_Menu_ToBeRendered(true, true);
	}

	public void testPart_Menu_ToBeRendered_TrueFalse() {
		testPart_Menu_ToBeRendered(true, false);
	}

	public void testPart_Menu_ToBeRendered_FalseTrue() {
		testPart_Menu_ToBeRendered(false, true);
	}

	public void testPart_Menu_ToBeRendered_FalseFalse() {
		testPart_Menu_ToBeRendered(false, false);
	}

	public void testPart_ToolBar_Set() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		window.getChildren().add(part);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MToolBar toolBar = MApplicationFactory.eINSTANCE.createToolBar();
		part.setToolbar(toolBar);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		part = (MPart) window.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertNull(part.getToolbar());

		applyAll(deltas);

		assertNotNull(part.getToolbar());
	}

	public void testPart_ToolBar_Unset() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		window.getChildren().add(part);

		MToolBar toolBar = MApplicationFactory.eINSTANCE.createToolBar();
		part.setToolbar(toolBar);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		part.setToolbar(null);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		part = (MPart) window.getChildren().get(0);

		toolBar = MApplicationFactory.eINSTANCE.createToolBar();
		part.setToolbar(toolBar);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(toolBar, part.getToolbar());

		applyAll(deltas);

		assertNull(part.getToolbar());
	}

	private void testPart_ToolBar_ToBeRendered(boolean before, boolean after) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		window.getChildren().add(part);

		MToolBar toolBar = MApplicationFactory.eINSTANCE.createToolBar();
		toolBar.setToBeRendered(before);
		part.setToolbar(toolBar);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		toolBar.setToBeRendered(after);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		part = (MPart) window.getChildren().get(0);
		toolBar = part.getToolbar();

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(before, toolBar.isToBeRendered());

		applyAll(deltas);

		assertEquals(after, toolBar.isToBeRendered());
	}

	public void testPart_ToolBar_ToBeRendered_TrueTrue() {
		testPart_ToolBar_ToBeRendered(true, true);
	}

	public void testPart_ToolBar_ToBeRendered_TrueFalse() {
		testPart_ToolBar_ToBeRendered(true, false);
	}

	public void testPart_ToolBar_ToBeRendered_FalseTrue() {
		testPart_ToolBar_ToBeRendered(false, true);
	}

	public void testPart_ToolBar_ToBeRendered_FalseFalse() {
		testPart_ToolBar_ToBeRendered(false, false);
	}
}

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
import org.eclipse.e4.workbench.modeling.ModelDeltaOperation;
import org.eclipse.e4.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerPartTest extends ModelReconcilerTest {

	public void testPart_Menus_Add() {
		String applicationId = createId();
		String windowId = createId();
		String partId = createId();
		String menuId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		part.setId(partId);
		window.getChildren().add(part);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MMenu menu = MApplicationFactory.eINSTANCE.createMenu();
		menu.setId(menuId);
		part.getMenus().add(menu);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		part = MApplicationFactory.eINSTANCE.createPart();
		part.setId(partId);
		window.getChildren().add(part);

		Collection<ModelDeltaOperation> operations = applyDeltas(application,
				state);

		assertEquals(0, part.getMenus().size());

		applyAll(operations);

		assertEquals(1, part.getMenus().size());

		menu = part.getMenus().get(0);
		assertEquals(menuId, menu.getId());
	}

	public void testPart_Menus_Remove() {
		String applicationId = createId();
		String windowId = createId();
		String partId = createId();
		String menuId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		part.setId(partId);
		window.getChildren().add(part);

		MMenu menu = MApplicationFactory.eINSTANCE.createMenu();
		menu.setId(menuId);
		part.getMenus().add(menu);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		part.getMenus().remove(menu);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		part = MApplicationFactory.eINSTANCE.createPart();
		part.setId(partId);
		window.getChildren().add(part);

		menu = MApplicationFactory.eINSTANCE.createMenu();
		menu.setId(menuId);
		part.getMenus().add(menu);

		Collection<ModelDeltaOperation> operations = applyDeltas(application,
				state);

		assertEquals(1, part.getMenus().size());
		assertEquals(menu, part.getMenus().get(0));
		assertEquals(menuId, part.getMenus().get(0).getId());

		applyAll(operations);

		assertEquals(0, part.getMenus().size());
	}

	private void testPart_Menu_Visible(boolean before, boolean after) {
		String applicationId = createId();
		String windowId = createId();
		String partId = createId();
		String menuId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		part.setId(partId);
		window.getChildren().add(part);

		MMenu menu = MApplicationFactory.eINSTANCE.createMenu();
		menu.setVisible(before);
		menu.setId(menuId);
		part.getMenus().add(menu);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		application.setFactory(new Object());
		menu.setVisible(after);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		part = MApplicationFactory.eINSTANCE.createPart();
		part.setId(partId);
		window.getChildren().add(part);

		menu = MApplicationFactory.eINSTANCE.createMenu();
		menu.setVisible(before);
		menu.setId(menuId);
		part.getMenus().add(menu);

		Collection<ModelDeltaOperation> operations = applyDeltas(application,
				state);

		assertEquals(before, menu.isVisible());

		applyAll(operations);

		assertEquals(after, menu.isVisible());
	}

	public void testPart_Menu_Visible_TrueTrue() {
		testPart_Menu_Visible(true, true);
	}

	public void testPart_Menu_Visible_TrueFalse() {
		testPart_Menu_Visible(true, false);
	}

	public void testPart_Menu_Visible_FalseTrue() {
		testPart_Menu_Visible(false, true);
	}

	public void testPart_Menu_Visible_FalseFalse() {
		testPart_Menu_Visible(false, false);
	}

	private void testPart_ToolBar_Visible(boolean before, boolean after) {
		String applicationId = createId();
		String windowId = createId();
		String partId = createId();
		String toolBarId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MPart part = MApplicationFactory.eINSTANCE.createPart();
		part.setId(partId);
		window.getChildren().add(part);

		MToolBar toolBar = MApplicationFactory.eINSTANCE.createToolBar();
		toolBar.setVisible(before);
		toolBar.setId(toolBarId);
		part.setToolbar(toolBar);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		application.setFactory(new Object());
		toolBar.setVisible(after);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		part = MApplicationFactory.eINSTANCE.createPart();
		part.setId(partId);
		window.getChildren().add(part);

		toolBar = MApplicationFactory.eINSTANCE.createToolBar();
		toolBar.setVisible(before);
		toolBar.setId(toolBarId);
		part.setToolbar(toolBar);

		Collection<ModelDeltaOperation> operations = applyDeltas(application,
				state);

		assertEquals(before, toolBar.isVisible());

		applyAll(operations);

		assertEquals(after, toolBar.isVisible());
	}

	public void testPart_ToolBar_Visible_TrueTrue() {
		testPart_ToolBar_Visible(true, true);
	}

	public void testPart_ToolBar_Visible_TrueFalse() {
		testPart_ToolBar_Visible(true, false);
	}

	public void testPart_ToolBar_Visible_FalseTrue() {
		testPart_ToolBar_Visible(false, true);
	}

	public void testPart_ToolBar_Visible_FalseFalse() {
		testPart_ToolBar_Visible(false, false);
	}
}

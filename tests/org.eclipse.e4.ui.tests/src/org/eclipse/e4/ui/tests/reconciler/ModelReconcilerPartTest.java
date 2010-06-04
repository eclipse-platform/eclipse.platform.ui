/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
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
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuFactoryImpl;
import org.eclipse.e4.ui.workbench.modeling.ModelDelta;
import org.eclipse.e4.ui.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerPartTest extends ModelReconcilerTest {

	public void testPart_Menus_Add() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(part);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MMenu menu = MenuFactoryImpl.eINSTANCE.createMenu();
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

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(part);

		MMenu menu = MenuFactoryImpl.eINSTANCE.createMenu();
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

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(part);

		MMenu menu = MenuFactoryImpl.eINSTANCE.createMenu();
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

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(part);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MToolBar toolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
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

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(part);

		MToolBar toolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
		part.setToolbar(toolBar);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		part.setToolbar(null);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		part = (MPart) window.getChildren().get(0);

		toolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
		part.setToolbar(toolBar);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(toolBar, part.getToolbar());

		applyAll(deltas);

		assertNull(part.getToolbar());
	}

	private void testPart_ToolBar_ToBeRendered(boolean before, boolean after) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(part);

		MToolBar toolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
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

	public void testPart_NewWithToolBar() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(part);

		MToolBar toolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
		part.setToolbar(toolBar);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(0, window.getChildren().size());

		applyAll(deltas);

		assertEquals(1, window.getChildren().size());

		part = (MPart) window.getChildren().get(0);
		assertNotNull(part.getToolbar());
	}

	public void testPart_NewWithToolBar2() {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part1 = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(part1);

		MToolBar toolBar = MenuFactoryImpl.eINSTANCE.createToolBar();
		part1.setToolbar(toolBar);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MPart part2 = BasicFactoryImpl.eINSTANCE.createPart();
		window.getChildren().add(part2);

		part2.setToolbar(toolBar);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		part1 = (MPart) window.getChildren().get(0);
		toolBar = part1.getToolbar();

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(1, window.getChildren().size());
		assertEquals(part1, window.getChildren().get(0));
		assertEquals(toolBar, part1.getToolbar());

		applyAll(deltas);

		assertEquals(2, window.getChildren().size());
		assertEquals(part1, window.getChildren().get(0));
		assertNull(part1.getToolbar());

		part2 = (MPart) window.getChildren().get(1);
		assertEquals(toolBar, part2.getToolbar());
	}

	private void testPart_Closeable(boolean before, boolean after) {
		MApplication application = createApplication();

		MWindow window = createWindow(application);

		MPart part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setCloseable(before);
		window.getChildren().add(part);

		saveModel();

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		part.setCloseable(after);

		Object state = reconciler.serialize();

		application = createApplication();
		window = application.getChildren().get(0);
		part = (MPart) window.getChildren().get(0);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(before, part.isCloseable());

		applyAll(deltas);

		assertEquals(after, part.isCloseable());
	}

	public void testPart_Closeable_TrueTrue() {
		testPart_Closeable(true, true);
	}

	public void testPart_Closeable_TrueFalse() {
		testPart_Closeable(true, false);
	}

	public void testPart_Closeable_FalseTrue() {
		testPart_Closeable(false, true);
	}

	public void testPart_Closeable_FalseFalse() {
		testPart_Closeable(false, false);
	}
}

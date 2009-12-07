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
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.workbench.modeling.ModelDelta;
import org.eclipse.e4.workbench.modeling.ModelReconciler;

public abstract class ModelReconcilerWindowTest extends ModelReconcilerTest {

	// public void testWindow() {
	// String applicationId = createId();
	// String windowId = createId();
	//
	// MApplication application = createApplication();
	// application.setId(applicationId);
	//
	// MWindow window = createWindow(application);
	// window.setId(windowId);
	// window.setX(100);
	//
	// XMLResourceImpl resource = new XMLResourceImpl() {
	// @Override
	// protected boolean useUUIDs() {
	// return true;
	// }
	// };
	// resource.getContents().add((EObject) application);
	//
	// String id = resource.getID((EObject) application);
	// System.out.print(id + "\t");
	// id = resource.getID((EObject) window);
	// System.out.println(id);
	//
	// resource = new XMLResourceImpl() {
	// @Override
	// protected boolean useUUIDs() {
	// return true;
	// }
	// };
	// resource.getContents().add((EObject) application);
	//
	// id = resource.getID((EObject) application);
	// System.out.print(id + "\t");
	// id = resource.getID((EObject) window);
	// System.out.println(id);
	// }

	public void testWindow_X() {
		String applicationId = createId();
		String windowId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);
		window.setX(100);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.setX(200);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);
		window.setX(100);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(100, window.getX());

		applyAll(deltas);

		assertEquals(200, window.getX());
	}

	public void testWindow_Y() {
		String applicationId = createId();
		String windowId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);
		window.setY(100);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.setY(200);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);
		window.setY(100);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(100, window.getY());

		applyAll(deltas);

		assertEquals(200, window.getY());
	}

	public void testWindow_Width() {
		String applicationId = createId();
		String windowId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);
		window.setWidth(100);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.setWidth(200);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);
		window.setWidth(100);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(100, window.getWidth());

		applyAll(deltas);

		assertEquals(200, window.getWidth());
	}

	public void testWindow_Height() {
		String applicationId = createId();
		String windowId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);
		window.setHeight(100);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.setHeight(200);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);
		window.setHeight(100);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(100, window.getHeight());

		applyAll(deltas);

		assertEquals(200, window.getHeight());
	}

	public void testMenu_Set() {
		String applicationId = createId();
		String windowId = createId();
		String menuId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		MMenu menu = MApplicationFactory.eINSTANCE.createMenu();
		menu.setId(menuId);
		window.setMainMenu(menu);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertNull(window.getMainMenu());

		applyAll(deltas);

		menu = window.getMainMenu();
		assertNotNull(menu);
		assertEquals(menuId, menu.getId());
	}

	public void testMenu_Unset() {
		String applicationId = createId();
		String windowId = createId();
		String menuId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MMenu menu = MApplicationFactory.eINSTANCE.createMenu();
		menu.setId(menuId);
		window.setMainMenu(menu);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		window.setMainMenu(null);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		menu = MApplicationFactory.eINSTANCE.createMenu();
		menu.setId(menuId);
		window.setMainMenu(menu);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(menu, window.getMainMenu());
		assertEquals(menuId, window.getMainMenu().getId());

		applyAll(deltas);

		assertNull(window.getMainMenu());
	}

	private void testMenu_Visible(boolean before, boolean after) {
		String applicationId = createId();
		String windowId = createId();
		String menuId = createId();

		MApplication application = createApplication();
		application.setId(applicationId);

		MWindow window = createWindow(application);
		window.setId(windowId);

		MMenu menu = MApplicationFactory.eINSTANCE.createMenu();
		menu.setId(menuId);
		menu.setToBeRendered(before);
		window.setMainMenu(menu);

		ModelReconciler reconciler = createModelReconciler();
		reconciler.recordChanges(application);

		menu.setToBeRendered(after);

		Object state = reconciler.serialize();

		application = createApplication();
		application.setId(applicationId);

		window = createWindow(application);
		window.setId(windowId);

		menu = MApplicationFactory.eINSTANCE.createMenu();
		menu.setId(menuId);
		menu.setToBeRendered(before);
		window.setMainMenu(menu);

		Collection<ModelDelta> deltas = constructDeltas(application, state);

		assertEquals(before, menu.isToBeRendered());

		applyAll(deltas);

		assertEquals(after, menu.isToBeRendered());
	}

	public void testMenu_Visible_TrueTrue() {
		testMenu_Visible(true, true);
	}

	public void testMenu_Visible_TrueFalse() {
		testMenu_Visible(true, false);
	}

	public void testMenu_Visible_FalseTrue() {
		testMenu_Visible(false, true);
	}

	public void testMenu_Visible_FalseFalse() {
		testMenu_Visible(false, false);
	}
}
